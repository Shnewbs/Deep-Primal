from pathlib import Path
import subprocess,json,zipfile,hashlib
P=Path(__file__).resolve().parent.parent;R=Path('/workspace/scratch/a22609fabda0');out=P/'native';cp=Path('/dev/shm/dp-builder1905-oaqp2q1h/classpath.txt').read_text().strip();j=R/'toolchain/jdk-25.0.4.1+1/bin'
(out/'producer-complete.json').unlink(missing_ok=True)
runtime_receipt=json.loads((P/'runtime/runtime-results.json').read_text())
for name,digest in runtime_receipt['source_sha256'].items():
 if hashlib.sha256((P/'stage'/name).read_bytes()).hexdigest()!=digest:raise RuntimeError('Runtime source changed since API compilation: '+name)
for name,digest in runtime_receipt['class_sha256'].items():
 if hashlib.sha256((P/'runtime/candidate-runtime'/name).read_bytes()).hexdigest()!=digest:raise RuntimeError('Runtime class changed since API compilation: '+name)
cmd=[str(j/'javac'),'-J-Xmx128m','-J-XX:-UsePerfData','-sourcepath','','-implicit:none','-proc:none','-cp',str(P/'runtime/candidate-runtime')+':'+cp,'-d',str(out/'fixtures'),str(out/'StreamIndexNativeChecks.java')]
r=subprocess.run(cmd,capture_output=True,text=True);(out/'compile.txt').write_text(r.stdout+r.stderr)
if r.returncode:raise RuntimeError(r.stderr)
base=R/'builder1700/final/DeepPrimal-26.1.2-0.0.1-alpha.29-dev.31-vulkan-windows.jar'
replacements={str(p.relative_to(P/'runtime/candidate-runtime')):p.read_bytes() for p in (P/'runtime/candidate-runtime').rglob('*.class') if p.name.startswith(('HydrologyRuntime','StreamSourceIndex')) and 'Checks' not in p.name}
target=out/'private-index-diagnostic.jar'
with zipfile.ZipFile(base) as src,zipfile.ZipFile(target,'w',compression=zipfile.ZIP_DEFLATED,compresslevel=1) as dst:
 for info in src.infolist():
  if info.filename in replacements or info.filename.startswith('META-INF/jarjar/'):continue
  dst.writestr(info,src.read(info))
 for name,b in replacements.items():dst.writestr(name,b)
with zipfile.ZipFile(target) as z:
 if z.testzip():raise RuntimeError('CRC failure')
 for name,b in replacements.items():
  if z.read(name)!=b:raise RuntimeError(name)
(out/'diagnostic-package.json').write_text(json.dumps({'base_source':'a1583a508b0d67130d387bc018defd5758acd496','base_sha256':hashlib.sha256(base.read_bytes()).hexdigest(),'diagnostic_sha256':hashlib.sha256(target.read_bytes()).hexdigest(),'bytes':target.stat().st_size,'replacements':{n:hashlib.sha256(b).hexdigest() for n,b in replacements.items()},'release':False,'full_build':False,'assembly':'Private diagnostic root plus six separately loaded immutable nested module JARs; not a distributable bundle'},indent=2))
launch=[p for p in cp.split(':') if p.endswith('.jar') and '/jst-cli-bundle/' not in p and '/neoform-runtime/' not in p]
(out/'classpath.txt').write_text(':'.join(launch))
print('PRIVATE_DIAGNOSTIC_READY',target.stat().st_size,len(launch))

import re
modules=[]
with zipfile.ZipFile(base) as z:
 for name in z.namelist():
  if name.startswith('META-INF/jarjar/') and name.endswith('.jar'):
   b=z.read(name);h=hashlib.sha256(b).hexdigest()
   matches=list((R/'builder1700/final/native-cache').rglob(Path(name).name))
   matches=[p for p in matches if hashlib.sha256(p.read_bytes()).hexdigest()==h]
   if len(matches)!=1:raise RuntimeError(('module cache identity',name))
   with zipfile.ZipFile(matches[0]) as mz:modid=re.search(r'modId\s*=\s*["\']([^"\']+)',mz.read('META-INF/neoforge.mods.toml').decode()).group(1) if 'META-INF/neoforge.mods.toml' in mz.namelist() else 'deepprimal_core'
   modules.append(dict(modid=modid,path=str(matches[0]),sha256=h))
(out/'module-inputs.json').write_text(json.dumps(modules,indent=2))

(out/'producer-complete.json').write_text(json.dumps({'source_sha256':hashlib.sha256((out/'StreamIndexNativeChecks.java').read_bytes()).hexdigest(),'fixture_classes':{str(p.relative_to(out/'fixtures')):hashlib.sha256(p.read_bytes()).hexdigest() for p in (out/'fixtures').rglob('*.class')},'jar_sha256':hashlib.sha256(target.read_bytes()).hexdigest()},indent=2))
