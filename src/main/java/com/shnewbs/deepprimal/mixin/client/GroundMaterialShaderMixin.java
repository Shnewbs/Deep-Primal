package com.shnewbs.deepprimal.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.shaders.ShaderType;
import com.shnewbs.deepprimal.client.GroundMaterialShaders;
import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ShaderManager.class)
public abstract class GroundMaterialShaderMixin {
  @ModifyExpressionValue(method = "loadShader", at = @At(value = "INVOKE",
      target = "Lorg/apache/commons/io/IOUtils;toString(Ljava/io/Reader;)Ljava/lang/String;"))
  private static String deepprimal$materials(String source, @Local(argsOnly = true) Identifier location,
      @Local(argsOnly = true) ShaderType type) {
    return GroundMaterialShaders.patch(location, type, source);
  }
}
