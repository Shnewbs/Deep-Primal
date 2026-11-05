package com.shnewbs.deepprimal.core.density;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class V8ChunkGenerationStampChecks {
  private static long checks;
  private static void check(boolean ok) { checks++; if (!ok) throw new AssertionError("check " + checks); }
  private static void rejects(Runnable work) {
    boolean rejected = false;
    try { work.run(); } catch (IllegalArgumentException | NullPointerException expected) { rejected = true; }
    check(rejected);
  }
  private static List<V8ChunkGenerationStamp.Observation> observations(
      V8ChunkGenerationStamp.Stamp owner, boolean generated) {
    var result = new ArrayList<V8ChunkGenerationStamp.Observation>();
    for (var dir : V8GenerationBorderAdmission.Direction.values()) {
      int x = owner.chunkX() + (dir == V8GenerationBorderAdmission.Direction.EAST ? 1 : dir == V8GenerationBorderAdmission.Direction.WEST ? -1 : 0);
      int z = owner.chunkZ() + (dir == V8GenerationBorderAdmission.Direction.SOUTH ? 1 : dir == V8GenerationBorderAdmission.Direction.NORTH ? -1 : 0);
      String stamp = generated ? new V8ChunkGenerationStamp.Stamp(owner.revision(), x, z, owner.profileFingerprint()).encode() : null;
      result.add(new V8ChunkGenerationStamp.Observation(dir, generated ? V8GenerationBorderAdmission.State.GENERATED : V8GenerationBorderAdmission.State.UNGENERATED, stamp));
    }
    return result;
  }
  public static void main(String[] args) {
    var random = new Random(20261030L);
    for (int i = 0; i < 5000; i++) {
      int x = random.nextInt(-1000000, 1000000), z = random.nextInt(-1000000, 1000000);
      int rev = 2 + random.nextInt(5);
      long fp = random.nextLong(); if (fp == 0) fp = 1;
      var owner = new V8ChunkGenerationStamp.Stamp(rev, x, z, fp);
      String wire = owner.encode();
      check(wire.equals(V8ChunkGenerationStamp.decode(wire).encode()));
      check(owner.equals(V8ChunkGenerationStamp.decode(wire)));
      var witness = V8ChunkGenerationStamp.witnessForNewChunk(owner,
          V8GenerationBorderAdmission.State.UNGENERATED, null, observations(owner, i % 2 == 0));
      check(witness.chunkX() == x && witness.chunkZ() == z);
      check(witness.savedProfileFingerprint() == fp);
      V8GenerationBorderAdmission.requireCompatible(witness, x, z, fp);
      check(witness.neighbors().size() == 4);
      String bad = wire.substring(0, wire.length()-1) + (wire.endsWith("0") ? "1" : "0");
      rejects(() -> V8ChunkGenerationStamp.decode(bad));
      rejects(() -> V8ChunkGenerationStamp.witnessForNewChunk(owner,
          V8GenerationBorderAdmission.State.GENERATED, wire, observations(owner, true)));
      rejects(() -> V8ChunkGenerationStamp.witnessForNewChunk(owner,
          V8GenerationBorderAdmission.State.UNKNOWN, null, observations(owner, true)));
      var wrong = observations(owner, true);
      wrong.set(0, new V8ChunkGenerationStamp.Observation(V8GenerationBorderAdmission.Direction.NORTH,
          V8GenerationBorderAdmission.State.GENERATED,
          new V8ChunkGenerationStamp.Stamp(rev, x, z-1, fp^1L).encode()));
      rejects(() -> V8ChunkGenerationStamp.witnessForNewChunk(owner,
          V8GenerationBorderAdmission.State.UNGENERATED, null, wrong));
      var duplicate = observations(owner, false);
      duplicate.set(0, duplicate.get(1));
      rejects(() -> V8ChunkGenerationStamp.witnessForNewChunk(owner,
          V8GenerationBorderAdmission.State.UNGENERATED, null, duplicate));
      var unknown = observations(owner, false);
      unknown.set(2, new V8ChunkGenerationStamp.Observation(V8GenerationBorderAdmission.Direction.SOUTH,
          V8GenerationBorderAdmission.State.UNKNOWN, null));
      rejects(() -> V8ChunkGenerationStamp.witnessForNewChunk(owner,
          V8GenerationBorderAdmission.State.UNGENERATED, null, unknown));
      var stampedAbsent = observations(owner, false);
      stampedAbsent.set(3, new V8ChunkGenerationStamp.Observation(V8GenerationBorderAdmission.Direction.WEST,
          V8GenerationBorderAdmission.State.UNGENERATED, wire));
      rejects(() -> V8ChunkGenerationStamp.witnessForNewChunk(owner,
          V8GenerationBorderAdmission.State.UNGENERATED, null, stampedAbsent));
      var wrongRevision = observations(owner, true);
      wrongRevision.set(1, new V8ChunkGenerationStamp.Observation(V8GenerationBorderAdmission.Direction.EAST,
          V8GenerationBorderAdmission.State.GENERATED,
          new V8ChunkGenerationStamp.Stamp(rev == 6 ? 5 : rev + 1, x + 1, z, fp).encode()));
      rejects(() -> V8ChunkGenerationStamp.witnessForNewChunk(owner,
          V8GenerationBorderAdmission.State.UNGENERATED, null, wrongRevision));
    }
    var owner = new V8ChunkGenerationStamp.Stamp(6, -1, 0, Long.MIN_VALUE);
    rejects(() -> V8ChunkGenerationStamp.decode(owner.encode().replace("|-1|", "|-01|")));
    rejects(() -> V8ChunkGenerationStamp.decode(owner.encode().toUpperCase(java.util.Locale.ROOT)));
    rejects(() -> new V8ChunkGenerationStamp.Stamp(1, 0, 0, 1));
    rejects(() -> new V8ChunkGenerationStamp.Stamp(7, 0, 0, 1));
    rejects(() -> new V8ChunkGenerationStamp.Stamp(6, 0, 0, 0));
    rejects(() -> V8ChunkGenerationStamp.witnessForNewChunk(
        new V8ChunkGenerationStamp.Stamp(6, Integer.MAX_VALUE, 0, 1),
        V8GenerationBorderAdmission.State.UNGENERATED, null, List.of()));
    System.out.println("V8_STAMP_PASS assertions=" + checks);
  }
}
