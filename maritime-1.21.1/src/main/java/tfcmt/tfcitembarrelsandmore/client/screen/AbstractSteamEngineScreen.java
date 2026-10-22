package tfcmt.tfcitembarrelsandmore.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Objects;
import net.dries007.tfc.client.RenderHelpers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import tfcmt.tfcitembarrelsandmore.common.PistonPressure;
import tfcmt.tfcitembarrelsandmore.common.container.ISteamEngineMenu;
import tfcmt.tfcitembarrelsandmore.common.network.TIBAMNetwork;

public abstract class AbstractSteamEngineScreen<M extends AbstractContainerMenu>
extends AbstractContainerScreen<M> {
    protected static final int IMG_W = 176;
    protected static final int IMG_H = 166;
    private static final int WB_X = 8;
    private static final int WB_Y = 8;
    private static final int WB_W = 8;
    private static final int WB_H = 66;
    private static final int PG_X = 20;
    private static final int PG_Y = 8;
    private static final int PG_W = 60;
    private static final int PG_H = 56;
    private static final float DIAL_MIN_DEG = -110.0f;
    private static final float DIAL_MAX_DEG = 110.0f;
    private static final int NEEDLE_LEN = 18;
    private static final int TIER_BAND_H = 6;
    private static final int SB_X = 84;
    private static final int SB_Y = 8;
    private static final int SB_W = 8;
    private static final int SB_H = 66;
    private static final int TH_TRACK_X = 102;
    private static final int TH_TRACK_Y1 = 16;
    private static final int TH_TRACK_Y2 = 66;
    private static final int TH_KNOB_W = 8;
    private static final int TH_KNOB_H = 5;
    private static final int TH_STEP = 5;
    private static final int TH_TRAVEL = 45;
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
    protected final Palette palette;
    private boolean throttleDragging = false;
    private int throttleLastSent = -1;

    protected AbstractSteamEngineScreen(M container, Inventory playerInventory, Component title, Palette palette) {
        super(container, playerInventory, title);
        this.palette = palette;
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    protected final void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        this.renderTexture(graphics, partialTick, mouseX, mouseY);
        this.renderWaterBar(graphics);
        this.renderSteamBar(graphics);
        this.renderPsiGauge(graphics);
        this.renderThrottleLever(graphics, ((ISteamEngineMenu)this.menu).getThrottle());
        this.renderLever(graphics, 122, 16, 66, 8, 5, 45, 118, 6, 118, 68, 10, 8, ((ISteamEngineMenu)this.menu).getBleederTier(), 4, false);
    }

    protected abstract void renderTexture(GuiGraphics var1, float var2, int var3, int var4);

    private void renderWaterBar(GuiGraphics graphics) {
        int capacity;
        FluidTank tank = ((ISteamEngineMenu)this.menu).getWaterTank();
        if (tank.isEmpty()) {
            return;
        }
        FluidStack fluid = tank.getFluid();
        int amount = fluid.getAmount();
        int fillH = (int)((float)amount / (float)(capacity = tank.getCapacity()) * 66.0f);
        if (fillH <= 0) {
            return;
        }
        TextureAtlasSprite sprite = (TextureAtlasSprite)Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(IClientFluidTypeExtensions.of((Fluid)fluid.getFluid()).getStillTexture(fluid));
        RenderSystem.setShaderTexture((int)0, (ResourceLocation)InventoryMenu.BLOCK_ATLAS);
        RenderHelpers.fillAreaWithSprite((GuiGraphics)graphics, (TextureAtlasSprite)sprite, (int)(this.leftPos + 8), (int)(this.topPos + 8 + (66 - fillH)), (int)8, (int)fillH, (int)16, (int)16);
    }

    private void renderSteamBar(GuiGraphics graphics) {
        int maxSteam = ((ISteamEngineMenu)this.menu).getMaxSteam();
        if (maxSteam <= 0) {
            return;
        }
        graphics.fill(this.leftPos + 84, this.topPos + 8, this.leftPos + 84 + 8, this.topPos + 8 + 66, this.palette.steamBarBg());
        int steam = ((ISteamEngineMenu)this.menu).getSteam();
        int fillH = (int)((float)steam / (float)maxSteam * 66.0f);
        if (fillH > 0) {
            graphics.fill(this.leftPos + 84, this.topPos + 8 + (66 - fillH), this.leftPos + 84 + 8, this.topPos + 8 + 66, this.palette.steamBarFill());
        }
    }

    private void renderPsiGauge(GuiGraphics graphics) {
        int psi = ((ISteamEngineMenu)this.menu).getPsi();
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
        AbstractSteamEngineScreen.drawLine(graphics, dialCX, dialCY, nx, ny, 0xFF000000 | tierColor);
        graphics.fill(dialCX - 1, dialCY - 1, dialCX + 1, dialCY + 1, -5089758);
    }

    private void renderThrottleLever(GuiGraphics graphics, int value) {
        graphics.fill(this.leftPos + 102, this.topPos + 16, this.leftPos + 102 + 1, this.topPos + 66, this.palette.trackColor());
        int ky = 61 - Math.round((float)value / 100.0f * 45.0f);
        int kx = 98;
        if (this.throttleDragging) {
            this.drawLeverKnobHighlighted(graphics, this.leftPos + kx, this.topPos + ky, 8, 5);
        } else {
            this.drawLeverKnob(graphics, this.leftPos + kx, this.topPos + ky, 8, 5);
        }
        String label = value + "%";
        int lx = this.leftPos + 102 + 1 - this.font.width(label) / 2;
        Objects.requireNonNull(this.font);
        int ly = this.topPos + 16 - 9 - 3;
        graphics.drawString(this.font, label, lx, ly, this.palette.labelColor(), false);
    }

    private void renderLever(GuiGraphics graphics, int trackX, int trackY1, int trackY2, int knobW, int knobH, int travel, int plusX, int plusY, int minusX, int minusY, int btnW, int btnH, int value, int maxValue, boolean showLabel) {
        graphics.fill(this.leftPos + trackX, this.topPos + trackY1, this.leftPos + trackX + 1, this.topPos + trackY2, this.palette.trackColor());
        int ky = trackY2 - knobH - Math.round((float)value / (float)maxValue * (float)travel);
        int kx = trackX - knobW / 2;
        this.drawLeverKnob(graphics, this.leftPos + kx, this.topPos + ky, knobW, knobH);
        this.drawStepButton(graphics, this.leftPos + plusX, this.topPos + plusY, btnW, btnH, "+");
        this.drawStepButton(graphics, this.leftPos + minusX, this.topPos + minusY, btnW, btnH, "\u2212");
        if (showLabel) {
            String label = value + "%";
            int lx = this.leftPos + plusX + btnW / 2 - this.font.width(label) / 2;
            Objects.requireNonNull(this.font);
            int ly = this.topPos + plusY - 9 - 7;
            graphics.drawString(this.font, label, lx, ly, this.palette.labelColor(), false);
        }
    }

    private void drawLeverKnob(GuiGraphics graphics, int sx, int sy, int w, int h) {
        graphics.fill(sx, sy, sx + w, sy + h, this.palette.knobMid());
        graphics.fill(sx + 1, sy, sx + w - 1, sy + 1, this.palette.knobHi());
        graphics.fill(sx, sy + 1, sx + 1, sy + h - 1, this.palette.knobHi());
        graphics.fill(sx + 1, sy + h - 1, sx + w - 1, sy + h, this.palette.knobShadow());
        graphics.fill(sx + w - 1, sy + 1, sx + w, sy + h - 1, this.palette.knobShadow());
        graphics.fill(sx + 2, sy + h / 2, sx + w - 2, sy + h / 2 + 1, this.palette.knobShadow());
    }

    private void drawLeverKnobHighlighted(GuiGraphics graphics, int sx, int sy, int w, int h) {
        graphics.fill(sx, sy, sx + w, sy + h, this.palette.knobHi());
        graphics.fill(sx + 1, sy, sx + w - 1, sy + 1, this.palette.knobMid());
        graphics.fill(sx, sy + 1, sx + 1, sy + h - 1, this.palette.knobMid());
        graphics.fill(sx + 1, sy + h - 1, sx + w - 1, sy + h, this.palette.knobShadow());
        graphics.fill(sx + w - 1, sy + 1, sx + w, sy + h - 1, this.palette.knobShadow());
        graphics.fill(sx + 2, sy + h / 2, sx + w - 2, sy + h / 2 + 1, this.palette.knobShadow());
    }

    private void drawStepButton(GuiGraphics graphics, int sx, int sy, int w, int h, String label) {
        graphics.fill(sx, sy, sx + w, sy + h, this.palette.btnFace());
        graphics.fill(sx, sy, sx + w, sy + 1, this.palette.btnHi());
        graphics.fill(sx, sy, sx + 1, sy + h, this.palette.btnHi());
        graphics.fill(sx, sy + h - 1, sx + w, sy + h, this.palette.btnShadow());
        graphics.fill(sx + w - 1, sy, sx + w, sy + h, this.palette.btnShadow());
        int lx = sx + (w - this.font.width(label)) / 2;
        Objects.requireNonNull(this.font);
        int ly = sy + (h - 9) / 2 + 1;
        graphics.drawString(this.font, label, lx, ly, this.palette.btnLabel(), false);
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
        this.renderInstrumentTooltips(graphics, mouseX, mouseY);
    }

    protected void renderInstrumentTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
        int blZoneH;
        int psi;
        PistonPressure tier;
        if (this.isHovering(8, 8, 8, 66, mouseX, mouseY)) {
            graphics.renderTooltip(this.font, (Component)Component.literal((String)(((ISteamEngineMenu)this.menu).getWaterAmount() + " / " + ((ISteamEngineMenu)this.menu).getWaterCapacity() + " mB")).withStyle(s -> s.withColor(5939412)), mouseX, mouseY);
        }
        if (this.isHovering(84, 8, 8, 66, mouseX, mouseY)) {
            int pct = ((ISteamEngineMenu)this.menu).getMaxSteam() > 0 ? (int)((float)((ISteamEngineMenu)this.menu).getSteam() / (float)((ISteamEngineMenu)this.menu).getMaxSteam() * 100.0f) : 0;
            graphics.renderTooltip(this.font, (Component)Component.literal((String)("Steam reserve: " + ((ISteamEngineMenu)this.menu).getSteam() + " / " + ((ISteamEngineMenu)this.menu).getMaxSteam() + " (" + pct + "%)")).withStyle(s -> s.withColor(13162720)), mouseX, mouseY);
        }
        if (this.isHovering(20, 8, 60, 56, mouseX, mouseY) && (tier = PistonPressure.getTier(psi = ((ISteamEngineMenu)this.menu).getPsi())) != null) {
            graphics.renderTooltip(this.font, (Component)tier.getDisplayName().copy().withStyle(s -> s.withColor(tier.getColor())).append((Component)Component.literal((String)(" (" + psi + " PSI)")).withStyle(s -> s.withColor(0xAAAAAA))), mouseX, mouseY);
        }
        if (this.isHovering(96, 16, 12, 50, mouseX, mouseY)) {
            int t = ((ISteamEngineMenu)this.menu).getThrottle();
            String tip = t == 0 ? "Throttle: closed" : (t == 100 ? "Throttle: fully open" : "Throttle: " + t + "%");
            graphics.renderTooltip(this.font, (Component)Component.literal((String)tip).withStyle(s -> s.withColor(this.palette.labelColor())), mouseX, mouseY);
        }
        if (this.isHovering(118, 6, 24, blZoneH = 70, mouseX, mouseY)) {
            int tierIdx = ((ISteamEngineMenu)this.menu).getBleederTier();
            if (tierIdx <= 0) {
                graphics.renderTooltip(this.font, (Component)Component.literal((String)"Bleeder: off").withStyle(s -> s.withColor(0xAAAAAA)), mouseX, mouseY);
            } else {
                PistonPressure tier2 = PistonPressure.values()[tierIdx];
                graphics.renderTooltip(this.font, (Component)Component.literal((String)"Bleeder: ").withStyle(s -> s.withColor(0xAAAAAA)).append((Component)tier2.getDisplayName().copy().withStyle(s -> s.withColor(tier2.getColor()))), mouseX, mouseY);
            }
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int thHitX = 96;
            int thHitW = 12;
            if (this.isHovering(thHitX, 16, thHitW, 50, mouseX, mouseY)) {
                this.throttleDragging = true;
                this.throttleLastSent = -1;
                this.applyThrottleFromMouseY(mouseY);
                return true;
            }
            if (this.isHovering(118, 6, 10, 8, mouseX, mouseY)) {
                this.sendBleederTier(1);
                return true;
            }
            if (this.isHovering(118, 68, 10, 8, mouseX, mouseY)) {
                this.sendBleederTier(-1);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == 0 && this.throttleDragging) {
            this.applyThrottleFromMouseY(mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && this.throttleDragging) {
            this.throttleDragging = false;
            this.throttleLastSent = -1;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void applyThrottleFromMouseY(double mouseY) {
        double panelY = mouseY - (double)this.topPos;
        double trackTop = 18.5;
        double trackBottom = 63.5;
        double fraction = 1.0 - (panelY - trackTop) / (trackBottom - trackTop);
        int raw = (int)Math.round((fraction = Math.max(0.0, Math.min(1.0, fraction))) * 100.0);
        int snap = Math.round((float)raw / 5.0f) * 5;
        int next = Math.max(0, Math.min(100, snap));
        if (next != this.throttleLastSent) {
            this.throttleLastSent = next;
            ((ISteamEngineMenu)this.menu).setOptimisticThrottle(next);
            TIBAMNetwork.setThrottle(((ISteamEngineMenu)this.menu).getEnginePos(), next);
            this.click();
        }
    }

    private void sendBleederTier(int delta) {
        int cur = ((ISteamEngineMenu)this.menu).getBleederTier();
        int next = Math.max(0, Math.min(4, cur + delta));
        if (next != cur) {
            ((ISteamEngineMenu)this.menu).setOptimisticBleederTier(next);
            TIBAMNetwork.setBleederTier(((ISteamEngineMenu)this.menu).getEnginePos(), next);
            this.click();
        }
    }

    private void click() {
        Minecraft.getInstance().getSoundManager().play((SoundInstance)SimpleSoundInstance.forUI((Holder)SoundEvents.UI_BUTTON_CLICK, (float)1.0f));
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

    public record Palette(int panelBg, int knobMid, int knobHi, int knobShadow, int labelColor, int steamBarFill, int steamBarBg, int trackColor, int btnFace, int btnHi, int btnShadow, int btnLabel) {
    }
}

