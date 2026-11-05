"""Coalesce only a proven uniform, fully supported and closed drain component."""
from pathlib import Path
import difflib
import hashlib

ROOT = Path(__file__).resolve().parent
before = (ROOT/'ticket-WaterRuntime.java').read_text()
s = before.replace('int drainRestarts, drainCompleted, drainSaturated, maxDrainSlice;',
    'int drainRestarts, drainCompleted, drainSaturated, maxDrainSlice, drainCoalesced;')
s = s.replace('    boolean invalidated, saturated;\n\n    DrainJob(BlockPos origin) {',
    '    boolean invalidated, saturated, quiet;\n    final int quietUnits;\n\n    DrainJob(BlockPos origin, int amount, boolean supported) {', 1)
s = s.replace('      seen.add(origin.asLong());\n    }\n  }',
    '      seen.add(origin.asLong());\n      quietUnits = amount;\n      quiet = supported;\n    }\n  }', 1)
s = s.replace('    if (volumeOnly && before.getValue(EngineFluid.UNITS).equals(after.getValue(EngineFluid.UNITS))) return;',
'''    if (volumeOnly && before.getValue(EngineFluid.UNITS).equals(after.getValue(EngineFluid.UNITS))) return;
    // A settled-component proof needs unchanged amounts, even when connectivity
    // survives a partial-to-partial write. Conservatively revoke it for the chunk.
    var drainWatchers = budget.drainChunks.get(
        net.minecraft.world.level.ChunkPos.pack(p.getX() >> 4, p.getZ() >> 4));
    if (drainWatchers != null) for (var job : drainWatchers) job.quiet = false;''')
s = s.replace('int readyProbes, int readyPending) {}', 'int readyProbes, int readyPending, int coalesced) {}')
s = s.replace('return new DrainDiagnostics(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);',
    'return new DrainDiagnostics(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);')
s = s.replace('budget.drainReadyProbes, budget.loadedDrainChunks.size());',
    'budget.drainReadyProbes, budget.loadedDrainChunks.size(), budget.drainCoalesced);')
s = s.replace('    job = new DrainJob(origin);',
    '    job = new DrainJob(origin, units(l, origin), !open(l, origin.below()));', 1)
s = s.replace('      q.set(p.getX(), p.getY() - 1, p.getZ());\n      if (drainOpen(l, q, job, budget) && units(l, q) < WaterTransport.CAPACITY) {',
'''      if (units(l, p) != job.quietUnits) job.quiet = false;
      q.set(p.getX(), p.getY() - 1, p.getZ());
      boolean belowOpen = drainOpen(l, q, job, budget);
      if (belowOpen) job.quiet = false;
      if (belowOpen && units(l, q) < WaterTransport.CAPACITY) {''',1)
s = s.replace('    if (!job.invalidated && job.head == job.tail) {\n      finish(budget, job);',
'''    if (!job.invalidated && job.head == job.tail) {
      if (job.quiet && !job.saturated && job.missingChunks.isEmpty())
        coalesceQuietDrains(budget, job);
      finish(budget, job);''',1)
marker = '  private static boolean move(ServerLevel l, BlockPos a, BlockPos b, boolean down) {'
s = s.replace(marker, '''  /** A complete unchanged witness proves every cell has no local transport left. */
  private static void coalesceQuietDrains(Budget budget, DrainJob complete) {
    // At most 8192 keys and 16 resident jobs. No world reads or retained cache:
    // normal neighbor/edit activation can immediately wake this component again.
    for (long key : complete.seen)
      if (budget.active.remove(key)) budget.drainCoalesced++;
    var overlaps = new ArrayList<DrainJob>();
    for (var job : budget.drains.values())
      if (job != complete && complete.seen.contains(job.origin.asLong())) overlaps.add(job);
    for (var job : overlaps) {
      finish(budget, job);
      budget.drainCoalesced++;
    }
  }

''' + marker, 1)
assert s != before
(ROOT/'quiet-WaterRuntime.java').write_text(s)
(ROOT/'quiet-component.patch').write_text(''.join(difflib.unified_diff(before.splitlines(True),s.splitlines(True),fromfile='a/WaterRuntime.java',tofile='b/WaterRuntime.java')))
(ROOT/'project/src/main/java/com/shnewbs/deepprimal/engine/WaterRuntime.java').write_text(s)
print('candidate_sha256',hashlib.sha256(s.encode()).hexdigest())
