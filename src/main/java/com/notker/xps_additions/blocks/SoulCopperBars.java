package com.notker.xps_additions.blocks;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.MapColor;
import net.minecraft.block.PaneBlock;
import net.minecraft.sound.BlockSoundGroup;

public class SoulCopperBars extends PaneBlock {

    public SoulCopperBars() {
        super(FabricBlockSettings
                .create()
                .mapColor(MapColor.IRON_GRAY)
                .sounds(BlockSoundGroup.METAL)
                .strength(6f, 5f)
                .nonOpaque()
                .requiresTool()
        );
    }
}
