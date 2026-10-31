#!/usr/bin/env python3
"""Check the exact production V8 coordinator's durable-epoch prewrite gate.

Focused Java API-double test, not Gradle, NeoForge, a disk transaction or GPU.
"""
from pathlib import Path
import hashlib
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[1]
NATIVE = ROOT / 'src/main/java/com/shnewbs/deepprimal/engine/V8ChunkGenerationNativeCommit.java'
NBT = ROOT / 'src/main/java/com/shnewbs/deepprimal/engine/V8ChunkGenerationNbtAdapter.java'
CORE = ROOT / 'engine-core/src/main/java/com/shnewbs/deepprimal/core/density'
TEST = ROOT / 'tools/tests/V8NativeEpochPreflightChecks.java'
NBT_DOUBLES = {
    'net/minecraft/nbt/Tag.java': 'package net.minecraft.nbt; public interface Tag {}',
    'net/minecraft/nbt/StringTag.java': 'package net.minecraft.nbt; public final class StringTag implements Tag {private final String value; public StringTag(String value){this.value=value;} public String getAsString(){return value;}}',
    'net/minecraft/nbt/CompoundTag.java': 'package net.minecraft.nbt; import java.util.*; public final class CompoundTag {private final Map<String,Tag> map=new HashMap<>(); public Tag get(String key){return map.get(key);} public String getStringOr(String key,String fallback){Tag value=map.get(key);return value instanceof StringTag s?s.getAsString():fallback;} public void putString(String key,String value){map.put(key,new StringTag(value));}}',
}


def execute(source: str) -> tuple[int, str]:
    with tempfile.TemporaryDirectory(prefix='v8-native-epoch-') as directory:
        work = Path(directory)
        java_sources = []
        for path, content in NBT_DOUBLES.items():
            target = work / path
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_text(content, encoding='utf-8')
            java_sources.append(str(target))
        native = work / 'com/shnewbs/deepprimal/engine/V8ChunkGenerationNativeCommit.java'
        native.parent.mkdir(parents=True, exist_ok=True)
        native.write_text(source, encoding='utf-8')
        java_sources.extend(str(CORE / name) for name in (
            'V8GenerationBorderAdmission.java', 'V8ChunkGenerationStamp.java',
            'V8ChunkGenerationCommitGate.java'))
        java_sources.extend([str(native), str(NBT), str(TEST)])
        classes = work / 'classes'
        classes.mkdir()
        compiler = subprocess.run(['javac', '--release', '21', '-Xlint:all', '-Werror',
            '-d', str(classes), *java_sources], capture_output=True, text=True, timeout=90)
        if compiler.returncode:
            return compiler.returncode, compiler.stdout + compiler.stderr
        runtime = subprocess.run(['java', '-ea', '-Xverify:all', '-cp', str(classes),
            'com.shnewbs.deepprimal.engine.V8NativeEpochPreflightChecks'],
            capture_output=True, text=True, timeout=90)
        return runtime.returncode, runtime.stdout + runtime.stderr


def main() -> None:
    source = NATIVE.read_text(encoding='utf-8')
    anchor = 'if (admission.snapshot().targetEpoch() == Long.MAX_VALUE)'
    if source.count(anchor) != 1:
        raise AssertionError('missing or duplicate epoch preflight')
    first, output = execute(source)
    if first or 'V8_NATIVE_EPOCH_PREFLIGHT_PASS assertions=72037' not in output:
        raise AssertionError('production failed: ' + output[-1000:])
    second, repeated = execute(source)
    if second or repeated != output:
        raise AssertionError('nondeterministic production result: ' + repeated[-1000:])
    broken, failure = execute(source.replace(anchor, 'if (false)', 1))
    if broken == 0 or 'exhausted epoch admitted' not in failure:
        raise AssertionError('epoch-overflow mutant not rejected: ' + failure[-1000:])
    guard = 'requireWorldCoordinateRange(intended.chunkX(), intended.chunkZ());'
    if source.count(guard) != 1:
        raise AssertionError('missing or duplicate coordinate preflight')
    broken_coordinate, coordinate_failure = execute(source.replace(guard, '/* coordinate gate bypassed */', 1))
    if broken_coordinate == 0 or 'coordinate overflow admitted' not in coordinate_failure:
        raise AssertionError('coordinate mutant not rejected: ' + coordinate_failure[-1000:])
    print('V8_NATIVE_EPOCH_PREFLIGHT_PASS 2x72037 assertions; 2 mutants rejected; 
          'sha256=' + hashlib.sha256(source.encode('utf-8')).hexdigest())


if __name__ == '__main__':
    main()
