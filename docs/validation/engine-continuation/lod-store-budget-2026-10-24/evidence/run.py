from pathlib import Path
import subprocess,json,hashlib,os
r=Path(__file__).resolve().parent.parent;e=r/'evidence';java=Path(os.environ.get('JAVA_HOME','/workspace/scratch/a22609fabda0/water-frame-1732/toolchain/jdk-25.0.4.1+1'))/'bin';result={}
for variant in ['baseline','candidate']:
 out=r/('classes-'+variant);out.mkdir(exist_ok=True);sources=list((r/variant).glob('*.java'))+[e/'LodStoreBudgetChecks.java']
 cmd=[str(java/'javac'),'-g','-d',str(out),*map(str,sources)];p=subprocess.run(cmd,capture_output=True,text=True);(e/(variant+'-compile.txt')).write_text(p.stdout+p.stderr);assert p.returncode==0,p.stderr
 cmd=[str(java/'java'),'-Xmx256m','-cp',str(out),'com.shnewbs.deepprimal.core.lod.LodStoreBudgetChecks']+(['--expect-bug'] if variant=='baseline' else [])
 p=subprocess.run(cmd,capture_output=True,text=True);(e/(variant+'-result.txt')).write_text(p.stdout+p.stderr);assert p.returncode==0,p.stderr
 result[variant]={'exit':p.returncode,'sources':{f.name:hashlib.sha256(f.read_bytes()).hexdigest() for f in sources},'classes':{str(f.relative_to(out)):hashlib.sha256(f.read_bytes()).hexdigest() for f in out.rglob('*.class')},'result':p.stdout};print(p.stdout,flush=True)
(e/'direct-checks.json').write_text(json.dumps(result,indent=2)+'\n')
