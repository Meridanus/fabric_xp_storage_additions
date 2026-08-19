package com.notker.xps_additions.regestry;

import com.notker.xp_storage.regestry.ModItems;
import com.notker.xps_additions.XpsAdditions;
import com.notker.xps_additions.items.Mystical_Candy;
import com.notker.xps_additions.items.StaffOfRebark;
import com.notker.xps_additions.items.Street_Item;
import dev.architectury.registry.fuel.FuelRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Rarity;

import java.util.function.Supplier;

public class AdditionItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(XpsAdditions.MOD_ID, RegistryKeys.ITEM);

    /** Everything goes into the creative tab of the XP Obelisk base mod ({@code xps:general}). */
    private static Item.Settings newSettings() {
        return new Item.Settings().arch$tab(ModItems.XP_TAB);
    }

    private static Supplier<BlockItem> blockItem(Supplier<? extends Block> block) {
        return () -> new BlockItem(block.get(), newSettings());
    }

    private static Supplier<BlockItem> blockItem(Supplier<? extends Block> block, Rarity rarity) {
        return () -> new BlockItem(block.get(), newSettings().rarity(rarity));
    }

    // Items. Registration order is the order they show up in the creative tab.
    public static final RegistrySupplier<Item> MYSTICAL_CANDY = ITEMS.register("mystical_candy", Mystical_Candy::new);

    public static final RegistrySupplier<Item> RAW_ESSENCE = ITEMS.register("raw_essence",
            () -> new Item(newSettings().rarity(Rarity.RARE)));

    public static final RegistrySupplier<Item> RAW_ESSENCE_SHARD = ITEMS.register("raw_essence_shard",
            () -> new Item(newSettings().rarity(Rarity.RARE)));

    public static final RegistrySupplier<Item> STAFF_OF_REBARK = ITEMS.register("staff_of_rebark",
            () -> new StaffOfRebark(newSettings().rarity(Rarity.EPIC).maxDamage(2880)));

    public static final RegistrySupplier<Item> ESSENCE_CRYSTAL = ITEMS.register("essence_crystal",
            () -> new Item(newSettings().rarity(Rarity.EPIC)));

    //Block Items
    public static final RegistrySupplier<BlockItem> SOUL_COPPER_DOOR_ITEM =
            ITEMS.register("soul_copper_door", blockItem(AdditionBlocks.SOUL_COPPER_DOOR));

    public static final RegistrySupplier<BlockItem> SOUL_COPPER_TRAP_DOOR_ITEM =
            ITEMS.register("soul_copper_trap_door", blockItem(AdditionBlocks.SOUL_COPPER_TRAP_DOOR));

    public static final RegistrySupplier<BlockItem> SOUL_COPPER_PRESSURE_PLATE_ITEM =
            ITEMS.register("soul_copper_pressure_plate", blockItem(AdditionBlocks.SOUL_COPPER_PRESSURE_PLATE));

    public static final RegistrySupplier<BlockItem> SOUL_COPPER_BARS_ITEM =
            ITEMS.register("soul_copper_bars", blockItem(AdditionBlocks.SOUL_COPPER_BARS));

    public static final RegistrySupplier<BlockItem> CUT_SOUL_COPPER_ITEM =
            ITEMS.register("cut_soul_copper", blockItem(AdditionBlocks.CUT_SOUL_COPPER));

    public static final RegistrySupplier<BlockItem> CUT_SOUL_COPPER_SLAB_ITEM =
            ITEMS.register("cut_soul_copper_slab", blockItem(AdditionBlocks.CUT_SOUL_COPPER_SLAB));

    public static final RegistrySupplier<BlockItem> CUT_SOUL_COPPER_STAIRS_ITEM =
            ITEMS.register("cut_soul_copper_stairs", blockItem(AdditionBlocks.CUT_SOUL_COPPER_STAIRS));

    public static final RegistrySupplier<BlockItem> XP_ITEM_INSERTER_ITEM =
            ITEMS.register("xp_item_inserter", blockItem(AdditionBlocks.XP_ITEM_INSERTER));

    public static final RegistrySupplier<BlockItem> RAW_ESSENCE_BLOCK_ITEM =
            ITEMS.register("raw_essence_block", blockItem(AdditionBlocks.RAW_ESSENCE_BLOCK, Rarity.RARE));

    public static final RegistrySupplier<BlockItem> STREET_ITEM =
            ITEMS.register("street", () -> new Street_Item(AdditionBlocks.STREET.get(), newSettings()));

    public static void registerItems() {
        ITEMS.register();
    }

    /** Runs once the registries are filled on both loaders. */
    public static void registerFuel() {
        FuelRegistry.register(XpsAdditions.RAW_ESSENCE_FUEL_DURATION, RAW_ESSENCE.get());
        FuelRegistry.register(XpsAdditions.RAW_ESSENCE_SHARD_FUEL_DURATION, RAW_ESSENCE_SHARD.get());
        FuelRegistry.register(XpsAdditions.RAW_ESSENCE_BLOCK_FUEL_DURATION, RAW_ESSENCE_BLOCK_ITEM.get());
    }
}
