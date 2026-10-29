# Expanded replica — maritime-1.21.1/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/maritime-1.21.1/README.md) |
| Source identity | git: maritime-1.21.1/README.md |
| Preserved source SHA-256 | `66f52a6bf15ba7f9b53871e1e485641b338fb99427dfaf957d45d404c6aac98b` |
| Review method | full-text |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../README.md) · [Integrated design](../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../chapters/architecture-authoring.md) · [Discrepancy register](../../../DISCREPANCIES.md)

## Research expansion — source scope

The Maritime README describes a separate Minecraft 1.21.1/Java 21/NeoForge 21.1.264 port with TFC 4.2.12 and Patchouli 92, preserving the original tfc_item_barrels_and_more namespace. It identifies original supplied-JAR provenance, retains assets without shipping old classes, and lists optional Create, JEI, Curios, dynamic-light and Valkyrien Skies integration expectations. Its configuration already uses config/deepprimal/maritime/common.toml. This is an independent alpha project, not a module loaded by the incompatible root 26.1.2 installer. It does not promise old-save migration, complete optional integration or published release status.

## Research expansion — gaps and details to deepen

- The dependency list needs a versioned compatibility matrix with separate compile, startup, operational and save/reopen status. Merely having the Valkyrien Skies API present is insufficient when the actual physics backend/listener is unavailable. Optional integrations must therefore be admitted by verified capability rather than class existence alone.
- Retained namespace and asset provenance do not establish old-world compatibility or rights beyond the stated source. The port should record what was retained, rewritten and excluded, while preserving the original artifact identity. A target-platform mismatch should be visible before users follow root-project installation instructions.

## Research expansion — proposed contracts

- Add a project boundary card naming its independent toolchain, hard dependencies, optional capabilities, archive identity, configuration owner and unsupported migration path. Link it from the root module table as a separate project. Keep the existing domain path as an example of implemented organization without extrapolating that all other configs migrated.
- Specify adapter contracts for TFC heat/food/rotation and optional ship physics, including units, callback thread, coordinate frames and shutdown behavior. Record source and asset provenance in a machine-readable port manifest; treat tools or platforms studied for architecture as references unless deliberately selected as dependencies.

## Research expansion — acceptance checks

- Build and start the independent minimal dependency profile, then add each optional integration separately and in supported combinations. Verify absent or incomplete physics capability disables only its adapter without crashing common class loading or silently simulating unsupported forces.
- Audit the root installer to confirm Maritime classes and incompatible native dependencies remain outside it. Reopen new-format disposable saves and test actual machinery independently; do not use retained registry names or a placement smoke test as proof of old-save migration or operating behavior.

## Research expansion — source anchors

- TFC Maritime Travelers — Minecraft 1.21.1 port
- Runtime
- Build

## Research expansion — source-context links

These pinned links resolve original dependencies outside their new replica locations. Exact tracked repository paths also resolve original links with an incorrect relative base; the original text remains unchanged. Project-collection references use an identical Git counterpart, an explicit tracked path, or a unique tracked filename where available.

| Original relative target | Pinned source target |
| --- | --- |
| `NOTICE.txt` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/maritime-1.21.1/NOTICE.txt) |
| `upstream/provenance.json` | [Open](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/maritime-1.21.1/upstream/provenance.json) |

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# TFC Maritime Travelers — Minecraft 1.21.1 port

An independent development port on Deep Primal's `maritime-1.21.1` branch, reconstructed from the user-supplied 1.20.1 jar. The complete source now compiles and produces `TFCMaritimeTravelers-1.21.1-0.0.4-port.1-alpha.jar`. This is an **alpha for testing**; the validation scope and remaining work are recorded in [docs/PORT_STATUS.md](docs/PORT_STATUS.md).

The original `tfc_item_barrels_and_more` namespace and author attribution are preserved. This module builds independently of Deep Primal and has not been wired into its gameplay or main build.

## Runtime

Use Minecraft **1.21.1**, Java **21**, NeoForge **21.1.264**, TerraFirmaCraft **4.2.12**, and Patchouli **1.21.1-92-NEOFORGE**. Put the port, TFC and Patchouli jars in the instance's `mods` directory. Start with a disposable test world. This branch does not provide a 1.20.1 world upgrade tool.

Optional integrations are isolated from the core mod. Their target APIs are Create 6.0.11, JEI 19.25, Curios 9.5.1 and Sodium Dynamic Lights 1.0.10. They have not passed a full integration test matrix. Ship propulsion needs a compatible Valkyrien Skies backend exposing the ship physics listener API; incompatible backends are detected and that integration is disabled with a log message. The compile-time VS core API alone is insufficient to run ships.

Configuration: `config/deepprimal/maritime/common.toml`.

## Build

Run from this module directory with Java 21:

```bash
python3 tools/fetch_dependencies.py
./gradlew build
```

On Windows use `python tools/fetch_dependencies.py` and `gradlew.bat build`. The dependency helper verifies the pinned TFC jar's SHA-256. Other API dependencies resolve through Gradle. The output is under `build/libs/`.

The checked-in original jar is retained unmodified for provenance and assets. `processResources` extracts only `assets/**`; original Forge classes and old metadata are not packaged into the port. Active migrated datapack files live in `src/ported/resources/data`.

## Provenance

Original input: `TFCMaritimeTravelers-1.20.1-0.0.4..jar`, by commandodonut / commando_donut. Its metadata declares **All Rights Reserved**. This port retains that notice and does not relicense the original implementation or assets. See [NOTICE.txt](NOTICE.txt) and [upstream/provenance.json](upstream/provenance.json). No CurseForge publication has been made.


<!-- END PRESERVED SOURCE -->
