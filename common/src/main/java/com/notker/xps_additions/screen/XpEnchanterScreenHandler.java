package com.notker.xps_additions.screen;

import com.notker.xps_additions.XpEnchanting;
import com.notker.xps_additions.entity.XpEnchanterEntity;
import com.notker.xps_additions.regestry.AdditionMenus;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.Nullable;

/**
 * The three enchanter slots plus the player inventory, and the buttons that change the configured
 * level or start an enchant. The block entity does the actual work, this only forwards the clicks.
 */
public class XpEnchanterScreenHandler extends ScreenHandler {

    public static final int BUTTON_LEVEL_DOWN = 0;
    public static final int BUTTON_LEVEL_UP = 1;
    public static final int BUTTON_LEVEL_DOWN_10 = 2;
    public static final int BUTTON_LEVEL_UP_10 = 3;
    public static final int BUTTON_ENCHANT = 4;

    /** How far a shift click moves the level. */
    public static final int LEVEL_STEP_LARGE = 10;

    // Property delegate layout. Values travel as signed shorts, so the obelisk XP needs two slots.
    public static final int PROPERTY_LEVEL = 0;
    public static final int PROPERTY_OBELISK_XP_LOW = 1;
    public static final int PROPERTY_STATUS = 2;
    public static final int PROPERTY_OBELISK_XP_HIGH = 3;
    public static final int PROPERTY_COUNT = 4;

    private static final int CONTAINER_SLOTS = XpEnchanterEntity.SIZE;

    private final Inventory inventory;
    private final PropertyDelegate propertyDelegate;

    /** Only set server side: the client copy has nothing to act on. */
    @Nullable
    private final XpEnchanterEntity enchanter;

    /** Client side: an empty stand-in inventory, the contents arrive through the slot sync. */
    public XpEnchanterScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        this(syncId, playerInventory, new SimpleInventory(CONTAINER_SLOTS), new ArrayPropertyDelegate(PROPERTY_COUNT), null);
        // Written by XpEnchanterEntity#saveExtraData, the screen itself does not need it
        buf.readBlockPos();
    }

    public XpEnchanterScreenHandler(int syncId, PlayerInventory playerInventory, XpEnchanterEntity enchanter, PropertyDelegate propertyDelegate) {
        this(syncId, playerInventory, enchanter, propertyDelegate, enchanter);
    }

    private XpEnchanterScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory,
                                     PropertyDelegate propertyDelegate, @Nullable XpEnchanterEntity enchanter) {
        super(AdditionMenus.XP_ENCHANTER_SCREEN_HANDLER.get(), syncId);
        checkSize(inventory, CONTAINER_SLOTS);

        this.inventory = inventory;
        this.propertyDelegate = propertyDelegate;
        this.enchanter = enchanter;

        inventory.onOpen(playerInventory.player);

        addSlot(new InputSlot(inventory, XpEnchanterEntity.INPUT_SLOT, 26, 38));
        addSlot(new LapisSlot(inventory, XpEnchanterEntity.LAPIS_SLOT, 44, 38));
        addSlot(new OutputSlot(inventory, XpEnchanterEntity.OUTPUT_SLOT, 125, 38));

        // Standard 176x166 container body: 27 inventory slots above the hotbar
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }

        addProperties(propertyDelegate);
    }

    // --- synced values --------------------------------------------------------------------------

    public int getEnchantLevel() {
        return propertyDelegate.get(PROPERTY_LEVEL);
    }

    /** Both halves arrive sign extended from a short, the masks turn them back into 16 bits each. */
    public int getObeliskXp() {
        return ((propertyDelegate.get(PROPERTY_OBELISK_XP_HIGH) & 0xFFFF) << 16)
                | (propertyDelegate.get(PROPERTY_OBELISK_XP_LOW) & 0xFFFF);
    }

    public XpEnchanting.Status getStatus() {
        return XpEnchanting.Status.byId(propertyDelegate.get(PROPERTY_STATUS));
    }

    // --- interaction ----------------------------------------------------------------------------

    @Override
    public boolean onButtonClick(PlayerEntity player, int id) {
        if (enchanter == null) {
            return false;
        }
        switch (id) {
            case BUTTON_LEVEL_DOWN -> enchanter.adjustLevel(-1);
            case BUTTON_LEVEL_UP -> enchanter.adjustLevel(1);
            case BUTTON_LEVEL_DOWN_10 -> enchanter.adjustLevel(-LEVEL_STEP_LARGE);
            case BUTTON_LEVEL_UP_10 -> enchanter.adjustLevel(LEVEL_STEP_LARGE);
            case BUTTON_ENCHANT -> enchanter.tryEnchant();
            default -> {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        ItemStack movedStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);

        if (slot.hasStack()) {
            ItemStack slotStack = slot.getStack();
            movedStack = slotStack.copy();

            if (slotIndex < CONTAINER_SLOTS) {
                // Out of the enchanter, into the player inventory
                if (!insertItem(slotStack, CONTAINER_SLOTS, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (slotStack.isOf(Items.LAPIS_LAZULI)) {
                if (!insertItem(slotStack, XpEnchanterEntity.LAPIS_SLOT, XpEnchanterEntity.LAPIS_SLOT + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (XpEnchanterEntity.isEnchantInput(slotStack)) {
                if (!insertItem(slotStack, XpEnchanterEntity.INPUT_SLOT, XpEnchanterEntity.INPUT_SLOT + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                return ItemStack.EMPTY;
            }

            if (slotStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }

            if (slotStack.getCount() == movedStack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTakeItem(player, slotStack);
        }

        return movedStack;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return inventory.canPlayerUse(player);
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        inventory.onClose(player);
    }

    // --- slots ----------------------------------------------------------------------------------

    /** Books and anything the vanilla table would enchant. */
    private static class InputSlot extends Slot {
        InputSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return XpEnchanterEntity.isEnchantInput(stack);
        }
    }

    private static class LapisSlot extends Slot {
        LapisSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return stack.isOf(Items.LAPIS_LAZULI);
        }
    }

    /** Result only, nothing goes in by hand. */
    private static class OutputSlot extends Slot {
        OutputSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return false;
        }
    }
}
