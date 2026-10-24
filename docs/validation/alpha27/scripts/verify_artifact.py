from pathlib import Path
import subprocess,hashlib,json,zipfile,io,shutil
r=Path(__file__).resolve().parent;p=r/'project';out=r/'independent-core';h=lambda b:hashlib.sha256(b).hexdigest()
if out.exists():shutil.rmtree(out)
sources=sorted((p/'engine-core/src/main/java').rglob('*.java'))
subprocess.run([str(r/'toolchain/jdk-25.0.4.1+1/bin/javac'),'-g','-d',str(out)]+list(map(str,sources)),check=True)
for f in out.rglob('*.class'):assert f.read_bytes()==(p/'engine-core/build/classes/java/main'/f.relative_to(out)).read_bytes(),str(f)
jar=p/'build/libs/DeepPrimal-26.1.2-0.0.1-alpha.27-vulkan-linux.jar';archives=0;classes=0
with zipfile.ZipFile(jar) as z:
 assert z.testzip() is None
 for f in (p/'build/classes/java/main').rglob('*.class'):assert z.read(f.relative_to(p/'build/classes/java/main').as_posix())==f.read_bytes(),str(f);classes+=1
 for name in z.namelist():
  if name.endswith('.jar'):
   with zipfile.ZipFile(io.BytesIO(z.read(name))) as inner:
    assert inner.testzip() is None;archives+=1
    for module in ['engine-core','graphics-settings','performance-engine','lod-engine','gui-themes','renderer-vulkan']:
     for f in (p/module/'build/classes/java/main').rglob('*.class'):
      key=f.relative_to(p/module/'build/classes/java/main').as_posix()
      if key in inner.namelist():assert inner.read(key)==f.read_bytes(),str(f);classes+=1
record={'independent_core_sources':len(sources),'independent_core_classes':len(list(out.rglob('*.class'))),'packaged_class_comparisons':classes,'nested_archives':archives,'jar':str(jar),'sha256':h(jar.read_bytes())}
(r/'artifact-audit.json').write_text(json.dumps(record,indent=2)+'\n');print(record)
