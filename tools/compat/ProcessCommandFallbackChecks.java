package com.shnewbs.deepprimal.buildcompat;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipFile;
import net.neoforged.neoform.runtime.engine.NeoFormEngine;

/** Dependency/tool checks only; not included in any mod source set. */
public final class ProcessCommandFallbackChecks {
  private static void check(boolean value, String message) {
    if (!value) throw new AssertionError(message);
  }

  public static void main(String[] arguments) throws Exception {
    var captured = new AtomicReference<ClassFileTransformer>();
    var instrumentation = (Instrumentation) Proxy.newProxyInstance(
        Instrumentation.class.getClassLoader(), new Class<?>[] {Instrumentation.class},
        (proxy, method, args) -> {
          if (method.getName().equals("addTransformer")) captured.set((ClassFileTransformer) args[0]);
          return null;
        });
    ProcessCommandFallback.premain(null, instrumentation);
    ClassFileTransformer transformer = captured.get();
    try (var zip = new ZipFile(arguments[0])) {
      for (String name : new String[] {
          "net/neoforged/neoform/runtime/engine/NeoFormEngine",
          "net/neoforged/neoform/runtime/actions/ExternalJavaToolAction"}) {
        byte[] bytes = zip.getInputStream(zip.getEntry(name + ".class")).readAllBytes();
        byte[] changed = transformer.transform(null, name, null, null, bytes);
        check(changed != null && !Arrays.equals(bytes, changed), "Pinned target was not transformed");
        check(transformer.transform(null, "unrelated/ModClass", null, null, bytes) == null,
            "An unrelated class was transformed");
        byte[] unknown = bytes.clone(); unknown[unknown.length - 1] ^= 1;
        boolean rejected = false;
        try { transformer.transform(null, name, null, null, unknown); }
        catch (IllegalStateException expected) { rejected = true; }
        check(rejected, "An unrecognized target was accepted");
      }
    }
    check(ProcessCommandFallback.executable(Optional.of("retained")).equals("retained"),
        "A valid command was replaced");
    check(Files.isExecutable(Path.of((String) ProcessCommandFallback.executable(Optional.empty()))),
        "The fallback is not executable");
    new NeoFormEngine(null, null, null, null);
    System.out.println("BUILD_COMPAT_CHECKS_PASS pinnedTargets=2 unrelatedUntouched=true"
        + " unknownRejected=true retainedCommand=true emptyFallback=true constructor=true");
  }

  private ProcessCommandFallbackChecks() {}
}
