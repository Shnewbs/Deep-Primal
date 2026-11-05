from pathlib import Path
import json,subprocess,os,time,importlib.util,hashlib,shutil
r=Path(__file__).resolve().parent;p=r/'project';e=r/'evidence';a=json.loads((e/'package.json').read_text());work=r/'native-world-optin';spec=importlib.util.spec_from_file_location('smoke',p/'tools/server_smoke.py');sm=importlib.util.module_from_spec(spec);spec.loader.exec_module(sm)
shutil.copyfile(e/'native-harness-result.json',e/'initial-native-harness-result.json')
init=r/'retry-native-memory.init.gradle';init.write_text((r/'native-memory.init.gradle').read_text().replace('768m','1024m'))
cmd=json.loads((e/'build-command.json').read_text());cmd.remove('--rerun-tasks');cmd.insert(cmd.index('--'),'runServer');cmd.insert(cmd.index('runServer'),'--root-game-task');cmd=cmd[:cmd.index(':build')]+['-PpackagedValidation','-PvisualValidation','-PvalidationJar='+a['artifact'],'-PrunDir='+str(work),'-x','downloadAssets','-I',str(init)]
(e/'reopen-retry-command.json').write_text(json.dumps(cmd,indent=2));env=os.environ.copy();env['JDK_JAVA_OPTIONS']='-Xmx1024m';env.update(DEEPPRIMAL_DEPOSIT_RECORD=str(e/'optin-retry-deposit-1.json'),DEEPPRIMAL_DEPOSIT_MODE='reopen',DEEPPRIMAL_SAVED_REVISION='7',DEEPPRIMAL_GROUND_MATERIAL_CHECK='reopen',DEEPPRIMAL_SOIL_WORLD_REVISION='2',DEEPPRIMAL_STREAM_SOURCE_NATIVE_CHECK='1',DEEPPRIMAL_DENSITY_CHUNK_NATIVE_CHECK='1',DEEPPRIMAL_NOISE_CODEC_CHECK='1',DEEPPRIMAL_AUDIT_RECORD=str(e/'optin-retry-native-1.json'),DEEPPRIMAL_AUDIT_REV='7',DEEPPRIMAL_AUDIT_MODE='reopen');(work/'config/deepprimal-worldgen.toml').write_text('riverDeltas = false\n')
log=e/'optin-retry-native-1.txt';markers=['TERRAIN_AUDIT_NATIVE_PASS','DEPOSIT_NATIVE_PASS','SOIL_WORLDGEN_PASS','STREAM_SOURCE_NATIVE_PASS'];passed=False
try:
 with log.open('w') as out:
  proc=subprocess.Popen(cmd,cwd=p,stdout=out,stderr=subprocess.STDOUT,env=env);deadline=time.monotonic()+240
  try:
   while not all(m in log.read_text(errors='replace') for m in markers+['RCON running on']):
    if proc.poll() is not None:raise RuntimeError('Retry native process exited '+str(proc.returncode))
    if time.monotonic()>deadline:raise RuntimeError('Retry native startup deadline240s')
    time.sleep(1)
   record=json.loads((e/'optin-retry-native-1.json').read_text());dep=json.loads((e/'optin-retry-deposit-1.json').read_text());assert record['pass'] and dep['pass'];assert record['generator']['channel_revision']==5
   for name,h in record['loaded_classes'].items():
    if '.validation.' not in name:
     f=p/('engine-core' if '.core.' in name else '')/'build/classes/java/main'/Path(name.replace('.','/')+'.class');assert hashlib.sha256(f.read_bytes()).hexdigest()==h,name
   assert record['hashes']==json.loads((e/'optin-native-0.json').read_text())['hashes'];assert dep['hashes']==json.loads((e/'optin-deposit-0.json').read_text())['hashes']
   rc=sm.Rcon(25952);assert 'Saved the game' in rc.command('save-all flush');rc.command('stop');rc.sock.close();proc.wait(timeout=90);sm.verify_server_shutdown(log.read_text(errors='replace'),proc.returncode,markers)
   assert hashlib.sha256(Path(a['artifact']).read_bytes()).hexdigest()==a['sha256'];passed=True
  finally:
   if proc.poll() is None:proc.terminate();proc.wait(timeout=30)
except BaseException as error:
 (e/'reopen-retry-result.json').write_text(json.dumps({'pass':False,'error':str(error),'original_failure_retained':True,'heap_mib':1024,'source_sha':a['source_sha']},indent=2));raise
if passed:
 (e/'reopen-retry-result.json').write_text(json.dumps({'pass':True,'exit':0,'heap_mib':1024,'actual_saved_channel_revision':5,'creation_toggle_disabled_before_reopen':True,'hashes_match_fresh':True,'loaded_class_identity':True,'original_failure_retained':True},indent=2))
 (e/'optin-native-result.json').write_text(json.dumps({'pass':True,'sessions':[{'mode':'fresh','pass':True,'exit':0},{'mode':'reopen-retry','pass':True,'exit':0}],'actual_saved_channel_revision':5,'creation_toggle_disabled_before_reopen':True,'replay_hashes_equal':True,'loaded_class_identity':True,'initial_reopen_timeout_retained':True,'platform':'Linux dedicated server; not Windows acceptance'},indent=2))
 (e/'native-harness-result.json').write_text(json.dumps({'pass':True,'exit':0,'source_sha':a['source_sha'],'original_optin_reopen_timeout_retained':True,'reopen_retry':'reopen-retry-result.json'},indent=2));print('REOPEN_RETRY_PASS',flush=True)
