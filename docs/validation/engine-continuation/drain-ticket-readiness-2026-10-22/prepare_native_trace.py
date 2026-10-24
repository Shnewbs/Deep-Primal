"""Apply an evidence-only experiment to the isolated project after its producer exits."""
from pathlib import Path
import argparse
import hashlib

ROOT=Path(__file__).resolve().parent
parser=argparse.ArgumentParser()
parser.add_argument('variant',choices=['before','deferred','ticket'])
parser.add_argument('--audit',type=Path,default=ROOT.parent/'drain-reload-audit-2026-10-22')
args=parser.parse_args()
AUDIT=args.audit.resolve()
source=((ROOT/'ticket-WaterRuntime.java') if args.variant=='ticket' else (AUDIT/('staged-WaterRuntime.java' if args.variant=='before' else 'candidate-WaterRuntime.java'))).read_text()
source=source.replace('  private static final Direction[] ALL = Direction.values();',
 '  private static final boolean DRAIN_TRACE = System.getenv("DEEPPRIMAL_DRAIN_TRACE") != null;\n  private static final Direction[] ALL = Direction.values();',1)
source=source.replace('    budget.waitingDrains.put(saved, missing);',
 '    budget.waitingDrains.put(saved, missing);\n    if (DRAIN_TRACE) System.out.println("DRAIN_WAIT origin=" + saved + " missing=" + missing);',1)
source=source.replace('    var origins = budget.drainWakeChunks.get(key);\n    if (origins == null) return;',
 '    var origins = budget.drainWakeChunks.get(key);\n    if (DRAIN_TRACE) System.out.println("DRAIN_RESUME key=" + key + " origins=" + origins);\n    if (origins == null) return;',1)
source=source.replace('    public void chunkLoaded(net.neoforged.neoforge.event.level.ChunkEvent.Load event) {',
 '''    public void chunkLoaded(net.neoforged.neoforge.event.level.ChunkEvent.Load event) {
      if (DRAIN_TRACE && event.getLevel() instanceof ServerLevel observed)
        System.out.println("DRAIN_LOAD thread=" + Thread.currentThread().getName()
            + " ownerThread=" + observed.getServer().isSameThread() + " chunk=" + event.getChunk().getPos());''',1)
source=source.replace('        var pos = event.getChunk().getPos();',
 '''        var pos = event.getChunk().getPos();
        if (DRAIN_TRACE) System.out.println("DRAIN_LOAD_INDEX key=" + pos.pack()
            + " waiting=" + budget.waitingDrains.size() + " origins=" + budget.drainWakeChunks.get(pos.pack()));''',1)
source=source.replace('  public static final class Events {',
 '''  public static final class Events {
    @net.neoforged.bus.api.SubscribeEvent
    public void traceTicket(net.neoforged.neoforge.event.level.ChunkTicketLevelUpdatedEvent event) {
      long key = event.getChunkPos();
      int x = (int) key, z = (int) (key >>> 32);
      if (DRAIN_TRACE && x >= 255 && x <= 273 && z == 0)
        System.out.println("DRAIN_TICKET chunk=" + x + "," + z + " old=" + event.getOldTicketLevel()
            + " new=" + event.getNewTicketLevel() + " thread=" + Thread.currentThread().getName());
    }''',1)
source=source.replace('    public void chunkUnloaded(net.neoforged.neoforge.event.level.ChunkEvent.Unload event) {',
 '''    public void chunkUnloaded(net.neoforged.neoforge.event.level.ChunkEvent.Unload event) {
      if (DRAIN_TRACE) System.out.println("DRAIN_UNLOAD chunk=" + event.getChunk().getPos());''',1)
