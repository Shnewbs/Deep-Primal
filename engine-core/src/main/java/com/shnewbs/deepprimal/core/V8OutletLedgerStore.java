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
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

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
  private static final int LEGACY_DISK_VERSION = 1;
  private static final int BOUND_DISK_VERSION = 2;
  private static final int WORLD_HASH_BYTES = 32;

  /**
   * A native owner-supplied identity for one immutable V8 saved profile in one
   * world. The digest must be of the canonical, already saved generation
   * profile, not of mutable current creation-screen options. Do not infer a
   * world UUID or hash from this helper; callers must provide the persisted
   * authority and reject unknown legacy identity before adoption.
   */
  public record WorldBinding(UUID worldId, String savedProfileSha256) {
    public WorldBinding {
      Objects.requireNonNull(worldId, "worldId");
      Objects.requireNonNull(savedProfileSha256, "savedProfileSha256");
      if (savedProfileSha256.length() != WORLD_HASH_BYTES * 2)
        throw new IllegalArgumentException("V8 saved-profile SHA256 must be 64 lowercase hex characters");
      for (int i = 0; i < savedProfileSha256.length(); i++) {
        char c = savedProfileSha256.charAt(i);
        if (!((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f')))
          throw new IllegalArgumentException("V8 saved-profile SHA256 must be lowercase hex");
      }
    }
  }
  private static final int HASH_BYTES = 32;
  private static final int MAX_BYTES = 3_000_000;

  public record Commit(long generation, V8ConservedOutletLedger.Result result) {
    public Commit { Objects.requireNonNull(result, "result"); }
  }

  private record Image(long generation, V8ConservedOutletLedger.Snapshot snapshot,
                       WorldBinding binding, byte[] fingerprint) {}

  /**
   * A separately persisted, native-world ownership witness. It MUST be
   * reconstructed from the authoritative saved finite-cell/coarse-store state,
   * never from this outlet checkpoint or its pending image. The stamp and all
   * balances must be durable at the same world transaction boundary. Java
   * cannot authenticate a caller's claim of Minecraft durability: the native
   * integration and crash/save tests remain required.
   */
  public record NativeCheckpoint(WorldBinding binding, long generation,
      Map<Long, V8ConservedOutletLedger.Balance> accounts) {
    public NativeCheckpoint {
      Objects.requireNonNull(binding, "native world binding");
      if (generation <= 0) throw new IllegalArgumentException("invalid native checkpoint generation");
      accounts = Map.copyOf(Objects.requireNonNull(accounts, "native balances"));
      if (accounts.size() > V8ConservedOutletLedger.MAX_ACCOUNTS)
        throw new IllegalArgumentException("unbounded native outlet accounts");
    }
  }

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

  /**
   * Legacy V1 entry point, retained for pre-bound source/API compatibility.
   * NEVER use this for a newly adopted Minecraft world; call createBound.
   */
  public static V8OutletLedgerStore create(Path file,
      V8ConservedOutletLedger.Snapshot first) throws IOException {
    return createImage(file, first, null);
  }

  /** Fresh-world V2 checkpoint, bound to the saved world and frozen V8 profile. */
  public static V8OutletLedgerStore createBound(Path file,
      V8ConservedOutletLedger.Snapshot first, WorldBinding binding) throws IOException {
    return createImage(file, first, Objects.requireNonNull(binding, "binding"));
  }

  private static V8OutletLedgerStore createImage(Path file,
      V8ConservedOutletLedger.Snapshot first, WorldBinding binding) throws IOException {
    Objects.requireNonNull(first, "first");
    Path path = checkedPath(file);
    LockHandle handle = acquire(path);
    try {
      Path pending = path.resolveSibling(path.getFileName().toString() + ".pending");
      if (Files.exists(path) || Files.exists(pending))
        throw new IOException("V8 outlet store already exists or has an uncommitted creation");
      byte[] bytes = encode(1L, first, binding);
      publish(path, pending, bytes);
      return new V8OutletLedgerStore(path, handle.channel(), handle.lock(),
          new Image(1L, first, binding, digest(bytes)));
    } catch (IOException | RuntimeException | Error failure) {
      handle.close();
      throw failure;
    }
  }

  /** Legacy V1 only; never accepts V2, which requires explicit identity. */
  public static V8OutletLedgerStore open(Path file) throws IOException {
    return openImage(file, null);
  }

  /**
   * Existing-world V2 only. A transplanted checkpoint, edited saved policy,
   * unknown V1 image or missing file fails without constructing a new store.
   */
  public static V8OutletLedgerStore openBound(Path file, WorldBinding binding)
      throws IOException {
    return openImage(file, Objects.requireNonNull(binding, "binding"));
  }

  /**
   * Explicit, bounded crash reconciliation for a COMPLETE, sealed pending
   * image. Normal openBound still refuses ALL pending frames. The caller must
   * supply an independently durable native account/receipt-generation witness;
   * a reconstructed ledger snapshot is NOT such evidence.
   *
   * <p>If native evidence confirms the pending generation, atomically promote
   * that exact image. If it confirms the original generation, move the pending
   * bytes to a single non-overwritable forensic quarantine. Any identity,
   * quantity, sequence, missing file, damaged frame or durability ambiguity
   * fails closed without touching either ledger image. No automatic adoption
   * of legacy V1 snapshots or cross-world migration is possible.
   *
   * <p>This API does NOT itself make native Minecraft writes atomic with this
   * file: the caller must prove it saved its native witness before invoking it.
   */
  public static V8OutletLedgerStore openBoundReconciled(Path file,
      WorldBinding binding, NativeCheckpoint nativeWitness) throws IOException {
    Objects.requireNonNull(binding, "world binding");
    Objects.requireNonNull(nativeWitness, "durable native witness");
    if (!binding.equals(nativeWitness.binding()))
      throw new IOException("native checkpoint is from a different world/profile");
    Path path = checkedPath(file);
    LockHandle handle = acquire(path);
    try {
      Image committed = read(path);
      if (!binding.equals(committed.binding()))
        throw new IOException("committed outlet checkpoint world/profile mismatch");
      Path pendingPath = path.resolveSibling(path.getFileName().toString() + ".pending");
      Image chosen = committed;
      if (Files.exists(pendingPath, LinkOption.NOFOLLOW_LINKS)) {
        if (!checkedPath(pendingPath).equals(pendingPath))
          throw new IOException("pending outlet checkpoint path identity changed");
        Image pendingImage = read(pendingPath);
        if (!binding.equals(pendingImage.binding())
            || pendingImage.generation() != Math.addExact(committed.generation(), 1L))
          throw new IOException("pending outlet checkpoint generation/world mismatch");
        checkReconstructableTransition(committed.snapshot(), pendingImage.snapshot());
        if (nativeWitness.generation() == pendingImage.generation()
            && nativeWitness.accounts().equals(pendingImage.snapshot().accounts())) {
          chosen = pendingImage;
        } else if (nativeWitness.generation() == committed.generation()
            && nativeWitness.accounts().equals(committed.snapshot().accounts())) {
          chosen = committed;
        } else {
          throw new IOException("native checkpoint does not certify either outlet generation");
        }
        // Re-check physical identities under the same participating file lock.
        // Out-of-band writers are not permitted to replace observed bytes.
        if (!Arrays.equals(read(path).fingerprint(), committed.fingerprint())
            || !Arrays.equals(read(pendingPath).fingerprint(), pendingImage.fingerprint()))
          throw new IOException("out-of-band change during outlet reconciliation");
        if (chosen == pendingImage) {
          Files.move(pendingPath, path, StandardCopyOption.ATOMIC_MOVE,
              StandardCopyOption.REPLACE_EXISTING);
        } else {
          Path quarantine = path.resolveSibling(path.getFileName().toString() + ".reconciled-aborted");
          if (Files.exists(quarantine, LinkOption.NOFOLLOW_LINKS))
            throw new IOException("outlet forensic quarantine already exists");
          Files.move(pendingPath, quarantine, StandardCopyOption.ATOMIC_MOVE);
        }
        forceDirectory(path.getParent());
        Image disk = read(path);
        if (disk.generation() != chosen.generation()
            || !Arrays.equals(disk.fingerprint(), chosen.fingerprint()))
          throw new IOException("reconciled outlet checkpoint differs from chosen image");
        chosen = disk;
      } else if (nativeWitness.generation() != committed.generation()
          || !nativeWitness.accounts().equals(committed.snapshot().accounts())) {
        throw new IOException("native checkpoint differs from committed outlet authority");
      }
      return new V8OutletLedgerStore(path, handle.channel(), handle.lock(), chosen);
    } catch (IOException | RuntimeException | Error failure) {
      handle.close();
      throw failure;
    }
  }

  /**
   * A complete pending frame can have at most one newly retained sequence per
   * source for deterministic reconstruction. A batch with multiple new events
   * from one source needs an external explicit recovery journal and fails
   * closed here; never infer missing intermediate receipts or minted volume.
   */
  private static void checkReconstructableTransition(
      V8ConservedOutletLedger.Snapshot old,
      V8ConservedOutletLedger.Snapshot next) throws IOException {
    if (old.version() != next.version()
        || !old.receivers().equals(next.receivers())
        || !old.accounts().keySet().equals(next.accounts().keySet())
        || !next.lastReceipts().keySet().containsAll(old.lastReceipts().keySet()))
      throw new IOException("pending outlet authority changed immutable account topology");
    Map<Long, long[]> deltas = new HashMap<>();
    int advanced = 0;
    try {
      for (var entry : next.lastReceipts().entrySet()) {
        long source = entry.getKey();
        V8ConservedOutletLedger.Transfer transfer = entry.getValue();
        V8ConservedOutletLedger.Transfer previous = old.lastReceipts().get(source);
        if (transfer.equals(previous)) continue;
        long expected = previous == null ? 1L : Math.addExact(previous.sequence(), 1L);
        if (transfer.sequence() != expected
            || !Objects.equals(old.receivers().get(source), transfer.receiver()))
          throw new IOException("pending outlet receipt is missing an intermediate event");
        addDelta(deltas, source, transfer, false);
        addDelta(deltas, transfer.receiver(), transfer, true);
        advanced++;
      }
      if (advanced == 0)
        throw new IOException("pending outlet generation advanced without a new receipt");
      for (var entry : old.accounts().entrySet()) {
        long key = entry.getKey();
        var before = entry.getValue();
        long[] delta = deltas.getOrDefault(key, new long[3]);
        var expected = new V8ConservedOutletLedger.Balance(
            Math.addExact(before.water(), delta[0]),
            Math.addExact(before.salt(), delta[1]),
            Math.addExact(before.sediment(), delta[2]), before.capacity());
        if (!expected.equals(next.accounts().get(key)))
          throw new IOException("pending outlet balances do not conserve accepted receipts");
      }
    } catch (ArithmeticException | IllegalArgumentException failure) {
      throw new IOException("invalid or overflowing outlet recovery account delta", failure);
    }
  }

  private static void addDelta(Map<Long, long[]> deltas, long key,
      V8ConservedOutletLedger.Transfer transfer, boolean credit) {
    long[] value = deltas.computeIfAbsent(key, unused -> new long[3]);
    value[0] = credit ? Math.addExact(value[0], transfer.water())
        : Math.subtractExact(value[0], transfer.water());
    value[1] = credit ? Math.addExact(value[1], transfer.salt())
        : Math.subtractExact(value[1], transfer.salt());
    value[2] = credit ? Math.addExact(value[2], transfer.sediment())
        : Math.subtractExact(value[2], transfer.sediment());
  }

  private static V8OutletLedgerStore openImage(Path file, WorldBinding expected)
      throws IOException {
    Path path = checkedPath(file);
    LockHandle handle = acquire(path);
    try {
      rejectPending(path);
      Image image = read(path);
      if (!Objects.equals(image.binding(), expected))
        throw new IOException("V8 outlet checkpoint world/profile identity mismatch");
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
      // An unacknowledged pre-rename frame is NOT disposable scratch. It may
      // represent a crash boundary requiring native-cell reconciliation.
      // Never overwrite it on an ordinary retry, including an exact replay.
      if (!checkedPath(file).equals(file))
        throw new IOException("V8 outlet authority path identity changed");
      rejectPending(file);
      // Detect an out-of-band file replacement even if it ignored our lock,
      // including when an identical replay requests acknowledgment.
      Image onDisk = read(file);
      if (onDisk.generation() != current.generation()
          || !Objects.equals(onDisk.binding(), current.binding())
          || !Arrays.equals(onDisk.fingerprint(), current.fingerprint()))
        throw new IOException("out-of-band V8 outlet store modification");
      if (candidate.applied() == 0)
        return new Commit(current.generation(), candidate);
      long next = Math.addExact(current.generation(), 1L);
      byte[] bytes = encode(next, candidate.snapshot(), current.binding());
      publish(file, pending, bytes);
      current = new Image(next, candidate.snapshot(), current.binding(), digest(bytes));
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
    Path requested = value.toAbsolutePath().normalize();
    if (requested.getFileName() == null || !Files.isDirectory(requested.getParent()))
      throw new IOException("existing V8 outlet store parent directory required");
    // Canonicalize directory aliases BEFORE choosing the lock name. Two callers
    // entering via different symbolic parent directories must contend for the
    // same world-owned lock instead of independently debiting one checkpoint.
    Path path = requested.getParent().toRealPath().resolve(requested.getFileName());
    String basename = path.getFileName().toString();
    if (Files.isSymbolicLink(path)
        || Files.isSymbolicLink(path.resolveSibling(basename + ".lock"))
        || Files.isSymbolicLink(path.resolveSibling(basename + ".pending")))
      throw new IOException("symbolic V8 outlet authority path is not allowed");
    if (Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
      try {
        // On supporting Unix providers a hard link is also an alias with a
        // different .lock name. Fail closed instead of forking the authority.
        Object count = Files.getAttribute(path, "unix:nlink", LinkOption.NOFOLLOW_LINKS);
        if (count instanceof Number number && number.longValue() != 1L)
          throw new IOException("hard-linked V8 outlet authority is not allowed");
      } catch (UnsupportedOperationException | IllegalArgumentException unsupported) {
        // Other providers need native filesystem acceptance; the standard Java
        // attribute API offers no portable hard-link count on Windows.
      }
    }
    return path;
  }

  private static void rejectPending(Path file) throws IOException {
    Path unacknowledged = file.resolveSibling(file.getFileName() + ".pending");
    if (Files.exists(unacknowledged, LinkOption.NOFOLLOW_LINKS))
      throw new IOException("unreconciled V8 outlet pending checkpoint; manual recovery required");
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
    forceDirectory(file.getParent());
  }

  private static void forceDirectory(Path parent) throws IOException {
    try (FileChannel directory = FileChannel.open(parent, StandardOpenOption.READ)) {
      directory.force(true);
    }
  }

  private static byte[] digest(byte[] bytes) {
    try { return MessageDigest.getInstance("SHA-256").digest(bytes); }
    catch (NoSuchAlgorithmException failure) { throw new AssertionError(failure); }
  }

  private static byte[] encode(long generation, V8ConservedOutletLedger.Snapshot snapshot,
                               WorldBinding binding) throws IOException {
    if (generation <= 0) throw new IOException("V8 outlet generation must be positive");
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    try (DataOutputStream out = new DataOutputStream(bytes)) {
      out.writeInt(MAGIC);
      out.writeInt(binding == null ? LEGACY_DISK_VERSION : BOUND_DISK_VERSION);
      out.writeLong(generation);
      if (binding != null) {
        out.writeLong(binding.worldId().getMostSignificantBits());
        out.writeLong(binding.worldId().getLeastSignificantBits());
        out.write(HexFormat.of().parseHex(binding.savedProfileSha256()));
      }
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
      if (in.readInt() != MAGIC)
        throw new IOException("unknown V8 outlet checkpoint magic");
      int diskVersion = in.readInt();
      if (diskVersion != LEGACY_DISK_VERSION && diskVersion != BOUND_DISK_VERSION)
        throw new IOException("unknown V8 outlet checkpoint envelope");
      long generation = in.readLong();
      if (generation <= 0) throw new IOException("invalid V8 outlet generation");
      WorldBinding binding = null;
      if (diskVersion == BOUND_DISK_VERSION) {
        UUID worldId = new UUID(in.readLong(), in.readLong());
        byte[] profile = new byte[WORLD_HASH_BYTES];
        in.readFully(profile);
        binding = new WorldBinding(worldId, HexFormat.of().formatHex(profile));
      }
      int policy = in.readInt();
      Map<Long, V8ConservedOutletLedger.Balance> accounts = new HashMap<>();
      int count = boundedCount(in.readInt());
      long previous = 0L;
      for (int i = 0; i < count; i++) {
        long key = in.readLong();
        if (i > 0 && key <= previous)
          throw new IOException("noncanonical V8 outlet account order");
        previous = key;
        var b = new V8ConservedOutletLedger.Balance(
            in.readLong(), in.readLong(), in.readLong(), in.readLong());
        if (accounts.putIfAbsent(key, b) != null)
          throw new IOException("duplicate V8 outlet account key");
      }
      Map<Long, Long> receivers = new HashMap<>();
      count = boundedCount(in.readInt());
      previous = 0L;
      for (int i = 0; i < count; i++) {
        long key = in.readLong(), to = in.readLong();
        if (i > 0 && key <= previous)
          throw new IOException("noncanonical V8 outlet receiver order");
        previous = key;
        if (receivers.putIfAbsent(key, to) != null)
          throw new IOException("duplicate V8 outlet receiver");
      }
      Map<Long, V8ConservedOutletLedger.Transfer> receipts = new HashMap<>();
      count = boundedCount(in.readInt());
      previous = 0L;
      for (int i = 0; i < count; i++) {
        long key = in.readLong();
        if (i > 0 && key <= previous)
          throw new IOException("noncanonical V8 outlet receipt order");
        previous = key;
        var t = new V8ConservedOutletLedger.Transfer(in.readLong(), in.readLong(),
            in.readLong(), in.readLong(), in.readLong(), in.readLong());
        if (receipts.putIfAbsent(key, t) != null)
          throw new IOException("duplicate V8 outlet receipt");
      }
      if (in.available() != 0) throw new IOException("trailing V8 outlet checkpoint bytes");
      var snapshot = new V8ConservedOutletLedger.Snapshot(policy, accounts,
          receivers, receipts);
      return new Image(generation, snapshot, binding, digest(frame));
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
