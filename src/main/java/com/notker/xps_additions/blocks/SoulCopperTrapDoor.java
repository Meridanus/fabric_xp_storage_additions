package com.notker.xps_additions.blocks;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.BlockSetType;
import net.minecraft.block.Material;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.sound.BlockSoundGroup;

public class SoulCopperTrapDoor extends TrapdoorBlock {

    public SoulCopperTrapDoor() {
        super(FabricBlockSettings
                .of(Material.METAL)
                .sounds(BlockSoundGroup.METAL)
                .strength(5f, 5f)
                .nonOpaque()
                .requiresTool(),
                BlockSetType.IRON);
    }
}
