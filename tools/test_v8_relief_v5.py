#!/usr/bin/env python3
"""Isolated Java21 compile/negative checks for the production V8 relief-5 join nodes.

This is an API-double component check, not real DensityGraph VM or Minecraft.
"""
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import sys
import tempfile

ROOT = Path(__file__).resolve().parents[1]
JOIN = Path('engine-core/src/main/java/com/shnewbs/deepprimal/core/density/V8ReliefJoinV5.java')
STUB = Path('tools/tests/v8_relief_v5_stubs/com/shnewbs/deepprimal/core/density/DensityGraph.java')
CHECKS = Path('tools/tests/V8ReliefJoinV5ComponentChecks.java')
MAIN = 'com.shnewbs.deepprimal.core.density.V8ReliefJoinV5ComponentChecks'


def execute(args, work, *, must_pass):
    done = subprocess.run(args, cwd=work, text=True, capture_output=True, check=False, timeout=50)
    if must_pass and done.returncode != 0:
        raise RuntimeError(f'command failed: {args}\n{done.stdout}\n{done.stderr}')
    return done


def compile_run(source, tmp):
    pkg = tmp / 'com/shnewbs/deepprimal/core/density'
    pkg.mkdir(parents=True, exist_ok=True)
    (pkg / 'V8ReliefJoinV5.java').write_text(source, encoding='utf-8')
    files = [str((ROOT / p).resolve()) for p in (STUB, CHECKS)] + [str(pkg/'V8ReliefJoinV5.java')]
    out = tmp / 'classes'
    out.mkdir()
    execute(['javac','--release','21','-Xlint:all','-Werror','-d',str(out),*files],tmp,must_pass=True)
    return execute(['java','-ea','-Xverify:all','-cp',str(out),MAIN],tmp,must_pass=False)


def once(source, mutant=None):
    with tempfile.TemporaryDirectory(prefix='dp-relief-v5-') as path:
        done = compile_run(source, Path(path))
        if mutant is None:
            if done.returncode != 0 or 'RELIEF5_JOIN_COMPONENT_PASS' not in done.stdout:
                raise RuntimeError('positive Java21 component check failed: '+done.stdout+done.stderr)
            match=re.search(r'assertions=(\d+)',done.stdout)
            if match is None or int(match.group(1)) < 100000:
                raise RuntimeError('missing/count-insufficient executed Java assertions')
            return {'assertions':int(match.group(1)),'receipt':done.stdout.strip()}
        if done.returncode == 0 or 'AssertionError' not in done.stderr:
            raise RuntimeError('broken implementation escaped detection: '+mutant)
        return {'mutation':mutant,'rejected':True}


def main():
    java=execute(['java','-version'],ROOT,must_pass=True)
    javac=execute(['javac','-version'],ROOT,must_pass=True)
    source=(ROOT/JOIN).read_text(encoding='utf-8')
    positives=[once(source),once(source)]
    if positives[0]!=positives[1]:
        raise RuntimeError('positive replay receipts differ')
    mutations={
        'disabled_spatial_band':('"relief5_shifted_continent", .06, .40)',
                                 '"relief5_shifted_continent", 1.2, 1.4)'),
        'unbounded_height_amplitude':('Node.constant("relief5_join_scale", 11)',
                                      'Node.constant("relief5_join_scale", 30)'),
    }
    negatives=[]
    for name,(old,new) in mutations.items():
        if source.count(old)!=1:
            raise RuntimeError('mutation anchor drift: '+name)
        negatives.append(once(source.replace(old,new),name))
    manifest={
        'mode':'Java21 isolated production join plus DensityGraph API double',
        'java':java.stderr.strip(), 'javac':javac.stdout.strip(),
        'optimized_python':not __debug__,
        'source_sha256':hashlib.sha256(source.encode()).hexdigest(),
        'test_sha256':hashlib.sha256((ROOT/CHECKS).read_bytes()).hexdigest(),
        'stub_sha256':hashlib.sha256((ROOT/STUB).read_bytes()).hexdigest(),
        'positive_processes':positives,
        'compiled_negative_controls':negatives,
        'not_verified':['actual production DensityGraph VM','V8 snapshot Mojang codec','Minecraft native chunks','saved-world reopening','Java25/Gradle','GPU','full performance'],
    }
    output=Path(sys.argv[1]) if len(sys.argv)>1 else ROOT/'relief-v5-results.json'
    output.parent.mkdir(parents=True,exist_ok=True)
    output.write_text(json.dumps(manifest,indent=2)+'\n',encoding='utf-8')
    print('RELIEF5_RUNNER_PASS assertions_per_process='+str(positives[0]['assertions'])+
          ' positive=2 broken_rejected=2 optimized_python='+str(not __debug__))


if __name__ == '__main__':
    main()
