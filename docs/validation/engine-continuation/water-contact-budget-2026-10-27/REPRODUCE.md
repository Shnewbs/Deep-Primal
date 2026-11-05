# Reproduction

Use a new private directory and Java 25. The stage contains four candidate
sources. Copy the prior contact boundary fixture from the pinned source at
4aa62dd72fe72090efc07630d8ee3a1a5c46accd; the new budget checks are independent.
The scripts in this directory retain the exact private paths used for this run;
substitute only a new owned root and verified immutable dependency snapshot.
Never run them against another writer's mutable outputs.

1. make_fixture.py creates boundary doubles. Compile fixture/**/*.java,
   WaterAccount, WaterContactBudget, WaterContactRuntime, ContactChecks and
   WaterContactBudgetChecks into private classes. Run both check main classes
   with -Xverify:all and private java.io.tmpdir.
2. For full core, verify all 139 baseline sources against core-source-snapshot.json
   and the committed 0230 auditor source-identity.json. Overlay WaterAccount and
   add WaterContactBudget; compile all 140. For the final combined snapshot,
   apply the four exact source blobs listed in late-main-reconciliation.json
   from 2e1aa76 (three additions and the CoreChecks update); compile all 143. Run CoreChecks with private temp.
   Its final success line is PASS: ... differential surface cases, bounded-cache
   stress, tick-ring rollover. An exit code or partial log alone is insufficient.
3. api_compile.py verifies/copies completed immutable dependencies and compiles
   the four candidate sources against the actual Minecraft/NeoForge API.
4. prepare_native.py creates a PRIVATE DIAGNOSTIC overlay and separate fixture;
   split_native.py avoids duplicate nested extraction. These archives are not
   user distributions. run_native.py fresh then reopen requires loaded hashes,
   exact page-boundary parcels, real solver tick, native budget cap, codec,
   acknowledged save-all flush, clean shutdown and exit 0; errors take precedence.
5. Preserve evidence and delete only completed owned dependency/output/world
   copies after the exact source/evidence checkpoint is read back from main.

A full production Gradle build, module checks, full bundled package/native suite
and relevant real GPU/platform gates must pass before promotion or delivery.
