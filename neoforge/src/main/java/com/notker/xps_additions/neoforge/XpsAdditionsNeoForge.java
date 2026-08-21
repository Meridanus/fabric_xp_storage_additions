package com.notker.xps_additions.neoforge;

import com.notker.xps_additions.XpsAdditions;
import com.notker.xps_additions.regestry.AdditionBlocks;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;

@Mod(XpsAdditions.MOD_ID)
public final class XpsAdditionsNeoForge {

    public XpsAdditionsNeoForge(IEventBus modBus) {
        XpsAdditions.init();

        modBus.addListener(XpsAdditionsNeoForge::registerCapabilities);

        if (Platform.getEnvironment() == Env.CLIENT) {
            modBus.addListener(XpsAdditionsNeoForgeClient::onClientSetup);
            modBus.addListener(XpsAdditionsNeoForgeClient::onRegisterMenuScreens);
        }
    }

    /**
     * Lets neoforge item pipes talk to the block entities. Fabric needs nothing here, its item
     * storage wraps any Inventory block entity by itself. Both inventories ignore the side and
     * decide per slot, so the wrapper is safe to hand out for a null side as well.
     */
    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, AdditionBlocks.XP_ITEM_INSERTER_ENTITY.get(),
                (blockEntity, side) -> new SidedInvWrapper(blockEntity, side));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, AdditionBlocks.XP_ENCHANTER_ENTITY.get(),
                (blockEntity, side) -> new SidedInvWrapper(blockEntity, side));
    }
}
