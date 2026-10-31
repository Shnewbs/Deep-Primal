#!/usr/bin/env python3
"""Dev120: compile exact native V8 commit coordinator with isolated Java21 API doubles.

Does NOT execute a real Minecraft/NeoForge chunk serializer or durable transaction.
"""
from pathlib import Path
import hashlib
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / 'src/main/java/com/shnewbs/deepprimal/engine/V8ChunkGenerationNativeCommit.java'
STUBS = ROOT / 'tools/tests/v8_native_commit_stubs'
TEST = ROOT / 'tools/tests/V8ChunkGenerationNativeCommitChecks.java'


def execute(source: str):
    with tempfile.TemporaryDirectory(prefix='v8-native-commit-') as temp:
        work = Path(temp)
        prod = work / 'com/shnewbs/deepprimal/engine/V8ChunkGenerationNativeCommit.java'
        prod.parent.mkdir(parents=True)
        prod.write_text(source, encoding='utf-8')
        files = sorted(STUBS.rglob('*.java')) + [prod, TEST]
        compile_run = subprocess.run(
            ['javac', '--release', '21', '-Xlint:all', '-Werror', '-d', str(work / 'classes'),
             *(str(file) for file in files)], capture_output=True, text=True, timeout=60)
        if compile_run.returncode:
            return compile_run.returncode, compile_run.stdout + compile_run.stderr
        run = subprocess.run(
            ['java', '-ea', '-Xverify:all', '-cp', str(work / 'classes'),
             'com.shnewbs.deepprimal.engine.V8ChunkGenerationNativeCommitChecks'],
            capture_output=True, text=True, timeout=60)
        return run.returncode, run.stdout + run.stderr


def main():
    source = SOURCE.read_text(encoding='utf-8')
    code, result = execute(source)
    if code or 'V8_NATIVE_COMMIT_PASS assertions=30004' not in result:
        raise RuntimeError('production rejected: ' + result[-1200:])
    code2, result2 = execute(source)
    if code2 or result2 != result:
        raise RuntimeError('nondeterministic production: ' + result2[-1200:])
    mutants = {
        'skip-precommit-recheck': (
            'V8ChunkGenerationCommitGate.requireUnchanged(admission,\n'
            '        Objects.requireNonNull(owner.snapshot(), "durable V8 commit snapshot"));',
            'Objects.requireNonNull(owner.snapshot(), "durable V8 commit snapshot");'),
        'allow-epoch-wrap': (
            'Math.addExact(admission.snapshot().targetEpoch(), 1L)',
            'admission.snapshot().targetEpoch() + 1L'),
        'allow-ticket-reuse': (
            'if (!ticket.consumed.compareAndSet(false, true))',
            'if (false)'),
    }
    for name, (original, broken) in mutants.items():
        if source.count(original) != 1:
            raise RuntimeError('mutation anchor missing: ' + name)
        bad, output = execute(source.replace(original, broken, 1))
        if bad == 0 or 'accepted invalid input' not in output and 'AssertionError' not in output:
            raise RuntimeError('mutant not rejected: ' + name + ': ' + output[-600:])
    print('V8_NATIVE_COMMIT_PASS 2x30004 assertions; 3/3 compiled mutants rejected; mode=Java21 API doubles; sha256='
          + hashlib.sha256(source.encode('utf-8')).hexdigest())


if __name__ == '__main__':
    main()
