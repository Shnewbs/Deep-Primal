"""Keep independent FIFO drain requests; reserve bounded first-turn service."""
from pathlib import Path
import difflib
import hashlib
ROOT=Path(__file__).resolve().parent
before=(ROOT/'ticket-WaterRuntime.java').read_text()
# Start from the separately recorded bounded-probe attempt; no quiet proof changes.
s=(ROOT/'admission-WaterRuntime.java').read_text()
s=s.replace('    final LongLinkedOpenHashSet pressure = new LongLinkedOpenHashSet();',
    '    final LongLinkedOpenHashSet pressure = new LongLinkedOpenHashSet();\n    final LongLinkedOpenHashSet drainPending = new LongLinkedOpenHashSet();',1)
s=s.replace('b.drains.size() + b.waitingDrains.size();', 'b.drains.size() + b.waitingDrains.size() + b.drainPending.size();',1)
s=s.replace('          && budget.drains.isEmpty()) break;', '          && budget.drains.isEmpty() && budget.drainPending.isEmpty()) break;',1)
s=s.replace('  private static boolean drain(ServerLevel l, BlockPos origin, Budget budget) {',
'''  private static void deferDrain(ServerLevel level, BlockPos origin, Budget budget) {
    long key = origin.asLong();
    if (budget.drainPending.contains(key)) return;
    if (budget.drainPending.size() < MAX_ACTIVE) budget.drainPending.add(key);
    else activate(level, origin);
  }

  private static boolean drain(ServerLevel l, BlockPos origin, Budget budget) {
    return drain(l, origin, budget, false);
  }

  private static boolean drain(ServerLevel l, BlockPos origin, Budget budget, boolean admitted) {''',1)
s=s.replace('    // Reserve half of each phase\'s visits for fair retained-frontier service.',
'''    // A dedicated queue keeps its head when the visit quota ends. Cycling the
    // whole active queue would give the same prefix every phase's first turns.
    if (budget.stepped && !admitted) {
      deferDrain(l, origin, budget);
      return false;
    }
    // Reserve half of each phase's visits for fair retained-frontier service.''',1)
s=s.replace('if (budget.drainVisits >= budget.drainLimit || allowance <= 0) {\n      activate(l, origin);',
    'if (budget.drainVisits >= budget.drainLimit || allowance <= 0) {\n      deferDrain(l, origin, budget);',1)
s=s.replace('if (!resident && !moved && (job.head < job.tail || job.invalidated)) activate(l, origin);',
    'if (!resident && !moved && (job.head < job.tail || job.invalidated)) deferDrain(l, origin, budget);',1)
start=s.index('  private static void runDrains(')
end=s.index('  private static boolean continueDrain(',start)
s=s[:start]+'''  private static void runDrains(ServerLevel level, Budget budget) {
    int limit = budget.drainLimit;
    // Pending short requests and retained long frontiers each receive a bounded
    // lane. An idle lane leaves its remaining visits available to resident work.
    if (!budget.drainPending.isEmpty())
      budget.drainLimit = Math.min(limit, budget.drainVisits + DRAIN_VISITS_PER_TICK / 2);
    try {
      int turns = budget.drains.size();
      while (turns-- > 0 && !budget.drains.isEmpty() && budget.drainVisits < budget.drainLimit) {
        var entry = budget.drains.pollFirstEntry();
        budget.drains.put(entry.getKey(), entry.getValue());
        continueDrain(level, entry.getValue(), budget);
      }
    } finally { budget.drainLimit = limit; }
    int turns = Math.min(CELLS_PER_SUBSTEP, budget.drainPending.size());
    while (turns-- > 0 && !budget.drainPending.isEmpty()
        && budget.drainVisits < limit && budget.drainAdmissionVisits < limit / 2) {
      BlockPos origin = BlockPos.of(budget.drainPending.removeFirstLong());
      if (level.isInWorldBounds(origin) && level.hasChunkAt(origin) && units(level, origin) > 0)
        drain(level, origin, budget, true);
    }
  }

'''+s[end:]
s=s.replace('int readyProbes, int readyPending) {}', 'int readyProbes, int readyPending, int pending, int admissionVisits) {}')
s=s.replace('return new DrainDiagnostics(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);',
    'return new DrainDiagnostics(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);')
s=s.replace('budget.drainReadyProbes, budget.loadedDrainChunks.size());',
    'budget.drainReadyProbes, budget.loadedDrainChunks.size(), budget.drainPending.size(), budget.drainAdmissionVisits);')
s=s.replace("the active FIFO. Completed missing-chunk waits retain their normal index.", "the drain-request FIFO. Completed missing-chunk waits retain their normal index.")
assert s!=before
(ROOT/'fifo-WaterRuntime.java').write_text(s)
(ROOT/'drain-fifo-admission.patch').write_text(''.join(difflib.unified_diff(before.splitlines(True),s.splitlines(True),fromfile='a/WaterRuntime.java',tofile='b/WaterRuntime.java')))
(ROOT/'project/src/main/java/com/shnewbs/deepprimal/engine/WaterRuntime.java').write_text(s)
print('candidate_sha256',hashlib.sha256(s.encode()).hexdigest())
