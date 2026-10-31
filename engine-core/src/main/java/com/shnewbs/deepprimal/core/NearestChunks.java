package com.shnewbs.deepprimal.core;

/** Reusable primitive max-heap; retains only the nearest K positions from a large queue. */
public final class NearestChunks {
    private final long[] positions;
    // Signed chunk-coordinate deltas can span 2^32-1. Their squared sum needs
    // 65 unsigned bits, not one signed long.
    private final long[] distances;
    private final byte[] distanceCarries;
    private int size, limit, centerX, centerZ;
    private boolean sorted;
    public NearestChunks(int capacity) {
        if (capacity < 1) throw new IllegalArgumentException("Empty selection capacity");
        positions = new long[capacity]; distances = new long[capacity];
        distanceCarries = new byte[capacity];
    }
    public void reset(int limit, int centerX, int centerZ) {
        if (limit < 1 || limit > positions.length) throw new IllegalArgumentException("Invalid selection limit");
        this.limit = limit; this.centerX = centerX; this.centerZ = centerZ; size = 0; sorted = false;
    }
    public void offer(long packedPosition) {
        // Heap sort destroys the max-heap. Rebuild only if a caller offers
        // another candidate after reading sorted results (not the hot path).
        if (sorted) {
            for (int index = (size >>> 1) - 1; index >= 0; index--) siftDown(index, size);
            sorted = false;
        }
        long dx = (int)packedPosition - (long)centerX;
        long dz = (int)(packedPosition >>> 32) - (long)centerZ;
        // Each square fits in unsigned 64 bits; the sum can carry once.
        long xSquare = dx * dx, zSquare = dz * dz;
        long distance = xSquare + zSquare;
        byte carry = (byte)(Long.compareUnsigned(distance, xSquare) < 0 ? 1 : 0);
        if (size < limit) {
            int index = size++;
            positions[index] = packedPosition; distances[index] = distance;
            distanceCarries[index] = carry;
            while (index > 0) {
                int parent = (index - 1) >>> 1;
                if (!greater(index, parent)) break;
                swap(index, parent); index = parent;
            }
        } else {
            int comparison = compareDistance(carry, distance, distanceCarries[0], distances[0]);
            if (comparison < 0 || (comparison == 0 && packedPosition < positions[0])) {
                positions[0] = packedPosition; distances[0] = distance;
                distanceCarries[0] = carry;
                siftDown(0, size);
            }
        }
    }
    public void sort() {
        if (sorted) return;
        for (int end = size - 1; end > 0; end--) { swap(0, end); siftDown(0, end); }
        sorted = true;
    }
    private void siftDown(int index, int end) {
        while (index * 2 + 1 < end) {
            int child = index * 2 + 1;
            if (child + 1 < end && greater(child + 1, child)) child++;
            if (!greater(child, index)) return;
            swap(index, child); index = child;
        }
    }
    private static int compareDistance(byte carryA, long lowA, byte carryB, long lowB) {
        int high = Byte.compare(carryA, carryB);
        return high != 0 ? high : Long.compareUnsigned(lowA, lowB);
    }
    private boolean greater(int a, int b) {
        int comparison = compareDistance(distanceCarries[a], distances[a],
            distanceCarries[b], distances[b]);
        return comparison > 0 || (comparison == 0 && positions[a] > positions[b]);
    }
    private void swap(int a, int b) {
        long p = positions[a]; positions[a] = positions[b]; positions[b] = p;
        long d = distances[a]; distances[a] = distances[b]; distances[b] = d;
        byte c = distanceCarries[a]; distanceCarries[a] = distanceCarries[b]; distanceCarries[b] = c;
    }
    public int size() { return size; }
    public long position(int index) {
        if (index < 0 || index >= size) throw new IndexOutOfBoundsException(index);
        return positions[index];
    }
}
