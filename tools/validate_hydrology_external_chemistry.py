#!/usr/bin/env python3
"""Compile the exact native external-change method against isolated Java 25 stubs.

Tests composition invalidation and its negative control without claiming
Minecraft chunk/unload/save-reopen acceptance.
"""
import os
import subprocess
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "src/main/java/com/shnewbs/deepprimal/engine/HydrologyRuntime.java"


def method(source, signature):
    start = source.index("{", source.index(signature))
    depth = 0
    for i in range(start, len(source)):
        if source[i] == "{":
            depth += 1
        elif source[i] == "}":
            depth -= 1
            if depth == 0:
                return source[source.index(signature):i + 1]
    raise AssertionError("Unbalanced Java method: " + signature)


HARNESS = r'''import java.util.*;
public final class HydrologyRuntime {
  private static int internalWrites;
  static final class ServerLevel {}
  static final class BlockPos {long asLong(){return 42L;}}
  static final class EngineFluid {static final Units UNITS=new Units(); static final class Units {}}
  static final class EngineWaterBlock {static final Ocean OCEAN=new Ocean(); static final class Ocean {}}
  static final class Holder {Object get(){return EngineRegistry.WATER;}}
  static final class EngineRegistry {
    static final Object WATER=new Object();
    static final Holder WATER_BLOCK=new Holder();
  }
  static final class BlockState {
    final boolean water, ocean;
    final int units, flow;
    BlockState(boolean water, boolean ocean, int units, int flow) {
      this.water=water;this.ocean=ocean;this.units=units;this.flow=flow;
    }
    boolean is(Object block){return water && block==EngineRegistry.WATER;}
    Integer getValue(EngineFluid.Units property){return units;}
    Boolean getValue(EngineWaterBlock.Ocean property){return ocean;}
  }
  static final class HydrologyData {
    static final HydrologyData INSTANCE=new HydrologyData();
    final Map<Long,String> composition=new HashMap<>();
    int dirty;
    static HydrologyData get(ServerLevel l){return INSTANCE;}
    void setDirty(){dirty++;}
  }
  /*METHODS*/
  private static final ServerLevel LEVEL=new ServerLevel();
  private static final BlockPos POS=new BlockPos();
  private static BlockState state(boolean w,boolean ocean,int units,int flow){
    return new BlockState(w,ocean,units,flow);
  }
  private static int cases;
  private static void scenario(String label,BlockState before,BlockState after,
      boolean drop,int nestedWrites) {
    var d=HydrologyData.INSTANCE;
    d.composition.clear();d.composition.put(42L,"carried-chemistry");d.dirty=0;
    internalWrites=nestedWrites;
    externalChange(LEVEL,POS,before,after);
    internalWrites=0;
    if(d.composition.containsKey(42L)==drop || d.dirty!=(drop?1:0))
      throw new AssertionError(label+" drop="+drop+" retained="+d.composition
          +" dirty="+d.dirty);
    cases++;
  }
  public static void main(String[] args) {
    var river=state(true,false,16,0);
    scenario("river-to-ocean-same-volume",river,state(true,true,16,0),true,0);
    scenario("ocean-to-river-same-volume",state(true,true,16,0),river,true,0);
    scenario("flow-only-preserves-parcel",river,state(true,false,16,12),false,0);
    scenario("volume-change",river,state(true,false,15,0),true,0);
    scenario("block-removal",river,state(false,false,0,0),true,0);
    scenario("unrelated-block",state(false,false,0,0),river,false,0);
    scenario("internal-water-write",river,state(true,true,16,0),false,1);
    scenario("same-state-reference",river,river,false,0);
    var d=HydrologyData.INSTANCE;
    d.composition.clear();d.dirty=0;
    externalChange(LEVEL,POS,river,state(true,true,16,0));
    if(d.dirty!=0)throw new AssertionError("missing chemistry must not dirty save");
    cases++;
    System.out.println("HYDROLOGY_EXTERNAL_CHEMISTRY_PASS cases="+cases
        +" finiteVolumeUnchanged=true nestedWritesIgnored=true");
  }
}
'''


def run_case(methods, expect_pass, label):
    with tempfile.TemporaryDirectory(prefix="deepprimal-chemistry-") as path:
        f = Path(path) / "HydrologyRuntime.java"
        f.write_text(HARNESS.replace("/*METHODS*/", methods), encoding="utf-8")
        java = Path(os.environ["JAVA_HOME"]) / "bin" / "java"
        javac = Path(os.environ["JAVA_HOME"]) / "bin" / "javac"
        compiled = subprocess.run([str(javac), "--release", "25", str(f)],
                                  capture_output=True, text=True)
        if compiled.returncode:
            raise AssertionError(label + " source compile failure:\n" + compiled.stderr)
        ran = subprocess.run([str(java), "-Xverify:all", "-cp", path,
                              "HydrologyRuntime"], capture_output=True, text=True)
        if (ran.returncode == 0) != expect_pass:
            raise AssertionError(label + " unexpected result:\n" + ran.stdout + ran.stderr)
        return (ran.stdout if expect_pass else ran.stderr).strip()


def main():
    source = SOURCE.read_text(encoding="utf-8")
    boundary = method(source, "public static boolean boundary(")
    external = method(source, "public static void externalChange(")
    needle = "|| boundary(before) != boundary(after)"
    if external.count(needle) != 1:
        raise AssertionError("Missing/duplicate boundary transition guard")
    methods = boundary + "\n" + external
    positive = run_case(methods, True, "production")
    negative = run_case(methods.replace(needle, "", 1), False, "negative-control")
    if "river-to-ocean-same-volume" not in negative:
        raise AssertionError("Wrong negative-control failure: " + negative)
    print(positive)
    print("HYDROLOGY_EXTERNAL_CHEMISTRY_MUTATION_REJECTED marker=true")


if __name__ == "__main__":
    main()
