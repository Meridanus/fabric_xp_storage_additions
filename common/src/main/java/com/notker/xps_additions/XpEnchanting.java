package com.notker.xps_additions;

import com.notker.xp_storage.XpFunctions;

/**
 * The maths and the status codes of the XP Enchanter, shared by the block entity (server) and
 * the screen (client) so both always agree on what an enchant costs.
 *
 * <h2>Cost model</h2>
 * The enchanter behaves like a player standing in front of a vanilla enchanting table:
 * <ul>
 *     <li>The chosen {@code level} (1..30) is the slot the player would click.</li>
 *     <li>A vanilla table charges as many levels as the slot number, capped at 3, which is
 *         {@code tier = 1 + (level - 1) / 10} here (1..10 -> 1, 11..20 -> 2, 21..30 -> 3)
 *         and the same amount of lapis.</li>
 *     <li>To offer the slot at all, the player has to <em>be</em> that level, so the obelisk has
 *         to hold {@link #requiredXp(int)} - the total XP of a level-{@code level} player.</li>
 *     <li>Paying {@code tier} levels from there costs {@link #costXp(int)} XP, which is exactly
 *         what the player would lose.</li>
 * </ul>
 */
public final class XpEnchanting {

    public static final int MIN_LEVEL = 1;
    public static final int MAX_LEVEL = 30;
    public static final int DEFAULT_LEVEL = MAX_LEVEL;

    /** Highest number of levels (and lapis) a single enchant can ever cost. */
    public static final int MAX_TIER = 3;

    private XpEnchanting() {
    }

    public static int clampLevel(int level) {
        return Math.max(MIN_LEVEL, Math.min(MAX_LEVEL, level));
    }

    /** Levels (and lapis) a single enchant costs: 1..10 -> 1, 11..20 -> 2, 21..30 -> 3. */
    public static int tier(int level) {
        return 1 + (clampLevel(level) - 1) / 10;
    }

    /** XP the obelisk has to hold before it can offer this level, i.e. what such a player has. */
    public static int requiredXp(int level) {
        return XpFunctions.get_total_xp_value_from_level(clampLevel(level));
    }

    /** XP a single enchant takes out of the obelisk: the {@link #tier(int)} levels a player would pay. */
    public static int costXp(int level) {
        int clamped = clampLevel(level);
        return XpFunctions.get_total_xp_value_from_level(clamped)
                - XpFunctions.get_total_xp_value_from_level(clamped - tier(clamped));
    }

    /**
     * Why the enchanter can (not) run right now. The ordinal is what travels through the
     * screen handler property delegate, so only ever append new values at the end.
     */
    public enum Status {
        /** Everything is in place, the next trigger enchants. */
        READY("ready"),
        /** No XP Obelisk next to the block. */
        NO_OBELISK("no_obelisk"),
        /** The obelisk is bound to another player and refuses to hand out XP. */
        LOCKED("locked"),
        /** Nothing in the input slot. */
        NO_ITEM("no_item"),
        /** The input can not be enchanted (already enchanted items included, like the vanilla table). */
        NOT_ENCHANTABLE("not_enchantable"),
        /** The output slot still holds the last result. */
        OUTPUT_FULL("output_full"),
        /** Less lapis than the level costs. */
        NO_LAPIS("no_lapis"),
        /** The obelisk holds less XP than a player of that level would have. */
        NO_XP("no_xp"),
        /** No enchantment of the enchanting table pool fits this item at this level. */
        NOTHING("nothing");

        private static final Status[] VALUES = values();

        private final String name;

        Status(String name) {
            this.name = name;
        }

        public String translationKey() {
            return "gui.xps_additions.xp_enchanter.status." + name;
        }

        /** Null safe lookup for the value that came in through the property delegate. */
        public static Status byId(int id) {
            return id >= 0 && id < VALUES.length ? VALUES[id] : NO_OBELISK;
        }
    }
}
