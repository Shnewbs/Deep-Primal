"""Put the shared pressure limit in core math, avoiding a replaced intermediate exchange."""
from pathlib import Path
import difflib,hashlib
ROOT=Path(__file__).resolve().parent
core=ROOT/'project/engine-core/src/main/java/com/shnewbs/deepprimal/core/WaterTransport.java'
baseline=ROOT/'baseline-WaterTransport.java'
if not baseline.exists():baseline.write_text(core.read_text())
old=baseline.read_text()
start=old.index('  public static Exchange pressure(');end=old.index('  /** Shared global corners',start)
new=old[:start]+'''  public static Exchange pressure(int fromY, int from, int toY, int to) {
    return pressure(fromY, from, toY, to, CAPACITY);
  }

  /** Apply a caller's transfer quantum before constructing the conserved result. */
  public static Exchange pressure(int fromY, int from, int toY, int to, int maximum) {
    if (from < 0 || from > CAPACITY || to < 0 || to > CAPACITY)
      throw new IllegalArgumentException("Water capacity");
    if (maximum < 0) throw new IllegalArgumentException("Negative pressure transfer limit");
    long difference = ((long) fromY - toY) * CAPACITY + from - to;
    int moved =
        (int) Math.max(0, Math.min(maximum,
            Math.min(Math.min(from, CAPACITY - to), difference / 2)));
    return new Exchange(from - moved, to + moved, moved);
  }

'''+old[end:]
core.write_text(new)
(ROOT/'limited-WaterTransport.java').write_text(new)
(ROOT/'limited-pressure-core.patch').write_text(''.join(difflib.unified_diff(old.splitlines(True),new.splitlines(True),fromfile='a/WaterTransport.java',tofile='b/WaterTransport.java')))
s=(ROOT/'pressure-drain-WaterRuntime.java').read_text()
start=s.index('  /** Keep the same head/capacity calculation');end=s.index('  /** Immediate L-shaped',start)
s=s[:start]+s[end:]
for args in ['origin.getY(), from, outlet.getY(), to','high.getY(), from, low.getY(), to','origin.getY(), from, target.getY(), to']:
    call='pressureExchange('+args+')';assert s.count(call)==1
    s=s.replace(call,'WaterTransport.pressure('+args+', PRESSURE_TRANSFER)',1)
(ROOT/'limited-drain-WaterRuntime.java').write_text(s)
(ROOT/'project/src/main/java/com/shnewbs/deepprimal/engine/WaterRuntime.java').write_text(s)
base=(ROOT/'fifo-WaterRuntime.java').read_text()
(ROOT/'limited-drain.patch').write_text(''.join(difflib.unified_diff(base.splitlines(True),s.splitlines(True),fromfile='a/WaterRuntime.java',tofile='b/WaterRuntime.java')))
checks=ROOT/'project/src/coreChecks/java/com/shnewbs/deepprimal/core/IntegratedEngineChecks.java'
c=(ROOT/'baseline-IntegratedEngineChecks.java').read_text().replace('      int pressureCases = 0;','      int pressureCases = 0, limitedCases = 0;',1)
c=c.replace('            pressureCases++;', '''            for (int maximum : new int[] {0, 1, 2, 4, 16, Integer.MAX_VALUE}) {
              var limited = WaterTransport.pressure(dy, from, 0, to, maximum);
              int moved = Math.min(maximum, e.moved());
              require(limited.moved() == moved && limited.from() == from - moved
                      && limited.to() == to + moved,
                  "A transfer quantum preserves head, source volume and destination capacity");
              limitedCases++;
            }
            pressureCases++;''',1)
c=c.replace('      System.out.println(\n          "ENGINE_PRESSURE_CORE_PASS', '''      boolean pressureLimitRejected = false;
      try { WaterTransport.pressure(0, 1, 0, 0, -1); }
      catch (IllegalArgumentException expected) { pressureLimitRejected = true; }
      require(pressureLimitRejected, "Negative pressure transfer limit is rejected");
      System.out.println(
          "ENGINE_PRESSURE_CORE_PASS''',1)
c=c.replace('              + " conserved=true monotonicHead=true");', '              + " limitedCases=" + limitedCases + " conserved=true monotonicHead=true negativeLimit=true");',1)
checks.write_text(c);(ROOT/'IntegratedEngineChecks.java').write_text(c)
print('runtime_sha256',hashlib.sha256(s.encode()).hexdigest(),'core_sha256',hashlib.sha256(new.encode()).hexdigest())