target=ROOT/'project/src/main/java/com/shnewbs/deepprimal/engine/WaterRuntime.java'
target.write_text(source)
fixture=(AUDIT/'ClosedWaterChecks.java').read_text()
fixture=fixture.replace('private boolean gapUnloaded, gapReloaded;', 'private boolean gapUnloaded, gapReloaded;\n  private final java.util.HashSet<Integer> physicallyUnloaded = new java.util.HashSet<>();')
fixture=fixture.replace('  @SubscribeEvent\n  public void pre(', '''  @SubscribeEvent
  public void unloaded(net.neoforged.neoforge.event.level.ChunkEvent.Unload event) {
    if (event.getLevel() == level && event.getChunk().getPos().z() == 0)
      physicallyUnloaded.add(event.getChunk().getPos().x());
  }
  @SubscribeEvent
  public void pre(''',1)
fixture=fixture.replace('mode.equals("reload")', '(mode.equals("reload") || mode.equals("physical-reload") || mode.equals("reload-edit"))')
fixture=fixture.replace('target = mode.equals("basin-inspect")', 'target = mode.equals("physical-reload") ? 3200 : mode.equals("basin-inspect")')
fixture=fixture.replace('tube(4096, 257, mode.equals("reopen"))', 'tube(4096, mode.equals("physical-reload") ? 1025 : 257, mode.equals("reopen"))')
fixture=fixture.replace('        if (mode.equals("edit")) put(4104, 200, 8, false);', '''        if (mode.equals("physical-reload")) {
          for (int turn = 0; turn < 5; turn++) WaterRuntime.tick(level, scenes.getFirst().getFirst());
          check(diagnostic("retainedNodes") > 512, "Retain a witness through the future physical gap");
        }
        if (mode.equals("edit")) put(4104, 200, 8, false);''',1)
fixture=fixture.replace('x <= 269', 'x <= (mode.equals("physical-reload") ? 317 : 269)')
fixture=fixture.replace('source.getChunk(262, 0, ChunkStatus.FULL, false) == null', 'source.getChunk(mode.equals("physical-reload") ? 290 : 262, 0, ChunkStatus.FULL, false) == null && (!mode.equals("physical-reload") || physicallyUnloaded.contains(290))')
fixture=fixture.replace('level.getChunkSource().getChunk(262, 0, ChunkStatus.FULL, false) == null', 'level.getChunkSource().getChunk(mode.equals("physical-reload") ? 290 : 262, 0, ChunkStatus.FULL, false) == null')
fixture=fixture.replace('"First drain turn remains bounded"', '"First drain turn remains bounded"')
fixture=fixture.replace('    check(diagnostic("waitingJobs") <= 128', '    check(diagnostic("readyProbes") <= 16 && diagnostic("readyPending") <= 1024, "Bounded readiness probes and keys");\n    check(diagnostic("waitingJobs") <= 128') if args.variant=='ticket' else fixture
fixture=fixture.replace('"CLOSED_DRAIN_GAP_RELOADED noOriginInjection=true"', '"CLOSED_DRAIN_GAP_RELOADED noOriginInjection=true physicalUnload=" + physicallyUnloaded.contains(290)')
fixture=fixture.replace('durations.add(System.nanoTime() - before); ticks++;', 'durations.add(System.nanoTime() - before); ticks++;\n    if (mode.equals("reload-edit") && ticks == 60) put(4104, 200, 8, false);')
fixture=fixture.replace('mode.equals("reproduce") || mode.equals("edit") || mode.equals("unload")', 'mode.equals("reproduce") || mode.equals("edit") || mode.equals("unload") || mode.equals("reload-edit")')
fixture=fixture.replace('if (mode.equals("edit")) check(', 'if (mode.equals("edit") || mode.equals("reload-edit")) check(')
fixture=fixture.replace('    if (ticks % 100 == 0) System.out.println(',
 '    if (ticks % 20 == 0) System.out.println("DRAIN_NATIVE_STATE tick=" + ticks + " jobs=" + diagnostic("jobs") + " waiting=" + diagnostic("waitingJobs") + " links=" + diagnostic("waitingLinks") + " source=" + amount(scenes.getFirst().getFirst()));\n    if (ticks % 100 == 0) System.out.println(',1)
(ROOT/'project/src/visualValidation/java/com/shnewbs/deepprimal/validation/ClosedWaterChecks.java').write_text(fixture)
print('variant',args.variant,'traced_source_sha256',hashlib.sha256(source.encode()).hexdigest())
