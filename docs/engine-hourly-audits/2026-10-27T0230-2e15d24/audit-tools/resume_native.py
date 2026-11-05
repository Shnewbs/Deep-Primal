from pathlib import Path
import json,subprocess,hashlib,traceback
r=Path(__file__).resolve().parent;e=r/'evidence';a=json.loads((e/'package.json').read_text());result={'pass':False,'source_sha':a['source_sha']}
def run(cmd,log):
 with (e/log).open('w') as f:rc=subprocess.run(cmd,stdout=f,stderr=subprocess.STDOUT).returncode
 assert rc==0,(log,rc)
 print('GATE_PASS',log,flush=True)
try:
 for mode in ['fresh','reopen']:run(['python3',str(r/'precipitation-native/run_native.py'),mode,a['artifact']],'precipitation-'+mode+'-harness.txt')
 run(['python3',str(r/'native_smoke.py')],'native-harness.txt')
 run(['python3',str(r/'aquifer_native_smoke.py')],'aquifer-harness.txt')
 run(['python3',str(r/'receiver-native/run_native.py'),'candidate',a['artifact']],'receiver-harness.txt')
 for d in ['precipitation-native','receiver-native']:
  for f in (r/d).iterdir():
   if f.is_file() and f.suffix in ['.json','.txt','.log']:(e/(d+'-'+f.name)).write_bytes(f.read_bytes())
 assert hashlib.sha256(Path(a['artifact']).read_bytes()).hexdigest()==hashlib.sha256(Path(a['producer']).read_bytes()).hexdigest()==a['sha256']
 result.update(pass_all_native=True,native_sessions=10,producer_export_unchanged=True,sha256=a['sha256']);result['pass']=True
except BaseException as ex:result.update(error=str(ex),traceback=traceback.format_exc())
(e/'qualification-result.json').write_text(json.dumps(result,indent=2));print(json.dumps(result),flush=True)
if not result['pass']:raise SystemExit(1)
run(['python3',str(r/'post_native_verify.py')],'post-native-retry.txt')
(e/'all-gates.json').write_text(json.dumps({'pass':True,'source_sha':a['source_sha']}))
run(['python3',str(r/'final_identity.py')],'final-identity-retry.txt')
