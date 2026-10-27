from pathlib import Path
import zipfile,hashlib,json,shutil
r=Path(__file__).resolve().parent;p=r/'project';e=r/'evidence';b=json.loads((e/'build-result.json').read_text());assert b['pass'];jars=list((p/'build/libs').glob('*audit-20261027T0332-2e1aa76-vulkan-windows.jar'));assert len(jars)==1;jar=jars[0];assembly=p/'build/generated/jarJar-0.0.1-alpha.29-dev.31-audit-20261027T0332-2e1aa76-lod-vulkan-windows';rows=[]
with zipfile.ZipFile(jar) as z:
 assert z.testzip() is None
 for f in assembly.rglob('*.jar'):
  ns=[n for n in z.namelist() if n.endswith('/'+f.name)];assert len(ns)==1 and z.read(ns[0])==f.read_bytes();rows.append({'path':str(f),'bytes':f.stat().st_size,'sha256':hashlib.sha256(f.read_bytes()).hexdigest()})
assert len(rows)==6;shutil.rmtree(assembly);(e/'completed-assembly-cleanup.json').write_text(json.dumps({'pass':True,'matches':rows,'completed_producer_preserved':str(jar),'source_and_dependency_caches_untouched':True,'other_roles_untouched':True},indent=2));print('COMPLETED_DUPLICATE_ASSEMBLY_RELEASED')
