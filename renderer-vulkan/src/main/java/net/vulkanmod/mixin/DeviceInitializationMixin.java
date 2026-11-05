package net.vulkanmod.mixin;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import net.vulkanmod.vulkan.Renderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(RenderSystem.class)
public abstract class DeviceInitializationMixin {
    @Inject(method="initRenderer", at=@At(value="FIELD", target="Lcom/mojang/blaze3d/systems/RenderSystem;DEVICE:Lcom/mojang/blaze3d/systems/GpuDevice;", opcode=179, shift=At.Shift.AFTER))
    private static void deepprimal$initialize(GpuDevice device, CallbackInfo ci) {
        if ("Vulkan".equals(device.getBackendName())) Renderer.initRenderer();
    }
}
