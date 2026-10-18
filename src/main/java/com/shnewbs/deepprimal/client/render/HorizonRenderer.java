package com.shnewbs.deepprimal.client.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.shnewbs.deepprimal.client.ClientPerformanceConfig;
import com.shnewbs.deepprimal.core.FarTerrain;
import com.shnewbs.deepprimal.render.RendererOwnership;
import com.shnewbs.deepprimal.render.network.HorizonRequest;
import com.shnewbs.deepprimal.render.network.HorizonTile;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.minecraft.util.context.ContextKey;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

/** Authored horizon renderer. Bounded meshes use the platform GPU API, with no external renderer dependency. */
public final class HorizonRenderer {
    public static final HorizonRenderer INSTANCE = new HorizonRenderer();
    private static final ContextKey<List<Tile>> VISIBLE = new ContextKey<>(Identifier.fromNamespaceAndPath("deepprimal", "horizon_visible"));
    private static final RenderPipeline PIPELINE = RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
        .withLocation(Identifier.fromNamespaceAndPath("deepprimal", "horizon"))
        .withVertexShader(Identifier.fromNamespaceAndPath("deepprimal", "core/horizon"))
        .withFragmentShader(Identifier.fromNamespaceAndPath("deepprimal", "core/horizon"))
        .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLES)
        .withDepthStencilState(DepthStencilState.DEFAULT).withCull(true).build();
    private final LinkedHashMap<Long, Tile> cache = new LinkedHashMap<>(400, 0.75f, true);
    private ClientLevel level;
    private long session, pending, pendingAt;
    private boolean waiting, unsupported;
    private int ticks, centerX = Integer.MIN_VALUE, centerZ, range;
    private List<Long> wanted = List.of();
    private HorizonTile upload;
    public volatile int cachedTiles, visibleTiles, targetTiles;
    public volatile long drawnTriangles, uploadedBytes;
    public volatile String status = "Waiting for world";

    public static int farDistance() {
        return RendererOwnership.owned() ? ClientPerformanceConfig.FAR_RENDER_DISTANCE.get() : 0;
    }
    public static boolean active() { return farDistance() > 0 && INSTANCE.level != null && !INSTANCE.unsupported; }
    @SubscribeEvent public void tick(ClientTickEvent.Post event) {
        var mc = Minecraft.getInstance();
        if (level != mc.level) { clear(); level = mc.level; session++; }
        if (!RendererOwnership.owned()) { status = "Delegated to " + RendererOwnership.owner(); return; }
        if (level == null || mc.player == null || mc.getConnection() == null) return;
        if (farDistance() == 0) { if (!cache.isEmpty()) clearMeshes(); status = "Disabled"; return; }
        if (unsupported) return;
        if (!mc.getConnection().hasChannel(HorizonRequest.TYPE)) { status = "Server has no horizon channel"; return; }
        ticks++;
        int cx = Math.floorDiv(mc.player.getBlockX(), FarTerrain.TILE_BLOCKS), cz = Math.floorDiv(mc.player.getBlockZ(), FarTerrain.TILE_BLOCKS);
        if (cx != centerX || cz != centerZ || range != farDistance()) {
            centerX = cx; centerZ = cz; range = farDistance();
            int radius = (range * 16 + FarTerrain.TILE_BLOCKS - 1) / FarTerrain.TILE_BLOCKS + 1;
            var next = new ArrayList<Long>();
            for (int x = cx - radius; x <= cx + radius; x++) for (int z = cz - radius; z <= cz + radius; z++)
                if (FarTerrain.inRange(x, z, mc.player.getX(), mc.player.getZ(), range)) next.add(FarTerrain.key(x, z));
            next.sort(Comparator.comparingDouble(k -> Math.pow(FarTerrain.x(k) + 0.5 - cx, 2) + Math.pow(FarTerrain.z(k) + 0.5 - cz, 2)));
            wanted = List.copyOf(next); targetTiles = wanted.size();
            var keep = new java.util.HashSet<>(wanted);
            cache.entrySet().removeIf(e -> { if (keep.contains(e.getKey())) return false; e.getValue().close(); return true; });
            cachedTiles = cache.size();
        }
        if (waiting && ticks - pendingAt > 100) waiting = false;
        if (waiting || upload != null || ticks % 4 != 0) return;
        for (long key : wanted) if (!cache.containsKey(key)) {
            pending = key; pendingAt = ticks; waiting = true;
            ClientPacketDistributor.sendToServer(new HorizonRequest(session, level.dimension().identifier(), FarTerrain.x(key), FarTerrain.z(key)));
            status = "Sampling horizon"; return;
        }
        status = "Ready";
    }
    public void accept(HorizonTile tile) {
        if (!RendererOwnership.owned() || level == null || tile.session() != session || !tile.dimension().equals(level.dimension().identifier())
            || !waiting || FarTerrain.key(tile.x(), tile.z()) != pending) return;
        waiting = false;
        if (!tile.supported()) { unsupported = true; clearMeshes(); status = "Horizon available in Deep Primal terrain only"; return; }
        upload = tile;
    }
    @SubscribeEvent public void extract(ExtractLevelRenderStateEvent event) {
        if (!active()) return;
        if (upload != null) {
            HorizonTile ready = upload; upload = null;
            Tile tile = build(ready); Tile old = cache.put(FarTerrain.key(tile.x, tile.z), tile); if (old != null) old.close();
            while (cache.size() > FarTerrain.MAX_TILES) { var i = cache.entrySet().iterator(); i.next().getValue().close(); i.remove(); }
            cachedTiles = cache.size();
        }
        var camera = event.getCamera().position();
        int vertical = ClientPerformanceConfig.VERTICAL_RENDER_DISTANCE.get();
        var visible = new ArrayList<Tile>();
        for (Tile tile : cache.values()) {
            if (!FarTerrain.inRange(tile.x, tile.z, camera.x, camera.z, farDistance())) continue;
            if (vertical != 0 && (tile.minY > camera.y + vertical * 16 || tile.maxY < camera.y - vertical * 16)) continue;
            if (event.getFrustum().isVisible(tile.bounds)) visible.add(tile);
        }
        visibleTiles = visible.size(); event.getRenderState().setRenderData(VISIBLE, List.copyOf(visible));
    }
    @SubscribeEvent public void render(RenderLevelStageEvent.AfterOpaqueBlocks event) {
        if (!active()) return;
        var tiles = event.getLevelRenderState().getRenderData(VISIBLE);
        if (tiles == null || tiles.isEmpty()) return;
        var mc = Minecraft.getInstance(); var target = mc.getMainRenderTarget(); var camera = event.getLevelRenderState().cameraRenderState.pos;
        var infos = new net.minecraft.client.renderer.DynamicUniforms.Transform[tiles.size()];
        for (int i = 0; i < tiles.size(); i++) {
            var tile = tiles.get(i);
            infos[i] = new net.minecraft.client.renderer.DynamicUniforms.Transform(new Matrix4f(event.getModelViewMatrix()),
                new Vector4f(1, 1, 1, 1), new Vector3f((float)(tile.x * 512.0 - camera.x), (float)-camera.y, (float)(tile.z * 512.0 - camera.z)), new Matrix4f());
        }
        var transforms = RenderSystem.getDynamicUniforms().writeTransforms(infos);
        try (var pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Deep Primal horizon",
                target.getColorTextureView(), OptionalInt.empty(), target.getDepthTextureView(), OptionalDouble.empty())) {
            pass.setPipeline(PIPELINE); RenderSystem.bindDefaultUniforms(pass);
            for (int i = 0; i < tiles.size(); i++) {
                Tile tile = tiles.get(i);
                pass.setUniform("DynamicTransforms", transforms[i]); pass.setVertexBuffer(0, tile.vertices); pass.draw(0, tile.vertexCount);
                drawnTriangles += tile.vertexCount / 3;
            }
        }
    }
    private Tile build(HorizonTile p) {
        int low = 32767, high = -32768;
        try (var bytes = new ByteBufferBuilder(8192)) {
            var builder = new BufferBuilder(bytes, VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
            for (int z = 0; z < 8; z++) for (int x = 0; x < 8; x++) {
                int a = z * 9 + x, b = a + 9, c = a + 1, d = b + 1;
                for (int i : new int[]{a, b, c, c, b, d}) {
                    int h = p.heights()[i]; low = Math.min(low, h); high = Math.max(high, h);
                    builder.addVertex(i % 9 * 64, h, i / 9 * 64).setColor(p.colors()[i]);
                }
            }
            try (var mesh = builder.buildOrThrow()) {
                var buffer = RenderSystem.getDevice().createBuffer(() -> "Deep Primal horizon tile", GpuBuffer.USAGE_VERTEX, mesh.vertexBuffer());
                uploadedBytes += mesh.vertexBuffer().remaining();
                return new Tile(p.x(), p.z(), low, high, buffer, mesh.drawState().vertexCount());
            }
        }
    }
    private void clearMeshes() { cache.values().forEach(Tile::close); cache.clear(); cachedTiles = visibleTiles = 0; upload = null; }
    private void clear() { clearMeshes(); wanted = List.of(); targetTiles = 0; waiting = unsupported = false; centerX = Integer.MIN_VALUE; ticks = 0; drawnTriangles = uploadedBytes = 0; status = "Waiting for world"; }
    private static final class Tile implements AutoCloseable {
        final int x, z, minY, maxY, vertexCount; final GpuBuffer vertices; final AABB bounds;
        Tile(int x, int z, int min, int max, GpuBuffer buffer, int count) {
            this.x = x; this.z = z; minY = min; maxY = max; vertices = buffer; vertexCount = count;
            bounds = new AABB(x * 512.0, min - 1, z * 512.0, (x + 1.0) * 512, max + 1, (z + 1.0) * 512);
        }
        @Override public void close() { vertices.close(); }
    }
}
