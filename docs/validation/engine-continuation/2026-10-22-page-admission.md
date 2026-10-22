# Water page admission under cache pressure

Source baseline: `d021557dd9fc6a7e3d29d81a80b55a608fdbb3d6` (delivered alpha.24).
Run: 2026-10-22, isolated continuation after the foreground lease ended.

## Reproduced defect and focused change

Animated water globally suppresses the chunk water presentation while it owns the
renderer. Discovery at the 2,048-page limit called `evict(camera, null)` before
considering the incoming page's distance. The victim selection also allowed an
upload to evict a closer idle page when farther pages were busy. Thus a farther
request could remove an already covering closer mesh, even though there was no
per-page chunk fallback. This proves a scheduling defect; it does not identify
the cause of every distant pattern in the owner's screenshots.

Discovery now supplies the candidate page to eviction. Victim selection requires
a strictly greater camera distance than the candidate/replacement. Equal-priority
discoveries do not churn the cache. If all farther candidates are busy, the upload
retains the existing meshes and follows the existing bounded retry path. Limits,
worker counts, geometry, fluids, saves and supplied alpha.24 bytes are unchanged.

## Fresh checks in this run

`python3 tools/water/run_page_admission_check.py` extracts and compiles the actual
production discovery, eviction and upload-budget blocks into a dependency-free
Java fixture. Only the Minecraft Vec3 type name is substituted; page/bounds/release
doubles stand in for world/GPU state. The fixture uses the production page limit.

- The delivered source fails with `Farther discovery evicted a closer resident
  page with the cache full`; see `page-admission-before.json`.
- Patched source passes 513 farther overflow discoveries without residency churn,
  closer replacement, equal-distance stability, busy-mesh protection, byte-budget
  rejection/recovery, fresh-water transition preservation and 2,000 randomized
  victim selections; see `page-admission-after.json`.
- Both checks ran with OpenJDK 17.0.20 source-file compilation. This checks the
  extracted scheduler logic, not the complete JDK 25 mod or Minecraft adapters.

To repeat against the baseline, obtain AnimatedWaterRenderer.java at the commit
above and pass `--source /path/to/AnimatedWaterRenderer.java`. The script accepts
`--json /path/to/result.json` and `--java /path/to/java`.

## Blockers and acceptance still open

Workspace maintenance removed the prior checkout/toolchain. This isolated
directory has no JDK 25, Gradle 9.2.1, mapped Minecraft dependencies or native
display/GPU test environment. Full build/core/native/visual/performance tests
were not run. The earlier alpha.24 native evidence does not validate this change.
No installer is produced or merged into main by this continuation.

Nearest-first admission is only a focused reduction in avoidable coverage loss.
It does not make the entire enabled view fit a 32 MiB / 2,048-page budget, provide
fallback coverage for every unresident page, or solve mixed transparency. A dirty
upload denied memory can still retry; queue fairness and settling require native
measurement. Native teleport, edits, unload/reload and resource-reload checks are
still required before release. Restore the toolchain, run matched enabled-view
waves/animated/fallback/LOD scenes under pressure, and implement explicit coverage
handoff if the fixed-camera evidence demands it. Pressure continuation and regional
water budgets remain separate unfinished requirements. Published V1/V2/V3 terrain
is untouched.
