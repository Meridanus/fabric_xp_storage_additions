package com.notker.xps_additions;

import com.notker.xps_additions.items.StaffOfRebark;
import com.notker.xps_additions.regestry.AdditionBlocks;
import com.notker.xps_additions.regestry.AdditionMenus;
import com.notker.xps_additions.screen.PositionedScreen;
import dev.architectury.event.events.client.ClientPlayerEvent;
import dev.architectury.registry.client.rendering.RenderTypeRegistry;
import dev.architectury.registry.menu.MenuRegistry;
import net.minecraft.client.render.RenderLayer;


public final class XpsAdditionsClient {

    private XpsAdditionsClient() {
    }

    /**
     * Call when the registries are ready: directly from the fabric client entrypoint,
     * from FMLClientSetupEvent on neoforge. Do not defer through arch CLIENT_SETUP,
     * its timing races the entrypoints on fabric.
     */
    public static void init() {
        RenderTypeRegistry.register(RenderLayer.getCutout(),
                AdditionBlocks.SOUL_COPPER_TRAP_DOOR.get(),
                AdditionBlocks.SOUL_COPPER_DOOR.get(),
                AdditionBlocks.SOUL_COPPER_BARS.get());

        // Build the stripped -> unstripped block map once all mods registered their strippable blocks
        ClientPlayerEvent.CLIENT_PLAYER_JOIN.register(player -> StaffOfRebark.getStrippedBlocks());
    }

    /**
     * Fabric only. Architectury's neoforge implementation of this reacts to
     * RegisterMenuScreensEvent, which has already fired by the time a client setup
     * listener runs, so the factory would be dropped silently and the inserter GUI would
     * never open. NeoForge registers the same screen through that event directly.
     */
    public static void registerScreens() {
        MenuRegistry.registerScreenFactory(AdditionMenus.BOX_SCREEN_HANDLER.get(), PositionedScreen::new);
    }
}
