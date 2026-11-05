from pathlib import Path
import subprocess,sys,json,hashlib,gzip,datetime,time
r=Path(__file__).resolve().parent;p=r/'project';t=r/'toolchain'
cmd=[sys.executable,'tools/build_with_current_proxy.py','--java-home',str(t/'jdk-25.0.4.1+1'),'--gradle',str(t/'gradle-9.2.1/bin/gradle'),'--gradle-user-home',str(t/'gradle-cache'),'--trust-store','/etc/ssl/certs/java/cacerts','--neoform-agent',str(t/'checked-agent/neoform-process-fallback.jar'),'--','-I',str(r/'fresh-compile.gradle'),'--offline','--no-daemon','--no-build-cache','--console=plain','--max-workers=2','-Dorg.gradle.jvmargs=-Xmx1G','-Porg.gradle.java.installations.paths='+str(t/'jdk-21.0.12.1+1')+','+str(t/'jdk-25.0.4.1+1'),'-PvulkanPlatform=linux','--rerun-tasks',':build']
start=datetime.datetime.now(datetime.timezone.utc).isoformat()
with (r/'build.log').open('wb') as f: result=subprocess.run(cmd,cwd=p,stdout=f,stderr=subprocess.STDOUT,timeout=600)
raw=(r/'build.log').read_bytes();(r/'build.log.gz').write_bytes(gzip.compress(raw,mtime=0));text=raw.decode(errors='replace')
required=['DEPOSIT_FIELD_CORE_PASS cases=90960', 'BUILD SUCCESSFUL','31 actionable tasks: 31 executed','WATER_RETIREMENT_STATE_CORE_PASS cases=60000','WATER_BUDGET_PASS','TERRAIN_V4_PASS','HYDROLOGY_PASS','WATERSHED_CORE_PASS']
d={'exit_code':result.returncode,'started_utc':start,'finished_utc':datetime.datetime.now(datetime.timezone.utc).isoformat(),'command':cmd,'raw_sha256':hashlib.sha256(raw).hexdigest(),'required':{x:x in text for x in required}}
d['pass']=result.returncode==0 and all(d['required'].values());(r/'build-result.json').write_text(json.dumps(d,indent=2)+'\n');print(json.dumps(d));raise SystemExit(0 if d['pass'] else 1)
