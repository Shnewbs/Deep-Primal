from pathlib import Path
import json,zipfile,io,subprocess,hashlib
r=Path('/dev/shm/dp-builder2600'); api=r/'api'; receipt=json.loads((r/'api-inputs.json').read_text())
cp=':'.join([str(api/'classes'),str(api/'baseline')]+[x['copy'] for x in receipt['immutable_private_snapshots']])
cmd=['/workspace/scratch/a22609fabda0/builder2300/toolchain/jdk-25.0.4.1+1/bin/javac','-J-XX:+PerfDisableSharedMem','-proc:none','-implicit:none','-sourcepath',str(api/'empty'),'-cp',cp,'-d',str(r/'native-fixture'),str(r/'ContactNativeChecks.java')]
subprocess.run(cmd,check=True)
baseline=Path('/dev/shm/dp-audit-5ba6c80-0127/DeepPrimal-26.1.2-0.0.1-alpha.29-dev.31-audit-20261027T0127-5ba6c80-vulkan-windows.jar')
replacements={str(p.relative_to(api/'classes')):p.read_bytes() for p in (api/'classes').rglob('*.class')}
seen=set()
def patch(data):
 out=io.BytesIO()
 with zipfile.ZipFile(io.BytesIO(data)) as src,zipfile.ZipFile(out,'w',zipfile.ZIP_DEFLATED) as dst:
  for n in src.namelist():
   v=src.read(n)
   if n in replacements:v=replacements[n];seen.add(n)
   elif n.startswith('META-INF/jarjar/') and n.endswith('.jar'):v=patch(v)
   dst.writestr(n,v)
  # Add contact classes only to the root containing WaterRuntime; WaterAccount is inside core.
  if 'com/shnewbs/deepprimal/engine/WaterRuntime.class' in src.namelist():
   for n,v in replacements.items():
    if 'WaterContactRuntime' in n and n not in src.namelist():dst.writestr(n,v);seen.add(n)
  if 'com/shnewbs/deepprimal/core/WaterAccount.class' in src.namelist():
   for n,v in replacements.items():
    if 'WaterAccount$Diffusion' in n and n not in src.namelist():dst.writestr(n,v);seen.add(n)
 return out.getvalue()
v=patch(baseline.read_bytes());assert seen==set(replacements),(set(replacements)-seen)
jar=r/'contact-diagnostic-private.jar';jar.write_bytes(v)
with zipfile.ZipFile(jar) as z:assert z.testzip() is None;assert not any('ContactNativeChecks' in n for n in z.namelist())
(r/'native-assembly.json').write_text(json.dumps({'diagnostic_only':True,'full_gradle_build':False,'user_facing':False,'replaced_or_added':sorted(seen),'bytes':len(v),'sha256':hashlib.sha256(v).hexdigest()},indent=2)+'\n')
print('PRIVATE_NATIVE_ASSEMBLY_PASS classes='+str(len(seen)))
