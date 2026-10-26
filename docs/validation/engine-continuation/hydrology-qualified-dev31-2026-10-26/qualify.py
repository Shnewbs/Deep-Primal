from pathlib import Path
import json,time,subprocess,sys,hashlib
r=Path(__file__).resolve().parent;f=r/'final';deadline=time.monotonic()+1000
while not (r/'build-result.json').exists():
    if time.monotonic()>deadline:raise RuntimeError('Build completion timed out')
    time.sleep(2)
if json.loads((r/'build-result.json').read_text())['exit']!=0:raise RuntimeError('Full build failed')
records=[]
def run(name,args):
    t=time.time()
    with (r/(name+'.log')).open('w') as log:p=subprocess.run(args,stdout=log,stderr=subprocess.STDOUT,timeout=1800)
    records.append({'gate':name,'exit':p.returncode,'seconds':time.time()-t});(r/'qualification.json').write_text(json.dumps(records,indent=2))
    if p.returncode:raise RuntimeError('Qualification failed: '+name)
run('package',['python3',str(f/'validate_package.py')])
run('native-profiles',['python3',str(f/'native_smoke.py')])
run('native-aquifer',['python3',str(f/'aquifer_native_smoke.py')])
jar=json.loads((f/'evidence/package.json').read_text())['artifact']
run('native-precipitation-fresh',['python3',str(f/'precipitation-native/run_native.py'),'fresh',jar])
run('native-precipitation-reopen',['python3',str(f/'precipitation-native/run_native.py'),'reopen',jar])
run('native-receiver',['python3',str(f/'receiver-native/run_native.py'),'candidate',jar])
run('post-native-package',['python3',str(f/'validate_package.py')])
print('QUALIFICATION_PASS',flush=True)
