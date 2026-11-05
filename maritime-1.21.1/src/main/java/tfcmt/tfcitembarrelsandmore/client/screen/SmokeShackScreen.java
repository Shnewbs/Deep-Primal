package tfcmt.tfcitembarrelsandmore.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import tfcmt.tfcitembarrelsandmore.common.container.SmokeShackContainer;

public class SmokeShackScreen
extends AbstractContainerScreen<SmokeShackContainer> {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "textures/gui/smoke_shack.png");
    private static final int COL_X = 8;
    private static final int COL_Y = 9;
    private static final int COL_W = 16;
    private static final int COL_H = 50;

    public SmokeShackScreen(SmokeShackContainer container, Inventory playerInventory, Component title) {
        super(container, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = 72;
    }

    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);
    }

    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(BACKGROUND, this.leftPos, this.topPos, 0.0f, 0.0f, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
        this.renderSmokeColumn(graphics, ((SmokeShackContainer)this.menu).getProgress());
    }

    private void renderSmokeColumn(GuiGraphics graphics, float progress) {
        if (progress <= 0.0f) {
            return;
        }
        int screenX = this.leftPos + 8;
        int screenY = this.topPos + 9;
        int fillRows = (int)(50.0f * progress);
        if (fillRows <= 0) {
            return;
        }
        float time = (float)(System.currentTimeMillis() % 2000L) / 2000.0f;
        for (int row = 0; row < fillRows; ++row) {
            int py = screenY + 50 - 1 - row;
            float frac = (float)row / (float)Math.max(fillRows - 1, 1);
            float wobble = (float)Math.sin((double)time * Math.PI * 2.0 + (double)((float)row * 0.45f)) * 2.0f;
            int wobbleOff = (int)wobble;
            int alpha = 80 + (int)(130.0f * frac);
            int grey = 180 + (int)(55.0f * frac);
            int color = alpha << 24 | grey << 16 | grey << 8 | grey;
            int x0 = screenX + Math.max(wobbleOff, 0);
            int x1 = screenX + 16 + Math.min(wobbleOff, 0);
            if (x1 <= x0) continue;
            graphics.fill(x0, py, x1, py + 1, color);
        }
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
        if (this.isHovering(8, 9, 16, 50, mouseX, mouseY)) {
            float progress = ((SmokeShackContainer)this.menu).getProgress();
            int pct = (int)(progress * 100.0f);
            MutableComponent tip = progress <= 0.0f ? Component.translatable((String)"message.tfc_item_barrels_and_more.smoke_shack.idle") : Component.translatable((String)"message.tfc_item_barrels_and_more.smoke_shack.progress", (Object[])new Object[]{pct});
            graphics.renderTooltip(this.font, (Component)tip, mouseX, mouseY);
        }
    }
}

