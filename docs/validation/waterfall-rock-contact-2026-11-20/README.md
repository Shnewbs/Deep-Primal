# Rock contact along steep waterways

On the seed 20261020 mountain reach near X 819, Y 175, Z 1084, the client showed small triangular openings in front of the terraced streambed. Two independent geometry checks reproduced cases that open a gap.

* Horizontal vertex displacement could carry a bank vertex past a vertical rock face. At X −17 the old mesh missed the exact contact plane. The normal offset now tapers to zero at each face, while vertices inside the cell retain their original wave shape at its midpoint. Both fallback and animated water use this shaping function.
* A lower surface could end 7/16 of a block below the bottom of an adjoining stacked curtain. The corner now recognizes an upper water column continuing for a second cell. Single-step films retain their smooth shared surface. At the top of a section, a small wet-column bitset records this additional row only when necessary. A change within reach invalidates the affected section, including the section below a changed continuation.

| Check | Result |
| --- | --- |
| Horizontal rock contact | 6,300 cases, including signed coordinates, thin films and two section boundaries |
| Free stepped surfaces | 1,024 shared vertices at vertical section boundaries and 1,056 retargeted edge samples |
| Water over rock ledges | 8,192 cases across two axes and both downhill directions |
| Stacked wall mixed with a tributary | 18,432 vertices across all 256 quantity pairs |
| Lower surface meeting a stacked upper wall | 1,440 edge vertices across three vertical section positions |
| Native fixed-seed replay | 3 generated habitats. The river retained 530 wet cells and 6,634 units. |

The scene check lives in `ShorelineSceneChecks`. Its seed, coordinates and native run options are in the [habitat survey](../shoreline-habitats-2026-11-20/README.md). The in-game mountain view still contains exposed stone where the watercourse descends several blocks. Further work on those drops requires measurements of the occupied cells and water delivery over time.
