# dev31 delivery and next-stage checkpoint

Qualified production source: `a1583a508b0d67130d387bc018defd5758acd496` on main. Final root-JAR producer ran after promotion, completed successfully (27 tasks: 1 executed, 26 up-to-date), and reproduced the exact candidate SHA256 tested by all ten native sessions. It does not replace or relabel the earlier successful full build. Updated package evidence identifies this exact source. SHA256: `d1823835f3c2a8c54217edfcf7349df379375df29f0714707c8d028aaaeac876`; size 125,296,337 bytes.

Testing download: https://chatgpt.com/library?file_id=libfile_04a8b79f273c8191907d6c704c833a13&account_id=personal

The save succeeded with metadata applied. Owner retention keeps this dev31 JAR, renderer.5 and the 16:27 audit JAR; renderer.4 and renderer.3 were moved to trash by their resolved stable IDs. Art/source/evidence were not part of retention. GitHub prerelease create/upload remains unavailable; no GitHub release was published.

## Additional implementation after delivery qualification

Reproduced an unbounded prelude in the actual current source: even with source admission already expired, an 8,192-stream index performs 8,192 map lookups and 127,948 headwater reads before reaching a deadline check. This is an operation-count reproduction with a controlled clock, not a latency benchmark.

An independently authored guard is preserved in `next-stage/HydrologyRuntime.java` and `next-stage/admission-prelude.patch`. It skips the source-index scan/sort when source admission has already closed while retaining the later regular-accounting loop. The extracted actual baseline/candidate preludes each passed ten open/expired-deadline cases (0, 1, 64, 1,024, 8,192 streams); candidate open-deadline counts and descending order match baseline, expired cases perform zero prelude work. The candidate harness also passed under Python -OO.

This overlay is NOT in production or the delivered JAR. Full Minecraft API compilation, core/module/build and packaged native gates remain required before promotion. It does not bound a scan/sort that begins before expiry and does not solve per-callback latency or cross-region fairness. Next combine the admission guard with a properly invalidated/incremental source index, preserve headwater ordering and finite quotas, and exercise load/unload/reload and budget transitions. World-ledger transfer/catch-up, V8 compatibility, real GPU surfaces and long-soak gates remain open.

Large input-manifest evidence is stored in ordered `.part0`, `.part1`, etc. Concatenate in numeric order to reconstruct the original JSON. Last complete external grade remains 50/100 (5.0/10); no new complete regrade or completion claim.
