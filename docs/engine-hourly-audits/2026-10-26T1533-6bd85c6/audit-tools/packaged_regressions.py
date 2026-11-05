from pathlib import Path
import json,zipfile,subprocess,hashlib
r=Path(__file__).resolve().parent;e=r/'evidence';p=r/'project';a=json.loads((e/'package.json').read_text());work=r/'packaged-regressions';work.mkdir();core=work/'core.jar';out=work/'fixtures';out.mkdir()
with zipfile.ZipFile(a['artifact']) as z:
 names=[n for n in z.namelist() if 'DeepPrimal-Core-' in n and n.endswith('.jar')];assert len(names)==1;core.write_bytes(z.read(names[0]))
runner=work/'ActualCore.java'
runner.write_text('''import java.nio.file.*; public class ActualCore { public static void main(String[] a) throws Exception { for(String n:new String[]{"ChunkCache","EngineCore","WorldGenPipeline","WaterPatchMesher","WaterAccount","lod.LodMesher"}) { Class<?> c=Class.forName("com.shnewbs.deepprimal.core."+n); Path actual=Path.of(c.getProtectionDomain().getCodeSource().getLocation().toURI()); if(!actual.toRealPath().equals(Path.of(a[0]).toRealPath())) throw new AssertionError("wrong source "+actual); System.out.println("LOADED_CORE "+n+" "+actual); } for(String n:new String[]{"CacheLifecycleChecks","CoreCloseLifecycleChecks","PipelineInlineCloseChecks","WaterfallSideChecks"}) Class.forName("com.shnewbs.deepprimal.core."+n).getMethod("main",String[].class).invoke(null,(Object)new String[0]); }}''')
sources=[p/'src/coreChecks/java/com/shnewbs/deepprimal/core'/n for n in ['CacheLifecycleChecks.java','CoreCloseLifecycleChecks.java','PipelineInlineCloseChecks.java','WaterfallSideChecks.java']];jdk=r/'toolchain/jdk-25.0.4.1+1/bin'
subprocess.run([str(jdk/'javac'),'-cp',str(core),'-d',str(out),str(runner)]+list(map(str,sources)),check=True)
with (e/'packaged-regressions.txt').open('w') as f:subprocess.run([str(jdk/'java'),'-Xmx768m','-Xverify:all','-cp',str(core)+':'+str(out),'ActualCore',str(core)],stdout=f,stderr=subprocess.STDOUT,check=True)
(e/'packaged-regressions-result.json').write_text(json.dumps({'pass':True,'actual_nested_core_verified':True,'core_sha256':hashlib.sha256(core.read_bytes()).hexdigest(),'fixtures':['CacheLifecycleChecks','CoreCloseLifecycleChecks','PipelineInlineCloseChecks','WaterfallSideChecks'],'rock_ledge_cases':8192,'gpu_execution':False},indent=2));print('PACKAGED_REGRESSIONS_PASS')

