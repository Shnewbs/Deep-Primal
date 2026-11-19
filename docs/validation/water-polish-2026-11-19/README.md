# Water polish for dev.130

This pass builds on dev.129 at `8f46154fb0d5c3dc86b45b51ea23010caa99aad8`.

New standard and deep worlds use saved channel revision 12. Narrow channels now sample the relief between their drainage knots, fit toward measured lows, and join at the same positions and tangents. The fit stays within six blocks and uses at most twelve terrain queries per eligible reach. It preserves the planned stage and catchment at every subdivision. Wide trunks, lake outlet approaches, and reaches near sea level retain the previous routes. Existing saves select their stored channel revisions and receive the renderer and stream scheduling changes.

The smooth mesh and its height histories now agree on half-block vertices. This cuts smooth top faces from nine quads to four per wet cell and keeps their horizontal coordinates fixed during level changes. The renderer skips light and biome tint work for buried water while retaining the appearance needed by faces that are fading. Delivered sections are considered from the top down. Water at the world ceiling has an open sky halo, and distant detailed water uses terrain's fog distance and a final opacity fade at the edge of the loaded view.

Stream surveys advance past chunks that are not ready. They can admit a river column as soon as it is found, so a partially surveyed chunk can use its existing budget. The shared groundwater ledger continues to pay for accepted water only.

| Check | Evidence |
| --- | --- |
| Lower terrain between drainage knots | Five seeds and 3,333 reaches. 2,062 interior samples moved and 835 measured stations lowered the terrain to be cut by 0.624 blocks on average. The hydraulic centers remained wet, including underground reaches. |
| Planning cost | 365,343 → 383,770 height calls in a five-seed probe with the same 3,418 reaches. The 5.04% increase counts height queries during planning. |
| Saved geography | 768 columns and 7,680 sampled fields exactly matched the parent implementation across revisions 0–11. |
| Water edge regression | The parent moved a shared edge sideways by 0.0266 blocks. The candidate passed all 1,056 comparisons with a maximum horizontal difference of 0.000001 blocks. |
| Native creation | An integrated client created, captured and saved six terrain scenes with channel revision 12. The production codec round-tripped 0–12 and rejected -1 and 13. |
| Natural headwater capture | The same camera showed the abrupt distant water edge before the fog change and a subdued edge afterward. The two runs had different resident page counts. |
| Native cliff and ceiling | OpenGL passed 106 frames and 221,434 shared-vertex comparisons. Vulkan passed 100 frames and 208,900 comparisons with Khronos validation active and zero validation errors. Both emitted a full water surface at y=639 and produced zero seam mismatches beyond the quantization tolerance. |
| Natural cascade fly-by | The camera followed an unedited cascade with a modeled 50-block drop. Each backend showed water in all 120 sampled frames. OpenGL and Vulkan found zero height mismatches in 4,428 and 4,856 cross-page comparisons respectively. Vulkan used a smaller viewport with validation active and finished with zero validation errors. |
| Native motion and renderer lifecycle | The smaller Vulkan software viewport captured two distinct intermediate heights during one 100 ms rise. Drying, fallback, re-enabling, and resource reload passed with zero Vulkan validation errors. The test can retry a settled rise if a slow render misses the interval. |
| Native stream scheduling | A ready stream advanced past an unavailable chunk without loading it. Drought and recharge checks covered six reserve scenarios with sixteen attempts each, with exact water, salt and sediment accounting. |
| Native save and reopen | A sealed parcel retained 4,096 water quanta, 37 salt and 71 sediment. The regional groundwater balance also matched after reopening the world. |
| Full build | Java 25 and Gradle 9.2.1 passed `build compileVisualValidationJava -PvulkanPlatform=linux` with the complete core and resource checks enabled. |
| Tool tests | All 42 tests enabled by the repository runner passed. Its 13 existing skips stayed in place. |

Live rendering used Mesa software OpenGL and Vulkan on Linux. The cliff and cascade checks sample the presented vertex positions. Windows GPU performance at long view distances, extended waterfall and chunk-streaming coverage across seeds, and the multi-season watershed and ocean persistence soak remain open.
