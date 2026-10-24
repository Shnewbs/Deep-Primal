from pathlib import Path
import os,subprocess,time,json,hashlib,signal,sys
r=Path(__file__).resolve().parent;p=r/'project';t=r/'toolchain';g=t/'graphics-verified';mode=sys.argv[1]
capture=r/('client-final-'+mode);capture.mkdir(exist_ok=False);run=r/'v6-client-20261024b';run.mkdir(exist_ok=True)
if mode=='create':assert not (run/'saves').exists()
(run/'options.txt').write_text('renderDistance:5\nsimulationDistance:2\nmaxFps:30\nenableVsync:false\npauseOnLostFocus:false\nautoJump:false\n')
display=527;env=dict(os.environ);env.update(LD_LIBRARY_PATH=str(g/'usr/lib/x86_64-linux-gnu'),VK_DRIVER_FILES=str(g/'usr/share/vulkan/icd.d/lvp_icd.json'),VK_LAYER_PATH=str(g/'usr/share/vulkan/explicit_layer.d'),DISPLAY='127.0.0.1:'+str(display),XDG_RUNTIME_DIR=str(capture),DEEPPRIMAL_SHUTDOWN_RECORD=str(capture/'shutdown.txt'),PATH=str(g/'usr/bin')+os.pathsep+env['PATH'],LP_NUM_THREADS='4',DEEPPRIMAL_V6_SITES=str(r/'native-f-initial/terrain.json'))
cmd=[sys.executable,'tools/build_with_current_proxy.py','--java-home',str(t/'jdk-25.0.4.1+1'),'--gradle',str(t/'gradle-9.2.1/bin/gradle'),'--gradle-user-home',str(t/'gradle-cache'),'--trust-store','/etc/ssl/certs/java/cacerts','--neoform-agent',str(t/'checked-agent/neoform-process-fallback.jar'),'--root-game-task','runClient','--','-I',str(r/'fresh-compile.gradle'),'--offline','--no-daemon','--no-build-cache','--console=plain','--max-workers=2','-Dorg.gradle.jvmargs=-Xmx1G','-Porg.gradle.java.installations.paths='+str(t/'jdk-21.0.12.1+1')+','+str(t/'jdk-25.0.4.1+1'),'-PvulkanPlatform=linux','-PvisualValidation','-PpackagedValidation','-PvalidationJar='+str(p/'build/libs/DeepPrimal-26.1.2-0.0.1-alpha.27-vulkan-linux.jar'),'-PgraphicsBackend=vulkan','-PvulkanValidation','-PvulkanDebug','-PclientRunDir='+str(run),'-PcaptureDir='+str(capture),'-x',':jar','-x',':jarJar']
if mode=='create':cmd+=['-PworldCreationValidation','-PcreationName=Terrain-V6-20261024']
else:cmd+=['-PcaptureWorld=Terrain-V6-20261024','-PterrainValidation','-PacceptWorldWarning']
(capture/'command.json').write_text(json.dumps(cmd,indent=2));actions=[]
if mode!='create':
 actions=[('backend-check=Vulkan','BACKEND_SELECTION_PASS backend=Vulkan',0)]
 for name in ['relief','glacier','dunes','tunnel']:actions += [('terrain-v6='+name,'TERRAIN_V6_CAMERA_READY '+name,30),(name,'DEEPPRIMAL_CAPTURE_SAVED '+name,2)]
 actions += [('vulkan-checks','VULKAN_STENCIL_PASS',1),('stop','CLIENT_SAVED_DISCONNECT_PASS',0)]
start=time.monotonic();index=0;waiting=False;sent=0;before=0;history=[]
xlog=(capture/'xvfb.log').open('w');x=subprocess.Popen([str(g/'usr/bin/Xvfb-private'),':'+str(display),'-screen','0','1200x800x24','-ac','-nolisten','unix','-nolisten','local','-listen','tcp','-fp',str(g/'usr/share/fonts/X11/misc')],env=env,stdout=xlog,stderr=subprocess.STDOUT)
try:
 time.sleep(1);assert x.poll() is None
 with (capture/'console.log').open('w') as f:
  proc=subprocess.Popen(cmd,cwd=p,env=env,stdout=f,stderr=subprocess.STDOUT,start_new_session=True)
  while proc.poll() is None:
   text=(capture/'console.log').read_text(errors='replace');request=capture/'capture-request.txt'
   if actions and index<len(actions):
    action,expected,delay=actions[index]
    if waiting and text.count(expected)>before and not request.exists() and time.monotonic()-sent>=delay:history.append(action);index+=1;waiting=False
    if not waiting and index<len(actions) and not request.exists():
     action,expected,delay=actions[index];before=text.count(expected);request.write_text(action);sent=time.monotonic();waiting=True
   if time.monotonic()-start>600:os.killpg(proc.pid,signal.SIGTERM);proc.wait(timeout=20);raise TimeoutError('Client gate')
   time.sleep(.5)
finally:x.terminate();x.wait(timeout=10);xlog.close()
text=(capture/'console.log').read_text(errors='replace');record={'exit_code':proc.returncode,'seconds':time.monotonic()-start,'crashes':[f.name for f in (run/'crash-reports').glob('*')],'saved':'All dimensions are saved' in text,'actions':history,'images':[str(f.relative_to(capture)) for f in capture.rglob('*.png')],'vuid_lines':[s for s in text.splitlines() if 'VUID-' in s],'software_vulkan':True}
record['pass']=proc.returncode==0 and not record['crashes'] and not record['vuid_lines'] and record['saved'] and ('WORLD_CREATION_PASS' in text if mode=='create' else 'CLIENT_SAVED_DISCONNECT_PASS' in text and len(record['images'])>=4)
(capture/'result.json').write_text(json.dumps(record,indent=2));print(record);raise SystemExit(0 if record['pass'] else 1)
