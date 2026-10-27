from pathlib import Path
import json,subprocess,time,traceback
r=Path(__file__).resolve().parent;e=r/'evidence';result={'pass':False,'source_sha':'2e1aa76da93518fe87ba1bc07e5dfb5b3cf4564e'}
try:
 for name in ['run_build.py','release_completed_assembly.py','run_checks.py','finish_gates.py','extra_gates.py','post_native_verify.py','final_identity.py']:
  with (e/(name+'-supervisor.txt')).open('w') as f:rc=subprocess.run(['python3',str(r/name)],stdout=f,stderr=subprocess.STDOUT).returncode
  assert rc==0,(name,rc)
  if name=='finish_gates.py':assert json.loads((e/'qualification-result.json').read_text())['pass']
  print('QUALIFIED_GATE',name,flush=True)
  if name=='post_native_verify.py':result['pass']=True;(e/'all-gates.json').write_text(json.dumps(result))
except BaseException as ex:result.update(pass_result=False,error=str(ex),traceback=traceback.format_exc());result['pass']=False
(e/'all-gates.json').write_text(json.dumps(result,indent=2));print(json.dumps(result),flush=True)
