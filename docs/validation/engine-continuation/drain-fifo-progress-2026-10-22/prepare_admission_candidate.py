"""Keep a bounded first-turn lane independent of retained drain residency."""
from pathlib import Path
import difflib
import hashlib
ROOT=Path(__file__).resolve().parent
before=(ROOT/'ticket-WaterRuntime.java').read_text()
s=before.replace('int count, pressureVisits, drainVisits, transfers;',
    'int count, pressureVisits, drainVisits, drainAdmissionVisits, transfers;')
s=s.replace('budget.count = budget.pressureVisits = budget.drainVisits = budget.transfers = 0;',
    'budget.count = budget.pressureVisits = budget.drainVisits = budget.drainAdmissionVisits = budget.transfers = 0;')
s=s.replace('    if (job != null) return continueDrain(l, job, budget);',
'''    // During a level step, resident frontiers run once per FIFO scheduling turn.
    // Explicit single-cell adapters can still advance a retained search directly.
    if (job != null) return !budget.stepped && continueDrain(l, job, budget);''',1)
s=s.replace('    if (budget.drainVisits >= budget.drainLimit || budget.drains.size() >= MAX_DRAIN_JOBS) {',
'''    // Reserve half of each phase's visits for fair retained-frontier service.
    // A full residency table must not prevent a nearby outlet's bounded first turn.
    int admissionLimit = budget.drainLimit / 2;
    int allowance = Math.min(DRAIN_SEARCH, admissionLimit - budget.drainAdmissionVisits);
    if (budget.drainVisits >= budget.drainLimit || allowance <= 0) {''',1)
s=s.replace('    budget.drains.put(job.origin, job);\n    watch(budget, job, origin);',
'''    boolean resident = budget.drains.size() < MAX_DRAIN_JOBS;
    if (resident) budget.drains.put(job.origin, job);
    watch(budget, job, origin);''',1)
s=s.replace('    return continueDrain(l, job, budget);\n  }\n\n  private static void runDrains',
'''    int beforeVisits = budget.drainVisits, limit = budget.drainLimit;
    boolean moved;
    try {
      budget.drainLimit = Math.min(limit, beforeVisits + allowance);
      moved = continueDrain(l, job, budget);
    } finally {
      budget.drainLimit = limit;
      budget.drainAdmissionVisits += budget.drainVisits - beforeVisits;
      if (!resident) finish(budget, job);
    }
    // Only the 16 retained jobs keep a frontier. A transient first turn has at
    // most 128 visits (one additional temporary witness); unresolved work rejoins
    // the active FIFO. Completed missing-chunk waits retain their normal index.
    if (!resident && !moved && (job.head < job.tail || job.invalidated)) activate(l, origin);
    return moved;
  }

  private static void runDrains''',1)
assert s!=before
(ROOT/'admission-WaterRuntime.java').write_text(s)
(ROOT/'drain-admission.patch').write_text(''.join(difflib.unified_diff(before.splitlines(True),s.splitlines(True),fromfile='a/WaterRuntime.java',tofile='b/WaterRuntime.java')))
(ROOT/'project/src/main/java/com/shnewbs/deepprimal/engine/WaterRuntime.java').write_text(s)
print('candidate_sha256',hashlib.sha256(s.encode()).hexdigest())
