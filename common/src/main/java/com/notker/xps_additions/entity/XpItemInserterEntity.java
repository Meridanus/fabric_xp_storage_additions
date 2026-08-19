package com.notker.xps_additions.entity;

import com.notker.xp_storage.XpStorage;
import com.notker.xp_storage.blocks.StorageBlockEntity;
import com.notker.xp_storage.regestry.ModBlocks;
import com.notker.xp_storage.regestry.ModItems;
import com.notker.xps_additions.XpsAdditions;
import com.notker.xps_additions.regestry.AdditionBlocks;
import com.notker.xps_additions.screen.BoxScreenHandler;
import com.notker.xps_additions.screen.ImplementedInventory;
import dev.architectury.registry.menu.ExtendedMenuProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class XpItemInserterEntity extends BlockEntity implements ImplementedInventory, ExtendedMenuProvider {


    private int syncedInt;

    //PropertyDelegate is an interface which we will implement inline here.
    //It can normally contain multiple integers as data identified by the index, but in this example we only have one.
    private final PropertyDelegate propertyDelegate = new PropertyDelegate() {
        @Override
        public int get(int index) {
            return syncedInt;
        }

        @Override
        public void set(int index, int value) {
            syncedInt = value;
        }

        //this is supposed to return the amount of integers you have in your delegate, in our example only one
        @Override
        public int size() {
            return 1;
        }
    };



    private final DefaultedList<ItemStack> items = DefaultedList.ofSize(size(), ItemStack.EMPTY);
    public XpItemInserterEntity(BlockPos pos, BlockState state) {
        super(AdditionBlocks.XP_ITEM_INSERTER_ENTITY.get(), pos, state);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        items.clear();
        Inventories.readNbt(nbt, items, registries);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        Inventories.writeNbt(nbt, items, registries);
        super.writeNbt(nbt, registries);
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return items;
    }

    @Override
    public int size() {
        return XpsAdditions.ITEM_SLOTS;
    }

    /**
     * Only let hoppers / pipes push things in that the inserter can actually turn into XP,
     * otherwise a hopper line fills every slot with junk and the inserter stops working.
     */
    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction side) {
        return xpValueMb(stack) > 0;
    }

    /** Keeps the GUI from being used from far away or after the block was removed. */
    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return Inventory.canPlayerUse(this, player);
    }

    /** Liquid XP (in droplets) that the whole stack is worth, 0 if the item can not be inserted. */
    public static long xpValueMb(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        if (stack.isOf(ModItems.XP_BERRIES.get())) {
            return XpStorage.MB_PER_BERRIE * stack.getCount();
        }
        if (stack.isOf(ModItems.XP_BUCKET.get())) {
            return XpStorage.BUCKET * stack.getCount();
        }
        if (stack.isOf(Items.EXPERIENCE_BOTTLE)) {
            return XpStorage.BOTTLE * stack.getCount();
        }
        if (stack.isOf(Items.SCULK)) {
            return XpStorage.MB_PER_XP * stack.getCount();
        }
        return 0;
    }

    /** What is left in the slot after the stack got turned into XP (empty containers stay). */
    private static ItemStack remainderOf(ItemStack stack) {
        if (stack.isOf(ModItems.XP_BUCKET.get())) {
            return new ItemStack(Items.BUCKET, stack.getCount());
        }
        if (stack.isOf(Items.EXPERIENCE_BOTTLE)) {
            return new ItemStack(Items.GLASS_BOTTLE, stack.getCount());
        }
        return ItemStack.EMPTY;
    }


    @Override
    public void saveExtraData(PacketByteBuf buf) {
        buf.writeBlockPos(pos);
    }

    public static void tick(World world, BlockPos blockPos, BlockState blockState, XpItemInserterEntity entity) {

        Direction facing = blockState.get(Properties.HORIZONTAL_FACING);
        BlockPos pos = blockPos.offset(facing, 1);
        Optional<StorageBlockEntity> storage = world.getBlockEntity(pos, ModBlocks.STORAGE_BLOCK_ENTITY.get());

        if (storage.isEmpty()) {
            entity.syncedInt = 0;
            return;
        }

        entity.syncedInt = storage.get().getContainerExperience();
        if (world.isReceivingRedstonePower(blockPos)) {
            return;
        }

        // Find the first stack that can be converted, one stack per tick
        int slot = -1;
        long mbToInsert = 0;
        for (int i = 0; i < XpsAdditions.ITEM_SLOTS; i++) {
            mbToInsert = xpValueMb(entity.getItems().get(i));
            if (mbToInsert > 0) {
                slot = i;
                break;
            }
        }
        if (slot < 0) {
            return;
        }

        // Only take the items when the obelisk really accepts all the XP, so nothing gets lost
        StorageBlockEntity sbe = storage.get();
        long inserted = sbe.insertDroplets(mbToInsert, true);
        if (inserted == mbToInsert) {
            sbe.insertDroplets(mbToInsert, false);
            entity.getItems().set(slot, remainderOf(entity.getItems().get(slot)));
            entity.markDirty();
        }
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("block.xps_additions.xp_item_inserter");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new BoxScreenHandler(syncId, inv, this, propertyDelegate);
    }

}
