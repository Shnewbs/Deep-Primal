# TFC Maritime Travelers — 1.21.1 development port

This is a separate development module on Deep Primal's `maritime-1.21.1` branch. It is **not yet a playable release**. The full source reconstruction is present, but Java/API migration and optional integrations are still incomplete. See [docs/PORT_STATUS.md](docs/PORT_STATUS.md) for verified results and remaining work.

The mod keeps the original `tfc_item_barrels_and_more` namespace and original author attribution. It has not been integrated into Deep Primal's gameplay or its main build.

## Target

| Component | Pinned development target |
| --- | --- |
| Minecraft | 1.21.1 |
| Loader | NeoForge 21.1.264 |
| Java | 21 |
| TerraFirmaCraft | 4.2.12 |
| Create compile dependency | 6.0.11-319 for 1.21.1 |
| JEI API | 19.25.0.321 |
| Curios API | 9.5.1+1.21.1 |

## Build workspace

Run commands from this directory, not from Deep Primal's repository root:

```bash
python3 tools/fetch_dependencies.py
./gradlew build
```

On Windows, run `python tools/fetch_dependencies.py` and `gradlew.bat build` with Java 21. Compilation currently stops on the migration errors documented in the status report; these are not installation instructions for a released mod.

The checked-in original jar is retained unmodified for provenance and assets. `processResources` extracts only `assets/**` from it. Original Forge classes, old metadata, and old mixin configurations are not copied into a new jar. The migrated data files live in `src/ported/resources/data`.

Configurations are registered under `config/deepprimal/maritime/common.toml`.

## Provenance

The user supplied `TFCMaritimeTravelers-1.20.1-0.0.4..jar`, by commandodonut / commando_donut. Its metadata declares **All Rights Reserved**. Attribution and that notice remain intact; this module does not relicense the original code or assets. See [NOTICE.txt](NOTICE.txt) and [upstream/provenance.json](upstream/provenance.json).
