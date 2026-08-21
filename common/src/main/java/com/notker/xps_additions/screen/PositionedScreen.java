package com.notker.xps_additions.screen;

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
    private static final int HEADER_HEIGHT = ObeliskXpBar.HEADER_HEIGHT;
    private static final int BODY_HEIGHT = ObeliskXpBar.BODY_HEIGHT;


    public PositionedScreen(ScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        screenHandler = (BoxScreenHandler) handler;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        //renderBackground(context);
        super.render(context, mouseX, mouseY, delta);

        // Storage title, xp bar and level number, shared with the other obelisk driven GUIs
        ObeliskXpBar.draw(context, textRenderer, TEXTURE, width, this.y, screenHandler.getSyncedNumber());

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
