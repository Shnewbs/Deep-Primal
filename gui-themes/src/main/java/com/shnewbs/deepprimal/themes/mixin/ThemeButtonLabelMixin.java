package com.shnewbs.deepprimal.themes.mixin;
import com.shnewbs.deepprimal.themes.ThemeMod;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.components.AbstractButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(AbstractButton.class)
abstract class ThemeButtonLabelMixin {
    @Inject(method="extractDefaultLabel",at=@At("HEAD"))
    private void deepprimalThemes$contrast(ActiveTextCollector output,CallbackInfo ci){
        var b=(AbstractButton)(Object)this;
        b.setFGColor(ThemeMod.active().custom()?ThemeMod.active().text:AbstractButton.UNSET_FG_COLOR);
    }
}
