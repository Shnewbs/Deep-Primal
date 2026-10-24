#!/usr/bin/env python3
"""Run the renderer's actual admission/eviction blocks with dependency-free Java doubles.

This is a scheduler logic regression, not a Minecraft/GPU/native acceptance test.
Java source-file mode needs a JDK 17+ compiler module; the mod itself still needs JDK 25.
"""
import argparse
import hashlib
import json
from pathlib import Path
import re
import subprocess
import tempfile


def body(source, start):
    opening = source.index("{", start)
    depth = 1
    end = opening + 1
    while depth:
        depth += (source[end] == "{") - (source[end] == "}")
        end += 1
    return source[opening + 1:end - 1]


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--source", type=Path, default=Path(__file__).resolve().parents[2] /
                        "src/main/java/com/shnewbs/deepprimal/client/render/AnimatedWaterRenderer.java")
    parser.add_argument("--java", default="java")
    parser.add_argument("--json", type=Path)
    args = parser.parse_args()
    source = args.source.read_text()
    cap = int(re.search(r"MAX_PAGES = (\d+)", source).group(1))
    start = source.index("  private boolean evict(")
    opening = source.index("{", start)
    eviction = source[start:opening + 1] + body(source, start) + "}"
    eviction = eviction.replace("net.minecraft.world.phys.Vec3", "Vec3")
    discovery = source.index(".maybeHas(s -> s.is(EngineRegistry.WATER_BLOCK.get()))) {")
    admission = body(source, discovery)
    budget_start = source.index("    while (residentBytes + bytes > MAX_BYTES")
    budget_end = source.index("    ByteBuffer data =", budget_start)
    budget = source[budget_start:budget_end]
    harness = r'''import java.util.*;
public class PageAdmissionAudit {
  static final int MAX_PAGES = @CAP@;
  static final long MAX_BYTES = 32L * 1024 * 1024;
  static final Object EMPTY = new Object();
  record Vec3() {}
  record Bounds(double distance) { double distanceToSqr(Vec3 camera) { return distance; } }
  static final class Page {
    final long key;
    final Bounds bounds;
    boolean busy;
    boolean changed;
    long bytes;
    Object snapshot;
    Page(long key) { this.key = key; bounds = new Bounds(key); }
  }
  final Vec3 camera = new Vec3();
  final LinkedHashMap<Long, Page> pages = new LinkedHashMap<>();
  final LinkedHashSet<Long> fresh = new LinkedHashSet<>(), dirty = new LinkedHashSet<>();
  int evictions;
  long residentBytes;
  boolean accepted;
  void release(Page page) {
    evictions++; residentBytes -= page.bytes;
    dirty.remove(page.key); fresh.remove(page.key);
  }
  void admit(long key) { @ADMISSION@ }
  void reserve(Page p, long bytes) { accepted = false; @BUDGET@ accepted = true; }
  @EVICTION@
  static PageAdmissionAudit filled() {
    var h = new PageAdmissionAudit();
    for (long k = 1; k <= MAX_PAGES; k++) h.pages.put(k, new Page(k));
    return h;
  }
  static void check(boolean ok, String message) {
    if (!ok) throw new AssertionError(message);
  }
  public static void main(String[] args) {
    var h = filled();
    var initial = new HashSet<>(h.pages.keySet());
    h.admit(1_000_000);
    check(h.pages.keySet().equals(initial),
        "Farther discovery evicted a closer resident page with the cache full");
    check(h.evictions == 0 && h.dirty.isEmpty(), "Rejected discovery changed resident work");
    for (long k = 1_000_001; k < 1_000_513; k++) h.admit(k);
    check(h.evictions == 0 && h.pages.keySet().equals(initial), "Far discovery churn");
    h.admit(0);
    check(h.pages.size() == MAX_PAGES && h.pages.containsKey(0L)
        && !h.pages.containsKey((long) MAX_PAGES) && h.evictions == 1,
        "Closer discovery failed to replace the farthest resident page");
    check(h.dirty.contains(0L), "Accepted discovery was not queued");
    var busy = new PageAdmissionAudit();
    var near = new Page(1); var far = new Page(100);
    far.busy = true; busy.pages.put(1L, near); busy.pages.put(100L, far);
    check(!busy.evict(busy.camera, new Page(50)) && busy.pages.get(1L) == near,
        "Busy far mesh forced eviction of a closer page");
    far.busy = false;
    check(busy.evict(busy.camera, new Page(50)) && busy.pages.get(1L) == near,
        "Eligible farther resident was not evicted");
    check(!busy.evict(busy.camera, near), "Replacement evicted its own page");
    check(!busy.evict(busy.camera, new Page(1)), "Equal distance caused churn");
    var memory = new PageAdmissionAudit();
    var closeMesh = new Page(1); closeMesh.bytes = MAX_BYTES / 2;
    var busyMesh = new Page(100); busyMesh.bytes = MAX_BYTES / 2; busyMesh.busy = true;
    memory.pages.put(1L, closeMesh); memory.pages.put(100L, busyMesh);
    memory.residentBytes = MAX_BYTES;
    var incoming = new Page(50);
    memory.reserve(incoming, 1024);
    check(!memory.accepted && incoming.changed && memory.evictions == 0
        && memory.residentBytes == MAX_BYTES, "Memory pressure evicted a closer mesh");
    busyMesh.busy = false; memory.reserve(incoming, 1024);
    check(memory.accepted && memory.pages.get(1L) == closeMesh && memory.evictions == 1
        && memory.residentBytes + 1024 <= MAX_BYTES, "Memory admission did not recover");
    var fresh = new PageAdmissionAudit(); fresh.fresh.add(7L); fresh.admit(7);
    check(fresh.pages.get(7L).snapshot == EMPTY && !fresh.fresh.contains(7L),
        "Fresh-water transition was lost");
    var random = new Random(0x24CA5E);
    for (int test = 0; test < 2000; test++) {
      var r = new PageAdmissionAudit();
      for (int i = 0; i < 20; i++) {
        long k = random.nextInt(1000) + 1;
        var p = new Page(k); p.busy = random.nextBoolean(); r.pages.put(k, p);
      }
      var keep = new Page(random.nextInt(1000));
      long expected = -1;
      for (var p : r.pages.values())
        if (!p.busy && p.bounds.distance > keep.bounds.distance
            && (expected < 0 || p.bounds.distance > expected)) expected = p.key;
      var before = new HashSet<>(r.pages.keySet());
      boolean removed = r.evict(r.camera, keep);
      check(removed == (expected >= 0), "Random admission eligibility");
      if (expected >= 0) before.remove(expected);
      check(r.pages.keySet().equals(before), "Random victim priority");
      check(r.evictions == (removed ? 1 : 0), "Release accounting");
    }
    System.out.println("WATER_PAGE_ADMISSION_LOGIC_PASS maxPages=" + MAX_PAGES
        + " fartherDiscoveries=513 randomizedVictims=2000 busyProtection=true"
        + " memoryBudgetProtection=true memoryRecovery=true"
        + " replacementProtection=true equalDistanceStable=true freshTransition=true");
  }
}
'''
    harness = (harness.replace("@CAP@", str(cap)).replace("@ADMISSION@", admission)
               .replace("@EVICTION@", eviction).replace("@BUDGET@", budget))
    with tempfile.TemporaryDirectory(prefix="deepprimal-admission-") as directory:
        java_file = Path(directory) / "PageAdmissionAudit.java"
        java_file.write_text(harness)
        run = subprocess.run([args.java, "--source", "17", str(java_file)],
                             text=True, capture_output=True)
    record = {"scope": "extracted-production-scheduler-logic",
              "source_sha256": hashlib.sha256(source.encode()).hexdigest(),
              "exit_code": run.returncode, "stdout": run.stdout, "stderr": run.stderr,
              "full_mod_build": "not run: JDK 25/Gradle/native environment unavailable",
              "native_visual_acceptance": "not run"}
    if args.json:
        args.json.parent.mkdir(parents=True, exist_ok=True)
        args.json.write_text(json.dumps(record, indent=2) + "\n")
    print(run.stdout, end="")
    print(run.stderr, end="")
    return run.returncode


if __name__ == "__main__":
    raise SystemExit(main())
