package com.notker.xps_additions.blocks;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockSetType;
import net.minecraft.block.MapColor;
import net.minecraft.block.PressurePlateBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SoulCopperPressurePlate extends PressurePlateBlock {

    public SoulCopperPressurePlate() {
        super(BlockSetType.IRON, AbstractBlock.Settings
                .create()
                .mapColor(MapColor.IRON_GRAY)
                .sounds(BlockSoundGroup.METAL)
                .strength(0.5f, 0.5f)
                .nonOpaque()
                .requiresTool()
        );
    }

    /**
     * Keeps the "creatures only" sensitivity the plate had before 1.20.3. Since then the sensitivity
     * comes from the {@link BlockSetType} and {@code BlockSetType.IRON} triggers on every entity,
     * while {@code BlockSetType.register} is not public, so the rule is reimplemented here. This is
     * what vanilla does for {@code ActivationRule.MOBS}.
     */
    @Override
    protected int getRedstoneOutput(World world, BlockPos pos) {
        return getEntityCount(world, BOX.offset(pos), LivingEntity.class) > 0 ? 15 : 0;
    }
}
