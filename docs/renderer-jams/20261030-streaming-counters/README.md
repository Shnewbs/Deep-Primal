# Renderer Jam 65 — bounded streaming apply counters

Parent main: 7364d882b267d3f2c77f3cf1a4386f2e49e9266b
Implementation branch: renderer/jam65-streaming-counters
Implementation commit: 66698c4e4c250ee07576c093c3fa5e9af32a22fe

This renderer-only increment adds lifetime diagnostics counters for owner-thread
bounded recovery apply work: removals, additions, and sheds. The counters do
not change admission, ordering, mesh generation, water ordering, Vulkan/OpenGL,
or FSR/DLSS behavior. They make the existing bounded streaming work measurable
for the owner's camera-travel/edit baseline capture.
