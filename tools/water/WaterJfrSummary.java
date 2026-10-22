import java.nio.file.Path;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import jdk.jfr.consumer.RecordedFrame;
import jdk.jfr.consumer.RecordedThread;
import jdk.jfr.consumer.RecordingFile;

/** Optional development profiler; streams JFR without exporting enormous metadata JSON. */
public final class WaterJfrSummary {
  private static final String WATER = "com.shnewbs.deepprimal.engine.WaterRuntime";

  private static String method(RecordedFrame frame) {
    return frame.getMethod().getType().getName() + "." + frame.getMethod().getName();
  }

  private static void top(String name, Map<String, Long> counts) {
    System.out.println("\"" + name + "\": {");
    var entries = counts.entrySet().stream()
        .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
        .limit(20).toList();
    for (int n = 0; n < entries.size(); n++) {
      var entry = entries.get(n);
      System.out.print("  \"" + entry.getKey() + "\": " + entry.getValue());
      System.out.println(n + 1 < entries.size() ? "," : "");
    }
    System.out.print("}");
  }

  public static void main(String[] args) throws Exception {
    long serverSamples = 0, waterSamples = 0, allocationSamples = 0, allocationWeight = 0;
    Map<String, Long> roots = new TreeMap<>(), leaves = new TreeMap<>();
    Map<String, Long> allocatedClasses = new TreeMap<>(), allocatedOwners = new TreeMap<>();
    try (var recording = new RecordingFile(Path.of(args[0]))) {
      while (recording.hasMoreEvents()) {
        var event = recording.readEvent();
        boolean execution = event.getEventType().getName().equals("jdk.ExecutionSample");
        boolean allocation = event.getEventType().getName().equals("jdk.ObjectAllocationSample");
        if (!execution && !allocation) continue;
        RecordedThread thread = event.getThread(execution ? "sampledThread" : "eventThread");
        if (thread == null || !thread.getJavaName().equals("Server thread")) continue;
        var stack = event.getStackTrace();
        if (execution) serverSamples++;
        if (stack == null || stack.getFrames().isEmpty()) continue;
        String owner = null;
        for (var frame : stack.getFrames()) {
          if (frame.getMethod().getType().getName().startsWith(WATER)) {
            owner = method(frame);
            break;
          }
        }
        if (owner == null) continue;
        if (execution) {
          waterSamples++;
          roots.merge(owner, 1L, Long::sum);
          leaves.merge(method(stack.getFrames().getFirst()), 1L, Long::sum);
        } else {
          long weight = event.getLong("weight");
          allocationSamples++;
          allocationWeight += weight;
          allocatedClasses.merge(event.getClass("objectClass").getName(), weight, Long::sum);
          allocatedOwners.merge(owner, weight, Long::sum);
        }
      }
    }
    System.out.println("{\"scope\":\"sampled-server-thread-water-stacks; allocation weights are estimates\",");
    System.out.println("\"serverExecutionSamples\":" + serverSamples + ",\"waterExecutionSamples\":"
        + waterSamples + ",\"waterAllocationSamples\":" + allocationSamples
        + ",\"estimatedWaterAllocationBytes\":" + allocationWeight + ",");
    top("nearestWaterMethodSamples", roots); System.out.println(",");
    top("waterStackLeafSamples", leaves); System.out.println(",");
    top("estimatedAllocationBytesByClass", allocatedClasses); System.out.println(",");
    top("estimatedAllocationBytesByWaterMethod", allocatedOwners); System.out.println("\n}");
  }
}
