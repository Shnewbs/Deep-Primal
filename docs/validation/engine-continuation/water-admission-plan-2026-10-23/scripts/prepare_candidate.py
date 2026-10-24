from pathlib import Path
import difflib,hashlib
R=Path(__file__).parent;s=(R/'baseline/AnimatedWaterRenderer.java').read_text();old='''    while (!reuse && residentBytes + bytes > MAX_BYTES && evict(camera, p)) {}
    if (!reuse && residentBytes + bytes > MAX_BYTES) {
''';new='''    if (!reuse && !reserveBytes(camera, p, bytes)) {
''';assert old in s;s=s.replace(old,new,1)
at=s.index('  private boolean evict(net.minecraft.world.phys.Vec3 camera, Page keep) {')
helper='''  /** Plan a sufficient farthest-first set before releasing any covering allocation.
   * Discovery's separate page-count policy is unchanged. A denied byte reservation is read-only. */
  private boolean reserveBytes(net.minecraft.world.phys.Vec3 camera, Page keep, long bytes) {
    if (bytes > MAX_BYTES) return false;
    long required = residentBytes + bytes - MAX_BYTES;
    if (required <= 0) return true;
    double minimumDistance = keep.bounds.distanceToSqr(camera);
    var candidates = new ArrayList<Page>();
    long available = 0;
    for (Page page : pages.values()) {
      if (page == keep || page.busy || page.gpu == null
          || page.bounds.distanceToSqr(camera) <= minimumDistance) continue;
      candidates.add(page);
      available += page.gpu.bytes;
    }
    if (available < required) return false;
    // Stable sorting retains the prior insertion-order rule for equal-distance victims.
    candidates.sort(Comparator.comparingDouble((Page page) -> page.bounds.distanceToSqr(camera)).reversed());
    for (Page page : candidates) {
      long reclaimed = page.gpu.bytes;
      pages.remove(page.key);
      release(page);
      required -= reclaimed;
      if (required <= 0) return true;
    }
    throw new IllegalStateException("Water reservation accounting changed during admission");
  }

'''
s=s[:at]+helper+s[at:];(R/'candidate').mkdir(exist_ok=True);(R/'candidate/AnimatedWaterRenderer.java').write_text(s)
before=(R/'baseline/AnimatedWaterRenderer.java').read_text();(R/'admission-plan.patch').write_text(''.join(difflib.unified_diff(before.splitlines(True),s.splitlines(True),fromfile='a/src/main/java/com/shnewbs/deepprimal/client/render/AnimatedWaterRenderer.java',tofile='b/src/main/java/com/shnewbs/deepprimal/client/render/AnimatedWaterRenderer.java')))
print({'baseline_sha256':hashlib.sha256(before.encode()).hexdigest(),'candidate_sha256':hashlib.sha256(s.encode()).hexdigest(),'applied':False})
