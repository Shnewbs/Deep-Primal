"""Create a separate candidate; never modify the failed foreground source."""
import difflib
from pathlib import Path

root = Path(__file__).resolve().parent
before = (root/'staged-WaterRuntime.java').read_text()
after = before.replace(
    '    int drainWaitLinks;\n',
    '    int drainWaitLinks;\n'
    '    final LongOpenHashSet loadedDrainChunks = new LongOpenHashSet();\n'
    '    boolean drainLoadOverflow;\n', 1)
anchor = '  private static void resumeDrainWaiters(ServerLevel level, Budget budget, long key) {'
helpers = '''  // ChunkEvent.Load precedes FULL promotion. Record only bounded coordinates in
  // the callback; activation/world queries belong to the following level tick.
  private static void rememberLoadedDrainChunk(Budget budget, long key) {
    if (!budget.drainWakeChunks.containsKey(key)) return;
    if (budget.loadedDrainChunks.contains(key)) return;
    if (budget.loadedDrainChunks.size() < MAX_DRAIN_WAIT_LINKS)
      budget.loadedDrainChunks.add(key);
    else budget.drainLoadOverflow = true;
  }

  private static void resumeLoadedDrainChunks(ServerLevel level, Budget budget) {
    if (budget.drainLoadOverflow) {
      // Turnover can exceed the live wait-index size before the next tick.
      // Fall back to the bounded current origin set instead of dropping wakeups.
      var origins = List.copyOf(budget.waitingDrains.keySet());
      budget.loadedDrainChunks.clear();
      budget.drainLoadOverflow = false;
      for (var origin : origins) {
        forgetDrainWait(budget, origin);
        activate(level, origin);
      }
      return;
    }
    for (long key : budget.loadedDrainChunks) resumeDrainWaiters(level, budget, key);
    budget.loadedDrainChunks.clear();
  }

'''
assert before.count(anchor) == 1
after = after.replace(anchor, helpers + anchor, 1)
anchor = '    if (budget.stepped) return;\n    budget.stepped = true;'
assert after.count(anchor) == 1
after = after.replace(anchor, '    if (budget.stepped) return;\n    resumeLoadedDrainChunks(level, budget);\n    budget.stepped = true;', 1)
anchor = '        resumeDrainWaiters(level, budget, pos.pack());'
assert after.count(anchor) == 1
after = after.replace(anchor, '        rememberLoadedDrainChunk(budget, pos.pack());', 1)
(root/'candidate-WaterRuntime.java').write_text(after)
path = 'src/main/java/com/shnewbs/deepprimal/engine/WaterRuntime.java'
patch = ''.join(difflib.unified_diff(before.splitlines(True), after.splitlines(True),
                                  fromfile='a/'+path, tofile='b/'+path))
(root/'deferred-drain-load-candidate.patch').write_text(patch)
print('Separate lifecycle candidate written; production source unchanged.')
