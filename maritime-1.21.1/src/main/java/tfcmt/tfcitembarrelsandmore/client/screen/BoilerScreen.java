package tfcmt.tfcitembarrelsandmore.client.screen;

import net.dries007.tfc.common.component.heat.Heat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import tfcmt.tfcitembarrelsandmore.common.container.BoilerContainer;

public class BoilerScreen
extends AbstractContainerScreen<BoilerContainer> {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "textures/gui/boiler.png");
    private static final int TEMP_X = 12;
    private static final int TEMP_Y = 27;
    private static final int TEMP_W = 12;
    private static final int TEMP_H = 48;
    private static final int EYE_X = 28;
    private static final int EYE_Y = 30;
    private static final int EYE_W = 8;
    private static final int EYE_H = 8;
    private static final float MAX_DISPLAY_TEMP = 1600.0f;

    public BoilerScreen(BoilerContainer container, Inventory playerInventory, Component title) {
        super(container, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(BACKGROUND, this.leftPos, this.topPos, 0.0f, 0.0f, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
        float temp = ((BoilerContainer)this.menu).getTemperature();
        int ashCount = ((BoilerContainer)this.menu).getAshCount();
        boolean lit = ((BoilerContainer)this.menu).isLit();
        this.renderTemperatureBar(graphics, temp, ashCount);
        this.renderEyehole(graphics, temp, ashCount, lit);
    }

    private void renderTemperatureBar(GuiGraphics graphics, float temp, int ashCount) {
        int screenX = this.leftPos + 12;
        int screenY = this.topPos + 27;
        if (temp >= 80.0f) {
            float tempFraction = Math.min(temp / 1600.0f, 1.0f);
            int fillH = (int)(48.0f * tempFraction);
            int fillY = screenY + (48 - fillH);
            int bands = Math.max(1, fillH);
            for (int i = 0; i < bands; ++i) {
                float bandTemp = temp * ((float)i + 0.5f) / (float)bands;
                int color = BoilerScreen.tempToColor(bandTemp);
                int by = fillY + (fillH - 1 - i);
                graphics.fill(screenX, by, screenX + 12, by + 1, 0xFF000000 | color);
            }
        }
        if (ashCount > 0) {
            float ashFraction = Math.min((float)ashCount / 64.0f, 1.0f);
            int ashH = (int)(48.0f * ashFraction);
            int ashY = screenY + (48 - ashH);
            int ashAlpha = (int)(ashFraction * 220.0f);
            graphics.fill(screenX, ashY, screenX + 12, ashY + ashH, ashAlpha << 24 | 0x808080);
        }
    }

    private static int tempToColor(float temp) {
        if (temp < 80.0f) {
            return 0;
        }
        if (temp < 180.0f) {
            return BoilerScreen.lerpColor(0, 0x4D0000, (temp - 80.0f) / 100.0f);
        }
        if (temp < 480.0f) {
            return BoilerScreen.lerpColor(0x4D0000, 9116160, (temp - 180.0f) / 300.0f);
        }
        if (temp < 580.0f) {
            return BoilerScreen.lerpColor(9116160, 16729344, (temp - 480.0f) / 100.0f);
        }
        if (temp < 700.0f) {
            return BoilerScreen.lerpColor(16729344, 16747520, (temp - 580.0f) / 120.0f);
        }
        if (temp < 800.0f) {
            return BoilerScreen.lerpColor(16747520, 16766720, (temp - 700.0f) / 100.0f);
        }
        if (temp < 1000.0f) {
            return BoilerScreen.lerpColor(16766720, 0xFFFF00, (temp - 800.0f) / 200.0f);
        }
        return BoilerScreen.lerpColor(0xFFFF00, 0xFFFFFF, (temp - 1000.0f) / 400.0f);
    }

    private static int lerpColor(int from, int to, float t) {
        t = Math.max(0.0f, Math.min(1.0f, t));
        int r = (int)((float)(from >> 16 & 0xFF) + t * (float)((to >> 16 & 0xFF) - (from >> 16 & 0xFF)));
        int g = (int)((float)(from >> 8 & 0xFF) + t * (float)((to >> 8 & 0xFF) - (from >> 8 & 0xFF)));
        int b = (int)((float)(from & 0xFF) + t * (float)((to & 0xFF) - (from & 0xFF)));
        return r << 16 | g << 8 | b;
    }

    private void renderEyehole(GuiGraphics graphics, float temp, int ashCount, boolean lit) {
        float tempFactor;
        int screenX = this.leftPos + 28;
        int screenY = this.topPos + 30;
        if (!lit || temp < 80.0f) {
            graphics.fill(screenX, screenY, screenX + 8, screenY + 8, -15066598);
            return;
        }
        float time = (float)(System.currentTimeMillis() % 1600L) / 1600.0f;
        float flicker = (float)((Math.sin((double)time * Math.PI * 2.0) + 1.0) * 0.5);
        float ashDamp = 1.0f - Math.max(0.0f, ((float)ashCount / 64.0f - 0.6f) / 0.4f);
        float combined = Math.min((flicker *= ashDamp) * 0.6f + (tempFactor = Math.min((temp - 80.0f) / 920.0f, 1.0f)) * 0.4f, 1.0f);
        int glowColor = combined < 0.33f ? BoilerScreen.lerpColor(16729344, 16747520, combined / 0.33f) : (combined < 0.66f ? BoilerScreen.lerpColor(16747520, 16766720, (combined - 0.33f) / 0.33f) : BoilerScreen.lerpColor(16766720, 0xFFFFFF, (combined - 0.66f) / 0.34f));
        graphics.fill(screenX, screenY, screenX + 8, screenY + 8, 0xFF000000 | glowColor);
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int ash;
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
        if (this.isHovering(12, 27, 12, 48, mouseX, mouseY)) {
            float temp = ((BoilerContainer)this.menu).getTemperature();
            Heat heat = Heat.getHeat((float)temp);
            MutableComponent tooltip = heat == null ? Component.literal((String)String.format("%.1f\u00b0C", Float.valueOf(temp))) : net.dries007.tfc.util.Helpers.translateEnum(heat).copy().withStyle(s -> s.withColor(heat.getColor())).append((Component)Component.literal((String)String.format(" (%.1f\u00b0C)", Float.valueOf(temp))));
            graphics.renderTooltip(this.font, (Component)tooltip, mouseX, mouseY);
        }
        if (this.isHovering(71, 44, 16, 16, mouseX, mouseY) && (ash = ((BoilerContainer)this.menu).getAshCount()) == 64) {
            graphics.renderTooltip(this.font, (Component)Component.translatable((String)"message.tfc_item_barrels_and_more.boiler.ash_full"), mouseX, mouseY);
        }
        if (this.isHovering(28, 30, 8, 8, mouseX, mouseY) && ((BoilerContainer)this.menu).isVentBlocked()) {
            graphics.renderTooltip(this.font, (Component)Component.translatable((String)"message.tfc_item_barrels_and_more.boiler.no_sky"), mouseX, mouseY);
        }
    }
}

