from pathlib import Path
import subprocess,hashlib,json,shutil
r=Path(__file__).resolve().parent.parent
java=r/'toolchain/jdk-25.0.4.1+1/bin/java';javac=java.with_name('javac')
manifest=json.loads((r/'evidence/core-inputs.json').read_text())
for row in manifest:
 b=(r/'project'/row['path']).read_bytes();actual=hashlib.sha1(b'blob '+str(len(b)).encode()+b'\0'+b).hexdigest();assert actual==row['sha'],row['path']
source=r/'candidate-core';shutil.copytree(r/'project/engine-core',source,dirs_exist_ok=True)
for p in (r/'staged/engine-core').rglob('*.java'):
 target=source/p.relative_to(r/'staged/engine-core');target.parent.mkdir(parents=True,exist_ok=True);target.write_bytes(p.read_bytes())
ss=sorted(source.rglob('*.java'));fixtures=[r/'evidence'/f for f in ['WorkerChecks.java','FutureChecks.java','AdapterChecks.java','RequestChecks.java']]+list((r/'evidence/stubs').rglob('*.java'))
out=r/'full-core-classes';out.mkdir(exist_ok=True)
record={'java':subprocess.run([str(java),'-version'],capture_output=True,text=True).stderr,'parent':'cbc20d86e626285dfa12bb2ed6ef747358fbec59','git_verified_parent_core_sources':len(manifest),'candidate_core_sources':len(ss),'source_sha256':{str(p.relative_to(r)):hashlib.sha256(p.read_bytes()).hexdigest() for p in ss+fixtures},'full_gradle_or_native':False}
p=subprocess.run([str(javac),'-g','-d',str(out),*map(str,ss+fixtures)],capture_output=True,text=True,timeout=60)
record['compile']={'exit':p.returncode,'stdout':p.stdout,'stderr':p.stderr};(r/'evidence/full-core-compile.log').write_text(p.stdout+p.stderr)
if p.returncode==0:
 for main,marker in [('WorkerChecks','checks=898'),('FutureChecks','checks=803'),('AdapterChecks','checks=20'),('com.shnewbs.deepprimal.core.lod.RequestChecks','checks=347')]:
  p=subprocess.run([str(java),'-Xverify:all','-cp',str(out),main],capture_output=True,text=True,timeout=120);record[main]={'exit':p.returncode,'stdout':p.stdout,'stderr':p.stderr,'pass':p.returncode==0 and marker in p.stdout};(r/'evidence'/f'jdk25-{main.rsplit(".",1)[-1]}.log').write_text(p.stdout+p.stderr)
record['class_sha256']={str(p.relative_to(out)):hashlib.sha256(p.read_bytes()).hexdigest() for p in out.rglob('*.class')}
record['pass']=record['compile']['exit']==0 and all(record.get(k,{}).get('pass',False) for k in ['WorkerChecks','FutureChecks','AdapterChecks','com.shnewbs.deepprimal.core.lod.RequestChecks'])
(r/'evidence/full-core-results.json').write_text(json.dumps(record,indent=2)+'\n');print(json.dumps({k:v for k,v in record.items() if k not in ['class_sha256','source_sha256']},indent=2));assert record['pass']
