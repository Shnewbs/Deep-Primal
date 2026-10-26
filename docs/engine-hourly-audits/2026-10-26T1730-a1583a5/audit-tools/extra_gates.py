from pathlib import Path
import subprocess,json,time,hashlib
r=Path(__file__).resolve().parent;e=r/'evidence';out=r/'independent-core';java=r/'toolchain/jdk-25.0.4.1+1/bin/java'
while not (e/'independent-core-result.json').exists():time.sleep(2)
rows=[]
for name in ['lod.LodScratchChecks','lod.LodReductionChecks','RollingTimingChecks','lod.DeliveredChunkScanChecks']:
 with (e/(name+'-direct.txt')).open('w') as f:rc=subprocess.run([str(java),'-Xmx768m','-Xverify:all','-cp',str(out),'com.shnewbs.deepprimal.core.'+name],stdout=f,stderr=subprocess.STDOUT).returncode
 rows.append({'test':name,'direct':True,'exit':rc});assert rc==0,name
while not (r/'packaged-regressions/core.jar').exists():time.sleep(2)
core=r/'packaged-regressions/core.jar'
for name in ['lod.LodScratchChecks','lod.LodReductionChecks','RollingTimingChecks','lod.DeliveredChunkScanChecks']:
 with (e/(name+'-packaged.txt')).open('w') as f:rc=subprocess.run([str(java),'-Xmx768m','-Xverify:all','-cp',str(core)+':'+str(out),'com.shnewbs.deepprimal.core.'+name],stdout=f,stderr=subprocess.STDOUT).returncode
 rows.append({'test':name,'packaged':True,'exit':rc});assert rc==0,name
(e/'extra-gates.json').write_text(json.dumps({'pass':True,'tests':rows,'extracted_core_sha256':hashlib.sha256(core.read_bytes()).hexdigest(),'current_artifact':True},indent=2));print('EXTRA_SCRATCH_REDUCTION_TIMING_DIRECT_AND_PACKAGED_PASS')
