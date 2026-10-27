from pathlib import Path
import os,sys,json,subprocess,time,hashlib
r=Path('/dev/shm/dp-builder2600'); mode=sys.argv[1];assert mode in ('fresh','reopen')
w=r/'native-world-final';w.mkdir(exist_ok=True);tmp=r/'native-tmp';tmp.mkdir(exist_ok=True)
(w/'eula.txt').write_text('eula=true\n')
if mode=='fresh':
 assert not (w/'world').exists()
 (w/'server.properties').write_text('online-mode=false\nserver-ip=127.0.0.1\nserver-port=0\nlevel-name=world\nlevel-type=minecraft:flat\nview-distance=2\nsimulation-distance=2\nspawn-protection=0\nmax-players=1\nmotd=Private water diagnostic\npause-when-empty-seconds=0\n')
 with (w/'server.properties').open('a') as f:f.write('generator-settings='+json.dumps({'layers':[{'block':'minecraft:bedrock','height':1},{'block':'minecraft:dirt','height':2},{'block':'minecraft:grass_block','height':1}],'biome':'minecraft:plains','features':False,'lakes':False})+'\n')
jar=r/'contact-diagnostic-private.jar';h=hashlib.sha256(jar.read_bytes()).hexdigest()
build_tools=('AutoRenamingTool','DiffPatch','binarypatcher','cli-utils','foojay','gradle-idea','installertools','jst-cli','mergetool-1','moddev-gradle','neoform-runtime','vineflower','srgutils')
old=('fastutil-8.3.1','asm-analysis-9.5','asm-util-9.5','accesstransformers-11','at-parser-11','commons-compress-1.18','commons-lang3-3.9','gson-2.10','jopt-simple-5')
libs=[str(p) for p in sorted((r/'api/lib').glob('*.jar')) if not p.name[17:].startswith(build_tools+old)]
mods='deepprimal%%'+str(jar)+':deepprimal%%'+str(r/'native-fixture')
for module in json.loads((r/'split-modules.json').read_text()):
 assert hashlib.sha256(Path(module['path']).read_bytes()).hexdigest()==module['sha256']
 if module['mod_id']:mods+=':'+module['mod_id']+'%%'+module['path']
 else:libs.append(module['path'])
env=os.environ.copy();env.update({'TMPDIR':str(tmp),'MOD_CLASSES':mods,'DP_CONTACT_MODE':mode,'DP_CONTACT_RECORD':str(r/(mode+'-native-marker.txt'))})
for name,pkg in [('WaterAccount','core'),('WaterRuntime','engine'),('WaterContactRuntime','engine')]:
 env['DP_HASH_'+name]=hashlib.sha256((r/'api/classes/com/shnewbs/deepprimal'/pkg/(name+'.class')).read_bytes()).hexdigest()
cmd=['/workspace/scratch/a22609fabda0/builder2300/toolchain/jdk-25.0.4.1+1/bin/java','-Xmx512m','-XX:+PerfDisableSharedMem','--sun-misc-unsafe-memory-access=allow','--enable-native-access=ALL-UNNAMED','--add-opens=java.base/java.lang.invoke=ALL-UNNAMED','-Djava.io.tmpdir='+str(tmp),'-Djna.tmpdir='+str(tmp),'-Dfml.modFolders='+mods,'-cp',':'.join(libs),'net.neoforged.fml.startup.Server','--nogui']
cmd.insert(1,'-Dlog4j2.configurationFile='+str(r/'serverLog4j2.xml'))
log=r/(mode+'-native.log');start=time.monotonic();sent=False
with log.open('w') as f:
 p=subprocess.Popen(cmd,cwd=w,env=env,stdin=subprocess.PIPE,stdout=f,stderr=subprocess.STDOUT,text=True)
 while p.poll() is None:
  content=log.read_text(errors='replace')
  if 'CONTACT_NATIVE_PASS mode='+mode in content and not sent:
   p.stdin.write('save-all flush\nstop\n');p.stdin.flush();sent=True
  if time.monotonic()-start>240:
   p.terminate();p.wait(timeout=20);break
  time.sleep(0.25)
content=log.read_text(errors='replace')
failed=[line for line in content.splitlines() if any(x in line for x in ['Exception','AssertionError','No space left','Failed to load','ERROR'])]
offline=[line for line in failed if ('Yggdrasil Key Fetcher/ERROR' in line and 'Failed to request yggdrasil public key' in line) or ('MinecraftClientException: Failed to read from https://api.minecraftservices.com/publickeys due to api.minecraftservices.com' in line) or ('UnknownHostException: api.minecraftservices.com' in line)]
failed=[line for line in failed if line not in offline]
receipt={'mode':mode,'exit':p.returncode,'marker':('CONTACT_NATIVE_PASS mode='+mode) in content,'commands_sent':sent,'flush':('Saved the game' in content or 'Saved the world' in content),'stopped':'Stopping server' in content,'errors':failed,'sha256_unchanged':hashlib.sha256(jar.read_bytes()).hexdigest()==h,'seconds':time.monotonic()-start,'diagnostic_only':True}
receipt['pass']=receipt['exit']==0 and receipt['marker'] and receipt['commands_sent'] and receipt['flush'] and receipt['stopped'] and not failed and receipt['sha256_unchanged']
receipt['offline_auth_network_warnings']=offline
(r/(mode+'-native-result.json')).write_text(json.dumps(receipt,indent=2)+'\n');print(json.dumps({**receipt,'errors':failed[:8],'error_count':len(failed)}));raise SystemExit(0 if receipt['pass'] else 1)
