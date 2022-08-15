package com.notker.xps_additions.blocks;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Material;
import net.minecraft.block.SlabBlock;
import net.minecraft.sound.BlockSoundGroup;

public class CutSoulCopperSlab extends SlabBlock {

    public CutSoulCopperSlab() {
        super(FabricBlockSettings
                .of(Material.METAL)
                .sounds(BlockSoundGroup.METAL)
                .strength(3f, 6f)
                .requiresTool()
        );
    }
}
