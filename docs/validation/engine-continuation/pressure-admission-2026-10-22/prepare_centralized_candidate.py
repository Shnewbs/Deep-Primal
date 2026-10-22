"""Centralize normal admission validation; preserve the defensive ownership guard."""
from pathlib import Path
import difflib,hashlib,json
ROOT=Path(__file__).resolve().parent
records=[]
for label,name in [('accepted','accepted-baseline-WaterRuntime.java'),('staged','baseline-WaterRuntime.java')]:
    old=(ROOT/name).read_text()
    redundant='''      if (level.isInWorldBounds(origin) && level.hasChunkAt(origin)
          && units(level, origin) > 0) startPressure(level, origin, budget);'''
    assert old.count(redundant)==1
    new=old.replace(redundant,'      startPressure(level, origin, budget);',1)
    start='  private static PressureJob startPressure(ServerLevel level, BlockPos origin, Budget budget) {\n'
    new=new.replace(start,start+'''    // Keep a resident frontier if an overlapping request reaches admission.
    // Its normal completion or invalidation owns watcher cleanup.
    var existing = budget.searches.get(origin);
    if (existing != null) return existing;
''',1)
    checks='''    if (!level.isInWorldBounds(origin) || !level.hasChunkAt(origin)
        || units(level, origin) == 0 || !surface(level, origin, units(level, origin))) return null;'''
    assert new.count(checks)==1
    new=new.replace(checks,'''    if (!level.isInWorldBounds(origin) || !level.hasChunkAt(origin)) return null;
    int amount = units(level, origin);
    if (amount == 0 || !surface(level, origin, amount)) return null;''',1)
    path=ROOT/(label+'-cleanup-WaterRuntime.java');path.write_text(new)
    (ROOT/(label+'-cleanup.patch')).write_text(''.join(difflib.unified_diff(old.splitlines(True),new.splitlines(True),
      fromfile='a/src/main/java/com/shnewbs/deepprimal/engine/WaterRuntime.java',tofile='b/src/main/java/com/shnewbs/deepprimal/engine/WaterRuntime.java')))
    records.append({'variant':label,'baseline_sha256':hashlib.sha256(old.encode()).hexdigest(),
      'candidate_sha256':hashlib.sha256(new.encode()).hexdigest(),
      'scope':'Only runPressure delegates validation and startPressure validates one amount snapshot and retains an existing owner. Transport, topology, budgets and all other bytes unchanged.',
      'reachability_limit':'Native overlap test explicitly arranges queued+resident ownership through private reflection; occurrence under ordinary current callers is unproven. Fresh-admission redundant checks are present on the normal path.'})
(ROOT/'centralized-source-delta.json').write_text(json.dumps(records,indent=2)+'\n');print(json.dumps(records))
