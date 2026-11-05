# Expanded replica — renderer-vulkan/README.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/renderer-vulkan/README.md) |
| Source identity | git: renderer-vulkan/README.md |
| Preserved source SHA-256 | `5751faf7a6d4b01fce4393eaee1256867ab326a97fabd64d5b25d13977491497` |
| Review method | full-text |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../README.md) · [Integrated design](../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../chapters/render-performance.md) · [Discrepancy register](../../../DISCREPANCIES.md)

## Research expansion — source scope

A concise module entry point identifies the optional Vulkan backend, its separation from Minecraft/Fabric-specific meshing, lean build behavior, platform build choices, and replaceable nested adapter artifact. It is navigation and packaging context, not a complete renderer capability or hardware qualification document.

## Research expansion — gaps and details to deepen

- The short README would benefit from a practical path from module selection to verified active backend. Readers need links to startup identity, saved-request versus effective fallback, required device features, diagnostics, and known native acceptance limits without duplicating the detailed backport audit.
- Optional-module packaging is described at a high level, but the entry point should identify which artifact owns the adapter classes, graphics natives, shader/compiler payload, and optional NGX bridge. Presence in an archive is distinct from safe loading on a supported client or a headless dedicated server.
- The module boundary should state its ownership obligations to the common renderer: resource lifetime, frame/device generation, format capability queries, state restoration, and completion-aware retirement. This makes future lighting and reconstruction integration reviewable without moving game authority into the backend.

## Research expansion — proposed contracts

- Add a compact navigation table linking Vulkan backport, capability audit, renderer diagnostics, upscaling, and native NGX documentation. Each link should name the question it answers: source origin, device gate, fallback, resource contract, build target, or qualified evidence.
- Define a build receipt template with source commit, module inclusion flag, target OS/architecture, exact nested artifacts and hashes, native-loading policy, and expected backend identity. Explain how lean and server builds avoid graphics initialization and how a supported client reports an unavailable optional backend.
- Add an adapter contract summary: the common engine retains world and material authority; the backend implements creation, recording, submission, completion, and retirement; providers use negotiated resource descriptors. Optional features are exposed only through explicit capability results, never inferred from a module name.

## Research expansion — acceptance checks

- Inspect full and lean distribution manifests for duplicate or missing adapter classes and target-native payloads. Start a dedicated server and a client with the optional module absent, verifying no accidental graphics initialization and a clear effective-backend result.
- On an exact supported artifact, verify startup, restart selection, required-feature rejection, saved request preservation, resize/reload, and shutdown. Link the resulting device and image evidence from this entry point; a successful packaging check alone must not advertise rendered Vulkan or optional DLSS output.

## Research expansion — source anchors

- Deep Primal Vulkan adapter

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Deep Primal Vulkan adapter

LGPL-3.0-only adaptation of VulkanMod 0.6.8+26.1.2 by Collateral and contributors. See LICENSE, COPYING, UPSTREAM.json and [integration documentation](../docs/VULKAN-BACKPORT.md).

Vulkan is the primary sample/reference API and graphics validation target. Build this optional module with the root Java 25/NeoForge project: `./gradlew -PvulkanPlatform=linux build` (substitute the target platform), or use `-PbundleVulkan` for universal natives. The lean default package omits it. Launch the primary Linux sample with `./gradlew -PvulkanPlatform=linux -PgraphicsBackend=Vulkan -PvulkanValidation runClient` and verify that Vulkan was actually selected. The resulting nested adapter JAR remains separately replaceable/rebuildable. Its sources are under `src/main/java`; source shaders and startup/mixin metadata are under `src/main/resources`. The module bundles LWJGL Vulkan/shaderc/VMA bindings and their required platform natives, not LWJGL core/GLFW or Minecraft classes.

The native Minecraft chunk mesher is retained. This directory excludes Fabric API/Loader and VulkanMod's custom terrain renderer, its configuration screens and its unrelated gameplay/mixin optimizations. Authored bootstrap, recovery, copy/query/stencil and native API adaptations are included with the matching reconstructed LGPL backend source.


<!-- END PRESERVED SOURCE -->
