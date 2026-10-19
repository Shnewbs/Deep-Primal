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
import com.shnewbs.deepprimal.core.HorizonGeometry;
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
    private final com.shnewbs.deepprimal.core.WorkBudget budget = new com.shnewbs.deepprimal.core.WorkBudget();
    private long previousFrame;
    private final HorizonGpuTimer gpuTimer = new HorizonGpuTimer();
    private Tile transition;
    private long transitionX = Long.MIN_VALUE, transitionZ, meshVersion, transitionVersion;
    private int transitionRadius, transitionDetail;
    public volatile long meshBuildNanos, meshPackNanos, uploadNanos, drawCpuNanos, culledTiles, receivedBytes;
    public double gpuMs() { return gpuTimer.milliseconds; }
    public boolean gpuTimingAvailable() { return gpuTimer.available; }
    public static float nearRadius() { return Math.max(16, Minecraft.getInstance().options.getEffectiveRenderDistance() * 16 - 48); }
    public static double nearCenter(double coordinate) { return Math.floor(coordinate / 16) * 16; }
    public volatile long residentBytes;
    public double workFraction() { return ClientPerformanceConfig.ADAPTIVE_WORK.get() ? budget.fraction() : 1; }
    public void observeFrame(long now) {
        if (previousFrame != 0 && level != null) {
            var runtime = Runtime.getRuntime();
            budget.observe((now - previousFrame) / 1e6, 1000.0 / Math.min(ClientPerformanceConfig.TARGET_FPS.get(), Minecraft.getInstance().options.framerateLimit().get()),
                (double)(runtime.totalMemory() - runtime.freeMemory()) / runtime.maxMemory());
        }
        previousFrame = now;
    }
    private ClientLevel level;
    private long session, pending, pendingAt;
    private int pendingDetail;
    private boolean waiting, unsupported;
    private int ticks, centerX = Integer.MIN_VALUE, centerZ, range;
    private List<Long> wanted = List.of();
    private java.util.Set<Long> wantedSet = java.util.Set.of();
    private HorizonTile upload, building;
    private java.util.concurrent.ThreadPoolExecutor worker;
    private java.util.concurrent.Future<com.shnewbs.deepprimal.core.HorizonMesh[]> meshFuture;
    public volatile int cachedTiles, visibleTiles, targetTiles, visibleFine, visibleMedium, visibleCoarse;
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
        if (cx != centerX || cz != centerZ || range != farDistance() || ticks % 20 == 0) {
            centerX = cx; centerZ = cz; range = farDistance();
            int radius = (range * 16 + FarTerrain.TILE_BLOCKS - 1) / FarTerrain.TILE_BLOCKS + 1;
            var next = new ArrayList<Long>();
            for (int x = cx - radius; x <= cx + radius; x++) for (int z = cz - radius; z <= cz + radius; z++)
                if (FarTerrain.inRange(x, z, mc.player.getX(), mc.player.getZ(), range)) next.add(FarTerrain.key(x, z));
            next.sort(Comparator.comparingDouble(k -> Math.pow(FarTerrain.x(k) + 0.5 - cx, 2) + Math.pow(FarTerrain.z(k) + 0.5 - cz, 2)));
            wanted = List.copyOf(next); targetTiles = wanted.size();
            var keep = new java.util.HashSet<>(wanted); wantedSet = keep;
            cache.entrySet().removeIf(e -> { if (keep.contains(e.getKey())) return false; residentBytes -= e.getValue().byteSize; e.getValue().close(); return true; });
            cachedTiles = cache.size();
        }
        if (waiting && ticks - pendingAt > 100) waiting = false;
        if (waiting || upload != null || meshFuture != null || ticks % (ClientPerformanceConfig.ADAPTIVE_WORK.get() ? (int)Math.ceil(4 / budget.fraction()) : 4) != 0) return;
        boolean limited = false;
        long memoryLimit = ClientPerformanceConfig.HORIZON_MEMORY_MIB.get() * 1048576L;
        for (long key : wanted) {
            int detail = detail(key); Tile existing = cache.get(key);
            if (existing != null && existing.detail <= detail) continue;
            long estimate = estimatedBytes(detail);
            if (residentBytes - (existing == null ? 0 : existing.byteSize) + estimate > memoryLimit) { limited = true; continue; }
            pending = key; pendingDetail = detail; pendingAt = ticks; waiting = true;
            ClientPacketDistributor.sendToServer(new HorizonRequest(session, level.dimension().identifier(), FarTerrain.x(key), FarTerrain.z(key), detail));
            status = "Sampling horizon"; return;
        }
        if (limited) { status = "Memory budget reached"; return; }
        status = "Ready";
    }
    public void accept(HorizonTile tile) {
        if (!RendererOwnership.owned() || level == null || tile.session() != session || !tile.dimension().equals(level.dimension().identifier())
            || !waiting || tile.detail() != pendingDetail || FarTerrain.key(tile.x(), tile.z()) != pending) return;
        waiting = false;
        if (!wantedSet.contains(FarTerrain.key(tile.x(), tile.z()))) return;
        if (!tile.supported()) { unsupported = true; clearMeshes(); status = "Horizon available in Deep Primal terrain only"; return; }
        receivedBytes += tile.heights().length * 6L; upload = tile;
    }
    @SubscribeEvent public void extract(ExtractLevelRenderStateEvent event) {
        if (!active()) return;
        if (upload != null && meshFuture == null) {
            if (worker == null) worker = new java.util.concurrent.ThreadPoolExecutor(1,1,15,java.util.concurrent.TimeUnit.SECONDS,
                new java.util.concurrent.ArrayBlockingQueue<>(1),r->{ var t=new Thread(r,"Deep Primal horizon mesh"); t.setDaemon(true); t.setPriority(Thread.MIN_PRIORITY); return t; });
            building=upload; upload=null; HorizonTile ready=building; int floor=level.getMinY();
            meshFuture=worker.submit(()->{
                long start=System.nanoTime();
                var meshes=new com.shnewbs.deepprimal.core.HorizonMesh[3-ready.detail()];
                int sourceCells=HorizonGeometry.cells(ready.detail());
                for (int detail=ready.detail();detail<=2;detail++) {
                    int cells=HorizonGeometry.cells(detail), ratio=sourceCells/cells; short[] heights=new short[(cells+1)*(cells+1)]; int[] colors=new int[heights.length];
                    for (int z=0;z<=cells;z++) for (int x=0;x<=cells;x++) {
                        int source=z*ratio*(sourceCells+1)+x*ratio, target=z*(cells+1)+x;
                        heights[target]=ready.heights()[source]; colors[target]=ready.colors()[source];
                    }
                    meshes[detail-ready.detail()]=com.shnewbs.deepprimal.core.HorizonMesh.build(heights,colors,detail,floor);
                }
                meshBuildNanos+=System.nanoTime()-start; return meshes;
            });
        }
        if (meshFuture != null && meshFuture.isDone()) {
            try {
                var data=meshFuture.get();
                if (wantedSet.contains(FarTerrain.key(building.x(),building.z()))) {
                    long packingStart=System.nanoTime(); Tile tile=build(building,data); meshPackNanos+=System.nanoTime()-packingStart; Tile old=cache.put(FarTerrain.key(tile.x,tile.z),tile);
                    if (old!=null) { residentBytes-=old.byteSize; old.close(); }
                    residentBytes+=tile.byteSize; meshVersion++;
                }
            } catch (java.util.concurrent.ExecutionException e) { throw new IllegalStateException("Horizon mesh worker failed",e.getCause()); }
              catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            finally { meshFuture=null; building=null; }
            trim(); cachedTiles=cache.size();
        }
        trim();
        var camera = event.getCamera().position();
        long snappedX = (long)nearCenter(camera.x), snappedZ = (long)nearCenter(camera.z);
        int near = (int)nearRadius();
        int transitionLod=(int)(workFraction()*1000)+Minecraft.getInstance().options.fov().get()+Minecraft.getInstance().getWindow().getHeight();
        if (transitionDetail != transitionLod || transitionX != snappedX || transitionZ != snappedZ || transitionRadius != near || transitionVersion != meshVersion) {
            if (transition != null) transition.close();
            transition = buildTransition(snappedX, snappedZ, near);
            transitionX = snappedX; transitionZ = snappedZ; transitionRadius = near; transitionVersion = meshVersion; transitionDetail=transitionLod;
        }
        int vertical = ClientPerformanceConfig.VERTICAL_RENDER_DISTANCE.get();
        var visible = new ArrayList<Tile>(); visibleFine=visibleMedium=visibleCoarse=0;
        for (Tile tile : cache.values()) {
            if (!FarTerrain.inRange(tile.x, tile.z, camera.x, camera.z, farDistance())) continue;
            if (vertical != 0 && (tile.minY > camera.y + vertical * 16 || tile.maxY < camera.y - vertical * 16)) continue;
            if (event.getFrustum().isVisible(tile.bounds)) {
                Tile selected=tile.forDetail(detail(FarTerrain.key(tile.x,tile.z))); visible.add(selected);
                if (selected.detail==0) visibleFine++; else if (selected.detail==1) visibleMedium++; else visibleCoarse++;
            }
        }
        culledTiles = cache.size() - visible.size();
        if (transition != null && event.getFrustum().isVisible(transition.bounds)) visible.add(transition);
        visibleTiles = visible.size(); event.getRenderState().setRenderData(VISIBLE, List.copyOf(visible));
    }
    @SubscribeEvent public void render(RenderLevelStageEvent.AfterOpaqueBlocks event) {
        if (!active()) return;
        var tiles = event.getLevelRenderState().getRenderData(VISIBLE);
        if (tiles == null || tiles.isEmpty()) return;
        var mc = Minecraft.getInstance(); var target = mc.getMainRenderTarget(); var camera = event.getLevelRenderState().cameraRenderState.pos;
        long drawStart = System.nanoTime();
        var infos = new net.minecraft.client.renderer.DynamicUniforms.Transform[tiles.size()];
        for (int i = 0; i < tiles.size(); i++) {
            var tile = tiles.get(i);
            infos[i] = new net.minecraft.client.renderer.DynamicUniforms.Transform(new Matrix4f(event.getModelViewMatrix()),
                new Vector4f(1, 1, 1, 1), new Vector3f((float)(tile.x * 512.0 - camera.x), (float)-camera.y, (float)(tile.z * 512.0 - camera.z)), new Matrix4f());
        }
        var transforms = RenderSystem.getDynamicUniforms().writeTransforms(infos);
        gpuTimer.begin();
        try (var pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Deep Primal horizon",
                target.getColorTextureView(), OptionalInt.empty(), target.getDepthTextureView(), OptionalDouble.empty())) {
            pass.setPipeline(PIPELINE); RenderSystem.bindDefaultUniforms(pass);
            for (int i = 0; i < tiles.size(); i++) {
                Tile tile = tiles.get(i);
                pass.setUniform("DynamicTransforms", transforms[i]); pass.setVertexBuffer(0, tile.vertices); pass.draw(0, tile.vertexCount);
                drawnTriangles += tile.vertexCount / 3;
            }
        } finally { gpuTimer.end(); drawCpuNanos += System.nanoTime() - drawStart; }
    }
    private int detail(long key) {
        var p = Minecraft.getInstance().player;
        double dx = Math.max(0, Math.abs((FarTerrain.x(key)+.5)*512 - p.getX())-256);
        double dz = Math.max(0, Math.abs((FarTerrain.z(key)+.5)*512 - p.getZ())-256);
        var mc=Minecraft.getInstance();
        double projection=Math.clamp(mc.getWindow().getHeight()/720.0 * Math.tan(Math.toRadians(35))/Math.tan(Math.toRadians(mc.options.fov().get()/2.0)),0.5,3);
        double workload=ClientPerformanceConfig.ADAPTIVE_WORK.get()?Math.sqrt(budget.fraction()):1;
        return HorizonGeometry.detail(Math.sqrt(dx*dx+dz*dz)/(projection*workload));
    }
    private static long estimatedBytes(int detail) {
        long bytes=0;
        for (int d=detail;d<=2;d++) { int cells=HorizonGeometry.cells(d); bytes+=(cells*cells*6L+cells*4L*12+6)*DefaultVertexFormat.POSITION_COLOR.getVertexSize(); }
        return bytes;
    }
    private void trim() {
        long limit=ClientPerformanceConfig.HORIZON_MEMORY_MIB.get()*1048576L;
        while (cache.size()>FarTerrain.MAX_TILES || residentBytes>limit) {
            long victim=cache.keySet().iterator().next(); double farthest=-1; var player=Minecraft.getInstance().player;
            if (player!=null) for (var e:cache.entrySet()) {
                double dx=(e.getValue().x+.5)*512-player.getX(),dz=(e.getValue().z+.5)*512-player.getZ(), distance=dx*dx+dz*dz;
                if (distance>farthest) { farthest=distance; victim=e.getKey(); }
            }
            Tile tile=cache.remove(victim); residentBytes-=tile.byteSize; tile.close(); meshVersion++;
        }
        cachedTiles=cache.size();
    }
    private Tile build(HorizonTile p, com.shnewbs.deepprimal.core.HorizonMesh[] levels) {
        Tile root=null, previous=null;
        try {
            for (int i=0;i<levels.length;i++) {
                var data=levels[i];
                try (var bytes=new ByteBufferBuilder(data.vertices()*DefaultVertexFormat.POSITION_COLOR.getVertexSize())) {
                    var b=new BufferBuilder(bytes,VertexFormat.Mode.TRIANGLES,DefaultVertexFormat.POSITION_COLOR);
                    float[] positions=data.positions(); int[] colors=data.colors();
                    for (int v=0;v<colors.length;v++) b.addVertex(positions[v*3],positions[v*3+1],positions[v*3+2]).setColor(colors[v]);
                    Tile tile=upload(b,p.x(),p.z(),data.minimum(),data.maximum(),p.detail()+i,data.heights());
                    if (root==null) root=tile; else { previous.coarse=tile; root.byteSize+=tile.byteSize; }
                    previous=tile;
                }
            }
            return root;
        } catch (RuntimeException e) { if (root!=null) root.close(); throw e; }
    }
    /** Both sides are deliberate: the camera can be above or below the terrain shell. */
    private static void wall(BufferBuilder b, float x, float z, float nx, float nz, float y, float ny, float floor, int color) {
        b.addVertex(x,y,z).setColor(color); b.addVertex(nx,ny,nz).setColor(color); b.addVertex(x,floor,z).setColor(color);
        b.addVertex(x,floor,z).setColor(color); b.addVertex(nx,ny,nz).setColor(color); b.addVertex(nx,floor,nz).setColor(color);
        b.addVertex(x,floor,z).setColor(color); b.addVertex(nx,ny,nz).setColor(color); b.addVertex(x,y,z).setColor(color);
        b.addVertex(nx,floor,nz).setColor(color); b.addVertex(nx,ny,nz).setColor(color); b.addVertex(x,floor,z).setColor(color);
    }
    private Tile upload(BufferBuilder builder, int x, int z, int low, int high, int detail, short[] heights) {
        try (var mesh = builder.buildOrThrow()) {
            long start = System.nanoTime();
            var buffer = RenderSystem.getDevice().createBuffer(() -> "Deep Primal horizon tile", GpuBuffer.USAGE_VERTEX, mesh.vertexBuffer());
            uploadedBytes += mesh.vertexBuffer().remaining(); uploadNanos += System.nanoTime()-start;
            return new Tile(x,z,low,high,buffer,mesh.drawState().vertexCount(),detail,heights);
        }
    }
    private double height(double x, double z) {
        int tx=(int)Math.floor(x/512), tz=(int)Math.floor(z/512); Tile tile=cache.get(FarTerrain.key(tx,tz));
        if (tile==null) return Double.NaN;
        tile=tile.forDetail(detail(FarTerrain.key(tx,tz)));
        return HorizonGeometry.sample(tile.heights,HorizonGeometry.cells(tile.detail),x-tx*512.0,z-tz*512.0);
    }
    private Tile buildTransition(long cx, long cz, int radius) {
        if (cache.isEmpty()) return null;
        int tx=(int)Math.floor(cx/512.0), tz=(int)Math.floor(cz/512.0), floor=level.getMinY();
        try (var bytes = new ByteBufferBuilder(65536)) {
            var b=new BufferBuilder(bytes,VertexFormat.Mode.TRIANGLES,DefaultVertexFormat.POSITION_COLOR); int count=0;
            for (int side=0;side<4;side++) for (int i=-radius;i<radius;i+=16) {
                double x=cx+(side<2?i:(side==2?-radius:radius));
                double z=cz+(side<2?(side==0?-radius:radius):i);
                double nx=x+(side<2?16:0), nz=z+(side<2?0:16);
                double h=height(x,z), nh=height(nx,nz);
                if (!Double.isFinite(h) || !Double.isFinite(nh)) continue;
                // Alpha is an internal marker: transition walls bypass the surface near-clip.
                wall(b,(float)(x-tx*512.0),(float)(z-tz*512.0),(float)(nx-tx*512.0),(float)(nz-tz*512.0),(float)h,(float)nh,floor,0xfc747c6b); count++;
            }
            if (count==0) return null;
            Tile tile=upload(b,tx,tz,floor,level.getMaxY(),2,new short[0]);
            tile.bounds=new AABB(cx-radius,floor,cz-radius,cx+radius,level.getMaxY(),cz+radius);
            return tile;
        }
    }
    private void clearMeshes() {
        if (meshFuture!=null) { meshFuture.cancel(true); meshFuture=null; building=null; }
        if (worker!=null) { worker.shutdownNow(); worker=null; }
        meshVersion++; if (transition != null) { transition.close(); transition = null; } transitionX = Long.MIN_VALUE; cache.values().forEach(Tile::close); cache.clear(); residentBytes = 0; cachedTiles = visibleTiles = 0; upload = null; }
    private void clear() { budget.reset(); previousFrame = 0; gpuTimer.close(); clearMeshes(); wanted = List.of(); targetTiles = 0; waiting = unsupported = false; centerX = Integer.MIN_VALUE; ticks = 0; drawnTriangles = uploadedBytes = 0; status = "Waiting for world"; }
    private static final class Tile implements AutoCloseable {
        final int x, z, minY, maxY, vertexCount; long byteSize; Tile coarse; final GpuBuffer vertices; AABB bounds; final int detail; final short[] heights;
        Tile(int x, int z, int min, int max, GpuBuffer buffer, int count, int detail, short[] heights) {
            this.detail = detail; this.heights = heights;
            this.x = x; this.z = z; byteSize = (long)count * DefaultVertexFormat.POSITION_COLOR.getVertexSize(); minY = min; maxY = max; vertices = buffer; vertexCount = count;
            bounds = new AABB(x * 512.0, min - 1, z * 512.0, (x + 1.0) * 512, max + 1, (z + 1.0) * 512);
        }
        Tile forDetail(int wanted) { return coarse!=null && wanted>detail ? coarse.forDetail(wanted) : this; }
        @Override public void close() { vertices.close(); if (coarse!=null) coarse.close(); }
    }
}
