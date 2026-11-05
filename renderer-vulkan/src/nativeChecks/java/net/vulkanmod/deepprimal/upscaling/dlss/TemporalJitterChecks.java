package net.vulkanmod.deepprimal.upscaling.dlss;
public final class TemporalJitterChecks {
  public static void main(String[] args) {
    int count = 0;
    for (int period : new int[]{1, 2, 8, 16, 32, 1024}) {
      for (long frame = 0; frame < 4096; frame++) {
        var a = TemporalJitter.at(frame, period);
        var b = TemporalJitter.at(frame + period, period);
        if (a.xPixels() != b.xPixels() || a.yPixels() != b.yPixels()) throw new AssertionError("periodicity");
        if (!(a.xPixels() >= -.5f && a.xPixels() < .5f && a.yPixels() >= -.5f && a.yPixels() < .5f)) throw new AssertionError("bounds");
        if (!Float.isFinite(a.xNdc(1920)) || !Float.isFinite(a.yNdc(1080))) throw new AssertionError("NDC");
        count += 3;
      }
    }
    for (Runnable r : new Runnable[]{() -> TemporalJitter.at(-1,8), () -> TemporalJitter.at(0,0), () -> TemporalJitter.at(0,1025), () -> TemporalJitter.at(0,8).xNdc(0), () -> TemporalJitter.at(0,8).yNdc(-1)}) {
      try { r.run(); throw new AssertionError("missing rejection"); }
      catch (IllegalArgumentException expected) { count++; }
    }
    System.out.println("TEMPORAL_JITTER_PASS checks=" + count);
  }
}
