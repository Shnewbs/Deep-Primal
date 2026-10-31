package com.shnewbs.deepprimal.engine;

import com.shnewbs.deepprimal.core.density.V8ChunkGenerationCommitGate;
import com.shnewbs.deepprimal.core.density.V8ChunkGenerationStamp;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.nbt.CompoundTag;

/**
 * Explicit V8 new-chunk admission and publication coordinator.
 *
 * <p>Call {@link #begin} before native materialization and {@link #commit} only
 * after every block, heightmap and callback has succeeded. The owner MUST
 * compare the target and four neighbor durable epochs under the SAME native
 * transaction/lock that publishes completed chunk bytes and candidate NBT.
 * This coordinator cannot make a native save atomic, register a chunk hook,
 * or roll back side effects from a failed native setter. On any failure the
 * caller must discard the unpublished chunk candidate; never retry its ticket.
 */
public final class V8ChunkGenerationNativeCommit {
  private V8ChunkGenerationNativeCommit() {}

  /** Native owner, including its durable target and four-neighbor journal. */
  public interface Owner {
    V8ChunkGenerationCommitGate.Snapshot snapshot();

    /**
     * Compare the admission's five durable epochs and publish completed chunk
     * bytes plus the marked NBT in ONE atomic transaction. Return false if
     * anything changed. Implementations MUST advance the target epoch to
     * {@code nextTargetEpoch} durably on success; an in-memory CAS is not enough.
     */
    boolean compareAndPublish(V8ChunkGenerationCommitGate.Admission admission,
                              CompoundTag completedChunkNbt, long nextTargetEpoch);
  }

  /** Single-use ticket bound to the exact native owner and admission. */
  public static final class Ticket {
    private final Owner owner;
    private final V8ChunkGenerationCommitGate.Admission admission;
    private final AtomicBoolean consumed = new AtomicBoolean();

    private Ticket(Owner owner, V8ChunkGenerationCommitGate.Admission admission) {
      this.owner = owner;
      this.admission = admission;
    }

    public V8ChunkGenerationStamp.Stamp intended() { return admission.intended(); }
  }

  /** Must precede native materialization; UNKNOWN/legacy borders fail closed. */
  public static Ticket begin(Owner owner, V8ChunkGenerationStamp.Stamp intended) {
    Objects.requireNonNull(owner, "native V8 chunk owner");
    Objects.requireNonNull(intended, "intended V8 generation identity");
    return new Ticket(owner, V8ChunkGenerationCommitGate.prepare(intended,
        Objects.requireNonNull(owner.snapshot(), "durable V8 admission snapshot")));
  }

  /**
   * One-shot post-materialization publication attempt. Recheck the durable
   * snapshot before mutating candidate NBT, then require a second comparison
   * inside the owner's atomic compare-and-publish implementation. A successful
   * callback is an owner assertion, NOT proof of a completed disk flush.
   */
  public static V8ChunkGenerationNbtAdapter.Generated commit(
      Ticket ticket, Owner owner, CompoundTag completedCandidateNbt) {
    Objects.requireNonNull(ticket, "V8 generation ticket");
    Objects.requireNonNull(owner, "native V8 chunk owner");
    Objects.requireNonNull(completedCandidateNbt, "completed candidate NBT");
    if (ticket.owner != owner)
      throw new IllegalArgumentException("V8 generation ticket belongs to another owner");
    if (!ticket.consumed.compareAndSet(false, true))
      throw new IllegalStateException("V8 generation ticket already consumed");
    var admission = ticket.admission;
    V8ChunkGenerationCommitGate.requireUnchanged(admission,
        Objects.requireNonNull(owner.snapshot(), "durable V8 commit snapshot"));
    long nextEpoch;
    try {
      nextEpoch = Math.addExact(admission.snapshot().targetEpoch(), 1L);
    } catch (ArithmeticException overflow) {
      throw new IllegalStateException("V8 durable target epoch exhausted", overflow);
    }
    var generated = new V8ChunkGenerationNbtAdapter.Generated(admission.intended(), nextEpoch);
    V8ChunkGenerationNbtAdapter.writeNewGenerated(completedCandidateNbt, generated);
    if (!owner.compareAndPublish(admission, completedCandidateNbt, nextEpoch))
      throw new IllegalStateException("V8 native atomic chunk/stamp publication rejected");
    return generated;
  }
}
