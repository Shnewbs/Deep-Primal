/** Independent, isolated Java-21 reproduction of the commit ordering in
 * WaterRuntime.transfer at pinned Deep-Primal commit 227f53b7.
 * Models native setBlock callbacks; it does NOT compile the NeoForge runtime.
 */
public final class ReceiverAfterWriteReentrancyProbe {
  static final class World {
    int source = 10, receiver = 2, compositionWrites;
    int receiverWrites;
    boolean sourceLoaded = true, receiverLoaded = true;
    boolean replaceReceiverOnSourceWrite;
    boolean replaceReceiverOnReceiverWrite;
    boolean replaceSourceOnReceiverWrite;
    boolean unloadReceiverOnReceiverWrite;
    boolean writeSource(int value) {
      if (!sourceLoaded) return false;
      source = value;
      if (replaceReceiverOnSourceWrite) receiver = -5;
      return true;
    }
    boolean writeReceiver(int value) {
      if (!receiverLoaded) return false;
      receiver = value;
      receiverWrites++;
      if (replaceReceiverOnReceiverWrite) receiver = -5;
      if (replaceSourceOnReceiverWrite) source = -7;
      if (unloadReceiverOnReceiverWrite) receiverLoaded = false;
      return true;
    }
  }
  static boolean current(World w, int move) {
    if (move <= 0 || !w.sourceLoaded || !w.receiverLoaded) return false;
    int beforeA = w.source, beforeB = w.receiver;
    if (move > beforeA || beforeB < 0 || beforeB + move > 16) return false;
    int appliedA = beforeA - move;
    if (!w.writeSource(appliedA)) return false;
    if (!w.sourceLoaded || w.source != appliedA)
      throw new IllegalStateException("Finite-water source replaced during commit");
    if (!w.receiverLoaded || w.receiver != beforeB || !w.writeReceiver(beforeB + move)) {
      if (!w.sourceLoaded || w.source != appliedA || !w.writeSource(beforeA))
        throw new IllegalStateException("Finite-water transfer rollback lost source ownership");
      return false;
    }
    w.compositionWrites++;
    return true;
  }
  static void require(boolean ok, String name) {
    if (!ok) throw new AssertionError(name);
  }
  public static void main(String[] args) {
    int cases = 0;
    World control = new World();
    require(current(control, 2), "normal transfer");
    require(control.source == 8 && control.receiver == 4 && control.compositionWrites == 1,
        "normal transfer state");
    cases++;
    World prior = new World(); prior.replaceReceiverOnSourceWrite = true;
    require(!current(prior, 2) && prior.source == 10 && prior.receiver == -5
        && prior.compositionWrites == 0, "dev38 pre-receiver-write guard");
    cases++;
    World receiver = new World(); receiver.replaceReceiverOnReceiverWrite = true;
    require(current(receiver, 2) && receiver.receiver == -5 && receiver.source == 8
        && receiver.compositionWrites == 1, "receiver edited after write is incorrectly accepted");
    cases++;
    World source = new World(); source.replaceSourceOnReceiverWrite = true;
    require(current(source, 2) && source.source == -7 && source.receiver == 4
        && source.compositionWrites == 1, "source edited on receiver callback incorrectly accepted");
    cases++;
    World unload = new World(); unload.unloadReceiverOnReceiverWrite = true;
    require(current(unload, 2) && !unload.receiverLoaded
        && unload.compositionWrites == 1, "receiver unload after write incorrectly accepted");
    cases++;
    System.out.println("REENTRANT_AFTER_RECEIVER_WRITE_REPRODUCED " + cases + "/" + cases
      + " model_cases; 3 unguarded callback transitions; NOT A NATIVE GAME REPRODUCTION");
  }
}
