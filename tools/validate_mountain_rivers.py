"""Native mountain-river checks. Requires a prepared Java 25 dev runtime and EULA acceptance."""
import argparse
import json
import os
from pathlib import Path
import subprocess
import time
import uuid

ROOT = Path(__file__).resolve().parents[1]

def run(launcher,case,offline,java_launch=None):
    family=case.removesuffix('-reopen')
    work=ROOT/('run-drainage-'+family+os.environ.get('DEEPPRIMAL_VALIDATION_SUFFIX',''))
    reopen=case.endswith('-reopen')
    if not reopen and (work/'world').exists():
        raise RuntimeError('Preserved existing test world: '+str(work))
    if reopen and not (work/'world/level.dat').exists():
        raise RuntimeError('Create the test save first: '+family)
    (work/'config').mkdir(parents=True,exist_ok=True)
    (work/'eula.txt').write_text('eula=true\n')
    preset='minecraft:normal'
    seed=947291 if family=='deep' else 452343
    (work/'server.properties').write_text(f'level-type={preset}\nlevel-seed={seed}\nserver-port=25587\nserver-ip=127.0.0.1\nonline-mode=false\nview-distance=4\nsimulation-distance=2\nmax-tick-time=-1\n')
    (work/'config/deepprimal-worldgen.toml').write_text('extraUndergroundDepth = '+str(family=='deep').lower()+'\nriversAndCoasts = '+str(case!='legacy').lower()+'\nmountainStreams = '+str(case!='alpha11').lower()+'\n')
    log=work/(case+'.log')
    events=work/'build'/('drainage-events-'+case+'-'+uuid.uuid4().hex+'.txt')
    with log.open('w') as output:
        env=os.environ.copy()
        env['DEEPPRIMAL_VALIDATION_CASE']=family
        env['DEEPPRIMAL_VALIDATION_RECORD']=str(events)
        if java_launch:
            spec=json.loads(java_launch.read_text())
            folders='deepprimal%%'+str(ROOT/'build/libs'/('DeepPrimal-26.1.2-'+next(l.split('=',1)[1] for l in (ROOT/'gradle.properties').read_text().splitlines() if l.startswith('mod_version='))+'.jar'))+os.pathsep+'deepprimal%%'+str(ROOT/'build/classes/java/visualValidation')
            jvm=[a for a in spec['jvm'] if not a.startswith('-Dfml.modFolders=')]
            command=[spec['java'],*jvm,'-Dfml.modFolders='+folders,'-cp',spec['classpath'],spec['main'],*spec['args']]
            env['MOD_CLASSES']=folders
            env['JAVA_TOOL_OPTIONS']='-Djavax.net.ssl.trustStore=/etc/ssl/certs/java/cacerts'
            directory=work
        else:
            command=launcher+['runServer','--console=plain','-PvisualValidation','-PpackagedValidation','-PrunDir='+str(work)]
            if (ROOT/'build/moddev/minecraft_assets.properties').exists():command.extend(['-x','downloadAssets'])
            if offline:command.append('--offline')
            directory=ROOT
        process=subprocess.Popen(command,cwd=directory,env=env,stdin=subprocess.DEVNULL,stdout=output,stderr=subprocess.STDOUT,text=True)
        def wait(marker,timeout=240):
            deadline=time.monotonic()+timeout
            while True:
                text=events.read_text(errors='replace') if events.exists() else ''
                console=log.read_text(errors='replace')
                if 'DRAINAGE_FAILURE' in text or 'AssertionError' in console or 'Exception executing command' in console:
                    raise RuntimeError('Native validation failed; inspect '+str(log))
                if marker in text:return text
                if process.poll() is not None or time.monotonic()>deadline:
                    raise RuntimeError('Missing '+marker+'; inspect '+str(log))
                time.sleep(.5)
        try:
            wait('DRAINAGE_SERVER_STARTED',360)
            print('DRAINAGE_SERVER_READY case='+case,flush=True)
            content=wait('DRAINAGE_AUTO_PASS',360)
            profile=[l for l in content.splitlines() if l.startswith('DRAINAGE_PROFILE')][-1]
            if content.count('DRAINAGE_AUTO_PASS') != 1:raise AssertionError('Stale or duplicate validation completion')
            expected='prototype' if family=='legacy' else 'waterways_v1' if family=='alpha11' else 'waterways_v2_deep' if family=='deep' else 'waterways_v2'
            if 'settings=deepprimal:'+expected+' ' not in profile:raise AssertionError(profile)
            snapshot=work/'profile.txt'
            if reopen:
                if snapshot.read_text()!=profile:raise AssertionError('Saved profile/height changed')
            else:snapshot.write_text(profile)
            process.wait(timeout=120)
            if process.returncode!=0:raise RuntimeError('Server exited '+str(process.returncode))
            print('DRAINAGE_RUNTIME_PASS case='+case+' saveExit=true',flush=True)
        finally:
            if process.poll() is None:
                process.terminate()
                try:process.wait(timeout=20)
                except subprocess.TimeoutExpired:process.kill();process.wait()

if __name__=='__main__':
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('launcher',nargs='*',default=['./gradlew'])
    parser.add_argument('--java-launch',type=Path)
    parser.add_argument('--accept-eula',action='store_true')
    parser.add_argument('--offline',action='store_true')
    parser.add_argument('--cases',nargs='+',choices=['standard','deep','legacy','legacy-reopen','alpha11','alpha11-reopen'],default=['standard','deep','legacy','legacy-reopen','alpha11','alpha11-reopen'])
    args=parser.parse_args()
    if not args.accept_eula:parser.error('Accept the Minecraft EULA before running this fixture')
    for case in args.cases:run(args.launcher,case,args.offline,args.java_launch)
