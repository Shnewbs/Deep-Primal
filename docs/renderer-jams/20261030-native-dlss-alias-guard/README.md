# Renderer jam 48 — native DLSS resource-handle alias guard

Source: [5cf284fd62632757a8cb740e565c541f58c07c58](https://github.com/Shnewbs/Deep-Primal/commit/5cf284fd62632757a8cb740e565c541f58c07c58). This jam closes the JNI-side image/view identity gap without activating DLSS. C++17 standalone fixture and sanitizer checks passed; no proprietary NGX/Vulkan runtime or GPU acceptance.

- [Read-only scored self-audit](REVIEW.md)
- [Machine-readable ledger](LEDGER.json)
- [Next renderer-only work](BACKLOG.md)
