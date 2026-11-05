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
import tfcmt.tfcitembarrelsandmore.common.blockentities.ExpellerPressBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.ExpellerPressBlock;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.GrinderBlock;

public class ExpellerPressBlockEntityRenderer
implements BlockEntityRenderer<ExpellerPressBlockEntity> {
    private static final String MODID = "tfc_item_barrels_and_more";
    private static final ResourceLocation MODEL_NO_SCREW = ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/expeller_press_rotating_parts");
    private static final ResourceLocation MODEL_WROUGHT_IRON = ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/expeller_press_rotating/screw_wrought_iron");
    private static final ResourceLocation MODEL_STEEL = ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/expeller_press_rotating/screw_steel");
    private static final ResourceLocation MODEL_BLACK_STEEL = ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/expeller_press_rotating/screw_black_steel");
    private static final ResourceLocation MODEL_BLUE_STEEL = ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/expeller_press_rotating/screw_blue_steel");
    private static final ResourceLocation MODEL_RED_STEEL = ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/expeller_press_rotating/screw_red_steel");

    public ExpellerPressBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    public void render(ExpellerPressBlockEntity be, float partialTick, PoseStack ps, MultiBufferSource buffers, int packedLight, int packedOverlay) {
        if (be.getLevel() == null) {
            return;
        }
        BlockState state = be.getBlockState();
        Direction facing = (Direction)state.getValue((Property)ExpellerPressBlock.FACING);
        if (state.getValue(ExpellerPressBlock.SCREW) == GrinderBlock.ScrewType.NONE) {
            return;
        }
        if (!be.isConnectedToNetwork()) {
            return;
        }
        BakedModel model = Minecraft.getInstance().getModelManager().getModel(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ExpellerPressBlockEntityRenderer.modelFor((GrinderBlock.ScrewType)((Object)state.getValue(ExpellerPressBlock.SCREW)))));
        float rawAngleDeg = (float)Math.toDegrees(Rotation.angle((Rotation)be.getSinkRotation(), (float)partialTick));
        float spinAngleDeg = switch (facing) {
            case Direction.EAST -> rawAngleDeg;
            case Direction.WEST -> -rawAngleDeg;
            case Direction.NORTH -> -rawAngleDeg;
            case Direction.SOUTH -> rawAngleDeg;
            default -> rawAngleDeg;
        };
        ps.pushPose();
        ExpellerPressBlockEntityRenderer.applyFacingTransform(ps, facing);
        ps.translate(0.5, 0.5, 0.5);
        if (facing == Direction.NORTH || facing == Direction.SOUTH) {
            ps.mulPose(Axis.YP.rotationDegrees(180.0f));
        }
        ps.mulPose(Axis.XP.rotationDegrees(spinAngleDeg));
        ps.translate(-0.5, -0.5, -0.5);
        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(ps.last(), buffers.getBuffer(RenderType.cutoutMipped()), state, model, 1.0f, 1.0f, 1.0f, packedLight, packedOverlay, ModelData.EMPTY, RenderType.cutoutMipped());
        ps.popPose();
    }

    private static void applyFacingTransform(PoseStack ps, Direction facing) {
        ps.translate(0.5, 0.5, 0.5);
        switch (facing) {
            case NORTH: {
                ps.mulPose(Axis.YP.rotationDegrees(90.0f));
                break;
            }
            case EAST: {
                ps.mulPose(Axis.YP.rotationDegrees(180.0f));
                break;
            }
            case SOUTH: {
                ps.mulPose(Axis.YP.rotationDegrees(270.0f));
                break;
            }
        }
        ps.translate(-0.5, -0.5, -0.5);
    }

    private static ResourceLocation modelFor(GrinderBlock.ScrewType type) {
        return switch (type) {
            case GrinderBlock.ScrewType.WROUGHT_IRON -> MODEL_WROUGHT_IRON;
            case GrinderBlock.ScrewType.STEEL -> MODEL_STEEL;
            case GrinderBlock.ScrewType.BLACK_STEEL -> MODEL_BLACK_STEEL;
            case GrinderBlock.ScrewType.BLUE_STEEL -> MODEL_BLUE_STEEL;
            case GrinderBlock.ScrewType.RED_STEEL -> MODEL_RED_STEEL;
            default -> MODEL_NO_SCREW;
        };
    }
}

