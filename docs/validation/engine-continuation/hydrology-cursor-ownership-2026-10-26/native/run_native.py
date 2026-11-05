from pathlib import Path
import subprocess,os,json,time,hashlib,importlib.util,sys
N=Path(__file__).resolve().parent;P=N.parent;R=Path('/workspace/scratch/a22609fabda0');project=R/'builder1700/project';mode=sys.argv[1]
producer=json.loads((N/'producer-complete.json').read_text())
if hashlib.sha256((N/'StreamIndexNativeChecks.java').read_bytes()).hexdigest()!=producer['source_sha256']:raise RuntimeError('Fixture source changed since successful producer')
for name,digest in producer['fixture_classes'].items():
 if hashlib.sha256((N/'fixtures'/name).read_bytes()).hexdigest()!=digest:raise RuntimeError('Fixture compiled identity mismatch')
if hashlib.sha256((N/'private-index-diagnostic.jar').read_bytes()).hexdigest()!=producer['jar_sha256']:raise RuntimeError('Diagnostic producer archive mismatch')
if mode=='preflight':print('PRODUCER_IDENTITY_PASS');sys.exit(0)
work=N/'world';work.mkdir(exist_ok=True)
(work/'eula.txt').write_text('eula=true\n');(work/'server.properties').write_text('level-type=minecraft:normal\nlevel-seed=20261020\nlevel-name=world\nonline-mode=false\nserver-ip=127.0.0.1\nserver-port=27921\nenable-rcon=true\nrcon.port=27922\nrcon.password=deepprimal-local-smoke-only\nmax-tick-time=-1\nview-distance=2\nsimulation-distance=2\n')
jar=N/'private-index-diagnostic.jar';modules=json.loads((N/'module-inputs.json').read_text());folders='deepprimal%%'+str(jar)+':deepprimal%%'+str(N/'fixtures')+''.join(':'+m['modid']+'%%'+m['path'] for m in modules);log=N/(mode+'-native.txt');marker='SOURCE_INDEX_NATIVE_PASS mode='+mode
cmd=[str(R/'toolchain/jdk-25.0.4.1+1/bin/java'),'-Xmx1024m','-XX:-UsePerfData','-Djava.io.tmpdir='+str(P/'java-tmp'),'-Djna.tmpdir='+str(P/'java-tmp'),'--sun-misc-unsafe-memory-access=allow','--enable-native-access=ALL-UNNAMED','--add-opens','java.base/java.lang.invoke=ALL-UNNAMED','--add-exports','jdk.naming.dns/com.sun.jndi.dns=java.naming','-Dfml.modFolders='+folders,'-Dlog4j2.configurationFile='+str(project/'build/moddev/serverLog4j2.xml'),'-cp',(N/'classpath.txt').read_text(),'net.neoforged.fml.startup.Server','--nogui']
(N/(mode+'-command.json')).write_text(json.dumps(cmd));env=os.environ.copy();env.update(MOD_CLASSES=folders,DP_INDEX_MODE=mode,DP_INDEX_RECORD=str(N/'saved-quantities.txt'))
for key in ['JDK_JAVA_OPTIONS','JAVA_TOOL_OPTIONS']:env.pop(key,None)
spec=importlib.util.spec_from_file_location('smoke',project/'tools/server_smoke.py');sm=importlib.util.module_from_spec(spec);spec.loader.exec_module(sm)
t=time.monotonic();digest=hashlib.sha256(jar.read_bytes()).hexdigest()
with log.open('w') as output:
 proc=subprocess.Popen(cmd,cwd=work,env=env,stdout=output,stderr=subprocess.STDOUT)
 try:
  while True:
   text=log.read_text(errors='replace')
   if marker in text and 'RCON running on' in text:break
   if proc.poll() is not None:raise RuntimeError('Server exited before explicit native pass: '+str(proc.returncode))
   if time.monotonic()-t>360:raise RuntimeError('Native deadline')
   time.sleep(1)
  rcon=sm.Rcon(27922)
  try:
   if 'Saved the game' not in rcon.command('save-all flush'):raise RuntimeError('Missing save flush acknowledgement')
   rcon.command('stop')
  finally:rcon.sock.close()
  proc.wait(timeout=90);sm.verify_server_shutdown(log.read_text(errors='replace'),proc.returncode,[marker])
  if hashlib.sha256(jar.read_bytes()).hexdigest()!=digest:raise RuntimeError('Archive changed')
  for m in modules:
   if hashlib.sha256(Path(m['path']).read_bytes()).hexdigest()!=m['sha256']:raise RuntimeError('Immutable module changed')
  expected=json.loads((N/'diagnostic-package.json').read_text())['replacements']
  for cls in ['com.shnewbs.deepprimal.engine.HydrologyRuntime','com.shnewbs.deepprimal.engine.StreamSourceIndex']:
   if 'INDEX_LOADED_CLASS '+cls+' '+expected[cls.replace('.','/')+'.class'] not in log.read_text():raise RuntimeError('Loaded class identity mismatch: '+cls)
  (N/(mode+'-result.json')).write_text(json.dumps(dict(pass_result=True,mode=mode,exit=proc.returncode,marker=marker,save_flush=True,clean_shutdown=True,loaded_class_identity=True,module_hashes_rechecked=True,sha256=digest,seconds=time.monotonic()-t,scope='Private substituted-class diagnostic root with external immutable modules, not full build or release'),indent=2));print(marker,flush=True)
 except Exception as error:
  (N/(mode+'-failed.json')).write_text(json.dumps({'failure':str(error),'exit':proc.poll()},indent=2));raise
 finally:
  if proc.poll() is None:proc.terminate();proc.wait(timeout=30)
