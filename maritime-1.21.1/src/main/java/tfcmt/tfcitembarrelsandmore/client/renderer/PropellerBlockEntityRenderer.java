package tfcmt.tfcitembarrelsandmore.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import tfcmt.tfcitembarrelsandmore.common.blockentities.rotation.PropellerBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blocks.rotation.PropellerBlock;

public class PropellerBlockEntityRenderer
implements BlockEntityRenderer<PropellerBlockEntity> {
    public static final ResourceLocation TWO_BLADE_TEXTURE = ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/two_blade_propeller");
    public static final ResourceLocation FOUR_BLADE_TEXTURE = ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/four_blade_propeller");
    private static final double[][] BOX_2 = new double[][]{{5.0, 10.0, 5.0, 11.0, 16.0, 11.0}, {6.0, 16.0, 6.0, 10.0, 19.0, 10.0}, {7.0, 19.0, 7.0, 9.0, 20.0, 9.0}, {2.0, 12.5, -10.1, 6.0, 13.5, 5.9}, {5.7, 11.0, -12.1, 9.7, 12.0, 5.9}, {9.4, 9.5, -12.1, 13.4, 10.5, 3.9}, {10.0, 12.5, 10.1, 14.0, 13.5, 26.1}, {6.3, 11.0, 10.1, 10.3, 12.0, 28.1}, {2.6, 9.5, 12.1, 6.6, 10.5, 28.1}, {5.0, 4.0, 5.0, 11.0, 10.0, 11.0}, {5.0, -2.0, 5.0, 11.0, 4.0, 11.0}};
    private static final double[][] BOX_4 = new double[][]{{5.0, 10.0, 5.0, 11.0, 16.0, 11.0}, {6.0, 16.0, 6.0, 10.0, 19.0, 10.0}, {7.0, 19.0, 7.0, 9.0, 20.0, 9.0}, {-10.0, 12.5, 9.7, 6.0, 13.5, 13.7}, {-12.0, 11.0, 6.0, 6.0, 12.0, 10.0}, {-12.0, 9.5, 2.3, 4.0, 10.5, 6.3}, {10.0, 12.5, 2.3, 26.0, 13.5, 6.3}, {10.0, 11.0, 6.0, 28.0, 12.0, 10.0}, {2.0, 12.5, -10.1, 6.0, 13.5, 5.9}, {5.7, 11.0, -12.1, 9.7, 12.0, 5.9}, {9.4, 9.5, -12.1, 13.4, 10.5, 3.9}, {10.0, 12.5, 10.1, 14.0, 13.5, 26.1}, {6.3, 11.0, 10.1, 10.3, 12.0, 28.1}, {2.6, 9.5, 12.1, 6.6, 10.5, 28.1}, {5.0, 4.0, 5.0, 11.0, 10.0, 11.0}, {5.0, -2.0, 5.0, 11.0, 4.0, 11.0}, {12.0, 9.5, 9.7, 28.0, 10.5, 13.7}};
    private static final float[][] ROT_2 = new float[][]{null, null, null, {0.0f, 0.0f, 1.0f, -22.5f}, {0.0f, 0.0f, 1.0f, -22.5f}, {0.0f, 0.0f, 1.0f, -22.5f}, {0.0f, 0.0f, 1.0f, 22.5f}, {0.0f, 0.0f, 1.0f, 22.5f}, {0.0f, 0.0f, 1.0f, 22.5f}, null, null};
    private static final float[][] ROT_4 = new float[][]{null, null, null, {1.0f, 0.0f, 0.0f, -22.5f}, {1.0f, 0.0f, 0.0f, -22.5f}, {1.0f, 0.0f, 0.0f, -22.5f}, {1.0f, 0.0f, 0.0f, 22.5f}, {1.0f, 0.0f, 0.0f, 22.5f}, {0.0f, 0.0f, 1.0f, -22.5f}, {0.0f, 0.0f, 1.0f, -22.5f}, {0.0f, 0.0f, 1.0f, -22.5f}, {0.0f, 0.0f, 1.0f, 22.5f}, {0.0f, 0.0f, 1.0f, 22.5f}, {0.0f, 0.0f, 1.0f, 22.5f}, null, null, {1.0f, 0.0f, 0.0f, 22.5f}};
    private static final float[][][] UV_2 = new float[][][]{new float[][]{{0.0f, 0.0f, 0.1875f, 0.1875f}, {0.0f, 0.1875f, 0.1875f, 0.375f}, {0.1875f, 0.0f, 0.375f, 0.1875f}, {0.1875f, 0.1875f, 0.375f, 0.375f}, {0.1875f, 0.5625f, 0.0f, 0.375f}, {0.5625f, 0.0f, 0.375f, 0.1875f}}, new float[][]{{0.1875f, 0.1875f, 0.0f, 0.0f}, {0.1875f, 0.375f, 0.0f, 0.1875f}, {0.375f, 0.1875f, 0.1875f, 0.0f}, {0.375f, 0.375f, 0.1875f, 0.1875f}, {0.1875f, 0.375f, 0.0f, 0.5625f}, {0.5625f, 0.0f, 0.375f, 0.1875f}}, new float[][]{{0.1875f, 0.1875f, 0.0f, 0.0f}, {0.1875f, 0.375f, 0.0f, 0.1875f}, {0.375f, 0.1875f, 0.1875f, 0.0f}, {0.375f, 0.375f, 0.1875f, 0.1875f}, {0.1875f, 0.375f, 0.0f, 0.5625f}, {0.5625f, 0.0f, 0.375f, 0.1875f}}, new float[][]{{0.375f, 0.1875f, 0.1875f, 0.375f}, {0.1875f, 0.0f, 0.0f, 0.1875f}, {0.1875f, 0.1875f, 0.0f, 0.375f}, {0.375f, 0.0f, 0.1875f, 0.1875f}, {0.5625f, 0.0f, 0.375f, 0.1875f}, {0.1875f, 0.375f, 0.0f, 0.5625f}}, new float[][]{{0.375f, 0.1875f, 0.1875f, 0.375f}, {0.1875f, 0.0f, 0.0f, 0.1875f}, {0.1875f, 0.1875f, 0.0f, 0.375f}, {0.375f, 0.0f, 0.1875f, 0.1875f}, {0.5625f, 0.0f, 0.375f, 0.1875f}, {0.1875f, 0.375f, 0.0f, 0.5625f}}, new float[][]{{0.375f, 0.1875f, 0.1875f, 0.375f}, {0.1875f, 0.0f, 0.0f, 0.1875f}, {0.1875f, 0.1875f, 0.0f, 0.375f}, {0.375f, 0.0f, 0.1875f, 0.1875f}, {0.5625f, 0.0f, 0.375f, 0.1875f}, {0.1875f, 0.375f, 0.0f, 0.5625f}}, new float[][]{{0.1875f, 0.1875f, 0.0f, 0.375f}, {0.375f, 0.0f, 0.1875f, 0.1875f}, {0.375f, 0.1875f, 0.1875f, 0.375f}, {0.1875f, 0.0f, 0.0f, 0.1875f}, {0.375f, 0.1875f, 0.5625f, 0.0f}, {0.0f, 0.5625f, 0.1875f, 0.375f}}, new float[][]{{0.1875f, 0.1875f, 0.0f, 0.375f}, {0.375f, 0.0f, 0.1875f, 0.1875f}, {0.375f, 0.1875f, 0.1875f, 0.375f}, {0.1875f, 0.0f, 0.0f, 0.1875f}, {0.375f, 0.1875f, 0.5625f, 0.0f}, {0.0f, 0.5625f, 0.1875f, 0.375f}}, new float[][]{{0.1875f, 0.1875f, 0.0f, 0.375f}, {0.375f, 0.0f, 0.1875f, 0.1875f}, {0.375f, 0.1875f, 0.1875f, 0.375f}, {0.1875f, 0.0f, 0.0f, 0.1875f}, {0.375f, 0.1875f, 0.5625f, 0.0f}, {0.0f, 0.5625f, 0.1875f, 0.375f}}, new float[][]{{0.1875f, 0.1875f, 0.0f, 0.0f}, {0.1875f, 0.375f, 0.0f, 0.1875f}, {0.375f, 0.1875f, 0.1875f, 0.0f}, {0.375f, 0.375f, 0.1875f, 0.1875f}, {0.1875f, 0.375f, 0.0f, 0.5625f}, {0.5625f, 0.0f, 0.375f, 0.1875f}}, new float[][]{{0.1875f, 0.1875f, 0.0f, 0.0f}, {0.1875f, 0.375f, 0.0f, 0.1875f}, {0.375f, 0.1875f, 0.1875f, 0.0f}, {0.375f, 0.375f, 0.1875f, 0.1875f}, {0.1875f, 0.375f, 0.0f, 0.5625f}, {0.5625f, 0.0f, 0.375f, 0.1875f}}};
    private static final float[][][] UV_4 = new float[][][]{new float[][]{{0.0f, 0.0f, 0.1875f, 0.1875f}, {0.0f, 0.1875f, 0.1875f, 0.375f}, {0.1875f, 0.0f, 0.375f, 0.1875f}, {0.1875f, 0.1875f, 0.375f, 0.375f}, {0.1875f, 0.5625f, 0.0f, 0.375f}, {0.5625f, 0.0f, 0.375f, 0.1875f}}, new float[][]{{0.1875f, 0.1875f, 0.0f, 0.0f}, {0.1875f, 0.375f, 0.0f, 0.1875f}, {0.375f, 0.1875f, 0.1875f, 0.0f}, {0.375f, 0.375f, 0.1875f, 0.1875f}, {0.1875f, 0.375f, 0.0f, 0.5625f}, {0.5625f, 0.0f, 0.375f, 0.1875f}}, new float[][]{{0.1875f, 0.1875f, 0.0f, 0.0f}, {0.1875f, 0.375f, 0.0f, 0.1875f}, {0.375f, 0.1875f, 0.1875f, 0.0f}, {0.375f, 0.375f, 0.1875f, 0.1875f}, {0.1875f, 0.375f, 0.0f, 0.5625f}, {0.5625f, 0.0f, 0.375f, 0.1875f}}, new float[][]{{0.1875f, 0.0f, 0.0f, 0.1875f}, {0.1875f, 0.1875f, 0.0f, 0.375f}, {0.375f, 0.0f, 0.1875f, 0.1875f}, {0.375f, 0.1875f, 0.1875f, 0.375f}, {0.5625f, 0.0f, 0.375f, 0.1875f}, {0.1875f, 0.375f, 0.0f, 0.5625f}}, new float[][]{{0.1875f, 0.0f, 0.0f, 0.1875f}, {0.1875f, 0.1875f, 0.0f, 0.375f}, {0.375f, 0.0f, 0.1875f, 0.1875f}, {0.375f, 0.1875f, 0.1875f, 0.375f}, {0.5625f, 0.0f, 0.375f, 0.1875f}, {0.1875f, 0.375f, 0.0f, 0.5625f}}, new float[][]{{0.1875f, 0.0f, 0.0f, 0.1875f}, {0.1875f, 0.1875f, 0.0f, 0.375f}, {0.375f, 0.0f, 0.1875f, 0.1875f}, {0.375f, 0.1875f, 0.1875f, 0.375f}, {0.5625f, 0.0f, 0.375f, 0.1875f}, {0.1875f, 0.375f, 0.0f, 0.5625f}}, new float[][]{{0.375f, 0.0f, 0.1875f, 0.1875f}, {0.375f, 0.1875f, 0.1875f, 0.375f}, {0.1875f, 0.0f, 0.0f, 0.1875f}, {0.1875f, 0.1875f, 0.0f, 0.375f}, {0.375f, 0.1875f, 0.5625f, 0.0f}, {0.0f, 0.5625f, 0.1875f, 0.375f}}, new float[][]{{0.375f, 0.0f, 0.1875f, 0.1875f}, {0.375f, 0.1875f, 0.1875f, 0.375f}, {0.1875f, 0.0f, 0.0f, 0.1875f}, {0.1875f, 0.1875f, 0.0f, 0.375f}, {0.375f, 0.1875f, 0.5625f, 0.0f}, {0.0f, 0.5625f, 0.1875f, 0.375f}}, new float[][]{{0.375f, 0.1875f, 0.1875f, 0.375f}, {0.1875f, 0.0f, 0.0f, 0.1875f}, {0.1875f, 0.1875f, 0.0f, 0.375f}, {0.375f, 0.0f, 0.1875f, 0.1875f}, {0.5625f, 0.0f, 0.375f, 0.1875f}, {0.1875f, 0.375f, 0.0f, 0.5625f}}, new float[][]{{0.375f, 0.1875f, 0.1875f, 0.375f}, {0.1875f, 0.0f, 0.0f, 0.1875f}, {0.1875f, 0.1875f, 0.0f, 0.375f}, {0.375f, 0.0f, 0.1875f, 0.1875f}, {0.5625f, 0.0f, 0.375f, 0.1875f}, {0.1875f, 0.375f, 0.0f, 0.5625f}}, new float[][]{{0.375f, 0.1875f, 0.1875f, 0.375f}, {0.1875f, 0.0f, 0.0f, 0.1875f}, {0.1875f, 0.1875f, 0.0f, 0.375f}, {0.375f, 0.0f, 0.1875f, 0.1875f}, {0.5625f, 0.0f, 0.375f, 0.1875f}, {0.1875f, 0.375f, 0.0f, 0.5625f}}, new float[][]{{0.1875f, 0.1875f, 0.0f, 0.375f}, {0.375f, 0.0f, 0.1875f, 0.1875f}, {0.375f, 0.1875f, 0.1875f, 0.375f}, {0.1875f, 0.0f, 0.0f, 0.1875f}, {0.375f, 0.1875f, 0.5625f, 0.0f}, {0.0f, 0.5625f, 0.1875f, 0.375f}}, new float[][]{{0.1875f, 0.1875f, 0.0f, 0.375f}, {0.375f, 0.0f, 0.1875f, 0.1875f}, {0.375f, 0.1875f, 0.1875f, 0.375f}, {0.1875f, 0.0f, 0.0f, 0.1875f}, {0.375f, 0.1875f, 0.5625f, 0.0f}, {0.0f, 0.5625f, 0.1875f, 0.375f}}, new float[][]{{0.1875f, 0.1875f, 0.0f, 0.375f}, {0.375f, 0.0f, 0.1875f, 0.1875f}, {0.375f, 0.1875f, 0.1875f, 0.375f}, {0.1875f, 0.0f, 0.0f, 0.1875f}, {0.375f, 0.1875f, 0.5625f, 0.0f}, {0.0f, 0.5625f, 0.1875f, 0.375f}}, new float[][]{{0.1875f, 0.1875f, 0.0f, 0.0f}, {0.1875f, 0.375f, 0.0f, 0.1875f}, {0.375f, 0.1875f, 0.1875f, 0.0f}, {0.375f, 0.375f, 0.1875f, 0.1875f}, {0.1875f, 0.375f, 0.0f, 0.5625f}, {0.5625f, 0.0f, 0.375f, 0.1875f}}, new float[][]{{0.1875f, 0.1875f, 0.0f, 0.0f}, {0.1875f, 0.375f, 0.0f, 0.1875f}, {0.375f, 0.1875f, 0.1875f, 0.0f}, {0.375f, 0.375f, 0.1875f, 0.1875f}, {0.1875f, 0.375f, 0.0f, 0.5625f}, {0.5625f, 0.0f, 0.375f, 0.1875f}}, new float[][]{{0.375f, 0.0f, 0.1875f, 0.1875f}, {0.375f, 0.1875f, 0.1875f, 0.375f}, {0.1875f, 0.0f, 0.0f, 0.1875f}, {0.1875f, 0.1875f, 0.0f, 0.375f}, {0.375f, 0.1875f, 0.5625f, 0.0f}, {0.0f, 0.5625f, 0.1875f, 0.375f}}};

    public PropellerBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    public void render(PropellerBlockEntity be, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        PropellerBlock pb;
        Block block = be.getBlockState().getBlock();
        boolean isFourBlade = block instanceof PropellerBlock && (pb = (PropellerBlock)block).requiresCastIron();
        Direction facing = (Direction)be.getBlockState().getValue((Property)PropellerBlock.FACING);
        ResourceLocation texLoc = isFourBlade ? FOUR_BLADE_TEXTURE : TWO_BLADE_TEXTURE;
        TextureAtlasSprite sprite = (TextureAtlasSprite)Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(texLoc);
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.cutoutMipped());
        float angleDeg = (float)Math.toDegrees(be.getRotationAngle(partialTick));
        double[][] boxes = isFourBlade ? BOX_4 : BOX_2;
        float[][] rots = isFourBlade ? ROT_4 : ROT_2;
        float[][][] uvs = isFourBlade ? UV_4 : UV_2;
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        switch (facing) {
            case EAST: {
                poseStack.mulPose(Axis.XP.rotationDegrees(angleDeg));
                poseStack.mulPose(Axis.YP.rotationDegrees(180.0f));
                poseStack.mulPose(Axis.ZP.rotationDegrees(-90.0f));
                break;
            }
            case WEST: {
                poseStack.mulPose(Axis.XP.rotationDegrees(-angleDeg));
                poseStack.mulPose(Axis.YP.rotationDegrees(0.0f));
                poseStack.mulPose(Axis.ZP.rotationDegrees(-90.0f));
                break;
            }
            case SOUTH: {
                poseStack.mulPose(Axis.ZP.rotationDegrees(angleDeg));
                poseStack.mulPose(Axis.YP.rotationDegrees(90.0f));
                poseStack.mulPose(Axis.ZP.rotationDegrees(-90.0f));
                break;
            }
            case NORTH: {
                poseStack.mulPose(Axis.ZP.rotationDegrees(-angleDeg));
                poseStack.mulPose(Axis.YP.rotationDegrees(270.0f));
                poseStack.mulPose(Axis.ZP.rotationDegrees(-90.0f));
                break;
            }
            default: {
                poseStack.mulPose(Axis.YP.rotationDegrees(angleDeg));
            }
        }
        poseStack.translate(-0.5, -0.5, -0.5);
        for (int i = 0; i < boxes.length; ++i) {
            double[] b = boxes[i];
            float[] r = rots[i];
            float[][] uv = uvs[i];
            double x0 = b[0] / 16.0;
            double y0 = b[1] / 16.0;
            double z0 = b[2] / 16.0;
            double x1 = b[3] / 16.0;
            double y1 = b[4] / 16.0;
            double z1 = b[5] / 16.0;
            poseStack.pushPose();
            if (r != null) {
                double cx = (x0 + x1) / 2.0;
                double cy = (y0 + y1) / 2.0;
                double cz = (z0 + z1) / 2.0;
                Quaternionf q = new Quaternionf().rotateAxis((float)Math.toRadians(r[3]), r[0], r[1], r[2]);
                poseStack.translate(cx, cy, cz);
                poseStack.mulPose(q);
                poseStack.translate(-cx, -cy, -cz);
            }
            PropellerBlockEntityRenderer.emitCuboid(poseStack, buffer, sprite, packedLight, packedOverlay, x0, y0, z0, x1, y1, z1, uv);
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private static void emitCuboid(PoseStack ps, VertexConsumer buf, TextureAtlasSprite spr, int light, int overlay, double x0, double y0, double z0, double x1, double y1, double z1, float[][] uv) {
        float fx0 = (float)x0;
        float fy0 = (float)y0;
        float fz0 = (float)z0;
        float fx1 = (float)x1;
        float fy1 = (float)y1;
        float fz1 = (float)z1;
        PropellerBlockEntityRenderer.emitFace(ps, buf, spr, light, overlay, uv[0], fx1, fy1, fz0, fx1, fy0, fz0, fx0, fy0, fz0, fx0, fy1, fz0, 0.0f, 0.0f, -1.0f);
        PropellerBlockEntityRenderer.emitFace(ps, buf, spr, light, overlay, uv[1], fx1, fy1, fz1, fx1, fy0, fz1, fx1, fy0, fz0, fx1, fy1, fz0, 1.0f, 0.0f, 0.0f);
        PropellerBlockEntityRenderer.emitFace(ps, buf, spr, light, overlay, uv[2], fx0, fy1, fz1, fx0, fy0, fz1, fx1, fy0, fz1, fx1, fy1, fz1, 0.0f, 0.0f, 1.0f);
        PropellerBlockEntityRenderer.emitFace(ps, buf, spr, light, overlay, uv[3], fx0, fy1, fz0, fx0, fy0, fz0, fx0, fy0, fz1, fx0, fy1, fz1, -1.0f, 0.0f, 0.0f);
        PropellerBlockEntityRenderer.emitFace(ps, buf, spr, light, overlay, uv[4], fx0, fy1, fz0, fx0, fy1, fz1, fx1, fy1, fz1, fx1, fy1, fz0, 0.0f, 1.0f, 0.0f);
        PropellerBlockEntityRenderer.emitFace(ps, buf, spr, light, overlay, uv[5], fx0, fy0, fz1, fx0, fy0, fz0, fx1, fy0, fz0, fx1, fy0, fz1, 0.0f, -1.0f, 0.0f);
    }

    private static void emitFace(PoseStack ps, VertexConsumer buf, TextureAtlasSprite spr, int light, int overlay, float[] uv, float ax, float ay, float az, float bx, float by, float bz, float cx, float cy, float cz, float dx, float dy, float dz, float nx, float ny, float nz) {
        Matrix4f pose = ps.last().pose();
        Matrix3f norm = ps.last().normal();
        float u0 = spr.getU((float)(uv[0]) / 16.0f);
        float v0 = spr.getV((float)(uv[1]) / 16.0f);
        float u1 = spr.getU((float)(uv[2]) / 16.0f);
        float v1 = spr.getV((float)(uv[3]) / 16.0f);
        tfcmt.tfcitembarrelsandmore.client.MaritimeRender.normal(buf.addVertex(pose, ax, ay, az).setColor(255, 255, 255, 255).setUv(u0, v0).setOverlay(overlay).setLight(light), norm, nx, ny, nz);
        tfcmt.tfcitembarrelsandmore.client.MaritimeRender.normal(buf.addVertex(pose, bx, by, bz).setColor(255, 255, 255, 255).setUv(u0, v1).setOverlay(overlay).setLight(light), norm, nx, ny, nz);
        tfcmt.tfcitembarrelsandmore.client.MaritimeRender.normal(buf.addVertex(pose, cx, cy, cz).setColor(255, 255, 255, 255).setUv(u1, v1).setOverlay(overlay).setLight(light), norm, nx, ny, nz);
        tfcmt.tfcitembarrelsandmore.client.MaritimeRender.normal(buf.addVertex(pose, dx, dy, dz).setColor(255, 255, 255, 255).setUv(u1, v0).setOverlay(overlay).setLight(light), norm, nx, ny, nz);
    }

    public boolean shouldRenderOffScreen(PropellerBlockEntity be) {
        return true;
    }

    public int getViewDistance() {
        return 128;
    }
}

