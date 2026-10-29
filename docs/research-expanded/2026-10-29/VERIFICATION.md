# Corpus verification receipt

**Status: PASS**

**Edition and verification date:** 2026-10-29

**Final input commit:** `33c43c943f0469195e7ab1a6852ab4360fad1788`  
**Declared source version:** `0.0.1-alpha.29-dev.77-seam-contract.1`

Research began at `31126b3996064e65351fd08870cd6a0949e38970`. The first 17-commit refresh added three Markdown files and updated the backlog at Dev69. The final 102-commit comparison added another 35 Markdown files and updated the backlog at Dev77. All are included. The nineteen project-collection originals were rechecked and unchanged. Later concurrent repository commits are outside this fixed source snapshot and are preserved by guarded publication.

This receipt covers documentation integrity, coverage, source anchors, and authored navigation. It does not certify game implementation or runtime behavior. The containing Git commit identifies publication; the checks below are reproducible independently of branch movement.

| Check | Result |
| --- | --- |
| Inventoried originals and matching replicas | 487/487 |
| Embedded original-source SHA-256 checks | 487 passed |
| Exact Git blob-identity checks | 468 passed |
| Complete replica hashes | 487 passed |
| Per-document review cards | 487, complete and unique |
| Literal source-anchor checks | 942 passed against downloaded originals |
| Newly authored relative file links | 2808 resolved |
| Exact duplicate groups retained | 9 pairs |
| Original project files changed | 0 |

Review methods are recorded per source: 95 full-text reviews and 392 structured-evidence reviews. The latter use structured extraction and targeted reading of scope, findings, limitations, evidence, and named subjects; the method does not imply a line-by-line reread of every repeated historical record or catalog row.

## Historical dependency references

0 relative references inside original source text did not resolve to a verified pinned target. They remain unchanged as historical evidence. All newly authored relative file links pass.



## Reproduction

From the repository root:

```sh
python3 docs/research-expanded/2026-10-29/tools/verify_corpus.py
```

The verifier uses the Python standard library. It verifies every replica and its embedded source slice against `manifest.json`, checks coverage and review fields, recomputes Git blob identities, and checks authored relative file links. Embedded historical links are outside that authored-link gate and separately indexed.

The literal source-anchor cross-check was additionally performed against the complete downloaded originals during assembly. The complete final Git tree inventories for the three changed Markdown subtrees were independently matched to the source manifest. External source availability and vendor compatibility are bounded by the research notebooks and their review dates; this is not an ongoing link monitor.

## Engineering evidence boundaries

No game build, shader execution, native host session, GPU benchmark, installer qualification, saved-world migration, or new engine grade was performed by this documentation expansion. Proposed acceptance checks are future qualification work. Historical executed tests retain their original source and environment.
