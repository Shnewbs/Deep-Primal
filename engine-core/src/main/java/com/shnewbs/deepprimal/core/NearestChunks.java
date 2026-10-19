package com.shnewbs.deepprimal.core;

/** Reusable primitive max-heap; retains only the nearest K positions from a large queue. */
public final class NearestChunks {
    private final long[] positions;
    private final long[] distances;
    private int size, limit, centerX, centerZ;
    public NearestChunks(int capacity) {
        if (capacity < 1) throw new IllegalArgumentException("Empty selection capacity");
        positions = new long[capacity]; distances = new long[capacity];
    }
    public void reset(int limit, int centerX, int centerZ) {
        if (limit < 1 || limit > positions.length) throw new IllegalArgumentException("Invalid selection limit");
        this.limit = limit; this.centerX = centerX; this.centerZ = centerZ; size = 0;
    }
    public void offer(long packedPosition) {
        long dx = (int)packedPosition - (long)centerX;
        long dz = (int)(packedPosition >>> 32) - (long)centerZ;
        long distance = dx * dx + dz * dz;
        if (size < limit) {
            int index = size++;
            positions[index] = packedPosition; distances[index] = distance;
            while (index > 0) {
                int parent = (index - 1) >>> 1;
                if (!greater(index, parent)) break;
                swap(index, parent); index = parent;
            }
        } else if (distance < distances[0] || (distance == distances[0] && packedPosition < positions[0])) {
            positions[0] = packedPosition; distances[0] = distance; siftDown(0, size);
        }
    }
    public void sort() {
        for (int end = size - 1; end > 0; end--) { swap(0, end); siftDown(0, end); }
    }
    private void siftDown(int index, int end) {
        while (index * 2 + 1 < end) {
            int child = index * 2 + 1;
            if (child + 1 < end && greater(child + 1, child)) child++;
            if (!greater(child, index)) return;
            swap(index, child); index = child;
        }
    }
    private boolean greater(int a, int b) {
        return distances[a] > distances[b] || (distances[a] == distances[b] && positions[a] > positions[b]);
    }
    private void swap(int a, int b) {
        long p = positions[a]; positions[a] = positions[b]; positions[b] = p;
        long d = distances[a]; distances[a] = distances[b]; distances[b] = d;
    }
    public int size() { return size; }
    public long position(int index) {
        if (index < 0 || index >= size) throw new IndexOutOfBoundsException(index);
        return positions[index];
    }
}
