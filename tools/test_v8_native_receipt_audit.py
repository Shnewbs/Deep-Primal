#!/usr/bin/env python3
"""Compile exact receipt source against explicit API doubles; never a native-game claim."""
import os
from pathlib import Path
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / 'src/main/java/com/shnewbs/deepprimal/engine/V8VegetationNativeReceipt.java'
CLASS = 'com.shnewbs.deepprimal.engine.ReceiptAuditChecks'
STUBS = {
 'net/minecraft/core/BlockPos.java': '''package net.minecraft.core;
public class BlockPos { public int x,y,z;
 public static final class MutableBlockPos extends BlockPos {
 public MutableBlockPos set(int a,int b,int c){x=a;y=b;z=c;return this;}}}''',
 'net/minecraft/core/Direction.java': 'package net.minecraft.core; public enum Direction {UP}',
 'net/minecraft/world/level/block/state/BlockState.java': '''package net.minecraft.world.level.block.state;
import net.minecraft.core.*; import net.minecraft.world.level.chunk.ChunkAccess;
public record BlockState(String id, boolean dry, boolean sturdy) {
 public boolean isAir(){return id.equals("air");}
 public record Fluid(boolean isEmpty){}
 public Fluid getFluidState(){return new Fluid(dry);}
 public boolean isFaceSturdy(ChunkAccess c,BlockPos p,Direction d){return sturdy;}}''',
 'net/minecraft/world/level/chunk/ChunkAccess.java': '''package net.minecraft.world.level.chunk;
import net.minecraft.core.*; import net.minecraft.world.level.block.state.BlockState;
public class ChunkAccess {
 public final BlockState[] support=new BlockState[256],plant=new BlockState[256];
 public boolean fail; public int reads;
 public int getMinY(){return -64;} public int getHeight(){return 384;}
 public record Pos(){public int getMinBlockX(){return -32;} public int getMinBlockZ(){return -16;}}
 public Pos getPos(){return new Pos();}
 public BlockState getBlockState(BlockPos p){if(fail)throw new IllegalStateException("read failure");
 reads++; int i=(p.z+16)*16+p.x+32;return p.y==63?support[i]:plant[i];}}''',
 'com/shnewbs/deepprimal/core/biome/EcotoneSurfaceField.java': 'package com.shnewbs.deepprimal.core.biome; public final class EcotoneSurfaceField {}',
 'com/shnewbs/deepprimal/engine/V8EcotoneColumnPlan.java': '''package com.shnewbs.deepprimal.engine;
public class V8EcotoneColumnPlan {
 public enum Kind {BARE,GRASS,PODZOL}
 public record Cover(boolean snowPatch,Kind cover){}
 public record Column(Cover cover,int topY){}
 public final Column[] columns=new Column[256];
 public Column column(int x,int z){return columns[z*16+x];}}''',
 'com/shnewbs/deepprimal/engine/V8VegetationColumnPlan.java': '''package com.shnewbs.deepprimal.engine;
public class V8VegetationColumnPlan {
 public enum Plant {FERN,SHORT_GRASS}
 public record Placement(int y,Plant plant){}
 public final Placement[] placements=new Placement[256]; public int total;
 public Placement placement(int x,int z){return placements[z*16+x];}
 public int count(){return total;}}''',
 'com/shnewbs/deepprimal/engine/ReceiptAuditChecks.java': '''package com.shnewbs.deepprimal.engine;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.block.state.BlockState;
public final class ReceiptAuditChecks {
 static int checks;
 static void check(boolean ok){checks++;if(!ok)throw new AssertionError("receipt check "+checks);}
 static final BlockState GRASS=new BlockState("grass",true,true),
 PODZOL=new BlockState("podzol",true,true), FERN=new BlockState("fern",true,false),
 SHORT=new BlockState("short",true,false), AIR=new BlockState("air",true,false);
 static V8VegetationNativeReceipt capture(ChunkAccess c,V8VegetationColumnPlan p,V8EcotoneColumnPlan s){
 return V8VegetationNativeReceipt.capture(c,p,s,GRASS,PODZOL,SHORT,FERN);}
 public static void main(String[] args){
 var c=new ChunkAccess();var p=new V8VegetationColumnPlan();var s=new V8EcotoneColumnPlan();
 // Dense signed-coordinate chunk crosses every 64-bit receipt boundary.
 for(int i=0;i<256;i++){
  p.placements[i]=new V8VegetationColumnPlan.Placement(64,i%2==0?V8VegetationColumnPlan.Plant.FERN:V8VegetationColumnPlan.Plant.SHORT_GRASS);
  s.columns[i]=new V8EcotoneColumnPlan.Column(new V8EcotoneColumnPlan.Cover(false,i%2==0?V8EcotoneColumnPlan.Kind.GRASS:V8EcotoneColumnPlan.Kind.PODZOL),63);
  c.support[i]=i%2==0?GRASS:PODZOL;c.plant[i]=i%2==0?FERN:SHORT;
 }p.total=256;
 var full=capture(c,p,s);check(full.plannedCount()==256&&full.realizedCount()==256&&full.skippedCount()==0);
 for(int z=0;z<16;z++)for(int x=0;x<16;x++)check(full.realized(x,z));
 var words=full.realizedWords();words[0]=0;check(full.realized(0,0));
 // Callback-style changes: displaced/wet/non-sturdy support, species overwrite,
 // wet foliage and snow must all be absent from realized placements.
 c.support[0]=PODZOL;c.support[1]=new BlockState("podzol",false,true);
 c.support[2]=new BlockState("grass",true,false);c.plant[3]=AIR;
 c.plant[4]=new BlockState("fern",false,false);
 s.columns[5]=new V8EcotoneColumnPlan.Column(new V8EcotoneColumnPlan.Cover(true,V8EcotoneColumnPlan.Kind.PODZOL),63);
 var changed=capture(c,p,s);check(changed.realizedCount()==250&&changed.skippedCount()==6);
 for(int i=0;i<256;i++)check(changed.realized(i%16,i/16)==(i>=6));
 check(full.realizedCount()==256&&full.realized(0,0));
 for(int[] xy:new int[][]{{-1,0},{16,0},{0,-1},{0,16}}){
  boolean rejected=false;try{changed.realized(xy[0],xy[1]);}catch(IndexOutOfBoundsException e){rejected=true;}check(rejected);}
 p.total=255;boolean mismatch=false;try{capture(c,p,s);}catch(IllegalStateException e){mismatch=true;}check(mismatch);
 p.total=256;c.fail=true;boolean readFailure=false;try{capture(c,p,s);}catch(IllegalStateException e){readFailure=true;}check(readFailure);
 check(changed.realizedCount()==250);System.out.println("V8_RECEIPT_AUDIT_PASS assertions="+checks);
 }}'''
}

