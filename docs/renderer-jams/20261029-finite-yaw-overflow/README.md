# Renderer jam 29 — finite camera-angle overflow guard

**Source and test checkpoint:** [ce3ff1d8](https://github.com/Shnewbs/Deep-Primal/commit/ce3ff1d88b468c26c1c3ce2f1471dd00d04fe1ad). This scoped renderer-only repair consumes the [independent Dev74 audit](../../engine-hourly-audits/2026-10-29T0830-15d01f6c/REVIEW.md).

In the optional adaptive page-frustum admission gate (`-Ddeepprimal.lod.pageFrustumAutoSkip=true`), subtracting extreme *finite* float yaw values could produce infinity before modulo, incorrectly postponing a coarse page sample. The correction normalizes large headings individually as doubles before comparing their shortest circular difference, while preserving the original float fast path for ordinary camera angles within ±360°. Invalid inputs still force conservative sampling.

**Intentionally unchanged:** fine-part visibility and order, page-bound AABBs, water transparency, terrain/water simulation, mesh geometry, Vulkan/GL draw paths, GPU buffers, game settings, FSR1 and DLSS provider behavior. The optional adaptive gate remains disabled by default. The original source fails the dedicated overflow regression; the new source passes. This is not a demonstrated Minecraft FPS or native GPU improvement.

Read the [six-category renderer self-audit](REVIEW.md) and exact [source/test commit](https://github.com/Shnewbs/Deep-Primal/commit/ce3ff1d88b468c26c1c3ce2f1471dd00d04fe1ad). Source-only evidence ZIP: `DeepPrimal_Renderer_FiniteYaw_20261029_ce3ff1d_SourceEvidence.zip`, SHA256 `a75703c45913108f35f936b7db9fc6167f0abd81b61762e2580f837637e0c3c9`, 25,098 bytes; Library `/Deep Primal/Renderer Jams/`. **No new development JAR.** Owner still performs Java25 full build, named-hardware Vulkan/OpenGL and packaged acceptance.
