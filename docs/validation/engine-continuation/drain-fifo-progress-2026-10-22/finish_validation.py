"""Run only after the sequential native safety batch has exited."""
from pathlib import Path
import hashlib
import json
import shutil
import subprocess
import sys

ROOT=Path(__file__).resolve().parent
PROJECT=ROOT/'project';TOOL=ROOT/'toolchain'
records=json.loads((ROOT/'fifo-safety.json').read_text())
assert len(records)==17 and all(r['explicit_pass'] and r['normal_save'] and not r['crash_reports'] for r in records)
def classes():
    base=PROJECT/'build/classes/java/main'
    return {p.relative_to(base).as_posix():hashlib.sha256(p.read_bytes()).hexdigest()
        for p in base.rglob('*.class')}
before_classes=classes()
old=(ROOT/'fifo-WaterRuntime.java').read_text()
new=old.replace('the active FIFO. Completed missing-chunk waits retain their normal index.',
    'the drain-request FIFO. Completed missing-chunk waits retain their normal index.')
assert old!=new and len(old.splitlines())==len(new.splitlines())
generator=ROOT/'prepare_fifo_candidate.py'
s=generator.read_text().replace('assert s!=before',
    's=s.replace("the active FIFO. Completed missing-chunk waits retain their normal index.", "the drain-request FIFO. Completed missing-chunk waits retain their normal index.")\nassert s!=before')
generator.write_text(s)
subprocess.run([sys.executable,str(generator)],cwd=ROOT,check=True)
assert (ROOT/'fifo-WaterRuntime.java').read_text()==new
shutil.copyfile(ROOT/'DrainIdleChecks.java',PROJECT/'src/visualValidation/java/com/shnewbs/deepprimal/validation/DrainIdleChecks.java')
runner=ROOT/'run_native.py';s=runner.read_text().replace("'drain-queue'],default=", "'drain-queue','drain-idle'],default=")
s=s.replace("elif args.suite=='drain-queue':", "elif args.suite=='drain-idle':\n env['DEEPPRIMAL_DRAIN_IDLE_RECORD']=record_path;marker='DRAIN_IDLE_NATIVE_PASS'\nelif args.suite=='drain-queue':",1)
runner.write_text(s)
subprocess.run([sys.executable,str(runner),'fifo-loaded-idle','idle','--suite','drain-idle','--no-trace'],cwd=ROOT,check=True)
command=[sys.executable,'tools/build_with_current_proxy.py','--java-home',str(TOOL/'jdk-25.0.4.1+1'),
    '--gradle',str(TOOL/'gradle-9.2.1/bin/gradle'),'--gradle-user-home',str(TOOL/'gradle-cache'),
    '--trust-store','/etc/ssl/certs/java/cacerts','--neoform-agent',str(TOOL/'checked-agent/neoform-process-fallback.jar'),
    '--','--no-daemon','--console=plain','--max-workers=2','-Dorg.gradle.jvmargs=-Xmx1G',
    '-Porg.gradle.java.installations.paths='+str(TOOL/'jdk-21.0.12.1+1')+','+str(TOOL/'jdk-25.0.4.1+1'),
    '-PwithoutVulkan','-Pmod_version=0.0.1-alpha.25-dev','build']
with (ROOT/'fifo-build.log').open('w') as log:
    result=subprocess.run(command,cwd=PROJECT,stdout=log,stderr=subprocess.STDOUT,timeout=480)
assert result.returncode==0,'Full build failed; inspect fifo-build.log'
after_classes=classes()
assert before_classes==after_classes,'Comment-only normalization must preserve all compiled main-class bytes'
(ROOT/'canonical-source-identity.json').write_text(json.dumps({
    'safety_batch_source_sha256':hashlib.sha256(old.encode()).hexdigest(),
    'canonical_source_sha256':hashlib.sha256(new.encode()).hexdigest(),
    'change':'one comment says drain-request FIFO instead of active FIFO; no source body or line-count change',
    'compiled_main_class_digests_equal':True,'matched_classes':len(after_classes)},indent=2)+'\n')
lines=(ROOT/'fifo-build.log').read_text().splitlines()
(ROOT/'fifo-core-records.txt').write_text('\n'.join(x for x in lines if '_PASS' in x or x.startswith('PASS:') or x.startswith('BUILD SUCCESSFUL'))+'\n')
subprocess.run([sys.executable,str(ROOT/'audit_package.py')],cwd=ROOT,check=True)
audit=json.loads((ROOT/'fifo-build-audit.json').read_text())
packaged=[]
for name,mode,suite in [('fifo-packaged-filling','filling','filling'),('fifo-packaged-reload','reload','closed')]:
    subprocess.run([sys.executable,str(runner),name,mode,'--suite',suite,'--packaged','--no-trace'],cwd=ROOT,check=True)
    packaged.append(json.loads((ROOT/(name+'.json')).read_text()))
jar,=list((PROJECT/'build/libs').glob('*.jar'))
digest=hashlib.sha256(jar.read_bytes()).hexdigest()
assert digest==audit['jar_sha256'],'Packaged tests must not change audited archive'
(ROOT/'package-post-native.json').write_text(json.dumps({'jar_sha256':digest,'unchanged_after_native':True,
    'packaged_explicit_passes':all(r['explicit_pass'] and r['normal_save'] and not r['crash_reports'] for r in packaged),
    'accepted_for_release':False,'reason':'Known 3200-tick basin settling failure remains'},indent=2)+'\n')
print('FINAL_VALIDATION_COMPLETE',flush=True)
