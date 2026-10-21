package com.shnewbs.deepprimal.settings.mixin.client;

import com.shnewbs.deepprimal.settings.PresetCapture;
import net.minecraft.client.GraphicsPreset;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GraphicsPreset.class)
public abstract class GraphicsPresetMixin {
  @Inject(method = "set", at = @At("HEAD"), cancellable = true)
  private <T> void deepprimal$stage(
      OptionsSubScreen screen, OptionInstance<T> option, T value, CallbackInfo ci) {
    if (PresetCapture.capture(option, value)) ci.cancel();
  }
}
