from pathlib import Path
import zipfile,io,hashlib,json,subprocess
r=Path(__file__).resolve().parent;p=r/'project';e=r/'evidence';jar=p/'build/libs/DeepPrimal-26.1.2-0.0.1-alpha.27-vulkan-linux.jar';raw=jar.read_bytes();z=zipfile.ZipFile(io.BytesIO(raw));assert z.testzip() is None
checks=[('root',z,p/'build/classes/java/main')];modules={'Core':'engine-core','GraphicsSettings':'graphics-settings','LOD':'lod-engine','Performance':'performance-engine','Themes':'gui-themes','Vulkan':'renderer-vulkan'};core=None
for name,module in modules.items():
 entry,=[x for x in z.namelist() if x.startswith('META-INF/jarjar/') and '.DeepPrimal-'+name+'-' in x and x.endswith('.jar')];b=z.read(entry);nested=zipfile.ZipFile(io.BytesIO(b));assert nested.testzip() is None;checks.append((module,nested,p/module/'build/classes/java/main'))
 if name=='Core':core=r/'packaged-core.jar';core.write_bytes(b)
private={str(f.relative_to(p/'build/classes/java/coreChecks')) for f in (p/'build/classes/java/coreChecks').rglob('*.class')};count=0
for name,archive,folder in checks:
 assert not(private&set(archive.namelist())),name
 for f in folder.rglob('*.class'):assert archive.read(str(f.relative_to(folder)))==f.read_bytes(),str(f);count+=1
manifest=json.loads((e/'independent-core.json').read_text());expected={n:v for n,v in manifest['classes'].items() if n.endswith('/LodStore.class') or n.endswith('/LodDiskIndex.class') or n.endswith('/LodIndexRequest.class')}
(r/'expected-loaded.json').write_text(json.dumps(expected));result={'pass':True,'jar_sha256':hashlib.sha256(raw).hexdigest(),'bytes':len(raw),'crc_archives':len(checks),'product_class_matches':count,'private_class_paths_excluded':len(private),'embedded_core_sha256':hashlib.sha256(core.read_bytes()).hexdigest()};(e/'package-audit.json').write_text(json.dumps(result,indent=2)+'\n');print(json.dumps(result))
