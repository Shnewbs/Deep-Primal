package com.shnewbs.deepprimal.water;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/** Native transactional water port. Each cell stores at most 1000 mB in 125 mB units. */
public final class WaterPort extends SnapshotJournal<BlockState>
    implements ResourceHandler<FluidResource> {
  // All callers touching one cell share a journal, including different machines in one transaction.
  // Weak keys and values release both worlds and unused ports without an unloading scan.
  private static final java.util.WeakHashMap<
          ServerLevel, java.util.WeakHashMap<BlockPos, java.lang.ref.WeakReference<WaterPort>>>
      PORTS = new java.util.WeakHashMap<>();
  private final ServerLevel level;
  private final BlockPos pos;

  private WaterPort(ServerLevel level, BlockPos pos) {
    this.level = level;
    this.pos = pos.immutable();
  }

  public static WaterPort at(ServerLevel level, BlockPos pos) {
    var ports = PORTS.computeIfAbsent(level, unused -> new java.util.WeakHashMap<>());
    var reference = ports.get(pos);
    var port = reference == null ? null : reference.get();
    if (port == null) {
      port = new WaterPort(level, pos);
      ports.put(port.pos, new java.lang.ref.WeakReference<>(port));
    }
    return port;
  }

  public static void register(RegisterCapabilitiesEvent event) {
    event.registerBlock(
        Capabilities.Fluid.BLOCK,
        (level, pos, state, entity, side) ->
            level instanceof ServerLevel server && FiniteWater.enabled() ? at(server, pos) : null,
        Blocks.WATER,
        Blocks.AIR,
        Blocks.CAVE_AIR,
        RiverWater.BLOCK.get(),
        RiverWater.CHANNEL.get());
  }

  private BlockState current() {
    return level.hasChunkAt(pos) ? level.getBlockState(pos) : Blocks.BARRIER.defaultBlockState();
  }

  private static void index(int index) {
    if (index != 0) throw new IndexOutOfBoundsException(index);
  }

  @Override
  public int size() {
    return 1;
  }

  @Override
  public FluidResource getResource(int index) {
    index(index);
    return FiniteWater.units(current()) == 0 ? FluidResource.EMPTY : FluidResource.of(Fluids.WATER);
  }

  @Override
  public long getAmountAsLong(int index) {
    index(index);
    return FiniteWater.units(current()) * 125L;
  }

  @Override
  public long getCapacityAsLong(int index, FluidResource resource) {
    index(index);
    return resource.isEmpty() || isValid(index, resource) ? 1000 : 0;
  }

  @Override
  public boolean isValid(int index, FluidResource resource) {
    index(index);
    return resource.getFluid() == Fluids.WATER && resource.isComponentsPatchEmpty();
  }

  @Override
  public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
    return change(index, resource, amount, true, transaction);
  }

  @Override
  public int extract(
      int index, FluidResource resource, int amount, TransactionContext transaction) {
    return change(index, resource, amount, false, transaction);
  }

  private int change(
      int index,
      FluidResource resource,
      int amount,
      boolean insert,
      TransactionContext transaction) {
    index(index);
    TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
    if (!FiniteWater.enabled() || !isValid(index, resource)) return 0;
    var previous = current();
    int old = FiniteWater.units(previous);
    if (!previous.isAir() && old == 0) return 0;
    int units = Math.min(amount / 125, insert ? 8 - old : old);
    if (units == 0) return 0;
    updateSnapshots(transaction);
    return level.setBlock(pos, FiniteWater.state(previous, old + (insert ? units : -units)), 0)
        ? units * 125
        : 0;
  }

  @Override
  protected BlockState createSnapshot() {
    return current();
  }

  @Override
  protected void revertToSnapshot(BlockState previous) {
    if (level.hasChunkAt(pos)) level.setBlock(pos, previous, 0);
  }

  @Override
  protected void onRootCommit(BlockState original) {
    if (!level.hasChunkAt(pos)) return;
    var result = current();
    level.setBlock(pos, original, 0);
    level.setBlock(pos, result, 2 | 16);
    FiniteWater.wake(level, pos);
  }
}
