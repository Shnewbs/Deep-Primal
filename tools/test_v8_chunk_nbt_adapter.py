#!/usr/bin/env python3
"""Compile exact production V8 NBT adapter against isolated Java21 API doubles.

Not a NeoForge/Minecraft save, chunk serializer, GPU, or durable transaction test.
"""
from __future__ import annotations
import hashlib
from pathlib import Path
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / 'src/main/java/com/shnewbs/deepprimal/engine/V8ChunkGenerationNbtAdapter.java'
TEST = ROOT / 'tools/tests/V8ChunkGenerationNbtAdapterChecks.java'
ANCHOR = 'actual.epoch() != expectedDurableEpoch'
STUBS = {
 'net/minecraft/nbt/Tag.java': '''package net.minecraft.nbt; public interface Tag {}''',
 'net/minecraft/nbt/StringTag.java': '''package net.minecraft.nbt; public final class StringTag implements Tag {private final String value; public StringTag(String v){value=v;} public String getAsString(){return value;}}''',
 'net/minecraft/nbt/CompoundTag.java': '''package net.minecraft.nbt; import java.util.*; public final class CompoundTag {private final Map<String,Tag> m=new HashMap<>(); public Tag get(String k){return m.get(k);} public String getStringOr(String k,String d){Tag t=m.get(k);return t instanceof StringTag s?s.getAsString():d;} public void putString(String k,String v){m.put(k,new StringTag(v));} public void put(String k,Tag t){m.put(k,t);}}''',
 'com/shnewbs/deepprimal/core/density/V8GenerationBorderAdmission.java': '''package com.shnewbs.deepprimal.core.density; public final class V8GenerationBorderAdmission {private V8GenerationBorderAdmission(){} public enum Direction {NORTH,EAST,SOUTH,WEST} public enum State {UNGENERATED,GENERATED,UNKNOWN}}''',
 'com/shnewbs/deepprimal/core/density/V8ChunkGenerationCommitGate.java': '''package com.shnewbs.deepprimal.core.density; import java.util.List; public final class V8ChunkGenerationCommitGate {private V8ChunkGenerationCommitGate(){} public record VersionedNeighbor(V8GenerationBorderAdmission.Direction direction,V8GenerationBorderAdmission.State state,String persistedStamp,long epoch){} public record Snapshot(V8GenerationBorderAdmission.State targetState,String targetPersistedStamp,long targetEpoch,List<VersionedNeighbor> neighbors){}}''',
 'com/shnewbs/deepprimal/core/density/V8ChunkGenerationStamp.java': '''package com.shnewbs.deepprimal.core.density; import java.nio.charset.StandardCharsets; import java.util.*; import java.util.zip.CRC32; public final class V8ChunkGenerationStamp {private V8ChunkGenerationStamp(){} public record Stamp(int revision,int chunkX,int chunkZ,long profileFingerprint){public Stamp{if(revision<2||revision>6||profileFingerprint==0)throw new IllegalArgumentException();} public String encode(){String b="dp-v8-chunk/1|"+revision+"|"+chunkX+"|"+chunkZ+"|"+String.format(Locale.ROOT,"%016x",profileFingerprint);CRC32 c=new CRC32();c.update(b.getBytes(StandardCharsets.US_ASCII));return b+"|"+String.format(Locale.ROOT,"%08x",c.getValue());}} public static Stamp decode(String text){String[] p=text.split("\\|",-1);if(p.length!=6||!p[0].equals("dp-v8-chunk/1"))throw new IllegalArgumentException();Stamp s=new Stamp(Integer.parseInt(p[1]),Integer.parseInt(p[2]),Integer.parseInt(p[3]),Long.parseUnsignedLong(p[4],16));if(!s.encode().equals(text))throw new IllegalArgumentException();return s;}}'''
}

def run(source: str) -> tuple[int, str]:
    with tempfile.TemporaryDirectory(prefix='v8-nbt-adapter-') as tmp:
        work = Path(tmp)
        java = []
        for name, content in STUBS.items():
            path = work / name
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(content, encoding='utf-8')
            java.append(str(path))
        production = work / 'com/shnewbs/deepprimal/engine/V8ChunkGenerationNbtAdapter.java'
        production.parent.mkdir(parents=True, exist_ok=True)
        production.write_text(source, encoding='utf-8')
        java += [str(production), str(TEST)]
        classes = work / 'classes'
        classes.mkdir()
        compile_result = subprocess.run(['javac','--release','21','-Xlint:all','-Werror',
            '-d',str(classes),*java],capture_output=True,text=True,timeout=90)
        if compile_result.returncode:
            return compile_result.returncode, compile_result.stdout+compile_result.stderr
        test_result = subprocess.run(['java','-ea','-Xverify:all','-cp',str(classes),
            'com.shnewbs.deepprimal.engine.V8ChunkGenerationNbtAdapterChecks'],
            capture_output=True,text=True,timeout=90)
        return test_result.returncode, test_result.stdout+test_result.stderr

def main() -> None:
    production = SOURCE.read_text(encoding='utf-8')
    if production.count(ANCHOR) != 1:
        raise AssertionError('missing or duplicated durable-epoch guard')
    digest = hashlib.sha256(production.encode('utf-8')).hexdigest()
    first, out = run(production)
    if first or 'V8_NBT_ADAPTER_PASS assertions=84706' not in out:
        raise AssertionError('production failed: '+out[-1200:])
    second, repeat = run(production)
    if second or repeat != out:
        raise AssertionError('nondeterministic production: '+repeat[-1200:])
    broken, failure = run(production.replace(ANCHOR,'false',1))
    if broken == 0 or 'accepted invalid input' not in failure:
        raise AssertionError('epoch mutant not rejected: '+failure[-1200:])
    print('V8_NBT_ADAPTER_PASS 2x84706 assertions; epoch mutant rejected; sha256='+digest)

if __name__ == '__main__':
    main()
