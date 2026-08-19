package com.notker.xps_additions.blocks;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.MapColor;
import net.minecraft.block.StairsBlock;
import net.minecraft.sound.BlockSoundGroup;

public class CutSoulCopperStairs extends StairsBlock {

    public CutSoulCopperStairs(BlockState baseBlockState) {
        super(baseBlockState, AbstractBlock.Settings
                .create()
                .mapColor(MapColor.IRON_GRAY)
                .sounds(BlockSoundGroup.METAL)
                .strength(3f, 6f)
                .requiresTool()
        );
    }
}
