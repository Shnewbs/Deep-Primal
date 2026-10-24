from pathlib import Path
R=Path(__file__).resolve().parent;P=R/'project';p=P/'engine-core/src/main/java/com/shnewbs/deepprimal/core/WaterCornerPacket.java';s=p.read_text();needle='  /** Read-only admission check: steady slices need no temporary copy or GPU write. */';addition='''  /** Rebase failed material corners over current accepted bytes; never restore retired fields.
   * Full monotonic elapsed time disambiguates the queued16-bit timestamps across later wraps. */
  public static void rebaseCorners(ByteBuffer current, int dst, ByteBuffer queued, int src,
      int mask, long queuedAt, long now) {
    long elapsed = Math.max(0, now - queuedAt);
    int targets = 0, sources = 0, ages = 0;
    for (int n = 0; n < 4; n++) {
      int to = target(queued, src, n);
      int age = elapsed >= 100 ? 100 : (int)Math.min(100, elapsed
          + ((queuedAt - start(queued, src, n)) & 65535));
      int from = age == 100 ? to : source(queued, src, n);
      targets |= to << (n * 5); sources |= from << (n * 5); ages |= age << (n * 8);
    }
    patchCorners(current, dst, mask, targets, sources, ages, now);
  }

''';assert needle in s;s=s.replace(needle,addition+needle);p.write_text(s)
p=P/'src/main/java/com/shnewbs/deepprimal/client/render/AnimatedWaterRenderer.java';s=p.read_text();a=s.index('        long elapsed = Math.max(0, clock - retry.queuedAt);');b=s.index('        try {\n          p.gpu.updateRange',a);s=s[:a]+'''        for (int q = 0; q < length; q += 144) {
          int original = retry.offset + q;
          WaterCornerPacket.rebaseCorners(patch, q, queued, original,
              retry.masks[original / 144] & 15, retry.queuedAt, clock);
        }
'''+s[b:];p.write_text(s)
p=P/'src/coreChecks/java/com/shnewbs/deepprimal/core/CoreChecks.java';s=p.read_text().replace('    WaterBoundaryIndexChecks.run();','    WaterBoundaryIndexChecks.run();\n    WaterCornerRetryChecks.run();');p.write_text(s)
