from pathlib import Path
import subprocess,hashlib,json,os
r=Path(__file__).resolve().parent.parent;e=r/'evidence';java=os.environ.get('JAVA','java');out=r/'classes';out.mkdir(exist_ok=True)
manifest=json.loads((e/'source-manifest.json').read_text())
for item in manifest['inputs']:
 b=(e/item['file']).read_bytes();assert hashlib.sha1(b'blob '+str(len(b)).encode()+b'\0'+b).hexdigest()==item['sha'],item['file']
s=(e/'VoxelLodRenderer.java').read_text();start=s.index('var keys = new ArrayList<Long>();');end=s.index('return keys;',start)+len('return keys;');body=s[start:end]
extracted='import java.nio.file.*;\nimport java.util.*;\nimport java.util.concurrent.*;\npublic class BaselineIndex {\n static final int MAX_DISK_KEYS=8192;\n static long key(int x,int z){return ((long)x<<32)^(z&0xffffffffL);}\n public static List<Long> scan(Path root)throws Exception {\n'+body+'\n}\n}\n'
assert extracted==(e/'BaselineIndex.java').read_text(),'extraction drift'
sources=[r/'staged/engine-core/src/main/java/com/shnewbs/deepprimal/core/lod/LodDiskIndex.java',e/'BaselineIndex.java',e/'IndexChecks.java',e/'ColumnarChunk.java',e/'LodStore.java']
before={str(p.relative_to(r)):hashlib.sha256(p.read_bytes()).hexdigest() for p in sources}
cmd=[java,'-m','jdk.compiler/com.sun.tools.javac.Main','-g','-d',str(out),*map(str,sources)]
a=subprocess.run(cmd,capture_output=True,text=True);(e/'compile.txt').write_text(a.stdout+a.stderr);assert a.returncode==0,a.stdout+a.stderr
run=[java,'-Xmx256m','-cp',str(out),'IndexChecks'];a=subprocess.run(run,capture_output=True,text=True);(e/'result.txt').write_text(a.stdout+a.stderr);assert a.returncode==0,a.stdout+a.stderr
assert 'LOD_PREFIX_BUG_REPRODUCED' in a.stdout and 'LOD_PAGED_INDEX_PASS checks=14303' in a.stdout
assert before=={str(p.relative_to(r)):hashlib.sha256(p.read_bytes()).hexdigest() for p in sources}
v=subprocess.run([java,'-version'],capture_output=True,text=True)
record={'pass':True,'parent':manifest['parent'],'java':v.stdout+v.stderr,'compile_command':cmd,'run_command':run,'source_sha256':before,'class_sha256':{str(p.relative_to(out)):hashlib.sha256(p.read_bytes()).hexdigest() for p in sorted(out.rglob('*.class'))},'result_sha256':hashlib.sha256((e/'result.txt').read_bytes()).hexdigest(),'full_engine_or_native_validation':False}
(e/'identity.json').write_text(json.dumps(record,indent=2)+'\n');print(a.stdout)
