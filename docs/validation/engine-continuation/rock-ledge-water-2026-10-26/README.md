# Rock-ledge water seam repair

A supported upper water film and the neighboring lower pool used different
world-space corner heights. The lower cell ignored the film above the solid
ledge, while the upper cell applied its solid-floor constraint. This can leave
separated sheets at a one-block rock step.

The core patch mesher and the chunk-buffer fallback now include that supported
upper film and apply its floor constraint in both coordinate layers. The
shared edge remains coincident through the existing linear interpolation.
Water quantities, constituents, transport, generation and saved profiles are
unchanged. No new renderer feature or completed water-engine acceptance is claimed.

## Reproduction and regression

The new regression fails against the unchanged mesher at source
`1317b6f95e9905fc368e8095a7803615d43cc1cd`; see baseline.txt.
The candidate passes 8,192 ledge cases: both horizontal axes, both orientations,
16 upper and 16 lower quantities, both smoothing settings, four signed vertical
positions including section boundaries, current/previous states and five
interpolation samples. Both upper and lower top faces must be witnessed.
Existing waterfall-side and stepped-basin controls also pass.

The test is part of the existing WaterfallSideChecks invocation in CoreChecks.
No source sets or assertions are excluded.

## Coordination and source identity

Water-only owner resumption; preserve the separate hourly cache/pipeline writer.
The scoped lease is docs/WATER-ENGINE-WORK-STATE.json. No root coordination fields,
cache/pipeline code, approved art or geography revisions are changed by this patch.
The Windows checkout is reconciled to committed cache repair dev.25 at 61dc785.
It landed after the first passing water build. Final validation repeats the full
build/core suite with both repairs; dev24-water-build.log and
dev24-water-package.json retain the earlier composition.
source-identity.json compares 538 Java/build inputs with pinned main:
only the four intended water sources and the unique version differ, with zero unexpected differences.

The latest completed independent audit consumed is
docs/engine-hourly-audits/2026-10-26T0154-4fb29e0/REVIEW.md, added by eeb907b.
Its dev.24 core/package/native gates pass and its scoped score returns to the
existing 5.0/10 baseline. It does not evaluate this water patch. Its next gates
include temporal water/visual acceptance and bounded publication, which this
source-level ledge regression begins to address. No grade changes are assigned here.
The concurrent audit commit changes documentation only and is preserved.

## Build and acceptance

Build version override: `0.0.1-alpha.29-dev.26-water.1`, to avoid the other
builder's reserved dev.25. water-version.gradle applies it only outside Themes;
Themes retains 0.1.0-alpha.8. After the dev.25 writer released its lease, gradle.properties is advanced to this
unique water version. A normal assemble without water-version.gradle is fully
up-to-date and produces identical verified bytes; standard builds need no custom
version override.
Java 25.0.4.1, Gradle 9.2.1, Windows, cached matching Minecraft 26.1.2 /
NeoForge 26.1.2.122 artifacts. Offline in-process Gradle with command-line javac,
workspace java.io.tmpdir, build -PvulkanPlatform=windows and
-I water-version.gradle -x createMinecraftArtifacts.
The init script is retained unchanged from the prior integration evidence.

Full build and all 73 pass markers are recorded in checks.json/build.log.
The initial global -P version override also changed Themes metadata; the strict
archive verifier rejected it. Final assembly uses water-version.gradle to retain
Themes independently. Runtime/test sources are unchanged; final archives and
compiled-class identity must pass the original strict version/CRC checks.
See repackage.log and package.json.
The first launch attempts hit shell argument splitting and restricted daemon
connection; the configured in-process retry performs the actual checks.
A diagnostic standalone core run initially hit the sandbox default temp-directory
rename restriction; the authoritative build uses a writable workspace temp.

Not validated: fresh online dependency bootstrap; actual owner scene;
Minecraft client pixels; OpenGL/Vulkan GPU rendering; packaged native gameplay,
save/reopen; timing/FPS or long-soak behavior. The source-level ledge correction
does not establish that every defect in the supplied screenshot is resolved.

The Library water design document returned no readable content, and its download
failed. This cycle uses the repository water requirements plus the reported symptom.
