#!/usr/bin/env python3
"""Source-only Java21 receiver route tests; no native Minecraft compilation."""
import argparse
import json
import os
import re
from pathlib import Path
import subprocess
import tempfile
ROOT=Path(__file__).resolve().parents[1]
ROUTES=ROOT/'engine-core/src/main/java/com/shnewbs/deepprimal/core/WatershedInteriorPortRoutes.java'
TOPOLOGY=ROOT/'engine-core/src/main/java/com/shnewbs/deepprimal/core/WatershedOutletTopology.java'
CHECKS=ROOT/'tools/tests/WatershedInteriorPortRoutesChecks.java'
def run(source,out):
    out.mkdir(parents=True,exist_ok=True)
    cmd=[os.environ.get('JAVAC','javac'),'--release','21','-Xlint:all','-Werror',
         '-d',str(out),str(source),str(TOPOLOGY),str(CHECKS)]
    c=subprocess.run(cmd,capture_output=True,text=True)
    if c.returncode:
        raise RuntimeError(f"{source}: Java compilation failed (exit {c.returncode}):\n"
                           + c.stdout + c.stderr)
    p=subprocess.run([os.environ.get('JAVA','java'),'-ea','-Xverify:all',
                      '-cp',str(out),'WatershedInteriorPortRoutesChecks'],
                     capture_output=True,text=True)
    log = p.stdout + p.stderr
    if p.returncode == 0:
        if "V8_INTERIOR_ROUTES_PASS" not in p.stdout:
            raise RuntimeError(f"{source}: Java checks exited without success marker:\n" + log)
        return True, log
    # A compiler/launcher/VM failure is not evidence that a mutant was caught.
    # Require an uncaught test assertion with this fixture in the stack trace.
    assertion = re.search(
        r'^(?:Exception in thread "[^"]+" |Caused by: )java\.lang\.AssertionError(?::|$)',
        p.stderr, re.MULTILINE)
    if (p.returncode != 1 or not assertion
            or "\tat WatershedInteriorPortRoutesChecks." not in p.stderr):
        raise RuntimeError(f"{source}: Java checks failed without a fixture assertion "
                           f"(exit {p.returncode}):\n" + log)
    return False, log

def main():
    ap=argparse.ArgumentParser();ap.add_argument('--output',type=Path)
    opts=ap.parse_args()
    source=ROUTES.read_text()
    mutations=[('allow_receiver_jumps','dx > 1 || dz > 1','false'),
               ('forget_interior_crest','Math.max(ground[i], crest[p])',
                'ground[i]'),
               ('admit_unobserved_cycle','if (at >= 0 && pending[at] == 1)',
                'if (false)')]
    outcomes=[]
    with tempfile.TemporaryDirectory(prefix='dp-v8-interior-routes-') as t:
        tmp=Path(t)
        for i in range(2):
            ok,log=run(ROUTES,tmp/('pass'+str(i)))
            if not ok: raise SystemExit('positive failed: '+log)
            outcomes.append({'run':f'positive{i}','passed':True,'log':log.strip()})
        for name,needle,replacement in mutations:
            if source.count(needle)!=1: raise SystemExit('mutation not unique: '+name)
            target=tmp/name/'WatershedInteriorPortRoutes.java'
            target.parent.mkdir(parents=True); target.write_text(source.replace(needle,replacement))
            ok,log=run(target,tmp/(name+'-classes'))
            if ok: raise SystemExit('bad control accepted: '+name)
            outcomes.append({'run':name,'rejected':True,'diagnostic':log[-750:]})
    if opts.output:
        opts.output.mkdir(parents=True,exist_ok=True)
        (opts.output/'results.json').write_text(json.dumps(outcomes,indent=2)+'\n')
    print('V8_INTERIOR_ROUTES_RUNNER_PASS positive=2 broken_rejected=3')
if __name__=='__main__':main()
