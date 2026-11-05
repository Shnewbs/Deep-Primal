package com.shnewbs.deepprimal.core.density;
import java.util.List;
public final class V8ChunkGenerationCommitGate {
  public record Snapshot(long targetEpoch, long[] neighborEpochs) {}
  public static final class Admission {
    private final V8ChunkGenerationStamp.Stamp stamp; private final Snapshot snapshot;
    Admission(V8ChunkGenerationStamp.Stamp s, Snapshot snap) {stamp=s; snapshot=snap;}
    public V8ChunkGenerationStamp.Stamp intended(){return stamp;}
    public Snapshot snapshot(){return snapshot;}
  }
  public static Admission prepare(V8ChunkGenerationStamp.Stamp stamp, Snapshot snapshot){
    if(snapshot.neighborEpochs().length!=4)throw new IllegalArgumentException();
    return new Admission(stamp,snapshot);
  }
  public static void requireUnchanged(Admission admission, Snapshot latest){
    if(admission.snapshot.targetEpoch()!=latest.targetEpoch() ||
       !java.util.Arrays.equals(admission.snapshot.neighborEpochs(),latest.neighborEpochs()))
      throw new IllegalStateException("changed");
  }
}
