# Watershed constructor and build audit

Audited source: main at `7b16d12b4d39f453e4ea61a7f125127d11360866`.

## Reproduced failure and repair

Java 25 reproduced the owner's complete 14-error failure in `WatershedRegion`.
The configuration integration had replaced the full constructor without retaining
the revision-10 overload or assigning its five final fields: `terrainResolved`,
`sourceHeight`, `channelized`, `pathX`, and `pathZ`.

The constructor chain now joins configuration and saved routing policy in one
canonical constructor. Older overloads explicitly retain legacy routing and do
not allocate the revision-10 routing arrays. The revision-10 overload used by the
existing drainage fixtures is restored. Invalid combinations with legacy lake,
delta and oxbow templates remain rejected.

`TerrainEngine` now passes revision 10 explicitly and excludes revision-8/9 lake
sculpting in that mode. Existing configuration values and revisions 0–9 retain
their meaning relative to the audited main snapshot.

## Additional audit fixes

- The full core suite exposed a real shoreline regression: the spatial sand
  selector had been replaced by elevation/climate thresholds alone. The constant
  shore fixture produced zero sand columns. Restoring its coherent world-space
  selector preserves the newly added intermediate sandy-soil classes while
  restoring a mixed shoreline; the original regression assertion is retained.
- The material-band source guard no longer requires an obsolete dev.108 release
  string and resolves its source path independently of the working directory.
- The vegetation identity guard follows the current profile-bound receipt
  constructor, retaining the existing chunk-identity and count checks.
- The ledger replay fixture reports rejection of a valid exact replay as an
  assertion failure. Its duplicate-replay mutant previously reached an uncaught
  `IllegalArgumentException`, so the deliberately strict mutation runner could
  not identify a fixture assertion. The runner's distinction between fixture
  assertions and compiler/launcher failures is preserved.

## Focused validation

- Organic drainage: 816,677 assertions including eight new constructor checks,
  eight real engine seeds, conserved runoff, terrain-fitted knots, connected wet
  reaches, unmodified basin beds and deterministic sampling.
- Saved revisions 0–9: 640 columns / 6,400 bit-exact fields. Since audited main
  cannot compile, its reference constructor receives only false/null field
  initializations to expose the existing legacy branch. This comparison is not
  a claim of equivalence to an older release before the configuration changes.
- Python discovery: 74 unit tests pass, with the import-time source guards also
  passing. Many other scripts have explicit `main` runners and are not included
  in that discovery count.
- Explicit runners: watershed interior routes, outlet topology, region-port
  bridge, conserved outlet ledger and durable outlet store. Positive runs and
  their compiled broken controls pass; see the result summary.
- Shoreline regression: 1,024 fixed-condition columns now include 129 sand and
  895 non-sand columns, with 1,920 / 1,984 adjacent pairs matching. Frozen legacy
  soil replay passes 150,001 checks. No test thresholds were relaxed.
- The separate `standalone-engine` build/check and native-check source
  compilation also pass against the actual NeoForge dependencies.

## Reproduction

The complete main `build compileVisualValidationJava` run passed in 6m 47s,
including `coreChecks`, Vulkan cache checks, module-boundary checks, and the
119,922-assertion ground-resource check against all 11 owner-image hashes.
All production modules, including Vulkan, compiled and packaged successfully.
The Windows Vulkan `assemble -PvulkanPlatform=windows` run also passed in 14s;
the resulting archive passes CRC and nested-module/native-DLL inspection.

Use Java 25 and the repository's Gradle 9.2.1 wrapper:

```powershell
git pull --ff-only origin main
.\gradlew.bat build compileVisualValidationJava
python -m unittest discover -s tools -p "test_*.py"
```

For a Windows Vulkan package, add `-PvulkanPlatform=windows` to the Gradle command.

This audit uses the actual Minecraft 26.1.2 / NeoForge 26.1.2.122 dependencies.
The restricted Linux validation host additionally uses the repository's private
NeoForm process-command compatibility agent and current-proxy helper; neither
changes production code or ships inside the mod.

This is a compilation, packaging and automated-regression audit. Native client
visual acceptance, GPU behavior and Windows gameplay are separate validation
gates. Existing Vulkan `sun.misc.Unsafe` removal warnings remain.
