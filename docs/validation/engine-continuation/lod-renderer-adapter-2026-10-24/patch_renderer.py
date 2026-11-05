from pathlib import Path
root=Path(__file__).resolve().parent
s=(root/'evidence/before-VoxelLodRenderer.java').read_text()
s=s.replace('private ThreadPoolExecutor worker;', 'private LodIndexWorker worker;')
s=s.replace('private Future<List<Long>> scan;', '''private Future<LodIndexRequest.Step> scan;
  private LodIndexRequest indexRequest;
  private int indexCameraX, indexCameraZ;
  private boolean indexStarted;''')
s=s.replace('''new ThreadPoolExecutor(
            1,
            1,
            20,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(2),''','''new LodIndexWorker(''')
a=s.index('        if (LodConfig.PERSIST.get())\n          scan =')
b=s.index('      } catch (java.security.NoSuchAlgorithmException',a)
s=s[:a]+s[b:]
a=s.index('    if (scan != null && scan.isDone()) {')
b=s.index('    queuedChunks = dirty.size();',a)
s=s[:a]+'''    serviceIndex(mc.player.chunkPosition().x(), mc.player.chunkPosition().z());
'''+s[b:]
pos=s.index('  /** Keep cache restoration moving')
s=s[:pos]+'''  /** One metadata turn per tick; geometry keeps its existing single outstanding job. */
  private void serviceIndex(int cx, int cz) {
    boolean enabled = LodConfig.PERSIST.get() && root != null;
    boolean moved = indexStarted && (cx != indexCameraX || cz != indexCameraZ);
    if (indexRequest != null && (!enabled || moved) && !indexRequest.isCancelled())
      worker.cancelIndex();
    if (scan != null) {
      if (!scan.isDone()) return;
      try {
        LodIndexRequest.Step result = scan.get();
        if (enabled && indexRequest.canPublish(result, session, cx, cz)) {
          // Preserve recovery ordering and edits; share the existing key cap.
          for (long k : result.keys()) {
            if (disk.size() >= MAX_DISK_KEYS) break;
            disk.add(k);
          }
        }
      } catch (InterruptedException interrupted) {
        Thread.currentThread().interrupt();
        return;
      } catch (ExecutionException | CancellationException failure) {
        status = "Cache index unavailable";
        com.shnewbs.deepprimal.lod.LodModule.LOGGER.warn("LOD index failed", failure);
      } finally { scan = null; }
    }
    // Never replace a worker-owned open cursor before its cancellation cleanup completes.
    if (indexRequest != null && indexRequest.isCancelled() && !indexRequest.isTerminal()) return;
    if (!enabled) {
      indexRequest = null;
      indexStarted = false;
      return;
    }
    if (!indexStarted || moved || indexRequest.isCancelled()) {
      indexCameraX = cx;
      indexCameraZ = cz;
      indexStarted = true;
      indexRequest = new LodIndexRequest(root, session, cx, cz, MAX_DISK_KEYS, 256);
    }
    if (indexRequest.isTerminal()) return;
    try { scan = worker.submitIndex(indexRequest); }
    catch (RejectedExecutionException full) {
      // Retain the request and retry a single turn next tick; no unbounded self-resubmission.
    }
  }

'''+s[pos:]
s=s.replace('    if (scan != null) scan.cancel(true);\n    if (worker != null) worker.shutdownNow();', '''    // Signal/wake the cursor before closing admission; queued cleanup runs on its owner.
    // Do not cancel its Future or discard the queue with shutdownNow.
    if (worker != null) worker.shutdown();''')
s=s.replace('    scan = null;\n    worker = null;', '''    scan = null;
    indexRequest = null;
    indexStarted = false;
    worker = null;''')
p=root/'staged/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java'
p.parent.mkdir(parents=True,exist_ok=True);p.write_text(s)
