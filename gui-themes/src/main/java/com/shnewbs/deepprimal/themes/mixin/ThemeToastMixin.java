package com.shnewbs.deepprimal.themes.mixin;

import com.shnewbs.deepprimal.themes.ThemeMod;
import net.minecraft.client.gui.components.toasts.AdvancementToast;
import net.minecraft.client.gui.components.toasts.RecipeToast;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

/** Match toast text to the themed backplate, including preformatted tutorial lines.
 * Keep the native alpha so advancement text still fades normally. */
@Mixin({RecipeToast.class,SystemToast.class,TutorialToast.class,AdvancementToast.class})
abstract class ThemeToastMixin {
    @ModifyArgs(method="extractRenderState",at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)V"),require=0)
    private void deepprimalThemes$componentContrast(Args args){
        var theme=ThemeMod.active();if(!theme.custom())return;
        Component message=args.get(1);
        args.set(1,message.copy().withStyle(style->style.withColor(theme.text).withoutShadow()));
        args.set(4,((int)args.get(4)&0xff000000)|(theme.text&0xffffff));
        args.set(5,false);
    }
    @ModifyArgs(method="extractRenderState",at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;IIIZ)V"),require=0)
    private void deepprimalThemes$formattedContrast(Args args){
        var theme=ThemeMod.active();if(!theme.custom())return;
        FormattedCharSequence message=args.get(1);
        FormattedCharSequence readable=sink->message.accept((index,style,codePoint)->sink.accept(index,style.withColor(theme.text).withoutShadow(),codePoint));
        args.set(1,readable);
        args.set(4,((int)args.get(4)&0xff000000)|(theme.text&0xffffff));
        args.set(5,false);
    }
}
