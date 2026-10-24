package com.shnewbs.deepprimal.themes.mixin;

import com.shnewbs.deepprimal.themes.ThemeArt;
import com.shnewbs.deepprimal.themes.ThemeMod;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

/** Change only the submitted text; never mutate a widget's own foreground setting. */
@Mixin(AbstractButton.class)
abstract class ThemeButtonLabelMixin {
    @ModifyArgs(method="extractDefaultLabel",at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/components/AbstractButton;extractScrollingStringOverContents(Lnet/minecraft/client/gui/ActiveTextCollector;Lnet/minecraft/network/chat/Component;I)V"))
    private void deepprimalThemes$cleanLabel(Args args){
        var theme=ThemeMod.active();var button=(AbstractButton)(Object)this;
        if(!theme.custom()||((ThemeWidgetAccessor)button).deepprimalThemes$foreground()!=AbstractButton.UNSET_FG_COLOR)return;
        Component message=args.get(1);
        args.set(1,message.copy().withStyle(style->style.withColor(ThemeArt.textColor(theme,button.isHoveredOrFocused(),button.active)).withoutShadow()));
    }
}
