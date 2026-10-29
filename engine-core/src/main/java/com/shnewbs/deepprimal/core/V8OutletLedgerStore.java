package com.shnewbs.deepprimal.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Single-writer, file-backed authority for an opt-in V8 watershed ledger.
 * Each accepted generation is one canonical, SHA-256-sealed snapshot containing
 * balances, receiver edges AND replay receipts. Caller-visible commits advance
 * only after a forced temporary file, an atomic replacement and a directory
 * metadata force succeed. On any I/O failure the instance is poisoned: reopen
 * and inspect the durable generation rather than guessing whether it committed.
 *
 * <p>This store owns no Minecraft chunk or finite-cell bookkeeping. The native
 * owner must integrate a world transaction and must not acknowledge or mutate
 * native cells independently of this authority. An unknown/missing old world
 * file must NEVER be silently recreated: {@link #create} is distinct from
 * {@link #open}. Receiver edges remain caller-provided, not ocean certificates.
 * No background worker, network access or renderer mutation is performed.
 *
 * <p>Atomic renames and directory forces are required; an unsupported filesystem
 * fails closed. Callers must ensure all participants use the same world-owned
 * lock path. Stronger crash guarantees on Windows/network filesystems require
 * native testing and filesystem-specific evidence.
 */
public final class V8OutletLedgerStore implements AutoCloseable {
  private static final int MAGIC = 0x56384c53; // V8LS
  private static final int DISK_VERSION = 1;
  private static final int HASH_BYTES = 32;
  private static final int MAX_BYTES = 3_000_000;

  public record Commit(long generation, V8ConservedOutletLedger.Result result) {
    public Commit { Objects.requireNonNull(result, "result"); }
  }

  private record Image(long generation, V8ConservedOutletLedger.Snapshot snapshot,
                       byte[] fingerprint) {}

  private final Path file;
  private final Path pending;
  private final FileChannel mutexChannel;
  private final FileLock mutex;
  private Image current;
  private boolean closed;
  private boolean poisoned;

  private V8OutletLedgerStore(Path file, FileChannel channel, FileLock lock,
                              Image image) {
    this.file = file;
    this.pending = file.resolveSibling(file.getFileName().toString() + ".pending");
    this.mutexChannel = channel;
    this.mutex = lock;
    this.current = image;
  }

  /** Fresh-world only; rejects an existing checkpoint or an abandoned first write. */
  public static V8OutletLedgerStore create(Path file,
      V8ConservedOutletLedger.Snapshot first) throws IOException {
    Objects.requireNonNull(first, "first");
    Path path = checkedPath(file);
    LockHandle handle = acquire(path);
    try {
      Path pending = path.resolveSibling(path.getFileName().toString() + ".pending");
      if (Files.exists(path) || Files.exists(pending))
        throw new IOException("V8 outlet store already exists or has an uncommitted creation");
      byte[] bytes = encode(1L, first);
      publish(path, pending, bytes);
      return new V8OutletLedgerStore(path, handle.channel(), handle.lock(),
          new Image(1L, first, digest(bytes)));
    } catch (IOException | RuntimeException | Error failure) {
      handle.close();
      throw failure;
    }
  }

  /** Existing-world only; absence, corruption and unknown format fail closed. */
  public static V8OutletLedgerStore open(Path file) throws IOException {
    Path path = checkedPath(file);
    LockHandle handle = acquire(path);
    try {
      Image image = read(path);
      return new V8OutletLedgerStore(path, handle.channel(), handle.lock(), image);
    } catch (IOException | RuntimeException | Error failure) {
      handle.close();
      throw failure;
    }
  }

  public synchronized long generation() {
    ensureOpen();
    return current.generation();
  }

  public synchronized V8ConservedOutletLedger.Snapshot snapshot() {
    ensureOpen();
    return current.snapshot();
  }

  /**
   * CAS on the durable generation. A losing caller must reread the winner and
   * retry with the correct sequence; there is no unbounded implicit retry.
   * An identical replay returns the same generation without a disk write.
   */
  public synchronized Commit transact(long expectedGeneration,
      Collection<V8ConservedOutletLedger.Transfer> transfers) throws IOException {
    ensureOpen();
    if (expectedGeneration != current.generation())
      throw new IllegalStateException("stale V8 outlet ledger generation");
    Objects.requireNonNull(transfers, "transfers");
    V8ConservedOutletLedger.Result candidate = V8ConservedOutletLedger.applyBatch(
        current.snapshot(), transfers);
    try {
      // Detect an out-of-band file replacement even if it ignored our lock,
      // including when an identical replay requests acknowledgment.
      Image onDisk = read(file);
      if (onDisk.generation() != current.generation()
          || !Arrays.equals(onDisk.fingerprint(), current.fingerprint()))
        throw new IOException("out-of-band V8 outlet store modification");
      if (candidate.applied() == 0)
        return new Commit(current.generation(), candidate);
      long next = Math.addExact(current.generation(), 1L);
      byte[] bytes = encode(next, candidate.snapshot());
      publish(file, pending, bytes);
      current = new Image(next, candidate.snapshot(), digest(bytes));
      return new Commit(next, candidate);
    } catch (IOException | RuntimeException | Error failure) {
      poisoned = true; // May have renamed before fsync; reopen to determine outcome.
      throw failure;
    }
  }

  public synchronized void close() throws IOException {
    if (closed) return;
    closed = true;
    try {
      mutex.release();
    } finally {
      mutexChannel.close();
    }
  }

  private void ensureOpen() {
    if (closed || poisoned || !mutex.isValid())
      throw new IllegalStateException("V8 outlet store closed or uncertain; reopen required");
  }

  private static Path checkedPath(Path value) throws IOException {
    Objects.requireNonNull(value, "file");
    Path path = value.toAbsolutePath().normalize();
    if (path.getFileName() == null || !Files.isDirectory(path.getParent()))
      throw new IOException("existing V8 outlet store parent directory required");
    return path;
  }

  private record LockHandle(FileChannel channel, FileLock lock) {
    void close() throws IOException {
      try { lock.release(); } finally { channel.close(); }
    }
  }

  private static LockHandle acquire(Path file) throws IOException {
    Path lockFile = file.resolveSibling(file.getFileName().toString() + ".lock");
    FileChannel channel = FileChannel.open(lockFile,
        StandardOpenOption.CREATE, StandardOpenOption.WRITE);
    try {
      // tryLock rejects independent processes and overlapping local instances.
      FileLock lock = channel.tryLock();
      if (lock == null) throw new IOException("V8 outlet authority already owned");
      return new LockHandle(channel, lock);
    } catch (java.nio.channels.OverlappingFileLockException failure) {
      channel.close();
      throw new IOException("V8 outlet authority already owned", failure);
    } catch (IOException | RuntimeException | Error failure) {
      channel.close();
      throw failure;
    }
  }

  private static void publish(Path file, Path pending, byte[] bytes) throws IOException {
    // The parent is deliberately preexisting: never create world directories or
    // silently recover another world's missing store.
    try (FileChannel channel = FileChannel.open(pending,
        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING,
        StandardOpenOption.WRITE)) {
      ByteBuffer buffer = ByteBuffer.wrap(bytes);
      while (buffer.hasRemaining()) channel.write(buffer);
      channel.force(true);
    }
    Files.move(pending, file, StandardCopyOption.ATOMIC_MOVE,
        StandardCopyOption.REPLACE_EXISTING);
    try (FileChannel directory = FileChannel.open(file.getParent(), StandardOpenOption.READ)) {
      directory.force(true);
    }
  }

  private static byte[] digest(byte[] bytes) {
    try { return MessageDigest.getInstance("SHA-256").digest(bytes); }
    catch (NoSuchAlgorithmException failure) { throw new AssertionError(failure); }
  }

  private static byte[] encode(long generation, V8ConservedOutletLedger.Snapshot snapshot)
      throws IOException {
    if (generation <= 0) throw new IOException("V8 outlet generation must be positive");
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    try (DataOutputStream out = new DataOutputStream(bytes)) {
      out.writeInt(MAGIC);
      out.writeInt(DISK_VERSION);
      out.writeLong(generation);
      out.writeInt(snapshot.version());
      out.writeInt(snapshot.accounts().size());
      for (long key : sorted(snapshot.accounts())) {
        var b = snapshot.accounts().get(key);
        out.writeLong(key);
        out.writeLong(b.water()); out.writeLong(b.salt());
        out.writeLong(b.sediment()); out.writeLong(b.capacity());
      }
      out.writeInt(snapshot.receivers().size());
      for (long key : sorted(snapshot.receivers())) {
        out.writeLong(key); out.writeLong(snapshot.receivers().get(key));
      }
      out.writeInt(snapshot.lastReceipts().size());
      for (long key : sorted(snapshot.lastReceipts())) {
        var t = snapshot.lastReceipts().get(key);
        out.writeLong(key); out.writeLong(t.source()); out.writeLong(t.receiver());
        out.writeLong(t.sequence()); out.writeLong(t.water());
        out.writeLong(t.salt()); out.writeLong(t.sediment());
      }
    }
    byte[] payload = bytes.toByteArray();
    if (payload.length + HASH_BYTES > MAX_BYTES)
      throw new IOException("V8 outlet store exceeds bounded frame size");
    ByteArrayOutputStream framed = new ByteArrayOutputStream();
    framed.write(payload); framed.write(digest(payload));
    return framed.toByteArray();
  }

  private static List<Long> sorted(Map<Long, ?> map) {
    ArrayList<Long> keys = new ArrayList<>(map.keySet());
    keys.sort(Long::compare);
    return keys;
  }

  private static Image read(Path file) throws IOException {
    if (!Files.isRegularFile(file))
      throw new IOException("missing V8 outlet authority checkpoint");
    long size = Files.size(file);
    if (size < 6L * Integer.BYTES + Long.BYTES + HASH_BYTES || size > MAX_BYTES)
      throw new IOException("invalid V8 outlet checkpoint frame size");
    byte[] frame = Files.readAllBytes(file);
    if (frame.length != size || frame.length < HASH_BYTES)
      throw new IOException("short V8 outlet checkpoint read");
    byte[] payload = Arrays.copyOf(frame, frame.length - HASH_BYTES);
    byte[] hash = digest(payload);
    byte[] recordedHash = Arrays.copyOfRange(frame, frame.length - HASH_BYTES, frame.length);
    if (!MessageDigest.isEqual(hash, recordedHash))
      throw new IOException("V8 outlet checkpoint digest mismatch");
    try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(payload))) {
      if (in.readInt() != MAGIC || in.readInt() != DISK_VERSION)
        throw new IOException("unknown V8 outlet checkpoint envelope");
      long generation = in.readLong();
      if (generation <= 0) throw new IOException("invalid V8 outlet generation");
      int policy = in.readInt();
      Map<Long, V8ConservedOutletLedger.Balance> accounts = new HashMap<>();
      int count = boundedCount(in.readInt());
      for (int i = 0; i < count; i++) {
        long key = in.readLong();
        var b = new V8ConservedOutletLedger.Balance(
            in.readLong(), in.readLong(), in.readLong(), in.readLong());
        if (accounts.putIfAbsent(key, b) != null)
          throw new IOException("duplicate V8 outlet account key");
      }
      Map<Long, Long> receivers = new HashMap<>();
      count = boundedCount(in.readInt());
      for (int i = 0; i < count; i++) {
        long key = in.readLong(), to = in.readLong();
        if (receivers.putIfAbsent(key, to) != null)
          throw new IOException("duplicate V8 outlet receiver");
      }
      Map<Long, V8ConservedOutletLedger.Transfer> receipts = new HashMap<>();
      count = boundedCount(in.readInt());
      for (int i = 0; i < count; i++) {
        long key = in.readLong();
        var t = new V8ConservedOutletLedger.Transfer(in.readLong(), in.readLong(),
            in.readLong(), in.readLong(), in.readLong(), in.readLong());
        if (receipts.putIfAbsent(key, t) != null)
          throw new IOException("duplicate V8 outlet receipt");
      }
      if (in.available() != 0) throw new IOException("trailing V8 outlet checkpoint bytes");
      var snapshot = new V8ConservedOutletLedger.Snapshot(policy, accounts,
          receivers, receipts);
      return new Image(generation, snapshot, digest(frame));
    } catch (IllegalArgumentException | ArithmeticException failure) {
      throw new IOException("invalid V8 outlet checkpoint contents", failure);
    }
  }

  private static int boundedCount(int count) throws IOException {
    if (count < 0 || count > V8ConservedOutletLedger.MAX_ACCOUNTS)
      throw new IOException("V8 outlet checkpoint entry count outside bounds");
    return count;
  }
}
