package com.notker.xps_additions.blocks;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.MapColor;
import net.minecraft.block.SlabBlock;
import net.minecraft.sound.BlockSoundGroup;

public class CutSoulCopperSlab extends SlabBlock {

    public CutSoulCopperSlab() {
        super(AbstractBlock.Settings
                .create()
                .mapColor(MapColor.IRON_GRAY)
                .sounds(BlockSoundGroup.METAL)
                .strength(3f, 6f)
                .requiresTool()
        );
    }
}
