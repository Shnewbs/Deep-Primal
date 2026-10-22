package tfcmt.tfcitembarrelsandmore.client.renderer.mast;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
import org.joml.Vector3f;
import tfcmt.tfcitembarrelsandmore.client.renderer.TubeMesh;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.BoomHost;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.MastBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.MastCell;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.RigLink;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.RigLinkRegistry;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.RigLinkable;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.RigSlot;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.YardHost;
import tfcmt.tfcitembarrelsandmore.common.blocks.mast.YardGrowth;

@EventBusSubscriber(modid="tfc_item_barrels_and_more", bus=EventBusSubscriber.Bus.FORGE, value={Dist.CLIENT})
public final class RigLinkRenderer {
    private static final ResourceLocation RIG_LINE_TEXTURE = ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "textures/entity/rig_line.png");
    private static final RenderType RIG_RENDER_TYPE = RenderType.entityCutoutNoCull((ResourceLocation)RIG_LINE_TEXTURE);
    private static final double HALF_WIDTH = 0.025;
    private static final double TEXTURE_REPEAT_LENGTH = 0.3;
    private static final double MESH_SEGMENT_LENGTH = 1000.0;
    private static final int BOW_SAMPLES = 8;
    private static final double BOW_SAG_FRACTION = 0.05;
    private static final double BOW_MAX_SAG = 0.35;
    private static final double CLEAT_LOOP_RADIUS = 0.09;
    private static final int CLEAT_LOOP_SAMPLES = 10;
    private static final int CLEAT_MAX_LAYERS = 6;
    private static final int CLEAT_STACK_COUNT = 3;
    private static final double CLEAT_STACK_STEP = 0.035;
    private static final double CLEAT_TIER_RADIUS_STEP = 0.035;

    private RigLinkRenderer() {
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }
        if (Minecraft.getInstance().level == null) {
            return;
        }
        Collection<RigLinkable> hosts = RigLinkRegistry.allClient();
        if (hosts.isEmpty()) {
            return;
        }
        ClientLevel level = Minecraft.getInstance().level;
        Vec3 cam = event.getCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        poseStack.pushPose();
        poseStack.translate(-cam.x, -cam.y, -cam.z);
        for (RigLinkable host : hosts) {
            for (RigSlot slot : new RigSlot[]{RigSlot.YARD_LEFT, RigSlot.YARD_RIGHT, RigSlot.BOOM_END, RigSlot.BOOM_END_PORT}) {
                Vec3 from = RigLinkRenderer.resolveEndpoint(host, slot);
                if (from == null) continue;
                for (RigLink link : host.getLinks(slot)) {
                    Vec3 to;
                    RigLinkable target;
                    int uuidCompare;
                    if (!RigLinkRenderer.isRotationSlot(link.targetSlot()) && link.targetSlot() != RigSlot.CLEAT_LINE || RigLinkRenderer.isRotationSlot(link.targetSlot()) && ((uuidCompare = host.getRigUUID().compareTo(link.targetUuid())) > 0 || uuidCompare == 0 && slot.ordinal() >= link.targetSlot().ordinal()) || (target = RigLinkRegistry.findClient(link.targetUuid())) == null || (to = RigLinkRenderer.resolveEndpoint(target, link.targetSlot())) == null) continue;
                    BlockPos midBlock = BlockPos.containing((Position)from.add(to).scale(0.5));
                    int packedLight = LevelRenderer.getLightColor((BlockAndTintGetter)level, (BlockPos)midBlock);
                    List<Vec3> path = link.targetSlot() == RigSlot.CLEAT_LINE ? RigLinkRenderer.cleatLinkPath(from, to, target, RigLinkRenderer.cleatLayerIndex(target, host, slot)) : RigLinkRenderer.bowedPath(from, to);
                    TubeMesh.render(path, 1000.0, 0.3, 0.025, RIG_RENDER_TYPE, poseStack, (MultiBufferSource)bufferSource, packedLight);
                }
            }
        }
        poseStack.popPose();
        bufferSource.endBatch(RIG_RENDER_TYPE);
    }

    private static boolean isRotationSlot(RigSlot slot) {
        return slot == RigSlot.YARD_LEFT || slot == RigSlot.YARD_RIGHT || slot == RigSlot.BOOM_END || slot == RigSlot.BOOM_END_PORT;
    }

    private static Vec3 resolveEndpoint(RigLinkable host, RigSlot slot) {
        BoomHost boomHost;
        YardHost yardHost;
        BlockEntity be;
        if (!(host instanceof BlockEntity) || (be = (BlockEntity)host).isRemoved() || be.getLevel() == null) {
            return null;
        }
        BlockPos basePos = be.getBlockPos();
        if (slot == RigSlot.CLEAT_LINE) {
            return Vec3.atCenterOf((Vec3i)basePos);
        }
        if ((slot == RigSlot.YARD_LEFT || slot == RigSlot.YARD_RIGHT) && host instanceof YardHost && (yardHost = (YardHost)((Object)host)).hasYard() && host instanceof MastCell) {
            MastCell hostCell = (MastCell)((Object)host);
            Direction posDir = YardGrowth.positiveDirectionFor(yardHost.getYardAxis());
            Direction dir = slot == RigSlot.YARD_RIGHT ? posDir : posDir.getOpposite();
            int armLength = yardHost.getYardArmLength();
            double perpOffsetBlocks = YardGrowth.faceOffsetBlocks(yardHost, hostCell.getWidthPx());
            Direction faceBias = yardHost.getYardFaceBias();
            float offsetX = (float)(dir.getStepX() * armLength) + (float)faceBias.getStepX() * (float)perpOffsetBlocks;
            float offsetZ = (float)(dir.getStepZ() * armLength) + (float)faceBias.getStepZ() * (float)perpOffsetBlocks;
            Vector3f offset = new Vector3f(offsetX, 0.0f, offsetZ);
            int yawSteps = yardHost.getYawSteps();
            if (yawSteps != 0) {
                Axis.YP.rotationDegrees((float)yawSteps * 20.0f).transform(offset);
            }
            int tipWidthPx = MastBlockEntity.widthForOffset(armLength, armLength + 1);
            float halfHeight = (float)tipWidthPx / 16.0f / 2.0f;
            return Vec3.atCenterOf((Vec3i)basePos).add((double)offset.x, (double)halfHeight, (double)offset.z);
        }
        if ((slot == RigSlot.BOOM_END || slot == RigSlot.BOOM_END_PORT) && host instanceof BoomHost && (boomHost = (BoomHost)((Object)host)).hasBoom()) {
            int distance;
            int boomLength = boomHost.getBoomLength();
            int n = distance = slot == RigSlot.BOOM_END ? boomLength : boomLength - 1;
            if (distance <= 0) {
                return null;
            }
            Direction face = boomHost.getBoomFace();
            float offsetX = face.getStepX() * distance;
            float offsetZ = face.getStepZ() * distance;
            Vector3f offset = new Vector3f(offsetX, 0.0f, offsetZ);
            int yawSteps = boomHost.getBoomYawSteps();
            if (yawSteps != 0) {
                Axis.YP.rotationDegrees((float)yawSteps * 20.0f).transform(offset);
            }
            int widthPx = MastBlockEntity.widthForOffset(distance, boomLength + 1);
            float halfHeight = (float)widthPx / 16.0f / 2.0f;
            return Vec3.atCenterOf((Vec3i)basePos).add((double)offset.x, (double)halfHeight, (double)offset.z);
        }
        return null;
    }

    private static List<Vec3> bowedPath(Vec3 from, Vec3 to) {
        double horizLen = Math.sqrt((to.x - from.x) * (to.x - from.x) + (to.z - from.z) * (to.z - from.z));
        double sag = Math.min(horizLen * 0.05, 0.35);
        ArrayList<Vec3> path = new ArrayList<Vec3>(9);
        for (int i = 0; i <= 8; ++i) {
            double t = (double)i / 8.0;
            Vec3 base = from.add(to.subtract(from).scale(t));
            double drop = sag * 4.0 * t * (1.0 - t);
            path.add(base.subtract(0.0, drop, 0.0));
        }
        return path;
    }

    private static List<Vec3> cleatLinkPath(Vec3 from, Vec3 cleatCenter, RigLinkable cleatHost, int layerIndex) {
        BlockEntity be;
        if (!(cleatHost instanceof BlockEntity) || (be = (BlockEntity)cleatHost).getLevel() == null) {
            return RigLinkRenderer.bowedPath(from, cleatCenter);
        }
        BlockState state = be.getBlockState();
        if (!state.hasProperty((Property)BlockStateProperties.FACING)) {
            return RigLinkRenderer.bowedPath(from, cleatCenter);
        }
        Direction facing = (Direction)state.getValue((Property)BlockStateProperties.FACING);
        List<Vec3> loop = RigLinkRenderer.cleatWrapLoop(Vec3.atLowerCornerOf((Vec3i)be.getBlockPos()), facing, layerIndex);
        ArrayList<Vec3> path = new ArrayList<Vec3>(RigLinkRenderer.bowedPath(from, loop.get(0)));
        path.addAll(loop.subList(1, loop.size()));
        path.add(cleatCenter);
        return path;
    }

    private static int cleatLayerIndex(RigLinkable cleat, RigLinkable host, RigSlot hostSlot) {
        int index = 0;
        for (RigLink reciprocal : cleat.getLinks(RigSlot.CLEAT_LINE)) {
            if (reciprocal.targetUuid().equals(host.getRigUUID()) && reciprocal.targetSlot() == hostSlot) {
                return Math.min(index, 5);
            }
            ++index;
        }
        return 0;
    }

    private static List<Vec3> cleatWrapLoop(Vec3 blockOrigin, Direction facing, int layerIndex) {
        HornGeometry g = RigLinkRenderer.hornGeometry(facing);
        Vec3 rawPost1 = blockOrigin.add(g.post1Offset());
        Vec3 rawPost2 = blockOrigin.add(g.post2Offset());
        Vec3 w = rawPost2.subtract(rawPost1).normalize();
        Vec3 perp = g.axis().cross(w).normalize();
        int stackPos = layerIndex % 3;
        int tier = layerIndex / 3;
        Vec3 stackOffset = g.axis().scale((double)stackPos * 0.035);
        Vec3 post1 = rawPost1.add(stackOffset);
        Vec3 post2 = rawPost2.add(stackOffset);
        double radius = 0.09 + (double)tier * 0.035;
        ArrayList<Vec3> path = new ArrayList<Vec3>();
        RigLinkRenderer.appendArc(path, post1, w, perp, radius, 180.0, 540.0, 10);
        RigLinkRenderer.appendArc(path, post2, w, perp, radius, 180.0, -180.0, 10);
        return path;
    }

    private static void appendArc(List<Vec3> path, Vec3 center, Vec3 w, Vec3 perp, double radius, double startDeg, double endDeg, int samples) {
        for (int i = 0; i <= samples; ++i) {
            double t = (double)i / (double)samples;
            double rad = Math.toRadians(startDeg + (endDeg - startDeg) * t);
            double cx = Math.cos(rad) * radius;
            double cz = Math.sin(rad) * radius;
            path.add(center.add(w.scale(cx)).add(perp.scale(cz)));
        }
    }

    private static HornGeometry hornGeometry(Direction facing) {
        return switch (facing) {
            case Direction.DOWN -> new HornGeometry(new Vec3(0.5, 0.875, 0.625), new Vec3(0.5, 0.875, 0.375), new Vec3(0.0, -1.0, 0.0));
            case Direction.NORTH -> new HornGeometry(new Vec3(0.5, 0.375, 0.875), new Vec3(0.5, 0.625, 0.875), new Vec3(0.0, 0.0, -1.0));
            case Direction.SOUTH -> new HornGeometry(new Vec3(0.5, 0.375, 0.125), new Vec3(0.5, 0.625, 0.125), new Vec3(0.0, 0.0, 1.0));
            case Direction.EAST -> new HornGeometry(new Vec3(0.125, 0.375, 0.5), new Vec3(0.125, 0.625, 0.5), new Vec3(1.0, 0.0, 0.0));
            case Direction.WEST -> new HornGeometry(new Vec3(0.875, 0.375, 0.5), new Vec3(0.875, 0.625, 0.5), new Vec3(-1.0, 0.0, 0.0));
            default -> new HornGeometry(new Vec3(0.5, 0.125, 0.375), new Vec3(0.5, 0.125, 0.625), new Vec3(0.0, 1.0, 0.0));
        };
    }

    private record HornGeometry(Vec3 post1Offset, Vec3 post2Offset, Vec3 axis) {
    }
}

