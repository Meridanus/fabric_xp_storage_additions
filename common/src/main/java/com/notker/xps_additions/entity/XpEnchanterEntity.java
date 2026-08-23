package com.notker.xps_additions.entity;

import com.notker.xp_storage.XpStorage;
import com.notker.xp_storage.blocks.StorageBlockEntity;
import com.notker.xp_storage.regestry.ModBlocks;
import com.notker.xps_additions.XpEnchanting;
import com.notker.xps_additions.regestry.AdditionBlocks;
import com.notker.xps_additions.screen.ImplementedInventory;
import com.notker.xps_additions.screen.XpEnchanterScreenHandler;
import dev.architectury.registry.menu.ExtendedMenuProvider;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * An enchanting table that takes its XP out of an XP Obelisk standing next to it instead of out
 * of a player, so it can be driven by redstone and fed by hoppers.
 *
 * <p>See {@link XpEnchanting} for the cost model.
 */
public class XpEnchanterEntity extends BlockEntity implements ImplementedInventory, ExtendedMenuProvider {

    public static final int INPUT_SLOT = 0;
    public static final int LAPIS_SLOT = 1;
    public static final int OUTPUT_SLOT = 2;
    public static final int SIZE = 3;

    /** Hoppers and pipes see every slot, what they may do with them is decided per slot. */
    private static final int[] AVAILABLE_SLOTS = {INPUT_SLOT, LAPIS_SLOT, OUTPUT_SLOT};

    /** The obelisk XP and the status are only needed for the GUI, no reason to look them up every tick. */
    private static final int DISPLAY_REFRESH_TICKS = 5;

    private static final String ENCHANT_LEVEL_KEY = "EnchantLevel";

    private final DefaultedList<ItemStack> items = DefaultedList.ofSize(SIZE, ItemStack.EMPTY);

    /** Persisted in NBT so an automated enchanter keeps its setting over a reload. */
    private int enchantLevel = XpEnchanting.DEFAULT_LEVEL;

    // Display only, recomputed by the ticker and pushed to the open GUI through the property delegate
    private int obeliskXp;
    private int statusId = XpEnchanting.Status.NO_OBELISK.ordinal();

    // A roll that came up empty is remembered so the GUI can keep saying "nothing applicable"
    // instead of flickering back to "ready" on the next display refresh.
    @Nullable
    private Item nothingApplicableItem;
    private int nothingApplicableLevel;

    /**
     * What the clients were last told sits in the input slot, so {@link #syncIfInputChanged()} can
     * tell a real change from the many other reasons this block entity is marked dirty.
     */
    private ItemStack lastSyncedInput = ItemStack.EMPTY;

    /**
     * Screen handler properties travel as signed shorts, so the obelisk XP goes over the wire in
     * two halves and {@code XpEnchanterScreenHandler#getObeliskXp} puts it back together. A full
     * obelisk holds far more than 32767 XP, and the header bar would show a nonsense level for it.
     */
    private final PropertyDelegate propertyDelegate = new PropertyDelegate() {
        @Override
        public int get(int index) {
            return switch (index) {
                case XpEnchanterScreenHandler.PROPERTY_LEVEL -> enchantLevel;
                case XpEnchanterScreenHandler.PROPERTY_OBELISK_XP_LOW -> obeliskXp & 0xFFFF;
                case XpEnchanterScreenHandler.PROPERTY_STATUS -> statusId;
                case XpEnchanterScreenHandler.PROPERTY_OBELISK_XP_HIGH -> (obeliskXp >>> 16) & 0xFFFF;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case XpEnchanterScreenHandler.PROPERTY_LEVEL -> enchantLevel = value;
                case XpEnchanterScreenHandler.PROPERTY_STATUS -> statusId = value;
                default -> {
                    // the obelisk xp is display only and never set from the outside
                }
            }
        }

        @Override
        public int size() {
            return XpEnchanterScreenHandler.PROPERTY_COUNT;
        }
    };

