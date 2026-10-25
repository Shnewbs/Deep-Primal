package com.shnewbs.deepprimal.validation;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.jar.JarFile;

final class GroundPackageChecks {
  static void verify(Class<?>... classes) throws IOException {
    String expected = System.getenv("DEEPPRIMAL_GROUND_VALIDATION_JAR");
    if (expected == null) return;
    Path path = Path.of(expected);
    try (var jar = new JarFile(path.toFile())) {
      for (var type : classes) {
        String origin = type.getProtectionDomain().getCodeSource().getLocation().toExternalForm();
        if (!origin.contains(path.getFileName().toString()))
          throw new AssertionError("Production class not loaded from selected JAR: " + type + " " + origin);
        var entry = jar.getJarEntry(type.getName().replace('.', '/') + ".class");
        if (entry == null) throw new AssertionError("Selected JAR lacks " + type);
        try (var packaged = jar.getInputStream(entry);
            var loaded = type.getResourceAsStream(type.getSimpleName() + ".class")) {
          if (loaded == null || !Arrays.equals(packaged.readAllBytes(), loaded.readAllBytes()))
            throw new AssertionError("Loaded production bytes differ from selected JAR: " + type);
        }
        System.out.println("GROUND_CLASS_ORIGIN class=" + type.getName() + " source=" + origin);
      }
    }
    System.out.println("GROUND_PACKAGE_IDENTITY_PASS classes=" + classes.length);
  }

  private GroundPackageChecks() {}
}
