from pathlib import Path
import json,subprocess,os,time
r=Path(__file__).resolve().parent;p=r/'project';e=r/'evidence';t=r/'toolchain';tmp=r/'tmp';tmp.mkdir(exist_ok=True)
cmd=['python3',str(p/'tools/build_with_current_proxy.py'),'--java-home',str(t/'jdk-25.0.4.1+1'),'--gradle',str(t/'gradle-9.2.1/bin/gradle'),'--gradle-user-home',str(r/'gradle-cache'),'--trust-store','/etc/ssl/certs/java/cacerts','--neoform-agent',str(t/'checked-agent/neoform-process-fallback.jar'),'--','--no-daemon','--no-build-cache','--console=plain','--max-workers=1','-Dorg.gradle.jvmargs=-Xmx768m -Djava.io.tmpdir='+str(tmp),'-Porg.gradle.java.installations.paths='+str(t/'jdk-21.0.12.1+1')+','+str(t/'jdk-25.0.4.1+1'),'-PvulkanPlatform=windows','-I',str(r/'audit-version.gradle'),'--rerun-tasks',':build',':visualValidationClasses']
(e/'build-command.json').write_text(json.dumps(cmd,indent=2));env=os.environ.copy();env.update(JDK_JAVA_OPTIONS='-Xmx768m -Djava.io.tmpdir='+str(tmp),TMPDIR=str(tmp));start=time.monotonic()
with (e/'build-output.txt').open('w') as f:
 proc=subprocess.Popen(cmd,cwd=p,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,text=True,bufsize=1,env=env)
 for line in proc.stdout:f.write(line);f.flush();print(line,end='',flush=True)
 rc=proc.wait()
(e/'build-result.json').write_text(json.dumps({'pass':rc==0,'exit':rc,'source_sha':'a8491871875bf214cf4ad532c3ccc19e00f23804','elapsed_seconds':time.monotonic()-start,'fresh_product_compilation':True,'private_agent_verified':True},indent=2));raise SystemExit(rc)
