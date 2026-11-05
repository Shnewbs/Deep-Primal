package com.shnewbs.deepprimal.settings;

import java.util.Map;
import java.util.Objects;

/** Optional rendering feature contract. Consumers never load the LOD implementation. */
public final class LodAccess {
  public interface View {
    default boolean available() {
      return true;
    }

    boolean active();

    int distance();

    int visibleDistance();

    String status();

    Map<String, Object> diagnostics();
  }

  private static final View ABSENT =
      new View() {
        public boolean available() {
          return false;
        }

        public boolean active() {
          return false;
        }

        public int distance() {
          return 0;
        }

        public int visibleDistance() {
          return 0;
        }

        public String status() {
          return "LOD module is not installed";
        }

        public Map<String, Object> diagnostics() {
          return Map.of("lodState", status());
        }
      };
  private static volatile View view = ABSENT;

  public static void install(View provider) {
    view = Objects.requireNonNull(provider);
  }

  public static boolean available() {
    return view.available();
  }

  public static boolean active() {
    return view.active();
  }

  public static int distance() {
    return view.distance();
  }

  public static int visibleDistance() {
    return view.visibleDistance();
  }

  public static String status() {
    return view.status();
  }

  public static Map<String, Object> diagnostics() {
    return view.diagnostics();
  }

  private LodAccess() {}
}
