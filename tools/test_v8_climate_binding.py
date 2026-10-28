#!/usr/bin/env python3
"""Source-only Java21 contract for V8-r4 saved climate key. Not Minecraft/DFU/Gradle."""
import argparse
import hashlib
import json
import os
import subprocess
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ENGINE = ROOT / 'src/main/java/com/shnewbs/deepprimal/engine/V8ClimateBinding.java'
OPTIONS = ROOT / 'engine-core/src/main/java/com/shnewbs/deepprimal/core/TerrainOptions.java'
CODEC = '''package com.mojang.serialization;
import java.util.function.Function;
public class Codec<T> {
  public static final Codec<String> STRING = new Codec<>(s->(String)s,s->s);
  private final Function<Object,T> read;
  private final Function<T,Object> write;
  public Codec(Function<Object,T> read, Function<T,Object> write){this.read=read;this.write=write;}
  public T decode(Object source){return read.apply(source);}
  public Object encode(T value){return write.apply(value);}
  public <U> Codec<U> comapFlatMap(Function<T,DataResult<U>> map,Function<U,T> reverse){
    return new Codec<>(v->map.apply(read.apply(v)).value(),v->write.apply(reverse.apply(v)));
  }
}
'''
RESULT = '''package com.mojang.serialization;
public record DataResult<T>(T result,String error){
  public static <T> DataResult<T> success(T value){return new DataResult<>(value,null);}
  public static <T> DataResult<T> error(java.util.function.Supplier<String> reason){
    return new DataResult<>(null,reason.get());
  }
  public T value(){if(error!=null)throw new IllegalArgumentException(error);return result;}
}
'''
CHECK = '''import com.shnewbs.deepprimal.engine.V8ClimateBinding;
import com.shnewbs.deepprimal.core.TerrainOptions;
import java.util.Random;
public final class BindingCheck {
  private static long checks;
  private static void need(boolean p){checks++;if(!p)throw new AssertionError("binding mismatch "+checks);}
  private static void deny(Runnable work){boolean denied=false;
    try{work.run();}catch(RuntimeException ok){denied=true;}
    need(denied);
  }
  public static void main(String[] args){
    Random r=new Random(20261028);
    for(int i=0;i<4096;i++){
      TerrainOptions options=new TerrainOptions(r.nextBoolean(),r.nextBoolean(),
        r.nextBoolean(),r.nextBoolean(),r.nextBoolean(),
        .5+r.nextDouble()*1.5,4000+r.nextDouble()*996000);
      V8ClimateBinding binding=V8ClimateBinding.fromSavedV7(options,i%6);
      need(binding.equals(V8ClimateBinding.CODEC.decode(binding.key())));
      need(binding.key().equals(V8ClimateBinding.CODEC.encode(binding)));
      need(binding.savedOptions().equals(options));
    }
    for(String bad:new String[]{"", "1:7:6:11111:3ff4cccccccccccd:40cf400000000000",
      "1:7:0:21111:3ff4cccccccccccd:40cf400000000000",
      "1:7:0:11111:7ff8000000000000:40cf400000000000",
      "1:7:0:11111:3ff4CCCCCCCCCCCD:40cf400000000000",
      "1:7:0:11111:3ff4cccccccccccd:0000000000000000"}) {
      deny(()->V8ClimateBinding.CODEC.decode(bad));
    }
    deny(()->new V8ClimateBinding(1,7,6,TerrainOptions.DEFAULT));
    deny(()->new V8ClimateBinding(2,7,5,TerrainOptions.DEFAULT));
    deny(()->new V8ClimateBinding(1,6,5,TerrainOptions.DEFAULT));
    System.out.println("V8_BINDING_ISOLATED_PASS assertions="+checks+" java="+System.getProperty("java.version")+" apiDoubles=true");
  }
}
'''

def invoke(args, cwd=None):
    return subprocess.run(args, cwd=cwd, capture_output=True, text=True, timeout=90)

def run_case(home, source, expect_pass):
    home.mkdir(parents=True, exist_ok=True)
    files = [
        (home / 'com/mojang/serialization/Codec.java', CODEC),
        (home / 'com/mojang/serialization/DataResult.java', RESULT),
        (home / 'com/shnewbs/deepprimal/engine/V8ClimateBinding.java', source),
        (home / 'com/shnewbs/deepprimal/core/TerrainOptions.java', OPTIONS.read_text()),
        (home / 'BindingCheck.java', CHECK),
    ]
    for p, txt in files:
        p.parent.mkdir(parents=True, exist_ok=True)
        p.write_text(txt)
    classes = home / 'classes'
    classes.mkdir(exist_ok=True)
    command = ['javac', '--release', '21', '-Xlint:all', '-Werror', '-d', str(classes)]
    build = invoke(command + [str(p) for p, _ in files])
    if build.returncode != 0:
        raise RuntimeError('focused compile failure: ' + build.stderr[:2000])
    run = invoke(['java', '-ea', '-Xverify:all', '-cp', str(classes), 'BindingCheck'])
    if (run.returncode == 0) != expect_pass:
        raise RuntimeError('unexpected positive/negative outcome: ' + run.stdout + run.stderr[:2000])
    return {'compile_exit': build.returncode, 'test_exit': run.returncode,
            'stdout': run.stdout.strip(), 'stderr': run.stderr.strip()[:500]}

def main():
    parser=argparse.ArgumentParser()
    parser.add_argument('--output', type=Path)
    args=parser.parse_args()
    original=ENGINE.read_text()
    mutation='|| channelRevision > 5'
    if original.count(mutation) != 1:
        raise RuntimeError('negative mutation anchor missing')
    with tempfile.TemporaryDirectory(prefix='deepprimal-v8-binding-') as root:
        root=Path(root)
        positive=run_case(root/'positive', original, True)
        negative=run_case(root/'negative', original.replace(mutation,''), False)
    receipt={'backend':'isolated Java21 API doubles, NOT Mojang/NeoForge/native',
             'source_sha256':hashlib.sha256(original.encode()).hexdigest(),
             'options_sha256':hashlib.sha256(OPTIONS.read_bytes()).hexdigest(),
             'positive':positive,'negative_mutant':negative}
    if args.output:
        args.output.parent.mkdir(parents=True,exist_ok=True)
        args.output.write_text(json.dumps(receipt,indent=2)+'\n')
    print(positive['stdout'])
    print('V8_BINDING_MUTANT_REJECTED' if negative['test_exit'] else 'MUTANT_FAILURE')

if __name__=='__main__':
    main()
