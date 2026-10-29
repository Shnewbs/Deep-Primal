#!/usr/bin/env python3
"""Strict Java21 source-only V8 outlet ledger checks and compiled broken controls.

These checks do NOT exercise the native Minecraft save system, IO, Gradle, or GPU.
"""
import argparse
import hashlib
import json
import os
import pathlib
import re
import subprocess
import tempfile

ROOT = pathlib.Path(__file__).resolve().parents[1]
SOURCE = ROOT / 'engine-core/src/main/java/com/shnewbs/deepprimal/core/V8ConservedOutletLedger.java'
TOPOLOGY = ROOT / 'engine-core/src/main/java/com/shnewbs/deepprimal/core/WatershedOutletTopology.java'
TEST = ROOT / 'tools/tests/V8ConservedOutletLedgerChecks.java'
CLASS = 'com.shnewbs.deepprimal.core.V8ConservedOutletLedgerChecks'

def rejected_by_fixture(result):
    """Only a compiled mutant reaching a fixture assertion is a caught bug."""
    test = result['test']
    return (result['compile']['returncode'] == 0 and test is not None
            and test['returncode'] == 1
            and re.search(
                r'^(?:Exception in thread "[^\"]+" |Caused by: )java\.lang\.AssertionError(?::|$)',
                test['stderr'], re.MULTILINE) is not None
            and '\tat ' + CLASS + '.' in test['stderr'])

def run(argv, **kwargs):
    result = subprocess.run(argv, text=True, capture_output=True, check=False, **kwargs)
    return {'command': argv, 'returncode': result.returncode,
            'stdout': result.stdout, 'stderr': result.stderr}

def one(temp, source, name):
    directory = temp / name
    directory.mkdir()
    mutated = directory / 'V8ConservedOutletLedger.java'
    mutated.write_text(source, encoding='utf-8')
    compiled = run([os.environ.get('JAVAC', 'javac'), '--release', '21', '-Xlint:all', '-Werror', '-d', str(directory),
                    str(TOPOLOGY), str(mutated), str(TEST)])
    if compiled['returncode'] != 0:
        return {'compile': compiled, 'test': None}
    return {'compile': compiled, 'test': run([os.environ.get('JAVA', 'java'), '-ea', '-Xverify:all', '-cp', str(directory), CLASS])}

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--output', type=pathlib.Path, required=True)
    args = parser.parse_args()
    output = args.output.resolve()
    output.mkdir(parents=True, exist_ok=True)
    contents = SOURCE.read_text(encoding='utf-8')
    patterns = [
        ('duplicate-replay', 'if (last.equals(t)) { replayed++; continue; }',
         'if (last.equals(t)) { replayed++; /* broken: charge again */ }'),
        ('mint-water', 'Balance debit = source.debit(t), credit = destination.credit(t);',
         'Balance debit = source, credit = destination.credit(t);'),
        ('lose-receipt', 'receipts.put(t.source(), t);',
         '/* broken: do not persist receipt */'),
    ]
    versions = {'javac': run([os.environ.get('JAVAC', 'javac'), '-version']),
                'java': run([os.environ.get('JAVA', 'java'), '-version'])}
    for name, old, _ in patterns:
        if contents.count(old) != 1:
            raise SystemExit(f'mutation {name} source anchor not unique')
    with tempfile.TemporaryDirectory(prefix='deepprimal-ledger-') as folder:
        temp = pathlib.Path(folder)
        tests = {'positive': one(temp, contents, 'positive')}
        for name, old, new in patterns:
            tests[name] = one(temp, contents.replace(old, new), name)
    passed = tests['positive']['compile']['returncode'] == 0 and (
        tests['positive']['test'] is not None and
        tests['positive']['test']['returncode'] == 0 and
        'V8_OUTLET_LEDGER_PASS' in tests['positive']['test']['stdout'])
    rejected = {k: rejected_by_fixture(v)
                for k, v in tests.items() if k != 'positive'}
    manifest = {str(p.relative_to(ROOT)): hashlib.sha256(p.read_bytes()).hexdigest()
                for p in [SOURCE, TOPOLOGY, TEST]}
    receipt = {'passed': passed, 'broken_controls_rejected': rejected,
               'source_sha256': manifest, 'toolchains': versions, 'runs': tests,
               'scope': 'isolated Java21 strict source execution, not native Minecraft or water persistence'}
    (output / 'ledger-checks.json').write_text(json.dumps(receipt, indent=2) + '\n')
    if not passed or not all(rejected.values()):
        raise SystemExit('V8_OUTLET_LEDGER_FAIL')
    print('V8_OUTLET_LEDGER_SUITE_PASS positive=1 broken=3 ' +
          tests['positive']['test']['stdout'].strip())

if __name__ == '__main__':
    main()

