package tfcmt.tfcitembarrelsandmore.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix3f;
import org.joml.Vector3f;

public final class MaritimeRender {
    private MaritimeRender() {}
    public static void normal(VertexConsumer buffer, Matrix3f matrix, float x, float y, float z) {
        Vector3f normal = matrix.transform(new Vector3f(x, y, z)).normalize();
        buffer.setNormal(normal.x(), normal.y(), normal.z());
    }
}
