package com.shnewbs.deepprimal.validation;

import com.shnewbs.deepprimal.core.Aquifer;
import com.shnewbs.deepprimal.engine.HydrologyRuntime;
import java.lang.reflect.Method;
import java.math.BigInteger;

/** Private check of the loaded runtime class, also called by the server stream fixture. */
public final class StreamAllowanceNativeChecks {
  static void run() throws ReflectiveOperationException {
    Method allowance = HydrologyRuntime.class.getDeclaredMethod(
        "streamAllowance", int.class, long.class, long.class);
    allowance.setAccessible(true);
    long[] ticks = {Long.MIN_VALUE, Long.MIN_VALUE + 1, Long.MIN_VALUE + 19,
        -100, -20, -19, -1, 0, 1, 18, 19, 20, 100,
        Long.MAX_VALUE - 20, Long.MAX_VALUE - 19, Long.MAX_VALUE - 1, Long.MAX_VALUE};
    int cases = 0;
    for (int quota : new int[] {0, 1, 32, Aquifer.baseflowUnits(64), Aquifer.baseflowUnits(4096)}) {
      for (long start : ticks) for (long now : ticks) {
        int expected = quota;
        if (start != Long.MIN_VALUE && now >= start) {
          BigInteger phase = BigInteger.valueOf(now).subtract(BigInteger.valueOf(start))
              .min(BigInteger.valueOf(19));
          expected = BigInteger.valueOf(quota).multiply(phase.add(BigInteger.ONE))
              .divide(BigInteger.valueOf(20)).intValueExact();
        }
        int actual = (int) allowance.invoke(null, quota, start, now);
        if (actual != expected || actual < 0 || actual > quota) {
          throw new AssertionError("Stream allowance mismatch quota=" + quota + " start="
              + start + " now=" + now + " expected=" + expected + " actual=" + actual);
        }
        cases++;
      }
    }
    System.out.println("STREAM_ALLOWANCE_RUNTIME_PASS cases=" + cases
        + " exactIntegerOracle=true signedOverflow=true rewind=true sentinel=true"
        + " runtime=" + HydrologyRuntime.class.getProtectionDomain().getCodeSource().getLocation());
  }

  public static void main(String[] args) throws ReflectiveOperationException { run(); }
  private StreamAllowanceNativeChecks() {}
}
