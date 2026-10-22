"""Sequential safety gates; retain known basin failure instead of claiming acceptance."""
from pathlib import Path
import json
import subprocess
import sys

ROOT=Path(__file__).resolve().parent
cases=[('fifo-filling','filling','filling',None),
 ('fifo-fixed','fixed','closed',None),('fifo-edit','edit','closed',None),
 ('fifo-reload','reload','closed',None),('fifo-reload-edit','reload-edit','closed',None),
 ('fifo-unload','unload','closed',None),('fifo-reopen','reopen','closed','run-fifo-fixed')]
cases += [('fifo-pressure-'+mode,mode,'long-pressure','run-fifo-pressure-idle' if mode=='reopen' else None)
    for mode in ['heads','fixed','edit','unload','idle','reopen']]
cases += [('fifo-short-pressure','pressure','pressure',None),('fifo-active-queue','queue','queue',None),
    ('fifo-drain-queue','queue','drain-queue',None),('fifo-physical','physical-reload','closed',None)]
records=[]
for name,mode,suite,reuse in cases:
    cmd=[sys.executable,str(ROOT/'run_native.py'),name,mode,'--suite',suite,'--no-trace']
    if reuse:cmd += ['--reuse',reuse]
    print('START',name,flush=True)
    subprocess.run(cmd,cwd=ROOT,check=True)
    record=json.loads((ROOT/(name+'.json')).read_text());records.append(record)
    (ROOT/'fifo-safety.json').write_text(json.dumps(records,indent=2)+'\n')
    if not record['explicit_pass'] or not record['normal_save'] or record['crash_reports']:
        print('STOP_ON_FAILED_GATE',name,flush=True);raise SystemExit(1)
