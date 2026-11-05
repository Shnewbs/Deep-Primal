# Renderer jam38: two-back repeated AABB visibility

Exact implementation: [2d7959741f8a43c1790d19f5e976e5eef95cdfb5](https://github.com/Shnewbs/Deep-Primal/commit/2d7959741f8a43c1790d19f5e976e5eef95cdfb5). The resident page now stores a bounded i-2 repeated-bounds mask alongside its existing adjacent mask, allowing interleaved mixed-bound parts to reuse prior frustum verdicts while retaining draw order. The transparent-water pipeline and broadphase-off baseline are unchanged.

The [read-only author self-audit](REVIEW.md) and [ledger](LEDGER.json) report Java21-only validation: 16 positive runs, 10,702,115 assertions, one rejected mutant, 12/12 pinned integration checks. Synthetic frustum predicates avoided 32.97% of tests in a randomized fixture, but page-publication CPU p50 **regressed** on alternating bounds; no FPS or actual GPU improvement is claimed. Reviewer grade 5.83/10 (+0.02), independent engine grade unchanged. Owner retains full builds; no JAR generated.
