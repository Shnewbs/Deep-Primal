"""Install validation-only zero-work assertions, only after the safety producer exits."""
from pathlib import Path
ROOT=Path(__file__).resolve().parent
VALIDATION=ROOT/'project/src/visualValidation/java/com/shnewbs/deepprimal/validation'
p=VALIDATION/'ClosedWaterChecks.java'; s=p.read_text()
needle='    peakPending = Math.max(peakPending, WaterRuntime.queued(level));'
replacement='''    if (firstSettledTick > 0 && ticks > firstSettledTick)
      check(physics.processed() == 0 && physics.pressureVisits() == 0
              && physics.drainVisits() == 0 && physics.transfers() == 0,
          "The settled basin performs zero transport work on subsequent host ticks");
'''+needle
assert s.count(needle)==1
if 'zero transport work' not in s: s=s.replace(needle,replacement,1)
p.write_text(s);(ROOT/p.name).write_text(s)
p=VALIDATION/'DrainIdleChecks.java';s=p.read_text()
needle='      idleSamples++;'
replacement='''      var physics=WaterRuntime.diagnostics(level);
      check(physics.processed()==0 && physics.pressureVisits()==0
          && physics.drainVisits()==0 && physics.transfers()==0,
          "An idle loaded pool performs zero transport work");
'''+needle
assert s.count(needle)==1
if 'zero transport work' not in s: s=s.replace(needle,replacement,1)
needle='    String result="DRAIN_IDLE_NATIVE_PASS'
replacement='''    check(WaterRuntime.queued(level)==0 && d.watchedChunks()==0 && d.waitingJobs()==0,
        "The edited pool releases all work and watches again");
'''+needle
assert s.count(needle)==1
if 'The edited pool releases all work' not in s: s=s.replace(needle,replacement,1)
s=s.replace('" editedSinkUnits=16 conservedEveryTick=true', '" editedSinkUnits=16 zeroIdleTickWork=true conservedEveryTick=true',1)
p.write_text(s);(ROOT/p.name).write_text(s)
print('Stronger zero-idle-work and post-edit cleanup gates installed',flush=True)
