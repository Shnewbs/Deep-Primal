"""Isolated reproduction against the recorded main snapshot; never edits the supplied checkout."""
from pathlib import Path
import hashlib, json, subprocess, sys, tempfile

repo, jdk, output = map(Path, sys.argv[1:4])
here = Path(__file__).resolve().parent
output.mkdir(parents=True, exist_ok=True)
inputs = json.loads((here / 'inputs.json').read_text())
for entry in inputs:
    data = (repo / entry['path']).read_bytes()
    digest = hashlib.sha1(b'blob ' + str(len(data)).encode() + b'\0' + data).hexdigest()
    if digest != entry['sha']:
        raise RuntimeError('Pinned source mismatch: ' + entry['path'])
grid = 'engine-core/src/main/java/com/shnewbs/deepprimal/core/density/DensityGrid.java'
with tempfile.TemporaryDirectory() as temporary:
    work = Path(temporary)
    sources = [str(repo / e['path']) for e in inputs]
    regression = str(here / 'DensityGridFiniteChecks.java')
    def compile_to(name, files):
        dest = work / name
        subprocess.run([str(jdk / 'bin/javac'), '-g', '-d', str(dest)] + files, check=True, timeout=120)
        return dest
    baseline = compile_to('baseline', sources + [regression])
    with (output / 'baseline.log').open('w') as log:
        result = subprocess.run([str(jdk / 'bin/java'), '-cp', str(baseline), 'DensityGridFiniteChecks'], stdout=log, stderr=subprocess.STDOUT, timeout=60)
    assert result.returncode != 0 and 'nonfinite axis=' in (output / 'baseline.log').read_text()
    candidate = compile_to('candidate', [s for s in sources if s != str(repo / grid)] + [str(here / 'DensityGrid.java'), regression])
    with (output / 'candidate.log').open('w') as log:
        for name in ['DensityGridFiniteChecks', 'com.shnewbs.deepprimal.core.CoreChecks']:
            subprocess.run([str(jdk / 'bin/java'), '-Xverify:all', '-cp', str(candidate), name], stdout=log, stderr=subprocess.STDOUT, check=True, timeout=900)
print('STAGED_DENSITY_GRID_CHECKS_PASS; full Gradle/native/package gates still required')
