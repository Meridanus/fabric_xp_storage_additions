package com.notker.xps_additions.screen;

import com.mojang.blaze3d.systems.RenderSystem;

import com.notker.xp_storage.XpFunctions;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

public class PositionedScreen extends HandledScreen<ScreenHandler> {
    private static final Identifier TEXTURE = new Identifier("xps_additions", "textures/gui/container/xp_item_inserter.png");
    BoxScreenHandler screenHandler;

    int[] color = {0xec00b8, 0x99ff33, 4210752, 0x000000}; //Purple - Green - light Gray - Black


    public PositionedScreen(ScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        screenHandler = (BoxScreenHandler) handler;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        int xp = screenHandler.getSyncedNumber();
        int y = ((height - backgroundHeight) / 2) + 6;



        //renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        drawMouseoverTooltip(matrices, mouseX, mouseY);

        //Draw Storage Title
        int xStorage = (width - textRenderer.getWidth(new TranslatableText("block.xps.block_xp_obelisk"))) / 2;
        textRenderer.draw(matrices, new TranslatableText("block.xps.block_xp_obelisk"), xStorage, y - 15, color[3]);

        //Draw Xp bar Background

        //Vanilla Xp Bar
        //RenderSystem.setShaderTexture(0, DrawableHelper.GUI_ICONS_TEXTURE);
        //int v = 64;
        //int barWidth = 182;
        //int barHeight = 5;

        //Custom xp Bar
        RenderSystem.setShaderTexture(0, TEXTURE);
        int v = 228; //236
        int barWidth = 164; // 162
        int barHeight = 7; //5



        drawTexture(matrices,  (width - barWidth) / 2  , y + (barHeight - 1), 0, v, barWidth, barHeight);

        int level = XpFunctions.getLevelFromExp(xp);
        int excess_xp = xp - XpFunctions.get_total_xp_value_from_level(level);
        int next_level_xp = XpFunctions.getToNextExperienceLevel(level);
        float container_progress = ((1f / next_level_xp) * excess_xp);

        //Draw Xp bar Overlay
        if (container_progress > 0) {
           int scaledWidth = (int)(container_progress * (float)barWidth + 1f);
            drawTexture(matrices, (width - barWidth) / 2 , y + (barHeight - 1), 0, v + barHeight, scaledWidth, barHeight);
        }

        //Draw Level String
        String string = String.valueOf(level);
        int levelStringCenter = (width - textRenderer.getWidth(string)) / 2;

        textRenderer.draw(matrices, string, (levelStringCenter + 1), y, 0);
        textRenderer.draw(matrices, string, (levelStringCenter - 1), y, 0);
        textRenderer.draw(matrices, string, levelStringCenter, (y + 1), 0);
        textRenderer.draw(matrices, string, levelStringCenter, (y - 1), 0);
        textRenderer.draw(matrices, string, levelStringCenter, y, color[1]);
    }

    @Override
    protected void init() {
        super.init();
        // Center the title
        backgroundHeight = 200;
        this.titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
        this.titleY = 4;


    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - backgroundWidth) / 2;
        int y = -17 + (height - backgroundHeight) / 2;
        drawTexture(matrices, x, y, 0, 0, backgroundWidth, backgroundHeight);
    }

    protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
        this.textRenderer.draw(matrices, this.title, (float)this.titleX, (float)this.titleY, color[3]);
        this.textRenderer.draw(matrices, this.playerInventoryTitle, (float)this.playerInventoryTitleX, (float)this.playerInventoryTitleY, color[3]);
    }
}
