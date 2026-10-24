from pathlib import Path
import subprocess,json,hashlib
r=Path(__file__).resolve().parent.parent; e=r/'evidence';tool=r.parent/'resume-tools/toolchain/jdk-25.0.4.1+1/bin'
tests=['ReuseChecks.java','NoiseCoreChecks.java','DensityGraphChecks.java','AdversarialChecks.java','MetadataBenchmark.java']
record={}
for variant in ['control','candidate']:
 source=r/variant/'engine-core';out=r/('classes-'+variant);out.mkdir(exist_ok=True)
 sources=sorted(source.rglob('*.java'))+[e/x for x in tests]
 command=[str(tool/'javac'),'-g','-d',str(out),*map(str,sources)]
 p=subprocess.run(command,capture_output=True,text=True,timeout=60);assert p.returncode==0,p.stdout+p.stderr
 result={'core_sources':len(sources)-len(tests),'source_sha256':{str(p.relative_to(r)):hashlib.sha256(p.read_bytes()).hexdigest() for p in sources},'compile_exit':p.returncode,'compile_command':command,'checks':{}}
 for name,marker in [('ReuseChecks','DENSITY_REUSE_PASS'),('com.shnewbs.deepprimal.core.density.NoiseCoreChecks','NOISE_CORE_PASS'),('com.shnewbs.deepprimal.core.density.DensityGraphChecks','DENSITY_GRAPH_PASS'),('AdversarialChecks','DENSITY_ADVERSARIAL_PASS')]:
  p=subprocess.run([str(tool/'java'),'-Xverify:all','-cp',str(out),name],capture_output=True,text=True,timeout=180)
  (e/(variant+'-'+name.rsplit('.',1)[-1]+'.txt')).write_text(p.stdout+p.stderr)
  result['checks'][name]={'exit':p.returncode,'pass':p.returncode==0 and marker in p.stdout,'output':p.stdout+p.stderr};print(variant,name,p.returncode,p.stdout+p.stderr,flush=True)
  assert result['checks'][name]['pass']
 result['class_sha256']={str(p.relative_to(out)):hashlib.sha256(p.read_bytes()).hexdigest() for p in out.rglob('*.class')}
 record[variant]=result
record['pass']=True;(e/'checks.json').write_text(json.dumps(record,indent=2)+'\n')
