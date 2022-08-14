package com.notker.xps_additions.blocks;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Material;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.sound.BlockSoundGroup;

public class SoulCopperTrapDoorBlock extends TrapdoorBlock {

    public SoulCopperTrapDoorBlock() {
        super(FabricBlockSettings
                .of(Material.METAL)
                .sounds(BlockSoundGroup.METAL)
                .strength(5f, 5f)
                .nonOpaque()
                .requiresTool()
        );
    }
}
