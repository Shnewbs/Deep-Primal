from pathlib import Path
import json,hashlib,subprocess,time
P=Path(__file__).resolve().parent;R=Path('/workspace/scratch/a22609fabda0');project=R/'builder1700/project';jdk=R/'toolchain/jdk-25.0.4.1+1/bin'
manifest=json.loads((R/'builder1700/candidate-inputs-final.json').read_text())
paths=sorted(p for p in manifest if p.endswith('.java') and (p.startswith('engine-core/src/main/java/') or p.startswith('src/coreChecks/java/')))
if len(paths)!=138:raise RuntimeError(('unexpected inputs',len(paths)))
def verify():
 bad=[p for p in paths if hashlib.sha256((project/p).read_bytes()).hexdigest()!=manifest[p]]
 if bad:raise RuntimeError(('source drift',bad))
verify();out=P/'fresh-core';out.mkdir(exist_ok=True)
t=time.monotonic()
with (P/'core-compile.txt').open('w') as log:r=subprocess.run([str(jdk/'javac'),'-J-Xmx256m','-J-XX:-UsePerfData','-d',str(out),*[str(project/p) for p in paths]],stdout=log,stderr=subprocess.STDOUT)
if r.returncode:raise RuntimeError('core compilation failed')
with (P/'core-run.txt').open('w') as log:r=subprocess.run([str(jdk/'java'),'-Xmx512m','-XX:-UsePerfData','-Xverify:all','-Djava.io.tmpdir='+str(P/'java-tmp'),'-cp',str(out),'com.shnewbs.deepprimal.core.CoreChecks'],cwd=P,stdout=log,stderr=subprocess.STDOUT)
verify()
receipt={'source_paths':paths,'source_sha256':{p:manifest[p] for p in paths},'compiled_sources':len(paths),'exit':r.returncode,'seconds':time.monotonic()-t,'source_hashes_rechecked':True,'full_Gradle':False,'native':False}
(P/'fresh-core-result.json').write_text(json.dumps(receipt,indent=2)+'\n');print(json.dumps({k:v for k,v in receipt.items() if k not in ['source_paths','source_sha256']}))
if r.returncode:raise RuntimeError('core checks failed; see retained log')
