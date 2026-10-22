package tfcmt.tfcitembarrelsandmore.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.properties.Property;
import org.joml.Matrix4f;
import tfcmt.tfcitembarrelsandmore.common.blockentities.BoilerBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.BoilerBlock;

public class BoilerBlockEntityRenderer
implements BlockEntityRenderer<BoilerBlockEntity> {
    private static final float CAST_IRON_EYE_Y0 = 0.5625f;
    private static final float CAST_IRON_EYE_Y1 = 0.625f;
    private static final float STEEL_EYE_Y0 = 0.4375f;
    private static final float STEEL_EYE_Y1 = 0.5f;
    private static final float COL_A0 = 0.4375f;
    private static final float COL_A1 = 0.5f;
    private static final float COL_B0 = 0.5f;
    private static final float COL_B1 = 0.5625f;
    private static final float DOOR_OUTSET = 0.0625f;
    private static final float DOOR_ZFIGHT_BIAS = 0.001f;
    private static final float FLUE_X0 = 0.4375f;
    private static final float FLUE_MX = 0.5f;
    private static final float FLUE_X1 = 0.5625f;
    private static final float FLUE_Z0 = 0.4375f;
    private static final float FLUE_MZ = 0.5f;
    private static final float FLUE_Z1 = 0.5625f;
    private static final float FLUE_Y = 1.001f;
    private static final float MIN_RENDER_TEMP = 80.0f;
    private static final float TEMP_OFFSET = 300.0f;
    private static final int FLICKER_TICKS = 3;
    private static final float RANGE = 0.1f;

    public BoilerBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    public void render(BoilerBlockEntity be, float partialTick, PoseStack ps, MultiBufferSource buffers, int packedLight, int packedOverlay) {
        if (be.getLevel() == null) {
            return;
        }
        float temp = be.getTemperature();
        if (temp < 80.0f) {
            return;
        }
        float displayTemp = Math.max(80.0f, temp - 300.0f);
        float[] base = BoilerBlockEntityRenderer.tempToRgb(displayTemp);
        long bucket = be.getLevel().getGameTime() / 3L;
        Direction facing = (Direction)be.getBlockState().getValue((Property)BoilerBlock.FACING);
        boolean steelTank = (Boolean)be.getBlockState().getValue((Property)BoilerBlock.STEEL_TANK);
        float eyeY0 = steelTank ? 0.4375f : 0.5625f;
        float eyeY1 = steelTank ? 0.5f : 0.625f;
        ps.pushPose();
        Matrix4f mat = ps.last().pose();
        VertexConsumer buf = buffers.getBuffer(RenderType.debugQuads());
        BoilerBlockEntityRenderer.emitFrontPixels(buf, mat, facing, eyeY0, eyeY1, BoilerBlockEntityRenderer.flicker(base, bucket, 0), BoilerBlockEntityRenderer.flicker(base, bucket, 1));
        ps.translate(0.5, 0.5, 0.5);
        ps.mulPose(Axis.YP.rotationDegrees(BoilerBlockEntityRenderer.facingYaw(facing)));
        ps.translate(-0.5, -0.5, -0.5);
        mat = ps.last().pose();
        for (int i = 0; i < 4; ++i) {
            float[] c = BoilerBlockEntityRenderer.flicker(base, bucket, i + 2);
            float x0 = (i & 1) == 0 ? 0.4375f : 0.5f;
            float x1 = (i & 1) == 0 ? 0.5f : 0.5625f;
            float z0 = i < 2 ? 0.4375f : 0.5f;
            float z1 = i < 2 ? 0.5f : 0.5625f;
            BoilerBlockEntityRenderer.upQuad(buf, mat, x0, z0, x1, z1, 1.001f, c);
        }
        ps.popPose();
    }

    private static void emitFrontPixels(VertexConsumer buf, Matrix4f mat, Direction facing, float eyeY0, float eyeY1, float[] cA, float[] cB) {
        switch (facing) {
            case NORTH: {
                float z = -0.0635f;
                BoilerBlockEntityRenderer.northQuad(buf, mat, 0.5f, eyeY0, 0.5625f, eyeY1, z, cA);
                BoilerBlockEntityRenderer.northQuad(buf, mat, 0.4375f, eyeY0, 0.5f, eyeY1, z, cB);
                break;
            }
            case SOUTH: {
                float z = 1.0635f;
                BoilerBlockEntityRenderer.southQuad(buf, mat, 0.4375f, eyeY0, 0.5f, eyeY1, z, cA);
                BoilerBlockEntityRenderer.southQuad(buf, mat, 0.5f, eyeY0, 0.5625f, eyeY1, z, cB);
                break;
            }
            case EAST: {
                float x = 1.0635f;
                BoilerBlockEntityRenderer.eastQuad(buf, mat, x, eyeY0, 0.4375f, eyeY1, 0.5f, cA);
                BoilerBlockEntityRenderer.eastQuad(buf, mat, x, eyeY0, 0.5f, eyeY1, 0.5625f, cB);
                break;
            }
            case WEST: {
                float x = -0.0635f;
                BoilerBlockEntityRenderer.westQuad(buf, mat, x, eyeY0, 0.5f, eyeY1, 0.5625f, cA);
                BoilerBlockEntityRenderer.westQuad(buf, mat, x, eyeY0, 0.4375f, eyeY1, 0.5f, cB);
                break;
            }
        }
    }

    private static void northQuad(VertexConsumer buf, Matrix4f mat, float x0, float y0, float x1, float y1, float z, float[] c) {
        buf.addVertex(mat, x0, y0, z).setColor(c[0], c[1], c[2], 1.0f);
        buf.addVertex(mat, x1, y0, z).setColor(c[0], c[1], c[2], 1.0f);
        buf.addVertex(mat, x1, y1, z).setColor(c[0], c[1], c[2], 1.0f);
        buf.addVertex(mat, x0, y1, z).setColor(c[0], c[1], c[2], 1.0f);
    }

    private static void southQuad(VertexConsumer buf, Matrix4f mat, float x0, float y0, float x1, float y1, float z, float[] c) {
        buf.addVertex(mat, x0, y1, z).setColor(c[0], c[1], c[2], 1.0f);
        buf.addVertex(mat, x1, y1, z).setColor(c[0], c[1], c[2], 1.0f);
        buf.addVertex(mat, x1, y0, z).setColor(c[0], c[1], c[2], 1.0f);
        buf.addVertex(mat, x0, y0, z).setColor(c[0], c[1], c[2], 1.0f);
    }

    private static void eastQuad(VertexConsumer buf, Matrix4f mat, float x, float y0, float z0, float y1, float z1, float[] c) {
        buf.addVertex(mat, x, y0, z1).setColor(c[0], c[1], c[2], 1.0f);
        buf.addVertex(mat, x, y0, z0).setColor(c[0], c[1], c[2], 1.0f);
        buf.addVertex(mat, x, y1, z0).setColor(c[0], c[1], c[2], 1.0f);
        buf.addVertex(mat, x, y1, z1).setColor(c[0], c[1], c[2], 1.0f);
    }

    private static void westQuad(VertexConsumer buf, Matrix4f mat, float x, float y0, float z0, float y1, float z1, float[] c) {
        buf.addVertex(mat, x, y0, z0).setColor(c[0], c[1], c[2], 1.0f);
        buf.addVertex(mat, x, y0, z1).setColor(c[0], c[1], c[2], 1.0f);
        buf.addVertex(mat, x, y1, z1).setColor(c[0], c[1], c[2], 1.0f);
        buf.addVertex(mat, x, y1, z0).setColor(c[0], c[1], c[2], 1.0f);
    }

    private static void upQuad(VertexConsumer buf, Matrix4f mat, float x0, float z0, float x1, float z1, float y, float[] c) {
        buf.addVertex(mat, x0, y, z0).setColor(c[0], c[1], c[2], 1.0f);
        buf.addVertex(mat, x1, y, z0).setColor(c[0], c[1], c[2], 1.0f);
        buf.addVertex(mat, x1, y, z1).setColor(c[0], c[1], c[2], 1.0f);
        buf.addVertex(mat, x0, y, z1).setColor(c[0], c[1], c[2], 1.0f);
    }

    private static float[] flicker(float[] base, long bucket, int idx) {
        RandomSource rng = RandomSource.create((long)(bucket * 31L + (long)idx));
        return new float[]{BoilerBlockEntityRenderer.clamp01(base[0] + (rng.nextFloat() * 2.0f - 1.0f) * 0.1f), BoilerBlockEntityRenderer.clamp01(base[1] + (rng.nextFloat() * 2.0f - 1.0f) * 0.1f), BoilerBlockEntityRenderer.clamp01(base[2] + (rng.nextFloat() * 2.0f - 1.0f) * 0.1f)};
    }

    private static float[] tempToRgb(float temp) {
        if (temp < 180.0f) {
            return BoilerBlockEntityRenderer.lerp(0, 0x4D0000, (temp - 80.0f) / 100.0f);
        }
        if (temp < 480.0f) {
            return BoilerBlockEntityRenderer.lerp(0x4D0000, 9116160, (temp - 180.0f) / 300.0f);
        }
        if (temp < 580.0f) {
            return BoilerBlockEntityRenderer.lerp(9116160, 16729344, (temp - 480.0f) / 100.0f);
        }
        if (temp < 700.0f) {
            return BoilerBlockEntityRenderer.lerp(16729344, 16747520, (temp - 580.0f) / 120.0f);
        }
        if (temp < 800.0f) {
            return BoilerBlockEntityRenderer.lerp(16747520, 16766720, (temp - 700.0f) / 100.0f);
        }
        if (temp < 1000.0f) {
            return BoilerBlockEntityRenderer.lerp(16766720, 0xFFFF00, (temp - 800.0f) / 200.0f);
        }
        return BoilerBlockEntityRenderer.lerp(0xFFFF00, 0xFFFFFF, (temp - 1000.0f) / 400.0f);
    }

    private static float[] lerp(int from, int to, float t) {
        t = BoilerBlockEntityRenderer.clamp01(t);
        float r = (float)(from >> 16 & 0xFF) + t * (float)((to >> 16 & 0xFF) - (from >> 16 & 0xFF));
        float g = (float)(from >> 8 & 0xFF) + t * (float)((to >> 8 & 0xFF) - (from >> 8 & 0xFF));
        float b = (float)(from & 0xFF) + t * (float)((to & 0xFF) - (from & 0xFF));
        return new float[]{r / 255.0f, g / 255.0f, b / 255.0f};
    }

    private static float clamp01(float v) {
        return Math.max(0.0f, Math.min(1.0f, v));
    }

    private static float facingYaw(Direction facing) {
        return switch (facing) {
            case Direction.EAST -> 90.0f;
            case Direction.SOUTH -> 180.0f;
            case Direction.WEST -> 270.0f;
            default -> 0.0f;
        };
    }
}

