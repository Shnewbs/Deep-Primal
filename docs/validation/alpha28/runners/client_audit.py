from pathlib import Path
import os,subprocess,time,json,hashlib,signal,sys,pty,select
r=Path(__file__).resolve().parent;p=r/'project';t=r/'toolchain';g=t/'graphics-verified';mode=sys.argv[1];gl=mode=='gl-bed'
capture=r/'evidence'/('client-'+mode);capture.mkdir(exist_ok=False)
run=r/('client-baseline' if mode=='baseline-bed' else 'client-v7') if mode in ('create','baseline-bed') else Path('/tmp/deepprimal-water-audit-1a7cb29b-'+('gl' if gl else 'v7'))
if mode not in ('create','baseline-bed') and not run.exists():
 import shutil
 (run/'saves').mkdir(parents=True)
 shutil.copytree(r/'client-v7/saves/Terrain-V7-Audit',run/'saves/Terrain-V7-Audit')
run.mkdir(exist_ok=True)
if mode=='create':assert not (run/'saves').exists()
(run/'options.txt').write_text('renderDistance:5\nsimulationDistance:2\nmaxFps:30\nenableVsync:false\npauseOnLostFocus:false\nautoJump:false\n')
display=537;env=dict(os.environ);env.update(LD_LIBRARY_PATH=str(g/'usr/lib/x86_64-linux-gnu'),VK_DRIVER_FILES=str(g/'usr/share/vulkan/icd.d/lvp_icd.json'),VK_LAYER_PATH=str(g/'usr/share/vulkan/explicit_layer.d'),DISPLAY='127.0.0.1:'+str(display),XDG_RUNTIME_DIR=str(capture),DEEPPRIMAL_SHUTDOWN_RECORD=str(capture/'shutdown.txt'),PATH=str(g/'usr/bin')+os.pathsep+env['PATH'],LP_NUM_THREADS='4',DEEPPRIMAL_AUDIT_SITES=str(r/'evidence/native-initial/terrain.json'))
if gl:env.pop('DEEPPRIMAL_SHUTDOWN_RECORD',None)
cmd=[sys.executable,'tools/build_with_current_proxy.py','--java-home',str(t/'jdk-25.0.4.1+1'),'--gradle',str(t/'gradle-9.2.1/bin/gradle'),'--gradle-user-home',str(t/'gradle-cache'),'--trust-store','/etc/ssl/certs/java/cacerts','--neoform-agent',str(t/'checked-agent/neoform-process-fallback.jar'),'--root-game-task','runClient','--','--no-daemon','--no-build-cache','--console=plain','--max-workers=2','-Dorg.gradle.jvmargs=-Xmx1G','-Porg.gradle.java.installations.paths='+str(t/'jdk-21.0.12.1+1')+','+str(t/'jdk-25.0.4.1+1'),'-PvulkanPlatform=linux','-PvisualValidation','-PpackagedValidation','-PvalidationJar='+str(p/'build/libs/DeepPrimal-26.1.2-0.0.1-alpha.28-vulkan-linux.jar'),'-PgraphicsBackend='+('opengl' if gl else 'vulkan'),'-PvulkanValidation','-PvulkanDebug','-PclientRunDir='+str(run),'-PcaptureDir='+str(capture),'-x',':jar','-x',':jarJar']
if mode=='baseline-bed':
 import shutil
 (run/'saves').mkdir(exist_ok=True)
 shutil.copytree(r/'native-world-baseline/world',run/'saves/Audit-baseline')
 cmd[cmd.index(next(v for v in cmd if v.startswith('-PvalidationJar='))) ]='-PvalidationJar='+str(r/'recovery/DeepPrimal-26.1.2-0.0.1-alpha.27-vulkan-windows.jar')
