#!/usr/bin/env python3
"""Strict isolated Java21 V8 port-to-ledger admission checks; NOT Minecraft or native GPU."""
import argparse
import hashlib
import json
import os
from pathlib import Path
import re
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[1]
CORE = ROOT / 'engine-core/src/main/java/com/shnewbs/deepprimal/core'
ADMISSION = CORE / 'V8OutletAuthorityAdmission.java'
SOURCES = [CORE / p for p in ['WatershedOutletTopology.java',
    'V8ConservedOutletLedger.java', 'V8OutletLedgerStore.java']]
TEST = ROOT / 'tools/tests/V8OutletAuthorityAdmissionChecks.java'
CLASS = 'com.shnewbs.deepprimal.core.V8OutletAuthorityAdmissionChecks'
MUTATIONS = [
    ('accept-unobserved-frontier', 'if (port.unknownFrontier())',
     'if (false && port.unknownFrontier())'),
    ('mint-runoff', 'if (port.runoffQuanta() != 0)',
     'if (false && port.runoffQuanta() != 0)'),
    ('accept-wrong-marine-roots', 'if (!roots.equals(observedAnchors))',
     'if (false && !roots.equals(observedAnchors))'),
    ('accept-phantom-account', 'if (!funded.keySet().equals(keys))',
     'if (false && !funded.keySet().equals(keys))'),
    ('ignore-directed-receiver-witness', 'if (!approved.equals(routed))',
     'if (false && !approved.equals(routed))'),
    ('overflow-total-water', 'water = Math.addExact(water, balance.water());',
     'water += balance.water();'),
]


def run(args):
    result = subprocess.run(args, text=True, capture_output=True, check=False)
    return {'returncode': result.returncode, 'stdout': result.stdout,
            'stderr': result.stderr, 'command': list(map(str, args))}


def single(work, text):
    work.mkdir()
    java = work / 'V8OutletAuthorityAdmission.java'
    java.write_text(text, encoding='utf8')
    classes = work / 'classes'
    classes.mkdir()
    compile_result = run([os.getenv('JAVAC', 'javac'), '--release', '21',
        '-Xlint:all', '-Werror', '-d', str(classes),
        *map(str, SOURCES), str(java), str(TEST)])
    executed = None
    if compile_result['returncode'] == 0:
        executed = run([os.getenv('JAVA', 'java'), '-ea', '-Xverify:all',
            '-cp', str(classes), CLASS, str(work / 'journal')])
    return {'compile': compile_result, 'execution': executed}


def killed_by_test(c):
    t = c['execution']
    return (c['compile']['returncode'] == 0 and t is not None
        and t['returncode'] == 1
        and re.search(r'^(?:Exception in thread "[^"]+" |Caused by: )java\.lang\.AssertionError(?::|$)',
                      t['stderr'], re.MULTILINE) is not None
        and '\tat ' + CLASS + '.' in t['stderr'])


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--output', required=True, type=Path)
    args = parser.parse_args()
    text = ADMISSION.read_text(encoding='utf8')
    for name, token, _ in MUTATIONS:
        if text.count(token) != 1:
            raise RuntimeError('mutant anchor changed: ' + name)
    with tempfile.TemporaryDirectory(prefix='deepprimal-authority-') as temp:
        scratch = Path(temp)
        positive = single(scratch / 'positive', text)
        mutants = {name: single(scratch / name, text.replace(token, replacement))
                   for name, token, replacement in MUTATIONS}
    passed = (positive['compile']['returncode'] == 0
        and positive['execution'] is not None
        and positive['execution']['returncode'] == 0
        and 'V8_OUTLET_AUTHORITY_PASS assertions=8220 cases=2048' in positive['execution']['stdout'])
    killed = {name: killed_by_test(case) for name, case in mutants.items()}
    manifest = {str(file.relative_to(ROOT)): hashlib.sha256(file.read_bytes()).hexdigest()
                for file in [ADMISSION, *SOURCES, TEST, Path(__file__)]}
    receipt = {'toolchain': {'java': run([os.getenv('JAVA','java'), '-version']),
                             'javac': run([os.getenv('JAVAC','javac'), '-version'])},
               'manifest': manifest, 'positive': positive,
               'mutants': mutants, 'positive_pass': passed,
               'mutants_rejected': killed,
               'scope': 'Java21 source-level component, API and local-filesystem tests; no Java25 full build, NeoForge, native terrain, water-cell, GPU or crash fault injection'}
    args.output.mkdir(parents=True, exist_ok=True)
    (args.output/'checks.json').write_text(json.dumps(receipt, indent=2)+'\n',encoding='utf8')
    print('V8_OUTLET_AUTHORITY_RUNNER_PASS positive=' + str(passed)
          + ' mutants=' + str(killed))
    if not passed or not all(killed.values()):
        raise SystemExit(1)


if __name__ == '__main__':
    main()