    public XpEnchanterEntity(BlockPos pos, BlockState state) {
        super(AdditionBlocks.XP_ENCHANTER_ENTITY.get(), pos, state);
    }

    // --- inventory ------------------------------------------------------------------------------

    @Override
    public DefaultedList<ItemStack> getItems() {
        return items;
    }

    @Override
    public int size() {
        return SIZE;
    }

    /**
     * What the input slot takes: books (they turn into enchanted books) and anything the vanilla
     * table would accept. {@link ItemStack#isEnchantable()} is already false for items that carry
     * enchantments, so enchanted gear can not be fed back in - same rule as the vanilla table.
     */
    public static boolean isEnchantInput(ItemStack stack) {
        return stack.isOf(Items.BOOK) || stack.isEnchantable();
    }

    @Override
    public int[] getAvailableSlots(@Nullable Direction side) {
        return AVAILABLE_SLOTS;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction side) {
        return isValid(slot, stack);
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, @Nullable Direction side) {
        // Only the finished item may leave, otherwise a hopper below would drain the lapis again
        return slot == OUTPUT_SLOT;
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        return switch (slot) {
            case INPUT_SLOT -> isEnchantInput(stack);
            case LAPIS_SLOT -> stack.isOf(Items.LAPIS_LAZULI);
            default -> false;
        };
    }

