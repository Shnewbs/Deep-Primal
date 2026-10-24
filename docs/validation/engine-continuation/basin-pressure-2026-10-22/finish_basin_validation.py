"""Sequential current-source gates. Never infer Minecraft acceptance from Gradle exit alone."""
from pathlib import Path
import hashlib, json, subprocess, sys

ROOT=Path(__file__).resolve().parent
PROJECT=ROOT/'project'; TOOL=ROOT/'toolchain'
expected_runtime='b86fd1b63f57c666ca9fac5ff2b8b74935fa1a5dae709f05f6270cdc34565d33'
expected_core='df59081de2c2456e179e8c36055b907a62461916b2e3fff5ab2db9a0bf3fd645'
def digest(path): return hashlib.sha256(path.read_bytes()).hexdigest()
def source_identity():
    assert digest(PROJECT/'src/main/java/com/shnewbs/deepprimal/engine/WaterRuntime.java')==expected_runtime
    assert digest(PROJECT/'engine-core/src/main/java/com/shnewbs/deepprimal/core/WaterTransport.java')==expected_core
def passed(record):
    return record['explicit_pass'] and record['normal_save'] and not record['crash_reports'] and not record['failures']
records=json.loads((ROOT/'fifo-safety.json').read_text())
assert len(records)==17 and all(passed(r) for r in records), 'Safety matrix incomplete or failed'
source_identity()
subprocess.run([sys.executable,str(ROOT/'strengthen_idle_fixtures.py')],cwd=ROOT,check=True)
def native(name,mode,suite,packaged=False):
    source_identity()
    command=[sys.executable,str(ROOT/'run_native.py'),name,mode,'--suite',suite,'--no-trace']
    if packaged: command.append('--packaged')
    subprocess.run(command,cwd=ROOT,check=True)
    record=json.loads((ROOT/(name+'.json')).read_text())
    assert passed(record), 'Inspect native failure: '+name
    return record
if '--resume-build' not in sys.argv:
    for name,mode,suite in [('limited-transfer','transfer','drain-transfer'),
                           ('limited-loaded-idle','idle','drain-idle'),
                           ('basin-limited-b','basin-fixed','closed')]:
        print('START',name,flush=True); native(name,mode,suite)
command=[sys.executable,'tools/build_with_current_proxy.py','--java-home',str(TOOL/'jdk-25.0.4.1+1'),
    '--gradle',str(TOOL/'gradle-9.2.1/bin/gradle'),'--gradle-user-home',str(TOOL/'gradle-cache'),
    '--trust-store','/etc/ssl/certs/java/cacerts','--neoform-agent',str(TOOL/'checked-agent/neoform-process-fallback.jar'),
    '--','--no-daemon','--console=plain','--max-workers=2','-Dorg.gradle.jvmargs=-Xmx1G',
    '-Porg.gradle.java.installations.paths='+str(TOOL/'jdk-21.0.12.1+1')+','+str(TOOL/'jdk-25.0.4.1+1'),
    '-PwithoutVulkan','-Pmod_version=0.0.1-alpha.25-dev','--rerun-tasks','--no-build-cache','build']
print('START full default non-Vulkan build (uncached tasks)',flush=True)
with (ROOT/'limited-build.log').open('w') as log:
    result=subprocess.run(command,cwd=PROJECT,stdout=log,stderr=subprocess.STDOUT,timeout=480)
lines=(ROOT/'limited-build.log').read_text().splitlines()
(ROOT/'limited-core-records.txt').write_text('\n'.join(x for x in lines if '_PASS' in x or x.startswith('PASS:') or x.startswith('BUILD SUCCESSFUL'))+'\n')
assert result.returncode==0 and any('BUILD SUCCESSFUL' in x for x in lines), 'Full build failed'
source_identity()
subprocess.run([sys.executable,str(ROOT/'audit_package.py')],cwd=ROOT,check=True)
audit=json.loads((ROOT/'limited-build-audit.json').read_text())
packaged=[]
for name,mode,suite in [('limited-packaged-filling','filling','filling'),
                       ('limited-packaged-transfer','transfer','drain-transfer'),
                       ('limited-packaged-reload','reload','closed')]:
    print('START',name,flush=True); packaged.append(native(name,mode,suite,True))
jar,=list((PROJECT/'build/libs').glob('*.jar'))
assert digest(jar)==audit['jar_sha256'], 'Packaged tests changed the audited archive'
source_identity()
(ROOT/'package-post-native.json').write_text(json.dumps({
    'jar_sha256':digest(jar),'unchanged_after_native':True,'packaged_explicit_passes':all(passed(r) for r in packaged),
    'accepted_for_release':False,'reason':'Broader latency, residency, budgets and visual acceptance remain open'},indent=2)+'\n')
print('FINAL_BASIN_VALIDATION_COMPLETE',flush=True)
