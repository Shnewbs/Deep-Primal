// Deep Primal Vulkan cache implementation. LGPL-3.0-only.
package net.vulkanmod.cache;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.*;
import java.security.*;
import java.util.*;
import java.util.zip.CRC32C;

/** Bounded, checksummed local cache. Corrupt or missing data is a cache miss. */
public final class BinaryCache {
  private static final int MAGIC = 0x44505643, HEADER = 16;

  private record Entry(Path file, long size) {}

  private final Path directory;
  private final int maximumEntryBytes, maximumEntries;
  private final long maximumBytes;
  private final LinkedHashMap<String, Entry> entries = new LinkedHashMap<>(16, .75f, true);
  private long bytes;
  private boolean initialized;

  public BinaryCache(Path directory, int entryBytes, long totalBytes, int count) {
    if (entryBytes < 1 || totalBytes < HEADER + (long) entryBytes || count < 1)
      throw new IllegalArgumentException("Invalid cache bounds");
    this.directory = directory;
    maximumEntryBytes = entryBytes;
    maximumBytes = totalBytes;
    maximumEntries = count;
  }

  public static String key(byte[] data) {
    try {
      return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(data));
    } catch (NoSuchAlgorithmException impossible) {
      throw new AssertionError(impossible);
    }
  }

  private static boolean validName(String name) {
    return name.matches("[0-9a-f]{64}\\.bin");
  }

  private static long modified(Path path) {
    try {
      return Files.getLastModifiedTime(path).toMillis();
    } catch (IOException ignored) {
      return 0;
    }
  }

  private void initialize() throws IOException {
    if (initialized) return;
    Files.createDirectories(directory);
    try (var files = Files.list(directory)) {
      for (Path file :
          files
              .filter(
                  p ->
                      validName(p.getFileName().toString())
                          && Files.isRegularFile(p, LinkOption.NOFOLLOW_LINKS))
              .sorted(Comparator.comparingLong(BinaryCache::modified))
              .toList()) {
        long size = Files.size(file);
        entries.put(file.getFileName().toString(), new Entry(file, size));
        bytes += size;
      }
    }
    initialized = true;
    trim(0, 0);
  }

  private String name(String key) {
    String name = key + ".bin";
    if (!validName(name)) throw new IllegalArgumentException("Invalid cache key");
    return name;
  }

  public synchronized byte[] read(String key) throws IOException {
    initialize();
    String name = name(key);
    Entry entry = entries.get(name);
    if (entry == null) return null;
    byte[] raw;
    try (var input = Files.newInputStream(entry.file)) {
      raw = input.readNBytes(maximumEntryBytes + HEADER + 1);
    } catch (NoSuchFileException missing) {
      entries.remove(name);
      bytes -= entry.size;
      return null;
    }
    if (raw.length >= HEADER && raw.length <= maximumEntryBytes + HEADER) {
      var header = ByteBuffer.wrap(raw);
      if (header.getInt() == MAGIC
          && header.getInt() == 1
          && header.getInt() == raw.length - HEADER) {
        int checksum = header.getInt();
        var crc = new CRC32C();
        crc.update(raw, HEADER, raw.length - HEADER);
        if (checksum == (int) crc.getValue()) return Arrays.copyOfRange(raw, HEADER, raw.length);
      }
    }
    entries.remove(name);
    bytes -= entry.size;
    Files.deleteIfExists(entry.file);
    return null;
  }

  public synchronized void write(String key, byte[] data) throws IOException {
    if (data.length == 0 || data.length > maximumEntryBytes) return;
    initialize();
    String name = name(key);
    Entry old = entries.remove(name);
    if (old != null) bytes -= old.size;
    long size = HEADER + (long) data.length;
    trim(size, 1);
    var crc = new CRC32C();
    crc.update(data);
    Path temporary = Files.createTempFile(directory, "pending-", ".tmp");
    try {
      try (var output = new java.io.DataOutputStream(Files.newOutputStream(temporary))) {
        output.writeInt(MAGIC);
        output.writeInt(1);
        output.writeInt(data.length);
        output.writeInt((int) crc.getValue());
        output.write(data);
      }
      Path target = directory.resolve(name);
      try {
        Files.move(
            temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
      } catch (AtomicMoveNotSupportedException ignored) {
        Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
      }
      entries.put(name, new Entry(target, size));
      bytes += size;
    } finally {
      Files.deleteIfExists(temporary);
    }
  }

  private void trim(long extraBytes, int extraEntries) throws IOException {
    while (bytes + extraBytes > maximumBytes || entries.size() + extraEntries > maximumEntries) {
      var oldest = entries.pollFirstEntry();
      if (oldest == null) break;
      bytes -= oldest.getValue().size;
      Files.deleteIfExists(oldest.getValue().file);
    }
  }
}
