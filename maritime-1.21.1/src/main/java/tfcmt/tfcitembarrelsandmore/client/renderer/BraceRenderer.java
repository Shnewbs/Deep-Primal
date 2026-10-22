package tfcmt.tfcitembarrelsandmore.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import tfcmt.tfcitembarrelsandmore.common.BraceData;
import tfcmt.tfcitembarrelsandmore.common.TIBAMBlocks;

public class BraceRenderer {
    private static final float[][] BRACE_BOXES = new float[][]{{14.0f, 13.0f, 1.0f, 16.0f, 16.0f, 4.0f}, {14.0f, 13.0f, 12.0f, 16.0f, 16.0f, 15.0f}, {0.0f, 13.0f, 1.0f, 2.0f, 16.0f, 4.0f}, {0.0f, 13.0f, 12.0f, 2.0f, 16.0f, 15.0f}, {2.0f, 14.0f, 1.0f, 4.0f, 15.0f, 4.0f}, {2.0f, 14.0f, 12.0f, 4.0f, 15.0f, 15.0f}, {12.0f, 14.0f, 12.0f, 14.0f, 15.0f, 15.0f}, {12.0f, 14.0f, 1.0f, 14.0f, 15.0f, 4.0f}, {2.0f, 15.0f, 1.0f, 14.0f, 16.0f, 4.0f}, {2.0f, 15.0f, 12.0f, 14.0f, 16.0f, 15.0f}, {0.0f, 0.0f, 1.0f, 2.0f, 3.0f, 4.0f}, {14.0f, 0.0f, 1.0f, 16.0f, 3.0f, 4.0f}, {0.0f, 0.0f, 12.0f, 2.0f, 3.0f, 15.0f}, {14.0f, 0.0f, 12.0f, 16.0f, 3.0f, 15.0f}, {2.0f, 0.0f, 1.0f, 14.0f, 1.0f, 4.0f}, {2.0f, 0.0f, 12.0f, 14.0f, 1.0f, 15.0f}, {2.0f, 1.0f, 1.0f, 4.0f, 2.0f, 4.0f}, {12.0f, 1.0f, 1.0f, 14.0f, 2.0f, 4.0f}, {2.0f, 1.0f, 12.0f, 4.0f, 2.0f, 15.0f}, {12.0f, 1.0f, 12.0f, 14.0f, 2.0f, 15.0f}};
    private static final Set<String> VALID_METALS = Set.of("bismuth", "bismuth_bronze", "black_bronze", "brass", "bronze", "cast_iron", "copper", "gold", "nickel", "red_steel", "rose_gold", "silver", "steel", "tin", "wrought_iron", "zinc", "blue_steel");
    private static final Map<String, ResourceLocation> METAL_TEXTURES = VALID_METALS.stream().collect(Collectors.toMap(m -> m, m -> ResourceLocation.fromNamespaceAndPath("tfc", "block/metal/block/" + m)));

    public static void render(PoseStack ps, MultiBufferSource buffers, BraceData data, Direction barrelFacing, int packedLight, int packedOverlay, float yOffset) {
        TextureAtlasSprite sprite = BraceRenderer.resolveSprite(data);
        if (sprite == null) {
            return;
        }
        ps.pushPose();
        ps.translate(0.0f, yOffset, 0.0f);
        ps.translate(0.5, 0.5, 0.5);
        float yRot = switch (barrelFacing) {
            case Direction.NORTH -> 0.0f;
            case Direction.EAST -> 270.0f;
            case Direction.SOUTH -> 180.0f;
            case Direction.WEST -> 90.0f;
            default -> 0.0f;
        };
        ps.mulPose(Axis.YP.rotationDegrees(yRot));
        ps.translate(-0.5, -0.5, -0.5);
        VertexConsumer vc = buffers.getBuffer(RenderType.cutoutMipped());
        for (float[] b : BRACE_BOXES) {
            BraceRenderer.renderBox(ps, vc, sprite, packedLight, packedOverlay, b[0] / 16.0f, b[1] / 16.0f, b[2] / 16.0f, b[3] / 16.0f, b[4] / 16.0f, b[5] / 16.0f);
        }
        ps.popPose();
    }

    private static TextureAtlasSprite resolveSprite(BraceData data) {
        ResourceLocation texLoc;
        if (data.type.equals("wood")) {
            TIBAMBlocks.Wood wood = TIBAMBlocks.Wood.byKey(data.mod, data.variant);
            if (wood == null) {
                return null;
            }
            texLoc = wood.plankTexture();
        } else {
            texLoc = METAL_TEXTURES.get(data.variant);
            if (texLoc == null) {
                return null;
            }
        }
        return (TextureAtlasSprite)Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(texLoc);
    }

