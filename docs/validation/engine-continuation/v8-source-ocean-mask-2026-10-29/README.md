# Deep Primal dev72: saved ocean-domain priority-flood source

Source: `0.0.1-alpha.29-dev.72-saved-ocean-domain.1`, implementation commit `adbefcc16b3c5ac16526a93c2024a2cff3b05d75`; test commits `a8a88a8178768ded6df1c6c36f228cc89c9db7fd` and `316b940de29b2d9a8777ac364f362bf20323aa50`.

## Auditor finding consumed
Latest completed independent review: [Dev71 at 05:26 UTC](../../../engine-hourly-audits/2026-10-29T0526-9c2fe3a5/REVIEW.md), pinned `9c2fe3a5dddeeb1f0f80184fbd9ab90081c123fe`. Its newly identified false-sea-seed risk came from accepting `marineCoreSeen && unresolvedWetGateway` once a connected lake exceeds eight 8,192-block regions. This can contaminate actual V8 `WatershedRegion` priority-flood seeds, not just surface labeling.

## Implemented
New explicit saved **routing policy v5** requires a graph-authored signed `ocean_domain` channel and a wet V8 height before each exact 32-block priority-flood boundary seed is accepted. Four-corner classification must agree with that saved ocean domain. The source is 2D/Y-independent, world-seed/density/climate-bound and domain-separated through the persisted revision-5 hydrology fingerprint. Negative marks ocean; zero or positive is not ocean. Nonfinite samples reject. The old V8 route policies v1-v4, previous fingerprints, frozen V1-V7 geography, player edits and liquid quantities are unmodified. This is opt-in and **not activated native V8 world generation**.

## Checks
OpenJDK 21.0.12.1, `javac --release 21 -Xlint:all -Werror`, `java -ea -Xverify:all`: **19/19** focused API-double suites, **21,013,270** fixture assertions/checks. Java source/test blobs were matched against the published Git objects. New tests deny an 11+ region-wide marine-looking lake based on its saved domain, exercise positive ocean and dry-column gating, signed coordinates, query ordering, 32-block seed alignment, graph provenance, modeled saved-profile reopen and edited-mask fingerprint rejection. Both separately compiled wrong implementations (ocean-mask sign reversal; bypassing dry-column gate) were rejected.

Portable source evidence ZIP `DeepPrimal_Dev72_SavedOceanDomain_SourceEvidence.zip`: **157,780 bytes**, SHA-256 `ab5181b92a377b49df1657b9f66477ec7d962f541f361dab6ecf71038b252224`, 73 members, ZIP CRC passes. Includes exact source/test files, isolated dependency doubles, runner, positive log and mutation receipts. It is **not a JAR**.

## Still open
The new mask is **authored**, not itself a proven ocean source. No universal cross-region outlet/river discharge/lake spill accounting yet, no native old/new world border, Mojang codec, real production `WatershedRegion` compilation, complete JDK25/Gradle/NeoForge, GPU, water restart/conservation, multi-seed visual or CPU/memory acceptance. The standalone `tools/test_ecotone_blend.py` runner was unavailable in the local partial source archive, so it was not independently rerun. Root `ENGINE-WORK-STATE.json` remains at its dev53 pinned checkpoint pending complete validator access. No new playable JAR or independent grade; last accepted complete score remains **50/100 = 5.0/10**.

Next: author a globally consistent ocean-domain source based on actual cross-region spill topology and prove that it matches saved terrain geometry; independently validate production watershed and native generation. Maintain water conservation and renderer separation.
