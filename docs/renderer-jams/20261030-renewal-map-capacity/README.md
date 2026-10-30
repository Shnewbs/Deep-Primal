# Renderer jam 55 — bounded renewal-map capacity

Implementation commit: ad1b9597f977845d7ba793d780ebcbc3f75b2cb4
Parent main snapshot: 118d4dd7d172c19dd0386cec7bb25d6f20b4464c

## Scope claim
Renderer-only ownership: lod-engine renewal-map capacity and jam evidence. No water, engine, GUI, port, automation, or unrelated files were edited.

## Change
Pre-size the bounded renewal map to the scanner's 8,192-key admission limit with a 1.0 load factor. This avoids growth/rehash during the bounded render-thread handoff while preserving the existing fail-closed overflow behavior.

## Validation
Source readback and scoped diff review passed. Java25/full Gradle, native Vulkan/OpenGL, GPU timings, water pixels, FSR/DLSS dispatch, packaged JAR and release gates were not run under the owner build/GPU override.

## Next
Run owner-side travel/edit churn measurements and validate the bounded handoff under real disk latency before claiming a performance gain.
