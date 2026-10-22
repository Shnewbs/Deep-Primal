#!/usr/bin/env python3
"""Execute the production pressure-search body in a sealed in-memory U-bend.

Requires JDK 25. This is a source-extracted algorithm reproduction, not a native
Minecraft test. Exit zero means the documented defect was reproduced unless
--expect-fixed is supplied. The result label deliberately does not say PASS.
"""
import argparse
import hashlib
import json
from pathlib import Path
import re
import subprocess
import tempfile


def body(source, start):
    opening = source.index("{", start)
    depth, end = 1, opening + 1
    while depth:
        depth += (source[end] == "{") - (source[end] == "}")
        end += 1
    return source[opening + 1:end - 1]


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--root", type=Path, default=Path(__file__).resolve().parents[2])
    parser.add_argument("--java-home", type=Path, required=True)
    parser.add_argument("--json", type=Path)
    parser.add_argument("--expect-fixed", action="store_true")
    args = parser.parse_args()
    source = (args.root / "src/main/java/com/shnewbs/deepprimal/engine/WaterRuntime.java").read_text()
    limit = int(re.search(r"PRESSURE_SEARCH = (\d+)", source).group(1))
    pressure = body(source, source.index("private static boolean equalize("))
    surface = body(source, source.index("private static boolean surface("))
    harness = r'''import java.util.*;
import com.shnewbs.deepprimal.core.WaterTransport;
public class PressureCutoffAudit {
  static final int PRESSURE_SEARCH = @LIMIT@, PRESSURE_TRANSFER = 4;
  enum Direction {
    DOWN(0,-1,0), UP(0,1,0), NORTH(0,0,-1), SOUTH(0,0,1), WEST(-1,0,0), EAST(1,0,0);
    final int dx,dy,dz;
    Direction(int x,int y,int z) { dx=x;dy=y;dz=z; }
  }
  static final Direction[] ALL = Direction.values();
  static class BlockPos {
    int x,y,z;
    BlockPos(int x,int y,int z) { this.x=x;this.y=y;this.z=z; }
    int getX(){return x;} int getY(){return y;} int getZ(){return z;}
    BlockPos above(){return new BlockPos(x,y+1,z);}
    BlockPos immutable(){return new BlockPos(x,y,z);}
    BlockPos relative(Direction d){return new BlockPos(x+d.dx,y+d.dy,z+d.dz);}
    long asLong(){return ((long)(x & 0x3ffffff)<<38)|((long)(z & 0x3ffffff)<<12)|(y & 0xfff);}
    public boolean equals(Object o){return o instanceof BlockPos p && x==p.x&&y==p.y&&z==p.z;}
    public int hashCode(){return Objects.hash(x,y,z);}
    static final class MutableBlockPos extends BlockPos {
      MutableBlockPos(){super(0,0,0);}
      void set(long key){x=(int)(key>>38);y=(int)(key<<52>>52);z=(int)(key<<26>>38);}
      void setWithOffset(BlockPos p,Direction d){x=p.x+d.dx;y=p.y+d.dy;z=p.z+d.dz;}
    }
  }
  static final class ServerLevel {
    final HashMap<Long,Integer> water=new HashMap<>();
    final HashSet<Long> air=new HashSet<>();
    int transfers;
    boolean isInWorldBounds(BlockPos p){return p.y>=0&&p.y<16;}
    boolean hasChunkAt(BlockPos p){return true;}
    int volume(){return water.values().stream().mapToInt(Integer::intValue).sum();}
  }
  static final class Budget {
    int pressureVisits, pressureLimit=4096;
    final HashSet<Long> seen=new HashSet<>();
    final long[] searchQueue=new long[PRESSURE_SEARCH];
    final LinkedHashSet<BlockPos> pressure=new LinkedHashSet<>();
  }
  static int units(ServerLevel l,BlockPos p){return l.water.getOrDefault(p.asLong(),0);}
  static boolean open(ServerLevel l,BlockPos p){return l.air.contains(p.asLong())||units(l,p)>0;}
  static boolean surface(ServerLevel l,BlockPos p,int amount){@SURFACE@}
  static boolean transfer(ServerLevel l,BlockPos from,BlockPos to,WaterTransport.Exchange e,int flow){
    if(e.moved()==0)return false;
    check(e.from()>=0&&e.from()<=16&&e.to()>=0&&e.to()<=16,"Capacity");
    check(units(l,from)+units(l,to)==e.from()+e.to(),"Transfer conservation");
    l.water.put(from.asLong(),e.from());l.water.put(to.asLong(),e.to());l.transfers++;
    return true;
  }
  static boolean equalize(ServerLevel l,BlockPos origin,Budget budget){@PRESSURE@}
  static ServerLevel bend(int width){
    var l=new ServerLevel();
    for(int x=0;x<=width;x++)l.water.put(new BlockPos(x,0,0).asLong(),16);
    for(int y=1;y<=3;y++)l.water.put(new BlockPos(0,y,0).asLong(),16);
    for(int y=1;y<=2;y++)l.water.put(new BlockPos(width,y,0).asLong(),16);
    l.air.add(new BlockPos(0,4,0).asLong());l.air.add(new BlockPos(width,3,0).asLong());
    return l;
  }
  static int reachable(ServerLevel l,BlockPos start){
    var seen=new HashSet<Long>();var queue=new ArrayDeque<BlockPos>();
    queue.add(start);seen.add(start.asLong());
    while(!queue.isEmpty()){
      var current=queue.removeFirst();
      for(var d:ALL){
        var p=current.relative(d);
        if(units(l,p)>0&&seen.add(p.asLong()))queue.add(p);
      }
    }
    return seen.size();
  }
  static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
  public static void main(String[] args){
    var origin=new BlockPos(0,3,0);
    var shortBend=bend(32);int before=shortBend.volume();
    var shortBudget=new Budget();
    check(equalize(shortBend,origin,shortBudget),"Short-bend positive control did not transfer");
    check(shortBend.volume()==before&&units(shortBend,new BlockPos(32,3,0))==4,
        "Short-bend control quantity");
    var longBend=bend(256);before=longBend.volume();var budget=new Budget();
    int connected=reachable(longBend,origin);
    check(connected==262&&longBend.water.size()==262,"Fixture must be a face-connected sealed bend");
    boolean moved=equalize(longBend,origin,budget);
    boolean defect=!moved&&units(longBend,new BlockPos(256,3,0))==0
        &&budget.pressureVisits==PRESSURE_SEARCH&&budget.pressure.isEmpty();
    if(args.length>0&&args[0].equals("fixed"))check(!defect&&moved,"Pressure cutoff remains");
    else check(defect,"Expected cutoff reproduction changed; inspect the new implementation");
    int visits=budget.pressureVisits;
    for(int retry=0;retry<100&&defect;retry++){
      budget.pressureVisits=0;
      check(!equalize(longBend,origin,budget)&&budget.pressure.isEmpty(),"Restart unexpectedly progressed");
    }
    check(longBend.volume()==before,"Cutoff changed water quantity");
    System.out.println((defect?"PRESSURE_CUTOFF_REPRODUCED":"PRESSURE_CUTOFF_FIXED")
        +" connectedWaterCells="+connected+" searchLimit="+PRESSURE_SEARCH
        +" firstSearchVisits="+visits+" pendingOrigins="+budget.pressure.size()
        +" shortControlMovedUnits=4 externalRestarts=100 conserved=true native=false");
  }
}
'''.replace("@LIMIT@", str(limit)).replace("@SURFACE@", surface).replace("@PRESSURE@", pressure)
    with tempfile.TemporaryDirectory(prefix="deepprimal-pressure-") as directory:
        temp = Path(directory)
        java_file = temp / "PressureCutoffAudit.java"
        java_file.write_text(harness)
        core = args.root / "engine-core/src/main/java/com/shnewbs/deepprimal/core"
        compile_run = subprocess.run([str(args.java_home / "bin/javac"), "-d", str(temp),
                                     str(core / "WaterTransport.java"), str(core / "TerrainNoise.java"),
                                     str(java_file)], text=True, capture_output=True)
        run = compile_run
        if compile_run.returncode == 0:
            run = subprocess.run([str(args.java_home / "bin/java"), "-cp", str(temp),
                                  "PressureCutoffAudit", "fixed" if args.expect_fixed else "reproduce"],
                                 text=True, capture_output=True)
    record = {"scope": "extracted-production-pressure-search-with-world-doubles",
              "source_sha256": hashlib.sha256(source.encode()).hexdigest(),
              "exit_code": run.returncode, "stdout": run.stdout, "stderr": run.stderr,
              "defect_reproduced": "PRESSURE_CUTOFF_REPRODUCED" in run.stdout,
              "native_minecraft_test": False}
    if args.json:
        args.json.parent.mkdir(parents=True, exist_ok=True)
        args.json.write_text(json.dumps(record, indent=2) + "\n")
    print(run.stdout, end="");print(run.stderr, end="")
    return run.returncode


if __name__ == "__main__":
    raise SystemExit(main())
