#!/usr/bin/env python3
"""Strict isolated V8 border-admission test; not a Minecraft native fixture."""
from pathlib import Path
import subprocess
import tempfile
import shutil
import sys

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / 'engine-core/src/main/java/com/shnewbs/deepprimal/core/density/V8GenerationBorderAdmission.java'
TEST = ROOT / 'tools/tests/V8GenerationBorderAdmissionChecks.java'
MATERIALIZER = ROOT / 'src/main/java/com/shnewbs/deepprimal/engine/DensityChunkMaterializer.java'


def execute(source: str, *, mutant: bool) -> str:
    with tempfile.TemporaryDirectory(prefix='v8-border-') as folder:
        tmp = Path(folder)
        path = tmp / 'V8GenerationBorderAdmission.java'
        path.write_text(source, encoding='utf-8')
        out = tmp / 'classes'
        out.mkdir()
        compile_result = subprocess.run(
            ['javac', '--release', '21', '-Xlint:all', '-Werror', '-d', str(out),
             str(path), str(TEST)], capture_output=True, text=True, check=False)
        if compile_result.returncode:
            raise RuntimeError('strict Java21 compilation failed: ' + compile_result.stderr)
        result = subprocess.run(['java', '-ea', '-Xverify:all', '-cp', str(out),
                                 'V8GenerationBorderAdmissionChecks'],
                                capture_output=True, text=True, check=False)
        if mutant:
            if result.returncode == 0:
                raise AssertionError('broken neighbor-identity comparison passed')
            if 'incompatible generated V8 border' not in result.stderr:
                raise AssertionError('negative control did not reach behavioral rejection: '
                                     + result.stderr)
            return 'V8_BORDER_MUTANT_REJECTED'
        if result.returncode:
            raise RuntimeError('positive Java21 fixture failed: ' + result.stderr)
        if 'V8_BORDER_ADMISSION_PASS assertions=8102 native=false' not in result.stdout:
            raise AssertionError('unexpected positive fixture result: ' + result.stdout)
        return result.stdout.strip()


def main() -> None:
    if not shutil.which('javac') or not shutil.which('java'):
        raise RuntimeError('Java compiler/runtime missing')
    source = SRC.read_text(encoding='utf-8')
    materializer = MATERIALIZER.read_text(encoding='utf-8') if MATERIALIZER.exists() else ''
    required = [
        'V8GenerationBorderAdmission.requireCompatible(border,',
        'prepared.requireSavedProfileFingerprint(expectedSavedProfileFingerprint);',
        'fillSavedEcotoneVegetatedWithReceipt(prepared, expectedSavedProfileFingerprint,',
        'fillSavedSnapshot(prepared, expectedSavedProfileFingerprint, chunk, queriesPerSlab)',
    ]
    if materializer:
        for anchor in required:
            if anchor not in materializer:
                raise AssertionError('missing native opt-in border gate: ' + anchor)
    else:
        print('NATIVE_ADAPTER_SOURCE_NOT_PRESENT: isolated component only')
    print(execute(source, mutant=False))
    old = 'neighbor.savedProfileFingerprint() != expectedProfileFingerprint'
    if source.count(old) != 1:
        raise AssertionError('unexpected source identity/comparison anchor')
    print(execute(source.replace(old, 'neighbor.savedProfileFingerprint() == expectedProfileFingerprint'),
                  mutant=True))
    print('V8_BORDER_TEST_PASS java21_isolated=true native=false')


if __name__ == '__main__':
    try:
        main()
    except (OSError, RuntimeError, AssertionError) as error:
        sys.exit('V8_BORDER_TEST_FAIL: ' + str(error))
