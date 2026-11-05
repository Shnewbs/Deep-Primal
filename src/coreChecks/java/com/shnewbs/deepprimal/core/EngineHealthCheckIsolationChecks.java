package com.shnewbs.deepprimal.core;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/** No Minecraft dependencies: a broken invariant must not prevent subsequent checks. */
public final class EngineHealthCheckIsolationChecks {
  private static int assertions;

  public static void main(String[] args) {
    AtomicInteger after = new AtomicInteger();
    AtomicInteger mode = new AtomicInteger();
    EngineHealthCheck.setEnabled(true);
    EngineHealthCheck.register("dev138-first", () -> {
      int m = mode.get();
      if (m == 1) throw new AssertionError("water invariant failed");
      if (m == 2) throw new IllegalStateException("bad sensor");
      if (m == 3) return EngineHealthCheck.CheckResult.fail("dev138-first", "mismatch");
      if (m == 4) return null;
      if (m == 5) throw new LinkageError("fatal class linkage");
      return EngineHealthCheck.CheckResult.pass("dev138-first");
    });
    EngineHealthCheck.register("dev138-after", () -> {
      after.incrementAndGet();
      return EngineHealthCheck.CheckResult.pass("dev138-after");
    });

    mode.set(1);
    var failures = EngineHealthCheck.runAll();
    check(failures.size() == 1, "one assertion failure");
    check(failures.get(0).name().equals("dev138-first"), "registered check identity");
    check(failures.get(0).exception() != null, "assertion retains diagnostic exception");
    check(failures.get(0).exception().getCause() instanceof AssertionError,
        "assertion cause retained");
    check(failures.get(0).message().contains("water invariant failed"), "failure message");
    check(after.get() == 1, "subsequent check still executes");

    var random = new Random(20261031L);
    for (int i = 0; i < 50000; i++) {
      int m = random.nextInt(5);
      mode.set(m);
      int before = after.get();
      List<EngineHealthCheck.CheckResult> results = EngineHealthCheck.runAll();
      check(after.get() == before + 1, "continuation iteration=" + i);
      check(results.size() == (m == 0 ? 0 : 1), "failure cardinality iteration=" + i);
      if (m == 0) continue;
      var result = results.get(0);
      check(!result.passed() && result.name().equals("dev138-first"), "failure identity");
      if (m == 1) check(result.exception() != null
          && result.exception().getCause() instanceof AssertionError, "assertion cause");
      if (m == 2) check(result.exception() instanceof IllegalStateException,
          "runtime exception preserved");
      if (m == 3) check(result.exception() == null && result.message().equals("mismatch"),
          "explicit failed result preserved");
      if (m == 4) check(result.exception() instanceof NullPointerException,
          "null supplier failure isolated");
    }
    mode.set(1);
    try {
      EngineHealthCheck.runOrThrow();
      throw new AssertionError("runOrThrow must report failure");
    } catch (IllegalStateException expected) {
      check(expected.getMessage().contains("dev138-first"), "startup failure surfaced");
    }
    int before = after.get();
    EngineHealthCheck.setEnabled(false);
    check(EngineHealthCheck.runAll().isEmpty() && after.get() == before,
        "disabled checks must not run");
    EngineHealthCheck.setEnabled(true);
    mode.set(0);
    check(EngineHealthCheck.runAll().isEmpty(), "healthy checks pass");

    // Do not hide fatal VM/linkage errors behind ordinary invariant diagnostics.
    mode.set(5);
    try {
      EngineHealthCheck.runAll();
      throw new AssertionError("fatal error swallowed");
    } catch (LinkageError expected) {
      check(expected.getMessage().equals("fatal class linkage"), "fatal error propagated");
    }
    mode.set(0);
    System.out.println("ENGINE_HEALTH_ISOLATION_PASS checks=" + assertions
        + " trials=50000 assertionContinues=true exceptionContinues=true fatalPropagates=true");
  }

  private static void check(boolean condition, String message) {
    assertions++;
    if (!condition) throw new AssertionError(message);
  }

  private EngineHealthCheckIsolationChecks() {}
}
