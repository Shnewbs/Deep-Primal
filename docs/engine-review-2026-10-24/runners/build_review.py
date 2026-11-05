from pathlib import Path
import sys,subprocess,json,time
r=Path(__file__).resolve().parent;p=r/'engine-review';t=r/'toolchain';e=r/'review-evidence';e.mkdir(exist_ok=True)
cmd=[sys.executable,'tools/build_with_current_proxy.py','--java-home',str(t/'jdk-25.0.4.1+1'),'--gradle',str(t/'gradle-9.2.1/bin/gradle'),'--gradle-user-home',str(t/'gradle-cache'),'--trust-store','/etc/ssl/certs/java/cacerts','--neoform-agent',str(t/'checked-agent/neoform-process-fallback.jar'),'--','-I',str(r/'fresh-compile.gradle'),'--offline','--no-daemon','--no-build-cache','--console=plain','--max-workers=2','-Dorg.gradle.jvmargs=-Xmx1G','-Porg.gradle.java.installations.paths='+str(t/'jdk-21.0.12.1+1')+','+str(t/'jdk-25.0.4.1+1'),'-PvulkanPlatform=linux',':build',':visualValidationClasses',':noiseBenchmarkClasses']
(e/'build-command.json').write_text(json.dumps(cmd,indent=2));start=time.monotonic()
proc=subprocess.run(cmd,cwd=p,stdout=subprocess.PIPE,stderr=subprocess.STDOUT)
(e/'build.log').write_bytes(proc.stdout)
print('Build exit',proc.returncode,'seconds',time.monotonic()-start)
print(proc.stdout.decode(errors='replace')[-6000:]);sys.exit(proc.returncode)