    private static void renderBox(PoseStack ps, VertexConsumer vc, TextureAtlasSprite sprite, int light, int overlay, float x0, float y0, float z0, float x1, float y1, float z1) {
        Matrix4f m = ps.last().pose();
        Matrix3f n = ps.last().normal();
        BraceRenderer.quad(vc, m, n, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1, 0.0f, -1.0f, 0.0f, sprite.getU((float)((double)(x0 * 16.0f)) / 16.0f), sprite.getV((float)((double)(z0 * 16.0f)) / 16.0f), sprite.getU((float)((double)(x1 * 16.0f)) / 16.0f), sprite.getV((float)((double)(z1 * 16.0f)) / 16.0f), light, overlay);
        BraceRenderer.quad(vc, m, n, x0, y1, z1, x1, y1, z1, x1, y1, z0, x0, y1, z0, 0.0f, 1.0f, 0.0f, sprite.getU((float)((double)(x0 * 16.0f)) / 16.0f), sprite.getV((float)((double)(z1 * 16.0f)) / 16.0f), sprite.getU((float)((double)(x1 * 16.0f)) / 16.0f), sprite.getV((float)((double)(z0 * 16.0f)) / 16.0f), light, overlay);
        BraceRenderer.quad(vc, m, n, x0, y1, z0, x1, y1, z0, x1, y0, z0, x0, y0, z0, 0.0f, 0.0f, -1.0f, sprite.getU((float)((double)(x0 * 16.0f)) / 16.0f), sprite.getV((float)((double)((1.0f - y1) * 16.0f)) / 16.0f), sprite.getU((float)((double)(x1 * 16.0f)) / 16.0f), sprite.getV((float)((double)((1.0f - y0) * 16.0f)) / 16.0f), light, overlay);
        BraceRenderer.quad(vc, m, n, x1, y1, z1, x0, y1, z1, x0, y0, z1, x1, y0, z1, 0.0f, 0.0f, 1.0f, sprite.getU((float)((double)(x1 * 16.0f)) / 16.0f), sprite.getV((float)((double)((1.0f - y1) * 16.0f)) / 16.0f), sprite.getU((float)((double)(x0 * 16.0f)) / 16.0f), sprite.getV((float)((double)((1.0f - y0) * 16.0f)) / 16.0f), light, overlay);
        BraceRenderer.quad(vc, m, n, x0, y1, z1, x0, y1, z0, x0, y0, z0, x0, y0, z1, -1.0f, 0.0f, 0.0f, sprite.getU((float)((double)(z1 * 16.0f)) / 16.0f), sprite.getV((float)((double)((1.0f - y1) * 16.0f)) / 16.0f), sprite.getU((float)((double)(z0 * 16.0f)) / 16.0f), sprite.getV((float)((double)((1.0f - y0) * 16.0f)) / 16.0f), light, overlay);
        BraceRenderer.quad(vc, m, n, x1, y1, z0, x1, y1, z1, x1, y0, z1, x1, y0, z0, 1.0f, 0.0f, 0.0f, sprite.getU((float)((double)(z0 * 16.0f)) / 16.0f), sprite.getV((float)((double)((1.0f - y1) * 16.0f)) / 16.0f), sprite.getU((float)((double)(z1 * 16.0f)) / 16.0f), sprite.getV((float)((double)((1.0f - y0) * 16.0f)) / 16.0f), light, overlay);
    }

    private static void quad(VertexConsumer vc, Matrix4f m, Matrix3f n, float x0, float y0, float z0, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float nx, float ny, float nz, float u0, float v0, float u1, float v1, int light, int overlay) {
        vc.addVertex(m, x0, y0, z0).setColor(1.0f, 1.0f, 1.0f, 1.0f).setUv(u0, v1).setOverlay(overlay).setLight(light).setNormal(n, nx, ny, nz);
        vc.addVertex(m, x1, y1, z1).setColor(1.0f, 1.0f, 1.0f, 1.0f).setUv(u1, v1).setOverlay(overlay).setLight(light).setNormal(n, nx, ny, nz);
        vc.addVertex(m, x2, y2, z2).setColor(1.0f, 1.0f, 1.0f, 1.0f).setUv(u1, v0).setOverlay(overlay).setLight(light).setNormal(n, nx, ny, nz);
        vc.addVertex(m, x3, y3, z3).setColor(1.0f, 1.0f, 1.0f, 1.0f).setUv(u0, v0).setOverlay(overlay).setLight(light).setNormal(n, nx, ny, nz);
    }
}

