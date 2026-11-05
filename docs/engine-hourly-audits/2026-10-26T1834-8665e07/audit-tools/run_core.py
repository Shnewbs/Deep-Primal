from pathlib import Path
import subprocess,json,hashlib,time,os
r=Path('/workspace/scratch/a2f7076b7762/audit-8665e07-20261026T1834');m=Path('/dev/shm/audit-8665e07-20261026T1834');p=r/'project';e=m/'evidence';out=m/'core';out.mkdir();jdk=r/'toolchain/jdk-25.0.4.1+1/bin'
src=sorted((p/'engine-core/src/main/java').rglob('*.java'));tests=sorted((p/'src/coreChecks/java').rglob('*.java'));env=os.environ.copy();env.pop('JDK_JAVA_OPTIONS',None);rows=[]
def run(label,cmd):
 with (e/(label+'.txt')).open('w') as f:rc=subprocess.run(cmd,cwd=p,stdout=f,stderr=subprocess.STDOUT,env=env).returncode
 rows.append({'label':label,'pass':rc==0,'exit':rc});(e/'checks.json').write_text(json.dumps(rows,indent=2));assert rc==0,label
run('independent-compile',[str(jdk/'javac'),'-J-Xmx256m','-J-XX:+PerfDisableSharedMem','-J-Djava.io.tmpdir='+str(m/'tmp'),'-g','-d',str(out)]+list(map(str,src+tests)))
(e/'independent-compile.json').write_text(json.dumps({'pass':True,'production_sources':len(src),'test_sources':len(tests),'source_sha':'8665e07be705c9cf6470adf3d6a6d325cea094fc','heap_mib':256},indent=2))
run('independent-core',[str(jdk/'java'),'-Xmx256m','-XX:+PerfDisableSharedMem','-Djava.io.tmpdir='+str(m/'tmp'),'-Xverify:all','-cp',str(out),'com.shnewbs.deepprimal.core.CoreChecks'])
for name in ['lod.LodScratchChecks','lod.LodReductionChecks','lod.DeliveredChunkScanChecks','RollingTimingChecks']:run(name,[str(jdk/'java'),'-Xmx256m','-XX:+PerfDisableSharedMem','-Xverify:all','-cp',str(out),'com.shnewbs.deepprimal.core.'+name])
(e/'independent-core-result.json').write_text(json.dumps({'pass':True,'production_sources':len(src),'test_sources':len(tests),'source_sha':'8665e07be705c9cf6470adf3d6a6d325cea094fc','heap_mib':256,'all_fresh_direct_only':True},indent=2));print('FRESH_CORE_AND_LOD_PASS')

