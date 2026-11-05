from pathlib import Path
import tempfile,shutil,subprocess,sys,json
N=Path(__file__).resolve().parent;records=[]
for opt in ['', '-O','-OO']:
 for change in ['none','source','class','jar']:
  with tempfile.TemporaryDirectory(prefix='producer-control-',dir=N) as tmp:
   p=Path(tmp)
   for name in ['run_native.py','producer-complete.json','StreamIndexNativeChecks.java','private-index-diagnostic.jar']:shutil.copyfile(N/name,p/name)
   shutil.copytree(N/'fixtures',p/'fixtures')
   target={'source':p/'StreamIndexNativeChecks.java','class':next((p/'fixtures').rglob('*.class')),'jar':p/'private-index-diagnostic.jar'}.get(change)
   if target:
    with target.open('ab') as f:f.write(b'changed')
   cmd=[sys.executable]+([opt] if opt else [])+[str(p/'run_native.py'),'preflight']
   r=subprocess.run(cmd,capture_output=True,text=True)
   expected={'source':'Fixture source changed','class':'Fixture compiled identity mismatch','jar':'Diagnostic producer archive mismatch'}
   if change=='none':
    if r.returncode or 'PRODUCER_IDENTITY_PASS' not in r.stdout:raise RuntimeError(r.stdout+r.stderr)
   elif r.returncode==0 or expected[change] not in r.stderr:raise RuntimeError('negative control not rejected')
   if (p/'world').exists():raise RuntimeError('preflight wrote world')
   records.append({'mode':opt or 'normal','mutation':change,'exit':r.returncode,'accepted':change=='none','no_world_write':True})
(N/'producer-guard-results.json').write_text(json.dumps(records,indent=2));print('PRODUCER_GUARD_PASS cases='+str(len(records)))
