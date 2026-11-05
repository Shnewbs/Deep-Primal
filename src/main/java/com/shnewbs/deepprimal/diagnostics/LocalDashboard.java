package com.shnewbs.deepprimal.diagnostics;

import com.google.gson.Gson;
import com.shnewbs.deepprimal.DeepPrimal;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/** Read-only loopback HTTP service. Handlers see immutable snapshots, never live Minecraft objects. */
public final class LocalDashboard {
    public static final int HISTORY = 600;
    private static final Gson JSON = new Gson();
    private static final Object LOCK = new Object();
    private static final Point[] RING = new Point[HISTORY];
    private static String version = "development";
    private static java.util.List<Map<String, String>> mods = java.util.List.of();
    public static void metadata(String currentVersion, java.util.List<Map<String, String>> installed) { version = currentVersion; mods = java.util.List.copyOf(installed); }
    private static volatile Map<String, Object> server = Map.of("state", "No local server");
    private static volatile Map<String, Object> client = Map.of("state", "Dedicated server");
    private static volatile HttpServer listener;
    private static ScheduledExecutorService sampler;
    private static int next, count, port;
    private static boolean attempted;
    private static long sequence, oldGcMillis, oldGcCount;
    private static final long STARTED = System.currentTimeMillis();
    public static void server(Map<String, Object> value) { server = Map.copyOf(value); }
    public static void client(Map<String, Object> value) { client = Map.copyOf(value); }
    public static boolean running() { return listener != null; }
    public static String url() { return running() ? "http://127.0.0.1:" + port + "/" : "Disabled"; }
    public static synchronized void start() {
        if (listener != null || attempted || !DiagnosticsConfig.ENABLED.get()) return;
        attempted = true;
        try {
            int preferred = DiagnosticsConfig.PORT.get();
            for (int candidate = preferred; candidate < preferred + 20; candidate++) {
                try { listener = HttpServer.create(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), candidate), 4); port = candidate; break; }
                catch (java.net.BindException occupied) { /* Multiple game instances get independent dashboards. */ }
            }
            if (listener == null) throw new IOException("No dashboard port available");
            listener.createContext("/", LocalDashboard::handle);
            listener.setExecutor(new java.util.concurrent.ThreadPoolExecutor(1, 2, 30, TimeUnit.SECONDS,
                new java.util.concurrent.ArrayBlockingQueue<>(8), r -> { Thread t = new Thread(r, "deepprimal-dashboard-http"); t.setDaemon(true); return t; },
                new java.util.concurrent.ThreadPoolExecutor.AbortPolicy()));
            listener.start();
            sampler = Executors.newSingleThreadScheduledExecutor(r -> { Thread t = new Thread(r, "deepprimal-dashboard-sampler"); t.setDaemon(true); return t; });
            sampler.scheduleAtFixedRate(() -> { try { sample(); } catch (RuntimeException e) { DeepPrimal.LOGGER.debug("Dashboard sample skipped", e); } }, 0, 1, TimeUnit.SECONDS);
            Runtime.getRuntime().addShutdownHook(new Thread(LocalDashboard::stop, "deepprimal-dashboard-stop"));
            DeepPrimal.LOGGER.info("Read-only performance dashboard: {}", url());
        } catch (IOException | LinkageError e) {
            if (listener != null) listener.stop(0); listener = null;
            DeepPrimal.LOGGER.warn("Local performance dashboard unavailable: {}", e.toString());
        }
    }
    public static synchronized void stop() {
        if (sampler != null) sampler.shutdownNow();
        if (listener != null) { listener.stop(0); ((java.util.concurrent.ExecutorService)listener.getExecutor()).shutdownNow(); }
        sampler = null; listener = null;
    }
    private static void sample() {
        long gcMillis = 0, gcCount = 0, direct = 0;
        for (var gc : ManagementFactory.getGarbageCollectorMXBeans()) { gcMillis += Math.max(0, gc.getCollectionTime()); gcCount += Math.max(0, gc.getCollectionCount()); }
        for (var b : ManagementFactory.getPlatformMXBeans(java.lang.management.BufferPoolMXBean.class)) if (b.getName().equals("direct")) direct += Math.max(0, b.getMemoryUsed());
        var heap = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        var nonHeap = ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage();
        double cpu = -1;
        if (ManagementFactory.getOperatingSystemMXBean() instanceof com.sun.management.OperatingSystemMXBean os) cpu = os.getProcessCpuLoad();
        synchronized (LOCK) {
            RING[next] = new Point(++sequence, System.currentTimeMillis(), heap.getUsed(), heap.getCommitted(), heap.getMax(), nonHeap.getUsed(), direct,
                sequence == 1 ? 0 : gcMillis - oldGcMillis, sequence == 1 ? 0 : gcCount - oldGcCount, cpu,
                ManagementFactory.getThreadMXBean().getThreadCount(), server, client);
            next = (next + 1) % HISTORY; count = Math.min(HISTORY, count + 1);
        }
        oldGcMillis = gcMillis; oldGcCount = gcCount;
    }
    private static List<Point> history(long since) {
        synchronized (LOCK) {
            var points = new ArrayList<Point>(count);
            for (int i = 0; i < count; i++) { Point p = RING[Math.floorMod(next - count + i, HISTORY)]; if (p.id > since) points.add(p); }
            return List.copyOf(points);
        }
    }
    private static void handle(HttpExchange exchange) throws IOException {
        try (exchange) {
            String host = exchange.getRequestHeaders().getFirst("Host"), origin = exchange.getRequestHeaders().getFirst("Origin");
            if (!("127.0.0.1:" + port).equals(host) && !("localhost:" + port).equals(host)) { respond(exchange, 403, "text/plain", "Loopback host required"); return; }
            if (origin != null && !origin.equals("http://127.0.0.1:" + port) && !origin.equals("http://localhost:" + port)) { respond(exchange, 403, "text/plain", "Same origin required"); return; }
            String site = exchange.getRequestHeaders().getFirst("Sec-Fetch-Site");
            if ("cross-site".equals(site)) { respond(exchange, 403, "text/plain", "Same origin required"); return; }
            if (!exchange.getRequestMethod().equals("GET")) { exchange.getResponseHeaders().set("Allow", "GET"); respond(exchange, 405, "text/plain", "Read only"); return; }
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) {
                try (var input = LocalDashboard.class.getResourceAsStream("/diagnostics/dashboard.html")) {
                    if (input == null) { respond(exchange, 500, "text/plain", "Missing dashboard resource"); return; }
                    respond(exchange, 200, "text/html", new String(input.readAllBytes(), StandardCharsets.UTF_8));
                }
            } else if (path.equals("/api/metrics") || path.equals("/api/export")) {
                long since = 0; String query = exchange.getRequestURI().getRawQuery();
                if (query != null && query.startsWith("since=")) {
                    try { since = Math.max(0, Long.parseLong(query.substring(6))); }
                    catch (NumberFormatException e) { respond(exchange, 400, "text/plain", "Invalid sequence"); return; }
                }
                var data = Map.of("schema", 1, "started", STARTED, "version", version, "historyLimit", HISTORY,
                    "java", System.getProperty("java.version"), "os", System.getProperty("os.name"), "processors", Runtime.getRuntime().availableProcessors(), "points", history(since), "mods", mods);
                if (path.endsWith("export")) exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=deepprimal-diagnostics.json");
                respond(exchange, 200, "application/json", JSON.toJson(data));
            } else respond(exchange, 404, "text/plain", "Not found");
        }
    }
    private static void respond(HttpExchange x, int status, String type, String text) throws IOException {
        var h = x.getResponseHeaders(); h.set("Content-Type", type + "; charset=utf-8"); h.set("Cache-Control", "no-store");
        h.set("X-Content-Type-Options", "nosniff"); h.set("Referrer-Policy", "no-referrer");
        h.set("Content-Security-Policy", "default-src 'self'; style-src 'unsafe-inline'; script-src 'unsafe-inline'; connect-src 'self'; frame-ancestors 'none'; base-uri 'none'");
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8); x.sendResponseHeaders(status, bytes.length); x.getResponseBody().write(bytes);
    }
    private record Point(long id, long time, long heapUsed, long heapCommitted, long heapMax, long nonHeap, long directBytes,
                         long gcMillis, long gcCount, double processCpu, int threads, Map<String, Object> server, Map<String, Object> client) {}
    private LocalDashboard() {}
}
