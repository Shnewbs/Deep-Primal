#!/usr/bin/env python3
"""Compile the exact production water-unit reader with JDK21 API fakes.

Not a native game or real-world chunk-load test.
"""
from pathlib import Path
import re
import subprocess
import tempfile

root = Path(__file__).resolve().parents[4]
source = (root / "src/main/java/com/shnewbs/deepprimal/engine/WaterRuntime.java").read_text()
start = source.index("public static int units(ServerLevel l, BlockPos p) {")
opening = source.index("{", start)
depth = 0
end = None
for i in range(opening, len(source)):
    depth += (source[i] == "{") - (source[i] == "}")
    if depth == 0:
        end = i + 1
        break
assert end is not None
method = source[start:end]
assert method.count("l.getBlockState(p)") == 1
assert re.search(r"if\s*\(\s*!l\.isInWorldBounds\(p\)\s*\|\|\s*!l\.hasChunkAt\(p\)\s*\)\s*return\s+0\s*;", method)
assert method.index("!l.hasChunkAt(p)") < method.index("l.getBlockState(p)")
java = """package com.shnewbs.deepprimal.engine;
import java.util.function.Supplier;
public class LoadedReadProbe {
 static final class BlockPos {}
 static final class BlockState {
   final boolean water; final int amount;
   BlockState(boolean water,int amount){this.water=water;this.amount=amount;}
   boolean is(Object key){return water;}
   int getValue(Object key){return amount;}
 }
 static final class EngineFluid {static final Object UNITS=new Object();}
 static final class EngineRegistry {static final Supplier<Object> WATER_BLOCK=Object::new;}
 static final class ServerLevel {
   boolean bounds,loaded; BlockState state; int reads,checks;
   ServerLevel(boolean b,boolean l,BlockState s){bounds=b;loaded=l;state=s;}
   boolean isInWorldBounds(BlockPos p){checks++;return bounds;}
   boolean hasChunkAt(BlockPos p){checks++;return loaded;}
   BlockState getBlockState(BlockPos p){
     reads++; if(!bounds||!loaded)throw new AssertionError("forced unloaded read");
     return state;
   }
 }
""" + method + """
 static void check(boolean ok,String msg){if(!ok)throw new AssertionError(msg);}
 static void run(boolean b,boolean l,boolean wet,int amount,int expected,int reads,int checks){
   ServerLevel world=new ServerLevel(b,l,new BlockState(wet,amount));
   check(units(world,new BlockPos())==expected,"amount");
   check(world.reads==reads,"block reads");
   check(world.checks==checks,"gate calls");
 }
 public static void main(String[] args) {
   run(false,false,true,16,0,0,1);
   run(false,true,true,16,0,0,1);
   run(true,false,true,16,0,0,2);
   run(true,true,false,0,0,1,2);
   run(true,true,true,1,1,1,2);
   run(true,true,true,15,15,1,2);
   run(true,true,true,16,16,1,2);
   System.out.println("LOADED_ONLY_WATER_READ_PASS cases=7 source-extracted=true native_api_stubbed=true");
 }
}
"""
with tempfile.TemporaryDirectory() as td:
    out = Path(td)
    src = out / "LoadedReadProbe.java"
    src.write_text(java, encoding="utf-8")
    subprocess.run(["javac","-d",str(out),str(src)],check=True)
    subprocess.run(["java","-Xverify:all","-cp",str(out),
                    "com.shnewbs.deepprimal.engine.LoadedReadProbe"],check=True)
