package com.shnewbs.deepprimal.core;

import java.util.Arrays;
import java.util.NoSuchElementException;

/**
 * Min-heap of lattice indices for the regional priority flood.
 *
 * <p>Order is {@code Double.compare(key[a], key[b])} then {@code Integer.compare(a, b)}, exactly
 * the comparator the flood previously gave a boxed {@code PriorityQueue<Integer>}. Because the
 * index tie-break makes that a strict total order over distinct nodes, every correct heap pops the
 * same sequence, so replacing the queue cannot change routing, order or any saved-world output.
 * Keys must not change while an index is queued (the flood sets {@code flood[b]} before adding
 * {@code b} and adds each node once).
 */
final class FloodQueue {
  private final double[] key;
  private int[] heap;
  private int size;

  FloodQueue(double[] key, int capacity) {
    this.key = key;
    this.heap = new int[Math.max(16, capacity)];
  }

  boolean isEmpty() {
    return size == 0;
  }

  void add(int node) {
    if (size == heap.length) heap = Arrays.copyOf(heap, size * 2);
    int hole = size++;
    while (hole > 0) {
      int parent = (hole - 1) >>> 1, above = heap[parent];
      if (compare(node, above) >= 0) break;
      heap[hole] = above;
      hole = parent;
    }
    heap[hole] = node;
  }

  int remove() {
    if (size == 0) throw new NoSuchElementException();
    int top = heap[0], last = heap[--size];
    if (size > 0) {
      int hole = 0, half = size >>> 1;
      while (hole < half) {
        int child = 2 * hole + 1, right = child + 1;
        if (right < size && compare(heap[right], heap[child]) < 0) child = right;
        if (compare(last, heap[child]) <= 0) break;
        heap[hole] = heap[child];
        hole = child;
      }
      heap[hole] = last;
    }
    return top;
  }

  private int compare(int a, int b) {
    int byKey = Double.compare(key[a], key[b]);
    return byKey != 0 ? byKey : Integer.compare(a, b);
  }
}
