#!/usr/bin/env python3
"""Compile the exact V8 withBlendPolicy method in a Java21 stand-in.

This is a scoped source-extraction regression, not full NeoForge or a Mojang
codec/save-reopen test. Real production-class API-double tests are separate.
Negative controls must compile and fail on an assertion, even with Python -OO.
"""
import argparse
import hashlib
import json
import re
import subprocess
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / 'src/main/java/com/shnewbs/deepprimal/engine/V8GeneratorSnapshot.java'
START = 'public V8GeneratorSnapshot withBlendPolicy(V8BlendPolicy policy) {'


def extract(text):
    if text.count(START) != 1:
        raise RuntimeError('source method anchor is not unique')
    start = text.index(START)
    opening = text.index('{', start)
    depth = 0
    for i in range(opening, len(text)):
        if text[i] == '{':
            depth += 1
        elif text[i] == '}':
            depth -= 1
            if depth == 0:
                return text[start:i + 1]
    raise RuntimeError('unbalanced source method')


JAVA = r'''import java.util.Objects;
public final class AuthoringContract {
  private static long cases;
  private static void need(boolean p,String reason){
    cases++;
    if(!p)throw new AssertionError(reason+" case="+cases);
  }
  private static final class V8BlendPolicy {}
  private static final class V8GeneratorSnapshot {
    static final int VERSION=1, BLENDED_VERSION=2;
    final int version, minY, height;
    final long seed;
    final Object density,ground;
    final V8BlendPolicy blendPolicy;
    V8GeneratorSnapshot(int v,long s,int y,int h,Object d,Object g,V8BlendPolicy b){
      version=v;seed=s;minY=y;height=h;density=d;ground=g;blendPolicy=b;
    }
    __METHOD__
  }
  private static void deny(V8GeneratorSnapshot s,V8BlendPolicy b,int expected){
    boolean denied=false;
    try{s.withBlendPolicy(b);}catch(IllegalStateException correct){denied=true;}
    need(denied,"saved version "+expected+" downgraded");
    need(s.version==expected,"original version changed");
  }
  public static void main(String[] args){
    V8BlendPolicy p=new V8BlendPolicy();
    Object graph=new Object(), ground=new Object();
    for(int i=0;i<1024;i++){
      long seed=Long.rotateLeft(20261028L+i*73L,i&63);
      V8GeneratorSnapshot one=new V8GeneratorSnapshot(1,seed,-128,96,graph,ground,p);
      V8GeneratorSnapshot two=new V8GeneratorSnapshot(2,seed,-128,96,graph,ground,p);
      V8GeneratorSnapshot three=new V8GeneratorSnapshot(3,seed,-128,96,graph,ground,p);
      V8GeneratorSnapshot four=new V8GeneratorSnapshot(4,seed,-128,96,graph,ground,p);
      V8GeneratorSnapshot fromLegacy=one.withBlendPolicy(p);
      V8GeneratorSnapshot fromBlend=two.withBlendPolicy(p);
      need(fromLegacy.version==2&&fromBlend.version==2,"explicit upgrade/edit");
      need(fromLegacy.seed==seed&&fromBlend.seed==seed,"saved seed changed");
      need(fromLegacy.density==graph&&fromBlend.ground==ground,"saved graph lost");
      deny(three,p,3);deny(four,p,4);
      deny(three,null,3);deny(four,null,4);
      need(one.version==1&&two.version==2,"source profile changed");
    }
    System.out.println("V8_BLEND_AUTHORING_EXTRACT_PASS cases="+cases);
  }
}
'''


def run_variant(directory, method):
    directory.mkdir(parents=True, exist_ok=True)
    java = directory / 'AuthoringContract.java'
    java.write_text(JAVA.replace('__METHOD__', method), encoding='utf-8')
    compile_result = subprocess.run(
        ['javac', '--release', '21', '-Xlint:all', '-Werror',
         '-d', str(directory), str(java)], capture_output=True, text=True, timeout=45)
    if compile_result.returncode:
        raise RuntimeError('source-extract compile failed: ' + compile_result.stderr[:1800])
    run_result = subprocess.run(
        ['java', '-ea', '-Xverify:all', '-cp', str(directory), 'AuthoringContract'],
        capture_output=True, text=True, timeout=45)
    return {'compile_exit': compile_result.returncode,
            'run_exit': run_result.returncode, 'stdout': run_result.stdout.strip(),
            'stderr': run_result.stderr.strip()[:1400]}


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--source', type=Path, default=SOURCE)
    parser.add_argument('--output', type=Path)
    args = parser.parse_args()
    source = args.source.read_text(encoding='utf-8')
    method = extract(source)
    guard = 'if (version != VERSION && version != BLENDED_VERSION)'
    if method.count(guard) != 1:
        raise RuntimeError('explicit revision guard missing or duplicated')
    # The mutants preserve valid Java semantics, but must fail the test oracle.
    no_guard = re.sub(r'    if \(version != VERSION && version != BLENDED_VERSION\)\s*'
                      r'throw new IllegalStateException\(.*?\);\s*', '', method,
                      count=1, flags=re.DOTALL)
    r4_only = method.replace(guard, 'if (version == 4)')
    if no_guard == method or r4_only == method:
        raise RuntimeError('negative mutation did not change the method')
    with tempfile.TemporaryDirectory(prefix='dp-v8-authoring-') as temp:
        t = Path(temp)
        positive = run_variant(t / 'positive', method)
        negative = [run_variant(t / 'no-guard', no_guard),
                    run_variant(t / 'r4-only', r4_only)]
    if positive['run_exit'] or any(v['run_exit'] == 0 for v in negative):
        raise RuntimeError('authoring regression controls failed: '
                           + json.dumps({'positive': positive, 'negative': negative}))
    receipt = {
        'scope': 'real method extracted into Java21 contract class; NOT native or full V8 class',
        'production_source_sha256': hashlib.sha256(source.encode()).hexdigest(),
        'method_sha256': hashlib.sha256(method.encode()).hexdigest(),
        'positive': positive, 'negative_controls': negative,
    }
    if args.output:
        args.output.parent.mkdir(parents=True, exist_ok=True)
        args.output.write_text(json.dumps(receipt, indent=2) + '\n', encoding='utf-8')
    print(positive['stdout'])
    print('V8_AUTHORING_MUTANTS_REJECTED count=2')


if __name__ == '__main__':
    main()
