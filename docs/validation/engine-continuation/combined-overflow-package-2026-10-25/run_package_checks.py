"""Direct core-only qualification; never creates an installable Minecraft mod.

Usage: python run_package_checks.py PINNED_CHECKOUT JDK25_ROOT NEW_OUTPUT
Place the two staged runtime sources and two regressions beside this script.
"""
import hashlib
import json
from pathlib import Path
import subprocess
import sys
import zipfile

repo, jdk, out = map(Path, sys.argv[1:])
here = Path(__file__).resolve().parent
out.mkdir(parents=True, exist_ok=False)
entries = json.loads((here / 'inputs.json').read_text())
for entry in entries:
    data = (repo / entry['path']).read_bytes()
    actual = hashlib.sha1(b'blob ' + str(len(data)).encode() + b'\0' + data).hexdigest()
    if actual != entry['sha']:
        raise RuntimeError('Pinned input mismatch: ' + entry['path'])
product = out / 'product'
fixtures = out / 'fixtures'
product.mkdir(); fixtures.mkdir()
overlays = {'ByteLru.java', 'DensityGrid.java'}
sources = [here / Path(e['path']).name if Path(e['path']).name in overlays else repo / e['path']
           for e in entries if e['path'].startswith('engine-core/')]
subprocess.run([str(jdk / 'bin/javac'), '-g', '-d', str(product)] + list(map(str, sources)), check=True)
jar = out / 'candidate-core-only.jar'
with zipfile.ZipFile(jar, 'w', zipfile.ZIP_DEFLATED) as archive:
    for path in sorted(product.rglob('*.class')):
        archive.write(path, path.relative_to(product).as_posix())
with zipfile.ZipFile(jar) as archive:
    if archive.testzip() is not None: raise RuntimeError('Core archive CRC failure')
    classes = list(product.rglob('*.class'))
    if len(archive.namelist()) != len(classes): raise RuntimeError('Archive entry mismatch')
    for path in classes:
        if archive.read(path.relative_to(product).as_posix()) != path.read_bytes():
            raise RuntimeError('Packaged class mismatch')
tests = [repo / e['path'] for e in entries if e['path'].startswith('src/coreChecks/')]
tests += [here / name for name in ('ByteLruOverflowChecks.java', 'DensityGridFiniteChecks.java', 'PackagedOverflowChecks.java')]
subprocess.run([str(jdk / 'bin/javac'), '-g', '-cp', str(jar), '-d', str(fixtures)] + list(map(str, tests)), check=True)
fixture_entries = {p.relative_to(fixtures).as_posix() for p in fixtures.rglob('*.class')}
with zipfile.ZipFile(jar) as archive:
    if fixture_entries.intersection(archive.namelist()): raise RuntimeError('Fixture leaked into core archive')
for name, main, args, marker in (
    ('regression', 'PackagedOverflowChecks', [str(jar)], 'COMBINED_OVERFLOW_PACKAGE_PASS'),
    ('core', 'com.shnewbs.deepprimal.core.CoreChecks', [], 'PASS: 200000 differential surface cases')):
    with (out / (name + '.log')).open('w') as log:
        result = subprocess.run([str(jdk / 'bin/java'), '-Xmx1G', '-Xverify:all', '-cp', str(fixtures) + ':' + str(jar), main] + args, stdout=log, stderr=subprocess.STDOUT)
    if result.returncode or marker not in (out / (name + '.log')).read_text():
        raise RuntimeError('Failed ' + name + '; inspect retained log')
receipt = {'pass': True, 'scope': 'direct core-only JAR, not Minecraft/full Gradle',
           'inputs_verified': len(entries), 'product_classes': len(classes),
           'fixture_classes_excluded': len(fixture_entries), 'crc': True,
           'packaged_classes_match': True, 'loaded_candidate_locations_checked': True,
           'regression_exit': 0, 'core_exit': 0,
           'jar_bytes': jar.stat().st_size, 'jar_sha256': hashlib.sha256(jar.read_bytes()).hexdigest()}
(out / 'result.json').write_text(json.dumps(receipt, indent=2) + '\n')
print(json.dumps(receipt), flush=True)
