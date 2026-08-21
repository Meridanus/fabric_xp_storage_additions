package com.notker.xps_additions.screen;

import com.notker.xps_additions.XpEnchanting;
import com.notker.xps_additions.XpsAdditions;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class XpEnchanterScreen extends HandledScreen<XpEnchanterScreenHandler> {

    private static final Identifier TEXTURE = XpsAdditions.createModIdIdentifier("textures/gui/container/xp_enchanter.png");

    private static final int HEADER_HEIGHT = ObeliskXpBar.HEADER_HEIGHT;
    private static final int BODY_HEIGHT = ObeliskXpBar.BODY_HEIGHT;

    // Level controls, in GUI coordinates
    private static final int MINUS_X = 56;
    private static final int PLUS_X = 90;
    private static final int BUTTON_Y = 22;
    private static final int BUTTON_SIZE = 14;
    private static final int LEVEL_CENTER_X = 80;
    private static final int LEVEL_TEXT_Y = 26;
    private static final int ENCHANT_X = 54;
    private static final int ENCHANT_Y = 44;
    private static final int ENCHANT_WIDTH = 52;
    private static final int ENCHANT_HEIGHT = 18;

    private static final int TEXT_COLOR = 0x000000;  // black, like the other GUIs of the mod
    private static final int LEVEL_COLOR = 0xFFFFFF; // white, the level setting is a control value

    /** Greyed out while the enchanter can not run; its tooltip says why. */
    @Nullable
    private ButtonWidget enchantButton;

    /** Last status the button was built for, so the tooltip is only rebuilt when it really changed. */
    @Nullable
    private XpEnchanting.Status shownStatus;

    public XpEnchanterScreen(XpEnchanterScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        // Center the title
        this.titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
        this.titleY = 4;

        // Shift click moves the level in steps of ten, the server clamps it to 1..30 either way
        addDrawableChild(ButtonWidget.builder(Text.literal("-"), button -> sendButton(hasShiftDown()
                        ? XpEnchanterScreenHandler.BUTTON_LEVEL_DOWN_10
                        : XpEnchanterScreenHandler.BUTTON_LEVEL_DOWN))
                .dimensions(x + MINUS_X, y + BUTTON_Y, BUTTON_SIZE, BUTTON_SIZE)
                .build());

        addDrawableChild(ButtonWidget.builder(Text.literal("+"), button -> sendButton(hasShiftDown()
                        ? XpEnchanterScreenHandler.BUTTON_LEVEL_UP_10
                        : XpEnchanterScreenHandler.BUTTON_LEVEL_UP))
                .dimensions(x + PLUS_X, y + BUTTON_Y, BUTTON_SIZE, BUTTON_SIZE)
                .build());

        enchantButton = addDrawableChild(ButtonWidget.builder(Text.translatable("gui.xps_additions.xp_enchanter.enchant"),
                        button -> sendButton(XpEnchanterScreenHandler.BUTTON_ENCHANT))
                .dimensions(x + ENCHANT_X, y + ENCHANT_Y, ENCHANT_WIDTH, ENCHANT_HEIGHT)
                .build());

        // A resize rebuilds the widgets, so the tooltip has to be attached again
        shownStatus = null;
        updateEnchantButton();
    }

    @Override
    protected void handledScreenTick() {
        super.handledScreenTick();
        updateEnchantButton();
    }

    /**
     * The button is the status display: enabled when the enchanter would run, greyed out otherwise,
     * with the reason in its tooltip. Vanilla ignores clicks on an inactive button, and the server
     * checks the same conditions again in {@code XpEnchanterEntity#tryEnchant}, so this is only UX.
     */
    private void updateEnchantButton() {
        if (enchantButton == null) {
            return;
        }

        XpEnchanting.Status status = handler.getStatus();
        enchantButton.active = status == XpEnchanting.Status.READY;

        if (status != shownStatus) {
            shownStatus = status;
            enchantButton.setTooltip(Tooltip.of(Text.translatable(status == XpEnchanting.Status.READY
                    ? "gui.xps_additions.xp_enchanter.enchant.tooltip"
                    : status.translationKey())));
        }
    }

    /** The block entity does the work, the click only has to reach it. */
    private void sendButton(int id) {
        if (this.client != null && this.client.interactionManager != null) {
            this.client.interactionManager.clickButton(handler.syncId, id);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        // Storage title, xp bar and level number, shared with the other obelisk driven GUIs
        ObeliskXpBar.draw(context, textRenderer, TEXTURE, width, this.y, handler.getObeliskXp());

        // Tooltips last, so they are drawn above the xp bar and the texts
        drawMouseoverTooltip(context, mouseX, mouseY);
        drawCostTooltip(context, mouseX, mouseY);
    }

    /** What the current level setting will cost, shown when hovering the number between the buttons. */
    private void drawCostTooltip(DrawContext context, int mouseX, int mouseY) {
        int left = x + MINUS_X + BUTTON_SIZE;
        int right = x + PLUS_X;
        int top = y + BUTTON_Y;
        int bottom = top + BUTTON_SIZE;
        if (mouseX < left || mouseX >= right || mouseY < top || mouseY >= bottom) {
            return;
        }

        int level = handler.getEnchantLevel();
        int tier = XpEnchanting.tier(level);
        context.drawTooltip(textRenderer,
                Text.translatable("gui.xps_additions.xp_enchanter.cost", level, tier, tier),
                mouseX, mouseY);
    }

    // HandledScreen uses this to decide if a click lands outside the GUI (which drops the carried item),
    // so it has to cover the header that is drawn above the regular container area
    @Override
    protected boolean isClickOutsideBounds(double mouseX, double mouseY, int left, int top, int button) {
        return mouseX < left || mouseY < top - HEADER_HEIGHT || mouseX >= left + backgroundWidth || mouseY >= top + BODY_HEIGHT;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = (width - backgroundWidth) / 2;
        int y = this.y - HEADER_HEIGHT;
        context.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, HEADER_HEIGHT + BODY_HEIGHT);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        context.drawText(this.textRenderer, this.title, this.titleX, this.titleY, TEXT_COLOR, false);
        context.drawText(this.textRenderer, this.playerInventoryTitle, this.playerInventoryTitleX, this.playerInventoryTitleY, TEXT_COLOR, false);

        // The configured enchanting level, between the two buttons
        context.drawCenteredTextWithShadow(this.textRenderer, String.valueOf(handler.getEnchantLevel()),
                LEVEL_CENTER_X, LEVEL_TEXT_Y, LEVEL_COLOR);
    }
}
