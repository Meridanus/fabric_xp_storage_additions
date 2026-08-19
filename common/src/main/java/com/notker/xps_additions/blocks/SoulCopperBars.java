package com.notker.xps_additions.blocks;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.MapColor;
import net.minecraft.block.PaneBlock;
import net.minecraft.sound.BlockSoundGroup;

public class SoulCopperBars extends PaneBlock {

    public SoulCopperBars() {
        super(AbstractBlock.Settings
                .create()
                .mapColor(MapColor.IRON_GRAY)
                .sounds(BlockSoundGroup.METAL)
                .strength(6f, 5f)
                .nonOpaque()
                .requiresTool()
        );
    }
}
