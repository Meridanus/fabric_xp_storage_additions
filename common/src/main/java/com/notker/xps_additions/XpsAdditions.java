package com.notker.xps_additions;

import com.notker.xp_storage.XpStorage;
import com.notker.xps_additions.items.StaffOfRebark;
import com.notker.xps_additions.regestry.AdditionBlocks;
import com.notker.xps_additions.regestry.AdditionEffects;
import com.notker.xps_additions.regestry.AdditionItems;
import com.notker.xps_additions.regestry.AdditionMenus;
import dev.architectury.event.events.common.LifecycleEvent;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class XpsAdditions {

    public static final String MOD_ID = "xps_additions";
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

    private XpsAdditions() {
    }

    public static Identifier createModIdIdentifier(String path) {
        return Identifier.of(MOD_ID, path);
    }

    public static void init() {
        // Order matters: the mystical candy references the giggle effect, block items reference
        // their blocks and the block entity type references the inserter block.
        AdditionEffects.register();
        AdditionBlocks.registerBlocks();
        AdditionItems.registerItems();
        AdditionBlocks.registerBlockEntityTypes();
        AdditionMenus.register();

        // Runs after the registries are filled on both loaders
        LifecycleEvent.SETUP.register(XpsAdditions::setup);

        // Build the stripped -> unstripped block map once all mods registered their strippable blocks
        LifecycleEvent.SERVER_LEVEL_LOAD.register(level -> StaffOfRebark.getStrippedBlocks());
    }

    private static void setup() {
        AdditionItems.registerFuel();
    }
}
