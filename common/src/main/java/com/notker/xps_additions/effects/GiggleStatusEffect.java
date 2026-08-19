package com.notker.xps_additions.effects;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.random.Random;


public class GiggleStatusEffect extends StatusEffect {
    public GiggleStatusEffect() {
        super(StatusEffectCategory.HARMFUL, 0x93E477);

    }



    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        int i;
        i = 32 >> amplifier;
        if (i > 0) {
            return duration % i == 0;
        } else {
            return true;
        }

    }

    @Override
    public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (entity instanceof PlayerEntity player) {
            Random random = entity.getRandom();

            //Horizontal hiccup hop (runs on both sides so the local player sees it)
            float x = random.nextFloat() * 0.4f - 0.2f;
            float z = random.nextFloat() * 0.4f - 0.2f;
            entity.addVelocity(x, 0.2f, z);

            // Inventory changes are server authoritative; doing them on the client only desyncs the inventory
            if (entity.getWorld().isClient) {
                return true;
            }

            //Random inventory slot without Hotbar/Armor/Offhand (main inventory = slots 9..35)
            int randomSlot = random.nextInt(PlayerInventory.MAIN_SIZE - PlayerInventory.getHotbarSize()) + PlayerInventory.getHotbarSize();

            ItemStack itemToDrop = player.getInventory().getStack(randomSlot).copy();
            if (!itemToDrop.isEmpty()) {
                player.getInventory().getStack(randomSlot).decrement(1);
                itemToDrop.setCount(1);
                player.dropItem(itemToDrop, true, true);
            }

        }
        // Keep the effect running until its duration is over
        return true;
    }


}
