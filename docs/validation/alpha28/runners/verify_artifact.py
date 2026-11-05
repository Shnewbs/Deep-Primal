from pathlib import Path
import subprocess,hashlib,json,zipfile,io,sys
r=Path(__file__).resolve().parent;p=r/'project';platform=sys.argv[1] if len(sys.argv)>1 else 'linux'
jar=p/f'build/libs/DeepPrimal-26.1.2-0.0.1-alpha.28-vulkan-{platform}.jar'
independent=r/('independent-'+platform);independent.mkdir(exist_ok=True)
sources=sorted((p/'engine-core/src/main/java').rglob('*.java'))
subprocess.run([str(r/'toolchain/jdk-25.0.4.1+1/bin/javac'),'-g','-d',str(independent),*map(str,sources)],check=True)
matches=0
for f in independent.rglob('*.class'):
 q=p/'engine-core/build/classes/java/main'/f.relative_to(independent);assert q.read_bytes()==f.read_bytes(),str(q);matches+=1
payload={};archives=[]
def scan(b):
 with zipfile.ZipFile(io.BytesIO(b)) as z:
  assert z.testzip() is None;archives.append(len(b))
  for name in z.namelist():
   if name.endswith('.jar'):scan(z.read(name))
   elif name.endswith('.class'):payload[name]=hashlib.sha256(z.read(name)).hexdigest()
scan(jar.read_bytes());n=0;identities={}
for mod in ['', 'engine-core','performance-engine','graphics-settings','gui-themes','lod-engine','renderer-vulkan']:
 root=p/mod/'build/classes/java/main'
 for f in root.rglob('*.class'):
  key=f.relative_to(root).as_posix();digest=hashlib.sha256(f.read_bytes()).hexdigest();assert payload[key]==digest,key;identities[key]=digest;n+=1
private=0
for root in [p/'build/classes/java/visualValidation',p/'build/classes/java/coreChecks']:
 for f in root.rglob('*.class'):
  assert f.relative_to(root).as_posix() not in payload,str(f);private+=1
out={'pass':True,'core_sources':len(sources),'core_class_matches':matches,'packaged_class_matches':n,'archive_crcs':len(archives),'private_class_paths_excluded':private,'bytes':jar.stat().st_size,'sha256':hashlib.sha256(jar.read_bytes()).hexdigest(),'classes':identities}
(r/f'evidence/artifact-{platform}.json').write_text(json.dumps(out,indent=2));print({k:v for k,v in out.items() if k!='classes'})
