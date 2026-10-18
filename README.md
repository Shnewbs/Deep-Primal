# Deep Primal

A standalone survival total conversion for Minecraft, starting with an experimental world-generation foundation.

**Current build: 0.0.1-alpha.3 worldgen prototype — Minecraft 26.1.2 / NeoForge 26.1.2.122 / Java 25.**

The prototype provides two tall-terrain world presets, vanilla placeholder materials, geological strata, native aquifer/cave placeholders, XP suppression, and bounded diagnostic/benchmark commands. Alpha 3 adds numeric distance fields and sliders, expanded render range, bounded chunk delivery, faster camera-grid updates, and the 26.3 persistent-mob idle optimization. Rendering currently uses OpenGL. The full survival conversion remains in design. Use new disposable test worlds.

- [Install, play, and build](docs/WORLDGEN-PROTOTYPE.md)
- [Alpha 2 validation and comparison](docs/VALIDATION-0.0.1-alpha.3.md)
- [Original baseline](docs/VALIDATION-0.0.1.md)
- [Roadmap](roadmap.md)
- [Owner decisions](docs/design-decisions-2026-10-18.md)
- [Detailed design reference](docs/design-reference.md)

Build with `./gradlew build` or `gradlew.bat build`. Output: `build/libs/DeepPrimal-26.1.2-0.0.1-alpha.3.jar`.

Create a world using **Deep Primal — Terrain Prototype (768)**, preferably in Creative with commands enabled. The 1,024-block-deep comparison preset is also available. Try `/deepprimal info` and `/deepprimal bench 200 200 1`.

Worldgen performance is a mandatory development priority. Initial measurements are observational baselines, not proof of release-grade performance. Dedicated-server tests, visual checks, and remaining limitations are recorded in the validation report.

The mod does not require TerraFirmaCraft. Referenced projects provide design inspiration; no source or assets from TFC, Terralith, or other reference mods are bundled.

Open **Video Settings → Deep Primal distances…** for typed values and sliders. Render: 2–96 chunks; simulation: 2–32. Servers control delivered terrain and simulation. Reopen a local world when raising render range above its allocated session capacity.
