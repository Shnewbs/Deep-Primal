package com.shnewbs.deepprimal.mixin.compat;

import com.shnewbs.deepprimal.compat.BuiltinRegistryFallback;
import net.minecraft.core.RegistryAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Optional: compose built-ins before KubeJS builds its registry serialization contexts. */
@Pseudo
@Mixin(targets = "dev.latvian.mods.kubejs.util.RegistryAccessContainer", remap = false)
abstract class KubeJsRegistryContextMixin {
  @ModifyVariable(method = "<init>", at = @At("HEAD"), argsOnly = true, remap = false)
  private static RegistryAccess.Frozen deepprimal$completeContext(RegistryAccess.Frozen source) {
    return BuiltinRegistryFallback.complete(source);
  }
}