def run(source):
    with tempfile.TemporaryDirectory(prefix='receipt-audit-') as tmp:
        root = Path(tmp)
        files = dict(STUBS)
        files['com/shnewbs/deepprimal/engine/V8VegetationNativeReceipt.java'] = source
        for name, content in files.items():
            path = root / name
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(content)
        classes = root / 'classes'
        compile_result = subprocess.run([os.getenv('JAVAC', 'javac'), '--release', '21',
            '-Xlint:all', '-Werror', '-d', str(classes),
            *[str(root / name) for name in files]], capture_output=True, text=True, timeout=60)
        if compile_result.returncode:
            raise RuntimeError('Receipt fixture compilation failed: ' + compile_result.stderr)
        return subprocess.run([os.getenv('JAVA', 'java'), '-ea', '-Xverify:all',
            '-cp', str(classes), CLASS], capture_output=True, text=True, timeout=60)

def main():
    source = SOURCE.read_text()
    positive = run(source)
    if positive.returncode or 'V8_RECEIPT_AUDIT_PASS assertions=' not in positive.stdout:
        raise RuntimeError('Receipt positive failed: ' + positive.stdout + positive.stderr)
    mutations = [
        ('support', '!support.equals(actualSupport) || ', ''),
        ('species', '!species.equals(actualPlant) || ', ''),
        ('count', 'if (candidateCount != plants.count())', 'if (false)'),
        ('copy', 'return realizedWords.clone();', 'return realizedWords;'),
    ]
    for name, old, new in mutations:
        if source.count(old) != 1:
            raise RuntimeError('Receipt mutation anchor changed: ' + name)
        result = run(source.replace(old, new, 1))
        if (result.returncode != 1
                or not result.stderr.startswith('Exception in thread "main" java.lang.AssertionError:')
                or '\tat ' + CLASS + '.' not in result.stderr):
            raise RuntimeError('Receipt mutation not killed by fixture: ' + name + result.stderr)
    print(positive.stdout.strip() + ' compiled_mutations_rejected=4 native=false')

if __name__ == '__main__':
    main()
