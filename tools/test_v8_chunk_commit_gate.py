#!/usr/bin/env python3
"""Exact production V8 stamp/commit-gate Java21 core test; no Minecraft or NBT."""
from __future__ import annotations

import hashlib
import pathlib
import subprocess
import sys
import tempfile

ROOT = pathlib.Path(__file__).resolve().parents[1]
PKG = pathlib.Path('com/shnewbs/deepprimal/core/density')
CORE = ROOT / 'engine-core/src/main/java' / PKG
TEST = ROOT / 'tools/tests/V8ChunkGenerationCommitGateChecks.java'
SOURCES = ('V8GenerationBorderAdmission.java', 'V8ChunkGenerationStamp.java',
           'V8ChunkGenerationCommitGate.java')
ANCHOR = 'original.epoch() != observed.epoch()'


def execute(source_override: str | None = None) -> tuple[int, str]:
    with tempfile.TemporaryDirectory(prefix='v8-commit-gate-') as directory:
        work = pathlib.Path(directory)
        sources = []
        for name in SOURCES:
            content = (CORE / name).read_text(encoding='utf-8')
            if name == 'V8ChunkGenerationCommitGate.java' and source_override is not None:
                content = source_override
            destination = work / PKG / name
            destination.parent.mkdir(parents=True, exist_ok=True)
            destination.write_text(content, encoding='utf-8')
            sources.append(str(destination))
        sources.append(str(TEST))
        classes = work / 'classes'
        classes.mkdir()
        subprocess.run(['javac', '--release', '21', '-Xlint:all', '-Werror',
                        '-d', str(classes), *sources], check=True, timeout=90,
                       capture_output=True, text=True)
        result = subprocess.run(['java', '-ea', '-Xverify:all', '-cp', str(classes),
                                 'com.shnewbs.deepprimal.core.density.V8ChunkGenerationCommitGateChecks'],
                                timeout=90, capture_output=True, text=True)
        return result.returncode, result.stdout + result.stderr


def main() -> None:
    production = (CORE / 'V8ChunkGenerationCommitGate.java').read_text(encoding='utf-8')
    if production.count(ANCHOR) != 1:
        raise RuntimeError('commit epoch guard source anchor missing/duplicated')
    digest = hashlib.sha256(production.encode('utf-8')).hexdigest()
    first, output = execute()
    if first != 0 or 'V8_COMMIT_GATE_PASS assertions=60009' not in output:
        raise RuntimeError('production gate rejected: ' + output[-1000:])
    second, repeated = execute()
    if second != 0 or output != repeated:
        raise RuntimeError('nondeterministic gate result: ' + repeated[-1000:])
    mutant = production.replace(ANCHOR, 'false', 1)
    broken, failure = execute(mutant)
    if broken == 0 or 'accepted invalid input' not in failure:
        raise RuntimeError('compiled epoch-check mutant was not rejected: ' + failure[-1000:])
    print('V8_COMMIT_GATE_PASS 2x60009 assertions; epoch mutant rejected; sha256=' + digest)


if __name__ == '__main__':
    main()
