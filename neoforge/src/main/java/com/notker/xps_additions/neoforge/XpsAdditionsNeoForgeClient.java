package com.notker.xps_additions.neoforge;

import com.notker.xps_additions.XpsAdditionsClient;
import com.notker.xps_additions.regestry.AdditionMenus;
import com.notker.xps_additions.screen.PositionedScreen;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public final class XpsAdditionsNeoForgeClient {

    private XpsAdditionsNeoForgeClient() {
    }

    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(XpsAdditionsClient::init);
    }

    /**
     * The inserter screen has to be registered here rather than through architectury's
     * MenuRegistry: that one reacts to this very event, so calling it from client setup
     * registers the listener after the event already fired and the screen is dropped
     * ("Failed to create screen for menu type"). This event runs after the registries are
     * filled, so the menu type resolves.
     */
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(AdditionMenus.BOX_SCREEN_HANDLER.get(), PositionedScreen::new);
    }
}
