# Validation — 0.0.1-alpha.3

Minecraft 26.1.2 / NeoForge 26.1.2.122 / Java 25. This is an experimental alpha, with OpenGL rendering. Vulkan is assessed separately in [VULKAN-BACKPORT.md](VULKAN-BACKPORT.md); no Vulkan renderer is shipped or claimed.

## Implemented and exercised

**Typed distances and sliders:** Video Settings has a Deep Primal distance screen, with staged edits, Apply, Cancel, validation and persistence through native `options.txt`. Render values span 2–96; full simulation spans 2–32. The native video sliders use the same expanded ranges. Multiplayer server authority remains intact.

**Actual extended render tracking:** the client-information field is a signed byte and the native tracking maps also use byte levels, so this alpha selects a finite 96-chunk ceiling. The native simulation graph is a separate constraint. Tracking capacity is allocated on world/server startup, at least 32, growing only when a larger render range was chosen. Normal settings retain native graph capacity. A single-player increase beyond that allocation requires reopening the world, and the screen says so. Effective local rendering is clamped to the allocated capacity until then. Dedicated view/simulation inputs are bounded before being advertised to clients.

**Camera relocation:** unchanged X/Z columns are checked once rather than checking every vertical render section. Vertical-only movement avoids the column scan while still updating camera state and invalidating the occlusion graph. Changed columns retain native section updates. The first relocation is explicitly processed even if the spawn happens to equal the constructor's sentinel camera coordinates. A client config toggle restores native behavior.

**Chunk sends:** each player has a reusable primitive nearest-K selector with capacity 64. Native feedback and acknowledgements remain active; the default per-tick batch maximum is 16, including integrated-server connections. This replaces full-queue sorting/bursts on the native local-connection path, and avoids boxed position selection on the remote path. It does not discard pending chunks or change saved terrain. The native path remains configurable.

**26.3 persistent-mob idle behavior:** distant persistent mobs preserve accumulated idle time, so native random-stroll/swim goals can stop. Nearby players reset the timer; persistence and despawning remain native. Forced movement and other AI are not globally suspended. The behavior is configurable because this is an intentional change from the older persistent-mob idle behavior.

## Checks

- Clean build and platform-neutral core boundary verification.
- Existing 200,000 surface-height cases and cache/tick-ring checks.
- 3,000 randomized nearest-K differential cases against an independent sorted reference, including large/negative coordinates, ties and queue bounds.
- Numeric input/range checks and 34,300 ring-grid coordinates.
- Both tall terrain presets: fresh worlds, FULL requests, bedrock floors, save/flush, restart, matching sampler results, cache invalidation and cancellation. All geometry JSON and the existing height solver are unchanged from alpha 2; the geometry resources were checked byte-for-byte.
- Dedicated server configured with view distance **33**; a real graphical client loaded its render setting **33** and received a chunk at offset **33** from its current chunk, beyond the prior 32 limit.
- Native option values **96 render / 2 simulation** saved and reloaded. Widget input above the supported ranges disables Apply. Staged edits and Cancel preserve the live setting; Apply commits valid values and persists them.
- All **107,736** live render-section node positions match native relocation after horizontal, vertical, negative-coordinate and long-teleport moves. The constructor-sentinel first-move case is also compared with native behavior.
- Single-player/integrated-server checks verify startup capacity 33, delivered chunk 33, settings persistence and the local effective-distance cap, including an artificially higher advertised server radius. The integrated memory-connection path also stays within the 16-chunk batch cap.
- Live persistent-mob timer: a distant named zombie reached idleTicks=420; when moved beside the player it reset to idleTicks=3. It remained persistent. Temporary force-load was removed.

The delivered-radius test exercises 33. Values up to 96 pass range/persistence checks, but the full 96-chunk scene is not a completed hardware stress test. No unlimited mode or broad frame-rate gain is claimed.

## Focused CPU measurement

On the final integrated-client check, 30 one-section horizontal moves across a 33-chunk view area took **58.25ms total with native relocation** and **13.87ms total with optimized relocation**: approximately **4.2× faster for this operation**. Both paths were warmed and used the same live render area. This is one observational CPU measurement, not an overall FPS benchmark or confidence interval. Earlier runs varied in absolute timing while retaining the same direction of improvement.

The dedicated-client session recorded **263 selections**, **58,517 queued candidates inspected**, **23ms cumulative selector time**, and a largest send batch of **16**. These counters show bounded delivery under the test workload; they do not isolate a whole-game before/after improvement.

Environment: shared Linux container, 8 available processors, AMD EPYC 9V74 host, Java 25, 2 GiB client/server heap limits. The graphical test uses Mesa llvmpipe software rendering in a virtual display. Real GPU frame-time/VRAM tests, multi-player contention and large 96-chunk scenes remain open.

## Reproduction

`./gradlew clean build` includes the core checks. `tools/server_smoke.py` exercises the tall presets; see the existing prototype instructions.

For the distance check, create a disposable flat test world/server with view-distance 33 and simulation-distance 3, and start it with the optional validation source set:

```sh
./gradlew runServer -PvisualValidation -PrunDir=run-distance-server
./gradlew runClient -PvisualValidation -PdistanceValidation -PcaptureServer=127.0.0.1:25565 -PclientRunDir=run-distance-client
```

The server EULA must be accepted before use. Put `renderDistance:33` in the test client's `options.txt` before launch. The client waits for a chunk at radius 33, exercises the actual controls/persistence/native relocation fallback, captures the screen and exits. The validation-only `/deepprimaltest idle <entity>` command reads mob idle time on the test server. To test single-player, place the test world in `saves/DistanceChecks` and use `-PcaptureWorld=DistanceChecks` instead of `-PcaptureServer`.

These validation entry points, assertions and auto-capture code are excluded from the distributed JAR. Raw sampler, smoke, runtime and renderer-audit evidence is in [validation/0.0.1-alpha.3](validation/0.0.1-alpha.3/).

Artifact: `DeepPrimal-26.1.2-0.0.1-alpha.3.jar`. SHA-256: `f49e11162a0e4ad4c9c00ebb22d97e47f96749560acc4c10c0fe9e7e0d35e4ff`.
