from pathlib import Path
import hashlib,difflib
R=Path(__file__).parent
p=R/'baseline/AnimatedWaterRenderer.java';s=p.read_text();begin=s.index('  /** Only boundary quads change;');end=s.index('  /** Guaranteed service',begin)
replacement='''  /** Only boundary quads change; contiguous changed quads share one bounded submission. */
  private void synchronizeCornerNeighbors(Page admitted) {
    ByteBuffer transfer = null;
    ByteBuffer quad = ByteBuffer.allocate(144).order(ByteOrder.nativeOrder());
    try {
      for (int dz = -1; dz <= 1; dz++) for (int dx = -1; dx <= 1; dx++) {
        if (dx == 0 && dz == 0) continue;
        Page p = pages.get(SectionPos.asLong((admitted.x >> 4) + dx, admitted.y >> 4, (admitted.z >> 4) + dz));
        if (p == null || p.gpu == null || p.presentation == null) continue;
        ByteBuffer accepted = ByteBuffer.wrap(p.presentation).order(ByteOrder.nativeOrder());
        int start = -1, end = 0;
        long pendingCleanup = p.cleanup;
        for (int entry : p.boundaryQuads) {
          int at = (entry >>> 12) * 144, cell = entry & 4095;
          int cx = cell & 15, cz = (cell >> 4) & 15, y = p.y + (cell >> 8);
          ByteBuffer patch = null;
          long cleanup = start >= 0 ? pendingCleanup : p.cleanup;
          for (int n = 0; n < 4; n++) {
            int x = p.x + cx + (n & 1), z = p.z + cz + (n >> 1);
            if (x < admitted.x || x > admitted.x + 16 || z < admitted.z || z > admitted.z + 16
                || y < admitted.y || y >= admitted.y + 16) continue;
            var c = cornerDirections.get(BlockPos.asLong(x, y, z));
            if (c == null) continue;
            int to = c.target(), from = c.active(clock) ? c.source() : to, age = c.age(clock);
            if (!WaterCornerPacket.matches(patch == null ? accepted : patch, patch == null ? at : 0,
                n, to, from, age, clock)) {
              if (patch == null) {
                quad.clear().put(p.presentation, at, 144).flip();
                patch = quad;
              }
              WaterCornerPacket.corner(patch, 0, n, to, from, age, clock);
            }
            if (c.queued(clock)) {
              long next = clock + 100 - age + 1;
              cleanup = cleanup == 0 ? next : Math.min(cleanup, next);
            }
          }
          if (patch != null) {
            if (transfer == null)
              transfer = MemoryUtil.memAlloc(WaterCornerPacket.SLICE_BYTES).order(ByteOrder.nativeOrder());
            // Never bridge an unchanged quad or exceed the existing range-byte bound.
            if (start >= 0 && (at != end || transfer.remaining() < 144)) {
              publishCornerRange(p, start, transfer, pendingCleanup);
              start = -1;
            }
            if (start < 0) start = at;
            transfer.put(patch);
            end = at + 144;
            pendingCleanup = cleanup;
          } else {
            if (start >= 0) {
              publishCornerRange(p, start, transfer, pendingCleanup);
              start = -1;
            }
            p.cleanup = cleanup;
          }
        }
        if (start >= 0) publishCornerRange(p, start, transfer, pendingCleanup);
      }
    } finally {
      if (transfer != null) MemoryUtil.memFree(transfer);
    }
  }

  /** Commit the accepted CPU mirror and deadline only after the entire GPU range succeeds. */
  private void publishCornerRange(Page p, int start, ByteBuffer transfer, long cleanup) {
    transfer.flip();
    int length = transfer.remaining();
    p.gpu.updateRange(start, transfer);
    transfer.get(0, p.presentation, start, length);
    p.cleanup = cleanup;
    transfer.clear();
  }

'''
candidate=s[:begin]+replacement+s[end:];(R/'candidate').mkdir(exist_ok=True);(R/'candidate/AnimatedWaterRenderer.java').write_text(candidate)
(R/'boundary-batch.patch').write_text(''.join(difflib.unified_diff(s.splitlines(True),candidate.splitlines(True),fromfile='a/src/main/java/com/shnewbs/deepprimal/client/render/AnimatedWaterRenderer.java',tofile='b/src/main/java/com/shnewbs/deepprimal/client/render/AnimatedWaterRenderer.java')))
print({'baseline_sha256':hashlib.sha256(s.encode()).hexdigest(),'candidate_sha256':hashlib.sha256(candidate.encode()).hexdigest(),'production_applied':False})
