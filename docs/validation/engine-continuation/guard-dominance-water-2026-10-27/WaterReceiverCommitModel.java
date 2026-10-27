import java.util.Random;

/** Isolated callback admission model, NOT the compiled Minecraft water implementation. */
public final class WaterReceiverCommitModel {
  static int checked;
  static void check(boolean ok, String why) {
    checked++;
    if (!ok) throw new AssertionError(why);
  }
  static final class World {
    int a = 10, b = 2, chemistryWrites, sourceWrites, receiverWrites;
    boolean loadedA = true, loadedB = true, rejectB;
    boolean replaceBOnSource, editAOnSource, unloadBOnSource;
    boolean writeA(int value) {
      if (!loadedA) return false;
      a = value;
      sourceWrites++;
      if (sourceWrites == 1) {
        if (replaceBOnSource) b = -5;
        if (editAOnSource) a = -7;
        if (unloadBOnSource) loadedB = false;
      }
      return true;
    }
    boolean writeB(int value) {
      if (!loadedB || rejectB) return false;
      b = value;
      receiverWrites++;
      return true;
    }
  }
  static boolean transfer(World w, int moved) {
    if (moved <= 0 || moved > w.a || w.b + moved > 16 || !w.loadedA || !w.loadedB) return false;
    int beforeA = w.a, beforeB = w.b;
    int appliedA = beforeA - moved;
    if (!w.writeA(appliedA)) return false;
    if (!w.loadedA || w.a != appliedA)
      throw new IllegalStateException("Finite-water source replaced during commit");
    if (!w.loadedB || w.b != beforeB || !w.writeB(beforeB + moved)) {
      if (!w.loadedA || w.a != appliedA || !w.writeA(beforeA))
        throw new IllegalStateException("Finite-water transfer rollback lost source ownership");
      return false;
    }
    w.chemistryWrites++;
    return true;
  }
  public static void main(String[] args) {
    World ok = new World();
    check(transfer(ok, 2), "normal transfer");
    check(ok.a == 8 && ok.b == 4 && ok.chemistryWrites == 1, "normal conservation");
    World fail = new World(); fail.rejectB = true;
    check(!transfer(fail, 2), "rejected receiver");
    check(fail.a == 10 && fail.b == 2 && fail.chemistryWrites == 0, "source rollback");
    World replaced = new World(); replaced.replaceBOnSource = true;
    check(!transfer(replaced, 2), "changed receiver");
    check(replaced.a == 10 && replaced.b == -5 && replaced.receiverWrites == 0,
          "do not overwrite replacement");
    World unloaded = new World(); unloaded.unloadBOnSource = true;
    check(!transfer(unloaded, 2), "unloaded receiver");
    check(unloaded.a == 10 && unloaded.b == 2 && unloaded.chemistryWrites == 0,
          "unloaded rollback");
    World staleSource = new World(); staleSource.editAOnSource = true;
    boolean threw = false;
    try { transfer(staleSource, 2); } catch (IllegalStateException e) { threw = true; }
    check(threw && staleSource.a == -7, "do not clobber reentrant source edit");
    World staleQty = new World();
    check(!transfer(staleQty, 19) && staleQty.sourceWrites == 0, "reject stale quantity");
    Random random = new Random(0xD33D38);
    for (int i = 0; i < 100_000; i++) {
      World world = new World();
      world.a = 1 + random.nextInt(16);
      world.b = random.nextInt(17-world.a);
      int total = world.a + world.b;
      int moved = Math.min(world.a, 16-world.b);
      int event = random.nextInt(4);
      world.rejectB = event == 1;
      world.replaceBOnSource = event == 2;
      world.unloadBOnSource = event == 3;
      int startingB = world.b;
      boolean delivered = transfer(world, moved);
      check(world.a + (world.b < 0 ? startingB : world.b) == total || world.b == -5,
            "quantity model accounting");
      check(world.chemistryWrites == (delivered ? 1 : 0), "commit only once");
      if (event == 2) check(world.b == -5 && !delivered, "receiver edit preserved");
      else if (event == 1 || event == 3)
        check(!delivered && world.a == total - startingB, "source restoration");
    }
    System.out.println("WATER_RECEIVER_COMMIT_MODEL_PASS cases=" + checked);
  }
}
