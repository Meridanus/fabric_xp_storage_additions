package com.notker.xps_additions.fabric;

import com.notker.xps_additions.XpsAdditions;
import net.fabricmc.api.ModInitializer;

public final class XpsAdditionsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        XpsAdditions.init();
    }
}
