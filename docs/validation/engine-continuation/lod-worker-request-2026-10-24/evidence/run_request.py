from pathlib import Path
import subprocess,hashlib,json,os
r=Path(__file__).resolve().parent.parent;e=r/'evidence';home=Path(os.environ.get('JAVA_HOME',str(r/'toolchain/jdk-25.0.4.1+1')));sources=sorted((r/'staged/engine-core/src/main/java').rglob('*.java'))+[e/'RequestChecks.java'];out=r/'direct-final-classes';out.mkdir(exist_ok=True)
before={str(p.relative_to(r)):hashlib.sha256(p.read_bytes()).hexdigest() for p in sources};cmd=[str(home/'bin/javac'),'-g','-d',str(out),*map(str,sources)];a=subprocess.run(cmd,capture_output=True,text=True);(e/'direct-compile.txt').write_text(a.stdout+a.stderr);assert a.returncode==0,a.stderr
run=[str(home/'bin/java'),'-Xmx256m','-cp',str(out),'com.shnewbs.deepprimal.core.lod.RequestChecks'];a=subprocess.run(run,capture_output=True,text=True);(e/'direct-result.txt').write_text(a.stdout+a.stderr);assert a.returncode==0 and 'LOD_REQUEST_PASS checks=347' in a.stdout,a.stdout+a.stderr
assert before=={str(p.relative_to(r)):hashlib.sha256(p.read_bytes()).hexdigest() for p in sources};classes={str(p.relative_to(out)):hashlib.sha256(p.read_bytes()).hexdigest() for p in out.rglob('*.class')};matches=0
prod=r/'project/engine-core/build/classes/java/main'
if prod.exists():
 for name,sha in classes.items():
  f=prod/name
  if f.exists():assert hashlib.sha256(f.read_bytes()).hexdigest()==sha,name;matches+=1
(e/'direct-identity.json').write_text(json.dumps({'pass':True,'sources':before,'classes':classes,'production_class_matches_when_checkout_present':matches,'compile_command':cmd,'run_command':run,'result_sha256':hashlib.sha256((e/'direct-result.txt').read_bytes()).hexdigest()},indent=2)+'\n');print(a.stdout)
