import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
public class BaselineIndex {
 static final int MAX_DISK_KEYS=8192;
 static long key(int x,int z){return ((long)x<<32)^(z&0xffffffffL);}
 public static List<Long> scan(Path root)throws Exception {
var keys = new ArrayList<Long>();
                    if (Files.isDirectory(root))
                      try (var files = Files.newDirectoryStream(root, "*.plod")) {
                        for (Path p : files) {
                          if (Thread.interrupted()) throw new CancellationException();
                          String[] parts =
                              p.getFileName().toString().replace(".plod", "").split("_");
                          try {
                            keys.add(key(Integer.parseInt(parts[0]), Integer.parseInt(parts[1])));
                          } catch (RuntimeException ignored) {
                          }
                          if (keys.size() >= MAX_DISK_KEYS) break;
                        }
                      }
                    return keys;
}
}
