"""Reproduce against a checkout matching inputs.json; never edits that checkout."""
import hashlib
import json
from pathlib import Path
import subprocess
import sys

repo, jdk, output = map(Path, sys.argv[1:])
here = Path(__file__).resolve().parent
output.mkdir(parents=True, exist_ok=False)
inputs = json.loads((here / 'inputs.json').read_text())
for entry in inputs:
    data = (repo / entry['path']).read_bytes()
    actual = hashlib.sha1(b'blob ' + str(len(data)).encode() + b'\0' + data).hexdigest()
    if actual != entry['sha']:
        raise RuntimeError('Pinned input mismatch: ' + entry['path'])

target = 'engine-core/src/main/java/com/shnewbs/deepprimal/core/ByteLru.java'
test = here / 'ByteLruOverflowChecks.java'
for mode in ('baseline', 'candidate'):
    classes = output / mode
    classes.mkdir()
    sources = ([repo / target] if mode == 'baseline' else
               [repo / x['path'] for x in inputs if x['path'] != target] + [here / 'ByteLru.java'])
    subprocess.run([str(jdk / 'bin/javac'), '-g', '-d', str(classes)] +
                   list(map(str, sources + [test])), check=True)
    with (output / (mode + '.log')).open('w') as log:
        result = subprocess.run([str(jdk / 'bin/java'), '-Xverify:all', '-Xmx1G',
                                 '-cp', str(classes), 'ByteLruOverflowChecks'],
                                stdout=log, stderr=subprocess.STDOUT)
    transcript = (output / (mode + '.log')).read_text()
    expected_marker = ('overflow bypass: bytes=-9223372036854775808 size=2'
                       if mode == 'baseline' else 'BYTE_LRU_OVERFLOW_PASS')
    if (result.returncode == 0) != (mode == 'candidate') or expected_marker not in transcript:
        raise RuntimeError('Unexpected regression result: ' + mode)
with (output / 'core.log').open('w') as log:
    subprocess.run([str(jdk / 'bin/java'), '-Xverify:all', '-Xmx1G', '-cp',
                    str(output / 'candidate'), 'com.shnewbs.deepprimal.core.CoreChecks'],
                   stdout=log, stderr=subprocess.STDOUT, check=True)
print('BYTE_LRU_REPRODUCTION_PASS baselineRejected=true candidateAndCore=true')
