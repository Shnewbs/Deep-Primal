"""Validate only the independent accepted-solver ownership fix, sequentially."""
from pathlib import Path
import hashlib,json,shutil,subprocess,sys
ROOT=Path(__file__).resolve().parent;PROJECT=ROOT/'project';TOOL=ROOT/'toolchain'
expected='9206ab0b23c6393d084fdf216d97dfed7c47aeb55b718335ae7959c40201a981'
before=json.loads((ROOT/'admission-accepted-before.json').read_text())
assert not before['explicit_pass'] and before['crash_reports'],'Accepted baseline must reproduce the native defect'
crash=(PROJECT/'run-admission-accepted-before/crash-reports'/before['crash_reports'][0]).read_text()
assert 'sameJob=false orphanJobs=1 watchedJobsAfterFinish=1 total=4104 conserved=true' in crash
shutil.copyfile(ROOT/'accepted-cleanup-WaterRuntime.java',PROJECT/'src/main/java/com/shnewbs/deepprimal/engine/WaterRuntime.java')
records=[];rejected=[]
def passed(r):return r['explicit_pass'] and r['normal_save'] and not r['failures'] and not r['crash_reports']
def native(name,mode,suite,reuse=None,packaged=False):
    source=PROJECT/'src/main/java/com/shnewbs/deepprimal/engine/WaterRuntime.java'
    assert hashlib.sha256(source.read_bytes()).hexdigest()==expected
    for attempt in range(2):
        case=name if attempt==0 else name+'-capture-retry'
        command=[sys.executable,str(ROOT/'run_native.py'),case,mode,'--suite',suite,'--no-trace']
        if reuse:command+=['--reuse',reuse]
        if packaged:command+=['--packaged']
        print('START',case,flush=True);subprocess.run(command,cwd=ROOT,check=True)
        report=json.loads((ROOT/(case+'.json')).read_text())
        if passed(report):
            records.append(report);(ROOT/'cleanup-native-results.json').write_text(json.dumps(records,indent=2)+'\n');return report
        rejected.append(report);(ROOT/'rejected-captures.json').write_text(json.dumps(rejected,indent=2)+'\n')
        # One fresh capture retry is permitted only without a native/build failure.
        directory=PROJECT/(reuse or 'run-'+case);native_log=directory/'logs/latest.log'
        text=native_log.read_text() if native_log.exists() else ''
        assert report['exit_code']==0 and not report['failures'] and not report['crash_reports'] \
            and not any(word in text for word in ['AssertionError','Exception in server tick loop','Encountered an unexpected exception']), 'Stopped on real failed gate: '+case
    raise AssertionError('Repeated incomplete capture: '+name)
native('cleanup-admission','admission','pressure-admission')
native('cleanup-filling','filling','filling')
for mode in ['fixed','heads','edit','unload','idle']:
    report=native('cleanup-pressure-'+mode,mode,'long-pressure')
    if mode=='idle':idle_world='run-'+report['case']
native('cleanup-pressure-reopen','reopen','long-pressure',idle_world)
native('cleanup-short-pressure','pressure','pressure')
native('cleanup-active-queue','queue','queue')
assert len(records)==10
command=[sys.executable,'tools/build_with_current_proxy.py','--java-home',str(TOOL/'jdk-25.0.4.1+1'),
 '--gradle',str(TOOL/'gradle-9.2.1/bin/gradle'),'--gradle-user-home',str(TOOL/'gradle-cache'),
 '--trust-store','/etc/ssl/certs/java/cacerts','--neoform-agent',str(TOOL/'checked-agent/neoform-process-fallback.jar'),
 '--','--no-daemon','--console=plain','--max-workers=2','-Dorg.gradle.jvmargs=-Xmx1G',
 '-Porg.gradle.java.installations.paths='+str(TOOL/'jdk-21.0.12.1+1')+','+str(TOOL/'jdk-25.0.4.1+1'),
 '-PwithoutVulkan','-Pmod_version=0.0.1-alpha.25-dev','--no-build-cache','--rerun-tasks','build']
print('START full uncached non-Vulkan build',flush=True)
with (ROOT/'cleanup-build.log').open('w') as log:result=subprocess.run(command,cwd=PROJECT,stdout=log,stderr=subprocess.STDOUT,timeout=480)
lines=(ROOT/'cleanup-build.log').read_text().splitlines()
assert result.returncode==0 and any('BUILD SUCCESSFUL' in s for s in lines),'Inspect full build failure'
(ROOT/'cleanup-core-records.txt').write_text('\n'.join(s for s in lines if '_PASS' in s or s.startswith(('PASS:','BUILD SUCCESSFUL')))+'\n')
subprocess.run([sys.executable,str(ROOT/'audit_cleanup_package.py')],cwd=ROOT,check=True)
audit=json.loads((ROOT/'cleanup-build-audit.json').read_text())
native('cleanup-packaged-admission','admission','pressure-admission',packaged=True)
native('cleanup-packaged-filling','filling','filling',packaged=True)
native('cleanup-packaged-pressure','fixed','long-pressure',packaged=True)
jar,=list((PROJECT/'build/libs').glob('*.jar'))
assert hashlib.sha256(jar.read_bytes()).hexdigest()==audit['jar_sha256']
(ROOT/'cleanup-package-post-native.json').write_text(json.dumps({'unchanged_after_native':True,
 'jar_sha256':audit['jar_sha256'],'accepted_solver_native_passes':len(records),
 'installer_released':False,'full_engine_acceptance':False},indent=2)+'\n')
print('GUARD_ACCEPTED_SOURCE_VALIDATION_COMPLETE cases='+str(len(records)),flush=True)
