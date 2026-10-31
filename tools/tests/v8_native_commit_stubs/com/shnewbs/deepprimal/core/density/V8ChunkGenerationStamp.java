package com.shnewbs.deepprimal.core.density;
public final class V8ChunkGenerationStamp {
  public record Stamp(int revision, int chunkX, int chunkZ, long profileFingerprint) {}
}
