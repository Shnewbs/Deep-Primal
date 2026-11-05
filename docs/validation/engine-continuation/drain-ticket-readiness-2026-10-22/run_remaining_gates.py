"""Independent follow-up after the recorded filling failure; never imply a passing matrix."""
import json
from pathlib import Path
import subprocess
import sys
ROOT=Path(__file__).resolve().parent
results=[]
for name,mode,suite in [('remaining-pressure','pressure','pressure'),('remaining-queue','queue','queue'),('basin-ticket-a','basin-fixed','closed')]:
 print('START',name,flush=True)
 subprocess.run([sys.executable,str(ROOT/'run_native.py'),name,mode,'--suite',suite,'--no-trace'],cwd=ROOT,check=True)
 record=json.loads((ROOT/(name+'.json')).read_text());results.append(record)
 (ROOT/'remaining-gates.json').write_text(json.dumps({'known_failed_gate':'matrix-filling','results':results},indent=2)+'\n')
 if not record['explicit_pass'] or not record['normal_save'] or record['crash_reports']:raise SystemExit(1)
name='basin-ticket-b'
subprocess.run([sys.executable,str(ROOT/'run_native.py'),name,'basin-fixed','--no-trace'],cwd=ROOT,check=True)
record=json.loads((ROOT/(name+'.json')).read_text());results.append(record)
(ROOT/'remaining-gates.json').write_text(json.dumps({'known_failed_gate':'matrix-filling','results':results},indent=2)+'\n')
if not record['explicit_pass'] or not record['normal_save'] or record['crash_reports']:raise SystemExit(1)
