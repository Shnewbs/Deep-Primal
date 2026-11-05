#!/usr/bin/env python3
"""Compile/run isolated ecotone checks and rejection controls, without Gradle or game launch.

Default requires a full source checkout's unchanged legacy Landscape classifier. Use
--isolated only for the two-file component snapshot; the receipt explicitly records
that the legacy source and complete build were not locally verified.
"""
from __future__ import annotations

import argparse
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import sys
import tempfile
from datetime import datetime, timezone

PRODUCTION = Path('engine-core/src/main/java/com/shnewbs/deepprimal/core/biome/EcotoneBlend.java')
CHECKS = Path('src/coreChecks/java/com/shnewbs/deepprimal/core/EcotoneBlendChecks.java')
LEGACY = Path('engine-core/src/main/java/com/shnewbs/deepprimal/core/Landscape.java')
LEGACY_BLOB = '3c7b31e84c952878671511924188175f2b5181a0'
MAIN = 'com.shnewbs.deepprimal.core.EcotoneBlendChecks'


def git_blob(data: bytes) -> str:
    return hashlib.sha1(b'blob ' + str(len(data)).encode() + b'\0' + data).hexdigest()


def tool(name: str) -> str:
    home = os.environ.get('JAVA_HOME')
    if home:
        candidate = Path(home) / 'bin' / (name + ('.exe' if os.name == 'nt' else ''))
        if candidate.is_file():
            return str(candidate)
        raise RuntimeError(f'JAVA_HOME does not contain {name}')
    found = shutil.which(name)
    if not found:
        raise RuntimeError(f'{name} is unavailable')
    return found


def invoke(argv: list[str], timeout: int = 90) -> dict:
    print('RUN ' + ' '.join(argv), file=sys.stderr, flush=True)
    completed = subprocess.run(argv, stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
                               text=True, timeout=timeout, check=False)
    return {'exit_code': completed.returncode, 'output': completed.stdout}


def require(condition: bool, message: str) -> None:
    # Do not weaken validation under python -O or -OO.
    if not condition:
        raise RuntimeError(message)


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--root', type=Path, default=Path(__file__).resolve().parents[1])
    parser.add_argument('--output', type=Path, required=True, help='Fresh evidence directory; never overwritten')
    parser.add_argument('--isolated', action='store_true')
    args = parser.parse_args()
    root = args.root.resolve()
    args.output.mkdir(parents=True, exist_ok=False)
    result: dict = {'scope': 'dependency-free ecotone source checks, not native/Gradle/JAR acceptance',
                   'started_at': datetime.now(timezone.utc).isoformat(), 'all_passed': False,
                   'java_release_target': 21, 'isolated_component_snapshot': args.isolated,
                   'native_tested': False, 'full_gradle_build': False, 'new_jar': False}
    try:
        sources = {str(p): (root / p).read_bytes() for p in (PRODUCTION, CHECKS)}
        result['sources'] = {p: {'sha256': hashlib.sha256(data).hexdigest(),
                                'git_blob': git_blob(data), 'bytes': len(data)}
                             for p, data in sources.items()}
        if (root / LEGACY).is_file():
            actual = git_blob((root / LEGACY).read_bytes())
            require(actual == LEGACY_BLOB, 'Legacy classifier changed: review the independent hard oracle first')
            result['legacy_blob_verified'] = actual
        else:
            require(args.isolated, 'Landscape source absent: require complete checkout or explicit --isolated')
            result['legacy_blob_verified'] = None
        java, javac = tool('java'), tool('javac')
        result['java_version'] = invoke([java, '-version'])
        result['javac_version'] = invoke([javac, '-version'])
        require(result['java_version']['exit_code'] == 0 and result['javac_version']['exit_code'] == 0,
                'Java toolchain probe failed')
        with tempfile.TemporaryDirectory(prefix='deepprimal-ecotone-') as directory:
            temp = Path(directory)
            classes = temp / 'classes'
            result['compile'] = invoke([javac, '--release', '21', '-Xlint:all', '-Werror',
                                       '-d', str(classes), str(root / PRODUCTION), str(root / CHECKS)])
            require(result['compile']['exit_code'] == 0, 'Component compile failed')
            runs = [invoke([java, '-ea', '-Xverify:all', '-cp', str(classes), MAIN]) for _ in range(2)]
            result['positive_runs'] = runs
            for run in runs:
                require(run['exit_code'] == 0 and 'ECOTONE_BLEND_PASS ' in run['output'], 'Component check failed')
            require(runs[0]['output'] == runs[1]['output'], 'Separate process counts/fingerprint differ')
            result['fingerprint'] = re.search(r'fingerprint=([0-9a-f]{64})', runs[0]['output']).group(1)
            source = sources[str(PRODUCTION)].decode('utf-8')
            changes = {
                'non_strict_legacy_boundary': ('value < threshold ? 1 : 0', 'value <= threshold ? 1 : 0', 1),
                'disabled_smoothing': ('return width == 0 ?', 'return true ?', 2),
            }
            result['negative_controls'] = {}
            for name, (old, new, count) in changes.items():
                require(source.count(old) == count, f'Negative control anchor changed: {name}')
                mutant = temp / name
                mutant.mkdir()
                file = mutant / 'EcotoneBlend.java'
                file.write_text(source.replace(old, new), encoding='utf-8')
                compile_result = invoke([javac, '--release', '21', '-Xlint:all', '-Werror',
                                         '-d', str(mutant / 'classes'), str(file), str(root / CHECKS)])
                require(compile_result['exit_code'] == 0, f'Negative control failed to compile: {name}')
                run = invoke([java, '-ea', '-Xverify:all', '-cp', str(mutant / 'classes'), MAIN])
                result['negative_controls'][name] = {'compile': compile_result, 'run': run}
                require(run['exit_code'] != 0 and 'AssertionError' in run['output']
                        and 'ECOTONE_BLEND_PASS' not in run['output'], f'Broken control was accepted: {name}')
        for path, original in sources.items():
            require((root / path).read_bytes() == original, 'Source changed during checks')
        result['all_passed'] = True
    except (OSError, ValueError, RuntimeError, subprocess.TimeoutExpired) as error:
        result['error'] = str(error)
    finally:
        result['completed_at'] = datetime.now(timezone.utc).isoformat()
        text = json.dumps(result, indent=2) + '\n'
        (args.output / 'results.json').write_text(text, encoding='utf-8')
        print(text)
    return 0 if result['all_passed'] else 1


if __name__ == '__main__':
    sys.exit(main())
