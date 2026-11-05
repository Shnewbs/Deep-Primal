from pathlib import Path
import os,subprocess,json,time
base=Path('/workspace/scratch/c748d58856d9');r=Path('/dev/shm/dp-renderer-admission-gate-c748d588');jdk=base/'renderer-toolchain/jdk-25.0.4.1+1'
cmd=[str(base/'renderer-toolchain/gradle-9.2.1/bin/gradle'),'--offline','--no-daemon','--no-build-cache','--max-workers=1','--console=plain','-g',str(r/'gradle-cache'),'--project-cache-dir',str(r/'project-cache'),'-Dorg.gradle.jvmargs=-Xmx256m -XX:-UsePerfData -Djava.io.tmpdir='+str(r/'tmp'),'-Dorg.gradle.java.installations.auto-detect=false','-Dorg.gradle.java.installations.auto-download=false','-Dorg.gradle.java.installations.paths='+str(jdk),':check']
env={**os.environ,'JAVA_HOME':str(jdk),'TMPDIR':str(r/'tmp'),'JDK_JAVA_OPTIONS':'-XX:-UsePerfData -Xmx384m -Djava.io.tmpdir='+str(r/'tmp')};start=time.monotonic()
with (r/'evidence/gradle-gate.txt').open('w') as log: rc=subprocess.run(cmd,cwd=r/'harness',env=env,stdout=log,stderr=subprocess.STDOUT).returncode
(r/'evidence/gradle-gate.json').write_text(json.dumps({'command':cmd,'exit':rc,'seconds':time.monotonic()-start,'scope':'Targeted harness executes exact candidate lod-engine/build.gradle with all actual candidate LOD module Java sources and prior qualified core/graphics plus real game API dependencies; root NeoForge/full distribution is NOT built.'},indent=2));print('gate exit',rc);raise SystemExit(rc)
