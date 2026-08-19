package com.notker.xps_additions.neoforge;

import com.notker.xps_additions.XpsAdditions;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(XpsAdditions.MOD_ID)
public final class XpsAdditionsNeoForge {

    public XpsAdditionsNeoForge(IEventBus modBus) {
        XpsAdditions.init();

        if (Platform.getEnvironment() == Env.CLIENT) {
            modBus.addListener(XpsAdditionsNeoForgeClient::onClientSetup);
            modBus.addListener(XpsAdditionsNeoForgeClient::onRegisterMenuScreens);
        }
    }
}
