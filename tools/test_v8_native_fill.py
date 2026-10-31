#!/usr/bin/env python3
"""Dev121 native V8 fill/publish lifecycle: exact source, isolated Java21 API doubles.

No Minecraft runtime, real chunk serializer, durable CAS, Gradle, or GPU is used.
"""
from pathlib import Path
import hashlib
import re
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / 'src/main/java/com/shnewbs/deepprimal/engine/V8ChunkGenerationNativeFill.java'
STUBS = ROOT / 'tools/tests/v8_native_fill_stubs'
TEST = ROOT / 'tools/tests/V8ChunkGenerationNativeFillChecks.java'


def execute(source: str):
    with tempfile.TemporaryDirectory(prefix='v8-native-fill-') as directory:
        temp = Path(directory)
        production = temp / 'com/shnewbs/deepprimal/engine/V8ChunkGenerationNativeFill.java'
        production.parent.mkdir(parents=True)
        production.write_text(source, encoding='utf-8')
        sources = sorted(STUBS.rglob('*.java')) + [production, TEST]
        compile_run = subprocess.run(
            ['javac', '--release', '21', '-Xlint:all', '-Werror', '-d', str(temp / 'classes'),
             *(str(path) for path in sources)], capture_output=True, text=True, timeout=45)
        if compile_run.returncode:
            return compile_run.returncode, compile_run.stdout + compile_run.stderr
        run = subprocess.run(
            ['java', '-ea', '-Xverify:all', '-cp', str(temp / 'classes'),
             'com.shnewbs.deepprimal.engine.V8ChunkGenerationNativeFillChecks'],
            capture_output=True, text=True, timeout=45)
        return run.returncode, run.stdout + run.stderr


def main():
    source = SOURCE.read_text(encoding='utf-8')
    baseline = None
    for _ in range(2):
        code, output = execute(source)
        found = re.search(r'V8_NATIVE_FILL_PASS assertions=(\d+)', output)
        if code or not found or int(found.group(1)) != 36044:
            raise RuntimeError('production failed: ' + output[-900:])
        if baseline is not None and output != baseline:
            raise RuntimeError('nondeterministic positive run')
        baseline = output
    mutants = {
        'skip-profile-fingerprint': (
            'prepared.requireSavedProfileFingerprint(expectedFingerprint);',
            'Objects.requireNonNull(prepared);'),
        'skip-native-discard': (
            'discard.discard(candidate, candidateNbt);',
            'Objects.requireNonNull(discard);'),
        'skip-durable-commit': (
            'V8ChunkGenerationNativeCommit.commit(ticket, owner, candidateNbt)',
            'new V8ChunkGenerationNbtAdapter.Generated(intended, 42L)'),
        'skip-coordinate-identity': (
            '|| intended.chunkX() != candidate.getPos().x',
            '|| false'),
        'skip-coordinate-range-admission': (
            'requireWorldCoordinateRange(intended.chunkX(), intended.chunkZ());',
            'Objects.requireNonNull(intended);'),
        'skip-vegetation-revision-admission': (
            'if (prepared.snapshot().version() >= V8GeneratorSnapshot.VEGETATION_VERSION)',
            'if (false)'),
        'skip-prewrite-nbt-admission': (
            'V8ChunkGenerationNbtAdapter.requireFreshCandidate(candidateNbt);',
            'Objects.requireNonNull(candidateNbt);'),
    }
    for name, (anchor, broken) in mutants.items():
        if source.count(anchor) != 1:
            raise RuntimeError('mutation anchor missing: ' + name)
        code, output = execute(source.replace(anchor, broken, 1))
        if code == 0 or ('accepted invalid input' not in output
                         and 'AssertionError' not in output):
            raise RuntimeError('broken variant not rejected: ' + name + ': ' + output[-900:])
    print('V8_NATIVE_FILL_PASS 2x' + re.search(r'assertions=(\d+)', baseline).group(1)
          + ' assertions; 7/7 compiled broken controls rejected; '
          'Java21 API doubles; sha256=' + hashlib.sha256(source.encode('utf-8')).hexdigest())


if __name__ == '__main__':
    main()
