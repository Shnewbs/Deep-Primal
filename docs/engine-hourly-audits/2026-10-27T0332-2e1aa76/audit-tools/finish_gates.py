from pathlib import Path
import json,subprocess,time,hashlib,zipfile,traceback,os
os.environ.update(TMPDIR="/dev/shm/dp-audit-2e1aa76-0332/tmp",JDK_JAVA_OPTIONS="-Xmx768m -Djava.io.tmpdir=/dev/shm/dp-audit-2e1aa76-0332/tmp")
r=Path(__file__).resolve().parent;e=r/'evidence';result={'pass':False,'source_sha':'2e1aa76da93518fe87ba1bc07e5dfb5b3cf4564e'}
def run(cmd,log):
 with (e/log).open('w') as f:rc=subprocess.run(cmd,stdout=f,stderr=subprocess.STDOUT).returncode
 if rc:raise RuntimeError(log+' exit '+str(rc))
 print('GATE_PASS',log,flush=True)
try:
 while not (e/'native-permit.json').exists():time.sleep(2)
 while not all((e/n).exists() for n in ['build-result.json','independent-core-result.json']):time.sleep(2)
 assert json.loads((e/'build-result.json').read_text())['pass'] and json.loads((e/'independent-core-result.json').read_text())['pass']
 for s in ['validate_package.py','packaged_regressions.py','source-excerpts.py']:run(['python3',str(r/s)],s+'.log')
 a=json.loads((e/'package.json').read_text());jar=a['artifact'];core=r/'packaged-regressions/core.jar';jdk=r/'toolchain/jdk-25.0.4.1+1/bin/java';cp=str(core)+':'+str(r/'independent-core')
 for name in ['SplitAdmissionChecks','lod.LodPartBoundsChecks']:run([str(jdk),'-Xmx768m','-Xverify:all','-cp',cp,'com.shnewbs.deepprimal.core.'+name],name+'.txt')
 for mode in ['fresh','reopen']:run(['python3',str(r/'precipitation-native/run_native.py'),mode,jar],'precipitation-'+mode+'-harness.txt')
 run(['python3',str(r/'native_smoke.py')],'native-harness.txt')
 run(['python3',str(r/'aquifer_native_smoke.py')],'aquifer-harness.txt')
 run(['python3',str(r/'receiver-native/run_native.py'),'candidate',jar],'receiver-harness.txt')
 for d in ['precipitation-native','receiver-native']:
  for f in (r/d).iterdir():
   if f.is_file() and f.suffix in ['.json','.txt','.log']:(e/(d+'-'+f.name)).write_bytes(f.read_bytes())
 assert hashlib.sha256(Path(jar).read_bytes()).hexdigest()==hashlib.sha256(Path(a['producer']).read_bytes()).hexdigest()==a['sha256']
 result.update(pass_all_native=True,native_sessions=10,producer_export_unchanged=True,sha256=a['sha256']);result['pass']=True
except BaseException as ex:result.update(error=str(ex),traceback=traceback.format_exc())
(e/'qualification-result.json').write_text(json.dumps(result,indent=2));print(json.dumps(result),flush=True)

