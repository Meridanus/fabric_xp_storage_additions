package com.notker.xps_additions.regestry;

import com.notker.xps_additions.XpsAdditions;
import com.notker.xps_additions.blocks.*;
import com.notker.xps_additions.entity.XpItemInserterEntity;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;

public class AdditionBlocks {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(XpsAdditions.MOD_ID, RegistryKeys.BLOCK);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(XpsAdditions.MOD_ID, RegistryKeys.BLOCK_ENTITY_TYPE);

    public static final RegistrySupplier<SoulCopperDoor> SOUL_COPPER_DOOR =
            BLOCKS.register("soul_copper_door", SoulCopperDoor::new);

    public static final RegistrySupplier<SoulCopperTrapDoor> SOUL_COPPER_TRAP_DOOR =
            BLOCKS.register("soul_copper_trap_door", SoulCopperTrapDoor::new);

    public static final RegistrySupplier<SoulCopperPressurePlate> SOUL_COPPER_PRESSURE_PLATE =
            BLOCKS.register("soul_copper_pressure_plate", SoulCopperPressurePlate::new);

    public static final RegistrySupplier<SoulCopperBars> SOUL_COPPER_BARS =
            BLOCKS.register("soul_copper_bars", SoulCopperBars::new);

    public static final RegistrySupplier<CutSoulCopper> CUT_SOUL_COPPER =
            BLOCKS.register("cut_soul_copper", CutSoulCopper::new);

    public static final RegistrySupplier<CutSoulCopperSlab> CUT_SOUL_COPPER_SLAB =
            BLOCKS.register("cut_soul_copper_slab", CutSoulCopperSlab::new);

    // The stairs need the block they are cut from, so the lookup has to happen inside the supplier
    public static final RegistrySupplier<CutSoulCopperStairs> CUT_SOUL_COPPER_STAIRS =
            BLOCKS.register("cut_soul_copper_stairs", () -> new CutSoulCopperStairs(CUT_SOUL_COPPER.get().getDefaultState()));

    public static final RegistrySupplier<XpItemInserter> XP_ITEM_INSERTER =
            BLOCKS.register("xp_item_inserter", () -> new XpItemInserter(XpItemInserter.settings()));

    public static final RegistrySupplier<Block> RAW_ESSENCE_BLOCK =
            BLOCKS.register("raw_essence_block", () -> new Block(AbstractBlock.Settings.create()
                    .mapColor(MapColor.PURPLE)
                    .sounds(BlockSoundGroup.CALCITE)
                    .strength(5F, 6F)
                    .requiresTool()));

    public static final RegistrySupplier<Street> STREET = BLOCKS.register("street", Street::new);

    // The id has to stay "entity_xp_obelisk" so old worlds keep their inserters
    public static final RegistrySupplier<BlockEntityType<XpItemInserterEntity>> XP_ITEM_INSERTER_ENTITY =
            BLOCK_ENTITY_TYPES.register("entity_xp_obelisk",
                    () -> BlockEntityType.Builder.create(XpItemInserterEntity::new, XP_ITEM_INSERTER.get()).build(null));

    public static void registerBlocks() {
        BLOCKS.register();
    }

    public static void registerBlockEntityTypes() {
        BLOCK_ENTITY_TYPES.register();
    }
}
