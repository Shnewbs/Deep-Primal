#!/usr/bin/env python3
"""Exact V8 native plant admission helper, Java21 API doubles; NOT Minecraft."""
import json
import re
import subprocess
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / 'src/main/java/com/shnewbs/deepprimal/engine/V8VegetationNativeAdmission.java'
TEST = ROOT / 'tools/tests/V8VegetationNativeAdmissionChecks.java'
STUBS = sorted((ROOT / 'tools/tests/v8_vegetation_native_stubs').rglob('*.java'))
CLASS = 'com.shnewbs.deepprimal.engine.V8VegetationNativeAdmissionChecks'

def execute(command, timeout=35):
    p=subprocess.run(command,text=True,stdout=subprocess.PIPE,stderr=subprocess.PIPE,
                     check=False,timeout=timeout)
    return {'exit':p.returncode,'stdout':p.stdout,'stderr':p.stderr}

def compiled_run(source):
    with tempfile.TemporaryDirectory(prefix='v8-plant-support-') as work:
        place=Path(work)
        patched=place / 'com/shnewbs/deepprimal/engine/V8VegetationNativeAdmission.java'
        patched.parent.mkdir(parents=True)
        patched.write_text(source,encoding='utf-8')
        compiler=execute(['javac','--release','21','-Xlint:all','-Werror','-d',str(place),
                          str(patched),str(TEST),*[str(p) for p in STUBS]])
        if compiler['exit']:
            raise RuntimeError('strict javac failed: '+compiler['stderr'])
        return execute(['java','-ea','-Xverify:all','-cp',str(place),CLASS])

def rejected_by_fixture(result):
    return (result['exit']==1
            and re.search(r'^(?:Exception in thread "[^"]+" |Caused by: )java\.lang\.AssertionError(?::|$)',
                          result['stderr'],re.MULTILINE) is not None
            and '\tat '+CLASS+'.' in result['stderr'])

def main():
    src=SOURCE.read_text(encoding='utf-8')
    pos=[compiled_run(src) for _ in range(2)]
    for result in pos:
        if result['exit'] or 'V8_NATIVE_PLANT_SUPPORT_PASS' not in result['stdout']:
            raise RuntimeError('positive fixture failed: '+str(result))
    negative={
      'ignore_expected_support':('!plannedSupport.equals(actual) || ',''),
      'ignore_wet_support':('|| !actual.getFluidState().isEmpty()',''),
      'ignore_sturdy_face':('|| !actual.isFaceSturdy(chunk, below, Direction.UP)',''),
      'ignore_obstruction':('there.isAir() && there.getFluidState().isEmpty()',
                            'there.getFluidState().isEmpty()'),
      'ignore_liquid_destination':('there.isAir() && there.getFluidState().isEmpty()',
                                   'there.isAir()'),
    }
    rejected=[]
    for name,(old,new) in negative.items():
        if src.count(old)!=1: raise RuntimeError('mutation site not unique: '+name)
        result=compiled_run(src.replace(old,new,1))
        if not rejected_by_fixture(result):
            raise RuntimeError('broken variant not rejected by actual fixture: '+name+str(result))
        rejected.append(name)
    receipt={'java':execute(['java','-version'])['stderr'].splitlines()[0],
             'suite':'source-only Java21 compiler and API doubles; no NeoForge/native runtime',
             'positives':len(pos),'positive_output':pos[0]['stdout'].strip(),
             'rejected_compiled_broken_controls':rejected,
             'source_sha256':__import__('hashlib').sha256(src.encode()).hexdigest()}
    out=ROOT/'docs/validation/engine-continuation/v8-native-vegetation-support-2026-10-29'
    out.mkdir(parents=True,exist_ok=True)
    (out/'results.json').write_text(json.dumps(receipt,indent=2)+'\n',encoding='utf-8')
    print(json.dumps(receipt,indent=2))

if __name__=='__main__': main()
