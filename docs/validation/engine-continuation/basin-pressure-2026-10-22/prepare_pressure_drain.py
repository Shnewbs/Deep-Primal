"""Share the existing pressure cap and use head-limited quantities on valid drain paths."""
from pathlib import Path
import difflib,hashlib
ROOT=Path(__file__).resolve().parent
before=(ROOT/'fifo-WaterRuntime.java').read_text()
local='''      var exchange = WaterTransport.pressure(origin.getY(), from, outlet.getY(), to);
      if (exchange.moved() > PRESSURE_TRANSFER)
        exchange =
            new WaterTransport.Exchange(
                from - PRESSURE_TRANSFER, to + PRESSURE_TRANSFER, PRESSURE_TRANSFER);'''
assert before.count(local)==1
s=before.replace(local,'      var exchange = pressureExchange(origin.getY(), from, outlet.getY(), to);')
s=s.replace('          exchange = WaterTransport.pressure(high.getY(), from, low.getY(), to);',
    '          exchange = pressureExchange(high.getY(), from, low.getY(), to);',1)
cap='''    if (exchange.moved() > PRESSURE_TRANSFER)
      exchange = new WaterTransport.Exchange(from - PRESSURE_TRANSFER,
          to + PRESSURE_TRANSFER, PRESSURE_TRANSFER);
'''
assert s.count(cap)==1
s=s.replace(cap,'',1)
marker='  /** Immediate L-shaped communication avoids queueing the last unit behind unrelated pools. */'
s=s.replace(marker,'''  /** Keep the same head/capacity calculation and transfer ceiling on every pressure path. */
  private static WaterTransport.Exchange pressureExchange(int fromY, int from, int toY, int to) {
    var exchange = WaterTransport.pressure(fromY, from, toY, to);
    if (exchange.moved() <= PRESSURE_TRANSFER) return exchange;
    return new WaterTransport.Exchange(from - PRESSURE_TRANSFER, to + PRESSURE_TRANSFER, PRESSURE_TRANSFER);
  }

'''+marker,1)
old='        return transfer(l, origin, target, new WaterTransport.Exchange(from - 1, to + 1, 1), flow);'
assert s.count(old)==1
s=s.replace(old,'''        var exchange = pressureExchange(origin.getY(), from, target.getY(), to);
        // A validated route to a lower exit still advances a one-unit film when
        // the discrete pressure split rounds to zero. Larger heads use the same
        // conservative transfer cap as local and retained pressure exchanges.
        if (exchange.moved() == 0) exchange = new WaterTransport.Exchange(from - 1, to + 1, 1);
        return transfer(l, origin, target, exchange, flow);''',1)
(ROOT/'pressure-drain-WaterRuntime.java').write_text(s)
(ROOT/'pressure-drain.patch').write_text(''.join(difflib.unified_diff(before.splitlines(True),s.splitlines(True),fromfile='a/WaterRuntime.java',tofile='b/WaterRuntime.java')))
if __name__=='__main__':
    (ROOT/'project/src/main/java/com/shnewbs/deepprimal/engine/WaterRuntime.java').write_text(s)
    print('candidate_sha256',hashlib.sha256(s.encode()).hexdigest())
