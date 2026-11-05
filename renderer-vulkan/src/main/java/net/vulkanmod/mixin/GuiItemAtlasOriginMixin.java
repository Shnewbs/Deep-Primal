package net.vulkanmod.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.render.GuiItemAtlas;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Vulkan render targets share the top-left origin used by uploaded textures. */
@Mixin(GuiItemAtlas.class)
public abstract class GuiItemAtlasOriginMixin {
  @ModifyReturnValue(method = "getOrUpdate", at = @At("RETURN"))
  private GuiItemAtlas.SlotView deepprimal$atlasOrigin(GuiItemAtlas.SlotView slot) {
    if (slot == null || !"Vulkan".equals(RenderSystem.getDevice().getBackendName())) return slot;
    // Flip the atlas coordinate, not just each icon: slots can occupy any atlas row.
    return new GuiItemAtlas.SlotView(
        slot.textureView(), slot.u0(), 1 - slot.v0(), slot.u1(), 1 - slot.v1());
  }
}
