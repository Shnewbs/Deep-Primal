from pathlib import Path
import importlib.util,json,subprocess,os,time,hashlib,tempfile
r=Path('/dev/shm/dp-renderer-recovery-c748d588');basepath=Path('/workspace/scratch/c748d58856d9');p=basepath/'renderer-work';e=r/'evidence';a=json.loads((e/'package.json').read_text());jar=a['artifact'];spec=importlib.util.spec_from_file_location('smoke',p/'tools/server_smoke.py');sm=importlib.util.module_from_spec(spec);spec.loader.exec_module(sm)
assets=r/'server-unused-assets';assets.mkdir(exist_ok=True);f=p/'build/moddev/minecraft_assets.properties';f.parent.mkdir(parents=True,exist_ok=True);f.write_text('assets_root='+str(assets)+'\nasset_index=server-unused\n')
cache=r/'native-cache';cache.mkdir(exist_ok=True);base=json.loads((r/'baselines.json').read_text())
for optin in [False,True]:
 label='optin' if optin else 'default';work=Path(tempfile.mkdtemp(prefix='native-world-'+label+'-final-',dir=r));(e/(label+'-run-dir.txt')).write_text(str(work));(work/'.cache').symlink_to(cache,target_is_directory=True);(work/'config').mkdir();(work/'eula.txt').write_text('eula=true\n')
 (work/'server.properties').write_text('level-type=minecraft:normal\nlevel-seed=20261020\nlevel-name=world\nonline-mode=false\nserver-ip=127.0.0.1\nserver-port=26431\nenable-rcon=true\nrcon.port=26432\nrcon.password=deepprimal-local-smoke-only\nmax-tick-time=-1\nview-distance=2\nsimulation-distance=2\n')
 cmd=json.loads((e/'build-command.json').read_text());cmd[cmd.index('--gradle-user-home')+1]=str(r/'native-gradle-cache');cmd=[x for x in cmd if x!='--rerun-tasks'];cmd.insert(cmd.index('--'),'runServer');cmd.insert(cmd.index('runServer'),'--root-game-task');cmd=cmd[:cmd.index(':build')]+['-PpackagedValidation','-PvisualValidation','-PvalidationJar='+jar,'-PrunDir='+str(work),'-x','downloadAssets']
 cmd += ['-I',str(r/'native-memory.init.gradle'),'-x',':jarJar','--project-cache-dir',str(r/'project-cache'),'--offline']
 sessions=[]
 for i in range(2):
  if optin:(work/'config/deepprimal-worldgen.toml').write_text('riverDeltas = true\n' if i==0 else 'riverDeltas = false\n')
  env=os.environ.copy();env['JDK_JAVA_OPTIONS']='-XX:-UsePerfData -Xmx384m -Djava.io.tmpdir='+str(r/'tmp')+' -Djna.tmpdir='+str(r/'tmp');env.update(DEEPPRIMAL_DEPOSIT_RECORD=str(e/f'{label}-deposit-{i}.json'),DEEPPRIMAL_DEPOSIT_MODE='fresh' if i==0 else 'reopen',DEEPPRIMAL_SAVED_REVISION='7',DEEPPRIMAL_GROUND_MATERIAL_CHECK='fresh' if i==0 else 'reopen',DEEPPRIMAL_SOIL_WORLD_REVISION='2',DEEPPRIMAL_STREAM_SOURCE_NATIVE_CHECK='1',DEEPPRIMAL_DENSITY_CHUNK_NATIVE_CHECK='1',DEEPPRIMAL_NOISE_CODEC_CHECK='1',DEEPPRIMAL_AUDIT_RECORD=str(e/f'{label}-native-{i}.json'),DEEPPRIMAL_AUDIT_REV='7',DEEPPRIMAL_AUDIT_MODE='fresh' if i==0 else 'reopen')
  log=e/f'{label}-native-{i}.txt';markers=['TERRAIN_AUDIT_NATIVE_PASS','DEPOSIT_NATIVE_PASS','SOIL_WORLDGEN_PASS','STREAM_SOURCE_NATIVE_PASS']
  with log.open('w') as out:
   proc=subprocess.Popen(cmd,cwd=p,stdout=out,stderr=subprocess.STDOUT,env=env);deadline=time.monotonic()+480
   try:
    while not all(m in log.read_text(errors='replace') for m in markers+['RCON running on']):
     if proc.poll() is not None:raise RuntimeError('Own native process exited before acceptance: '+str(proc.returncode))
     if time.monotonic()>deadline:raise RuntimeError('Native startup deadline')
     time.sleep(1)
    record=json.loads((e/f'{label}-native-{i}.json').read_text());dep=json.loads((e/f'{label}-deposit-{i}.json').read_text());assert record['pass'] and dep['pass']
    if optin:assert record['generator']['channel_revision']==5
    rc=sm.Rcon(26432);ack=rc.command('save-all flush');assert 'Saved the game' in ack;rc.command('stop');rc.sock.close();proc.wait(timeout=90);sm.verify_server_shutdown(log.read_text(errors='replace'),proc.returncode,markers);assert proc.returncode==0
    for name,h in record['loaded_classes'].items():
     if '.validation.' not in name:
      f=p/('engine-core' if '.core.' in name else '')/'build/classes/java/main'/Path(name.replace('.','/')+'.class');assert hashlib.sha256(f.read_bytes()).hexdigest()==h,name
    sessions.append({'mode':'fresh' if i==0 else 'reopen','pass':True,'exit':proc.returncode,'saved_flush_acknowledged':True})
   finally:
    if proc.poll() is None:proc.terminate();proc.wait(timeout=30)
 assert json.loads((e/f'{label}-native-0.json').read_text())['hashes']==json.loads((e/f'{label}-native-1.json').read_text())['hashes']
 assert json.loads((e/f'{label}-deposit-0.json').read_text())['hashes']==json.loads((e/f'{label}-deposit-1.json').read_text())['hashes']
 if not optin:
  assert json.loads((e/f'{label}-native-0.json').read_text())['hashes']==base['native']
  assert json.loads((e/f'{label}-deposit-0.json').read_text())['hashes']==base['deposit']
 assert hashlib.sha256(Path(jar).read_bytes()).hexdigest()==a['sha256']
 (e/f'{label}-native-result.json').write_text(json.dumps({'pass':True,'platform':'Linux dedicated server; not Windows acceptance','sessions':sessions,'actual_saved_channel_revision':5 if optin else 2,'creation_toggle_disabled_before_reopen':optin,'replay_hashes_equal':True,'loaded_class_identity':True,'failure_precedence_and_ordered_shutdown':True,'default_hashes_match_previous_verified':not optin},indent=2));print('NATIVE_PROFILE_PASS',label,flush=True)


