from pathlib import Path
import hashlib, importlib.util, json, os, subprocess, sys, time
r = Path(__file__).resolve().parents[1]; p = r/'project'; out = r/'receiver-next'
baseline = sys.argv[1] == 'baseline'; label = sys.argv[1]; jar = Path(sys.argv[2]).resolve()
marker = 'WATER_RECEIVER_EXPECTED_REPRO failures=4' if baseline else 'WATER_RECEIVER_CAPACITY_PASS'
spec = importlib.util.spec_from_file_location('smoke', p/'tools/server_smoke.py'); sm = importlib.util.module_from_spec(spec); spec.loader.exec_module(sm)
work = out/('world-'+label); work.mkdir(); (work/'.cache').symlink_to(r/'native-cache', target_is_directory=True)
(work/'eula.txt').write_text('eula=true\n')
(work/'server.properties').write_text('level-type=minecraft:normal\nlevel-seed=20261020\nlevel-name=world\nonline-mode=false\nserver-ip=127.0.0.1\nserver-port=25981\nenable-rcon=true\nrcon.port=25982\nrcon.password=deepprimal-local-smoke-only\nmax-tick-time=-1\nview-distance=2\nsimulation-distance=2\n')
cmd=json.loads((r/'evidence/build-command.json').read_text());cmd=[x for x in cmd if x!='--rerun-tasks'];cmd.insert(cmd.index('--'),'runServer');cmd.insert(cmd.index('runServer'),'--root-game-task');cmd=cmd[:cmd.index(':build')]+['-PpackagedValidation','-PvisualValidation','-PvalidationJar='+str(jar),'-PrunDir='+str(work),'-x','downloadAssets','-I',str(r/'native-memory.init.gradle'),'-I',str(r/'strict-compile.init.gradle')]
record=out/(label+'-observations.txt');log=out/(label+'-native.log');env=os.environ.copy();env.update(JDK_JAVA_OPTIONS='-Xmx1024m',DEEPPRIMAL_RECEIVER_CAPACITY_RECORD=str(record),DEEPPRIMAL_RECEIVER_EXPECT_FAILURE=str(baseline).lower())
digest=hashlib.sha256(jar.read_bytes()).hexdigest()
with log.open('w') as target:
 proc=subprocess.Popen(cmd,cwd=p,stdout=target,stderr=subprocess.STDOUT,env=env);deadline=time.monotonic()+480
 try:
  while True:
   text=log.read_text(errors='replace')
   if marker in text and 'RCON running on' in text: break
   if proc.poll() is not None: raise RuntimeError('Exited before required record: '+str(proc.returncode))
   if time.monotonic()>deadline: raise RuntimeError('Native startup deadline')
   time.sleep(1)
  if not record.is_file(): raise RuntimeError('No observation record')
  rc=sm.Rcon(25982)
  try:
   if 'Saved the game' not in rc.command('save-all flush'): raise RuntimeError('Missing flush acknowledgment')
   rc.command('stop')
  finally:rc.sock.close()
  proc.wait(timeout=90);sm.verify_server_shutdown(log.read_text(errors='replace'),proc.returncode,[marker])
  if hashlib.sha256(jar.read_bytes()).hexdigest()!=digest:raise RuntimeError('Archive changed')
  (out/(label+'-result.json')).write_text(json.dumps({'expected_failure_control':baseline,'candidate_pass':not baseline,'explicit_marker':marker,'exit':proc.returncode,'jar':str(jar),'sha256':digest,'saved_flush_acknowledged':True,'clean_shutdown':True},indent=2))
  print(marker,flush=True)
 finally:
  if proc.poll() is None:proc.terminate();proc.wait(timeout=30)
