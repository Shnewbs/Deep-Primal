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
            int color = (ThemeMod.active().accent & 0xffffff) | opacity;
            g.pose().pushMatrix(); g.pose().scale(2, 2);
            g.centeredText(font, "DEEP PRIMAL", width / 4, y / 2, color);
            g.pose().popMatrix();
            g.centeredText(font, ThemeMod.active().displayName, width / 2, y + 26, 0xffffff | opacity);
            g.horizontalLine(width / 2 - 90, width / 2 + 90, y + 40, color);
        }
        ci.cancel();
    }
}
