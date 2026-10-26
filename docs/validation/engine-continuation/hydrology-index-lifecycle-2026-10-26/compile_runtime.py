from pathlib import Path
import json,subprocess,hashlib,argparse
parser=argparse.ArgumentParser(description='Complete staged runtime API compile and JVM lifecycle regressions; not a native world test')
parser.add_argument('--baseline-stage',type=Path,required=True)
parser.add_argument('--stage',type=Path,required=True)
parser.add_argument('--jdk',type=Path,required=True)
parser.add_argument('--classpath-file',type=Path,required=True,help='Text file containing resolved Minecraft/NeoForge/product API classpath')
parser.add_argument('--output',type=Path,required=True)
args=parser.parse_args();P=args.output.resolve();P.mkdir(parents=True,exist_ok=True)
STAGE=args.stage.resolve();BASELINE=args.baseline_stage.resolve();jdk=args.jdk.resolve()/'bin'
cp=args.classpath_file.read_text().strip();results=[]
for variant in ['baseline-runtime','candidate-runtime']:
 out=P/variant;out.mkdir(exist_ok=True)
 src=BASELINE if variant=='baseline-runtime' else STAGE
 files=[src/'HydrologyRuntime.java',src/'StreamSourceIndex.java',STAGE/'StreamSourceLifecycleChecks.java',STAGE/'StreamSourceIndexChecks.java']
 cmd=[str(jdk/'javac'),'-J-Xmx128m','-J-XX:-UsePerfData','-sourcepath','','-implicit:none','-proc:none','-g','-cp',cp,'-d',str(out),*map(str,files)]
 r=subprocess.run(cmd,text=True,capture_output=True);(out/'compile.txt').write_text(r.stdout+r.stderr)
 if r.returncode:raise RuntimeError(r.stdout+r.stderr)
 tests=['StreamSourceLifecycleChecks'] if variant=='baseline-runtime' else ['StreamSourceLifecycleChecks','StreamSourceIndexChecks']
 for test in tests:
  r=subprocess.run([str(jdk/'java'),'-Xmx128m','-XX:-UsePerfData','-Xverify:all','-cp',str(out)+':'+cp,'com.shnewbs.deepprimal.engine.'+test],text=True,capture_output=True)
  (out/(test+'.txt')).write_text(r.stdout+r.stderr)
  results.append(dict(variant=variant,test=test,exit=r.returncode,output=r.stdout+r.stderr))
  if variant=='baseline-runtime':
   if r.returncode==0 or 'retired cursor removed newer source generation' not in r.stderr:raise RuntimeError('baseline did not reproduce')
  elif r.returncode:raise RuntimeError(r.stdout+r.stderr)
receipt={'results':results,'source_sha256':{f.name:hashlib.sha256(f.read_bytes()).hexdigest() for f in STAGE.glob('*.java')},'class_sha256':{str(f.relative_to(P/'candidate-runtime')):hashlib.sha256(f.read_bytes()).hexdigest() for f in (P/'candidate-runtime').rglob('*.class')},'scope':'Complete candidate API compile and direct JVM fixtures; not Gradle/native/package qualification'}
(P/'runtime-results.json').write_text(json.dumps(receipt,indent=2)+'\n');print(json.dumps(receipt,indent=2))
