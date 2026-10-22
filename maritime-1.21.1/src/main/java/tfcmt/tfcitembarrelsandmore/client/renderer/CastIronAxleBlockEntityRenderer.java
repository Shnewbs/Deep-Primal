package tfcmt.tfcitembarrelsandmore.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.common.blocks.rotation.AxleBlock;
import net.dries007.tfc.common.blocks.rotation.ConnectedAxleBlock;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import tfcmt.tfcitembarrelsandmore.common.blockentities.rotation.CastIronAxleBlockEntity;

public class CastIronAxleBlockEntityRenderer
implements BlockEntityRenderer<CastIronAxleBlockEntity> {
    private static final float SHAFT_MIN = 0.375f;
    private static final float SHAFT_MAX = 0.625f;

    public CastIronAxleBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    public void render(CastIronAxleBlockEntity be, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        BlockState state = be.getBlockState();
        Block block = state.getBlock();
        if (!(block instanceof ConnectedAxleBlock)) {
            return;
        }
        ConnectedAxleBlock axleBlock = (ConnectedAxleBlock)block;
        Direction.Axis axis = (Direction.Axis)state.getValue((Property)AxleBlock.AXIS);
        float angleDeg = -((float)Math.toDegrees(be.getRotationAngle(partialTick)));
        TextureAtlasSprite sprite = RenderHelpers.blockTexture((ResourceLocation)axleBlock.getAxleTextureLocation());
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.cutoutMipped());
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        switch (axis) {
            case X: {
                poseStack.mulPose(Axis.XP.rotationDegrees(angleDeg));
                break;
            }
            case Y: {
                poseStack.mulPose(Axis.YP.rotationDegrees(angleDeg));
                break;
            }
            case Z: {
                poseStack.mulPose(Axis.ZP.rotationDegrees(angleDeg));
            }
        }
        poseStack.translate(-0.5, -0.5, -0.5);
        AABB aabb = switch (axis) {
            default -> throw new IncompatibleClassChangeError();
            case Direction.Axis.X -> new AABB(0.0, 0.375, 0.375, 1.0, 0.625, 0.625);
            case Direction.Axis.Y -> new AABB(0.375, 0.0, 0.375, 0.625, 1.0, 0.625);
            case Direction.Axis.Z -> new AABB(0.375, 0.375, 0.0, 0.625, 0.625, 1.0);
        };
        RenderHelpers.renderTexturedCuboid((PoseStack)poseStack, (VertexConsumer)buffer, (TextureAtlasSprite)sprite, (int)packedLight, (int)packedOverlay, (AABB)aabb);
        poseStack.popPose();
    }
}

