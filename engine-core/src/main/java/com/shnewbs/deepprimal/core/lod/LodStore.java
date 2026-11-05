package com.shnewbs.deepprimal.core.lod;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

/** Versioned, CRC-checked, bounded disk snapshots. Give each world/dimension its own root. */
public final class LodStore {
  private static final int MAGIC = 0x504c4f44, VERSION = 1;
  private final Path root;

  public LodStore(Path root) {
    this.root = root;
  }

  private Path path(int x, int z) {
    return root.resolve(x + "_" + z + ".plod");
  }

  public synchronized boolean write(ColumnarChunk chunk) throws IOException {
    // Validate the reader's palette budget before replacing a complete snapshot.
    long names = 0;
    for (String s : chunk.palette()) {
      names += s.length();
      if (s.length() > 4096 || names > 1_048_576)
        throw new IOException("LOD palette byte budget");
    }
    Path target = path(chunk.x, chunk.z);
    Files.createDirectories(root);
    if (Files.exists(target)) {
      try {
        if (read(chunk.x, chunk.z).revision > chunk.revision) return false;
      } catch (IOException bad) {
        /* A new complete snapshot replaces a corrupt cache entry. */
      }
    }
    Path pending = Files.createTempFile(root, "lod-", ".pending");
    try {
      try (var out = new DataOutputStream(new GZIPOutputStream(Files.newOutputStream(pending)))) {
        out.writeInt(MAGIC);
        out.writeInt(VERSION);
        out.writeInt(chunk.x);
        out.writeInt(chunk.z);
        out.writeInt(chunk.minY);
        out.writeInt(chunk.height);
        out.writeLong(chunk.revision);
        out.writeInt(chunk.palette().size());
        for (String s : chunk.palette()) {
          if (s.length() > 4096) throw new IOException("Palette name too long");
          out.writeUTF(s);
        }
        for (int n : chunk.offsets()) out.writeInt(n);
        int[] runs = chunk.runs();
        out.writeInt(runs.length);
        for (int n : runs) out.writeInt(n);
      }
      try {
        Files.move(
            pending, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
      } catch (AtomicMoveNotSupportedException e) {
        Files.move(pending, target, StandardCopyOption.REPLACE_EXISTING);
      }
      return true;
    } finally {
      Files.deleteIfExists(pending);
    }
  }

  public ColumnarChunk read(int x, int z) throws IOException {
    Path file = path(x, z);
    if (Files.size(file) > 16 * 1024 * 1024) throw new IOException("LOD compressed budget");
    try (var in = new DataInputStream(new GZIPInputStream(Files.newInputStream(file)))) {
      if (in.readInt() != MAGIC
          || in.readInt() != VERSION
          || in.readInt() != x
          || in.readInt() != z) throw new IOException("LOD header/key");
      int min = in.readInt(), height = in.readInt();
      long revision = in.readLong();
      if (height < 1 || height > 4096) throw new IOException("LOD height");
      int count = in.readInt();
      if (count < 1 || count > 65536) throw new IOException("LOD palette budget");
      List<String> palette = new ArrayList<>();
      long names = 0;
      for (int i = 0; i < count; i++) {
        String s = in.readUTF();
        names += s.length();
        if (s.length() > 4096 || names > 1_048_576)
          throw new IOException("LOD palette byte budget");
        palette.add(s);
      }
      int[] offsets = new int[257];
      for (int i = 0; i < 257; i++) offsets[i] = in.readInt();
      int n = in.readInt();
      if (n < 0 || n > 256 * height * 3) throw new IOException("LOD run budget");
      int[] runs = new int[n];
      for (int i = 0; i < n; i++) runs[i] = in.readInt();
      if (in.read() != -1) throw new IOException("LOD trailing payload");
      try {
        return new ColumnarChunk(x, z, min, height, revision, palette, offsets, runs);
      } catch (IllegalArgumentException e) {
        throw new IOException("LOD corrupt geometry", e);
      }
    }
  }
}
