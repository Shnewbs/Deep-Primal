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
    private static final ContextKey<List<HorizonGpuMesh>> VISIBLE = new ContextKey<>(Identifier.fromNamespaceAndPath("deepprimal", "horizon_visible"));
    private static final RenderPipeline PIPELINE = RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
        .withLocation(Identifier.fromNamespaceAndPath("deepprimal", "horizon"))
        .withVertexShader(Identifier.fromNamespaceAndPath("deepprimal", "core/horizon"))
        .withFragmentShader(Identifier.fromNamespaceAndPath("deepprimal", "core/horizon"))
        .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLES)
        .withDepthStencilState(DepthStencilState.DEFAULT).withCull(true).build();
    private final LinkedHashMap<Long, HorizonGpuMesh> cache = new LinkedHashMap<>(400, 0.75f, true);
    private final com.shnewbs.deepprimal.core.WorkBudget budget = new com.shnewbs.deepprimal.core.WorkBudget();
    private long previousFrame;
    private com.shnewbs.deepprimal.core.HorizonView view;
    private final net.minecraft.client.renderer.DynamicUniforms.Transform[] transformStorage = new net.minecraft.client.renderer.DynamicUniforms.Transform[FarTerrain.MAX_TILES + 1];
    private net.minecraft.client.renderer.DynamicUniforms.Transform[] transformPool = new net.minecraft.client.renderer.DynamicUniforms.Transform[0];
    private final Matrix4f modelView = new Matrix4f(), textureIdentity = new Matrix4f();
    private final Vector4f white = new Vector4f(1, 1, 1, 1);
    private final ArrayList<com.mojang.blaze3d.systems.RenderPass.Draw<Void>> indexedDraws = new ArrayList<>();
    private final HorizonGpuTimer gpuTimer = new HorizonGpuTimer();
    private HorizonGpuMesh transition;
    private final com.shnewbs.deepprimal.core.TransitionDependencies transitionSources = new com.shnewbs.deepprimal.core.TransitionDependencies(FarTerrain.MAX_TILES);
    private final ArrayList<HorizonGpuMesh> visibleWork = new ArrayList<>(FarTerrain.MAX_TILES + 1);
    public volatile long transitionBuilds;
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
            budget.observe((now - previousFrame) / 1e6, 1000.0 / Math.min(ClientPerformanceConfig.TARGET_FPS.get(), Minecraft.getInstance().getFramerateLimitTracker().getFramerateLimit()),
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
    private PendingUpload completedMesh;
    public volatile int uploadsLastFrame, maxUploadsPerFrame;
    public volatile long uploadBytesLastFrame, maxUploadBytesPerFrame;
    public static boolean backgroundWorkPaused() { return backgroundPaused(); }
    private static final class PendingUpload {
        final HorizonTile packet;
        final com.shnewbs.deepprimal.core.HorizonMesh[] levels;
        int next;
        HorizonGpuMesh root, previous;
        PendingUpload(HorizonTile packet, com.shnewbs.deepprimal.core.HorizonMesh[] levels) { this.packet = packet; this.levels = levels; }
        void close() { if (root != null) root.close(); }
    }
    private static boolean backgroundPaused() {
        return ClientPerformanceConfig.PAUSE_BACKGROUND.get() && !Minecraft.getInstance().isWindowActive();
    }
    private java.util.concurrent.ThreadPoolExecutor worker;
    private java.util.concurrent.Future<com.shnewbs.deepprimal.core.HorizonMesh[]> meshFuture;
    public volatile int cachedTiles, visibleTiles, targetTiles, visibleFine, visibleMedium, visibleCoarse;
    public volatile long drawnTriangles, uploadedBytes;
    public volatile String status = "Waiting for world";

    public static int farDistance() {
        return RendererOwnership.owned() ? ClientPerformanceConfig.FAR_RENDER_DISTANCE.get() : 0;
    }
    public static boolean active() { return INSTANCE.level != null && !INSTANCE.unsupported && farDistance() > Minecraft.getInstance().options.getEffectiveRenderDistance(); }
    @SubscribeEvent public void tick(ClientTickEvent.Post event) {
        var mc = Minecraft.getInstance();
        if (level != mc.level) { clear(); level = mc.level; session++; }
        if (!RendererOwnership.owned()) { status = "Delegated to " + RendererOwnership.owner(); return; }
        if (level == null || mc.player == null || mc.getConnection() == null) return;
        if (farDistance() == 0 || farDistance() <= mc.options.getEffectiveRenderDistance()) {
            if (range != 0 || meshFuture != null || upload != null || worker != null) {
                clearMeshes(); waiting = false; session++; range = 0; centerX = Integer.MIN_VALUE;
                wanted = List.of(); wantedSet = java.util.Set.of();
            }
            status = farDistance() == 0 ? "Disabled" : "Horizon within full-detail range"; return;
        }
        if (unsupported) return;
        if (!mc.getConnection().hasChannel(HorizonRequest.TYPE)) { status = "Server has no horizon channel"; return; }
        ticks++;
        updateView(mc.player.getX(), mc.player.getZ());
        int cx = Math.floorDiv(mc.player.getBlockX(), FarTerrain.TILE_BLOCKS), cz = Math.floorDiv(mc.player.getBlockZ(), FarTerrain.TILE_BLOCKS);
        if (cx != centerX || cz != centerZ || range != farDistance() || ticks % 20 == 0) {
            centerX = cx; centerZ = cz; range = farDistance();
            int radius = (range * 16 + FarTerrain.TILE_BLOCKS - 1) / FarTerrain.TILE_BLOCKS + 1;
            var next = new ArrayList<Long>();
            for (int x = cx - radius; x <= cx + radius; x++) for (int z = cz - radius; z <= cz + radius; z++)
                if (FarTerrain.inRange(x, z, mc.player.getX(), mc.player.getZ(), range)) next.add(FarTerrain.key(x, z));
            double px = mc.player.getX() / 512, pz = mc.player.getZ() / 512;
            next.sort(Comparator.comparingDouble(k -> {
                double dx = FarTerrain.x(k) + .5 - px, dz = FarTerrain.z(k) + .5 - pz;
                return dx * dx + dz * dz;
            }));
            wanted = List.copyOf(next); targetTiles = wanted.size();
            var keep = new java.util.HashSet<>(wanted); wantedSet = keep;
            cache.entrySet().removeIf(e -> { if (keep.contains(e.getKey())) return false; residentBytes -= e.getValue().byteSize; e.getValue().close(); return true; });
            cachedTiles = cache.size();
        }
        if (waiting && ticks - pendingAt > 100) waiting = false;
        if (waiting || upload != null || meshFuture != null || completedMesh != null || backgroundPaused() || ticks % (ClientPerformanceConfig.ADAPTIVE_WORK.get() ? (int)Math.ceil(4 / budget.fraction()) : 4) != 0) return;
        boolean limited = false;
        long memoryLimit = ClientPerformanceConfig.HORIZON_MEMORY_MIB.get() * 1048576L;
        for (long key : wanted) {
            int detail = detail(key); HorizonGpuMesh existing = cache.get(key);
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
        if (upload != null && meshFuture == null && completedMesh == null && !backgroundPaused()) {
            if (worker == null) worker = new java.util.concurrent.ThreadPoolExecutor(1,1,15,java.util.concurrent.TimeUnit.SECONDS,
                new java.util.concurrent.ArrayBlockingQueue<>(1),r->{ var t=new Thread(r,"Deep Primal horizon mesh"); t.setDaemon(true); t.setPriority(Thread.MIN_PRIORITY); return t; });
            building=upload; upload=null; HorizonTile ready=building; int floor=level.getMinY();
            meshFuture=worker.submit(()->{
                long start=System.nanoTime();
                var meshes=new com.shnewbs.deepprimal.core.HorizonMesh[3-ready.detail()];
                var samples = new com.shnewbs.deepprimal.core.HorizonSamples(ready.detail(), ready.heights(), ready.colors());
                for (int detail = ready.detail(); detail <= 2; detail++) {
                    var grid = samples.forDetail(detail);
                    meshes[detail - ready.detail()] = com.shnewbs.deepprimal.core.HorizonMesh.build(grid.heights(), grid.colors(), detail, floor);
                }
                meshBuildNanos+=System.nanoTime()-start; return meshes;
            });
        }
        if (meshFuture != null && meshFuture.isDone()) {
            try {
                var data=meshFuture.get();
                if (wantedSet.contains(FarTerrain.key(building.x(), building.z()))) completedMesh = new PendingUpload(building, data);
            } catch (java.util.concurrent.ExecutionException e) { throw new IllegalStateException("Horizon mesh worker failed",e.getCause()); }
              catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            finally { meshFuture=null; building=null; }
            trim(); cachedTiles=cache.size();
        }
        processUploads();
        trim();
        var camera = event.getCamera().position();
        updateView(camera.x, camera.z);
        long snappedX = (long)nearCenter(camera.x), snappedZ = (long)nearCenter(camera.z);
        int near = (int)nearRadius();
        if (transitionNeedsRebuild(snappedX, snappedZ, near)) {
            if (transition != null) transition.close();
            transition = buildTransition(snappedX, snappedZ, near);
            transitionBuilds++;
        }
        int vertical = ClientPerformanceConfig.VERTICAL_RENDER_DISTANCE.get();
        var visible = visibleWork; visible.clear(); visibleFine=visibleMedium=visibleCoarse=0;
        for (HorizonGpuMesh tile : cache.values()) {
            if (!FarTerrain.inRange(tile.x, tile.z, camera.x, camera.z, farDistance())) continue;
            if (vertical != 0 && (tile.minY > camera.y + vertical * 16 || tile.maxY < camera.y - vertical * 16)) continue;
            if (event.getFrustum().isVisible(tile.bounds)) {
                HorizonGpuMesh selected=tile.forDetail(view.detail(tile.x, tile.z)); visible.add(selected);
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
        if (transformPool.length != tiles.size()) {
            transformPool = new net.minecraft.client.renderer.DynamicUniforms.Transform[tiles.size()];
            for (int i = 0; i < transformPool.length; i++) {
                if (transformStorage[i] == null) transformStorage[i] = new net.minecraft.client.renderer.DynamicUniforms.Transform(modelView, white, new Vector3f(), textureIdentity);
                transformPool[i] = transformStorage[i];
            }
        }
        modelView.set(event.getModelViewMatrix());
        for (int i = 0; i < tiles.size(); i++) {
            var tile = tiles.get(i);
            ((Vector3f)transformPool[i].modelOffset()).set((float)(tile.x * 512.0 - camera.x), (float)-camera.y, (float)(tile.z * 512.0 - camera.z));
        }
        var transforms = RenderSystem.getDynamicUniforms().writeTransforms(transformPool);
        gpuTimer.begin();
        try (var pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Deep Primal horizon",
                target.getColorTextureView(), OptionalInt.empty(), target.getDepthTextureView(), OptionalDouble.empty())) {
            pass.setPipeline(PIPELINE); RenderSystem.bindDefaultUniforms(pass);
            if (ClientPerformanceConfig.BATCHED_DRAWS.get()) {
                indexedDraws.clear();
                for (int i = 0; i < tiles.size(); i++) {
                    HorizonGpuMesh tile = tiles.get(i);
                    if (tile.indexedDraw != null) {
                        tile.transformSlice = transforms[i]; indexedDraws.add(tile.indexedDraw);
                        drawnTriangles += tile.triangles();
                    }
                }
                if (!indexedDraws.isEmpty()) pass.drawMultipleIndexed(indexedDraws, null, VertexFormat.IndexType.SHORT, java.util.Set.of("DynamicTransforms"), null);
            }
            for (int i = 0; i < tiles.size(); i++) {
                HorizonGpuMesh tile = tiles.get(i);
                if (ClientPerformanceConfig.BATCHED_DRAWS.get() && tile.indices != null) continue;
                pass.setUniform("DynamicTransforms", transforms[i]); tile.draw(pass);
                drawnTriangles += tile.triangles();
            }
        } finally { gpuTimer.end(); drawCpuNanos += System.nanoTime() - drawStart; }
    }
    private void updateView(double x, double z) {
        var mc = Minecraft.getInstance();
        view = com.shnewbs.deepprimal.core.HorizonView.create(x, z, mc.getWindow().getHeight(),
            mc.options.fov().get(), workFraction());
    }
    private int detail(long key) { return view.detail(FarTerrain.x(key), FarTerrain.z(key)); }
    private static long estimatedBytes(int detail) {
        long bytes=0;
        for (int d=detail;d<=2;d++) { bytes+=com.shnewbs.deepprimal.core.HorizonMesh.gpuBytes(d); }
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
            HorizonGpuMesh tile=cache.remove(victim); residentBytes-=tile.byteSize; tile.close();
        }
        cachedTiles=cache.size();
    }
    private void processUploads() {
        uploadsLastFrame = 0; uploadBytesLastFrame = 0;
        if (completedMesh == null || backgroundPaused()) return;
        var pending = completedMesh;
        if (!wantedSet.contains(FarTerrain.key(pending.packet.x(), pending.packet.z()))) {
            pending.close(); completedMesh = null; return;
        }
        long start = System.nanoTime(), bytes = 0;
        long timeLimit = (long)(ClientPerformanceConfig.UPLOAD_MICROS.get() * 1000L * workFraction());
        long byteLimit = ClientPerformanceConfig.UPLOAD_KIB.get() * 1024L;
        while (pending.next < pending.levels.length && uploadsLastFrame < ClientPerformanceConfig.UPLOAD_LEVELS.get()) {
            var levelData = pending.levels[pending.next];
            if (bytes + levelData.gpuBytes() > byteLimit || uploadsLastFrame > 0 && System.nanoTime() - start >= timeLimit) break;
            long before = System.nanoTime();
            HorizonGpuMesh tile;
            try { tile = HorizonGpuMesh.upload(pending.packet.x(), pending.packet.z(), pending.packet.detail() + pending.next, levelData); }
            catch (RuntimeException error) { pending.close(); completedMesh = null; throw error; }
            long elapsed = System.nanoTime() - before;
            uploadNanos += elapsed; meshPackNanos += elapsed;
            bytes += tile.byteSize; uploadedBytes += tile.byteSize; uploadsLastFrame++; pending.next++;
            if (pending.root == null) pending.root = tile;
            else { pending.previous.coarse = tile; pending.root.byteSize += tile.byteSize; }
            pending.previous = tile;
        }
        uploadBytesLastFrame = bytes;
        maxUploadsPerFrame = Math.max(maxUploadsPerFrame, uploadsLastFrame);
        maxUploadBytesPerFrame = Math.max(maxUploadBytesPerFrame, bytes);
        if (pending.next == pending.levels.length) {
            HorizonGpuMesh tile = pending.root;
            HorizonGpuMesh old = cache.put(FarTerrain.key(tile.x, tile.z), tile);
            if (old != null) { residentBytes -= old.byteSize; old.close(); }
            residentBytes += tile.byteSize; completedMesh = null;
        }
    }
    /** Both sides are deliberate: the camera can be above or below the terrain shell. */
    private static void wall(BufferBuilder b, float x, float z, float nx, float nz, float y, float ny, float floor, int color) {
        b.addVertex(x,y,z).setColor(color); b.addVertex(nx,ny,nz).setColor(color); b.addVertex(x,floor,z).setColor(color);
        b.addVertex(x,floor,z).setColor(color); b.addVertex(nx,ny,nz).setColor(color); b.addVertex(nx,floor,nz).setColor(color);
        b.addVertex(x,floor,z).setColor(color); b.addVertex(nx,ny,nz).setColor(color); b.addVertex(x,y,z).setColor(color);
        b.addVertex(nx,floor,nz).setColor(color); b.addVertex(nx,ny,nz).setColor(color); b.addVertex(x,floor,z).setColor(color);
    }
    private HorizonGpuMesh upload(BufferBuilder builder, int x, int z, int low, int high, int detail, short[] heights) {
        try (var mesh = builder.buildOrThrow()) {
            long start = System.nanoTime();
            var buffer = RenderSystem.getDevice().createBuffer(() -> "Deep Primal horizon tile", GpuBuffer.USAGE_VERTEX, mesh.vertexBuffer());
            uploadedBytes += mesh.vertexBuffer().remaining(); uploadNanos += System.nanoTime()-start;
            return new HorizonGpuMesh(x,z,low,high,buffer,null,mesh.drawState().vertexCount(),0,detail,heights);
        }
    }
    private double height(double x, double z) {
        int tx=(int)Math.floor(x/512), tz=(int)Math.floor(z/512); HorizonGpuMesh tile=cache.get(FarTerrain.key(tx,tz));
        if (tile==null) return Double.NaN;
        tile=tile.forDetail(detail(FarTerrain.key(tx,tz)));
        return HorizonGeometry.sample(tile.heights,HorizonGeometry.cells(tile.detail),x-tx*512.0,z-tz*512.0);
    }
    private HorizonGpuMesh buildTransition(long cx, long cz, int radius) {
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
            HorizonGpuMesh tile=upload(b,tx,tz,floor,level.getMaxY(),2,new short[0]);
            tile.bounds=new AABB(cx-radius,floor,cz-radius,cx+radius,level.getMaxY(),cz+radius);
            return tile;
        }
    }
    private boolean transitionNeedsRebuild(long cx, long cz, int radius) {
        transitionSources.begin(cx, cz, radius);
        int minX = (int)Math.floorDiv(cx - radius, FarTerrain.TILE_BLOCKS), maxX = (int)Math.floorDiv(cx + radius, FarTerrain.TILE_BLOCKS);
        int minZ = (int)Math.floorDiv(cz - radius, FarTerrain.TILE_BLOCKS), maxZ = (int)Math.floorDiv(cz + radius, FarTerrain.TILE_BLOCKS);
        for (int x = minX; x <= maxX; x++) for (int z = minZ; z <= maxZ; z++) {
            if (x != minX && x != maxX && z != minZ && z != maxZ) continue;
            HorizonGpuMesh root = cache.get(FarTerrain.key(x, z));
            transitionSources.include(root == null ? null : root.forDetail(view.detail(x, z)));
        }
        return transitionSources.end();
    }
    private void clearMeshes() {
        if (completedMesh != null) { completedMesh.close(); completedMesh = null; }
        if (meshFuture!=null) { meshFuture.cancel(true); meshFuture=null; building=null; }
        if (worker!=null) { worker.shutdownNow(); worker=null; }
        indexedDraws.clear();
        transitionSources.clear(); visibleWork.clear(); if (transition != null) { transition.close(); transition = null; } cache.values().forEach(HorizonGpuMesh::close); cache.clear(); residentBytes = 0; cachedTiles = visibleTiles = 0; upload = null; }
    private void clear() { budget.reset(); previousFrame = 0; gpuTimer.close(); clearMeshes(); wanted = List.of(); targetTiles = 0; waiting = unsupported = false; centerX = Integer.MIN_VALUE; ticks = 0; drawnTriangles = uploadedBytes = transitionBuilds = 0; status = "Waiting for world"; }
}
