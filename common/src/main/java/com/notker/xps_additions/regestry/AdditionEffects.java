package com.notker.xps_additions.regestry;

import com.notker.xps_additions.XpsAdditions;
import com.notker.xps_additions.effects.GiggleStatusEffect;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.RegistryKeys;

public class AdditionEffects {

    public static final DeferredRegister<StatusEffect> STATUS_EFFECTS =
            DeferredRegister.create(XpsAdditions.MOD_ID, RegistryKeys.STATUS_EFFECT);

    /**
     * A {@code RegistrySupplier} is also a {@code RegistryEntry}, so this can be handed to a
     * {@code StatusEffectInstance} before the effect registry has been filled. That matters because
     * the loaders do not agree on the order in which the item and the status effect registry are
     * populated.
     */
    public static final RegistrySupplier<StatusEffect> GIGGLE =
            STATUS_EFFECTS.register("giggle", GiggleStatusEffect::new);

    public static void register() {
        STATUS_EFFECTS.register();
    }
}
