"""Independently authored bounded readiness patch on the preserved experiment."""
from pathlib import Path
import argparse
import difflib

ROOT=Path(__file__).resolve().parent
parser=argparse.ArgumentParser();parser.add_argument('--audit',type=Path,default=ROOT.parent/'drain-reload-audit-2026-10-22');args=parser.parse_args()
AUDIT=args.audit.resolve()
source=(AUDIT/'candidate-WaterRuntime.java').read_text()
source=source.replace('final LongOpenHashSet loadedDrainChunks = new LongOpenHashSet();',
 'final LongLinkedOpenHashSet loadedDrainChunks = new LongLinkedOpenHashSet();')
source=source.replace('    boolean drainLoadOverflow;', '    boolean drainLoadOverflow;\n    int drainReadyProbes;')
start=source.index('  private static void resumeLoadedDrainChunks(')
end=source.index('  private static void resumeDrainWaiters(',start)
source=source[:start]+'''  private static void resumeLoadedDrainChunks(ServerLevel level, Budget budget) {
    if (budget.drainLoadOverflow) {
      // Turnover may fill the event queue with obsolete keys. Rebuild only from
      // the bounded live wait index, without dropping the newest requested load.
      budget.loadedDrainChunks.clear();
      for (long key : budget.drainWakeChunks.keySet()) budget.loadedDrainChunks.add(key);
      budget.drainLoadOverflow = false;
    }
    int probes = Math.min(16, budget.loadedDrainChunks.size());
    while (probes-- > 0 && !budget.loadedDrainChunks.isEmpty()) {
      long key = budget.loadedDrainChunks.removeFirstLong();
      if (!budget.drainWakeChunks.containsKey(key)) continue;
      budget.drainReadyProbes++;
      // A ticket transition can precede FULL readiness. Never request generation
      // here, and retain the key fairly until its loaded chunk is available.
      if (level.getChunkSource().getChunk((int) key, (int) (key >>> 32),
          net.minecraft.world.level.chunk.status.ChunkStatus.FULL, false) != null)
        resumeDrainWaiters(level, budget, key);
      else budget.loadedDrainChunks.add(key);
    }
  }

'''+source[end:]
source=source.replace('        if (origins.isEmpty()) budget.drainWakeChunks.remove(key);',
 '''        if (origins.isEmpty()) {
          budget.drainWakeChunks.remove(key);
          budget.loadedDrainChunks.remove(key);
        }''',1)
source=source.replace('      budget.count = budget.pressureVisits = budget.drainVisits = budget.transfers = 0;',
 '      budget.count = budget.pressureVisits = budget.drainVisits = budget.transfers = 0;\n      budget.drainReadyProbes = 0;',1)
source=source.replace('int completed, int saturated, int maxSliceVisits, int waitingJobs, int waitingChunks, int waitingLinks) {}',
 'int completed, int saturated, int maxSliceVisits, int waitingJobs, int waitingChunks, int waitingLinks, int readyProbes, int readyPending) {}',1)
source=source.replace('new DrainDiagnostics(0, 0, 0, 0, 0, 0, 0, 0, 0, 0)',
 'new DrainDiagnostics(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)',1)
source=source.replace('budget.waitingDrains.size(), budget.drainWakeChunks.size(), budget.drainWaitLinks);',
 'budget.waitingDrains.size(), budget.drainWakeChunks.size(), budget.drainWaitLinks,\n        budget.drainReadyProbes, budget.loadedDrainChunks.size());',1)
source=source.replace('  public static final class Events {',
 '''  public static final class Events {
    @net.neoforged.bus.api.SubscribeEvent
    public void chunkTicketChanged(net.neoforged.neoforge.event.level.ChunkTicketLevelUpdatedEvent event) {
      var level = event.getLevel();
      if (!level.getServer().isSameThread()) return;
      Budget budget = BUDGETS.get(level);
      if (budget == null) return;
      long key = event.getChunkPos();
      if (net.minecraft.server.level.ChunkLevel.isLoaded(event.getNewTicketLevel()))
        rememberLoadedDrainChunk(budget, key);
      else budget.loadedDrainChunks.remove(key);
      // Availability can change while the LevelChunk stays resident, so Load/
      // Unload alone is not a complete invalidation or waiter-wakeup contract.
      invalidateChunk(budget, key);
    }
''',1)
assert 'readyProbes' in source and 'chunkTicketChanged' in source
(ROOT/'ticket-WaterRuntime.java').write_text(source)
base=(AUDIT/'staged-WaterRuntime.java').read_text()
path='src/main/java/com/shnewbs/deepprimal/engine/WaterRuntime.java'
(ROOT/'ticket-readiness-candidate.patch').write_text(''.join(difflib.unified_diff(base.splitlines(True),source.splitlines(True),fromfile='a/'+path,tofile='b/'+path)))
print('Separate ticket/readiness candidate generated.')
