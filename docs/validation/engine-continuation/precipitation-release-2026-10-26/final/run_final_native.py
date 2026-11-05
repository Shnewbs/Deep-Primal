"""Sequential isolated packaged qualification; never use owner worlds."""
from pathlib import Path
import json, subprocess, zipfile

r = Path(__file__).resolve().parent
def run(command, log):
    with (r / log).open('w') as output:
        result = subprocess.run(command, stdout=output, stderr=subprocess.STDOUT)
    if result.returncode:
        raise RuntimeError(f'{log}: exit {result.returncode}')
    print('QUALIFICATION_PASS', log, flush=True)

if (r / 'build-exit.txt').read_text().strip() != '0':
    raise RuntimeError('Full build must complete first')
run(['python3', str(r/'validate_package.py')], 'package.log')
a = json.loads((r/'evidence/package.json').read_text())
jar = a['artifact']
with zipfile.ZipFile(jar) as z:
    names = [n for n in z.namelist() if 'DeepPrimal-Core' in n and n.endswith('.jar')]
    if len(names) != 1: raise RuntimeError('Expected one core archive')
    (r/'packaged-core.jar').write_bytes(z.read(names[0]))
jdk = Path('/workspace/scratch/a22609fabda0/toolchain/jdk-25.0.4.1+1/bin')
cp = str(r/'packaged-core.jar')+':'+str(r/'independent-core')
run([str(jdk/'javac'), '-g', '-cp', cp, '-d', str(r/'independent-core'), str(r/'PackagedAdmissionChecks.java')], 'packaged-compile.log')
run([str(jdk/'java'), '-Xmx768m', '-Xverify:all', '-cp', cp, 'com.shnewbs.deepprimal.core.PackagedAdmissionChecks'], 'packaged-regressions.log')
for mode in ['fresh', 'reopen']:
    run(['python3', str(r/'precipitation-native/run_native.py'), mode, jar], 'precipitation-native/'+mode+'-harness.log')
run(['python3', str(r/'native_smoke.py')], 'native-harness.log')
run(['python3', str(r/'aquifer_native_smoke.py')], 'aquifer-native-harness.log')
run(['python3', str(r/'receiver-native/run_native.py'), 'candidate', jar], 'receiver-native/harness.log')
run(['python3', str(r/'validate_package.py')], 'post-native-package.log')
(r/'qualification-result.json').write_text(json.dumps({'pass': True, 'native_sessions': 10, 'jar_sha256':a['sha256'], 'windows_execution':False, 'gpu_visuals':False, 'controlled_performance':False}, indent=2))
print('FINAL_NATIVE_QUALIFICATION_PASS sessions=10', flush=True)
