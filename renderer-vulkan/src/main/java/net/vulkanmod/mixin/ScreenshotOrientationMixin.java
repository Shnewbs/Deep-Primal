package net.vulkanmod.mixin;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Screenshot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
@Mixin(Screenshot.class)
public abstract class ScreenshotOrientationMixin {
    @Redirect(method="lambda$takeScreenshot$1", at=@At(value="INVOKE", target="Lcom/mojang/blaze3d/platform/NativeImage;setPixelABGR(III)V"))
    private static void deepprimal$orientation(NativeImage image, int x, int y, int color) {
        image.setPixelABGR(x, "Vulkan".equals(RenderSystem.getDevice().getBackendName()) ? image.getHeight()-1-y : y, color);
    }
}
