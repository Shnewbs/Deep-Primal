package tfcmt.tfcitembarrelsandmore.client.compat;

import top.theillusivec4.curios.api.client.CuriosRendererRegistry;
import tfcmt.tfcitembarrelsandmore.client.curios.ItemBarrelCurioRenderer;
import tfcmt.tfcitembarrelsandmore.common.TIBAMBlocks;

/** Loaded only when Curios is installed. */
public final class CuriosClient {
    public static void registerRenderers() {
        TIBAMBlocks.ITEM_BARRELS.values().forEach(block ->
            CuriosRendererRegistry.register(block.get().asItem(), ItemBarrelCurioRenderer::new));
    }
}
