from pathlib import Path
R=Path(__file__).resolve().parent;P=R/'project';p=P/'src/main/java/com/shnewbs/deepprimal/client/render/AnimatedWaterRenderer.java';s=p.read_text()
s=s.replace('  private final LinkedHashSet<Long> fresh', '''  private static final int CORNER_RETRY_BYTES = 1024 * 1024, CORNER_RETRY_RANGES = 16;
  private static final int CORNER_RETRY_SLICE = 65_520;
  private final ArrayDeque<CornerRetry> cornerRetries = new ArrayDeque<>();
  private int cornerRetryBytes;
  private final LinkedHashSet<Long> fresh''')
s=s.replace('    long uploaded, used, cleanup;','    long uploaded, used, cleanup, cornerGeneration;')
s=s.replace('  private record WaterFaces', '''  private static final class CornerRetry {
    final Page page;
    final long generation, queuedAt, cleanup;
    final int start;
    final byte[] data, masks;
    int offset, failures;
    CornerRetry(Page page, int start, byte[] data, byte[] masks, long queuedAt, long cleanup) {
      this.page = page; this.generation = page.cornerGeneration; this.start = start;
      this.data = data; this.masks = masks; this.queuedAt = queuedAt; this.cleanup = cleanup;
    }
  }

  private record WaterFaces''')
s=s.replace('return dirty.size() + urgent.size() + work.size();','return dirty.size() + urgent.size() + work.size() + cornerRetries.size();')
s=s.replace('  private void release(Page page) {','  private void release(Page page) {\n    cancelCornerRetries(page, false);')
s=s.replace('    retirePresentation();\n    // Retirement','    retirePresentation();\n    retryCornerPublications();\n    // Retirement')
s=s.replace('      p.presentation = accepted;','      cancelCornerRetries(p, false);\n      p.presentation = accepted;')
s=s.replace('      synchronizeCornerNeighbors(p);','      synchronizeCornerNeighbors(p, true);')
s=s.replace('  private void synchronizeCornerNeighbors(Page admitted) {','''  private void synchronizeCornerNeighbors(Page admitted) {
    synchronizeCornerNeighbors(admitted, false);
  }

  /** Ordinary publication stays within one extraction; only rejected ranges are deferred. */
  private void synchronizeCornerNeighbors(Page admitted, boolean deferFailures) {''')
s=s.replace('        ByteBuffer accepted = ByteBuffer.wrap(p.presentation)', '        cancelCornerRetries(p, true);\n        ByteBuffer accepted = ByteBuffer.wrap(p.presentation)',1)
s=s.replace('publishCornerRange(p, start, transfer, pendingCleanup);','publishCornerRange(p, start, transfer, pendingCleanup, deferFailures);')
s=s.replace('  private void publishCornerRange(Page p, int start, ByteBuffer transfer, long cleanup) {','  private void publishCornerRange(Page p, int start, ByteBuffer transfer, long cleanup, boolean deferFailures) {')
s=s.replace('''    p.gpu.updateRange(start, transfer);
    transfer.get(0, p.presentation, start, length);''','''    try {
      p.gpu.updateRange(start, transfer);
    } catch (RuntimeException failure) {
      if (!deferFailures) throw failure;
      deferCornerRange(p, start, transfer, cleanup);
      transfer.clear();
      return;
    }
    transfer.get(0, p.presentation, start, length);''',1)
