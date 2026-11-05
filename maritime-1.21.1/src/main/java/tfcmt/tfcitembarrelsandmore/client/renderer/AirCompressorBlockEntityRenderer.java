package tfcmt.tfcitembarrelsandmore.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.dries007.tfc.util.rotation.Rotation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.data.ModelData;
import tfcmt.tfcitembarrelsandmore.client.renderer.HoseRope;
import tfcmt.tfcitembarrelsandmore.client.renderer.TubeMesh;
import tfcmt.tfcitembarrelsandmore.common.TIBAMConfig;
import tfcmt.tfcitembarrelsandmore.common.blockentities.AirCompressorBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.AirCompressorBlock;

public class AirCompressorBlockEntityRenderer
implements BlockEntityRenderer<AirCompressorBlockEntity> {
    private static final ResourceLocation MODEL_FLYWHEEL = ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/air_compressor_flywheel");
    private static final ResourceLocation HOSE_TEXTURE = ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "textures/entity/hose.png");
    private static final RenderType HOSE_RENDER_TYPE = RenderType.entityCutoutNoCull((ResourceLocation)HOSE_TEXTURE);
    private static final double MESH_SEGMENT_LENGTH = 0.4;
    private static final double TEXTURE_REPEAT_LENGTH = 0.6;
    private static final double HALF_WIDTH = 0.05;
    private final Map<PathKey, RopeState> ropeCache = new HashMap<PathKey, RopeState>();
    private static final double SHOULDER_HEIGHT = 1.4;
    private static final double SHOULDER_SIDE = 0.32;

    public AirCompressorBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    public boolean shouldRenderOffScreen(AirCompressorBlockEntity be) {
        return true;
    }

    public int getViewDistance() {
        return (Integer)TIBAMConfig.SERVER.compressorHoseMaxRange.get();
    }

    public void render(AirCompressorBlockEntity be, float partialTick, PoseStack ps, MultiBufferSource buffers, int packedLight, int packedOverlay) {
        this.renderHoses(be, partialTick, ps, buffers, packedLight);
        if (!be.isConnectedToNetwork()) {
            return;
        }
        BlockState state = be.getBlockState();
        Direction facing = (Direction)state.getValue((Property)AirCompressorBlock.FACING);
        BakedModel model = Minecraft.getInstance().getModelManager().getModel(net.minecraft.client.resources.model.ModelResourceLocation.standalone(MODEL_FLYWHEEL));
        float rawAngleDeg = (float)Math.toDegrees(Rotation.angle((Rotation)be.getSinkRotation(), (float)partialTick));
        float spinAngleDeg = switch (facing) {
            case Direction.SOUTH, Direction.EAST -> rawAngleDeg;
            case Direction.NORTH, Direction.WEST -> -rawAngleDeg;
            default -> rawAngleDeg;
        };
        ps.pushPose();
        AirCompressorBlockEntityRenderer.applyFacingTransform(ps, facing);
        if (facing == Direction.EAST || facing == Direction.WEST) {
            ps.translate(0.5, 0.5, 0.5);
            ps.mulPose(Axis.YP.rotationDegrees(180.0f));
            ps.translate(-0.5, -0.5, -0.5);
        }
        ps.translate(0.5, 0.5, 0.5);
        ps.mulPose(Axis.ZP.rotationDegrees(spinAngleDeg));
        ps.translate(-0.5, -0.5, -0.5);
        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(ps.last(), buffers.getBuffer(RenderType.cutoutMipped()), state, model, 1.0f, 1.0f, 1.0f, packedLight, packedOverlay, ModelData.EMPTY, RenderType.cutoutMipped());
        ps.popPose();
    }

    private static void applyFacingTransform(PoseStack ps, Direction facing) {
        ps.translate(0.5, 0.5, 0.5);
        switch (facing) {
            case EAST: {
                ps.mulPose(Axis.YP.rotationDegrees(90.0f));
                break;
            }
            case SOUTH: {
                ps.mulPose(Axis.YP.rotationDegrees(180.0f));
                break;
            }
            case WEST: {
                ps.mulPose(Axis.YP.rotationDegrees(270.0f));
                break;
            }
        }
        ps.translate(-0.5, -0.5, -0.5);
    }

    private static Vec3 rightShoulderOffset(float bodyYawDegrees) {
        float yawRad = bodyYawDegrees * ((float)Math.PI / 180);
        double rightX = -Mth.cos((float)yawRad);
        double rightZ = -Mth.sin((float)yawRad);
        return new Vec3(rightX * 0.32, 1.4, rightZ * 0.32);
    }

    private void renderHoses(AirCompressorBlockEntity be, float partialTick, PoseStack ps, MultiBufferSource buffers, int packedLight) {
        Level level = be.getLevel();
        if (level == null) {
            return;
        }
        BlockPos compressorPos = be.getBlockPos();
        Vec3 hosePortWorld = be.hosePortPos();
        Vec3 renderOrigin = Vec3.atLowerCornerOf((Vec3i)compressorPos);
        long gameTime = level.getGameTime();
        HashSet<UUID> stillConnected = new HashSet<UUID>();
        for (UUID uuid : be.getLinkedPlayerUUIDs()) {
            Player player = level.getPlayerByUUID(uuid);
            if (player == null || !be.isPlayerConnected(player)) continue;
            stillConnected.add(uuid);
            PathKey key = new PathKey(compressorPos, uuid);
            RopeState state = this.ropeCache.get(key);
            Vec3 anchorEndTick = player.position().add(AirCompressorBlockEntityRenderer.rightShoulderOffset(player.yBodyRot));
            if (state == null) {
                HoseRope rope = new HoseRope(hosePortWorld, anchorEndTick);
                List<Vec3> initial = rope.snapshot();
                state = new RopeState(rope, initial, initial, gameTime);
                this.ropeCache.put(key, state);
            } else if (state.lastStepTick != gameTime) {
                List<Vec3> stepped = state.rope.step(level, hosePortWorld, anchorEndTick);
                state = new RopeState(state.rope, state.currentTickPoints, stepped, gameTime);
                this.ropeCache.put(key, state);
            }
            ArrayList<Vec3> renderPoints = new ArrayList<Vec3>(state.currentTickPoints.size());
            for (int i = 0; i < state.currentTickPoints.size(); ++i) {
                Vec3 lerped = AirCompressorBlockEntityRenderer.lerp(state.prevTickPoints.get(i), state.currentTickPoints.get(i), partialTick);
                renderPoints.add(lerped.subtract(renderOrigin));
            }
            renderPoints.set(0, hosePortWorld.subtract(renderOrigin));
            float lerpedBodyYaw = Mth.rotLerp((float)partialTick, (float)player.yBodyRotO, (float)player.yBodyRot);
            renderPoints.set(renderPoints.size() - 1, player.getPosition(partialTick).add(AirCompressorBlockEntityRenderer.rightShoulderOffset(lerpedBodyYaw)).subtract(renderOrigin));
            AirCompressorBlockEntityRenderer.renderHosePath(renderPoints, ps, buffers, packedLight);
        }
        this.ropeCache.keySet().removeIf(k -> k.compressorPos().equals((Object)compressorPos) && !stillConnected.contains(k.player()));
    }

    private static Vec3 lerp(Vec3 a, Vec3 b, double t) {
        return new Vec3(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t, a.z + (b.z - a.z) * t);
    }

    private static void renderHosePath(List<Vec3> points, PoseStack ps, MultiBufferSource buffers, int packedLight) {
        TubeMesh.render(points, 0.4, 0.6, 0.05, HOSE_RENDER_TYPE, ps, buffers, packedLight);
    }

    private record PathKey(BlockPos compressorPos, UUID player) {
    }

    private static final class RopeState {
        final HoseRope rope;
        final List<Vec3> prevTickPoints;
        final List<Vec3> currentTickPoints;
        final long lastStepTick;

        RopeState(HoseRope rope, List<Vec3> prevTickPoints, List<Vec3> currentTickPoints, long lastStepTick) {
            this.rope = rope;
            this.prevTickPoints = prevTickPoints;
            this.currentTickPoints = currentTickPoints;
            this.lastStepTick = lastStepTick;
        }
    }
}

