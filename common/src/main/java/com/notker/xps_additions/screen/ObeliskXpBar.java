package com.notker.xps_additions.screen;

import com.notker.xp_storage.XpFunctions;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * The "XP Storage" header every enchanter/inserter GUI draws above its 166 px body: the obelisk
 * name, an XP bar and the level number on top of it.
 *
 * <p>The sprites are read out of the GUI texture of the calling screen, so every screen that uses
 * this has to keep the bar sprites at the same place (v = {@code 228} and {@code 235}).
 */
public final class ObeliskXpBar {

    /** Extra rows the header takes above the regular container area. */
    public static final int HEADER_HEIGHT = 34;

    /** A standard container body, the part the slots live in. */
    public static final int BODY_HEIGHT = 166;

    // Custom xp bar sprites, drawn below the 200 px of the GUI itself
    private static final int BAR_V = 228;
    private static final int BAR_WIDTH = 164;
    private static final int BAR_HEIGHT = 7;

    private static final int LEVEL_COLOR = 0x99ff33; // green
    private static final int TITLE_COLOR = 0x000000; // black
    private static final int OUTLINE_COLOR = 0;

    private ObeliskXpBar() {
    }

    /**
     * @param texture      GUI texture of the calling screen, has to carry the bar sprites
     * @param screenWidth  width of the whole screen, the header is centered in it
     * @param containerTop y of the container body ({@code HandledScreen#y}), the header sits above it
     * @param xp           experience the obelisk holds, 0 when there is none
     */
    public static void draw(DrawContext context, TextRenderer textRenderer, Identifier texture,
                            int screenWidth, int containerTop, int xp) {
        // Anchor the header to the container origin so it stays aligned after a resize
        int y = containerTop - HEADER_HEIGHT + 23;

        //Draw Storage Title
        Text storageTitle = Text.translatable("block.xps.block_xp_obelisk");
        int xStorage = (screenWidth - textRenderer.getWidth(storageTitle)) / 2;
        context.drawText(textRenderer, storageTitle, xStorage, y - 15, TITLE_COLOR, false);

        //Draw Xp bar Background
        context.drawTexture(texture, (screenWidth - BAR_WIDTH) / 2, y + (BAR_HEIGHT - 1), 0, BAR_V, BAR_WIDTH, BAR_HEIGHT);

        int level = XpFunctions.getLevelFromExp(xp);
        int excess_xp = xp - XpFunctions.get_total_xp_value_from_level(level);
        int next_level_xp = XpFunctions.getToNextExperienceLevel(level);
        float container_progress = ((1f / next_level_xp) * excess_xp);

        //Draw Xp bar Overlay
        if (container_progress > 0) {
            int scaledWidth = (int) (container_progress * (float) BAR_WIDTH + 1f);
            context.drawTexture(texture, (screenWidth - BAR_WIDTH) / 2, y + (BAR_HEIGHT - 1), 0, BAR_V + BAR_HEIGHT, scaledWidth, BAR_HEIGHT);
        }

        //Draw Level String
        String string = String.valueOf(level);
        int levelStringCenter = (screenWidth - textRenderer.getWidth(string)) / 2;

        context.drawText(textRenderer, string, (levelStringCenter + 1), y, OUTLINE_COLOR, false);
        context.drawText(textRenderer, string, (levelStringCenter - 1), y, OUTLINE_COLOR, false);
        context.drawText(textRenderer, string, levelStringCenter, (y + 1), OUTLINE_COLOR, false);
        context.drawText(textRenderer, string, levelStringCenter, (y - 1), OUTLINE_COLOR, false);
        context.drawText(textRenderer, string, levelStringCenter, y, LEVEL_COLOR, false);
    }
}
