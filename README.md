# Deep Primal

A standalone survival total conversion for Minecraft, starting with an experimental world-generation foundation.

**Current build: 0.0.1-alpha.1 worldgen prototype — Minecraft 26.1.2 / NeoForge 26.1.2.122 / Java 25.**

The first compiled prototype provides two tall-terrain world presets, vanilla placeholder materials, geological strata, native aquifer/cave placeholders, XP suppression, and diagnostic/benchmark commands. The full survival conversion remains in design. Use new disposable test worlds.

- [Install, play, and build](docs/WORLDGEN-PROTOTYPE.md)
- [Validation and initial measurements](docs/VALIDATION-0.0.1.md)
- [Roadmap](roadmap.md)
- [Owner decisions](docs/design-decisions-2026-10-18.md)
- [Detailed design reference](docs/design-reference.md)

Build with `./gradlew build` or `gradlew.bat build`. Output: `build/libs/DeepPrimal-26.1.2-0.0.1-alpha.1.jar`.

Create a world using **Deep Primal — Terrain Prototype (768)**, preferably in Creative with commands enabled. The 1,024-block-deep comparison preset is also available. Try `/deepprimal info` and `/deepprimal bench 200 200 1`.

Worldgen performance is a mandatory development priority. Initial measurements are observational baselines, not proof of release-grade performance. Dedicated-server tests and remaining client/visual limitations are recorded in the validation report.

The mod does not require TerraFirmaCraft. Referenced projects provide design inspiration; no source or assets from TFC, Terralith, or other reference mods are bundled.
