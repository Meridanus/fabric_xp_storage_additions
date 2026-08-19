package com.notker.xps_additions.screen;

import com.notker.xp_storage.XpFunctions;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class PositionedScreen extends HandledScreen<ScreenHandler> {
    private static final Identifier TEXTURE = Identifier.of("xps_additions", "textures/gui/container/xp_item_inserter.png");
    BoxScreenHandler screenHandler;

    int[] color = {0xec00b8, 0x99ff33, 4210752, 0x000000}; //Purple - Green - light Gray - Black

    // The texture is a standard 166 px tall 3x3 container GUI with a 34 px XP header drawn above it
    private static final int HEADER_HEIGHT = 34;
    private static final int BODY_HEIGHT = 166;


    public PositionedScreen(ScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        screenHandler = (BoxScreenHandler) handler;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int xp = screenHandler.getSyncedNumber();
        // Anchor the header to the container origin so it stays aligned after a resize
        int y = this.y - HEADER_HEIGHT + 23;



        //renderBackground(context);
        super.render(context, mouseX, mouseY, delta);

        //Draw Storage Title
        Text storageTitle = Text.translatable("block.xps.block_xp_obelisk");
        int xStorage = (width - textRenderer.getWidth(storageTitle)) / 2;
        context.drawText(textRenderer, storageTitle, xStorage, y - 15, color[3], false);

        //Draw Xp bar Background

        //Vanilla Xp Bar
        //int v = 64;
        //int barWidth = 182;
        //int barHeight = 5;

        //Custom xp Bar
        int v = 228; //236
        int barWidth = 164; // 162
        int barHeight = 7; //5



        context.drawTexture(TEXTURE, (width - barWidth) / 2, y + (barHeight - 1), 0, v, barWidth, barHeight);

        int level = XpFunctions.getLevelFromExp(xp);
        int excess_xp = xp - XpFunctions.get_total_xp_value_from_level(level);
        int next_level_xp = XpFunctions.getToNextExperienceLevel(level);
        float container_progress = ((1f / next_level_xp) * excess_xp);

        //Draw Xp bar Overlay
        if (container_progress > 0) {
            int scaledWidth = (int)(container_progress * (float)barWidth + 1f);
            context.drawTexture(TEXTURE, (width - barWidth) / 2, y + (barHeight - 1), 0, v + barHeight, scaledWidth, barHeight);
        }

        //Draw Level String
        String string = String.valueOf(level);
        int levelStringCenter = (width - textRenderer.getWidth(string)) / 2;

        context.drawText(textRenderer, string, (levelStringCenter + 1), y, 0, false);
        context.drawText(textRenderer, string, (levelStringCenter - 1), y, 0, false);
        context.drawText(textRenderer, string, levelStringCenter, (y + 1), 0, false);
        context.drawText(textRenderer, string, levelStringCenter, (y - 1), 0, false);
        context.drawText(textRenderer, string, levelStringCenter, y, color[1], false);

        // Tooltips last, so they are drawn above the xp bar and the texts
        drawMouseoverTooltip(context, mouseX, mouseY);
    }

    // HandledScreen uses this to decide if a click lands outside the GUI (which drops the carried item),
    // so it has to cover the header that is drawn above the regular container area
    @Override
    protected boolean isClickOutsideBounds(double mouseX, double mouseY, int left, int top, int button) {
        return mouseX < left || mouseY < top - HEADER_HEIGHT || mouseX >= left + backgroundWidth || mouseY >= top + BODY_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        // Center the title
        this.titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
        this.titleY = 4;


    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = (width - backgroundWidth) / 2;
        int y = this.y - HEADER_HEIGHT;
        context.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, HEADER_HEIGHT + BODY_HEIGHT);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        context.drawText(this.textRenderer, this.title, this.titleX, this.titleY, color[3], false);
        context.drawText(this.textRenderer, this.playerInventoryTitle, this.playerInventoryTitleX, this.playerInventoryTitleY, color[3], false);
    }
}