a=s.index('  /** Guaranteed service before other extraction work.')
s=s[:a]+'''  /** A new material publication supersedes old failed data; remesh unaccepted earlier corners. */
  private void cancelCornerRetries(Page p, boolean remesh) {
    boolean removed = false;
    for (var it = cornerRetries.iterator(); it.hasNext();) {
      var retry = it.next();
      if (retry.page != p) continue;
      cornerRetryBytes -= retry.data.length;
      it.remove(); removed = true;
    }
    p.cornerGeneration++;
    if (removed && remesh) { p.changed = true; urgent.add(p.key); }
  }

  /** Bounded retained recovery data. Overflow schedules a fresh authoritative mesh instead. */
  private void deferCornerRange(Page p, int start, ByteBuffer transfer, long cleanup) {
    int length = transfer.remaining();
    if (cornerRetries.size() >= CORNER_RETRY_RANGES || length > CORNER_RETRY_BYTES - cornerRetryBytes) {
      p.changed = true; urgent.add(p.key);
      return;
    }
    byte[] data = new byte[length], masks = new byte[length / 144];
    transfer.get(0, data);
    var next = ByteBuffer.wrap(data).order(ByteOrder.nativeOrder());
    var old = ByteBuffer.wrap(p.presentation).order(ByteOrder.nativeOrder());
    for (int q = 0; q < length; q += 144) for (int n = 0; n < 4; n++) {
      if (WaterCornerPacket.target(next, q, n) != WaterCornerPacket.target(old, start + q, n)
          || WaterCornerPacket.source(next, q, n) != WaterCornerPacket.source(old, start + q, n)
          || WaterCornerPacket.start(next, q, n) != WaterCornerPacket.start(old, start + q, n))
        masks[q / 144] |= (byte)(1 << n);
    }
    cornerRetries.addLast(new CornerRetry(p, start, data, masks, clock, cleanup));
    cornerRetryBytes += length;
  }

  /** Mandatory fair recovery: <=16 stale headers, one write and65520bytes per rendered frame.
   * Rebuild only requested material corners over the latest accepted bytes, retaining retirement,
   * height, light and opacity. A completed transition becomes steady rather than reviving on wrap.
   * Three consecutive synchronous failures on a range remain fatal; async loss is not handled. */
  private void retryCornerPublications() {
    for (int headers = 0; headers < CORNER_RETRY_RANGES && !cornerRetries.isEmpty(); headers++) {
      var retry = cornerRetries.removeFirst(); Page p = retry.page;
      if (pages.get(p.key) != p || p.gpu == null || p.presentation == null
          || p.cornerGeneration != retry.generation) {
        cornerRetryBytes -= retry.data.length;
        continue;
      }
      int length = Math.min(CORNER_RETRY_SLICE, retry.data.length - retry.offset);
      ByteBuffer patch = MemoryUtil.memAlloc(length).order(ByteOrder.nativeOrder());
      try {
        patch.put(p.presentation, retry.start + retry.offset, length).flip();
        var queued = ByteBuffer.wrap(retry.data).order(ByteOrder.nativeOrder());
        long elapsed = Math.max(0, clock - retry.queuedAt);
        for (int q = 0; q < length; q += 144) {
          int original = retry.offset + q, targets = 0, sources = 0, ages = 0;
          for (int n = 0; n < 4; n++) {
            int to = WaterCornerPacket.target(queued, original, n);
            int age = elapsed >= 100 ? 100 : (int)Math.min(100, elapsed
                + ((retry.queuedAt - WaterCornerPacket.start(queued, original, n)) & 65535));
            int from = age == 100 ? to : WaterCornerPacket.source(queued, original, n);
            targets |= to << (n * 5); sources |= from << (n * 5); ages |= age << (n * 8);
          }
          WaterCornerPacket.patchCorners(patch, q, retry.masks[original / 144] & 15,
              targets, sources, ages, clock);
        }
        try {
          p.gpu.updateRange(retry.start + retry.offset, patch);
        } catch (RuntimeException failure) {
          if (++retry.failures >= 3) {
            cornerRetryBytes -= retry.data.length;
            throw new IllegalStateException("Water corner publication failed after three recovery attempts", failure);
          }
          cornerRetries.addLast(retry);
          return;
        }
        patch.get(0, p.presentation, retry.start + retry.offset, length);
        if (retry.cleanup != 0) p.cleanup = p.cleanup == 0 ? retry.cleanup : Math.min(p.cleanup, retry.cleanup);
        retry.offset += length; retry.failures = 0;
        if (retry.offset == retry.data.length) cornerRetryBytes -= retry.data.length;
        else cornerRetries.addLast(retry);
        return;
      } finally { MemoryUtil.memFree(patch); }
    }
  }

'''+s[a:]
s=s.replace('    pages.clear();','    cornerRetries.clear();\n    cornerRetryBytes = 0;\n    pages.clear();')
p.write_text(s)
print('Prepared bounded recovery; normal private publication retains strict rejection controls')
