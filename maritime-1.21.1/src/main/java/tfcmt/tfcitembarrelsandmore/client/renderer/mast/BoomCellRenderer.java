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
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import tfcmt.tfcitembarrelsandmore.common.TIBAMBlocks;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.BoomFillerBlockEntity;

public class BoomCellRenderer
implements BlockEntityRenderer<BoomFillerBlockEntity> {
    public BoomCellRenderer(BlockEntityRendererProvider.Context context) {
    }

    public void render(BoomFillerBlockEntity be, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        float z1;
        float z0;
        float x1;
        float x0;
        TIBAMBlocks.Wood wood = be.getWood();
        int widthPx = be.getWidthPx();
        Direction face = be.getFace();
        boolean alongZ = face.getAxis() == Direction.Axis.Z;
        TextureAtlasSprite sprite = (TextureAtlasSprite)Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(wood.strippedLogTexture());
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.cutoutMipped());
        float half = (float)widthPx / 16.0f / 2.0f;
        if (alongZ) {
            x0 = 0.5f - half;
            x1 = 0.5f + half;
            z0 = 0.0f;
            z1 = 1.0f;
        } else {
            x0 = 0.0f;
            x1 = 1.0f;
            z0 = 0.5f - half;
            z1 = 0.5f + half;
        }
        float y0 = 0.5f - half;
        float y1 = 0.5f + half;
        int yawSteps = be.getYawSteps();
        if (yawSteps == 0) {
            BoomCellRenderer.emitCuboid(poseStack, buffer, sprite, packedLight, packedOverlay, x0, y0, z0, x1, y1, z1);
        } else {
            int distance = be.getDistanceFromHost();
            float pivotX = 0.5f - (float)(face.getStepX() * distance);
            float pivotZ = 0.5f - (float)(face.getStepZ() * distance);
            poseStack.pushPose();
            poseStack.translate(pivotX, 0.0f, pivotZ);
            poseStack.mulPose(Axis.YP.rotationDegrees((float)yawSteps * 20.0f));
            poseStack.translate(-pivotX, 0.0f, -pivotZ);
            BoomCellRenderer.emitCuboid(poseStack, buffer, sprite, packedLight, packedOverlay, x0, y0, z0, x1, y1, z1);
            poseStack.popPose();
        }
    }

    private static void emitCuboid(PoseStack ps, VertexConsumer buf, TextureAtlasSprite spr, int light, int overlay, float x0, float y0, float z0, float x1, float y1, float z1) {
        BoomCellRenderer.emitFace(ps, buf, spr, light, overlay, x1, y1, z0, x1, y0, z0, x0, y0, z0, x0, y1, z0, 0.0f, 0.0f, -1.0f);
        BoomCellRenderer.emitFace(ps, buf, spr, light, overlay, x1, y1, z1, x1, y0, z1, x1, y0, z0, x1, y1, z0, 1.0f, 0.0f, 0.0f);
        BoomCellRenderer.emitFace(ps, buf, spr, light, overlay, x0, y1, z1, x0, y0, z1, x1, y0, z1, x1, y1, z1, 0.0f, 0.0f, 1.0f);
        BoomCellRenderer.emitFace(ps, buf, spr, light, overlay, x0, y1, z0, x0, y0, z0, x0, y0, z1, x0, y1, z1, -1.0f, 0.0f, 0.0f);
        BoomCellRenderer.emitFace(ps, buf, spr, light, overlay, x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0, 0.0f, 1.0f, 0.0f);
        BoomCellRenderer.emitFace(ps, buf, spr, light, overlay, x0, y0, z1, x0, y0, z0, x1, y0, z0, x1, y0, z1, 0.0f, -1.0f, 0.0f);
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

