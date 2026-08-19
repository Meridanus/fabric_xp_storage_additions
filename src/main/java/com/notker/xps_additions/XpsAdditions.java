package com.notker.xps_additions;

import com.notker.xp_storage.XpStorage;
import com.notker.xps_additions.effects.GiggleStatusEffect;
import com.notker.xps_additions.items.StaffOfRebark;
import com.notker.xps_additions.regestry.AdditionBlocks;
import com.notker.xps_additions.regestry.AdditionItems;
import com.notker.xps_additions.screen.BoxScreenHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class XpsAdditions implements ModInitializer {

    public static final  String MOD_ID = "xps_additions";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final int XP_PER_MYSTICAL_CANDY = (XpStorage.XP_PER_BERRIE * 4);

    public static final int HASTE_EFFECT_DURATION = 1700;
    public static final float HASTE_EFFECT_CHANCE = 0.75f;
    public static final int HASTE_EFFECT_AMPLIFIER = 4;

    public static final int GIGGLE_EFFECT_DURATION = 200;
    public static final float GIGGLE_EFFECT_CHANCE = 0.25f;

    public static final int RAW_ESSENCE_BLOCK_FUEL_DURATION = 160 * 200; //160 Items * smelt time
    public static final int RAW_ESSENCE_FUEL_DURATION = RAW_ESSENCE_BLOCK_FUEL_DURATION / 5; //32 Items
    public static final int RAW_ESSENCE_SHARD_FUEL_DURATION = RAW_ESSENCE_FUEL_DURATION / 4; // 8 Items

    public static final float RUNNING_SPEED = 1.20F;

    public static final int ITEM_SLOTS = 9;

    // Creative tab of the XP Obelisk base mod (registered there as "xps:general"; the key object itself is private in the base mod)
    public static final RegistryKey<ItemGroup> XP_OBELISK_ITEM_GROUP = RegistryKey.of(RegistryKeys.ITEM_GROUP, new Identifier(XpStorage.MOD_ID, "general"));

    public static Identifier createModIdIdentifier (String path) {
        return new Identifier(MOD_ID, path);
    }


    public static final ScreenHandlerType<BoxScreenHandler> BOX_SCREEN_HANDLER = new ExtendedScreenHandlerType<>(BoxScreenHandler::new);

    public static final StatusEffect GIGGLE = new GiggleStatusEffect();


    @Override
    public void onInitialize() {
        AdditionBlocks.registerBlocks();
        AdditionItems.registerItems();
        Registry.register(Registries.STATUS_EFFECT, new Identifier(MOD_ID, "giggle"), GIGGLE);
        Registry.register(Registries.SCREEN_HANDLER, new Identifier(MOD_ID, "xp_item_inserter"), BOX_SCREEN_HANDLER);

        ItemGroupEvents.modifyEntriesEvent(XP_OBELISK_ITEM_GROUP).register(content -> {
            content.add(AdditionItems.MYSTICAL_CANDY);
            content.add(AdditionItems.RAW_ESSENCE);
            content.add(AdditionItems.RAW_ESSENCE_SHARD);
            content.add(AdditionItems.STAFF_OF_REBARK);
            content.add(AdditionItems.ESSENCE_CRYSTAL);

            content.add(AdditionItems.SOUL_COPPER_DOOR_ITEM);
            content.add(AdditionItems.SOUL_COPPER_TRAP_DOOR_ITEM);
            content.add(AdditionItems.SOUL_COPPER_PRESSURE_PLATE_ITEM);
            content.add(AdditionItems.SOUL_COPPER_BARS_ITEM);
            content.add(AdditionItems.CUT_SOUL_COPPER_ITEM);
            content.add(AdditionItems.CUT_SOUL_COPPER_SLAB_ITEM);
            content.add(AdditionItems.CUT_SOUL_COPPER_STAIRS_ITEM);
            content.add(AdditionItems.XP_ITEM_INSERTER_ITEM);
            content.add(AdditionItems.RAW_ESSENCE_BLOCK_ITEM);
            content.add(AdditionItems.STREET_ITEM);
        });


        // Build the stripped -> unstripped block map once all mods registered their strippable blocks
        ServerWorldEvents.LOAD.register((server, level) -> StaffOfRebark.getStrippedBlocks());

    }
}
