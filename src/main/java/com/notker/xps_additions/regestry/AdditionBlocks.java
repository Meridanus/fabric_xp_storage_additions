package com.notker.xps_additions.regestry;

import com.notker.xps_additions.XpsAdditions;
import com.notker.xps_additions.blocks.SoulCopperBars;
import com.notker.xps_additions.blocks.SoulCopperDoorBlock;
import com.notker.xps_additions.blocks.SoulCopperPressurePlate;
import com.notker.xps_additions.blocks.SoulCopperTrapDoorBlock;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Material;
import net.minecraft.block.PressurePlateBlock;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

import net.minecraft.util.registry.Registry;

public class AdditionBlocks {

    public static final SoulCopperDoorBlock SOUL_COPPER_DOOR = new SoulCopperDoorBlock();
    public static final SoulCopperTrapDoorBlock SOUL_COPPER_TRAP_DOOR = new SoulCopperTrapDoorBlock();
    public static final SoulCopperPressurePlate SOUL_COPPER_PRESSURE_PLATE = new SoulCopperPressurePlate();
    public static final SoulCopperBars SOUL_COPPER_BARS = new SoulCopperBars();



    public static void registerBlocks() {
        Registry.register(Registry.BLOCK, XpsAdditions.createModIdIdentifier("soul_copper_door"), SOUL_COPPER_DOOR);
        Registry.register(Registry.BLOCK, XpsAdditions.createModIdIdentifier("soul_copper_trap_door"), SOUL_COPPER_TRAP_DOOR);
        Registry.register(Registry.BLOCK, XpsAdditions.createModIdIdentifier("soul_copper_pressure_plate"), SOUL_COPPER_PRESSURE_PLATE);
        Registry.register(Registry.BLOCK, XpsAdditions.createModIdIdentifier("soul_copper_bars"), SOUL_COPPER_BARS);
    }
}
