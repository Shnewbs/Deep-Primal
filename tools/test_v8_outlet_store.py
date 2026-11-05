#!/usr/bin/env python3
"""Dev85 isolated Java21 world-bound filesystem/CAS regression; not native Minecraft."""
import argparse
import hashlib
import json
import os
from pathlib import Path
import re
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / 'engine-core/src/main/java/com/shnewbs/deepprimal/core/V8OutletLedgerStore.java'
TEST = ROOT / 'tools/tests/V8OutletLedgerStoreChecks.java'
LEDGER = ROOT / 'engine-core/src/main/java/com/shnewbs/deepprimal/core/V8ConservedOutletLedger.java'
TOPOLOGY = ROOT / 'engine-core/src/main/java/com/shnewbs/deepprimal/core/WatershedOutletTopology.java'
CLASS = 'com.shnewbs.deepprimal.core.V8OutletLedgerStoreChecks'


def execute(command):
    proc = subprocess.run(command, text=True, capture_output=True, check=False)
    return {'command': command, 'returncode': proc.returncode,
            'stdout': proc.stdout, 'stderr': proc.stderr}


def case(root, name, source):
    work = root / name
    work.mkdir()
    classes = work / 'classes'
    classes.mkdir()
    modified = work / 'V8OutletLedgerStore.java'
    modified.write_text(source, encoding='utf8')
    compiler = os.getenv('JAVAC', 'javac')
    runtime = os.getenv('JAVA', 'java')
    compiled = execute([compiler, '--release', '21', '-Xlint:all', '-Werror',
                        '-d', str(classes), str(TOPOLOGY), str(LEDGER),
                        str(modified), str(TEST)])
    tested = None
    if compiled['returncode'] == 0:
        tested = execute([runtime, '-ea', '-Xverify:all', '-cp', str(classes),
                          CLASS, str(work / 'world')])
    return {'compile': compiled, 'test': tested}


def caught_by_fixture(item):
    test = item['test']
    if item['compile']['returncode'] != 0 or test is None or test['returncode'] != 1:
        return False
    return (re.search(r'^(?:Exception in thread "[^"]+" |Caused by: )java\.lang\.AssertionError(?::|$)',
                      test['stderr'], flags=re.MULTILINE) is not None
            and '\tat ' + CLASS + '.' in test['stderr'])


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--output', type=Path, required=True)
    args = parser.parse_args()
    text = SOURCE.read_text(encoding='utf8')
    replacements = [
        ('no-generation-CAS', 'if (expectedGeneration != current.generation())',
         'if (false && expectedGeneration != current.generation())'),
        ('replay-as-new-generation', 'if (candidate.applied() == 0)',
         'if (false && candidate.applied() == 0)'),
        ('ignore-checksum', 'if (!MessageDigest.isEqual(hash, recordedHash))',
         'if (false && !MessageDigest.isEqual(hash, recordedHash))'),
        ('ignore-world-identity', 'if (!Objects.equals(image.binding(), expected))',
         'if (false && !Objects.equals(image.binding(), expected))'),
        ('ignore-unreconciled-pending', 'rejectPending(path);',
         '/* broken: ignore unacknowledged pending at open */'),
        ('ignore-pending-on-commit', 'rejectPending(file);',
         '/* broken: overwrite unacknowledged pending at commit */'),
        ('accept-symlinked-checkpoint', 'if (Files.isSymbolicLink(path)',
         'if (false && Files.isSymbolicLink(path)'),
        ('skip-durable-native-accounts',
         '&& nativeWitness.accounts().equals(pendingImage.snapshot().accounts()))',
         '&& true)'),
        ('skip-pending-conservation-receipts',
         'checkReconstructableTransition(committed.snapshot(), pendingImage.snapshot());',
         '/* broken: no conservation/replay verification of pending */'),
        ('skip-native-generation',
         'nativeWitness.generation() == pendingImage.generation()',
         'true'),
        ('discard-forensics',
         'Files.move(pendingPath, quarantine, StandardCopyOption.ATOMIC_MOVE);',
         'Files.delete(pendingPath);'),
        ('overwrite-forensic-quarantine',
         'if (Files.exists(quarantine, LinkOption.NOFOLLOW_LINKS))',
         'if (false && Files.exists(quarantine, LinkOption.NOFOLLOW_LINKS))'),
        ('ignore-native-no-pending-mismatch',
         'nativeWitness.generation() != committed.generation()\n          || !nativeWitness.accounts().equals(committed.snapshot().accounts())',
         'false'),
        ('allow-unsorted-accounts',
         'if (i > 0 && key <= previous)\n          throw new IOException("noncanonical V8 outlet account order");',
         'if (false && i > 0 && key <= previous)\n          throw new IOException("noncanonical V8 outlet account order");'),
    ]
    for name, before, _ in replacements:
        if text.count(before) != 1:
            raise RuntimeError('non-unique mutation anchor: ' + name)
    with tempfile.TemporaryDirectory(prefix='deepprimal-outlet-store-') as temp:
        base = Path(temp)
        positive = case(base, 'positive', text)
        negatives = {name: case(base, name, text.replace(before, after))
                     for name, before, after in replacements}
    success = (positive['compile']['returncode'] == 0
               and positive['test'] is not None
               and positive['test']['returncode'] == 0
               and 'V8_OUTLET_STORE_PASS assertions=181 generation=42' in positive['test']['stdout'])
    controls = {name: caught_by_fixture(item) for name, item in negatives.items()}
    manifest = {str(p.relative_to(ROOT)): hashlib.sha256(p.read_bytes()).hexdigest()
                for p in [SOURCE, TEST, LEDGER, TOPOLOGY]}
    result = {'toolchain': {'java': execute([os.getenv('JAVA', 'java'), '-version']),
                            'javac': execute([os.getenv('JAVAC', 'javac'), '-version'])},
              'source_sha256': manifest,
              'positive': positive, 'mutants': negatives,
              'positive_pass': success, 'mutants_rejected_by_fixture': controls,
              'scope': 'Java21 filesystem API and dependency-free source; no Minecraft/NeoForge/Windows/GPU or full Gradle'}
    args.output.mkdir(parents=True, exist_ok=True)
    (args.output / 'store-checks.json').write_text(json.dumps(result, indent=2) + '\n')
    print('V8_OUTLET_STORE_RUNNER_PASS positive=' + str(success) +
          ' mutations=' + str(controls))
    if not success or not all(controls.values()):
        raise SystemExit(1)


if __name__ == '__main__':
    main()
