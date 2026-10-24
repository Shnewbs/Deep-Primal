package com.shnewbs.deepprimal.themes.mixin;
import com.shnewbs.deepprimal.themes.ThemeArt;
import com.shnewbs.deepprimal.themes.ThemeMod;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Screen.class)
abstract class ThemeMenuMixin {
    @Inject(method="extractMenuBackground(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIII)V",at=@At("HEAD"),cancellable=true)
    private void deepprimalThemes$menu(GuiGraphicsExtractor g,int x,int y,int w,int h,CallbackInfo ci){
        var t=ThemeMod.active();if(!t.custom())return;
        ThemeArt.region(g,t,ThemeArt.of(t).material(),x,y,w,h);g.fill(x,y,x+w,y+h,0xb0000000);ci.cancel();
    }
}
