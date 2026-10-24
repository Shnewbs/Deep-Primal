from pathlib import Path
import hashlib,json,subprocess,shutil
r=Path(__file__).resolve().parent;p=r/'project';out=r/'independent-core';sources=sorted((p/'engine-core/src/main/java').rglob('*.java'));before={str(f.relative_to(p)):hashlib.sha256(f.read_bytes()).hexdigest() for f in sources}
if out.exists():shutil.rmtree(out)
cmd=[str(r/'toolchain/jdk-25.0.4.1+1/bin/javac'),'-g','-d',str(out),*map(str,sources)];a=subprocess.run(cmd,capture_output=True,text=True);assert a.returncode==0,a.stderr
assert before=={str(f.relative_to(p)):hashlib.sha256(f.read_bytes()).hexdigest() for f in sources}
classes={str(f.relative_to(out)):hashlib.sha256(f.read_bytes()).hexdigest() for f in out.rglob('*.class')}
for name,sha in classes.items():assert hashlib.sha256((p/'engine-core/build/classes/java/main'/name).read_bytes()).hexdigest()==sha,name
(r/'evidence/independent-core.json').write_text(json.dumps({'pass':True,'source_count':len(sources),'class_count':len(classes),'sources':before,'classes':classes,'command':cmd},indent=2)+'\n');print('ALL_CORE_SOURCE_CLASS_MATCH',len(sources),len(classes))
