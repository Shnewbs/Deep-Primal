import argparse
import json
from pathlib import Path
import subprocess
import sys

ROOT=Path(__file__).resolve().parent
results=[]
parser=argparse.ArgumentParser();parser.add_argument('--skip-physical',action='store_true');args=parser.parse_args()
cases=[('physical-reload-rerun','physical-reload','closed',None),
 ('matrix-fixed','fixed','closed',None),('matrix-edit','edit','closed',None),
 ('matrix-reload-edit','reload-edit','closed',None),
 ('matrix-unavailable','unload','closed',None),('matrix-reopen','reopen','closed','run-matrix-fixed')]
cases += [('matrix-pressure-'+mode,mode,'long-pressure','run-matrix-pressure-idle' if mode=='reopen' else None) for mode in ['heads','fixed','edit','unload','idle','reopen']]
cases += [('matrix-filling','filling','filling',None),('matrix-pressure-control','pressure','pressure',None),('matrix-queue','queue','queue',None),('basin-ticket-a','basin-fixed','closed',None)]
if args.skip_physical:cases=cases[1:]
for name,mode,suite,reuse in cases:
 command=[sys.executable,str(ROOT/'run_native.py'),name,mode,'--suite',suite,'--no-trace']
 if reuse:command+=['--reuse',reuse]
 print('START',name,flush=True)
 subprocess.run(command,cwd=ROOT,check=True)
 record=json.loads((ROOT/(name+'.json')).read_text());results.append(record)
 (ROOT/'safety-matrix.json').write_text(json.dumps(results,indent=2)+'\n')
 if not record['explicit_pass'] or not record['normal_save'] or record['crash_reports']:
  print('STOP_ON_FAILED_GATE',name,flush=True);raise SystemExit(1)
name='basin-ticket-b'
subprocess.run([sys.executable,str(ROOT/'run_native.py'),name,'basin-fixed','--no-trace'],cwd=ROOT,check=True)
record=json.loads((ROOT/(name+'.json')).read_text());results.append(record)
(ROOT/'safety-matrix.json').write_text(json.dumps(results,indent=2)+'\n')
if not record['explicit_pass'] or not record['normal_save'] or record['crash_reports']:raise SystemExit(1)
