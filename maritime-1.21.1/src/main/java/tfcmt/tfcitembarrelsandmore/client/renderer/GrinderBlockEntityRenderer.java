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
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.client.model.data.ModelData;
import tfcmt.tfcitembarrelsandmore.common.blockentities.GrinderBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.GrinderBlock;

public class GrinderBlockEntityRenderer
implements BlockEntityRenderer<GrinderBlockEntity> {
    private static final String MODID = "tfc_item_barrels_and_more";
    private static final ResourceLocation MODEL_NO_SCREW = ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/grinder_rotating_parts");
    private static final ResourceLocation MODEL_WROUGHT_IRON = ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/grinder_rotating/screw_wrought_iron");
    private static final ResourceLocation MODEL_STEEL = ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/grinder_rotating/screw_steel");
    private static final ResourceLocation MODEL_BLACK_STEEL = ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/grinder_rotating/screw_black_steel");
    private static final ResourceLocation MODEL_BLUE_STEEL = ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/grinder_rotating/screw_blue_steel");
    private static final ResourceLocation MODEL_RED_STEEL = ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/grinder_rotating/screw_red_steel");
    private static final float ITEM_SCALE = 0.45f;
    private static final float LAYER_Y_OFFSET = 0.04f;
    private static final float FUNNEL_Y = 0.78f;

    public GrinderBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    public void render(GrinderBlockEntity be, float partialTick, PoseStack ps, MultiBufferSource buffers, int packedLight, int packedOverlay) {
        ItemStack inputItem;
        boolean spinning;
        if (be.getLevel() == null) {
            return;
        }
        BlockState state = be.getBlockState();
        Direction facing = (Direction)state.getValue((Property)GrinderBlock.FACING);
        boolean bl = spinning = be.isConnectedToNetwork() && state.getValue(GrinderBlock.SCREW) != GrinderBlock.ScrewType.NONE;
        if (spinning) {
            BakedModel model = Minecraft.getInstance().getModelManager().getModel(net.minecraft.client.resources.model.ModelResourceLocation.standalone(GrinderBlockEntityRenderer.modelFor((GrinderBlock.ScrewType)((Object)state.getValue(GrinderBlock.SCREW)))));
            float rawAngleDeg = (float)Math.toDegrees(Rotation.angle((Rotation)be.getSinkRotation(), (float)partialTick));
            float spinAngleDeg = switch (facing) {
                case Direction.WEST -> rawAngleDeg;
                case Direction.EAST -> -rawAngleDeg;
                case Direction.NORTH -> rawAngleDeg;
                case Direction.SOUTH -> -rawAngleDeg;
                default -> rawAngleDeg;
            };
            ps.pushPose();
            GrinderBlockEntityRenderer.applyFacingTransform(ps, facing);
            ps.translate(0.5, 0.5, 0.5);
            if (facing == Direction.NORTH || facing == Direction.SOUTH) {
                ps.mulPose(Axis.YP.rotationDegrees(180.0f));
            }
            ps.mulPose(Axis.XP.rotationDegrees(spinAngleDeg));
            ps.translate(-0.5, -0.5, -0.5);
            Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(ps.last(), buffers.getBuffer(RenderType.cutoutMipped()), state, model, 1.0f, 1.0f, 1.0f, packedLight, packedOverlay, ModelData.EMPTY, RenderType.cutoutMipped());
            ps.popPose();
        }
        if (!(inputItem = be.getInputItem()).isEmpty()) {
            GrinderBlockEntityRenderer.renderFunnelItem(inputItem, spinning, ps, buffers, packedLight, packedOverlay);
        }
    }

    private static void applyFacingTransform(PoseStack ps, Direction facing) {
        ps.translate(0.5, 0.5, 0.5);
        switch (facing) {
            case SOUTH: {
                ps.mulPose(Axis.YP.rotationDegrees(90.0f));
                break;
            }
            case WEST: {
                ps.mulPose(Axis.YP.rotationDegrees(180.0f));
                break;
            }
            case NORTH: {
                ps.mulPose(Axis.YP.rotationDegrees(270.0f));
                break;
            }
        }
        ps.translate(-0.5, -0.5, -0.5);
    }

    private static void renderFunnelItem(ItemStack stack, boolean spinning, PoseStack ps, MultiBufferSource buffers, int packedLight, int packedOverlay) {
        int count = stack.getCount();
        int layers = count >= 17 ? 3 : (count >= 9 ? 2 : 1);
        float spinY = spinning ? (float)(System.currentTimeMillis() % 8000L) / 8000.0f * 360.0f : 0.0f;
        for (int layer = 0; layer < layers; ++layer) {
            ps.pushPose();
            ps.translate(0.5, (double)(0.78f + (float)layer * 0.04f), 0.5);
            ps.mulPose(Axis.YP.rotationDegrees(spinY));
            ps.mulPose(Axis.XP.rotationDegrees(90.0f));
            ps.scale(0.45f, 0.45f, 0.45f);
            Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, packedLight, packedOverlay, ps, buffers, null, 0);
            ps.popPose();
        }
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

