#!/usr/bin/env python3
"""Strict Java21 source-only V8-r5 provenance key test; NOT real Mojang/NeoForge."""
import argparse
import hashlib
import json
import subprocess
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / 'src/main/java/com/shnewbs/deepprimal/engine/V8HydrologyBinding.java'
STUBS = {
  'com/mojang/serialization/Codec.java': '''package com.mojang.serialization;
import java.util.function.Function;
public final class Codec<T> {
  public static final Codec<String> STRING=new Codec<>(x->(String)x,x->x);
  private final Function<Object,T> read;private final Function<T,Object> write;
  public Codec(Function<Object,T> r,Function<T,Object> w){read=r;write=w;}
  public T decode(Object o){return read.apply(o);}
  public Object encode(T v){return write.apply(v);}
  public <U> Codec<U> comapFlatMap(Function<T,DataResult<U>> next,Function<U,T> previous){
    return new Codec<>(v->next.apply(read.apply(v)).value(),v->write.apply(previous.apply(v)));
  }
}
''',
  'com/mojang/serialization/DataResult.java': '''package com.mojang.serialization;
import java.util.function.Supplier;
public record DataResult<T>(T result,String error){
  public static <T> DataResult<T> success(T v){return new DataResult<>(v,null);}
  public static <T> DataResult<T> error(Supplier<String> why){return new DataResult<>(null,why.get());}
  public T value(){if(error!=null)throw new IllegalArgumentException(error);return result;}
}
''',
  'HydrologySourceCheck.java': '''import com.shnewbs.deepprimal.engine.V8HydrologyBinding;
import java.util.Locale;
import java.util.Random;
public final class HydrologySourceCheck {
  private static int checks;
  private static void need(boolean ok){checks++;if(!ok)throw new AssertionError("check " + checks);}
  private static void deny(Runnable operation){boolean rejected=false;
    try{operation.run();}catch(RuntimeException expected){rejected=true;}need(rejected);}
  public static void main(String[] args){
    var r=new Random(20261029L);
    for(int i=0;i<2048;i++){
      StringBuilder text=new StringBuilder(64);
      for(int j=0;j<8;j++)text.append(String.format(Locale.ROOT,"%08x",r.nextInt()));
      String fp=text.toString();
      var saved=V8HydrologyBinding.pinned(fp);
      need(saved.sourceFingerprint().equals(fp));
      need(saved.key().equals("1:1:"+fp));
      need(V8HydrologyBinding.CODEC.decode(saved.key()).equals(saved));
      need(V8HydrologyBinding.CODEC.encode(saved).equals(saved.key()));
    }
    for(String bad:new String[]{"","1:1:","1:2:"+"a".repeat(64),
      "2:1:"+"a".repeat(64),"1:1:"+"A".repeat(64),
      "1:1:"+"0".repeat(64),"1:1:"+"a".repeat(63),
      "1:1:"+"a".repeat(65),"1:1:"+"a".repeat(64)+":extra"})
      deny(()->V8HydrologyBinding.CODEC.decode(bad));
    deny(()->new V8HydrologyBinding(2,1,"a".repeat(64)));
    deny(()->new V8HydrologyBinding(1,2,"a".repeat(64)));
    deny(()->V8HydrologyBinding.pinned(null));
    deny(()->V8HydrologyBinding.pinned("A".repeat(64)));
    deny(()->V8HydrologyBinding.pinned("0".repeat(64)));
    deny(()->V8HydrologyBinding.pinned("a".repeat(63)));
    System.out.println("V8_HYDROLOGY_KEY_PASS assertions="+checks+" java="+
        System.getProperty("java.version")+" apiDoubles=true native=false");
  }
}
'''
}

def execute(work, source):
    paths=[]
    for name, code in STUBS.items():
        target=work/name
        target.parent.mkdir(parents=True,exist_ok=True)
        target.write_text(code)
        paths.append(str(target))
    java=work/'com/shnewbs/deepprimal/engine/V8HydrologyBinding.java'
    java.parent.mkdir(parents=True,exist_ok=True)
    java.write_text(source)
    paths.append(str(java))
    out=work/'classes'
    out.mkdir()
    build=subprocess.run(['javac','--release','21','-Xlint:all','-Werror','-d',str(out),*paths],
                         text=True,capture_output=True,timeout=45)
    if build.returncode:
        raise RuntimeError('source compiler failure: '+build.stderr[:1400])
    run=subprocess.run(['java','-ea','-Xverify:all','-cp',str(out),'HydrologySourceCheck'],
                       text=True,capture_output=True,timeout=45)
    return {'compile_exit':build.returncode,'test_exit':run.returncode,
            'stdout':run.stdout.strip(),'stderr':run.stderr.strip()[:300]}

def main():
    parser=argparse.ArgumentParser()
    parser.add_argument('--output',type=Path)
    args=parser.parse_args()
    original=SRC.read_text()
    mutants={
      'ignore_fingerprint':('!sourceFingerprint.matches("[0-9a-f]{64}")', 'false'),
      'ignore_observer_revision':('maskInterfaceRevision != MASK_INTERFACE_REVISION', 'false'),
    }
    for key,(needle,_) in mutants.items():
        if original.count(needle)!=1:raise RuntimeError('mutation missing: '+key)
    with tempfile.TemporaryDirectory(prefix='deepprimal-v8-hydrology-') as d:
        p=Path(d)
        positives=[execute(p/f'positive-{i}',original) for i in range(2)]
        negatives={name:execute(p/name,original.replace(anchor,replacement))
                   for name,(anchor,replacement) in mutants.items()}
    if any(v['test_exit'] for v in positives) or positives[0]['stdout']!=positives[1]['stdout']:
        raise RuntimeError('positive test failure or replay drift')
    if any(v['test_exit']==0 for v in negatives.values()):
        raise RuntimeError('broken implementation escaped regression')
    result={'backend':'strict isolated Java21 and Mojang API doubles, not Minecraft or native saved codec',
            'source_sha256':hashlib.sha256(original.encode()).hexdigest(),
            'positive':positives,'compiled_negative_controls':negatives}
    if args.output:
        args.output.parent.mkdir(parents=True,exist_ok=True)
        args.output.write_text(json.dumps(result,indent=2)+'\n')
    print(positives[0]['stdout']);print('V8_HYDROLOGY_KEY_MUTANTS_REJECTED compiled=2')

if __name__=='__main__':main()
