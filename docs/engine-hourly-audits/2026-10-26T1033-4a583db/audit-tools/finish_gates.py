from pathlib import Path
import time,json,subprocess,hashlib,traceback,os
r=Path(__file__).resolve().parent;e=r/'evidence';result={'pass':False,'source_sha':'4a583dbcdec4df4be1ac7572c384813986067f2a'}
try:
 while not all((e/n).exists() for n in ['build-result.json','independent-core-result.json']):time.sleep(2)
 for n in ['build-result.json','independent-core-result.json']:assert json.loads((e/n).read_text())['pass'],n
 for script in ['validate_package.py','packaged_regressions.py','source-excerpts.py']:
  with (e/(script+'.log')).open('w') as f:subprocess.run(['python3',str(r/script)],stdout=f,stderr=subprocess.STDOUT,check=True)
 a=json.loads((e/'package.json').read_text());assert hashlib.sha256(Path(a['producer']).read_bytes()).hexdigest()==a['sha256']
 with (e/'precipitation-harness.txt').open('w') as f:rc=subprocess.run(['python3',str(r/'native-tools/run_native.py'),'fresh',a['artifact']],stdout=f,stderr=subprocess.STDOUT).returncode
 log=(r/'precipitation-native/fresh-native.log').read_text(errors='replace');result.update(native_harness_exit=rc,packaged_native_pass=rc==0,precipitation_capacity_crash_reproduced='Split transfer exceeds constituent capacity' in log,gradle_zero_despite_native_failure='BUILD SUCCESSFUL' in log and rc!=0)
 assert hashlib.sha256(Path(a['artifact']).read_bytes()).hexdigest()==hashlib.sha256(Path(a['producer']).read_bytes()).hexdigest()==a['sha256'];result['export_and_producer_unchanged_after_native']=True
 result['pass']=rc==0
except BaseException as ex:result.update(error=str(ex),traceback=traceback.format_exc())
(e/'essential-gate-result.json').write_text(json.dumps(result,indent=2));print(json.dumps(result),flush=True)
