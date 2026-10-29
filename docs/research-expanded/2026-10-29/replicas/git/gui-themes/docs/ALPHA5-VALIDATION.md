# Expanded replica — gui-themes/docs/ALPHA5-VALIDATION.md

**Edition:** 2026-10-29 · **Status:** research expansion and proposed design.

| Provenance | Value |
| --- | --- |
| Original | [Open original source](https://github.com/Shnewbs/Deep-Primal/blob/33c43c943f0469195e7ab1a6852ab4360fad1788/gui-themes/docs/ALPHA5-VALIDATION.md) |
| Source identity | git: gui-themes/docs/ALPHA5-VALIDATION.md |
| Preserved source SHA-256 | `7d1f95071434a925f6a5563d52403e5a52425e2687bcea48f5ff487c0b16bc46` |
| Review method | structured-evidence |
| Proposed-work priority | P1 |

The original document is preserved byte-for-byte below. New material is a dated proposal; historical results keep their original source, environment, and limitations. The priority describes follow-up design work, not a newly reproduced runtime defect.

[Corpus guide](../../../../README.md) · [Integrated design](../../../../ENGINE-DESIGN-EXPANSION.md) · [Detailed chapter](../../../../chapters/assets-materials-ui.md) · [Discrepancy register](../../../../DISCREPANCIES.md)

## Research expansion — source scope

Themes alpha5 notification contrast fixes preserving native icons and fade alpha, plus compact HUD spacing when Deep Primal is present without a progression provider. This is a historical record for its named source and candidate; its results are not current verification.

## Research expansion — gaps and details to deepen

- The recorded boundary is that the compatibility fixture uses a presence stub and synthetic providers, so it does not establish actual Deep Primal gameplay integration. Later summaries need to preserve that boundary when reusing this evidence.

## Research expansion — proposed contracts

- Add a provider-state and accessibility matrix covering absent, available and disabled data, text contrast, wrapping and native icon fidelity. This is a proposed enduring contract, not a claim that the historical defect is still present.

## Research expansion — acceptance checks

- Switch themes during tutorial and advancement toasts, verify fade alpha and readable text, then test real provider registration without restoring vanilla XP when Deep Primal suppresses it. Record the new source, fixture and artifact identities before upgrading the evidence status.

## Research expansion — source anchors

- Alpha.5 notification contrast and HUD spacing

## Preserved source document

Original links retain their original document context. Use the pinned source and source-context index above for dependencies not copied into this edition.

<!-- BEGIN PRESERVED SOURCE -->
# Alpha.5 notification contrast and HUD spacing

Target: Minecraft 26.1.2, NeoForge 26.1.2.122, Java 25.

## Fixes

Recipe notifications retained Minecraft's dark purple/black labels after the previous resource pack recolored their background. Recipe, tutorial, system and advancement toasts now draw a matching theme backplate and theme text. Preformatted tutorial lines receive the same treatment. Text fade alpha is preserved, and custom notification text does not add shadows. Vanilla mode keeps native colors and rendering.

Toast icons are no longer recolored by the fallback asset generator. Only the four supported toast backplates are replaced at render time; native icons retain their original artwork.

When Deep Primal is detected without a progression provider, health/hunger panels move down by 13 GUI pixels to close the unused progression row. Armor and air overlays follow the same offset. Registered progression and mounted jump bars retain their reserved space. This does not restore vanilla XP while Deep Primal is loaded.

## Checks

A clean Java 25 build includes the new toast mixin and regenerated resources. The opt-in client harness captures actual recipe, tutorial, system and advancement toast implementations for all eight themes and Vanilla, together with existing inventory/workstation/recipe-layout checks.

The `themeCompatValidation` Gradle property adds a development-only mod presence stub named `deepprimal`, allowing the no-progression-provider layout to be exercised. It does not implement or test Deep Primal gameplay. The normal test provider separately exercises the progression-visible layout. Both the stub and validation classes are excluded from the release JAR.

Run the compatibility harness with:

```sh
gradle --no-build-cache -PthemeValidation -PthemeCompatValidation clean build runClient
```

Manual visual review covers light/dark notification text, native recipe icons, tutorial text wrapping, system messages, advancement text, and the compact no-provider survival HUD. Normal theme switching restores Vanilla resource origins and inventory slot coordinates.


<!-- END PRESERVED SOURCE -->
