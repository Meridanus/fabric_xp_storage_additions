package com.notker.xps_additions.blocks;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.sound.BlockSoundGroup;

public class CutSoulCopper extends Block {

    public CutSoulCopper() {
        super(AbstractBlock.Settings
                .create()
                .mapColor(MapColor.IRON_GRAY)
                .sounds(BlockSoundGroup.METAL)
                .strength(6f, 3f)
                .requiresTool()
        );
    }
}
