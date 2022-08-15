package com.notker.xps_additions.blocks;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.sound.BlockSoundGroup;

public class CutSoulCopper extends Block {

    public CutSoulCopper() {
        super(FabricBlockSettings
                .of(Material.METAL)
                .sounds(BlockSoundGroup.METAL)
                .strength(6f, 3f)
                .requiresTool()
        );
    }
}
