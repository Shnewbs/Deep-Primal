from pathlib import Path
import json,zipfile,io,hashlib
R=Path(__file__).resolve().parent;P=R/'project';jar,=list((P/'build/libs').glob('*-vulkan-linux.jar'));crcs=[];classes={};modules=[]
def digest(b):return hashlib.sha256(b).hexdigest()
def audit(data,label):
 with zipfile.ZipFile(io.BytesIO(data))as z:
  assert z.testzip()is None;crcs.append(label)
  for n in z.namelist():
   assert '/validation/'not in n and 'buildcompat'not in n and not n.endswith(('CoreChecks.class','WaterPresentationChecks.class'))
   if n.startswith('META-INF/jarjar/')and n.endswith('.jar'):audit(z.read(n),n)
def compare(z,path):
 count=0
 for p in path.rglob('*.class'):assert z.read(str(p.relative_to(path)))==p.read_bytes();count+=1
 assert count;return count
audit(jar.read_bytes(),jar.name)
with zipfile.ZipFile(jar)as z:
 classes['main']=compare(z,P/'build/classes/java/main')
 for label,module in [('Core','engine-core'),('GraphicsSettings','graphics-settings'),('LOD','lod-engine'),('Performance','performance-engine'),('Vulkan','renderer-vulkan')]:
  n,=[n for n in z.namelist()if n.startswith('META-INF/jarjar/')and('DeepPrimal-'+label+'-')in n and n.endswith('.jar')];b=z.read(n)
  with zipfile.ZipFile(io.BytesIO(b))as nested:
   classes[module]=compare(nested,P/module/'build/classes/java/main')
   if module=='graphics-settings':
    for p in(P/module/'src/main/resources').rglob('*'):
     if p.is_file():assert nested.read(str(p.relative_to(P/module/'src/main/resources')))==p.read_bytes()
  standalones=[p for p in P.glob('*/build/libs/*.jar')if p.read_bytes()==b];assert len(standalones)==1;modules.append({'nested':n,'standalone':str(standalones[0].relative_to(P)),'bytes':len(b),'sha256':digest(b),'identical':True})
assert len(crcs)==6 and sum(classes.values())==411 and len(modules)==5
record={'pass':True,'status':'staged only; performance gate not accepted','archive_crcs':crcs,'compiled_class_identities':classes,'nested_standalone_modules':modules,'validation_private_agent_absent':True,'jar_bytes':jar.stat().st_size,'jar_sha256':digest(jar.read_bytes()),'installer_released':False,'source_sha256':{str(p.relative_to(P)):digest(p.read_bytes())for p in[P/'src/main/java/com/shnewbs/deepprimal/client/render/AnimatedWaterRenderer.java',P/'engine-core/src/main/java/com/shnewbs/deepprimal/core/WaterPresentation.java']}}
(R/'package-audit.json').write_text(json.dumps(record,indent=2)+'\n');print(json.dumps(record))
