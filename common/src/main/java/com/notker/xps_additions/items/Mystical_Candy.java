package com.notker.xps_additions.items;

import com.notker.xp_storage.regestry.ModItems;
import com.notker.xps_additions.TooltipHelper;
import com.notker.xps_additions.XpsAdditions;
import com.notker.xps_additions.regestry.AdditionEffects;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

import java.util.List;

public class Mystical_Candy extends Item {
    public Mystical_Candy() {
        // Hiccup is NOT listed here on purpose. The food component would have to be built while the
        // status effect registry is still empty, so the only entry available at that point is the
        // architectury RegistrySupplier. That supplier hashes by id while the registry's own
        // RegistryEntry.Reference uses identity, so the effect would end up in the entity's
        // activeStatusEffects map under a key vanilla can never look up again: /effect clear would
        // miss it and a reload (which rebuilds instances with the canonical Reference) would let a
        // second copy stack on top. Hiccup is applied in finishUsing with the canonical entry
        // instead. HASTE is fine here, StatusEffects.HASTE already is the canonical entry.
        super(new Item.Settings()
                .arch$tab(ModItems.XP_TAB)
                .food(new FoodComponent.Builder()
                        .statusEffect(new StatusEffectInstance(StatusEffects.HASTE, XpsAdditions.HASTE_EFFECT_DURATION, XpsAdditions.HASTE_EFFECT_AMPLIFIER), XpsAdditions.HASTE_EFFECT_CHANCE)
                        .nutrition(2)
                        .saturationModifier(0.8f)
                        .snack()
                        .alwaysEdible()
                        .build()));
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        ServerWorld sw = world instanceof ServerWorld ? (ServerWorld) world : null;
        PlayerEntity pe = user instanceof PlayerEntity ? (PlayerEntity) user : null;
        if (sw != null && pe != null) {
            world.spawnEntity(new ExperienceOrbEntity(world, user.getX(), user.getY(), user.getZ(), XpsAdditions.XP_PER_MYSTICAL_CANDY));
        }

        // Roll the hiccup here, with the entry the registry itself hands out, so the effect is
        // stored under the same key vanilla uses (see the constructor). Server side only, and
        // before super.finishUsing since that consumes the stack.
        if (!world.isClient && world.random.nextFloat() < XpsAdditions.GIGGLE_EFFECT_CHANCE) {
            user.addStatusEffect(new StatusEffectInstance(
                    Registries.STATUS_EFFECT.getEntry(AdditionEffects.GIGGLE.get()),
                    XpsAdditions.GIGGLE_EFFECT_DURATION, 0));
        }

        return super.finishUsing(stack, world, user);
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);

        tooltip.add(Text.translatable("item.tooltip.mystical_candy_effect",
                Text.translatable("effect.xps_additions.giggle"),
                TooltipHelper.potionTooltipHelper(XpsAdditions.GIGGLE_EFFECT_DURATION),
                TooltipHelper.chanceToString(XpsAdditions.GIGGLE_EFFECT_CHANCE)
        ).formatted(AdditionEffects.GIGGLE.get().getCategory().getFormatting()));
        tooltip.add(Text.translatable("item.tooltip.mystical_candy_effect",
                Text.translatable("effect.minecraft.haste"),
                TooltipHelper.potionTooltipHelper(XpsAdditions.HASTE_EFFECT_AMPLIFIER, XpsAdditions.HASTE_EFFECT_DURATION),
                TooltipHelper.chanceToString(XpsAdditions.HASTE_EFFECT_CHANCE)
        ).formatted(StatusEffects.HASTE.value().getCategory().getFormatting()));
        tooltip.add(ScreenTexts.EMPTY);
        tooltip.add(Text.translatable("item.tooltip.mystical_candy", XpsAdditions.XP_PER_MYSTICAL_CANDY).formatted(Formatting.WHITE));
    }
}
