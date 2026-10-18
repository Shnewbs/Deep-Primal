# Deep Primal

A standalone survival total conversion for Minecraft, starting with an experimental world-generation foundation.

**Current build: 0.0.1-alpha.5 worldgen prototype — Minecraft 26.1.2 / NeoForge 26.1.2.122 / Java 25.**

Deep Primal is the default normal world generator. Extra underground depth is a startup config setting, rather than a second selectable world type. Alpha 4 replaced sheet-shaped mountain carving with buried tunnels and deeper chambers, and added an independent vertical render radius. Alpha 5 fixes native chunk-task queue bounds at extended render distances, including 96 chunks. The prototype retains vanilla placeholder materials, geological strata, native aquifers, XP suppression, bounded diagnostics, expanded horizontal rendering, and the alpha 3 client/server optimizations. Rendering uses OpenGL. The full survival conversion remains in design. Use disposable test worlds. Alpha 5 can reopen alpha 4 worlds without migration; terrain and rendering resources are unchanged.

- [Install, play, and build](docs/WORLDGEN-PROTOTYPE.md)
- [Alpha 5 crash-fix validation](docs/VALIDATION-0.0.1-alpha.5.md)
- [Alpha 4 validation](docs/VALIDATION-0.0.1-alpha.4.md)
- [Alpha 3 distance/performance validation](docs/VALIDATION-0.0.1-alpha.3.md)
- [Original baseline](docs/VALIDATION-0.0.1.md)
- [Roadmap](roadmap.md)
- [Owner decisions](docs/design-decisions-2026-10-18.md)
- [Detailed design reference](docs/design-reference.md)

Build with `./gradlew build` or `gradlew.bat build`. Output: `build/libs/DeepPrimal-26.1.2-0.0.1-alpha.5.jar`.

Create a new world normally; its default world type is **Deep Primal**. Prefer Creative with commands enabled for inspection. Set `extraUndergroundDepth = true` in `config/deepprimal-worldgen.toml` and restart before creating a new world to extend Y=-128..639 to -384..639. Saved worlds retain their height. Try `/deepprimal info` and `/deepprimal bench 200 200 1`.

Worldgen performance is a mandatory development priority. Initial measurements are observational baselines, not proof of release-grade performance. Dedicated-server tests, visual checks, and remaining limitations are recorded in the validation report.

The mod does not require TerraFirmaCraft. Referenced projects provide design inspiration; no source or assets from TFC, Terralith, or other reference mods are bundled.

Open **Video Settings → Deep Primal distances…** for typed values and sliders. Horizontal render: 2–96 chunks; simulation: 2–32; vertical render: 0–96 sections (0 = full height, each section = 16 blocks above/below the camera). Vertical limits reduce render-section allocations and mesh work; chunk loading/networking and server simulation remain full-height columns. Servers control delivered terrain and simulation. Reopen a local world when raising horizontal render range above its allocated session capacity.
