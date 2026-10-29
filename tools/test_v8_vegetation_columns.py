#!/usr/bin/env python3
"""Portable V8 vegetation component tests with clearly labeled API doubles.
No Java25/full Gradle/Minecraft/native GPU claim. Negative controls must compile
and fail by AssertionError, not merely by a compiler or classpath failure.
"""
import hashlib
import json
from pathlib import Path
import re
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / 'src/main/java/com/shnewbs/deepprimal/engine/V8VegetationColumnPlan.java'
TEST = ROOT / 'tools/tests/V8VegetationColumnPlanChecks.java'
STUBS = list((ROOT / 'tools/tests/v8_vegetation_stubs').rglob('*.java'))
CLASS = 'com.shnewbs.deepprimal.engine.V8VegetationColumnPlanChecks'

def rejected_by_fixture(result):
    """A JVM failure is not a caught mutation unless the fixture asserted."""
    return (result['exit'] == 1
            and re.search(
                r'^(?:Exception in thread "[^"]+" |Caused by: )java\.lang\.AssertionError(?::|$)',
                result['stderr'], re.MULTILINE) is not None
            and '\tat ' + CLASS + '.' in result['stderr'])

def run(cmd, cwd=None, timeout=45):
    completed = subprocess.run(cmd, cwd=cwd, text=True, stdout=subprocess.PIPE,
                               stderr=subprocess.PIPE, timeout=timeout)
    return {'exit': completed.returncode, 'stdout': completed.stdout,
            'stderr': completed.stderr, 'cmd':cmd}

def compile_run(src, mutation=None):
    with tempfile.TemporaryDirectory(prefix='dev86-vegetation-') as d:
        work=Path(d)
        file=work/'V8VegetationColumnPlan.java'
        file.write_text(src)
        c=run(['javac','--release','21','-Xlint:all','-Werror','-d',str(work),
               str(file),str(TEST),*[str(s) for s in STUBS]],timeout=45)
        if c['exit']:
            raise RuntimeError('compiled variant failed '+str(mutation)+' '+c['stderr'])
        r=run(['java','-ea','-Xverify:all','-cp',str(work),
               CLASS],timeout=45)
        return r

def main():
    print(run(['java','-version'])['stderr'].splitlines()[0])
    src=SOURCE.read_text()
    a=compile_run(src)
    b=compile_run(src)
    if a['exit'] or b['exit'] or a['stdout']!=b['stdout'] or 'V8_VEGETATION_PLAN_PASS' not in a['stdout']:
        raise RuntimeError('positive replay failed '+str(a)+' '+str(b))
    negative={
      'signed_division': ('Math.floorDiv(wx, spacing)', 'wx / spacing'),
      'snow_loss': ('|| cover.snowPatch()', ''),
      'plant_count_copy': ('placed++;', 'placed += 2;'),
      'density_thinning': ('if (unit(savedSeed ^ THIN_SALT, gx, gz) >= policy.densityScale()) continue;', 'if (false) continue;'),
    }
    bad=[]
    for name,(before,after) in negative.items():
        if before not in src:raise RuntimeError('missing mutation site '+name)
        out=compile_run(src.replace(before,after,1),name)
        if not rejected_by_fixture(out):
            raise RuntimeError('negative not rejected via assertion '+name+': '+str(out))
        bad.append(name)
    receipt={'java':run(['java','-version'])['stderr'].splitlines()[0],
             'source_sha256':hashlib.sha256(src.encode()).hexdigest(),
             'test_sha256':hashlib.sha256(TEST.read_bytes()).hexdigest(),
             'positive_runs':2,'positive_output':a['stdout'].strip(),
             'broken_variants_rejected':bad,'test_type':'Java21 source-only with API doubles'}
    dest=ROOT/'results.json'
    dest.write_text(json.dumps(receipt,indent=2)+'\n')
    print(json.dumps(receipt,indent=2))

if __name__=='__main__': main()

