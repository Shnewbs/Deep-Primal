package tfcmt.tfcitembarrelsandmore.compat.create;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.client.model.data.ModelData;
import tfcmt.tfcitembarrelsandmore.compat.create.SpeedTelegraphInputBlockEntity;
import tfcmt.tfcitembarrelsandmore.compat.create.SpeedTelegraphOrder;

public class SpeedTelegraphInputRenderer
implements BlockEntityRenderer<SpeedTelegraphInputBlockEntity> {
    public static final ResourceLocation MODEL_HANDLE = ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/speed_telegraph_input_handle");
    public static final ResourceLocation MODEL_RED = ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/speed_telegraph_input_red");
    private static final float PIVOT_Y = 1.28125f;
    private static final float PIVOT_Z = 0.53125f;
    private static final float STEP_DEG = 22.0f;
    private static final float MIN_DEG = -110.0f;
    private static final float RED_TICK_SPEED = 0.08f;

    public SpeedTelegraphInputRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    public void render(SpeedTelegraphInputBlockEntity be, float partialTick, PoseStack ps, MultiBufferSource buffers, int packedLight, int packedOverlay) {
        if (be.getLevel() == null) {
            return;
        }
        BlockState state = be.getBlockState();
        float handleDeg = SpeedTelegraphInputRenderer.orderToAngle(be.getOrder());
        float redPrev = be.getRedAnglePrev();
        float redCurr = be.getRedAngle();
        float redDeg = redPrev + (redCurr - redPrev) * partialTick;
        BakedModel handleModel = Minecraft.getInstance().getModelManager().getModel(MODEL_HANDLE);
        BakedModel redModel = Minecraft.getInstance().getModelManager().getModel(MODEL_RED);
        ps.pushPose();
        SpeedTelegraphInputRenderer.applyFacingTransform(ps, state);
        ps.pushPose();
        ps.translate(0.0f, 1.28125f, 0.53125f);
        ps.mulPose(Axis.XP.rotationDegrees(handleDeg));
        ps.translate(0.0f, -1.28125f, -0.53125f);
        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(ps.last(), buffers.getBuffer(RenderType.cutoutMipped()), state, handleModel, 1.0f, 1.0f, 1.0f, packedLight, packedOverlay, ModelData.EMPTY, RenderType.cutoutMipped());
        ps.popPose();
        ps.pushPose();
        ps.translate(0.0f, 1.28125f, 0.53125f);
        ps.mulPose(Axis.XP.rotationDegrees(redDeg));
        ps.translate(0.0f, -1.28125f, -0.53125f);
        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(ps.last(), buffers.getBuffer(RenderType.cutoutMipped()), state, redModel, 1.0f, 1.0f, 1.0f, packedLight, packedOverlay, ModelData.EMPTY, RenderType.cutoutMipped());
        ps.popPose();
        ps.popPose();
    }

    public static float orderToAngle(SpeedTelegraphOrder order) {
        return -110.0f + (float)order.ordinal() * 22.0f;
    }

    private static void applyFacingTransform(PoseStack ps, BlockState state) {
        Direction facing = (Direction)state.getValue((Property)BlockStateProperties.HORIZONTAL_FACING);
        ps.translate(0.5, 0.5, 0.5);
        switch (facing) {
            case NORTH: {
                ps.mulPose(Axis.YP.rotationDegrees(180.0f));
                break;
            }
            case EAST: {
                ps.mulPose(Axis.YP.rotationDegrees(90.0f));
                break;
            }
            case WEST: {
                ps.mulPose(Axis.YP.rotationDegrees(-90.0f));
                break;
            }
        }
        ps.translate(-0.5, -0.5, -0.5);
    }

    public int getViewDistance() {
        return 64;
    }
}

