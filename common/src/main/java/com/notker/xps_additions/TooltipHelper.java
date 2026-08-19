package com.notker.xps_additions;

import net.minecraft.util.StringHelper;

public final class TooltipHelper {

    /** Ticks per second the durations in the tooltips are based on. */
    private static final float TICK_RATE = 20.0f;

    public static String amplifierToRomanString (int amplifier) {
        if (amplifier > 9 || amplifier < 0) {
            return String.valueOf(amplifier);
        }

        String[] values = {"I ", "II ", "III ", "IV ", "V ", "VI ", "VII ", "VIII ", "IX ", "X "};

        return values[amplifier];
    }

    public static String potionTooltipHelper (int duration) {
        return potionTooltipHelper(0,duration);
    }

    public static String potionTooltipHelper (int amplifier, int duration) {
        return amplifierToRomanString(amplifier) + "(" + StringHelper.formatTicks(duration, TICK_RATE) + ")";
    }

    public static String chanceToString (float chance) {
        return (int) (100 * chance) + "%";
    }
}
