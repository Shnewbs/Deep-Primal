package net.vulkanmod.mixin;
import com.mojang.blaze3d.systems.GpuBackend;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.vulkanmod.render.engine.VkBackend;
import net.vulkanmod.vulkan.Renderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Minecraft.class)
public abstract class MinecraftBackendMixin {
    @ModifyVariable(method="<init>", at=@At("STORE"), name="backends")
    private GpuBackend[] deepprimal$backends(GpuBackend[] original) {
        GpuBackend[] result = new GpuBackend[original.length + 1];
        result[0] = new VkBackend(); System.arraycopy(original, 0, result, 1, original.length); return result;
    }
    @Inject(method="runTick", at=@At("HEAD"))
    private void deepprimal$begin(boolean advance, CallbackInfo ci) {
        if ("Vulkan".equals(RenderSystem.getDevice().getBackendName())) Renderer.getInstance().beginFrame();
    }
}
