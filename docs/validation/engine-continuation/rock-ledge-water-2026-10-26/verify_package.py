import hashlib,io,json,zipfile
from pathlib import Path
root=next(p for p in Path(__file__).resolve().parents if (p/'settings.gradle').is_file())
jars=list((root/'build/libs').glob('*dev.26-water.1-vulkan-windows.jar'))
assert len(jars)==1,jars
artifact=jars[0]
checks={'archive_count':0,'class_count':0,'native_dlls':0,'modules':[],'classes_match_compiler_output':{}}
all_names=[]
def scan(data,label):
 with zipfile.ZipFile(io.BytesIO(data)) as z:
  assert z.testzip() is None,label
  checks['archive_count']+=1
  names=z.namelist(); assert len(names)==len(set(names)),('duplicate',label)
  all_names.extend(label+'!'+n for n in names)
  if 'META-INF/neoforge.mods.toml' in names:
   import tomllib
   meta=tomllib.loads(z.read('META-INF/neoforge.mods.toml').decode())
   for mod in meta.get('mods',[]):
    expected_version='0.1.0-alpha.8' if mod['modId']=='deepprimal_themes' else '0.0.1-alpha.29-dev.26-water.1'
    assert mod['version']==expected_version,(label,mod)
    checks['modules'].append(mod['modId'])
  if 'META-INF/jarjar/metadata.json' in names:
   expected={v['path'] for v in json.loads(z.read('META-INF/jarjar/metadata.json'))['jars']}
   actual={n for n in names if n.startswith('META-INF/jarjar/') and n.endswith('.jar')}
   assert expected==actual,(label,expected^actual)
  for n in names:
   assert not n.endswith(('.so','.dylib')),(label,n)
   assert '/experimental/' not in n and '/prototype/' not in n,(label,n)
   assert not n.endswith(('EngineIntegrationTest.class','CoreChecks.class')),(label,n)
   assert not n.startswith('com/shnewbs/deepprimal/validation/'),(label,n)
   if n.endswith('.dll'):checks['native_dlls']+=1
   if n.endswith('.class'):checks['class_count']+=1
   if n.endswith('.jar'):scan(z.read(n),label+'!'+n)
scan(artifact.read_bytes(),artifact.name)
assert checks['native_dlls']>0
assert set(checks['modules'])=={'deepprimal','deepprimal_performance','deepprimal_graphics','deepprimal_themes','deepprimal_lod','deepprimal_vulkan'},checks['modules']
with zipfile.ZipFile(artifact) as outer:
 paths=outer.namelist()
 def match(module,prefix):
  nested=next(p for p in paths if p.startswith('META-INF/jarjar/') and prefix in p and p.endswith('.jar'))
  with zipfile.ZipFile(io.BytesIO(outer.read(nested))) as z:
   compare_classes(module,z)
 def compare_classes(module,z):
  classes=root/module/'build/classes/java/main' if module else root/'build/classes/java/main'
  disk={p.relative_to(classes).as_posix():p for p in classes.rglob('*.class')}
  archive={p for p in z.namelist() if p.endswith('.class') and p.startswith(('com/shnewbs/','net/vulkanmod/'))}
  assert set(disk)==archive,(module,'class set mismatch',sorted(set(disk)^archive)[:10])
  for name,path in disk.items():assert path.read_bytes()==z.read(name),(module,name)
  checks['classes_match_compiler_output'][module or 'host']=len(disk)
 compare_classes('',outer)
 for module,prefix in [('engine-core','DeepPrimal-Core-'),('graphics-settings','DeepPrimal-GraphicsSettings-'),('renderer-vulkan','DeepPrimal-Vulkan-'),('performance-engine','DeepPrimal-Performance-'),('lod-engine','DeepPrimal-LOD-'),('gui-themes','DeepPrimal-Themes-')]:match(module,prefix)
 for fragment in ['EngineCore.class','ChunkMaterializer.class','WorldGenPipeline.class','AdvancedDensityField.class','UpscalingPolicy.class','EngineRuntimeConfig.class','MainTargetScaleMixin.class','MainTargetResizeMixin.class']:
  assert any(n.endswith('/'+fragment) for n in all_names),fragment
 assert not any('/shaders/core/fsr_' in n or '/shaders/core/dlss' in n for n in all_names)
checks.update(artifact=artifact.name,size=artifact.stat().st_size,sha256=hashlib.file_digest(artifact.open('rb'),'sha256').hexdigest(),crc='pass',fixture_exclusion='pass',windows_payload='pass')
output=root/'build/water-ledge-package.json'
output.write_text(json.dumps(checks,indent=2)+'\n')
print(json.dumps(checks,indent=2))
