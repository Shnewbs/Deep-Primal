package tfcmt.tfcitembarrelsandmore.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.Property;
import tfcmt.tfcitembarrelsandmore.common.TIBAMBlocks;
import tfcmt.tfcitembarrelsandmore.common.blockentities.rotation.WaterPaddleBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blocks.rotation.WaterPaddleBlock;

public class WaterPaddleBlockEntityRenderer
implements BlockEntityRenderer<WaterPaddleBlockEntity> {
    public static final ModelLayerLocation WATER_PADDLE_LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "water_paddle"), "main");
    private static final Map<TIBAMBlocks.Wood, ResourceLocation> TEXTURE_CACHE = new EnumMap<TIBAMBlocks.Wood, ResourceLocation>(TIBAMBlocks.Wood.class);
    private final ModelPart main;

    private static ResourceLocation textureFor(TIBAMBlocks.Wood wood) {
        return TEXTURE_CACHE.computeIfAbsent(wood, w -> ResourceLocation.fromNamespaceAndPath("tfc", "textures/entity/water_wheel/" + w.getId() + ".png"));
    }

    public WaterPaddleBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        ModelPart root = context.bakeLayer(WATER_PADDLE_LAYER);
        this.main = root.getChild("main");
    }

    public void render(WaterPaddleBlockEntity be, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5, -0.5, 0.5);
        Direction facing = (Direction)be.getBlockState().getValue((Property)WaterPaddleBlock.FACING);
        if (facing.getAxis() == Direction.Axis.Z) {
            poseStack.mulPose(Axis.YN.rotationDegrees(90.0f));
        }
        this.main.xRot = -be.getRotationAngle(partialTick);
        ResourceLocation texture = WaterPaddleBlockEntityRenderer.textureFor(((WaterPaddleBlock)be.getBlockState().getBlock()).getWood());
        this.main.render(poseStack, buffer.getBuffer(RenderType.entityCutout((ResourceLocation)texture)), packedLight, packedOverlay);
        poseStack.popPose();
    }

    public boolean shouldRenderOffScreen(WaterPaddleBlockEntity be) {
        return true;
    }

    public int getViewDistance() {
        return 64;
    }
}

