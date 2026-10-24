package tfcmt.tfcitembarrelsandmore.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.dries007.tfc.util.rotation.Rotation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.client.model.data.ModelData;
import tfcmt.tfcitembarrelsandmore.common.blockentities.SinglePistonEngineBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.SinglePistonEngineBlock;

public class SinglePistonEngineBlockEntityRenderer
implements BlockEntityRenderer<SinglePistonEngineBlockEntity> {
    public static final ResourceLocation MODEL_FLYWHEEL = ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/single_piston_engine_rotating");
    public static final ResourceLocation MODEL_SHAFT = ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/single_piston_engine_shaft");
    private static final float PIVOT_X = 0.5f;
    private static final float PIVOT_Y = 0.5f;
    private static final float PIVOT_Z = 0.09375f;
    private static final float BEARING_OFFSET_Y = 0.1875f;
    private static final float SHAFT_TOP_X = 0.4375f;
    private static final float SHAFT_TOP_Y = 0.8125f;
    private static final float SHAFT_LEN = 0.4375f;

    public SinglePistonEngineBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    public void render(SinglePistonEngineBlockEntity be, float partialTick, PoseStack ps, MultiBufferSource buffers, int packedLight, int packedOverlay) {
        Rotation rot;
        if (be.getLevel() == null) {
            return;
        }
        BlockState state = be.getBlockState();
        Direction facing = (Direction)state.getValue((Property)SinglePistonEngineBlock.FACING);
        float angleDeg = 0.0f;
        float angleRad = 0.0f;
        if (be.isRunning() && (rot = be.getSourceRotation()) != null) {
            int correction = facing == Direction.SOUTH ? -1 : 1;
            angleDeg = (float)correction * (float)Math.toDegrees(Rotation.angle((Rotation)rot, (float)partialTick));
            if (facing != Direction.EAST) {
                angleDeg = -angleDeg;
            }
            angleRad = (float)Math.toRadians(angleDeg);
        }
        float bx = 0.5f - (float)Math.sin(angleRad) * 0.1875f;
        float by = 0.5f - (float)Math.cos(angleRad) * 0.1875f;
        float horizDist = bx - 0.4375f;
        float vert = (float)Math.sqrt(Math.max(0.0, (double)(0.19140625f - horizDist * horizDist)));
        float topY = by + vert;
        float shaftY = topY - 0.8125f;
        float tiltDeg = (float)Math.toDegrees(Math.atan2(-horizDist, vert));
        BakedModel flywheelModel = Minecraft.getInstance().getModelManager().getModel(net.minecraft.client.resources.model.ModelResourceLocation.standalone(MODEL_FLYWHEEL));
        BakedModel shaftModel = Minecraft.getInstance().getModelManager().getModel(net.minecraft.client.resources.model.ModelResourceLocation.standalone(MODEL_SHAFT));
        ps.pushPose();
        SinglePistonEngineBlockEntityRenderer.applyFacingTransform(ps, facing);
        ps.pushPose();
        ps.translate(0.5f, 0.5f, 0.09375f);
        ps.mulPose(Axis.ZP.rotationDegrees(angleDeg));
        ps.translate(-0.5f, -0.5f, -0.09375f);
        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(ps.last(), buffers.getBuffer(RenderType.cutoutMipped()), state, flywheelModel, 1.0f, 1.0f, 1.0f, packedLight, packedOverlay, ModelData.EMPTY, RenderType.cutoutMipped());
        ps.popPose();
        ps.pushPose();
        ps.translate(0.4375f, topY, 0.0f);
        ps.mulPose(Axis.ZP.rotationDegrees(tiltDeg));
        ps.translate(-0.4375f, -topY, 0.0f);
        ps.translate(0.0f, shaftY, 0.0f);
        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(ps.last(), buffers.getBuffer(RenderType.cutoutMipped()), state, shaftModel, 1.0f, 1.0f, 1.0f, packedLight, packedOverlay, ModelData.EMPTY, RenderType.cutoutMipped());
        ps.popPose();
        ps.popPose();
    }

    private static void applyFacingTransform(PoseStack ps, Direction facing) {
        ps.translate(0.5, 0.5, 0.5);
        switch (facing) {
            case SOUTH: {
                ps.mulPose(Axis.YP.rotationDegrees(180.0f));
                break;
            }
            case EAST: {
                ps.mulPose(Axis.YP.rotationDegrees(-90.0f));
                break;
            }
            case WEST: {
                ps.mulPose(Axis.YP.rotationDegrees(90.0f));
                break;
            }
        }
        ps.translate(-0.5, -0.5, -0.5);
    }

    public int getViewDistance() {
        return 96;
    }
}

