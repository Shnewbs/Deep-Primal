from pathlib import Path
import subprocess,os,json,time,sys,hashlib,zipfile,io,select,pty
r=Path(__file__).resolve().parent;p=r/'engine-review';t=r/'toolchain';mode=sys.argv[1]
candidate=mode.startswith('candidate');capture=r/'review-evidence'/('native-'+mode);capture.mkdir(exist_ok=False)
run=r/('review-native-'+mode);run.mkdir(exist_ok=False)
(run/'eula.txt').write_text('eula=true\n');(run/'server.properties').write_text('level-type=minecraft:normal\nlevel-seed=20261020\nlevel-name=world\nonline-mode=false\nserver-ip=127.0.0.1\nserver-port=25595\nview-distance=2\nsimulation-distance=2\nspawn-protection=0\n')
jar=p/'build/libs/DeepPrimal-26.1.2-0.0.1-alpha.29-dev-vulkan-linux.jar' if candidate else r/'project/build/libs/DeepPrimal-26.1.2-0.0.1-alpha.28-vulkan-linux.jar'
assert jar.is_file()
env=dict(os.environ);env['DEEPPRIMAL_WORLDGEN_BENCHMARK']=str(capture/'benchmark.json')
if candidate:env['DEEPPRIMAL_NOISE_CODEC_CHECK']='true'
cmd=[sys.executable,'tools/build_with_current_proxy.py','--java-home',str(t/'jdk-25.0.4.1+1'),'--gradle',str(t/'gradle-9.2.1/bin/gradle'),'--gradle-user-home',str(t/'gradle-cache'),'--trust-store','/etc/ssl/certs/java/cacerts','--neoform-agent',str(t/'checked-agent/neoform-process-fallback.jar'),'--root-game-task','runServer','--','--offline','--no-daemon','--no-build-cache','--console=plain','--max-workers=2','-Dorg.gradle.jvmargs=-Xmx1G','-Porg.gradle.java.installations.paths='+str(t/'jdk-21.0.12.1+1')+','+str(t/'jdk-25.0.4.1+1'),'-PvulkanPlatform=linux','-PvisualValidation','-PpackagedValidation','-PvalidationJar='+str(jar),'-PrunDir='+str(run),'-x',':jar','-x',':jarJar']
if mode.endswith('profile'):
 init=capture/'profile.gradle';init.write_text("allprojects { tasks.withType(JavaExec).configureEach { if (name == 'runServer') jvmArgs '-XX:StartFlightRecording=filename="+str(capture/'worldgen.jfr')+",settings=profile,dumponexit=true' } }\n");cmd+=['-I',str(init)]
(capture/'command.json').write_text(json.dumps(cmd,indent=2));start=time.monotonic();stopped=False
with (capture/'console.log').open('wb') as f:
 master,slave=pty.openpty();proc=subprocess.Popen(cmd,cwd=p,env=env,stdin=subprocess.PIPE,stdout=slave,stderr=slave)
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
  if not stopped and 'WORLDGEN_BENCHMARK_PASS' in text and 'Done (' in text and (not candidate or 'NOISE_PROFILE_CODEC_PASS' in text):
   proc.stdin.write(b'save-all flush\nstop\n');proc.stdin.flush();stopped=True
  if time.monotonic()-start>900:proc.terminate();raise TimeoutError('Native benchmark')
 proc.wait();os.close(master)
(capture/'console.log').write_bytes(received);text=received.decode(errors='replace')
record={'console_bytes':len(received),'console_sha256':hashlib.sha256(received).hexdigest(),'exit_code':proc.returncode,'saved':'All dimensions are saved' in text,'stopped':stopped,'crashes':[f.name for f in (run/'crash-reports').glob('*')],'seconds':time.monotonic()-start,'jar_sha256':hashlib.sha256(jar.read_bytes()).hexdigest(),'profile_recording':mode.endswith('profile')}
record['pass']=record['saved'] and stopped and not record['crashes'] and proc.returncode==0 and (capture/'benchmark.json').exists()
if record['pass']:
 data=json.loads((capture/'benchmark.json').read_text());payload={}
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
 record['loaded_class_hashes_verified']=True
 record['codec_checks']=candidate and 'NOISE_PROFILE_CODEC_PASS cases=21' in text
 if candidate:assert record['codec_checks']
(capture/'result.json').write_text(json.dumps(record,indent=2));print(json.dumps(record));raise SystemExit(0 if record['pass'] else 1)
