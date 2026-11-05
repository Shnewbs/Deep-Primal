# Same-cell ABA callback guard

Date: 2026-10-31. Follow-up to the Dev41 single-cell postwrite admission
checkpoint. This source-only change closes one observable callback window; it
does not qualify finite-water transactions or native callback behavior.

## Change

`HydrologyRuntime.writeAccounted` now keeps an outer admission scope active
around its raw native write. `WaterWriteScope` marks active tokens for the same
server level and packed cell when a transition is not the one exact event
owned by the innermost write, including a replay after that event was consumed.
The accounted operation rejects before its caller updates a reservoir or
chemistry ledger, even when a synchronous callback changes the cell away from
the requested state and restores that same value before returning.

Other cells and levels do not invalidate the token. The existing final-state
check remains as a separate fallback for replacements that are not observed by
the event hook. The raw write API and two-cell transfer rollback ordering are
unchanged.

## Checks

OpenJDK 21.0.12.1, exact-source extraction and explicit boundary doubles:

- `NATIVE_WRITE_GUARD_PASS sameCellAba=true silentReplacement=true`; the
  dedicated ABA-barrier mutant fails the ABA assertion.
- `NATIVE_WRITE_GUARD_PASS scenarios=2761 assertions=45539`; all five remaining
  guard mutations and the caller-bypass structural mutation are rejected.
- The source-extracted integration check reports 21 assertions.

These are API-double/source checks, not a Minecraft or conservation test.

## Limits

Only differing state transitions observed synchronously on the calling thread
can be marked. A same-value/no-op callback, an indistinguishable duplicate
transition, asynchronous or cross-thread writes, and callbacks outside the
instrumented event path are not proven detectable. Throwing on an observed ABA
does not itself restore physical water or account state. Exception-after-write
recovery, same-state ownership, durable journaling, unload/reload, save/reopen,
long-soak, native NeoForge injection, Java 25/Gradle packaging, and OpenGL/Vulkan
acceptance remain open.