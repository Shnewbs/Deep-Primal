package com.shnewbs.deepprimal.lod.client;

/** Small committed rerun gate; extended randomized oracle is retained in the evidence ZIP. */
public final class LodPageUploadSmoke {
  private LodPageUploadSmoke() {}
  public static void main(String[] args) {
    LodPageUploadProbe p = new LodPageUploadProbe(3);
    p.committed(10, 1, 92);
    p.committed(40, 4, 368);
    p.dropped(LodPageUploadProbe.Drop.STALE);
    p.dropped(LodPageUploadProbe.Drop.MEMORY);
    p.committed(20, 2, 184);
    var s = p.snapshot();
    if (s.committedPages() != 3 || s.staleDrops() != 1 || s.memoryDrops() != 1
        || s.peakUploadFrames() != 4 || s.wallNanos().p95Nanos() != 40
        || s.uploadFrames().p50Nanos() != 2 || s.committedBytes().p99Nanos() != 368)
      throw new AssertionError("page upload accounting");
    p.committed(50, 5, 460);
    if (p.snapshot().wallNanos().p50Nanos() != 40) throw new AssertionError("rolling eviction");
    p.reset();
    s = p.snapshot();
    if (s.wallNanos().samples() != 0 || s.committedPages() != 0 || s.memoryDrops() != 0)
      throw new AssertionError("reset");
    System.out.println("LOD_PAGE_UPLOAD_SMOKE_PASS");
  }
}
