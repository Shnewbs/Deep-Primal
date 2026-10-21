package com.shnewbs.deepprimal.themes.mixin;

import com.shnewbs.deepprimal.themes.ThemeConfig;
import com.shnewbs.deepprimal.themes.ThemeMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.LogoRenderer;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LogoRenderer.class)
abstract class ThemeLogoMixin {
    @Inject(method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IFI)V", at = @At("HEAD"), cancellable = true)
    private void deepprimalThemes$title(GuiGraphicsExtractor g, int width, float alpha, int y, CallbackInfo ci) {
        if (!ThemeMod.active().custom() || !ThemeConfig.TITLE_BRANDING.get() || !(Minecraft.getInstance().screen instanceof TitleScreen)) return;
        int opacity = (int)(Math.clamp(alpha, 0, 1) * 255) << 24;
        if (opacity != 0) {
            var font = Minecraft.getInstance().font;
            int color = (ThemeMod.active().text & 0xffffff) | opacity;
            int frame = (ThemeMod.active().accent & 0xffffff) | opacity;
            int background = (ThemeMod.active().surface & 0xffffff) | opacity;
            g.fill(width / 2 - 104, y - 6, width / 2 + 104, y + 46, background);
            g.outline(width / 2 - 104, y - 6, 208, 52, frame);
            g.pose().pushMatrix(); g.pose().scale(2, 2);
            g.centeredText(font, "DEEP PRIMAL", width / 4, y / 2, color);
            g.pose().popMatrix();
            g.centeredText(font, ThemeMod.active().displayName, width / 2, y + 26, color);
            g.horizontalLine(width / 2 - 90, width / 2 + 90, y + 40, frame);
        }
        ci.cancel();
    }
}
