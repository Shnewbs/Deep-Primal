package tfcmt.tfcitembarrelsandmore.client.compat;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import tfcmt.tfcitembarrelsandmore.compat.create.CreateCompat;
import tfcmt.tfcitembarrelsandmore.compat.create.SpeedTelegraphInputRenderer;
import tfcmt.tfcitembarrelsandmore.compat.create.SpeedTelegraphOutputRenderer;

/** Loaded only when Create is installed. */
public final class CreateClient {
    public static void registerRenderers() {
        BlockEntityRenderers.register(CreateCompat.SPEED_TELEGRAPH_INPUT_BE.get(), SpeedTelegraphInputRenderer::new);
        BlockEntityRenderers.register(CreateCompat.SPEED_TELEGRAPH_OUTPUT_BE.get(), SpeedTelegraphOutputRenderer::new);
    }
}
