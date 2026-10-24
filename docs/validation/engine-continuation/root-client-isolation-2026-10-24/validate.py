from pathlib import Path
import subprocess, json, hashlib, gzip, datetime, sys

root=Path(__file__).resolve().parent; project=root/'project'; t=root/'toolchain'
common=[sys.executable,str(project/'tools/build_with_current_proxy.py'),'--java-home',str(t/'jdk-25.0.4.1+1'),'--gradle',str(t/'gradle-9.2.1/bin/gradle'),'--gradle-user-home',str(t/'gradle-cache'),'--project',str(project),'--trust-store','/etc/ssl/certs/java/cacerts','--neoform-agent',str(t/'checked-agent/neoform-process-fallback.jar')]
gradle=['--offline','--no-daemon','--no-build-cache','--console=plain','--max-workers=2','-Dorg.gradle.jvmargs=-Xmx1G','-Porg.gradle.java.installations.paths='+str(t/'jdk-21.0.12.1+1')+','+str(t/'jdk-25.0.4.1+1'),'-PvulkanPlatform=linux','-PthemeValidation','--dry-run']
records=[]
cases=[('unqualified-control',[],['runClient'],[':runClient SKIPPED',':gui-themes:runClient SKIPPED']),('root-client',['--root-game-task','runClient'],[],[':runClient SKIPPED']),('standalone-themes',[],[':gui-themes:runClient'],[':gui-themes:runClient SKIPPED'])]
for name,options,tasks,expected in cases:
    cmd=common+options+['--']+gradle+tasks
    p=subprocess.run(cmd,capture_output=True,timeout=180); raw=p.stdout+p.stderr
    (root/(name+'.log.gz')).write_bytes(gzip.compress(raw,mtime=0))
    text=raw.decode(errors='replace'); selected=[line.strip() for line in text.splitlines() if line.endswith(':runClient SKIPPED')]
    record={'case':name,'exit_code':p.returncode,'selected_game_tasks':selected,'expected':expected,'raw_sha256':hashlib.sha256(raw).hexdigest(),'command':cmd,'pass':p.returncode==0 and set(selected)==set(expected)}
    records.append(record);(root/'results.json').write_text(json.dumps(records,indent=2)+'\n');print(json.dumps(record),flush=True)
    if not record['pass']:raise SystemExit(1)
for task in ['runClient',':runClient',':gui-themes:runClient',':runServer']:
    p=subprocess.run(common+['--root-game-task','runClient','--',task],capture_output=True,timeout=10)
    assert p.returncode==2 and b'cannot be combined' in p.stderr, task
records.append({'case':'four-conflicting-selectors','pass':True,'exit_codes':[2]*4})
(root/'results.json').write_text(json.dumps(records,indent=2)+'\n')
print('ROOT_GAME_TASK_ISOLATION_PASS real_gradle_graphs=3 rejected_conflicts=4')
