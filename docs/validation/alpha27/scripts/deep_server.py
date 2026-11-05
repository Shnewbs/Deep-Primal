from pathlib import Path
import subprocess,os,json,time,sys,hashlib
r=Path(__file__).resolve().parent;p=r/'project';t=r/'toolchain';mode=sys.argv[1];run=r/'v6-native-world-20261024f-deep';capture=r/('native-fdeep-'+mode);capture.mkdir(exist_ok=False);run.mkdir(exist_ok=True)
(run/'config').mkdir(exist_ok=True)
(run/'config/deepprimal-worldgen.toml').write_text('extraUndergroundDepth = true\n')
(run/'eula.txt').write_text('eula=true\n');(run/'server.properties').write_text('level-type=minecraft:normal\nlevel-seed=20261020\nlevel-name=world\nonline-mode=false\nserver-ip=127.0.0.1\nserver-port=25591\nview-distance=2\nsimulation-distance=2\nspawn-protection=0\n')
if mode=='initial':assert not (run/'world').exists(),'Fresh world required'
if mode=='reopen':
 config=run/'config/deepprimal/worldgen/terrain.toml'
 config.write_text('geologicalCaves = false\nriverTunnels = false\ndunes = false\nglaciers = false\nnaturalDebris = false\nmountainScale = 2.0\npolarDistance = 4000.0\n')
env=dict(os.environ);env.update(DEEPPRIMAL_V6_RECORD=str(capture/'terrain.json'),DEEPPRIMAL_V6_MODE=mode,DEEPPRIMAL_ENVIRONMENT_RECORD=str(capture/"environment.txt"),DEEPPRIMAL_ENVIRONMENT_MODE=mode,DEEPPRIMAL_ENVIRONMENT_REVISION="6")
cmd=[sys.executable,'tools/build_with_current_proxy.py','--java-home',str(t/'jdk-25.0.4.1+1'),'--gradle',str(t/'gradle-9.2.1/bin/gradle'),'--gradle-user-home',str(t/'gradle-cache'),'--trust-store','/etc/ssl/certs/java/cacerts','--neoform-agent',str(t/'checked-agent/neoform-process-fallback.jar'),'--root-game-task','runServer','--','-I',str(r/'fresh-compile.gradle'),'--offline','--no-daemon','--no-build-cache','--console=plain','--max-workers=2','-Dorg.gradle.jvmargs=-Xmx1G','-Porg.gradle.java.installations.paths='+str(t/'jdk-21.0.12.1+1')+','+str(t/'jdk-25.0.4.1+1'),'-PvulkanPlatform=linux','-PvisualValidation','-PpackagedValidation','-PvalidationJar='+str(p/'build/libs/DeepPrimal-26.1.2-0.0.1-alpha.27-vulkan-linux.jar'),'-PrunDir='+str(run),'-x',':jar','-x',':jarJar']
(capture/'command.json').write_text(json.dumps(cmd,indent=2));start=time.monotonic();stopped=False
with (capture/'console.log').open('w') as f:
 proc=subprocess.Popen(cmd,cwd=p,env=env,stdin=subprocess.PIPE,stdout=f,stderr=subprocess.STDOUT)
 while proc.poll() is None:
  text=(capture/'console.log').read_text(errors='replace')
  if not stopped and 'TERRAIN_V6_NATIVE_PASS' in text and 'Done (' in text and (capture/'environment.txt').exists():
   proc.stdin.write(b'save-all flush\nstop\n');proc.stdin.flush();stopped=True
  if time.monotonic()-start>480:proc.terminate();raise TimeoutError('Native gate')
  time.sleep(.5)
text=(capture/'console.log').read_text(errors='replace');record={'exit_code':proc.returncode,'saved':'All dimensions are saved' in text,'stopped':stopped,'crashes':[f.name for f in (run/'crash-reports').glob('*')],'seconds':time.monotonic()-start}
if (capture/'terrain.json').exists():
 data=json.loads((capture/'terrain.json').read_text());record['pass']=data['pass'] and record['saved'] and stopped and not record['crashes'] and proc.returncode==0
 for name,expected in data['loaded_classes'].items():
  key=Path(*name.split('.')).with_suffix('.class');root=p/('engine-core/build/classes/java/main' if '.core.' in name else 'build/classes/java/visualValidation' if '.validation.' in name else 'build/classes/java/main');assert hashlib.sha256((root/key).read_bytes()).hexdigest()==expected,name
 if mode=='reopen':
  original=json.loads((r/'native-fdeep-initial/terrain.json').read_text());assert original['generator']==data['generator'] and original['protochunk_hashes']==data['protochunk_hashes'],'Saved profile/terrain drift'
else:record['pass']=False
(capture/'result.json').write_text(json.dumps(record,indent=2));print(record);raise SystemExit(0 if record['pass'] else 1)
