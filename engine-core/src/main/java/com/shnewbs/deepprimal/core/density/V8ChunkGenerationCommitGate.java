package com.shnewbs.deepprimal.core.density;

import java.util.EnumMap;
import java.util.List;
import java.util.Objects;

/**
 * Explicit V8 chunk-stamp publication gate for a native durable chunk owner.
 * Admission alone is insufficient: neighbors may be generated or edited while
 * density is sampled. Capture owner-maintained, monotonically changing epochs
 * before materialization and compare them under the owner's atomic commit lock.
 *
 * <p>This class owns no NBT, chunk lock, disk transaction or native writes.
 * The owner must publish completed chunk data and its stamp atomically and
 * must increment epochs on every relevant mutation, including failed/retried
 * writes and ABA transitions. An in-memory epoch is not a durable witness.
 */
public final class V8ChunkGenerationCommitGate {
  private V8ChunkGenerationCommitGate() {}

  public record VersionedNeighbor(V8GenerationBorderAdmission.Direction direction,
                                  V8GenerationBorderAdmission.State state,
                                  String persistedStamp, long epoch) {
    public VersionedNeighbor {
      Objects.requireNonNull(direction, "direction");
      Objects.requireNonNull(state, "state");
      if (epoch < 0L) throw new IllegalArgumentException("negative V8 neighbor epoch");
      // A generated neighbor must have passed an atomic publication, which
      // advances its durable epoch from a nonnegative value at least once.
      if (state == V8GenerationBorderAdmission.State.GENERATED && epoch == 0L)
        throw new IllegalArgumentException("generated V8 neighbor requires positive durable epoch");
    }
  }

  /** A single consistent snapshot from the authoritative durable owner. */
  public record Snapshot(V8GenerationBorderAdmission.State targetState,
                         String targetPersistedStamp, long targetEpoch,
                         List<VersionedNeighbor> neighbors) {
    public Snapshot {
      Objects.requireNonNull(targetState, "targetState");
      if (targetEpoch < 0L) throw new IllegalArgumentException("negative V8 target epoch");
      if (targetState == V8GenerationBorderAdmission.State.GENERATED && targetEpoch == 0L)
        throw new IllegalArgumentException("generated V8 target requires positive durable epoch");
      neighbors = List.copyOf(Objects.requireNonNull(neighbors, "neighbors"));
    }
  }

  /** Immutable admission token; the caller cannot substitute a later stamp. */
  public static final class Admission {
    private final V8ChunkGenerationStamp.Stamp intended;
    private final Snapshot snapshot;
    private final EnumMap<V8GenerationBorderAdmission.Direction, VersionedNeighbor> byDirection;

    private Admission(V8ChunkGenerationStamp.Stamp intended, Snapshot snapshot,
                      EnumMap<V8GenerationBorderAdmission.Direction, VersionedNeighbor> byDirection) {
      this.intended = intended;
      this.snapshot = snapshot;
      this.byDirection = byDirection;
    }

    public V8ChunkGenerationStamp.Stamp intended() { return intended; }
    public Snapshot snapshot() { return snapshot; }
    public String encodedStamp() { return intended.encode(); }
  }

  /** Preflight before any native block write. UNKNOWN and legacy fail closed. */
  public static Admission prepare(V8ChunkGenerationStamp.Stamp intended, Snapshot snapshot) {
    Objects.requireNonNull(intended, "intended");
    Objects.requireNonNull(snapshot, "snapshot");
    var byDirection = checkedDirections(snapshot);
    V8ChunkGenerationStamp.witnessForNewChunk(intended, snapshot.targetState(),
        snapshot.targetPersistedStamp(), toObservations(snapshot));
    return new Admission(intended, snapshot, byDirection);
  }

  /**
   * Recheck after materialization, before publishing. This is a diagnostic
   * precondition, NOT an atomic commit: the native owner must compare again
   * under the same lock/transaction that publishes chunk data and the stamp.
   */
  public static void requireUnchanged(Admission admission, Snapshot latest) {
    Objects.requireNonNull(admission, "admission");
    Objects.requireNonNull(latest, "latest");
    V8ChunkGenerationStamp.witnessForNewChunk(admission.intended,
        latest.targetState(), latest.targetPersistedStamp(), toObservations(latest));
    var current = checkedDirections(latest);
    if (admission.snapshot.targetEpoch() != latest.targetEpoch())
      throw new IllegalStateException("V8 target changed during materialization");
    for (var direction : V8GenerationBorderAdmission.Direction.values()) {
      VersionedNeighbor original = admission.byDirection.get(direction);
      VersionedNeighbor observed = current.get(direction);
      if (original.epoch() != observed.epoch()
          || original.state() != observed.state()
          || !Objects.equals(original.persistedStamp(), observed.persistedStamp()))
        throw new IllegalStateException("V8 border changed during materialization: " + direction);
    }
  }

  /**
   * The owner must CAS all five durable epochs and atomically publish both
   * completed chunk bytes and encodedStamp(). A false result rejects the
   * candidate; the caller must discard/recreate any uncommitted native chunk.
   */
  @FunctionalInterface
  public interface AtomicPublisher {
    boolean publishCompletedChunkIfUnchanged(Admission admission);
  }

  public static void requirePublished(Admission admission, AtomicPublisher publisher) {
    Objects.requireNonNull(admission, "admission");
    Objects.requireNonNull(publisher, "publisher");
    if (!publisher.publishCompletedChunkIfUnchanged(admission))
      throw new IllegalStateException("V8 atomic chunk/stamp publication rejected");
  }

  private static List<V8ChunkGenerationStamp.Observation> toObservations(Snapshot snapshot) {
    return snapshot.neighbors().stream().map(n ->
        new V8ChunkGenerationStamp.Observation(n.direction(), n.state(), n.persistedStamp()))
        .toList();
  }

  private static EnumMap<V8GenerationBorderAdmission.Direction, VersionedNeighbor>
      checkedDirections(Snapshot snapshot) {
    var byDirection = new EnumMap<V8GenerationBorderAdmission.Direction, VersionedNeighbor>(
        V8GenerationBorderAdmission.Direction.class);
    for (VersionedNeighbor neighbor : snapshot.neighbors())
      if (byDirection.putIfAbsent(neighbor.direction(), neighbor) != null)
        throw new IllegalArgumentException("duplicate V8 neighbor direction");
    if (byDirection.size() != V8GenerationBorderAdmission.Direction.values().length)
      throw new IllegalArgumentException("incomplete V8 neighbor observations");
    return byDirection;
  }
}
