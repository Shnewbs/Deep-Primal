#!/usr/bin/env python3
"""Exact V8 native fill production class; Java21 API-double coordinate admission.

This is not a NeoForge/Minecraft integration, durable disk, GPU or JAR test.
"""
from pathlib import Path
import hashlib
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / 'src/main/java/com/shnewbs/deepprimal/engine/V8ChunkGenerationNativeFill.java'
STUBS = ROOT / 'tools/tests/v8_native_fill_stubs'
TEST = ROOT / 'tools/tests/V8NativeCoordinateAdmissionChecks.java'
ANCHOR = 'requireWorldCoordinateRange(intended.chunkX(), intended.chunkZ());'


def run(source: str):
    with tempfile.TemporaryDirectory(prefix='v8-native-coordinate-') as directory:
        temp = Path(directory)
        production = temp / 'com/shnewbs/deepprimal/engine/V8ChunkGenerationNativeFill.java'
        production.parent.mkdir(parents=True)
        production.write_text(source, encoding='utf-8')
        sources = sorted(STUBS.rglob('*.java')) + [production, TEST]
        compiled = subprocess.run(
            ['javac', '--release', '21', '-Xlint:all', '-Werror', '-d', str(temp / 'classes'),
             *(str(path) for path in sources)], capture_output=True, text=True, timeout=45)
        if compiled.returncode:
            return compiled.returncode, compiled.stdout + compiled.stderr
        executed = subprocess.run(
            ['java', '-ea', '-Xverify:all', '-cp', str(temp / 'classes'),
             'com.shnewbs.deepprimal.engine.V8NativeCoordinateAdmissionChecks'],
            capture_output=True, text=True, timeout=45)
        return executed.returncode, executed.stdout + executed.stderr


def main():
    source = SOURCE.read_text(encoding='utf-8')
    expected = 'V8_NATIVE_COORDINATE_PASS assertions=100605'
    for _ in range(2):
        code, output = run(source)
        if code or expected not in output:
            raise RuntimeError('production regression: ' + output[-1000:])
    if source.count(ANCHOR) != 1:
        raise RuntimeError('missing/duplicate coordinate guard')
    mutant = source.replace(ANCHOR, 'java.util.Objects.requireNonNull(intended);', 1)
    code, output = run(mutant)
    if code == 0 or 'terrain range' not in output:
        raise RuntimeError('compiled guard-removal mutant not rejected: ' + output[-1000:])
    print('V8_NATIVE_COORDINATE_PASS 2x100605 assertions; compiled guard-removal mutant rejected; '
          'Java21 API doubles; sha256=' + hashlib.sha256(source.encode('utf-8')).hexdigest())


if __name__ == '__main__':
    main()
