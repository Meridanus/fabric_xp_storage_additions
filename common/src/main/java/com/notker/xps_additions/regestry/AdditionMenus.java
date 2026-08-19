package com.notker.xps_additions.regestry;

import com.notker.xps_additions.XpsAdditions;
import com.notker.xps_additions.screen.BoxScreenHandler;
import dev.architectury.registry.menu.MenuRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.screen.ScreenHandlerType;

public class AdditionMenus {

    public static final DeferredRegister<ScreenHandlerType<?>> MENUS =
            DeferredRegister.create(XpsAdditions.MOD_ID, RegistryKeys.SCREEN_HANDLER);

    public static final RegistrySupplier<ScreenHandlerType<BoxScreenHandler>> BOX_SCREEN_HANDLER =
            MENUS.register("xp_item_inserter", () -> MenuRegistry.ofExtended(BoxScreenHandler::new));

    public static void register() {
        MENUS.register();
    }
}
