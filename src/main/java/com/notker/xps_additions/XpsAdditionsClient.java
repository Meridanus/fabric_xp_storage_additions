package com.notker.xps_additions;

import com.notker.xps_additions.items.StaffOfRebark;
import com.notker.xps_additions.regestry.AdditionBlocks;
import com.notker.xps_additions.screen.PositionedScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.RenderLayer;


public class XpsAdditionsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(AdditionBlocks.SOUL_COPPER_TRAP_DOOR, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AdditionBlocks.SOUL_COPPER_DOOR, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AdditionBlocks.SOUL_COPPER_BARS, RenderLayer.getCutout());

        HandledScreens.register(XpsAdditions.BOX_SCREEN_HANDLER, PositionedScreen::new);

        // Build the stripped -> unstripped block map once all mods registered their strippable blocks
        ClientPlayConnectionEvents.JOIN.register((packetListener, packetSender, mc) -> StaffOfRebark.getStrippedBlocks());
    }
}