if mode=='create':cmd+=['-PworldCreationValidation','-PcreationName=Terrain-V7-Audit']
else:cmd+=['-PcaptureWorld='+('Audit-baseline' if mode=='baseline-bed' else 'Terrain-V7-Audit'),'-PterrainValidation','-PacceptWorldWarning']
assert Path(next(v.split('=',1)[1] for v in cmd if v.startswith('-PvalidationJar='))).is_file(),'Selected package missing'
(capture/'command.json').write_text(json.dumps(cmd,indent=2));actions=[]
if mode!='create':
 backend='OpenGL' if gl else 'Vulkan'
 actions=[('backend-check='+backend,'BACKEND_SELECTION_PASS backend='+backend,0)]
 for name in (['water-bed-fallback','water-bed-full-fallback'] if gl else ['water-bed','water-bed-full'] if mode.endswith('bed') else ['forest','dry','mountain','river']):actions += [('audit='+name,'TERRAIN_AUDIT_CAMERA_READY '+name,25),(name,'DEEPPRIMAL_CAPTURE_SAVED '+name,2)]
 actions += ([('audit=smooth-check','SMOOTH_WATER_RENDER_PASS',0)] if gl else [('vulkan-checks','VULKAN_STENCIL_PASS',1)])+[('stop','CLIENT_SAVED_DISCONNECT_PASS',0)]
start=time.monotonic();index=0;waiting=False;sent=0;before=0;history=[]
xlog=(capture/'xvfb.log').open('w');x=subprocess.Popen([str(g/'usr/bin/Xvfb-private'),':'+str(display),'-screen','0','1200x800x24','-ac','-nolisten','unix','-nolisten','local','-listen','tcp','-fp',str(g/'usr/share/fonts/X11/misc')],env=env,stdout=xlog,stderr=subprocess.STDOUT)
try:
 time.sleep(1);assert x.poll() is None
 with (capture/'console.log').open('wb') as f:
  master,slave=pty.openpty();proc=subprocess.Popen(cmd,cwd=p,env=env,stdout=slave,stderr=slave,start_new_session=True);os.close(slave);received=b''
  while True:
   ready=select.select([master],[],[],.2)[0]
   if ready:
    try:chunk=os.read(master,65536)
    except OSError:chunk=b''
    if chunk:f.write(chunk);f.flush();received+=chunk
    elif proc.poll() is not None:break
   elif proc.poll() is not None:break
   text=received.decode(errors='replace');request=capture/'capture-request.txt'
   if actions and index<len(actions):
    action,expected,delay=actions[index]
    if waiting and text.count(expected)>before and not request.exists() and time.monotonic()-sent>=delay:history.append(action);index+=1;waiting=False
    if not waiting and index<len(actions) and not request.exists():
     action,expected,delay=actions[index];before=text.count(expected);request.write_text(action);sent=time.monotonic();waiting=True
   if 'Failed to start FML' in text and proc.poll() is None:os.killpg(proc.pid,signal.SIGTERM)
   if time.monotonic()-start>600:os.killpg(proc.pid,signal.SIGTERM);proc.wait(timeout=20);raise TimeoutError('Client gate')
   time.sleep(.5)
finally:x.terminate();x.wait(timeout=10);xlog.close()
(capture/'console.log').write_bytes(received)
text=received.decode(errors='replace');assert (capture/'console.log').read_bytes()==received
record={'console_bytes':len(received),'console_sha256':hashlib.sha256(received).hexdigest(),'exit_code':proc.returncode,'seconds':time.monotonic()-start,'crashes':[f.name for f in (run/'crash-reports').glob('*')],'saved':'All dimensions are saved' in text,'actions':history,'images':[str(f.relative_to(capture)) for f in capture.rglob('*.png')],'vuid_lines':[s for s in text.splitlines() if 'VUID-' in s],'software_vulkan':not gl}
record['recovery_marker_present_after_run']=(run/'config/deepprimal-vulkan-starting').exists()
record['released_resources']='VULKAN_RESOURCES_RELEASED buffers=0 images=0' in text
record['shutdown_continuation']=(capture/'shutdown.txt').exists() and 'VULKAN_SHUTDOWN_CONTINUATION_PASS' in (capture/'shutdown.txt').read_text()
record['pass']=('SMOOTH_WATER_RENDER_PASS' in text and 'BACKEND_SELECTION_PASS backend=OpenGL' in text if gl else record['released_resources'] and record['shutdown_continuation']) and proc.returncode==0 and not record['crashes'] and not record['vuid_lines'] and record['saved'] and ('WORLD_CREATION_PASS' in text if mode=='create' else 'CLIENT_SAVED_DISCONNECT_PASS' in text and len(record['images'])>=(2 if mode.endswith('bed') else 4))
(capture/'result.json').write_text(json.dumps(record,indent=2));print(record);raise SystemExit(0 if record['pass'] else 1)

