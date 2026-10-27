from pathlib import Path
import hashlib,json,shutil,zipfile,io,subprocess
r=Path('/dev/shm/dp-builder2800'); prior=Path('/dev/shm/dp-audit-2e15d24-0230')
out=r/'api';(out/'lib').mkdir(parents=True,exist_ok=True);(out/'baseline').mkdir(exist_ok=True);(out/'empty').mkdir(exist_ok=True)
def sha(p):
 with p.open('rb') as f:return hashlib.file_digest(f,'sha256').hexdigest()
files=[p for p in (prior/'gradle-cache/caches/modules-2/files-2.1').rglob('*.jar') if not any(x in p.name for x in ['sources','javadoc','userdev'])]
files.append(prior/'project/build/moddev/artifacts/minecraft-patched-26.1.2.122.jar')
receipt=[]
for p in files:
 h=sha(p);q=out/'lib'/(h[:16]+'-'+p.name);shutil.copyfile(p,q)
 if sha(p)!=h or sha(q)!=h:raise RuntimeError('Input changed during private snapshot')
 receipt.append({'source':str(p),'copy':str(q),'sha256':h,'bytes':q.stat().st_size})
jar=prior/'DeepPrimal-26.1.2-0.0.1-alpha.29-dev.31-audit-20261027T0230-2e15d24-vulkan-windows.jar'
assert sha(jar)=='77094017f10b1cdc9f1e72de48c0614245bd1ed8972e221ae8d6156b49392185'
classes={}
def extract(z):
 for name in z.namelist():
  if name.endswith('.class') and name.startswith('com/shnewbs/deepprimal/'):
   data=z.read(name)
   if name in classes and classes[name]!=data:raise RuntimeError('Conflicting baseline class')
   classes[name]=data;p=out/'baseline'/name;p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(data)
  elif name.startswith('META-INF/jarjar/') and name.endswith('.jar'):extract(zipfile.ZipFile(io.BytesIO(z.read(name))))
with zipfile.ZipFile(jar) as z: extract(z)
assert sha(jar)=='77094017f10b1cdc9f1e72de48c0614245bd1ed8972e221ae8d6156b49392185'
cp=':'.join([str(out/'baseline')]+[v['copy'] for v in receipt])
cmd=['/workspace/scratch/a22609fabda0/builder2300/toolchain/jdk-25.0.4.1+1/bin/javac','-J-XX:+PerfDisableSharedMem','-J-Xmx256m','-proc:none','-implicit:none','-sourcepath',str(out/'empty'),'-cp',cp,'-d',str(out/'classes')]+[str(r/f) for f in ['WaterAccount.java','WaterContactBudget.java','WaterContactRuntime.java','WaterRuntime.java']]
p=subprocess.run(cmd,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,text=True)
(r/'api-compile.log').write_text(p.stdout)
(r/'api-inputs.json').write_text(json.dumps({'immutable_private_snapshots':receipt,'baseline_jar_sha256':sha(jar),'baseline_class_count':len(classes),'command':cmd,'exit':p.returncode},indent=2)+'\n')
print(p.stdout);print(json.dumps({'exit':p.returncode,'compiled_classes':len(list((out/'classes').rglob('*.class'))),'private_snapshot_bytes':sum(v['bytes'] for v in receipt)}))
raise SystemExit(p.returncode)
