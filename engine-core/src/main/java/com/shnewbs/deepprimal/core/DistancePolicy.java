package com.shnewbs.deepprimal.core;

import java.util.OptionalInt;

/** Bounded integer input and native distance limits. Simulation has a separate level graph. */
public final class DistancePolicy {
    // The native tracker stores distance + 2 in a signed byte.
    public static final int MAX_RENDER = Byte.MAX_VALUE - 2;
    public static final int MAX_SIMULATION = 32;
    private DistancePolicy() {}
    public static OptionalInt parse(String text, int maximum) {
        return parse(text, 2, maximum);
    }
    public static OptionalInt parse(String text, int minimum, int maximum) {
        if (text.isEmpty() || text.length() > 10) return OptionalInt.empty();
        long parsed = 0;
        for (int i = 0; i < text.length(); i++) {
            char digit = text.charAt(i);
            if (digit < '0' || digit > '9') return OptionalInt.empty();
            parsed = parsed * 10 + digit - '0';
            if (parsed > maximum) return OptionalInt.empty();
        }
        int value = (int)parsed;
        return value >= minimum && value <= maximum ? OptionalInt.of(value) : OptionalInt.empty();
    }
    public static int clampRender(int value) { return Math.clamp(value, 2, MAX_RENDER); }
    public static int clampSimulation(int value) { return Math.clamp(value, 2, MAX_SIMULATION); }
    public static int queuePriorityCount(int nativeCount, int trackingCapacity) {
        // Tracker priorities include distance + 2 when a player leaves a chunk.
        // The queue's empty sentinel is one past the last valid bucket.
        return Math.max(nativeCount, clampRender(trackingCapacity) + 3);
    }
    public static long sectionSlots(int distance, int verticalSections) {
        long side = 2L * distance + 1;
        return side * side * verticalSections;
    }
}
