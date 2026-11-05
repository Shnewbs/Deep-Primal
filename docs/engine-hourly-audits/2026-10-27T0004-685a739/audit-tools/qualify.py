from pathlib import Path
import subprocess,json,time,traceback,hashlib
r=Path(__file__).resolve().parent;e=r/'evidence';result={'pass':False,'source_sha':'685a7390a8371c9b423594888a5a8e2aad744271'}
try:
 while not (e/'build-result.json').exists():time.sleep(2)
 assert json.loads((e/'build-result.json').read_text())['pass'],'Full build failed'
 for name in ['run_checks.py','finish_gates.py','extra_gates.py','post_native_verify.py']:
  with (e/(name+'-supervisor.txt')).open('w') as f:rc=subprocess.run(['python3',str(r/name)],stdout=f,stderr=subprocess.STDOUT).returncode
  assert rc==0,(name,rc)
  if name=='finish_gates.py':assert json.loads((e/'qualification-result.json').read_text())['pass']
  print('QUALIFIED_GATE',name,flush=True)
 result['pass']=True
except BaseException as ex:result.update(error=str(ex),traceback=traceback.format_exc())
(e/'all-gates.json').write_text(json.dumps(result,indent=2));print(json.dumps(result),flush=True)

