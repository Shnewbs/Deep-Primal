package com.shnewbs.deepprimal.themes.mixin;

import com.shnewbs.deepprimal.themes.ThemeMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(GuiGraphicsExtractor.class)
abstract class ThemeLabelMixin {
    @ModifyVariable(method = "text(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;IIIZ)V", at = @At("HEAD"), argsOnly = true, ordinal = 2)
    private int deepprimalThemes$labelColor(int color) {
        if (ThemeMod.active().custom() && (color & 0xffffff) == 0x404040 && Minecraft.getInstance().screen instanceof AbstractContainerScreen<?>)
            return ThemeMod.active().text;
        return color;
    }
}
