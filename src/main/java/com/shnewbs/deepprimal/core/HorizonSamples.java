package com.shnewbs.deepprimal.core;

/** Coordinate-aligned height/color snapshots can derive coarser grids without density evaluations. */
public record HorizonSamples(int detail, short[] heights, int[] colors) {
    public HorizonSamples {
        if (detail < 0 || detail > 2 || heights.length != HorizonGeometry.points(detail) || colors.length != heights.length)
            throw new IllegalArgumentException("Invalid sample grid");
    }
    public long payloadBytes() { return heights.length * 6L; }
    public HorizonSamples forDetail(int target) {
        if (target < detail || target > 2) throw new IllegalArgumentException("Cannot recover finer samples");
        if (target == detail) return this;
        int sourceCells = HorizonGeometry.cells(detail), cells = HorizonGeometry.cells(target), ratio = sourceCells / cells;
        short[] downHeights = new short[(cells + 1) * (cells + 1)];
        int[] downColors = new int[downHeights.length];
        for (int z = 0; z <= cells; z++) for (int x = 0; x <= cells; x++) {
            int source = z * ratio * (sourceCells + 1) + x * ratio, dest = z * (cells + 1) + x;
            downHeights[dest] = heights[source]; downColors[dest] = colors[source];
        }
        return new HorizonSamples(target, downHeights, downColors);
    }
}
