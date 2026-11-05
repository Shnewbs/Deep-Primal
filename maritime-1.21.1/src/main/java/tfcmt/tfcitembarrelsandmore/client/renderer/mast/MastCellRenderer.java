package tfcmt.tfcitembarrelsandmore.client.renderer.mast;

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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import tfcmt.tfcitembarrelsandmore.common.TIBAMBlocks;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.BoomHost;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.MastBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.MastCell;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.MastRakeable;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.YardHost;
import tfcmt.tfcitembarrelsandmore.common.blocks.mast.MastRake;
import tfcmt.tfcitembarrelsandmore.common.blocks.mast.YardGrowth;

public class MastCellRenderer<T extends BlockEntity>
implements BlockEntityRenderer<T> {
    public MastCellRenderer(BlockEntityRendererProvider.Context context) {
    }

    public void render(T be, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        BoomHost bh;
        YardHost yh;
        Direction rakeFace;
        MastRakeable r;
        TIBAMBlocks.Wood wood = ((MastCell)be).getWood();
        int widthPx = ((MastCell)be).getWidthPx();
        TextureAtlasSprite sprite = (TextureAtlasSprite)Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(wood.strippedLogTexture());
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.cutoutMipped());
        float half = (float)widthPx / 16.0f / 2.0f;
        float x0 = 0.5f - half;
        float x1 = 0.5f + half;
        float z0 = 0.5f - half;
        float z1 = 0.5f + half;
        MastRakeable rakeable = be instanceof MastRakeable ? (r = (MastRakeable)be) : null;
        int rakeSteps = rakeable != null ? rakeable.getRakeSteps() : 0;
        int offsetAboveAnchor = rakeable != null ? rakeable.getOffsetAboveAnchor() : 0;
        Direction direction = rakeFace = rakeable != null ? rakeable.getRakeFace() : Direction.NORTH;
        if (rakeSteps == 0) {
            MastCellRenderer.emitCuboid(poseStack, buffer, sprite, packedLight, packedOverlay, x0, 0.0f, z0, x1, 1.0f, z1);
        } else {
            poseStack.pushPose();
            poseStack.translate(0.5, (double)(-offsetAboveAnchor), 0.5);
            poseStack.mulPose(MastRake.rotation(rakeFace, rakeSteps));
            poseStack.translate(-0.5, (double)offsetAboveAnchor, -0.5);
            MastCellRenderer.emitCuboid(poseStack, buffer, sprite, packedLight, packedOverlay, x0, 0.0f, z0, x1, 1.0f, z1);
            poseStack.popPose();
        }
        if (be instanceof YardHost && (yh = (YardHost)be).hasYard()) {
            Vec3 rakeShift;
            float yz1;
            float yz0;
            float yx1;
            float yx0;
            TIBAMBlocks.Wood yardWood = yh.getYardWood();
            int yardWidthPx = MastBlockEntity.widthForOffset(0, yh.getYardArmLength() + 1);
            double offsetBlocks = YardGrowth.faceOffsetBlocks(yh, widthPx);
            Direction bias = yh.getYardFaceBias();
            boolean alongZ = yh.getYardAxis() == Direction.Axis.Z;
            int step = alongZ ? bias.getStepX() : bias.getStepZ();
            float perpOffset = (float)((double)step * offsetBlocks);
            TextureAtlasSprite yardSprite = (TextureAtlasSprite)Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(yardWood.strippedLogTexture());
            float yHalf = (float)yardWidthPx / 16.0f / 2.0f;
            float c = 0.5f + perpOffset;
            if (alongZ) {
                yx0 = c - yHalf;
                yx1 = c + yHalf;
                yz0 = 0.0f;
                yz1 = 1.0f;
            } else {
                yx0 = 0.0f;
                yx1 = 1.0f;
                yz0 = c - yHalf;
                yz1 = c + yHalf;
            }
            float yy0 = 0.5f - yHalf;
            float yy1 = 0.5f + yHalf;
            int yawSteps = yh.getYawSteps();
            boolean raked = rakeSteps != 0;
            Vec3 vec3 = rakeShift = raked ? MastRake.displacement(rakeFace, rakeSteps, (double)offsetAboveAnchor + 0.5) : Vec3.ZERO;
            if (raked) {
                poseStack.pushPose();
            }
            if (raked) {
                poseStack.translate(rakeShift.x, rakeShift.y, rakeShift.z);
            }
            if (yawSteps == 0) {
                MastCellRenderer.emitCuboid(poseStack, buffer, yardSprite, packedLight, packedOverlay, yx0, yy0, yz0, yx1, yy1, yz1);
            } else {
                poseStack.pushPose();
                poseStack.translate(0.5, 0.0, 0.5);
                poseStack.mulPose(Axis.YP.rotationDegrees((float)yawSteps * 20.0f));
                poseStack.translate(-0.5, 0.0, -0.5);
                MastCellRenderer.emitCuboid(poseStack, buffer, yardSprite, packedLight, packedOverlay, yx0, yy0, yz0, yx1, yy1, yz1);
                poseStack.popPose();
            }
            if (raked) {
                poseStack.popPose();
            }
        }
        if (be instanceof BoomHost && (bh = (BoomHost)be).hasBoom()) {
            float bz1;
            float bz0;
            float bx1;
            float bx0;
            TIBAMBlocks.Wood boomWood = bh.getBoomWood();
            int boomWidthPx = MastBlockEntity.widthForOffset(0, bh.getBoomLength() + 1);
            Direction face = bh.getBoomFace();
            boolean alongZ = face.getAxis() == Direction.Axis.Z;
            TextureAtlasSprite boomSprite = (TextureAtlasSprite)Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(boomWood.strippedLogTexture());
            float bHalf = (float)boomWidthPx / 16.0f / 2.0f;
            if (alongZ) {
                bx0 = 0.5f - bHalf;
                bx1 = 0.5f + bHalf;
                if (face.getStepZ() > 0) {
                    bz0 = 0.5f;
                    bz1 = 1.0f;
                } else {
                    bz0 = 0.0f;
                    bz1 = 0.5f;
                }
            } else {
                bz0 = 0.5f - bHalf;
                bz1 = 0.5f + bHalf;
                if (face.getStepX() > 0) {
                    bx0 = 0.5f;
                    bx1 = 1.0f;
                } else {
                    bx0 = 0.0f;
                    bx1 = 0.5f;
                }
            }
            float by0 = 0.5f - bHalf;
            float by1 = 0.5f + bHalf;
            MastCellRenderer.emitCuboid(poseStack, buffer, boomSprite, packedLight, packedOverlay, bx0, by0, bz0, bx1, by1, bz1);
        }
    }

    private static void emitCuboid(PoseStack ps, VertexConsumer buf, TextureAtlasSprite spr, int light, int overlay, float x0, float y0, float z0, float x1, float y1, float z1) {
        MastCellRenderer.emitFace(ps, buf, spr, light, overlay, x1, y1, z0, x1, y0, z0, x0, y0, z0, x0, y1, z0, 0.0f, 0.0f, -1.0f);
        MastCellRenderer.emitFace(ps, buf, spr, light, overlay, x1, y1, z1, x1, y0, z1, x1, y0, z0, x1, y1, z0, 1.0f, 0.0f, 0.0f);
        MastCellRenderer.emitFace(ps, buf, spr, light, overlay, x0, y1, z1, x0, y0, z1, x1, y0, z1, x1, y1, z1, 0.0f, 0.0f, 1.0f);
        MastCellRenderer.emitFace(ps, buf, spr, light, overlay, x0, y1, z0, x0, y0, z0, x0, y0, z1, x0, y1, z1, -1.0f, 0.0f, 0.0f);
        MastCellRenderer.emitFace(ps, buf, spr, light, overlay, x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0, 0.0f, 1.0f, 0.0f);
        MastCellRenderer.emitFace(ps, buf, spr, light, overlay, x0, y0, z1, x0, y0, z0, x1, y0, z0, x1, y0, z1, 0.0f, -1.0f, 0.0f);
    }

    private static void emitFace(PoseStack ps, VertexConsumer buf, TextureAtlasSprite spr, int light, int overlay, float ax, float ay, float az, float bx, float by, float bz, float cx, float cy, float cz, float dx, float dy, float dz, float nx, float ny, float nz) {
        Matrix4f pose = ps.last().pose();
        Matrix3f norm = ps.last().normal();
        float u0 = spr.getU((float)(0.0) / 16.0f);
        float v0 = spr.getV((float)(0.0) / 16.0f);
        float u1 = spr.getU((float)(16.0) / 16.0f);
        float v1 = spr.getV((float)(16.0) / 16.0f);
        tfcmt.tfcitembarrelsandmore.client.MaritimeRender.normal(buf.addVertex(pose, ax, ay, az).setColor(255, 255, 255, 255).setUv(u0, v0).setOverlay(overlay).setLight(light), norm, nx, ny, nz);
        tfcmt.tfcitembarrelsandmore.client.MaritimeRender.normal(buf.addVertex(pose, bx, by, bz).setColor(255, 255, 255, 255).setUv(u0, v1).setOverlay(overlay).setLight(light), norm, nx, ny, nz);
        tfcmt.tfcitembarrelsandmore.client.MaritimeRender.normal(buf.addVertex(pose, cx, cy, cz).setColor(255, 255, 255, 255).setUv(u1, v1).setOverlay(overlay).setLight(light), norm, nx, ny, nz);
        tfcmt.tfcitembarrelsandmore.client.MaritimeRender.normal(buf.addVertex(pose, dx, dy, dz).setColor(255, 255, 255, 255).setUv(u1, v0).setOverlay(overlay).setLight(light), norm, nx, ny, nz);
    }
}

