package com.shnewbs.deepprimal.core;
import java.util.concurrent.CountDownLatch;
/** Only linked into the isolated, instrumented regression build; never committed to production. */
final class RaceGate {
  static volatile CountDownLatch entered = new CountDownLatch(1);
  static volatile CountDownLatch release = new CountDownLatch(1);
  static void reset() {entered = new CountDownLatch(1);release = new CountDownLatch(1);}
  static void pause() {
    entered.countDown();
    try {release.await();}
    catch (InterruptedException e) {Thread.currentThread().interrupt();throw new AssertionError(e);}
  }
}
