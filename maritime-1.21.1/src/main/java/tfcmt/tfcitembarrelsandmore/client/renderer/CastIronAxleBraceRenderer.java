package tfcmt.tfcitembarrelsandmore.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.dries007.tfc.client.RenderHelpers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import tfcmt.tfcitembarrelsandmore.common.blockentities.rotation.CastIronAxleBraceBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blocks.rotation.CastIronAxleBraceBlock;

public class CastIronAxleBraceRenderer
implements BlockEntityRenderer<CastIronAxleBraceBlockEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/cast_iron_axle_brace");
    private static final float TEXTURE_PX = 64.0f;
    private static final AABB SHAFT_HOUSING = CastIronAxleBraceRenderer.aabb(5.0, 5.0, 0.0, 11.0, 11.0, 16.0);
    private static final AABB BRACKET_ARM_FRONT = CastIronAxleBraceRenderer.aabb(6.0, 1.0, 2.0, 10.0, 5.0, 6.0);
    private static final AABB BRACKET_ARM_REAR = CastIronAxleBraceRenderer.aabb(6.0, 1.0, 10.0, 10.0, 5.0, 14.0);
    private static final AABB BASE_PLATE_FRONT = CastIronAxleBraceRenderer.aabb(5.0, 0.0, 1.0, 11.0, 1.0, 7.0);
    private static final AABB BASE_PLATE_REAR = CastIronAxleBraceRenderer.aabb(5.0, 0.0, 9.0, 11.0, 1.0, 15.0);
    private static final float[][] UV_SHAFT_HOUSING = new float[][]{{12.0f, 12.0f, 6.0f, 28.0f}, {6.0f, 28.0f, 0.0f, 12.0f}, {12.0f, 12.0f, 18.0f, 18.0f}, {16.0f, 0.0f, 22.0f, 6.0f}, {0.0f, 6.0f, 16.0f, 12.0f}, {0.0f, 0.0f, 16.0f, 6.0f}};
    private static final float[][] UV_BRACKET_ARM_FRONT = new float[][]{{20.0f, 24.0f, 16.0f, 28.0f}, {28.0f, 16.0f, 24.0f, 12.0f}, {22.0f, 0.0f, 26.0f, 4.0f}, {22.0f, 8.0f, 26.0f, 12.0f}, {12.0f, 24.0f, 16.0f, 28.0f}, {22.0f, 4.0f, 26.0f, 8.0f}};
    private static final float[][] UV_BRACKET_ARM_REAR = new float[][]{{30.0f, 4.0f, 26.0f, 8.0f}, {30.0f, 4.0f, 26.0f, 0.0f}, {24.0f, 16.0f, 28.0f, 20.0f}, {24.0f, 20.0f, 28.0f, 24.0f}, {24.0f, 24.0f, 28.0f, 28.0f}, {20.0f, 24.0f, 24.0f, 28.0f}};
    private static final float[][] UV_BASE_PLATE_FRONT = new float[][]{{18.0f, 18.0f, 12.0f, 24.0f}, {22.0f, 12.0f, 16.0f, 6.0f}, {26.0f, 8.0f, 32.0f, 9.0f}, {26.0f, 10.0f, 32.0f, 11.0f}, {26.0f, 11.0f, 32.0f, 12.0f}, {26.0f, 9.0f, 32.0f, 10.0f}};
    private static final float[][] UV_BASE_PLATE_REAR = new float[][]{{24.0f, 18.0f, 18.0f, 24.0f}, {24.0f, 18.0f, 18.0f, 12.0f}, {0.0f, 28.0f, 6.0f, 29.0f}, {12.0f, 28.0f, 18.0f, 29.0f}, {28.0f, 12.0f, 34.0f, 13.0f}, {6.0f, 28.0f, 12.0f, 29.0f}};
    private static final Direction[] FACES = new Direction[]{Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};

    public CastIronAxleBraceRenderer(BlockEntityRendererProvider.Context context) {
    }

    public void render(CastIronAxleBraceBlockEntity be, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        BlockState state = be.getBlockState();
        Direction face = (Direction)state.getValue((Property)CastIronAxleBraceBlock.FACE);
        Direction.Axis axis = (Direction.Axis)state.getValue((Property)BlockStateProperties.AXIS);
        TextureAtlasSprite sprite = RenderHelpers.blockTexture((ResourceLocation)TEXTURE);
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.cutoutMipped());
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        switch (face) {
            case DOWN: {
                break;
            }
            case UP: {
                poseStack.mulPose(Axis.XP.rotationDegrees(180.0f));
                break;
            }
            case NORTH: {
                poseStack.mulPose(Axis.XP.rotationDegrees(90.0f));
                break;
            }
            case SOUTH: {
                poseStack.mulPose(Axis.XP.rotationDegrees(-90.0f));
                break;
            }
            case WEST: {
                poseStack.mulPose(Axis.ZP.rotationDegrees(-90.0f));
                break;
            }
            case EAST: {
                poseStack.mulPose(Axis.ZP.rotationDegrees(90.0f));
            }
        }
        switch (axis) {
            case X: {
                poseStack.mulPose(Axis.YP.rotationDegrees(90.0f));
                break;
            }
            case Z: {
                break;
            }
            case Y: {
                poseStack.mulPose(Axis.XP.rotationDegrees(90.0f));
            }
        }
        poseStack.translate(-0.5, -0.5, -0.5);
        CastIronAxleBraceRenderer.renderCuboidWithUV(poseStack, buffer, sprite, packedLight, packedOverlay, SHAFT_HOUSING, UV_SHAFT_HOUSING);
        CastIronAxleBraceRenderer.renderCuboidWithUV(poseStack, buffer, sprite, packedLight, packedOverlay, BRACKET_ARM_FRONT, UV_BRACKET_ARM_FRONT);
        CastIronAxleBraceRenderer.renderCuboidWithUV(poseStack, buffer, sprite, packedLight, packedOverlay, BRACKET_ARM_REAR, UV_BRACKET_ARM_REAR);
        CastIronAxleBraceRenderer.renderCuboidWithUV(poseStack, buffer, sprite, packedLight, packedOverlay, BASE_PLATE_FRONT, UV_BASE_PLATE_FRONT);
        CastIronAxleBraceRenderer.renderCuboidWithUV(poseStack, buffer, sprite, packedLight, packedOverlay, BASE_PLATE_REAR, UV_BASE_PLATE_REAR);
        poseStack.popPose();
    }

    private static void renderCuboidWithUV(PoseStack poseStack, VertexConsumer buffer, TextureAtlasSprite sprite, int light, int overlay, AABB aabb, float[][] faceUV) {
        for (int i = 0; i < FACES.length; ++i) {
            CastIronAxleBraceRenderer.renderFace(poseStack, buffer, sprite, light, overlay, aabb, FACES[i], faceUV[i]);
        }
    }

    private static void renderFace(PoseStack poseStack, VertexConsumer buffer, TextureAtlasSprite sprite, int light, int overlay, AABB aabb, Direction face, float[] uvRect) {
        float[][] fArrayArray;
        float x1 = (float)aabb.minX;
        float y1 = (float)aabb.minY;
        float z1 = (float)aabb.minZ;
        float x2 = (float)aabb.maxX;
        float y2 = (float)aabb.maxY;
        float z2 = (float)aabb.maxZ;
        switch (face) {
            default: {
                throw new IncompatibleClassChangeError();
            }
            case DOWN: {
                float[][] fArrayArray2 = new float[4][];
                fArrayArray2[0] = new float[]{x1, y1, z1};
                fArrayArray2[1] = new float[]{x2, y1, z1};
                fArrayArray2[2] = new float[]{x2, y1, z2};
                fArrayArray = fArrayArray2;
                fArrayArray2[3] = new float[]{x1, y1, z2};
                break;
            }
            case UP: {
                float[][] fArrayArray3 = new float[4][];
                fArrayArray3[0] = new float[]{x1, y2, z1};
                fArrayArray3[1] = new float[]{x1, y2, z2};
                fArrayArray3[2] = new float[]{x2, y2, z2};
                fArrayArray = fArrayArray3;
                fArrayArray3[3] = new float[]{x2, y2, z1};
                break;
            }
            case NORTH: {
                float[][] fArrayArray4 = new float[4][];
                fArrayArray4[0] = new float[]{x1, y1, z1};
                fArrayArray4[1] = new float[]{x1, y2, z1};
                fArrayArray4[2] = new float[]{x2, y2, z1};
                fArrayArray = fArrayArray4;
                fArrayArray4[3] = new float[]{x2, y1, z1};
                break;
            }
            case SOUTH: {
                float[][] fArrayArray5 = new float[4][];
                fArrayArray5[0] = new float[]{x1, y1, z2};
                fArrayArray5[1] = new float[]{x2, y1, z2};
                fArrayArray5[2] = new float[]{x2, y2, z2};
                fArrayArray = fArrayArray5;
                fArrayArray5[3] = new float[]{x1, y2, z2};
                break;
            }
            case WEST: {
                float[][] fArrayArray6 = new float[4][];
                fArrayArray6[0] = new float[]{x1, y1, z1};
                fArrayArray6[1] = new float[]{x1, y1, z2};
                fArrayArray6[2] = new float[]{x1, y2, z2};
                fArrayArray = fArrayArray6;
                fArrayArray6[3] = new float[]{x1, y2, z1};
                break;
            }
            case EAST: {
                float[][] fArrayArray7 = new float[4][];
                fArrayArray7[0] = new float[]{x2, y1, z1};
                fArrayArray7[1] = new float[]{x2, y2, z1};
                fArrayArray7[2] = new float[]{x2, y2, z2};
                fArrayArray = fArrayArray7;
                fArrayArray7[3] = new float[]{x2, y1, z2};
            }
        }
        float[][] corners = fArrayArray;
        float nx = face.getStepX();
        float ny = face.getStepY();
        float nz = face.getStepZ();
        float u16_0 = uvRect[0] / 64.0f * 16.0f;
        float v16_0 = uvRect[1] / 64.0f * 16.0f;
        float u16_1 = uvRect[2] / 64.0f * 16.0f;
        float v16_1 = uvRect[3] / 64.0f * 16.0f;
        float su0 = sprite.getU((float)(u16_0) / 16.0f);
        float sv0 = sprite.getV((float)(v16_0) / 16.0f);
        float su1 = sprite.getU((float)(u16_1) / 16.0f);
        float sv1 = sprite.getV((float)(v16_1) / 16.0f);
        float[][] uvs = new float[][]{{su1, sv0}, {su1, sv1}, {su0, sv1}, {su0, sv0}};
        for (int i = 0; i < 4; ++i) {
            float[] pos = corners[i];
            float[] uv = uvs[i];
            RenderHelpers.renderTexturedVertex((PoseStack)poseStack, (VertexConsumer)buffer, (int)light, (int)overlay, (float)pos[0], (float)pos[1], (float)pos[2], (float)uv[0], (float)uv[1], (float)nx, (float)ny, (float)nz);
        }
    }

    private static AABB aabb(double x1, double y1, double z1, double x2, double y2, double z2) {
        return new AABB(x1 / 16.0, y1 / 16.0, z1 / 16.0, x2 / 16.0, y2 / 16.0, z2 / 16.0);
    }
}

