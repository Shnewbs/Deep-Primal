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
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.client.model.data.ModelData;
import tfcmt.tfcitembarrelsandmore.common.blockentities.LargeSinglePistonEngineBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.LargeSinglePistonEngineBlock;

public class LargeSinglePistonEngineBlockEntityRenderer
implements BlockEntityRenderer<LargeSinglePistonEngineBlockEntity> {
    private static final String NS = "tfc_item_barrels_and_more";
    public static final ResourceLocation MODEL_CRANK = ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/large_single_piston_engine_crank");
    public static final ResourceLocation MODEL_PIVOT = ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/large_single_piston_engine_lever");
    public static final ResourceLocation MODEL_PISTON_NEAR = ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/large_single_piston_engine_piston_near");
    public static final ResourceLocation MODEL_PISTON_FAR = ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/large_single_piston_engine_piston_far");
    public static final ResourceLocation MODEL_LEVER = ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/large_single_piston_engine_pivot");
    private static final float PIVOT_Y = 0.5f;
    private static final float PIVOT_Z = 0.5f;
    private static final float CRANK_THROW = 0.125f;
    private static final float ROD_LEN = 0.4375f;
    private static final float ROD_TOP_Z = 0.5f;
    private static final float ROD_TOP_Y = 1.0625f;
    private static final float LEVER_PIVOT_Y = 1.3125f;
    private static final float LEVER_PIVOT_Z = 1.0625f;
    private static final float LEVER_HALF_LENGTH = 0.6875f;
    private static final float LEVER_REST_TILT_DEG = 18.0f;

    public LargeSinglePistonEngineBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    public void render(LargeSinglePistonEngineBlockEntity be, float partialTick, PoseStack ps, MultiBufferSource buffers, int packedLight, int packedOverlay) {
        Rotation rot;
        if (be.getLevel() == null) {
            return;
        }
        BlockState state = be.getBlockState();
        Direction facing = (Direction)state.getValue((Property)LargeSinglePistonEngineBlock.FACING);
        float globalAngleDeg = 0.0f;
        if (((Boolean)state.getValue((Property)LargeSinglePistonEngineBlock.RUNNING)).booleanValue() && (rot = be.getSourceRotation()) != null) {
            float rawDeg = (float)Math.toDegrees(Rotation.angle((Rotation)rot, (float)partialTick));
            int axisStep = switch (facing) {
                case Direction.NORTH, Direction.EAST -> 1;
                case Direction.SOUTH, Direction.WEST -> -1;
                default -> 1;
            };
            float trueAngle = rawDeg * (float)axisStep;
            globalAngleDeg = trueAngle;
        }
        float angleRad = (float)Math.toRadians(globalAngleDeg);
        float pinY = 0.5f + (float)Math.cos(angleRad) * 0.125f;
        float pinZ = 0.5f + (float)Math.sin(angleRad) * 0.125f;
        float horizDist = pinZ - 0.5f;
        float vert = (float)Math.sqrt(Math.max(0.0, (double)(0.19140625f - horizDist * horizDist)));
        float topY = pinY + vert;
        float rodY = topY - 1.0625f;
        float tiltDeg = (float)Math.toDegrees(Math.atan2(-horizDist, vert));
        float nearPistonY = rodY;
        float farPistonY = -rodY;
        float leverTiltDeg = 18.0f + (float)Math.toDegrees(Math.atan2(nearPistonY, 0.6875));
        ModelManager mgr = Minecraft.getInstance().getModelManager();
        BakedModel crankModel = mgr.getModel(net.minecraft.client.resources.model.ModelResourceLocation.standalone(MODEL_CRANK));
        BakedModel pivotModel = mgr.getModel(net.minecraft.client.resources.model.ModelResourceLocation.standalone(MODEL_PIVOT));
        BakedModel pistonNearModel = mgr.getModel(net.minecraft.client.resources.model.ModelResourceLocation.standalone(MODEL_PISTON_NEAR));
        BakedModel pistonFarModel = mgr.getModel(net.minecraft.client.resources.model.ModelResourceLocation.standalone(MODEL_PISTON_FAR));
        BakedModel leverModel = mgr.getModel(net.minecraft.client.resources.model.ModelResourceLocation.standalone(MODEL_LEVER));
        ps.pushPose();
        LargeSinglePistonEngineBlockEntityRenderer.applyFacingTransform(ps, facing);
        ps.pushPose();
        ps.translate(0.0f, 0.5f, 0.5f);
        ps.mulPose(Axis.XP.rotationDegrees(globalAngleDeg));
        ps.translate(0.0f, -0.5f, -0.5f);
        LargeSinglePistonEngineBlockEntityRenderer.renderModel(ps, buffers, state, crankModel, packedLight, packedOverlay);
        ps.popPose();
        ps.pushPose();
        ps.translate(0.0f, topY, 0.5f);
        ps.mulPose(Axis.XP.rotationDegrees(tiltDeg));
        ps.translate(0.0f, -topY, -0.5f);
        ps.translate(0.0f, rodY, 0.0f);
        LargeSinglePistonEngineBlockEntityRenderer.renderModel(ps, buffers, state, pivotModel, packedLight, packedOverlay);
        ps.popPose();
        ps.pushPose();
        ps.translate(0.0f, nearPistonY, 0.0f);
        LargeSinglePistonEngineBlockEntityRenderer.renderModel(ps, buffers, state, pistonNearModel, packedLight, packedOverlay);
        ps.popPose();
        ps.pushPose();
        ps.translate(0.0f, farPistonY, 0.0f);
        LargeSinglePistonEngineBlockEntityRenderer.renderModel(ps, buffers, state, pistonFarModel, packedLight, packedOverlay);
        ps.popPose();
        ps.pushPose();
        ps.translate(0.0f, 1.3125f, 1.0625f);
        ps.mulPose(Axis.XP.rotationDegrees(leverTiltDeg));
        ps.translate(0.0f, -1.3125f, -1.0625f);
        LargeSinglePistonEngineBlockEntityRenderer.renderModel(ps, buffers, state, leverModel, packedLight, packedOverlay);
        ps.popPose();
        ps.popPose();
    }

    private static void applyFacingTransform(PoseStack ps, Direction facing) {
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

    private static void renderModel(PoseStack ps, MultiBufferSource buffers, BlockState state, BakedModel model, int packedLight, int packedOverlay) {
        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(ps.last(), buffers.getBuffer(RenderType.cutoutMipped()), state, model, 1.0f, 1.0f, 1.0f, packedLight, packedOverlay, ModelData.EMPTY, RenderType.cutoutMipped());
    }

    public int getViewDistance() {
        return 96;
    }
}

