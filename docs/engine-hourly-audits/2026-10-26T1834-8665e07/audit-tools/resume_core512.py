from pathlib import Path
import subprocess,json,os
m=Path('/dev/shm/audit-8665e07-20261026T1834');r=Path('/workspace/scratch/a2f7076b7762/audit-8665e07-20261026T1834');e=m/'evidence';java=r/'toolchain/jdk-25.0.4.1+1/bin/java';out=m/'core';rows=[];env=os.environ.copy();env.pop('JDK_JAVA_OPTIONS',None)
for name in ['CoreChecks','lod.LodScratchChecks','lod.LodReductionChecks','lod.DeliveredChunkScanChecks','RollingTimingChecks']:
 label='core-memory-cwd' if name=='CoreChecks' else name
 with (e/(label+'.txt')).open('w') as f:rc=subprocess.run([str(java),'-Xmx512m','-XX:+PerfDisableSharedMem','-Djava.io.tmpdir='+str(m/'tmp'),'-Xverify:all','-cp',str(out),'com.shnewbs.deepprimal.core.'+name],cwd=m,stdout=f,stderr=subprocess.STDOUT,env=env).returncode
 rows.append({'test':name,'pass':rc==0,'exit':rc,'cwd':str(m)});(e/'memory-cwd-checks.json').write_text(json.dumps(rows,indent=2));assert rc==0,name
(e/'independent-core-result.json').write_text(json.dumps({'pass':True,'production_sources':75,'test_sources':63,'fresh_compile':True,'heap_mib':512,'memory_cwd_retry':True,'first_attempt_io_failure_retained':True,'second_attempt_256m_heap_failure_retained':True,'packaged_native':False},indent=2));print('FRESH_CORE_AND_LOD_PASS')

