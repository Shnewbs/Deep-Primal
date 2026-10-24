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
