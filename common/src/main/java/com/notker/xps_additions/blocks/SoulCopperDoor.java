package com.notker.xps_additions.blocks;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockSetType;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.MapColor;
import net.minecraft.sound.BlockSoundGroup;

public class SoulCopperDoor extends DoorBlock {

    public SoulCopperDoor() {
        super(BlockSetType.IRON, AbstractBlock.Settings
                .create()
                .mapColor(MapColor.IRON_GRAY)
                .sounds(BlockSoundGroup.METAL)
                .strength(5f, 5f)
                .nonOpaque()
                .requiresTool()
        );
    }
}
