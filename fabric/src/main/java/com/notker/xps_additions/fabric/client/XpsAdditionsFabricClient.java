package com.notker.xps_additions.fabric.client;

import com.notker.xps_additions.XpsAdditionsClient;
import net.fabricmc.api.ClientModInitializer;

public final class XpsAdditionsFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        XpsAdditionsClient.init();
        XpsAdditionsClient.registerScreens();
    }
}
