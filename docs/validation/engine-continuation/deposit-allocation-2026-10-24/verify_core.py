from pathlib import Path
import subprocess,hashlib,json,shutil
r=Path(__file__).resolve().parent;result={};expected={}
def digest(b):return hashlib.sha256(b).hexdigest()
for kind,project in [('control','baseline-project'),('candidate','project')]:
 p=r/project;out=r/('independent-fresh-'+kind);sources=sorted((p/'engine-core/src/main/java').rglob('*.java'))
 before={f.relative_to(p).as_posix():digest(f.read_bytes()) for f in sources}
 if out.exists():shutil.rmtree(out)
 command=[str(r/'toolchain/jdk-25.0.4.1+1/bin/javac'),'-g','-d',str(out)]+[str(f) for f in sources]
 process=subprocess.run(command,stdout=subprocess.PIPE,stderr=subprocess.STDOUT)
 assert process.returncode==0,process.stdout.decode()
 assert before=={f.relative_to(p).as_posix():digest(f.read_bytes()) for f in sources},'source changed during independent compilation'
 hashes={f.relative_to(out).as_posix():digest(f.read_bytes()) for f in out.rglob('*.class')}
 mismatches=[n for n,v in hashes.items() if digest((p/'engine-core/build/classes/java/main'/n).read_bytes())!=v]
 assert not mismatches,mismatches
 result[kind]={'source_files':len(sources),'classes_match':len(hashes),'class_sha256':hashes,'source_sha256':before,'command':command,'compiler_console':process.stdout.decode()}
 expected[kind]={n:v for n,v in hashes.items() if n.startswith('com/shnewbs/deepprimal/core/DepositField')}
(r/'independent-core-audit.json').write_text(json.dumps(result,indent=2)+'\n')
(r/'independent-compile.json').write_text(json.dumps(expected,indent=2)+'\n')
print('INDEPENDENT_ALL_CORE_MATCH',[(k,v['source_files'],v['classes_match']) for k,v in result.items()])
