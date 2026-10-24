# R49: bounded cross-cell deposit lookup candidate

Source parent24b32f4dff107c2a8b9f369aea268eb04f20a29a. V4 Geology.Column.ore only
examines its own32-block deposit cell. A body's X semiaxis can reach13.6 blocks
from a center8..24 inside the cell, so eligible material extending into a neighbor
is omitted by that lookup. This is reproduced in1072 core lookup witnesses, not
inferred from an image. First witness: seed0, x=-4064,z=-143,y=17, COPPER under
the declared deep-bed test input; legacy lookup returns NONE. This is a core
predicate witness, not a claim that a native world block at that point is ore
rather than a cave/soil/surface block.

Two staged core files add a DepositField and share the existing Geology host/shape
predicate without duplicating it. Build immutable per-column metadata for nine
neighboring source cells; each height query checks at most nine candidates with
no per-query allocation or chunk/world lookup. Query overlap order is absolute
grid X then Z, preserving deterministic priority at cell boundaries. Negative
coordinates use floor division and absolute center arithmetic uses long products.
The source shape bounds prove cells two steps away cannot contribute. Raw arrays
contain324 bytes per column, excluding object headers, host reference, allocator
and process/native/GPU peaks. Construction adds45 source hash calls per column;
these are work/storage bounds, not performance acceptance.

Two private validation files add the core test and its invocation. The wider
49-source oracle independently computes absolute centers, shapes and host
eligibility. Across90960 queries (including25000 signed cell-seam queries and five
seeds with long extremes), the candidate matches the oracle and deterministic
replay, preserves near-surface exclusion, and recovers1072 omitted legacy cases.
The tests do not change the published V4 behavior.

A separately compiled legacy harness runs against the retained pre-candidate core
and final built candidate core:4,280,000 geology/ore/rock results per version have
the same SHA256 e2fb13bb90dcb3e49a56a0d890e9387acf6aa1116cc9de6acba025570890358d.
The baseline artifact SHA is in legacy-audit.json. To reproduce, build the immutable
parent core and candidate core, compile LegacyOreDigest.java against the baseline,
then execute it with each core JAR plus the harness classes on the classpath.

The final forced31-task full build passes the new explicit deposit record and all
existing water accounting, R48 runtime compilation, V4/legacy terrain, watershed,
cache and module gates. An intermediate full build also passed; after code review,
array metadata was filled before constructing the immutable Column, and the final
full build was repeated. Those are two candidate builds, not independent native
controls. Raw compressed consoles retain their original hashes. Seven archive CRCs
and477 compiled product-class identities pass; private checks are absent from the
bundle. There were no failed checks or native game launches in this checkpoint.

The prototype remains staged in staged/. Public production source, generator
registration, default packs, V1–V4 world output and owner installers are unchanged.
It is not called by any active worldgen path. Applying the future field changes
deposit distribution/overlap priority, so integrate only behind a new explicitly
saved terrain revision; never silently switch existing V4 saves. The Geo predicate
refactor is included in the candidate only and its legacy equivalence is tested.

Next wire an opt-in versioned generator and validate native base-column versus
chunk generation, neighbor/chunk order, saved revision roundtrip, real host/cave/
soil interactions, density/overlap balance and construction/query/allocation cost.
No native visual, frame/FPS, owner-GPU or terrain-roadmap completion follows here.
Deposit datapack/editor authoring and broader geology-linked caves remain unfinished;
catalog/species work remains deferred. Preserve water quantities, edits and modular
Vulkan/OpenGL/LOD. No installer was released.

Reconstruction: checkout the immutable parent, verify source-manifest.json's679
Git input hashes (independent Maritime, standalone Themes wrapper/ignore and docs/
workflow files are outside this engine snapshot), then apply the four files in
staged-manifest.json. build.py records the retained JDK25/JDK21/Gradle9.2.1 invocation.
Its local paths refer to the retained validation workspace; scripts/sealed source,
raw hashes and exact build/package results are preserved for review.
