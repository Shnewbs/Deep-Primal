from pathlib import Path
import difflib,hashlib,json
R=Path(__file__).parent;P=R/'project';C=R/'candidate';C.mkdir(exist_ok=True)
p=Path('engine-core/src/main/java/com/shnewbs/deepprimal/core/WaterCornerPacket.java');s=(P/p).read_text();needle='  /** Read-only admission check: steady slices need no temporary copy or GPU write. */'
addition='''  private static int start(int a, int c, int marker, int n) {
    return switch (n) {
      case 0 -> (a >>> 1) & 65535;
      case 1 -> (a >>> 17) | ((c & 1) << 15);
      case 2 -> (c >>> 1) & 65535;
      case 3 -> (c >>> 17) | ((marker & 32) << 10);
      default -> throw new IllegalArgumentException();
    };
  }

  /** Read a quad's shared packet once; return only requested corners that need publication.
   * The packed target/source fields use five bits per corner and ages use eight. */
  public static int changedCorners(ByteBuffer b, int q, int mask, int targets, int sources,
      int ages, long now) {
    if (mask == 0) return 0;
    long pairs = pairCode(b, q);
    int a = word1(b, q), c = word2(b, q), marker = b.get(q + 33), changed = 0;
    for (int n = 0; n < 4; n++) {
      if ((mask & (1 << n)) == 0) continue;
      int pair = (int) (pairs / POW[n] % 289), to = (targets >>> (n * 5)) & 31;
      int from = (sources >>> (n * 5)) & 31, oldStart = start(a, c, marker, n);
      boolean same = pair / 17 == to &&
          (pair % 17 == to || (((int) now - oldStart) & 65535) >= 100
              ? from == to
              : pair % 17 == from && oldStart == (((int) now - (ages >>> (n * 8) & 255)) & 65535));
      if (!same) changed |= 1 << n;
    }
    return changed;
  }

  /** Apply the changed-corner mask to a private quad, encoding each vertex only once.
   * Unselected starts/pairs and each vertex's opacity/light/nonmaterial bytes are retained. */
  public static void patchCorners(ByteBuffer b, int q, int mask, int targets, int sources,
      int ages, long now) {
    if (mask == 0) return;
    long pairs = pairCode(b, q);
    int w1 = word1(b, q), w2 = word2(b, q), marker = b.get(q + 33);
    int a = start(w1, w2, marker, 0), c = start(w1, w2, marker, 1);
    int d = start(w1, w2, marker, 2), e = start(w1, w2, marker, 3);
    for (int n = 0; n < 4; n++) {
      if ((mask & (1 << n)) == 0) continue;
      long pair = ((targets >>> (n * 5)) & 31) * 17L + ((sources >>> (n * 5)) & 31);
      pairs += (pair - pairs / POW[n] % 289) * POW[n];
      int value = ((int) now - (ages >>> (n * 8) & 255)) & 65535;
      switch (n) { case 0 -> a = value; case 1 -> c = value; case 2 -> d = value; case 3 -> e = value; }
    }
    for (int v = 0; v < 4; v++) {
      int at = q + v * 36;
      write(b, at, pairs, a, c, d, e, opacityTarget(b, at) == 127);
    }
  }

'''
assert needle in s;s=s.replace(needle,addition+needle);(C/p.name).write_text(s)
p=Path('src/main/java/com/shnewbs/deepprimal/client/render/AnimatedWaterRenderer.java');s=(P/p).read_text();a=s.index('          ByteBuffer patch = null;',s.index('private void synchronizeCornerNeighbors'));b=s.index('            if (c.queued(clock))',a)
s=s[:a]+'''          int mask = 0, targets = 0, sources = 0, ages = 0;
          long cleanup = start >= 0 ? pendingCleanup : p.cleanup;
          for (int n = 0; n < 4; n++) {
            int x = p.x + cx + (n & 1), z = p.z + cz + (n >> 1);
            if (x < admitted.x || x > admitted.x + 16 || z < admitted.z || z > admitted.z + 16
                || y < admitted.y || y >= admitted.y + 16) continue;
            var c = cornerDirections.get(BlockPos.asLong(x, y, z));
            if (c == null) continue;
            int to = c.target(), from = c.active(clock) ? c.source() : to, age = c.age(clock);
            mask |= 1 << n;
            targets |= to << (n * 5); sources |= from << (n * 5); ages |= age << (n * 8);
'''+s[b:]
a=s.index('          if (patch != null) {',s.index('private void synchronizeCornerNeighbors'))
s=s[:a]+'''          int changed = WaterCornerPacket.changedCorners(accepted, at, mask, targets, sources, ages, clock);
          if (changed != 0) {
            quad.clear().put(p.presentation, at, 144).flip();
            WaterCornerPacket.patchCorners(quad, 0, changed, targets, sources, ages, clock);
'''+s[a+len('          if (patch != null) {\n'):]
s=s.replace('            transfer.put(patch);','            transfer.put(quad);');(C/p.name).write_text(s)
print(json.dumps({p.name:hashlib.sha256(p.read_bytes()).hexdigest()for p in C.iterdir()},indent=2))
