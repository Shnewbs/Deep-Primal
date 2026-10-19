# Deep Primal

A standalone survival total conversion for Minecraft, starting with an experimental world-generation foundation.

**Current build: 0.0.1-alpha.7 worldgen prototype — Minecraft 26.1.2 / NeoForge 26.1.2.122 / Java 25.**

Deep Primal is the default normal world generator. Extra underground depth is a startup config setting, rather than a second selectable world type. Alpha 4 replaced sheet-shaped mountain carving with buried tunnels and deeper chambers, and added an independent vertical render radius. Alpha 5 fixed native chunk-task queue bounds at extended render distances. Alpha 6 adds an authored macro-terrain horizon up to 256 chunks, automatic renderer handoff, and a localhost performance dashboard. The prototype retains vanilla placeholder materials, geological strata, native aquifers, XP suppression, bounded diagnostics, expanded horizontal rendering, and the alpha 3 client/server optimizations. Rendering uses OpenGL. The full survival conversion remains in design. Use disposable test worlds. Alpha 6 can reopen alpha 4/5 worlds without migration; generated voxel terrain is unchanged.

- [Install, play, and build](docs/WORLDGEN-PROTOTYPE.md)
- [Alpha 7 adaptive rendering validation](docs/VALIDATION-0.0.1-alpha.7.md)
- [Alpha 6 renderer and diagnostics validation](docs/VALIDATION-0.0.1-alpha.6.md)
- [Renderer and dashboard guide](docs/RENDERER-AND-DIAGNOSTICS.md)
- [Alpha 5 crash-fix validation](docs/VALIDATION-0.0.1-alpha.5.md)
- [Alpha 4 validation](docs/VALIDATION-0.0.1-alpha.4.md)
- [Alpha 3 distance/performance validation](docs/VALIDATION-0.0.1-alpha.3.md)
- [Original baseline](docs/VALIDATION-0.0.1.md)
- [Roadmap](roadmap.md)
- [Owner decisions](docs/design-decisions-2026-10-18.md)
- [Detailed design reference](docs/design-reference.md)

Build with `./gradlew build` or `gradlew.bat build`. Output: `build/libs/DeepPrimal-26.1.2-0.0.1-alpha.7.jar`.

Create a new world normally; its default world type is **Deep Primal**. Prefer Creative with commands enabled for inspection. Set `extraUndergroundDepth = true` in `config/deepprimal-worldgen.toml` and restart before creating a new world to extend Y=-128..639 to -384..639. Saved worlds retain their height. Try `/deepprimal info` and `/deepprimal bench 200 200 1`.

Worldgen performance is a mandatory development priority. Initial measurements are observational baselines, not proof of release-grade performance. Dedicated-server tests, visual checks, and remaining limitations are recorded in the validation report.

The mod does not require TerraFirmaCraft. Referenced projects provide design inspiration; no source or assets from TFC, Terralith, or other reference mods are bundled.

Open **Video Settings → Deep Primal distances…** for typed values and sliders. Horizontal render: 2–96 chunks; simulation: 2–32; vertical render: 0–96 sections (0 = full height, each section = 16 blocks above/below the camera). Vertical limits reduce render-section allocations and mesh work; chunk loading/networking and server simulation remain full-height columns. Servers control delivered terrain and simulation. Reopen a local world when raising horizontal render range above its allocated session capacity.

The built-in horizon defaults to 192 chunks and supports 0–256. It draws coarse generated landscape, with no extra full chunk tickets, trees, entities, or player builds. Open **Mods → Deep Primal → Config → Local performance dashboard**, or the same button in Deep Primal distance settings. The read-only dashboard normally uses `http://127.0.0.1:8787/`; additional game instances choose a free nearby port. Vulkan and a complete replacement for native near-block meshing are future renderer stages.

Alpha 7 closes horizon tile edges to the saved-world floor, adds a near-terrain closure and stitched 16/32/64-block sample grids, and selects prebuilt detail levels by projected sample size and workload pressure. A single bounded worker builds immutable mesh snapshots; GPU uploads stay on the rendering thread. Client requests and server sampling back off independently under frame, tick and heap pressure. The server coalesces matching in-flight requests and rotates sampling fairly. Payload-byte and entry ceilings bound retention.

Open **Deep Primal distances → Performance…** for adaptive work, target FPS and retained vertex-memory controls. The dashboard adds CPU mesh/upload/submission measurements and an asynchronous horizon-pass GPU timer on supported OpenGL devices. This alpha remains OpenGL. Vulkan, a replacement near-block renderer, edited-world LODs, GPU-driven occlusion and broad hardware performance certification are not implemented.
