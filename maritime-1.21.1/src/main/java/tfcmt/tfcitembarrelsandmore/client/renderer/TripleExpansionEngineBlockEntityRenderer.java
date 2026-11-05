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
import tfcmt.tfcitembarrelsandmore.common.blockentities.TripleExpansionEngineBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.TripleExpansionEngineBlock;

public class TripleExpansionEngineBlockEntityRenderer
implements BlockEntityRenderer<TripleExpansionEngineBlockEntity> {
    private static final String NS = "tfc_item_barrels_and_more";
    public static final ResourceLocation MODEL_CRANK = ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/triple_expansion_engine_crank");
    public static final ResourceLocation MODEL_UPPER_SHAFT_A = ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/triple_expansion_engine_upper_shaft_a");
    public static final ResourceLocation MODEL_UPPER_SHAFT_B = ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/triple_expansion_engine_upper_shaft_b");
    public static final ResourceLocation MODEL_UPPER_SHAFT_C = ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/triple_expansion_engine_upper_shaft_c");
    public static final ResourceLocation MODEL_LOWER_SHAFT_A = ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/triple_expansion_engine_lower_shaft_a");
    public static final ResourceLocation MODEL_LOWER_SHAFT_B = ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/triple_expansion_engine_lower_shaft_b");
    public static final ResourceLocation MODEL_LOWER_SHAFT_C = ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/triple_expansion_engine_lower_shaft_c");
    private static final float PIVOT_X = 0.5f;
    private static final float PIVOT_Y = 0.5f;
    private static final float THROW = 0.1875f;
    private static final float SHAFT_PHASE_A = 180.0f;
    private static final float SHAFT_PHASE_B = 90.0f;
    private static final float SHAFT_PHASE_C = 0.0f;
    private static final float PHASE_A = 180.0f;
    private static final float PHASE_B = 90.0f;
    private static final float PHASE_C = 0.0f;
    private static final float ROD_LEN_A = 0.5f;
    private static final float ROD_LEN_B = 0.5f;
    private static final float ROD_LEN_C = 0.5f;
    private static final float UPPER_SHAFT_BOTTOM_A = 0.8125f;
    private static final float UPPER_SHAFT_BOTTOM_B = 0.8125f;
    private static final float UPPER_SHAFT_BOTTOM_C = 0.8125f;
    private static final float LOWER_SHAFT_TOP_A = 0.8125f;
    private static final float LOWER_SHAFT_TOP_B = 0.8125f;
    private static final float LOWER_SHAFT_TOP_C = 0.8125f;
    private static final float Z_A = 0.3125f;
    private static final float Z_B = 0.875f;
    private static final float Z_C = 1.5625f;

    private static float computeRodLen(float phaseDeg, float upperShaftBottomPx) {
        float rad = (float)Math.toRadians(phaseDeg);
        float bx = 8.0f - (float)Math.sin(rad) * 3.0f;
        float by = 8.0f - (float)Math.cos(rad) * 3.0f;
        float horiz = bx - 8.0f;
        float vert = upperShaftBottomPx - by;
        return (float)Math.sqrt(horiz * horiz + vert * vert) / 16.0f;
    }

    public TripleExpansionEngineBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    public void render(TripleExpansionEngineBlockEntity be, float partialTick, PoseStack ps, MultiBufferSource buffers, int packedLight, int packedOverlay) {
        Rotation rot;
        if (be.getLevel() == null) {
            return;
        }
        BlockState state = be.getBlockState();
        Direction facing = (Direction)state.getValue((Property)TripleExpansionEngineBlock.FACING);
        if (!((Boolean)state.getValue((Property)TripleExpansionEngineBlock.RUNNING)).booleanValue()) {
            return;
        }
        float globalAngleDeg = 0.0f;
        if (((Boolean)state.getValue((Property)TripleExpansionEngineBlock.RUNNING)).booleanValue() && (rot = be.getSourceRotation()) != null) {
            int correction = facing == Direction.NORTH ? -1 : 1;
            globalAngleDeg = (float)correction * (float)Math.toDegrees(Rotation.angle((Rotation)rot, (float)partialTick));
            if (facing != Direction.WEST) {
                globalAngleDeg = -globalAngleDeg;
            }
        }
        ModelManager mgr = Minecraft.getInstance().getModelManager();
        BakedModel crank = mgr.getModel(net.minecraft.client.resources.model.ModelResourceLocation.standalone(MODEL_CRANK));
        BakedModel upperShaftA = mgr.getModel(net.minecraft.client.resources.model.ModelResourceLocation.standalone(MODEL_UPPER_SHAFT_A));
        BakedModel upperShaftB = mgr.getModel(net.minecraft.client.resources.model.ModelResourceLocation.standalone(MODEL_UPPER_SHAFT_B));
        BakedModel upperShaftC = mgr.getModel(net.minecraft.client.resources.model.ModelResourceLocation.standalone(MODEL_UPPER_SHAFT_C));
        BakedModel lowerShaftA = mgr.getModel(net.minecraft.client.resources.model.ModelResourceLocation.standalone(MODEL_LOWER_SHAFT_A));
        BakedModel lowerShaftB = mgr.getModel(net.minecraft.client.resources.model.ModelResourceLocation.standalone(MODEL_LOWER_SHAFT_B));
        BakedModel lowerShaftC = mgr.getModel(net.minecraft.client.resources.model.ModelResourceLocation.standalone(MODEL_LOWER_SHAFT_C));
        ps.pushPose();
        TripleExpansionEngineBlockEntityRenderer.applyFacingTransform(ps, facing);
        ps.pushPose();
        ps.translate(0.5f, 0.5f, 0.0f);
        ps.mulPose(Axis.ZP.rotationDegrees(globalAngleDeg));
        ps.translate(-0.5f, -0.5f, 0.0f);
        TripleExpansionEngineBlockEntityRenderer.renderModel(ps, buffers, state, crank, packedLight, packedOverlay);
        ps.popPose();
        this.renderCylinder(ps, buffers, state, packedLight, packedOverlay, globalAngleDeg, 180.0f, 0.3125f, 0.5f, 0.8125f, 0.8125f, upperShaftA, lowerShaftA);
        this.renderCylinder(ps, buffers, state, packedLight, packedOverlay, globalAngleDeg, 90.0f, 0.875f, 0.5f, 0.8125f, 0.8125f, upperShaftB, lowerShaftB);
        this.renderCylinder(ps, buffers, state, packedLight, packedOverlay, globalAngleDeg, 0.0f, 1.5625f, 0.5f, 0.8125f, 0.8125f, upperShaftC, lowerShaftC);
        ps.popPose();
    }

    private void renderCylinder(PoseStack ps, MultiBufferSource buffers, BlockState state, int packedLight, int packedOverlay, float globalAngleDeg, float phaseDeg, float cylZ, float rodLen, float upperShaftBottom, float lowerShaftTop, BakedModel upperShaftModel, BakedModel lowerShaftModel) {
        float kinematicAngleDeg = globalAngleDeg + phaseDeg;
        float kinematicAngleRad = (float)Math.toRadians(kinematicAngleDeg);
        float bx = 0.5f - (float)Math.sin(kinematicAngleRad) * 0.1875f;
        float by = 0.5f - (float)Math.cos(kinematicAngleRad) * 0.1875f;
        float horizDist = bx - 0.5f;
        float vert = (float)Math.sqrt(Math.max(0.0, (double)(rodLen * rodLen - horizDist * horizDist)));
        float crossY = by + vert;
        float upperShaftY = crossY - upperShaftBottom;
        float tiltDeg = (float)Math.toDegrees(Math.atan2(-horizDist, vert));
        ps.pushPose();
        ps.translate(0.0f, upperShaftY, 0.0f);
        TripleExpansionEngineBlockEntityRenderer.renderModel(ps, buffers, state, upperShaftModel, packedLight, packedOverlay);
        ps.popPose();
        float lowerShaftDelta = crossY - lowerShaftTop;
        ps.pushPose();
        ps.translate(0.5f, crossY, cylZ);
        ps.mulPose(Axis.ZP.rotationDegrees(tiltDeg));
        ps.translate(-0.5f, -crossY, -cylZ);
        ps.translate(0.0f, lowerShaftDelta, 0.0f);
        TripleExpansionEngineBlockEntityRenderer.renderModel(ps, buffers, state, lowerShaftModel, packedLight, packedOverlay);
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

