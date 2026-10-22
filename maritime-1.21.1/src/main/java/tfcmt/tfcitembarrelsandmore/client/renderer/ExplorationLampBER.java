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
import tfcmt.tfcitembarrelsandmore.common.blockentities.ExplorationLampBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.ExplorationLampBlock;

public class ExplorationLampBER
implements BlockEntityRenderer<ExplorationLampBlockEntity> {
    private static final float FRONT_Z = 0.249f;
    private static final float FRONT_X0 = 0.4375f;
    private static final float FRONT_X1 = 0.5625f;
    private static final float FRONT_Y0 = 0.1875f;
    private static final float FRONT_Y1 = 0.3125f;
    private static final float SIDE_Y0 = 0.1875f;
    private static final float SIDE_Y1 = 0.3125f;
    private static final float SIDE_C0 = 0.4375f;
    private static final float SIDE_C1 = 0.5625f;
    private static final float EAST_X = 0.6885f;
    private static final float WEST_X = 0.3115f;
    private static final float SOUTH_Z = 0.6885f;
    private static final int FLICKER_TICKS = 3;
    private static final float[][] FLAME_PALETTE = new float[][]{{1.0f, 0.8f, 0.1f}, {1.0f, 0.55f, 0.02f}, {1.0f, 0.7f, 0.05f}, {1.0f, 0.4f, 0.0f}, {1.0f, 0.75f, 0.2f}, {0.95f, 0.5f, 0.02f}, {1.0f, 0.65f, 0.08f}, {1.0f, 0.85f, 0.15f}};

    public ExplorationLampBER(BlockEntityRendererProvider.Context ctx) {
    }

    public void render(ExplorationLampBlockEntity be, float partialTick, PoseStack ps, MultiBufferSource buffers, int packedLight, int packedOverlay) {
        float yRot;
        if (be.getLevel() == null) {
            return;
        }
        if (!((Boolean)be.getBlockState().getValue((Property)ExplorationLampBlock.LIT)).booleanValue()) {
            return;
        }
        ExplorationLampBlockEntity.Mode mode = be.getMode();
        Direction facing = (Direction)be.getBlockState().getValue((Property)ExplorationLampBlock.FACING);
        long gameTime = be.getLevel().getGameTime();
        ps.pushPose();
        switch (facing) {
            case SOUTH: {
                float f = 180.0f;
                break;
            }
            case EAST: {
                float f = 270.0f;
                break;
            }
            case WEST: {
                float f = 90.0f;
                break;
            }
            default: {
                float f = yRot = 0.0f;
            }
        }
        if (yRot != 0.0f) {
            ps.translate(0.5f, 0.0f, 0.5f);
            ps.mulPose(Axis.YP.rotationDegrees(yRot));
            ps.translate(-0.5f, 0.0f, -0.5f);
        }
        ExplorationLampBER.drawGlow(mode, gameTime, ps, buffers);
        ps.popPose();
    }

    public static void drawGlow(ExplorationLampBlockEntity.Mode mode, long gameTime, PoseStack ps, MultiBufferSource buffers) {
        long bucket = gameTime / 3L;
        Matrix4f mat = ps.last().pose();
        VertexConsumer buf = buffers.getBuffer(RenderType.debugQuads());
        float[] c = ExplorationLampBER.flicker(bucket, 0);
        ExplorationLampBER.quad(buf, mat, 0.4375f, 0.1875f, 0.249f, 0.5625f, 0.3125f, 0.249f, true, c);
        if (mode == ExplorationLampBlockEntity.Mode.AREA) {
            float[] c1 = ExplorationLampBER.flicker(bucket, 1);
            ExplorationLampBER.quad(buf, mat, 0.6885f, 0.1875f, 0.4375f, 0.6885f, 0.3125f, 0.5625f, false, c1);
            float[] c2 = ExplorationLampBER.flicker(bucket, 2);
            ExplorationLampBER.quad(buf, mat, 0.4375f, 0.1875f, 0.6885f, 0.5625f, 0.3125f, 0.6885f, true, c2);
            float[] c3 = ExplorationLampBER.flicker(bucket, 3);
            ExplorationLampBER.quad(buf, mat, 0.3115f, 0.1875f, 0.4375f, 0.3115f, 0.3125f, 0.5625f, false, c3);
        }
    }

    private static void quad(VertexConsumer buf, Matrix4f mat, float x0, float y0, float z0, float x1, float y1, float z1, boolean zConst, float[] c) {
        if (zConst) {
            buf.addVertex(mat, x0, y0, z0).setColor(c[0], c[1], c[2], 1.0f);
            buf.addVertex(mat, x1, y0, z0).setColor(c[0], c[1], c[2], 1.0f);
            buf.addVertex(mat, x1, y1, z1).setColor(c[0], c[1], c[2], 1.0f);
            buf.addVertex(mat, x0, y1, z1).setColor(c[0], c[1], c[2], 1.0f);
        } else {
            buf.addVertex(mat, x0, y0, z0).setColor(c[0], c[1], c[2], 1.0f);
            buf.addVertex(mat, x1, y0, z1).setColor(c[0], c[1], c[2], 1.0f);
            buf.addVertex(mat, x1, y1, z1).setColor(c[0], c[1], c[2], 1.0f);
            buf.addVertex(mat, x0, y1, z0).setColor(c[0], c[1], c[2], 1.0f);
        }
    }

    private static float[] flicker(long bucket, int idx) {
        RandomSource rng = RandomSource.create((long)(bucket * 31L + (long)idx * 7L));
        int a = (int)(rng.nextLong() & 7L) % FLAME_PALETTE.length;
        int b = (int)((rng.nextLong() & 7L) + 1L) % FLAME_PALETTE.length;
        float t = rng.nextFloat();
        float[] ca = FLAME_PALETTE[a];
        float[] cb = FLAME_PALETTE[b];
        return new float[]{ExplorationLampBER.clamp(ca[0] + (cb[0] - ca[0]) * t), ExplorationLampBER.clamp(ca[1] + (cb[1] - ca[1]) * t), ExplorationLampBER.clamp(ca[2] + (cb[2] - ca[2]) * t)};
    }

    private static float clamp(float v) {
        return Math.max(0.0f, Math.min(1.0f, v));
    }
}

