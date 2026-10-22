package tfcmt.tfcitembarrelsandmore.client.screen;

import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import tfcmt.tfcitembarrelsandmore.common.PistonPressure;
import tfcmt.tfcitembarrelsandmore.common.container.AirCompressorContainer;
import tfcmt.tfcitembarrelsandmore.common.network.TIBAMNetwork;

public class AirCompressorScreen
extends AbstractContainerScreen<AirCompressorContainer> {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "textures/gui/compressor.png");
    private static final int COLOR_KNOB_MID = -9799030;
    private static final int COLOR_KNOB_HI = -6639681;
    private static final int COLOR_KNOB_SHADOW = -14009788;
    private static final int COLOR_LABEL = 10137535;
    private static final int COLOR_AIR_FILL = -11886657;
    private static final int COLOR_AIR_BG = -16115680;
    private static final int COLOR_TRACK = -12957100;
    private static final int COLOR_BTN_FACE = -14009788;
    private static final int COLOR_BTN_HI = -11902864;
    private static final int COLOR_BTN_SHADOW = -16115680;
    private static final int COLOR_BTN_LABEL = 10137535;
    private static final int IMG_W = 176;
    private static final int IMG_H = 166;
    private static final int PG_X = 20;
    private static final int PG_Y = 8;
    private static final int PG_W = 60;
    private static final int PG_H = 56;
    private static final float DIAL_MIN_DEG = -110.0f;
    private static final float DIAL_MAX_DEG = 110.0f;
    private static final int NEEDLE_LEN = 18;
    private static final int TIER_BAND_H = 6;
    private static final int AB_X = 84;
    private static final int AB_Y = 8;
    private static final int AB_W = 8;
    private static final int AB_H = 66;
    private static final int BL_PLUS_X = 118;
    private static final int BL_PLUS_Y = 6;
    private static final int BL_MINUS_X = 118;
    private static final int BL_MINUS_Y = 68;
    private static final int BL_BTN_W = 10;
    private static final int BL_BTN_H = 8;
    private static final int BL_TRACK_X = 122;
    private static final int BL_TRACK_Y1 = 16;
    private static final int BL_TRACK_Y2 = 66;
    private static final int BL_KNOB_W = 8;
    private static final int BL_KNOB_H = 5;
    private static final int BL_TRAVEL = 45;

    public AirCompressorScreen(AirCompressorContainer container, Inventory playerInventory, Component title) {
        super((AbstractContainerMenu)container, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(BACKGROUND, this.leftPos, this.topPos, 0.0f, 0.0f, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
        this.renderAirBar(graphics);
        this.renderPsiGauge(graphics);
        this.renderBleederLever(graphics);
        this.renderRpmLabel(graphics);
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
        this.renderInstrumentTooltips(graphics, mouseX, mouseY);
    }

    private void renderAirBar(GuiGraphics graphics) {
        graphics.fill(this.leftPos + 84, this.topPos + 8, this.leftPos + 84 + 8, this.topPos + 8 + 66, -16115680);
        int maxAir = ((AirCompressorContainer)this.menu).getMaxAir();
        if (maxAir <= 0) {
            return;
        }
        int fillH = (int)((float)((AirCompressorContainer)this.menu).getCompressedAir() / (float)maxAir * 66.0f);
        if (fillH > 0) {
            graphics.fill(this.leftPos + 84, this.topPos + 8 + (66 - fillH), this.leftPos + 84 + 8, this.topPos + 8 + 66, -11886657);
        }
    }

    private void renderPsiGauge(GuiGraphics graphics) {
        int psi = ((AirCompressorContainer)this.menu).getPsi();
        PistonPressure tier = PistonPressure.getTier(psi);
        if (tier == null) {
            return;
        }
        int tierColor = tier.getColor();
        int bx = this.leftPos + 20 + 2;
        int by = this.topPos + 8 + 56 - 6 - 2;
        graphics.fill(bx, by, bx + 60 - 4, by + 6, 0xFF000000 | tierColor);
        int dialCX = this.leftPos + 20 + 30;
        int dialCY = this.topPos + 8 + 22 + 2;
        float psiNorm = Math.max(0.0f, Math.min((float)psi / 100.0f, 1.0f));
        float angleDeg = -110.0f + psiNorm * 220.0f;
        double angleRad = Math.toRadians((double)angleDeg - 90.0);
        int nx = (int)((double)dialCX + 18.0 * Math.cos(angleRad));
        int ny = (int)((double)dialCY + 18.0 * Math.sin(angleRad));
        AirCompressorScreen.drawLine(graphics, dialCX, dialCY, nx, ny, 0xFF000000 | tierColor);
        graphics.fill(dialCX - 1, dialCY - 1, dialCX + 1, dialCY + 1, -5089758);
    }

    private void renderBleederLever(GuiGraphics graphics) {
        graphics.fill(this.leftPos + 122, this.topPos + 16, this.leftPos + 122 + 1, this.topPos + 66, -12957100);
        int rate = ((AirCompressorContainer)this.menu).getBleederRate();
        int ky = 61 - Math.round((float)rate / 10.0f * 45.0f);
        int kx = 118;
        this.drawLeverKnob(graphics, this.leftPos + kx, this.topPos + ky, 8, 5);
        this.drawStepButton(graphics, this.leftPos + 118, this.topPos + 6, 10, 8, "+");
        this.drawStepButton(graphics, this.leftPos + 118, this.topPos + 68, 10, 8, "\u2212");
    }

    private void renderRpmLabel(GuiGraphics graphics) {
        float rpm = ((AirCompressorContainer)this.menu).getCurrentRpm();
        String text = rpm > 0.0f ? String.format("%.0f RPM", Float.valueOf(rpm)) : "No shaft";
        int lx = this.leftPos + 20 + (60 - this.font.width(text)) / 2;
        int ly = this.topPos + 8 + 56 + 2;
        graphics.drawString(this.font, text, lx, ly, 10137535, false);
    }

    private void renderInstrumentTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
        int blZoneH;
        int psi;
        PistonPressure tier;
        if (this.isHovering(84, 8, 8, 66, mouseX, mouseY)) {
            int maxAir = ((AirCompressorContainer)this.menu).getMaxAir();
            int pct = maxAir > 0 ? (int)((float)((AirCompressorContainer)this.menu).getCompressedAir() / (float)maxAir * 100.0f) : 0;
            graphics.renderTooltip(this.font, (Component)Component.literal((String)("Compressed air: " + ((AirCompressorContainer)this.menu).getCompressedAir() + " / " + maxAir + " (" + pct + "%)")).withStyle(s -> s.withColor(4890559)), mouseX, mouseY);
        }
        if (this.isHovering(20, 8, 60, 56, mouseX, mouseY) && (tier = PistonPressure.getTier(psi = ((AirCompressorContainer)this.menu).getPsi())) != null) {
            graphics.renderTooltip(this.font, (Component)tier.getDisplayName().copy().withStyle(s -> s.withColor(tier.getColor())).append((Component)Component.literal((String)(" (" + psi + " PSI)")).withStyle(s -> s.withColor(0xAAAAAA))), mouseX, mouseY);
        }
        if (this.isHovering(118, 6, 13, blZoneH = 70, mouseX, mouseY)) {
            int rate = ((AirCompressorContainer)this.menu).getBleederRate();
            graphics.renderTooltip(this.font, (Component)(rate == 0 ? Component.literal((String)"Bleeder: off").withStyle(s -> s.withColor(0xAAAAAA)) : Component.literal((String)("Bleeding: " + rate + " air/s")).withStyle(s -> s.withColor(4890559))), mouseX, mouseY);
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (this.isHovering(118, 6, 10, 8, mouseX, mouseY)) {
                this.sendBleeder(1);
                return true;
            }
            if (this.isHovering(118, 68, 10, 8, mouseX, mouseY)) {
                this.sendBleeder(-1);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void sendBleeder(int delta) {
        int cur = ((AirCompressorContainer)this.menu).getBleederRate();
        int next = Math.max(0, Math.min(10, cur + delta));
        if (next != cur) {
            ((AirCompressorContainer)this.menu).setOptimisticBleederRate(next);
            TIBAMNetwork.setBleederRate(((AirCompressorContainer)this.menu).getEnginePos(), next);
            this.click();
        }
    }

    private void click() {
        Minecraft.getInstance().getSoundManager().play((SoundInstance)SimpleSoundInstance.forUI((Holder)SoundEvents.UI_BUTTON_CLICK, (float)1.0f));
    }

    private void drawLeverKnob(GuiGraphics graphics, int sx, int sy, int w, int h) {
        graphics.fill(sx, sy, sx + w, sy + h, -9799030);
        graphics.fill(sx + 1, sy, sx + w - 1, sy + 1, -6639681);
        graphics.fill(sx, sy + 1, sx + 1, sy + h - 1, -6639681);
        graphics.fill(sx + 1, sy + h - 1, sx + w - 1, sy + h, -14009788);
        graphics.fill(sx + w - 1, sy + 1, sx + w, sy + h - 1, -14009788);
        graphics.fill(sx + 2, sy + h / 2, sx + w - 2, sy + h / 2 + 1, -14009788);
    }

    private void drawStepButton(GuiGraphics graphics, int sx, int sy, int w, int h, String label) {
        graphics.fill(sx, sy, sx + w, sy + h, -14009788);
        graphics.fill(sx, sy, sx + w, sy + 1, -11902864);
        graphics.fill(sx, sy, sx + 1, sy + h, -11902864);
        graphics.fill(sx, sy + h - 1, sx + w, sy + h, -16115680);
        graphics.fill(sx + w - 1, sy, sx + w, sy + h, -16115680);
        int lx = sx + (w - this.font.width(label)) / 2;
        Objects.requireNonNull(this.font);
        int ly = sy + (h - 9) / 2 + 1;
        graphics.drawString(this.font, label, lx, ly, 10137535, false);
    }

    private static void drawLine(GuiGraphics graphics, int x0, int y0, int x1, int y1, int color) {
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;
        while (true) {
            graphics.fill(x0, y0, x0 + 1, y0 + 1, color);
            if (x0 == x1 && y0 == y1) break;
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x0 += sx;
            }
            if (e2 >= dx) continue;
            err += dx;
            y0 += sy;
        }
    }
}

