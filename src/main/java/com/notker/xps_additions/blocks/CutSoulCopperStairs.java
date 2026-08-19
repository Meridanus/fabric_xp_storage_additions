package com.notker.xps_additions.blocks;

import com.notker.xps_additions.regestry.AdditionBlocks;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.MapColor;
import net.minecraft.block.StairsBlock;
import net.minecraft.sound.BlockSoundGroup;

public class CutSoulCopperStairs extends StairsBlock {

    public CutSoulCopperStairs() {
        super(AdditionBlocks.CUT_SOUL_COPPER.getDefaultState(), FabricBlockSettings
                .create()
                .mapColor(MapColor.IRON_GRAY)
                .sounds(BlockSoundGroup.METAL)
                .strength(3f, 6f)
                .requiresTool()
        );
    }
}
