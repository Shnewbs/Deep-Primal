package com.shnewbs.deepprimal.client.render;

import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import net.minecraft.client.Minecraft;

/** Reuse the native day/night, weather, brightness and vision lightmap. */
final class GpuLighting {
  static void bind(RenderPass pass) {
    pass.bindTexture("NativeLight", Minecraft.getInstance().gameRenderer.lightmap(),
        RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST));
  }
  private GpuLighting() {}
}
