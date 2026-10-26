# Renderer backlog after jam4

Done: exact part bounds; opt-in bounded CPU diagnostics; bit-exact opaque/air work reduction; build-local Builder/mask reuse across three detail levels. Measured allocation reduction30–49% on five CPU fixtures; returned mesh/GPU bytes unchanged. Frozen geometry, aliases, retained output,64parallel jobs, maximal4096height and cancellation regressions pass.

Next: actual named-device camera/travel/edit/full-frame/culling/telemetry lifecycle; investigate mixed CPU timings (three component medians regressed~3%, no FPS gain established). Complete worker rebuild/allocation, descriptor/draw, GPU timing and actual RAM/VRAM instrumentation. Evaluate further mesh reuse/scheduling and hierarchy/coverage/fence-safe storage with real scenes.

No unseen-terrain delivery contract or stream yet; coordinate generator/network prerequisites within renderer scope. Independent full-detail/vertical/LOD admission stays separate from simulation. No raised-max-only credit. Actual FSR/DLSS providers/licensing/inputs/dispatch/lifetime/device gates remain open; frame generation is later and distinct.

Renderer self-audit3.67/10 (+0.17):4,6,4,0,4,4. External engine rubric unchanged. CPU timing work is completed/nonexclusive; publish completion immediately after future probes so other jobs need not infer a timing reservation from file ownership. Dedicated renderer schedule remains enabled; preserve other owners' work.
