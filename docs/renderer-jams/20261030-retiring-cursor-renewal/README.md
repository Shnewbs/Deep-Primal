# Renderer jam 54 — retiring-cursor renewal preservation

This jam closes a bounded LOD recovery race: a cancelled disk cursor can remain in the retiring handoff while its directory stream closes. Renewals arriving during that window must remain protected from stale scan discard/apply.

The production change keeps renewal tracking active while `retiring != null`, without changing queue caps, generation tokens, retry cadence, filesystem ownership, geometry, draw ordering, water behavior, Vulkan/OpenGL commands, or upscaler dispatch.

No qualified JAR or GPU acceptance is claimed.
