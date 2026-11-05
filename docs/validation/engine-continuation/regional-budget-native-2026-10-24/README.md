# R47: packaged validation of staged regional discharge fix

Parent aa71dd1f1dd570139612766ba17c00800965c12f. Current main engine/Themes source
inputs were restored with Git blob identity checks into an isolated directory.
Only the unchanged R46 candidate HydrologyRuntime replaced its production counterpart.
Independent Maritime, five standalone Themes wrapper/ignore files and repository
documentation/workflow files are outside this engine build snapshot. Current main,
Themes and historical V1–V4 geography were not overwritten.

One complete forced31-task Linux-Vulkan-bundle build passes, including core accounting,
R44 retirement, V4 terrain, full watershed, cache and module checks. The private fixture
is separate from release output. Seven archive CRCs and475 compiled product classes
match. The tested standalone core JAR exactly equals the embedded core; the packaged
root class origin is printed by the native checks. Archive bytes remained unchanged
through both accepted native runs. The test JAR was not delivered as an owner installer.

Two packaged dedicated-server processes pass explicit success records, exit zero,
all-dimensions saves, requested stop and zero crash reports:

1. Existing EnvironmentalWaterChecks passes shared outlets, blocked/head-limited output,
   infiltration, measured bucket chemistry/advection, fixed-stage ocean accounting,
   finite inland water, actual weather debit, codec and once-only initialization, and
   no forced unloaded destination. The added private RegionalCacheChecks uses actual
   loaded-world shafts and the production visit path. Eight accepted outlets emit32
   units. A real NeoForge ChunkEvent.Unload is then **injected** for an unrelated loaded
   chunk, with a controlled two-region/two-cursor cache population. The actual event
   handler clears the cache; eight more visits keep total emission at32, with exact
   groundwater+ocean+atmosphere+fixture-water conservation and unchanged aquifer interval.
2. A separate process reopens the same disposable saved world. Existing checks verify
   saved V4, groundwater, dynamic wet age, composition and player edit retention.

This is native Minecraft/NeoForge execution, but the cache population/event is controlled
fault injection. The chunk is not physically evicted from server storage/residency, so
physical unload/reload and natural multi-region churn/throughput remain unvalidated.
Server-start fixture calls execute synchronously; they are not independent60Hz ticks.
There was no graphics client, screenshot, Vulkan-driver/FPS, Windows or performance
test. Bundling Linux Vulkan does not make a dedicated-server test a Vulkan graphics pass.

The initial private fixture failed compilation before Minecraft started because this
version has no ServerLevel.setGameTime method. Its source and raw failed console are
retained. The fixture now uses the actual current game time and a prior-step aquifer;
the runtime candidate did not change. Both subsequent server runs pass. One failed
private compilation is not a product build failure or game crash.

Every raw log is gzip-compressed without content loss and its original SHA256 is in
build-result.json or the corresponding server result.json. source-identity.json records
source/class origins and package identity; package-audit.json records archive identities.
The source manifest plus immutable parent and R46 candidate reconstruct production
inputs. RegionalCacheChecks.java and unchanged EnvironmentalWaterChecks.java preserve
the private overlays. build.py/server.py/audit_package.py/seal.py record the exact local
commands and sealing process (their paths refer to this retained validation workspace).

The runtime fix remains staged. No additional runtime code was authored since R46 and
public production source/owner installer remain unchanged. Next test server-driven
physical eviction/reload, repeated unrelated-region churn, ordinary multi-region
throughput and constituent conservation before promotion. Then resume terrain/cave/
material authoring and bounded Vulkan water publication. Full engine acceptance is open.
