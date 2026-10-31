package com.shnewbs.deepprimal.engine;

import com.shnewbs.deepprimal.core.density.DensityChunk;
import com.shnewbs.deepprimal.core.density.V8ChunkGenerationStamp;
import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.chunk.ChunkAccess;

/**
 * Explicit new-V8 chunk lifecycle: profile admission, durable border ticket,
 * native materialization, then owner-controlled chunk/stamp publication.
 *
 * <p>This is deliberately NOT a registered generator or chunk-save hook.
 * Callers must supply a real authoritative durable owner and a discard action
 * that removes the entire unpublished native chunk candidate, not just its NBT.
 * A rejected publication cannot be retried with the same chunk or ticket.
 * The owner must still atomically publish chunk bytes, marker and target epoch;
 * this coordinator alone does not make a disk transaction crash-safe.
 */
public final class V8ChunkGenerationNativeFill {
  private V8ChunkGenerationNativeFill() {}

  @FunctionalInterface
  public interface Discarder {
    void discard(ChunkAccess unpublishedChunk, CompoundTag unpublishedNbt);
  }

  public record Published<T>(T result, V8ChunkGenerationNbtAdapter.Generated identity) {
    public Published {
      Objects.requireNonNull(result, "V8 native materialization result");
      Objects.requireNonNull(identity, "V8 committed generation identity");
    }
  }

  /**
   * A single admitted native terrain write and durable publication attempt.
   * The saved-profile identity and chunk dimensions are checked before the
   * owner's border snapshot or any native setter. The ticket is acquired
   * BEFORE the first materializer callback and consumed AFTER all writes.
   */
  public static Published<DensityChunk.Planned> fillSavedSnapshotAndCommit(
      V8GeneratorSnapshot.Prepared prepared, long expectedSavedProfileFingerprint,
      V8ChunkGenerationNativeCommit.Owner owner,
      V8ChunkGenerationStamp.Stamp intended, ChunkAccess candidate,
      int queriesPerSlab, CompoundTag candidateNbt, Discarder discard) {
    requireIdentity(prepared, expectedSavedProfileFingerprint, intended, candidate,
        V8GeneratorSnapshot.BLENDED_VERSION, false);
    return execute(owner, intended, candidate, candidateNbt, discard,
        () -> DensityChunkMaterializer.fillSavedSnapshot(prepared,
            expectedSavedProfileFingerprint, candidate, queriesPerSlab));
  }

  /**
   * Equivalent opt-in V8-r6 vegetation/receipt lifecycle. The realized native
   * receipt is returned only after the same candidate's durable publication.
   * The existing V8-r6 prewrite rollback remains the materializer's duty.
   */
  public static Published<DensityChunkMaterializer.V8VegetatedReceiptResult>
      fillSavedVegetationAndCommit(
          V8GeneratorSnapshot.Prepared prepared, long expectedSavedProfileFingerprint,
          V8ChunkGenerationNativeCommit.Owner owner,
          V8ChunkGenerationStamp.Stamp intended, ChunkAccess candidate,
          int queriesPerSlab, CompoundTag candidateNbt, Discarder discard,
          V8EcotoneColumnPlan.ClimateSource referenceClimate,
          V8WaterDomainAdmission.Authority waterAuthority) {
    requireIdentity(prepared, expectedSavedProfileFingerprint, intended, candidate,
        V8GeneratorSnapshot.VEGETATION_VERSION, true);
    Objects.requireNonNull(referenceClimate, "V8 reference climate");
    Objects.requireNonNull(waterAuthority, "V8 water-domain authority");
    return execute(owner, intended, candidate, candidateNbt, discard,
        () -> DensityChunkMaterializer.fillSavedEcotoneVegetatedWithReceipt(
            prepared, expectedSavedProfileFingerprint, candidate, queriesPerSlab,
            referenceClimate, waterAuthority));
  }

  private static void requireIdentity(V8GeneratorSnapshot.Prepared prepared,
      long expectedFingerprint, V8ChunkGenerationStamp.Stamp intended,
      ChunkAccess candidate, int minimumRevision, boolean exactRevision) {
    Objects.requireNonNull(prepared, "V8 saved prepared profile");
    Objects.requireNonNull(intended, "V8 intended generation stamp");
    Objects.requireNonNull(candidate, "V8 native candidate chunk");
    prepared.requireSavedProfileFingerprint(expectedFingerprint);
    int revision = prepared.snapshot().version();
    if ((exactRevision ? revision != minimumRevision : revision < minimumRevision)
        || intended.revision() != revision
        || intended.profileFingerprint() != expectedFingerprint
        || intended.chunkX() != candidate.getPos().x
        || intended.chunkZ() != candidate.getPos().z)
      throw new IllegalArgumentException("V8 saved profile/chunk generation identity mismatch");
    // ChunkPos uses int block coordinates; its usual left shift can wrap at
    // extreme signed chunk coordinates. Refuse aliases before any native setter.
    requireWorldCoordinateRange(intended.chunkX(), intended.chunkZ());
    prepared.requireHeight(candidate.getMinY(), candidate.getHeight());
  }

  private static void requireWorldCoordinateRange(int chunkX, int chunkZ) {
    try {
      int originX = Math.multiplyExact(chunkX, DensityChunk.WIDTH);
      int originZ = Math.multiplyExact(chunkZ, DensityChunk.WIDTH);
      Math.addExact(originX, DensityChunk.WIDTH - 1);
      Math.addExact(originZ, DensityChunk.WIDTH - 1);
    } catch (ArithmeticException overflow) {
      throw new IllegalArgumentException("V8 native chunk world-coordinate overflow", overflow);
    }
  }

  private static <T> Published<T> execute(
      V8ChunkGenerationNativeCommit.Owner owner,
      V8ChunkGenerationStamp.Stamp intended, ChunkAccess candidate,
      CompoundTag candidateNbt, Discarder discard, Supplier<T> materialize) {
    Objects.requireNonNull(owner, "V8 authoritative native owner");
    Objects.requireNonNull(candidateNbt, "V8 private candidate NBT");
    Objects.requireNonNull(discard, "V8 mandatory candidate discard action");
    // Reject reused candidates before a ticket or native setter. The owner
    // must still certify UNGENERATED independently from absent NBT.
    V8ChunkGenerationNbtAdapter.requireFreshCandidate(candidateNbt);
    // begin() rejects UNKNOWN, legacy, missing or changed border observations.
    // No native block write may precede this call.
    var ticket = V8ChunkGenerationNativeCommit.begin(owner, intended);
    try {
      T result = Objects.requireNonNull(materialize.get(), "V8 materialization result");
      var committed = V8ChunkGenerationNativeCommit.commit(ticket, owner, candidateNbt);
      return new Published<>(result, committed);
    } catch (RuntimeException | Error failure) {
      try {
        discard.discard(candidate, candidateNbt);
      } catch (RuntimeException | Error discardFailure) {
        // A discarder may rethrow the original failure instance. Java forbids
        // self-suppression; preserve that original failure unchanged.
        if (discardFailure != failure) failure.addSuppressed(discardFailure);
      }
      throw failure;
    }
  }
}
