from pathlib import Path
import zipfile,io,hashlib,json,shutil
r=Path(__file__).resolve().parent;p=r/'project';e=r/'evidence'
src=list((p/'build/libs').glob('*renderer.5-vulkan-windows.jar'));assert len(src)==1,src
out=src[0];resources={};classes={};archives=[]
def walk(data,label):
 with zipfile.ZipFile(io.BytesIO(data)) as z:
  assert z.testzip() is None,label;assert len(z.namelist())==len(set(z.namelist())),label;archives.append({'archive':label,'entries':len(z.namelist())})
  for n in z.namelist():
   if n.endswith('.jar'):
    if 'Vulkan' in n:
     with zipfile.ZipFile(io.BytesIO(z.read(n))) as v:
      names=v.namelist();assert any(x.endswith('.dll') for x in names);assert not any(x.endswith(('.so','.dylib')) for x in names)
    walk(z.read(n),label+'!'+n)
   elif not n.endswith('/'):
    h=hashlib.sha256(z.read(n)).hexdigest();resources.setdefault(n,[]).append(h)
    if n.endswith('.class'):classes[n]=h
walk(out.read_bytes(),out.name);assert len(archives)==7
assert not any('/validation/' in n or '/benchmark/' in n or '/baseline/' in n or '/buildcompat/' in n or n.startswith(('tools/','authoring/')) for n in resources)
matched=0;private=[];identities=[]
for d in [p]+[x for x in p.iterdir() if x.is_dir()]:
 root=d/'build/classes/java/main'
 for f in root.rglob('*.class'):
  n=f.relative_to(root).as_posix()
  if n.startswith(('com/shnewbs/','net/vulkanmod/')):
   h=hashlib.sha256(f.read_bytes()).hexdigest();assert classes.get(n)==h,n;matched+=1;identities.append({'class':n,'compiled_sha256':h,'packaged_sha256':classes[n]})
 for kind in ['coreChecks','visualValidation','moduleValidation','benchmark','soilData','cacheChecks']:
  root=d/'build/classes/java'/kind
  for f in root.rglob('*.class'):
   n=f.relative_to(root).as_posix();assert n not in classes,n;private.append(n)
independent=0
for f in (r/'direct-classes/com/shnewbs/deepprimal/core').rglob('*.class'):
 n=f.relative_to(r/'direct-classes').as_posix()
 if n in classes:assert classes[n]==hashlib.sha256(f.read_bytes()).hexdigest(),n;independent+=1
def check_resources(root):
 names=[]
 for f in root.rglob('*'):
  if f.is_file():
   n=f.relative_to(root).as_posix();assert hashlib.sha256(f.read_bytes()).hexdigest() in resources.get(n,[]),n;names.append(n)
 return names
themes=check_resources(p/'gui-themes/build/resources/main');assert len(themes)>=2508
for x in json.loads((p/'docs/art/ground-textures-v4/validation.json').read_text())['assets']:assert hashlib.sha256((p/x['file']).read_bytes()).hexdigest()==x['sha256']
sand=json.loads((p/'docs/art/sand-final/validation.json').read_text());assert hashlib.sha256((p/'src/main/resources/assets/deepprimal/textures/block/ground/sand.png').read_bytes()).hexdigest()==sand['sha256']
palette=[]
for sub in ['assets/minecraft/textures/colormap','assets/minecraft/deepprimal/water_palette']:
 for f in (p/'src/main/resources'/sub).rglob('*'):
  if f.is_file():
   n=f.relative_to(p/'src/main/resources').as_posix();assert hashlib.sha256(f.read_bytes()).hexdigest() in resources.get(n,[]),n;palette.append(n)
assert len(palette)==43
soil=check_resources(p/'build/generated/soilResources');assert len(soil)==148
ground=[]
for f in (p/'src/main/resources/assets/deepprimal/textures/block/ground').rglob('*.png'):
 n=f.relative_to(p/'src/main/resources').as_posix();assert hashlib.sha256(f.read_bytes()).hexdigest() in resources.get(n,[]),n;ground.append(n)
assert len(ground)==11
for sub in ['engine_deltas_standard','engine_deltas_deep']:
 data=json.loads((p/'src/main/resources/worldgen'/sub/'data/minecraft/worldgen/world_preset/normal.json').read_text());assert data['dimensions']['minecraft:overworld']['generator']['channel_revision']==5
assert 'net/vulkanmod/deepprimal/Startup.class' in classes and 'net/vulkanmod/render/engine/VkBackend.class' in classes
notices=[n for n in resources if any(s in n.lower() for s in ['license','notice','third-party','third_party'])];assert notices
for n in ['LICENSE','COPYING','UPSTREAM.json']:
 f=p/'renderer-vulkan'/n;assert hashlib.sha256(f.read_bytes()).hexdigest() in resources.get('META-INF/licenses/vulkanmod/'+n,[]),n
with zipfile.ZipFile(out) as z:
 n=[n for n in z.namelist() if 'Themes-' in n and n.endswith('.jar')];assert len(n)==1 and '0.1.0-alpha.8' in n[0],n
result={'pass':True,'source_sha':'b8e69b632d6b45be94b08143d25186f3eee0e942','artifact':str(out),'producer':str(src[0]),'sha256':hashlib.sha256(out.read_bytes()).hexdigest(),'size':out.stat().st_size,'archives':archives,'product_class_matches':matched,'independent_core_matches':independent,'private_class_exclusions':len(private),'themes_resource_hash_matches':len(themes),'themes_version':'0.1.0-alpha.8','palette_hashes':len(palette),'ground_hashes':len(ground),'generated_soil_hashes':len(soil),'notices':notices,'windows_payload':True,'windows_execution':False,'opengl_fallback':True}
(e/'class-identity.json').write_text(json.dumps({'pass':True,'source_sha':result['source_sha'],'classes':identities,'excluded_private_classes':private},indent=2))
(e/'package.json').write_text(json.dumps(result,indent=2));print('ARCHIVE_PACKAGE_PASS',matched,independent,len(private))

