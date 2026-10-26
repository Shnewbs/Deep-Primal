from pathlib import Path
import json,subprocess,os,time
r=Path(__file__).resolve().parent;p=r.parent/'renderer-work';t=r.parent/'renderer-toolchain';e=r/'evidence';tmp=r/'tmp'
cmd=['python3',str(p/'tools/build_with_current_proxy.py'),'--java-home',str(t/'jdk-25.0.4.1+1'),'--gradle',str(t/'gradle-9.2.1/bin/gradle'),'--gradle-user-home',str(r/'gradle-cache'),'--trust-store','/etc/ssl/certs/java/cacerts','--neoform-agent',str(t/'checked-agent/neoform-process-fallback.jar'),'--','--no-daemon','--no-build-cache','--console=plain','--max-workers=1','-Dorg.gradle.jvmargs=-Xmx768m -Djava.io.tmpdir='+str(tmp),'-Porg.gradle.java.installations.paths='+str(t/'jdk-21.0.12.1+1')+','+str(t/'jdk-25.0.4.1+1'),'-PvulkanPlatform=windows','-I',str(r/'renderer-version.gradle'),'--stacktrace','--rerun-tasks',':build',':visualValidationClasses']
(e/'build-command.json').write_text(json.dumps(cmd,indent=2));env=os.environ.copy();env.update(JDK_JAVA_OPTIONS='-Xmx768m -Djava.io.tmpdir='+str(tmp),TMPDIR=str(tmp));start=time.monotonic()
with (e/'build.txt').open('w') as out:
 rc=subprocess.run(cmd,cwd=p,stdout=out,stderr=subprocess.STDOUT,env=env).returncode
(e/'build-result.json').write_text(json.dumps({'pass':rc==0,'exit':rc,'source_sha':'dba66620bc6af6282f721f2cc2ab49085d572657','elapsed_seconds':time.monotonic()-start,'forced_tasks':True},indent=2));raise SystemExit(rc)
