# Validation and evidence — research notes

Research date: 2026-10-29. These sources inform the proposed evidence system; they do not certify Deep Primal. Project examples come from the preserved project documents. The proposals below are original recommendations.

## VE-01 — Unreal Engine Automation Test Framework

- Primary source: [Epic documentation](https://dev.epicgames.com/documentation/en-us/unreal-engine/automation-test-framework-in-unreal-engine).
- Observed: Epic distinguishes unit, feature, smoke, content-stress, and screenshot-comparison tests. It also warns that its engine-dependent automation framework is not an ideal substitute for pure unit tests. Its test-design guidance accounts for order independence, shared state, and cleanup.
- Design inference: classify Deep Primal evidence by the boundary exercised. A pure Java water-policy test, a host-world transaction test, and a rendered-frame comparison answer different questions. Give each a separate required gate and execution environment.
- Limit: Unreal's taxonomy is a useful comparison, not a requirement to embed Unreal automation or its one-second smoke-test convention.

## VE-02 — OpenJDK JMH

- Primary source: [OpenJDK JMH repository and guidance](https://github.com/openjdk/jmh).
- Observed: JMH supplies a JVM benchmark harness and recommends a correctly generated standalone benchmark setup. Its maintainers explicitly require understanding the samples and benchmarking pitfalls and recommend peer review; a harness does not eliminate misleading experiments.
- Design inference: measure pure density, cache, and mesh kernels in isolated harnesses, then separately measure their actual load-to-ready and frame effects. Preserve inputs, output checksums, JVM flags, warm-up, process boundaries, allocation, and raw distributions.
- Limit: microbenchmark gains cannot establish a whole-game improvement, and JMH is a candidate tool rather than a new project dependency in this documentation change.

## VE-03 — Gradle dependency verification

- Primary source: [Gradle verification manual](https://docs.gradle.org/current/userguide/dependency_verification.html).
- Observed: Gradle supports checksum and signature verification and scoped trusted-key configuration. Its documentation distinguishes signature validity from trusting the signer, and warns against unverified or excessively broad trust. Verification metadata is itself a controlled input.
- Design inference: retain hashes for downloaded dependencies and separately verify project-source, compiled-class, package, and runtime-selected-artifact identities. An intact dependency or archive does not prove the intended current code was executed.
- Limit: the opened manual is a moving current release. Apply only options supported by the project's pinned wrapper; this study does not upgrade Gradle or modify dependency policy.

## VE-04 — Khronos Vulkan validation

- Primary source: [Vulkan validation overview](https://docs.vulkan.org/guide/latest/validation_overview.html).
- Observed: Khronos recommends validation layers during development because Vulkan performs little API checking itself. The documentation identifies implicit and explicit validity requirements, and describes validation layers as development tooling with performance overhead.
- Design inference: keep instrumented correctness captures and representative performance runs separately attributed. A clean validation run supports the exercised API paths; it cannot establish water conservation, temporal image quality, or performance on another driver.
- Limit: absence of a reported validation error is not proof that all synchronization, device-loss, or lifetime paths were exercised.

## Common application to this corpus

The project already records many of these distinctions. Expansion should make them queryable, consistent, and hard to accidentally erase. Preserve failed attempts and API-double limits; attach a claim ID, source revision, environment, artifact identity, and invalidation rule to each result. The implementation specification is in [Validation and evidence](../chapters/validation-evidence.md).
