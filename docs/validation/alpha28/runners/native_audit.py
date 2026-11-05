from pathlib import Path
import subprocess,os,json,time,sys,hashlib,zipfile,io,select,pty
r=Path(__file__).resolve().parent;p=r/'project';t=r/'toolchain';mode=sys.argv[1]
baseline=mode=='baseline';legacy=mode=='legacy-reopen';deep=mode.startswith('deep');reopen=mode.endswith('reopen')
run=r/('native-world-baseline' if baseline or legacy else 'native-world-deep' if deep else 'native-world-v7')
capture=r/'evidence'/('native-'+mode);capture.mkdir(exist_ok=False);run.mkdir(exist_ok=True)
(run/'eula.txt').write_text('eula=true\n');(run/'server.properties').write_text('level-type=minecraft:normal\nlevel-seed=20261020\nlevel-name=world\nonline-mode=false\nserver-ip=127.0.0.1\nserver-port=25594\nview-distance=2\nsimulation-distance=2\nspawn-protection=0\n')
if not reopen:assert not (run/'world').exists()
if deep:
 (run/'config').mkdir(exist_ok=True);(run/'config/deepprimal-worldgen.toml').write_text('builtInTerrainEngine = true\nextraUndergroundDepth = true\n')
if reopen:
 config=run/'config/deepprimal/worldgen/terrain.toml';config.write_text('geologicalCaves = false\nriverTunnels = false\ndunes = false\nglaciers = false\nnaturalDebris = false\nmountainScale = 2.0\npolarDistance = 4000.0\n')
jar=r/'recovery/DeepPrimal-26.1.2-0.0.1-alpha.27-vulkan-windows.jar' if baseline else p/'build/libs/DeepPrimal-26.1.2-0.0.1-alpha.28-vulkan-linux.jar'
env=dict(os.environ);env.update(DEEPPRIMAL_AUDIT_RECORD=str(capture/'terrain.json'),DEEPPRIMAL_AUDIT_REV='6' if baseline or legacy else '7',DEEPPRIMAL_AUDIT_MODE='reopen' if reopen else 'initial')
cmd=[sys.executable,'tools/build_with_current_proxy.py','--java-home',str(t/'jdk-25.0.4.1+1'),'--gradle',str(t/'gradle-9.2.1/bin/gradle'),'--gradle-user-home',str(t/'gradle-cache'),'--trust-store','/etc/ssl/certs/java/cacerts','--neoform-agent',str(t/'checked-agent/neoform-process-fallback.jar'),'--root-game-task','runServer','--','-I',str(r/'trace-runtime.gradle'),'--no-daemon','--no-build-cache','--console=plain','--max-workers=2','-Dorg.gradle.jvmargs=-Xmx1G','-Porg.gradle.java.installations.paths='+str(t/'jdk-21.0.12.1+1')+','+str(t/'jdk-25.0.4.1+1'),'-PvulkanPlatform=linux','-PvisualValidation','-PpackagedValidation','-PvalidationJar='+str(jar),'-PrunDir='+str(run),'-x',':jar','-x',':jarJar']
(capture/'command.json').write_text(json.dumps(cmd,indent=2));start=time.monotonic();stopped=False
with (capture/'console.log').open('wb') as f:
 master,slave=pty.openpty()
 proc=subprocess.Popen(cmd,cwd=p,env=env,stdin=subprocess.PIPE,stdout=slave,stderr=slave)
 os.close(slave);received=b''
 while True:
  ready=select.select([master],[],[],.3)[0]
  if ready:
   try:chunk=os.read(master,65536)
   except OSError:chunk=b''
   if chunk:f.write(chunk);f.flush();received+=chunk
   elif proc.poll() is not None:break
  elif proc.poll() is not None:break
  text=received.decode(errors='replace')
  if not stopped and 'TERRAIN_AUDIT_NATIVE_PASS' in text and 'Done (' in text:
   proc.stdin.write(b'save-all flush\nstop\n');proc.stdin.flush();stopped=True
  if time.monotonic()-start>720:proc.terminate();raise TimeoutError('Native audit')
 proc.wait();os.close(master)
(capture/'console.log').write_bytes(received)
text=received.decode(errors='replace');assert (capture/'console.log').read_bytes()==received
record={'console_bytes':len(received),'console_sha256':hashlib.sha256(received).hexdigest(),'exit_code':proc.returncode,'saved':'All dimensions are saved' in text,'stopped':stopped,'crashes':[f.name for f in (run/'crash-reports').glob('*')],'seconds':time.monotonic()-start}
(capture/'process-result.json').write_text(json.dumps(record,indent=2))
record['pass']=record['saved'] and stopped and not record['crashes'] and proc.returncode==0 and (capture/'terrain.json').exists()
if record['pass']:
 data=json.loads((capture/'terrain.json').read_text());assert data['generator'].get('min_y',-128)==(-384 if deep else -128);payload={}
 def scan(raw):
  with zipfile.ZipFile(io.BytesIO(raw)) as z:
   assert z.testzip() is None
   for name in z.namelist():
    if name.endswith('.jar'):scan(z.read(name))
    elif name.endswith('.class'):payload[name]=hashlib.sha256(z.read(name)).hexdigest()
 scan(jar.read_bytes())
 for name,digest in data['loaded_classes'].items():
  path=name.replace('.','/')+'.class';expected=hashlib.sha256((p/'build/classes/java/visualValidation'/path).read_bytes()).hexdigest() if '.validation.' in name else payload[path]
  assert digest==expected,'Loaded class mismatch: '+name
 if not baseline and not deep:
  control=json.loads((r/'evidence/native-baseline/terrain.json').read_text())
  assert all(data['hashes'][k]==v for k,v in control['hashes'].items()),'V6 changed'
  record['legacy_native_digests']=len(control['hashes'])
 if reopen:
  initial=json.loads((r/'evidence'/('native-baseline' if legacy else 'native-deep-initial' if deep else 'native-initial')/'terrain.json').read_text());assert data['generator']==initial['generator'] and data['hashes']==initial['hashes'],'Saved profile changed'
 record.update({k:data[k] for k in ['blocks','chunks','logs','water','biome_checks']})
(capture/'result.json').write_text(json.dumps(record,indent=2));print(json.dumps(record));raise SystemExit(0 if record['pass'] else 1)
