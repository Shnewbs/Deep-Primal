from pathlib import Path
import zipfile,io,hashlib,json
r=Path(__file__).resolve().parent;p=r/'project';jar=p/'build/libs/DeepPrimal-26.1.2-0.0.1-alpha.27-vulkan-linux.jar';raw=jar.read_bytes();z=zipfile.ZipFile(io.BytesIO(raw));assert z.testzip() is None
modules={'Core':'engine-core','GraphicsSettings':'graphics-settings','LOD':'lod-engine','Performance':'performance-engine','Themes':'gui-themes','Vulkan':'renderer-vulkan'}
records=[];checks=[('root',z,p/'build/classes/java/main')]
for label,module in modules.items():
 name,=[n for n in z.namelist() if n.startswith('META-INF/jarjar/') and '.DeepPrimal-'+label+'-' in n and n.endswith('.jar')]
 b=z.read(name);a=zipfile.ZipFile(io.BytesIO(b));assert a.testzip() is None
 checks.append((module,a,p/module/'build/classes/java/main'))
 records.append({'module':module,'path':name,'sha256':hashlib.sha256(b).hexdigest()})
private=set()
for source_set in ['coreChecks','visualValidation']:
 folder=p/'build/classes/java'/source_set
 if folder.exists():private.update(f.relative_to(folder).as_posix() for f in folder.rglob('*.class'))
count=0
for module,a,classes in checks:
 assert not any('/validation/' in n or n.endswith('CoreChecks.class') for n in a.namelist())
 assert not (private & set(a.namelist())),str(private & set(a.namelist()))
 for file in classes.rglob('*.class'):
  assert a.read(file.relative_to(classes).as_posix())==file.read_bytes(),str(file);count+=1
result={'pass':True,'jar':jar.name,'bytes':len(raw),'sha256':hashlib.sha256(raw).hexdigest(),'root_and_nested_crcs':7,'compiled_class_matches':count,'nested':records,'private_validation_absent':True,'compiled_private_class_paths_excluded':len(private),'windows_execution':False}
(r/'package-audit.json').write_text(json.dumps(result,indent=2)+'\n');print(json.dumps(result))
