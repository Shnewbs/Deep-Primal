# R50: opt-in V5 deposit integration and footprint pruning

Source parent `75cdb6f057cacdccac41fc4155d0621b747d5822` (public main after
R49). Eight files are staged here: five production candidates and three private
checks. Public production paths, default presets, supplied installers and saved
V1–V4 worlds remain unchanged. Nothing in this checkpoint promotes V5.

The candidate registers a distinct `deepprimal:continental_v5` codec. Its terrain
shape, water, caves and surface rules remain V4; only its ore lookup uses the
neighboring deposit field. Both `getBaseColumn` and `fillFromNoise` construct one
field per column and reuse it for heights. A field wrapper is shared across a
chunk's columns. The native runner supplies a private world preset selecting V5;
the shipped normal/deep presets continue selecting V4. V5 is explicitly saved,
rather than silently changing V4 saves. This is a candidate version, not a newly
delivered/frozen terrain release.

R49's nine-source query repeated shape work for sources unable to reach the
column. A new immutable nine-bit footprint mask rejects those at construction.
Every term is nonnegative, so adding the vertical squared distance cannot turn a
horizontal sum greater than one into an intersection. Surviving queries retain
the exact original predicate arithmetic and absolute-grid X/Z overlap priority.
No per-query allocation, hashing, chunk access or world access is added. Source
capacity stays nine and arrays retain 324 raw bytes; the mask has an additional
object-layout cost measured below. Geology's legacy predicate remains unchanged
from the R49 staged refactor.

## Final checks

- Final forced, nonincremental 31-task full Linux-Vulkan bundle build passes all
  existing core/accounting/terrain/watershed/cache/module gates and the 90,960
  deposit oracle cases. Two earlier full builds also pass; their artifacts and
  source states are distinguished under `pre-pruning/` and `pre-fresh-build/`.
- Seven archive CRCs and 477 compiled product-class identities pass. Private
  validation classes are excluded. Native standalone core members equal the
  embedded core, and the final package remains byte-identical across the fresh
  build/native reopen/audit. This is a Linux test artifact, not an owner installer.
- A fresh packaged public-baseline control generates 96 chunks across revisions
  1–4, two seeds and twelve sites, checking 6,714,822 base-column blocks. All 96
  complete chunk digests match the candidate. Tests include signed cell/chunk
  boundaries and terrain-valid missing-neighbor ore witnesses.
- Candidate native generation checks 8,393,529 base-column blocks. Its wider
  49-source independent oracle matches 1,305,522 actual underground block states.
  There are 58 material changes: 35 added emerald ore blocks at seed zero and 23
  added copper ore blocks at seed 20261020 in this selected sample. First change:
  seed0, x=-1184,y=-11,z=-506, calcite to deepslate emerald ore. These are native
  generated blocks, unlike R49's deliberately declared deep-bed core witness.
- All 3,406,926 protected states match V4, including 80,204 cave-air blocks,
  259,565 native-water blocks, 21,128 soil-band blocks and 12,288 bedrock-band
  blocks. Those subsets can overlap. Twenty-four reversed-order chunks match
  forward generation; columns are checked on both sides of signed source seams.
- An actual V5 world is created with the private preset, saved and reopened. The
  fresh final reopen verifies revision5, a player gold-block edit, and 240,260
  underground blocks in three actual FULL saved chunks against fresh generation.
  Final baseline and reopen processes require explicit records, runtime fixture
  hashes, requested stop, all-dimensions saves, matching class origins and zero
  crashes. Full session evidence is in `strict-native-audit.json`; earlier
  creation/reopen records are retained separately.

These are bounded dedicated-server tests, not a rendered scene or independent
60Hz simulation. Bundling Vulkan on the server does not exercise a graphics
driver, establish Windows compatibility or certify owner GPU/FPS.

## CPU cost probe and limits

`DepositCost.java` uses 8 warmups and 9 alternating measured batches in each of
three fresh processes per variant (six interleaved processes). Each batch builds
8192 escaping columns and queries 384 heights per column. Both variants return
the same query checksum5064; legacy-host control timings remain about3.93ns/query.
The unpruned field median is35.28ns/query; footprint pruning is2.09ns/query
(-94.07%) for this workload. Its 8192 columns contain6843 empty,1348 one-source and
one two-source footprint, so this is a sparse-footprint microprobe, not general
world-generation/frame/FPS acceptance. No simultaneous Gradle/native job ran
during these measurements; no CPU isolation or broad hardware guarantee follows.

Construction rises from409.65 to436.49ns/column (~6.55%). Thread allocation for
escaping host+field rises472 to480bytes/column: legacy host alone88, final field
increment392 (eight more than unpruned). Raw metadata arrays are324bytes; object
headers/padding account for the additional measured cost. The probe's reference
array is allocated outside the measured interval. Both query paths allocate zero
measured thread bytes. This is not native generation allocation or process/GPU
peak memory. Matched whole-chunk timing/allocation and denser overlap cases remain
open. All raw batches, commands, exact core-JAR hashes and checksums are sealed.

## Rejected or limited attempts

Four attempts are excluded from final acceptance, with their sources/logs retained:
a private switch-expression syntax error before Minecraft; a vanished temporary
generated Themes resource before Minecraft; an actual game assertion crash
because the first selected sites contained no changed deposits (Gradle still0);
and an expanded baseline whose source had twelve sites but compiled private class
still had eight. The last run is valid only for its original eight sites and is
not the final expanded control. Its bytecode and original console are retained.
Causes of the resource disappearance and stale compilation remain unproven.

Final builds disable Java incremental compilation with the local
`fresh-compile.gradle`, clear only the isolated validation outputs before retry,
and record/compare the executing fixture's class hash. The fresh baseline has96
digests and the final candidate has120, rather than trusting a task's exit status
or source alone. The final fixture also records protected-state subcategories;
the earlier fresh-baseline fixture source is retained for exact review. No
speculative production workaround was made for either artifact issue.

## Continuation and reconstruction

The cross-cell functional, codec and scoped native persistence checks pass. Broad
ore/host/density/overlap balance is **not** accepted: this native sample adds only
two ore types and is intentionally selected around seam witnesses. Native client
visual transects, all-ore/host ensembles and matched full chunk generation/peak
allocation checks are next before promotion. Authorable deposits, wider material
catalogs and geology-linked cave mechanics remain unfinished; catalog/species
work stays separately deferred. Bounded Vulkan water publication, actual seamless
streams/oceans, enabled-view LOD and registered water/pressure gates remain open.
No installer, performance superiority or engine-completion claim follows.

Reconstruct from the immutable parent, verify the679 engine/Themes input hashes
in `source-manifest.json`, then apply `staged-manifest.json`'s eight files. Those
inputs exclude docs/workflows, independent Maritime and standalone wrapper files.
Use `build.py`, `server.py baseline-verified` (an isolated unmodified public-source
project), `server.py v5-initial`, and `server.py v5-verified-reopen`. Server runs use
root-task dispatch, disposable worlds, pinned JDK25/JDK21/Gradle9.2.1 and the
retained build-only NeoForm shim. `profile.py` compares final core to the archived
pre-pruning core; rebuild that control from the same core files with
`pre-pruning/DepositField.java` substituted. `audit_final.py` seals source,
archive, native, runtime-class and cost gates. Local paths in scripts identify
the retained environment, not a distributable toolchain or required installation.
