from pathlib import Path
import hashlib,json,subprocess,shutil,time
r=Path(__file__).resolve().parent.parent;java=r.parent/'lod-future-r57/toolchain/jdk-25.0.4.1+1/bin/java';javac=java.with_name('javac')
manifest=json.loads((r/'evidence/parent-inputs.json').read_text())
for row in manifest:
 b=(r/'parent'/row['path']).read_bytes();assert hashlib.sha1(b'blob '+str(len(b)).encode()+b'\0'+b).hexdigest()==row['sha'],row['path']
source=r/'candidate';shutil.copytree(r/'parent/engine-core',source,dirs_exist_ok=True)
for p in (r/'staged/engine-core').rglob('*.java'):(source/p.relative_to(r/'staged/engine-core')).write_bytes(p.read_bytes())
ss=list(source.rglob('*.java'))+[r/'evidence'/x for x in ['ReuseChecks.java','NoiseCoreChecks.java','DensityGraphChecks.java']];out=r/'classes';
if out.exists():shutil.rmtree(out)
out.mkdir()
rec={'parent':'44b89f541ae098f0ad6c16f622c7bffb058ffcc7','verified_parent_sources':len(manifest),'candidate_core_sources':len(list(source.rglob('*.java'))),'source_sha256':{str(p.relative_to(r)):hashlib.sha256(p.read_bytes()).hexdigest() for p in ss}}
p=subprocess.run([str(javac),'-g','-d',str(out),*map(str,ss)],capture_output=True,text=True,timeout=60);rec['compile']={'exit':p.returncode,'output':p.stdout+p.stderr}
if p.returncode==0:
 for main,marker in [('ReuseChecks','DENSITY_REUSE_PASS'),('com.shnewbs.deepprimal.core.density.NoiseCoreChecks','NOISE_CORE_PASS'),('com.shnewbs.deepprimal.core.density.DensityGraphChecks','DENSITY_GRAPH_PASS')]:
  p=subprocess.run([str(java),'-Xverify:all','-cp',str(out),main],capture_output=True,text=True,timeout=180);rec[main]={'exit':p.returncode,'output':p.stdout+p.stderr,'pass':p.returncode==0 and marker in p.stdout};(r/'evidence'/f'{main.rsplit(".",1)[-1]}.log').write_text(p.stdout+p.stderr);print(main,p.returncode,p.stdout+p.stderr,flush=True)
rec['pass']=rec['compile']['exit']==0 and all(rec.get(k,{}).get('pass') for k in ['ReuseChecks','com.shnewbs.deepprimal.core.density.NoiseCoreChecks','com.shnewbs.deepprimal.core.density.DensityGraphChecks'])
rec['class_sha256']={str(p.relative_to(out)):hashlib.sha256(p.read_bytes()).hexdigest() for p in out.rglob('*.class')};(r/'evidence/results.json').write_text(json.dumps(rec,indent=2)+'\n');assert rec['pass']
