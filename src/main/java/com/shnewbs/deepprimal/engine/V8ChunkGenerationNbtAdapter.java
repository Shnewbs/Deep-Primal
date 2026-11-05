package com.shnewbs.deepprimal.engine;

import com.shnewbs.deepprimal.core.density.V8ChunkGenerationCommitGate;
import com.shnewbs.deepprimal.core.density.V8ChunkGenerationStamp;
import com.shnewbs.deepprimal.core.density.V8GenerationBorderAdmission;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.zip.CRC32;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

/**
 * Strict NBT adapter for a native owner's V8 chunk-generation identity.
 *
 * <p>This is an opt-in codec for a caller-owned chunk NBT compound, NOT a
 * chunk-save hook or an atomic disk transaction. The native chunk owner must
 * durably commit the completed chunk and this marker together, and must own
 * monotonic epochs and consistent target/four-neighbor snapshots. A missing
 * marker NEVER proves that a chunk is ungenerated: the caller must certify
 * that state independently from the authoritative chunk lifecycle.
 */
public final class V8ChunkGenerationNbtAdapter {
  public static final String KEY = "deepprimal_v8_generation";
  private static final String PREFIX = "dp-v8-nbt/1";
  private static final int MAX_LENGTH = 160;

  private V8ChunkGenerationNbtAdapter() {}

  public record Generated(V8ChunkGenerationStamp.Stamp stamp, long epoch) {
    public Generated {
      Objects.requireNonNull(stamp, "V8 generation stamp");
      if (epoch <= 0L)
        throw new IllegalArgumentException("generated V8 epoch must be positive");
    }

    public String encode() {
      String body = PREFIX + "|" + epoch + "|" + stamp.encode();
      return body + "|" + String.format(java.util.Locale.ROOT, "%08x", crc(body));
    }
  }

  /** Rejects missing, wrong-typed, corrupt, noncanonical or future markers. */
  public static Generated readGenerated(CompoundTag nbt) {
    Objects.requireNonNull(nbt, "authoritative V8 chunk NBT");
    Tag value = nbt.get(KEY);
    if (!(value instanceof StringTag))
      throw new IllegalStateException("missing or non-string V8 generation marker");
    String encoded = nbt.getStringOr(KEY, "");
    if (encoded.length() > MAX_LENGTH)
      throw new IllegalArgumentException("oversized V8 NBT generation marker");
    String[] parts = encoded.split("\\|", -1);
    if (parts.length != 9 || !PREFIX.equals(parts[0])
        || !parts[1].matches("[1-9][0-9]*")
        || !parts[8].matches("[0-9a-f]{8}"))
      throw new IllegalArgumentException("invalid V8 NBT generation envelope");
    try {
      long epoch = Long.parseLong(parts[1]);
      StringBuilder stampText = new StringBuilder(parts[2]);
      for (int i = 3; i <= 7; i++) stampText.append('|').append(parts[i]);
      Generated generated = new Generated(V8ChunkGenerationStamp.decode(stampText.toString()), epoch);
      if (!generated.encode().equals(encoded))
        throw new IllegalArgumentException("noncanonical or corrupt V8 NBT generation marker");
      return generated;
    } catch (NumberFormatException failure) {
      throw new IllegalArgumentException("invalid durable V8 generation epoch", failure);
    }
  }

  /** Check identity against the exact reopened saved world and signed chunk. */
  public static Generated requireGenerated(CompoundTag nbt, int chunkX, int chunkZ,
                                           int revision, long profileFingerprint,
                                           long expectedDurableEpoch) {
    Generated actual = readGenerated(nbt);
    V8ChunkGenerationStamp.Stamp stamp = actual.stamp();
    if (actual.epoch() != expectedDurableEpoch || stamp.chunkX() != chunkX
        || stamp.chunkZ() != chunkZ || stamp.revision() != revision
        || stamp.profileFingerprint() != profileFingerprint)
      throw new IllegalStateException("V8 NBT chunk/profile/epoch mismatch");
    return actual;
  }

  /**
   * Write ONLY to a private, not-yet-published candidate NBT compound.
   * Never overwrite an existing marker; no existing chunk is migrated.
   * The owner must perform durable epoch CAS and publish chunk bytes plus
   * candidate NBT atomically. This method cannot provide that transaction.
   */
  public static void writeNewGenerated(CompoundTag candidate, Generated generated) {
    requireFreshCandidate(candidate);
    Objects.requireNonNull(generated, "V8 generated identity");
    candidate.putString(KEY, generated.encode());
  }

  /**
   * Reject reused or previously stamped candidates BEFORE native writes or
   * the owner's border-ticket acquisition. Any marker type is a rejection.
   * Marker absence alone never certifies an ungenerated native chunk.
   */
  public static void requireFreshCandidate(CompoundTag candidate) {
    Objects.requireNonNull(candidate, "candidate V8 chunk NBT");
    if (candidate.get(KEY) != null)
      throw new IllegalStateException("V8 generation marker already exists");
  }

  /**
   * Convert a native owner's CERTIFIED state into the existing commit gate.
   * An absent NBT marker cannot be classified as UNGENERATED without a
   * separate durable lifecycle observation supplied by that owner.
   */
  public static V8ChunkGenerationCommitGate.VersionedNeighbor neighbor(
      V8GenerationBorderAdmission.Direction direction,
      V8GenerationBorderAdmission.State certifiedState,
      CompoundTag authoritativeNbt, long durableEpoch,
      int chunkX, int chunkZ, int revision, long profileFingerprint) {
    Objects.requireNonNull(direction, "V8 border direction");
    Objects.requireNonNull(certifiedState, "certified V8 neighbor state");
    Objects.requireNonNull(authoritativeNbt, "neighbor NBT");
    if (durableEpoch < 0L)
      throw new IllegalArgumentException("negative durable V8 neighbor epoch");
    if (certifiedState == V8GenerationBorderAdmission.State.GENERATED) {
      Generated generated = requireGenerated(authoritativeNbt, chunkX, chunkZ,
          revision, profileFingerprint, durableEpoch);
      return new V8ChunkGenerationCommitGate.VersionedNeighbor(
          direction, certifiedState, generated.stamp().encode(), durableEpoch);
    }
    if (certifiedState != V8GenerationBorderAdmission.State.UNGENERATED
        || authoritativeNbt.get(KEY) != null)
      throw new IllegalStateException("unknown or stamped V8 neighbor cannot be admitted");
    return new V8ChunkGenerationCommitGate.VersionedNeighbor(
        direction, certifiedState, null, durableEpoch);
  }

  /** Build an admission snapshot only from an independently certified target. */
  public static V8ChunkGenerationCommitGate.Snapshot ungeneratedTarget(
      V8GenerationBorderAdmission.State certifiedState, CompoundTag targetNbt,
      long durableEpoch, List<V8ChunkGenerationCommitGate.VersionedNeighbor> neighbors) {
    Objects.requireNonNull(targetNbt, "target NBT");
    if (certifiedState != V8GenerationBorderAdmission.State.UNGENERATED
        || targetNbt.get(KEY) != null)
      throw new IllegalStateException("target is not certified ungenerated");
    return new V8ChunkGenerationCommitGate.Snapshot(
        certifiedState, null, durableEpoch, neighbors);
  }

  private static long crc(String value) {
    CRC32 crc = new CRC32();
    byte[] ascii = value.getBytes(StandardCharsets.US_ASCII);
    crc.update(ascii, 0, ascii.length);
    return crc.getValue();
  }
}
