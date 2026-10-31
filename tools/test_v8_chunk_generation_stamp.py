#!/usr/bin/env python3
"""Strict Java21 source fixture for V8 persisted chunk identity and border admission."""
import argparse
import pathlib
import subprocess
import tempfile


def run(command):
    result = subprocess.run(command, text=True, capture_output=True, check=False)
    if result.returncode:
        raise RuntimeError(f"{command[0]} failed ({result.returncode}):\n{result.stdout}\n{result.stderr}")
    return result.stdout.strip()


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--root', type=pathlib.Path, default=pathlib.Path(__file__).resolve().parents[1])
    parser.add_argument('--component-only', action='store_true', help='skip native adapter static checks when full checkout is unavailable')
    args = parser.parse_args()
    root = args.root
    base = root / 'engine-core/src/main/java/com/shnewbs/deepprimal/core/density'
    production = base / 'V8ChunkGenerationStamp.java'
    border = base / 'V8GenerationBorderAdmission.java'
    fixture = root / 'tools/tests/V8ChunkGenerationStampChecks.java'
    original = production.read_text(encoding='utf-8')
    if not args.component_only:
        native = (root / 'src/main/java/com/shnewbs/deepprimal/engine/DensityChunkMaterializer.java').read_text(encoding='utf-8')
        for anchor in ('V8ChunkGenerationStamp.Stamp intended',
                       'V8ChunkGenerationStamp.witnessForNewChunk(',
                       'prepared.requireSavedProfileFingerprint(expectedSavedProfileFingerprint);'):
            if anchor not in native:
                raise RuntimeError('missing native admission anchor: ' + anchor)
    print(run(['javac', '-version']))
    with tempfile.TemporaryDirectory(prefix='deepprimal-v8-stamp-') as temporary:
        folder = pathlib.Path(temporary)
        output = folder / 'out'
        output.mkdir()
        compile_cmd = ['javac', '--release', '21', '-Xlint:all', '-Werror', '-d', str(output),
                       str(border), str(production), str(fixture)]
        run(compile_cmd)
        positive = run(['java', '-ea', '-Xverify:all', '-cp', str(output),
                        'com.shnewbs.deepprimal.core.density.V8ChunkGenerationStampChecks'])
        if positive != 'V8_STAMP_PASS assertions=65006':
            raise RuntimeError('unexpected positive result: ' + positive)
        print(positive)
        mutated = original.replace('neighbor.profileFingerprint() != intended.profileFingerprint()',
                                   'neighbor.profileFingerprint() == intended.profileFingerprint()')
        if mutated == original:
            raise RuntimeError('negative control target missing')
        mutant_source = folder / 'V8ChunkGenerationStamp.java'
        mutant_source.write_text(mutated, encoding='utf-8')
        mutant_output = folder / 'mutant-out'
        mutant_output.mkdir()
        run(['javac', '--release', '21', '-Xlint:all', '-Werror', '-d', str(mutant_output),
             str(border), str(mutant_source), str(fixture)])
        rejected = subprocess.run(['java', '-ea', '-Xverify:all', '-cp', str(mutant_output),
                                   'com.shnewbs.deepprimal.core.density.V8ChunkGenerationStampChecks'],
                                  text=True, capture_output=True, check=False)
        if rejected.returncode == 0 or 'incompatible persisted V8 neighbor' not in rejected.stderr:
            raise RuntimeError('compiled negative control was not rejected')
        print('V8_STAMP_MUTANT_REJECTED')


if __name__ == '__main__':
    main()
