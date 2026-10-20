package com.shnewbs.deepprimal.core;

/** Small platform-independent stamina state; all quantities are simulation ticks. */
public final class SwimmingState {
  private int effort;

  public boolean tick(boolean swimming, int maximum) {
    effort = swimming ? Math.min(maximum, effort + 1) : Math.max(0, effort - 4);
    return effort >= maximum;
  }

  public int remaining(int maximum) {
    return Math.max(0, maximum - effort);
  }
}
