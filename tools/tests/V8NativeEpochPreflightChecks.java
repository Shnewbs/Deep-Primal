package com.shnewbs.deepprimal.engine;

import com.shnewbs.deepprimal.core.density.V8ChunkGenerationCommitGate;
import com.shnewbs.deepprimal.core.density.V8ChunkGenerationStamp;
import com.shnewbs.deepprimal.core.density.V8GenerationBorderAdmission;
import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;
import net.minecraft.nbt.CompoundTag;

/** Focused production coordinator test; Minecraft NBT is an API double. */
public final class V8NativeEpochPreflightChecks {
  private static long checks;
  private static void ok(boolean condition) {
    checks++;
    if (!condition) throw new AssertionError("check " + checks);
  }
  private static void reject(Runnable action) {
    checks++;
    try { action.run(); }
    catch (IllegalStateException expected) { return; }
    throw new AssertionError("exhausted epoch admitted at check " + checks);
  }
  private static final class Owner implements V8ChunkGenerationNativeCommit.Owner {
    final V8ChunkGenerationStamp.Stamp stamp;
    long epoch;
    int snapshots, publications;
    Owner(V8ChunkGenerationStamp.Stamp stamp, long epoch) { this.stamp=stamp; this.epoch=epoch; }
    @Override public V8ChunkGenerationCommitGate.Snapshot snapshot() {
      snapshots++;
      List<V8ChunkGenerationCommitGate.VersionedNeighbor> neighbors = new ArrayList<>();
      for (var direction : V8GenerationBorderAdmission.Direction.values())
        neighbors.add(new V8ChunkGenerationCommitGate.VersionedNeighbor(direction,
            V8GenerationBorderAdmission.State.UNGENERATED, null, 1L));
      return new V8ChunkGenerationCommitGate.Snapshot(
          V8GenerationBorderAdmission.State.UNGENERATED, null, epoch, neighbors);
    }
    @Override public boolean compareAndPublish(V8ChunkGenerationCommitGate.Admission admission,
        CompoundTag nbt, long nextEpoch) {
      publications++;
      if (epoch != admission.snapshot().targetEpoch() || nextEpoch != epoch + 1L) return false;
      V8ChunkGenerationNbtAdapter.requireGenerated(nbt, stamp.chunkX(), stamp.chunkZ(),
          stamp.revision(), stamp.profileFingerprint(), nextEpoch);
      epoch = nextEpoch;
      return true;
    }
  }
  public static void main(String[] args) {
    var random = new SplittableRandom(202610310630L);
    for (int i = 0; i < 12000; i++) {
      var stamp = new V8ChunkGenerationStamp.Stamp((i & 1) == 0 ? 2 : 6,
          random.nextInt(), random.nextInt(), random.nextLong() | 1L);
      var owner = new Owner(stamp, i);
      var ticket = V8ChunkGenerationNativeCommit.begin(owner, stamp);
      ok(owner.snapshots == 1);
      ok(owner.publications == 0);
      var tag = new CompoundTag();
      var generated = V8ChunkGenerationNativeCommit.commit(ticket, owner, tag);
      ok(generated.epoch() == i + 1L);
      ok(owner.epoch == i + 1L);
      ok(owner.publications == 1);
      ok(V8ChunkGenerationNbtAdapter.readGenerated(tag).equals(generated));
    }
    var stamp = new V8ChunkGenerationStamp.Stamp(6, -91, 57, 42L);
    var max = new Owner(stamp, Long.MAX_VALUE);
    reject(() -> V8ChunkGenerationNativeCommit.begin(max, stamp));
    ok(max.snapshots == 1);
    ok(max.publications == 0);
    var almost = new Owner(stamp, Long.MAX_VALUE - 1L);
    var ticket = V8ChunkGenerationNativeCommit.begin(almost, stamp);
    ok(ticket.intended().equals(stamp));
    var tag = new CompoundTag();
    var generated = V8ChunkGenerationNativeCommit.commit(ticket, almost, tag);
    ok(generated.epoch() == Long.MAX_VALUE);
    ok(almost.epoch == Long.MAX_VALUE);
    ok(almost.publications == 1);
    reject(() -> V8ChunkGenerationNativeCommit.begin(almost, stamp));
    ok(almost.publications == 1);
    System.out.println("V8_NATIVE_EPOCH_PREFLIGHT_PASS assertions=" + checks);
  }
}