    /** Keeps the GUI from being used from far away or after the block was removed. */
    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return Inventory.canPlayerUse(this, player);
    }

    /**
     * The item the block shows hovering above itself, see {@code XpEnchanterRenderer}. This is the
     * only slot that is worth sending to clients, everything else is behind the GUI where the
     * screen handler already keeps it up to date.
     */
    public ItemStack getRenderStack() {
        return getStack(INPUT_SLOT);
    }

    // --- client sync ----------------------------------------------------------------------------

    /**
     * Ask the server to resend this block entity to everyone who can see it
     * (flushUpdates -> {@link #toUpdatePacket()}). Never call this from writeNbt!
     */
    private void sync() {
        if (world != null && !world.isClient) {
            world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_LISTENERS);
        }
    }

    /**
     * Sends an update packet only when the hovering item actually changed its look. The enchanter
     * is marked dirty for plenty of reasons the clients do not care about - lapis being consumed,
     * the level being turned up, the output being pulled out - and every sync is a packet to every
     * player in view distance.
     */
    private void syncIfInputChanged() {
        if (world == null || world.isClient) {
            return;
        }
        ItemStack input = getStack(INPUT_SLOT);
        if (!ItemStack.areEqual(lastSyncedInput, input)) {
            // A copy, otherwise this would alias the live stack and never see a count change again
            lastSyncedInput = input.copy();
            sync();
        }
    }

    @Override
    public void markDirty() {
        super.markDirty();
        syncIfInputChanged();
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    /** The whole NBT, so a chunk arriving at a client already carries the item to show. */
    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registries) {
        return createNbt(registries);
    }

    // --- settings -------------------------------------------------------------------------------

    public int getEnchantLevel() {
        return enchantLevel;
    }

    /** Moves the configured level by {@code delta}, clamped to 1..30. Server side only. */
    public void adjustLevel(int delta) {
        int next = XpEnchanting.clampLevel(enchantLevel + delta);
        if (next != enchantLevel) {
            enchantLevel = next;
            forgetEmptyRoll();
            // Straight away, so the status line reacts to the click instead of to the next refresh
            StorageBlockEntity obelisk = findObelisk();
            refreshDisplay(obelisk, displayStatus(obelisk));
            markDirty();
        }
    }

    // --- persistence ----------------------------------------------------------------------------

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        items.clear();
        Inventories.readNbt(nbt, items, registries);
        enchantLevel = nbt.contains(ENCHANT_LEVEL_KEY)
                ? XpEnchanting.clampLevel(nbt.getInt(ENCHANT_LEVEL_KEY))
                : XpEnchanting.DEFAULT_LEVEL;
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        Inventories.writeNbt(nbt, items, registries);
        nbt.putInt(ENCHANT_LEVEL_KEY, enchantLevel);
        super.writeNbt(nbt, registries);
    }

    // --- obelisk --------------------------------------------------------------------------------

    /**
     * The first XP Obelisk touching this block, in {@link Direction#values()} order. Cheap enough
     * to look up whenever it is needed, so the enchanter never holds on to a stale position.
     */
    @Nullable
    private StorageBlockEntity findObelisk() {
        if (world == null) {
            return null;
        }

        Direction facing = this.getCachedState().get(Properties.HORIZONTAL_FACING);
        BlockPos pos = this.getPos().offset(facing, 1);
        Optional<StorageBlockEntity> obelisk = world.getBlockEntity(pos, ModBlocks.STORAGE_BLOCK_ENTITY.get());

        if (obelisk.isPresent()) {
            return obelisk.get();
        }

        return null;
    }

    // --- status ---------------------------------------------------------------------------------

    /** Everything that has to be true before an enchant can happen, in the order it is reported. */
    private XpEnchanting.Status computeStatus(@Nullable StorageBlockEntity obelisk) {
        if (obelisk == null) {
            return XpEnchanting.Status.NO_OBELISK;
        }
        if (!obelisk.canExtract()) {
            return XpEnchanting.Status.LOCKED;
        }

        ItemStack input = getStack(INPUT_SLOT);
        if (input.isEmpty()) {
            return XpEnchanting.Status.NO_ITEM;
        }
        if (!isEnchantInput(input)) {
            return XpEnchanting.Status.NOT_ENCHANTABLE;
        }
        if (!getStack(OUTPUT_SLOT).isEmpty()) {
            return XpEnchanting.Status.OUTPUT_FULL;
        }

        ItemStack lapis = getStack(LAPIS_SLOT);
        if (!lapis.isOf(Items.LAPIS_LAZULI) || lapis.getCount() < XpEnchanting.tier(enchantLevel)) {
            return XpEnchanting.Status.NO_LAPIS;
        }
        if (obelisk.getContainerExperience() < XpEnchanting.requiredXp(enchantLevel)) {
            return XpEnchanting.Status.NO_XP;
        }
        return XpEnchanting.Status.READY;
    }

    public boolean getXpEnchantingStatus() {
        StorageBlockEntity obelisk = findObelisk();
        if (obelisk == null) return false;

        XpEnchanting.Status status = computeStatus(obelisk);
        return status == XpEnchanting.Status.READY;
    }

    /**
     * What the GUI shows. Same as {@link #computeStatus}, except that an empty roll stays visible
     * until the item or the level changes. The next trigger still tries again: the rolled power
     * varies, so a second attempt on the same item can very well find something.
     */
    private XpEnchanting.Status displayStatus(@Nullable StorageBlockEntity obelisk) {
        XpEnchanting.Status status = computeStatus(obelisk);
        if (status == XpEnchanting.Status.READY
                && getStack(INPUT_SLOT).getItem() == nothingApplicableItem
                && enchantLevel == nothingApplicableLevel) {
            return XpEnchanting.Status.NOTHING;
        }
        return status;
    }

    private void rememberEmptyRoll(Item item) {
        nothingApplicableItem = item;
        nothingApplicableLevel = enchantLevel;
    }

    private void forgetEmptyRoll() {
        nothingApplicableItem = null;
        nothingApplicableLevel = 0;
    }

    private void refreshDisplay(@Nullable StorageBlockEntity obelisk, XpEnchanting.Status status) {
        obeliskXp = obelisk == null ? 0 : obelisk.getContainerExperience();
        statusId = status.ordinal();
    }

    // --- ticking --------------------------------------------------------------------------------

    /** Server side only, see {@code XpEnchanter#getTicker}. Just keeps the GUI numbers fresh. */
    public static void tick(World world, BlockPos pos, BlockState state, XpEnchanterEntity entity) {
        // A compare against one stack, cheap enough to do every tick. Not every way the input slot
        // can change runs through markDirty - hoppers and the loader item handlers write the list
        // directly, and so does /item replace - so this is what guarantees the hovering item
        // eventually catches up no matter who moved it.
        entity.syncIfInputChanged();

        if (world.getTime() % DISPLAY_REFRESH_TICKS != 0) {
            return;
        }
        StorageBlockEntity obelisk = entity.findObelisk();
        entity.refreshDisplay(obelisk, entity.displayStatus(obelisk));
    }

    // --- enchanting -----------------------------------------------------------------------------

    /**
     * Runs one enchant, exactly like a player clicking the chosen slot of a vanilla table.
     * Called by the redstone trigger and by the GUI button, at most once per trigger.
     *
     * @return true when an item was enchanted
     */
    public boolean tryEnchant() {
        if (world == null || world.isClient) {
            return false;
        }

        StorageBlockEntity obelisk = findObelisk();
        XpEnchanting.Status status = computeStatus(obelisk);
        refreshDisplay(obelisk, status);
        if (status != XpEnchanting.Status.READY || obelisk == null) {
            return false;
        }

        int level = enchantLevel;
        int tier = XpEnchanting.tier(level);
        long costDroplets = (long) XpEnchanting.costXp(level) * XpStorage.MB_PER_XP;

        // Simulate first: this is what respects a locked obelisk, so nothing is taken when it refuses
        if (obelisk.extractDroplets(costDroplets, true) != costDroplets) {
            refreshDisplay(obelisk, XpEnchanting.Status.LOCKED);
            return false;
        }

        ItemStack input = getStack(INPUT_SLOT);
        Random random = world.getRandom();
        Optional<RegistryEntryList.Named<Enchantment>> pool = world.getRegistryManager()
                .get(RegistryKeys.ENCHANTMENT)
                .getEntryList(EnchantmentTags.IN_ENCHANTING_TABLE);
        if (pool.isEmpty()) {
            rememberEmptyRoll(input.getItem());
            refreshDisplay(obelisk, XpEnchanting.Status.NOTHING);
            return false;
        }

        // One item at a time, and the roll sees the original stack so books get the book treatment
        ItemStack result = input.copyWithCount(1);
        List<EnchantmentLevelEntry> enchantments =
                EnchantmentHelper.generateEnchantments(random, result, level, pool.get().stream());

        boolean book = input.isOf(Items.BOOK);
        if (book && enchantments.size() > 1) {
            // Vanilla throws one of the rolled enchantments away when the table enchants a book
            enchantments.remove(random.nextInt(enchantments.size()));
        }
        if (enchantments.isEmpty()) {
            rememberEmptyRoll(input.getItem());
            refreshDisplay(obelisk, XpEnchanting.Status.NOTHING);
            return false;
        }

        if (book) {
            result = new ItemStack(Items.ENCHANTED_BOOK);
        }
        for (EnchantmentLevelEntry entry : enchantments) {
            result.addEnchantment(entry.enchantment, entry.level);
        }

        obelisk.extractDroplets(costDroplets, false);
        removeStack(INPUT_SLOT, 1);
        removeStack(LAPIS_SLOT, tier);
        setStack(OUTPUT_SLOT, result);
        forgetEmptyRoll();
        markDirty();

        world.playSound(null, pos, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS,
                1.0f, world.getRandom().nextFloat() * 0.1f + 0.9f);
        if (world instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(ParticleTypes.ENCHANT,
                    pos.getX() + 0.5, pos.getY() + 0.9, pos.getZ() + 0.5, 12, 0.4, 0.2, 0.4, 0.2);
        }

        refreshDisplay(obelisk, displayStatus(obelisk));
        return true;
    }

    // --- menu -----------------------------------------------------------------------------------

    @Override
    public void saveExtraData(PacketByteBuf buf) {
        buf.writeBlockPos(pos);
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("block.xps_additions.xp_enchanter");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new XpEnchanterScreenHandler(syncId, inv, this, propertyDelegate);
    }
}
