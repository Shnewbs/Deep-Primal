package net.vulkanmod.mixin.window;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.GpuBackend;
import net.neoforged.fml.loading.EarlyLoadingScreenController;
import net.vulkanmod.render.engine.VkBackend;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
@Mixin(Window.class)
public abstract class VulkanWindowMixin {
    @Unique private static boolean deepprimal$loadingWindowConsumed;
    @Redirect(method="close", at=@At(value="INVOKE", target="Lorg/lwjgl/glfw/GLFW;glfwTerminate()V"))
    private void deepprimal$keepGlfwForFallback() {
        // Native backend retry closes the failed Window. The next backend still needs GLFW alive.
        if (!(((Window)(Object)this).backend() instanceof VkBackend
                && net.vulkanmod.deepprimal.Startup.consumeWindowFallback())) GLFW.glfwTerminate();
    }
    @Redirect(method="createGlfwWindow", at=@At(value="INVOKE", target="Lnet/neoforged/fml/loading/EarlyLoadingScreenController;current()Lnet/neoforged/fml/loading/EarlyLoadingScreenController;"))
    private static EarlyLoadingScreenController deepprimal$window(int width, int height, String title, long monitor, GpuBackend backend) {
        if (deepprimal$loadingWindowConsumed) return null;
        var early = EarlyLoadingScreenController.current();
        if (backend instanceof VkBackend && early != null) {
            long old = early.takeOverGlfwWindow();
            if (early instanceof net.neoforged.fml.earlydisplay.DisplayWindow display) {
                GL.createCapabilities(); display.close();
            }
            GLFW.glfwMakeContextCurrent(0); GL.setCapabilities(null); GLFW.glfwDestroyWindow(old);
            deepprimal$loadingWindowConsumed = true; return null;
        }
        return early;
    }
}
