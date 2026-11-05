package net.vulkanmod.mixin;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
/** Rendered atlas coordinates must use the same origin as uploaded texture data. */
@Mixin(TextureAtlasSprite.class)
public abstract class AtlasProjectionMixin {
    @ModifyArgs(method="uploadSpriteUbo", at=@At(value="INVOKE", target="Lorg/joml/Matrix4f;ortho2D(FFFF)Lorg/joml/Matrix4f;"))
    private void deepprimal$atlasOrigin(Args args) {
        if (!"Vulkan".equals(RenderSystem.getDevice().getBackendName())) return;
        float bottom = args.get(2); args.set(2, args.get(3)); args.set(3, bottom);
    }
}
