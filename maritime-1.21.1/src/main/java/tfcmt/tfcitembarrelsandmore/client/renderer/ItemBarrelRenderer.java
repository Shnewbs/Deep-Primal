package tfcmt.tfcitembarrelsandmore.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import tfcmt.tfcitembarrelsandmore.client.BraceClientCache;
import tfcmt.tfcitembarrelsandmore.client.renderer.BraceRenderer;
import tfcmt.tfcitembarrelsandmore.common.BraceData;
import tfcmt.tfcitembarrelsandmore.common.blockentities.ItemBarrelBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.ItemBarrelBlock;

public class ItemBarrelRenderer
implements BlockEntityRenderer<ItemBarrelBlockEntity> {
    private final RandomSource random = RandomSource.create();

    public ItemBarrelRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    public void render(ItemBarrelBlockEntity be, float partialTick, PoseStack ps, MultiBufferSource buffers, int packedLight, int packedOverlay) {
        BlockState state = be.getBlockState();
        if (be.getLevel() == null) {
            return;
        }
        BlockPos pos = be.getBlockPos();
        BraceData brace = BraceClientCache.get(pos);
        boolean sideFacing = state.hasProperty(ItemBarrelBlock.FACING) && ((Direction)state.getValue(ItemBarrelBlock.FACING)).getAxis().isHorizontal();
        boolean braced = brace != null && brace.isValid() && sideFacing;
        Minecraft mc = Minecraft.getInstance();
        BakedModel model = mc.getBlockRenderer().getBlockModel(state);
        ModelBlockRenderer modelRenderer = mc.getBlockRenderer().getModelRenderer();
        ps.pushPose();
        modelRenderer.renderModel(ps.last(), buffers.getBuffer(RenderType.cutoutMipped()), state, model, 1.0f, 1.0f, 1.0f, packedLight, packedOverlay, ModelData.EMPTY, RenderType.cutoutMipped());
        ps.popPose();
        if (braced) {
            BraceRenderer.render(ps, buffers, brace, (Direction)state.getValue(ItemBarrelBlock.FACING), packedLight, packedOverlay, 0.0f);
        }
    }
}

