package com.shnewbs.deepprimal.themes.mixin;

import com.shnewbs.deepprimal.themes.ThemeMod;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
abstract class ThemeRecoveryMixin {
    @Inject(method = "clearResourcePacksOnError", at = @At("HEAD"))
    private void deepprimalThemes$restoreVanilla(CallbackInfo ci) {
        ThemeMod.recoverFromPackFailure();
    }
}
