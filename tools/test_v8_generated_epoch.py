#!/usr/bin/env python3
"""Exact-source Java21 generated-epoch core regression; not native Minecraft/NBT."""
from __future__ import annotations
import hashlib
from pathlib import Path
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[1]
CORE = ROOT / 'engine-core/src/main/java/com/shnewbs/deepprimal/core/density'
TEST = ROOT / 'tools/tests/GeneratedEpochChecks.java'
FILES = ('V8GenerationBorderAdmission.java', 'V8ChunkGenerationStamp.java',
         'V8ChunkGenerationCommitGate.java')
NEIGHBOR = 'if (state == V8GenerationBorderAdmission.State.GENERATED && epoch == 0L)'
TARGET = 'if (targetState == V8GenerationBorderAdmission.State.GENERATED && targetEpoch == 0L)'


def execute(override: str | None = None) -> tuple[int, str]:
    with tempfile.TemporaryDirectory(prefix='v8-generated-epoch-') as tmp:
        work = Path(tmp)
        sources = []
        for name in FILES:
            source = override if name == FILES[-1] and override is not None else (
                CORE / name).read_text(encoding='utf-8')
            dest = work / name
            dest.write_text(source, encoding='utf-8')
            sources.append(str(dest))
        classes = work / 'classes'
        classes.mkdir()
        compile_result = subprocess.run(
            ['javac', '--release', '21', '-Xlint:all', '-Werror',
             '-d', str(classes), *sources, str(TEST)],
            capture_output=True, text=True, timeout=90)
        if compile_result.returncode:
            return compile_result.returncode, compile_result.stdout + compile_result.stderr
        result = subprocess.run(
            ['java', '-ea', '-Xverify:all', '-cp', str(classes),
             'com.shnewbs.deepprimal.core.density.GeneratedEpochChecks'],
            capture_output=True, text=True, timeout=90)
        return result.returncode, result.stdout + result.stderr


def main() -> None:
    production = (CORE / FILES[-1]).read_text(encoding='utf-8')
    for anchor in (NEIGHBOR, TARGET):
        if production.count(anchor) != 1:
            raise RuntimeError('missing or duplicated generated-epoch guard: ' + anchor)
    first, output = execute()
    if first or 'V8_GENERATED_EPOCH_PASS assertions=120000' not in output:
        raise RuntimeError('production failed: ' + output[-1000:])
    second, repeated = execute()
    if second or repeated != output:
        raise RuntimeError('nondeterministic production: ' + repeated[-1000:])
    for label, anchor in (('neighbor', NEIGHBOR), ('target', TARGET)):
        broken, failure = execute(production.replace(anchor, 'if (false)', 1))
        if broken == 0 or 'accepted invalid input' not in failure:
            raise RuntimeError('compiled ' + label + ' mutant not rejected: ' + failure[-1000:])
    print('V8_GENERATED_EPOCH_PASS 2x120000 assertions; 2/2 compiled mutants rejected; '
          'mode=Java21 core; sha256=' + hashlib.sha256(production.encode()).hexdigest())


if __name__ == '__main__':
    main()
