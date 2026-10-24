from pathlib import Path
import subprocess,sys,json,hashlib,gzip,time,datetime,os
R=Path(__file__).resolve().parent;P=Path(os.environ.get('DEEPPRIMAL_PROJECT_ROOT',str(R/'project')));T=R/'toolchain';O=Path(os.environ.get('DEEPPRIMAL_BUILD_CAPTURE_ROOT',str(R)));O.mkdir(parents=True,exist_ok=True)
cmd=[sys.executable,'tools/build_with_current_proxy.py','--java-home',str(T/'jdk-25.0.4.1+1'),'--gradle',str(T/'gradle-9.2.1/bin/gradle'),'--gradle-user-home',str(T/'gradle-cache'),'--trust-store','/etc/ssl/certs/java/cacerts','--neoform-agent',str(T/'checked-agent/neoform-process-fallback.jar'),'--','--no-daemon','--console=plain','--max-workers=2','-Dorg.gradle.jvmargs=-Xmx1G','-Porg.gradle.java.installations.paths='+str(T/'jdk-21.0.12.1+1')+','+str(T/'jdk-25.0.4.1+1'),'-PvulkanPlatform=linux','-Pmod_version=0.0.1-alpha.25-dev','--no-build-cache','--no-parallel','--rerun-tasks','build']
start=datetime.datetime.now(datetime.timezone.utc).isoformat();started=time.monotonic()
with (O/'candidate-build.log').open('wb') as out:
    try:r=subprocess.run(cmd,cwd=P,stdout=out,stderr=subprocess.STDOUT,timeout=600)
    except subprocess.TimeoutExpired:raise SystemExit('Candidate build exceeded 600-second bound')
observations=[];previous=None;stable=0
for sample in range(20):
    raw=(O/'candidate-build.log').read_bytes();digest=hashlib.sha256(raw).hexdigest();observations.append({'bytes':len(raw),'sha256':digest})
    stable=stable+1 if digest==previous else 0;previous=digest
    if stable>=4:break
    time.sleep(.25)
compressed=gzip.compress(raw,mtime=0);tmp=O/'candidate-build-final.gz.tmp';tmp.write_bytes(compressed);tmp.replace(O/'candidate-build-final.gz')
text=raw.decode();required=['WATER_CORNER_BULK_CORE_PASS cases=210000', 'WATER_FLOW_FIELD_CORE_PASS', 'BUILD SUCCESSFUL','CACHE_CHECKS_PASS','WATER_SURFACE_SHAPE_PASS','INTEGRATED_CORE_PASS','TERRAIN_V2_CORE_PASS','WATERSHED_CORE_PASS','WATER_DYNAMICS_PASS','HYDROLOGY_PASS','25 actionable tasks: 25 executed']
if (P/'src/coreChecks/java/com/shnewbs/deepprimal/core/WaterRetirementStateChecks.java').exists():required.insert(0,'WATER_RETIREMENT_STATE_CORE_PASS cases=60000')
if (P/'src/coreChecks/java/com/shnewbs/deepprimal/core/WaterCornerRetryChecks.java').exists():required.insert(0,'WATER_CORNER_RETRY_CORE_PASS cases=470000')
if (P/'engine-core/src/main/java/com/shnewbs/deepprimal/core/WaterBoundaryIndex.java').exists():required.insert(0,'WATER_BOUNDARY_INDEX_CORE_PASS cases=688129')
record={'command':cmd,'started_utc':start,'finished_utc':datetime.datetime.now(datetime.timezone.utc).isoformat(),'elapsed_seconds':round(time.monotonic()-started,2),'exit_code':r.returncode,'stable_samples':stable,'observations':observations,'raw_sha256':digest,'gzip_sha256':hashlib.sha256(compressed).hexdigest(),'required_records':{s:s in text for s in required},'pass':r.returncode==0 and stable>=4 and all(s in text for s in required)}
(O/'candidate-build-result.json').write_text(json.dumps(record,indent=2)+'\n');print(json.dumps(record,indent=2));raise SystemExit(0 if record['pass'] else 1)
