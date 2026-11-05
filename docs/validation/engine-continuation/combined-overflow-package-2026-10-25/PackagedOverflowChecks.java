import java.nio.file.Path;
public final class PackagedOverflowChecks {
  public static void main(String[] args) throws Exception {
    Path jar = Path.of(args[0]).toRealPath();
    for (String name : new String[]{"com.shnewbs.deepprimal.core.ByteLru", "com.shnewbs.deepprimal.core.density.DensityGrid"}) {
      Class<?> type = Class.forName(name);
      Path loaded = Path.of(type.getProtectionDomain().getCodeSource().getLocation().toURI()).toRealPath();
      if (!loaded.equals(jar)) throw new AssertionError("Wrong product code source: " + loaded);
      System.out.println("PACKAGED_CLASS_PASS " + name);
    }
    ByteLruOverflowChecks.main(new String[0]);
    DensityGridFiniteChecks.main(new String[0]);
    System.out.println("COMBINED_OVERFLOW_PACKAGE_PASS");
  }
}
