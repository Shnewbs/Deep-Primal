from pathlib import Path
R=Path(__file__).resolve().parent;P=R/'project';p=P/'src/main/java/com/shnewbs/deepprimal/client/render/AnimatedWaterRenderer.java';s=p.read_text();(R/'before-AnimatedWaterRenderer.java').write_text(s)
s=s.replace('import com.shnewbs.deepprimal.core.WaterFlowField;','import com.shnewbs.deepprimal.core.WaterFlowField;\nimport com.shnewbs.deepprimal.core.WaterPresentation;')
s=s.replace('    WaterGpuMesh gpu;','    WaterGpuMesh gpu;\n    // Accepted vertex bytes only: total retained payload cannot exceed the 32 MiB GPU cap.\n    byte[] presentation;')
s=s.replace('  private void release(Page page) {','  private void release(Page page) {\n    page.presentation = null;')
s=s.replace('              .put((byte) 0);','              .put((byte) cy);')
s=s.replace('      data.flip();\n      if (reuse)', '      data.flip();\n      byte[] accepted = new byte[data.remaining()];\n      data.duplicate().get(accepted);\n      if (reuse)')
s=s.replace('      p.uploaded = clock;','      p.presentation = accepted;\n      p.uploaded = clock;')
s=s.replace('      uploads++;','      uploads++;\n      synchronizeCornerNeighbors(p);')
s=s.replace('  private boolean evict(','''  /** Patch resident neighbors before the next draw; no new capture, geometry or endpoint request.
   * At most eight same-layer pages and the existing 32 MiB resident payload are scanned/copied.
   * The retained CPU mirror is bounded by GPU residency. This cost requires native profiling. */
  private void synchronizeCornerNeighbors(Page admitted) {
    for (int dz = -1; dz <= 1; dz++) for (int dx = -1; dx <= 1; dx++) {
      if (dx == 0 && dz == 0) continue;
      Page p = pages.get(SectionPos.asLong((admitted.x >> 4) + dx, admitted.y >> 4, (admitted.z >> 4) + dz));
      if (p == null || p.gpu == null || p.presentation == null) continue;
      ByteBuffer old = ByteBuffer.wrap(p.presentation).order(ByteOrder.nativeOrder());
      boolean shared = false;
      for (int at = 0; at < old.limit(); at += 144) {
        if (old.getFloat(at + 16) >= -1) continue;
        int cell = old.get(at + 15) & 255, y = p.y + (old.get(at + 35) & 15);
        for (int n = 0; n < 4; n++) {
          int x = p.x + (cell & 15) + (n & 1), z = p.z + (cell >> 4) + (n >> 1);
          if (x >= admitted.x && x <= admitted.x + 16 && z >= admitted.z && z <= admitted.z + 16
              && y >= admitted.y && y < admitted.y + 16 && cornerDirections.containsKey(BlockPos.asLong(x,y,z))) shared = true;
        }
      }
      if (!shared) continue;
      ByteBuffer data = MemoryUtil.memAlloc(p.presentation.length).order(ByteOrder.nativeOrder());
      try {
        data.put(p.presentation).flip();
        WaterPresentation.rebase(data, clock - p.uploaded);
        for (int at = 0; at < data.limit(); at += 144) {
          if (data.getFloat(at + 16) >= -1) continue;
          int cell = data.get(at + 15) & 255, y = p.y + (data.get(at + 35) & 15);
          for (int n = 0; n < 4; n++) {
            int x = p.x + (cell & 15) + (n & 1), z = p.z + (cell >> 4) + (n >> 1);
            if (x < admitted.x || x > admitted.x + 16 || z < admitted.z || z > admitted.z + 16
                || y < admitted.y || y >= admitted.y + 16) continue;
            var corner = cornerDirections.get(BlockPos.asLong(x,y,z));
            if (corner == null) continue;
            WaterPresentation.corner(data, at, n, corner.target(), corner.active(clock) ? corner.source() : corner.target(), corner.age(clock));
            if (corner.queued(clock)) {
              long deadline = clock + WaterFlowTransition.MILLIS - corner.age(clock) + 1;
              p.cleanup = p.cleanup == 0 ? deadline : Math.min(p.cleanup, deadline);
            }
          }
        }
        p.gpu.update(data, p.presentation.length / 36);
        data.duplicate().get(p.presentation);
        p.uploaded = clock;
      } finally { MemoryUtil.memFree(data); }
    }
  }

  private boolean evict(''')
s=s.replace('      p.fading = Map.of();\n      if (p.gpu != null)', '      p.fading = Map.of();\n      p.presentation = null;\n      if (p.gpu != null)')
p.write_text(s)
