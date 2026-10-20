package com.shnewbs.deepprimal.mixin.client;

import com.shnewbs.deepprimal.client.WaterColors;
import com.shnewbs.deepprimal.settings.WaterVisualConfig;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ColorResolver;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BiomeColors.class)
abstract class WaterColorMixin {
  @Shadow @Final @Mutable public static ColorResolver WATER_COLOR_RESOLVER;
  @Inject(method="<clinit>",at=@At("RETURN"))
  private static void deepprimal$resolver(CallbackInfo ci) {
    // Replace the registered cache key once; defer configuration access until world rendering.
    WATER_COLOR_RESOLVER=(b,x,z)->WaterVisualConfig.COLORS.get()?WaterColors.palette(b):b.getWaterColor();
  }
  @Inject(method="getAverageWaterColor",at=@At("HEAD"),cancellable=true)
  private static void deepprimal$blend(BlockAndTintGetter level, BlockPos pos, CallbackInfoReturnable<Integer> ci) {
    if(WaterVisualConfig.COLORS.get())ci.setReturnValue(WaterColors.blended(level,pos));
  }
}
