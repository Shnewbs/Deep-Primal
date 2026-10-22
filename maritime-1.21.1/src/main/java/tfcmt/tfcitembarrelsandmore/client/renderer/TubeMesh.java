package tfcmt.tfcitembarrelsandmore.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class TubeMesh {
    private TubeMesh() {
    }

    public static void render(List<Vec3> waypoints, double meshSegmentLength, double textureRepeatLength, double halfWidth, RenderType renderType, PoseStack ps, MultiBufferSource buffers, int packedLight) {
        if (waypoints.size() < 2) {
            return;
        }
        ArrayList<Vec3> centreline = new ArrayList<Vec3>();
        ArrayList<Vec3> rightAxis = new ArrayList<Vec3>();
        ArrayList<Vec3> upAxis = new ArrayList<Vec3>();
        ArrayList<Float> vCoord = new ArrayList<Float>();
        double arcLength = 0.0;
        for (int seg = 0; seg < waypoints.size() - 1; ++seg) {
            int startI;
            Vec3 a = waypoints.get(seg);
            Vec3 b = waypoints.get(seg + 1);
            Vec3 delta = b.subtract(a);
            double len = delta.length();
            if (len < 1.0E-5) continue;
            int subdivisions = Math.max(1, (int)Math.round(len / meshSegmentLength));
            Vec3 tangent = delta.scale(1.0 / len);
            Vec3 right = TubeMesh.perpendicular(delta);
            Vec3 up = tangent.cross(right).normalize();
            for (int i = startI = centreline.isEmpty() ? 0 : 1; i <= subdivisions; ++i) {
                double t = (double)i / (double)subdivisions;
                Vec3 point = a.add(delta.scale(t));
                if (!centreline.isEmpty()) {
                    arcLength += point.distanceTo((Vec3)centreline.get(centreline.size() - 1));
                }
                centreline.add(point);
                rightAxis.add(right);
                upAxis.add(up);
                vCoord.add(Float.valueOf((float)(arcLength / textureRepeatLength)));
            }
        }
        int n = centreline.size();
        if (n < 2) {
            return;
        }
        Vec3[] corner0 = new Vec3[n];
        Vec3[] corner1 = new Vec3[n];
        Vec3[] corner2 = new Vec3[n];
        Vec3[] corner3 = new Vec3[n];
        float[] v = new float[n];
        for (int i = 0; i < n; ++i) {
            Vec3 c = (Vec3)centreline.get(i);
            Vec3 r = ((Vec3)rightAxis.get(i)).scale(halfWidth);
            Vec3 u = ((Vec3)upAxis.get(i)).scale(halfWidth);
            corner0[i] = c.add(r).add(u);
            corner1[i] = c.subtract(r).add(u);
            corner2[i] = c.subtract(r).subtract(u);
            corner3[i] = c.add(r).subtract(u);
            v[i] = ((Float)vCoord.get(i)).floatValue();
        }
        VertexConsumer buffer = buffers.getBuffer(renderType);
        Matrix4f pose = ps.last().pose();
        TubeMesh.renderRibbonFace(buffer, pose, corner0, corner1, v, upAxis, n, packedLight);
        TubeMesh.renderRibbonFace(buffer, pose, corner1, corner2, v, TubeMesh.negate(rightAxis), n, packedLight);
        TubeMesh.renderRibbonFace(buffer, pose, corner2, corner3, v, TubeMesh.negate(upAxis), n, packedLight);
        TubeMesh.renderRibbonFace(buffer, pose, corner3, corner0, v, rightAxis, n, packedLight);
    }

    private static List<Vec3> negate(List<Vec3> vectors) {
        ArrayList<Vec3> out = new ArrayList<Vec3>(vectors.size());
        for (Vec3 vec : vectors) {
            out.add(vec.scale(-1.0));
        }
        return out;
    }

    private static void renderRibbonFace(VertexConsumer buffer, Matrix4f pose, Vec3[] left, Vec3[] right, float[] v, List<Vec3> normals, int n, int packedLight) {
        for (int i = 0; i < n - 1; ++i) {
            Vec3 normal = normals.get(i);
            TubeMesh.emitVertex(buffer, pose, left[i], 0.0f, v[i], normal, packedLight);
            TubeMesh.emitVertex(buffer, pose, left[i + 1], 0.0f, v[i + 1], normal, packedLight);
            TubeMesh.emitVertex(buffer, pose, right[i + 1], 1.0f, v[i + 1], normal, packedLight);
            TubeMesh.emitVertex(buffer, pose, right[i], 1.0f, v[i], normal, packedLight);
        }
    }

    private static Vec3 perpendicular(Vec3 delta) {
        double horizLen = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        if (horizLen > 1.0E-4) {
            return new Vec3(-delta.z, 0.0, delta.x).scale(1.0 / horizLen);
        }
        return new Vec3(1.0, 0.0, 0.0);
    }

    private static void emitVertex(VertexConsumer buffer, Matrix4f pose, Vec3 pos, float u, float v, Vec3 normal, int packedLight) {
        buffer.addVertex(pose, (float)pos.x, (float)pos.y, (float)pos.z).setColor(255, 255, 255, 255).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal((float)normal.x, (float)normal.y, (float)normal.z);
    }
}

