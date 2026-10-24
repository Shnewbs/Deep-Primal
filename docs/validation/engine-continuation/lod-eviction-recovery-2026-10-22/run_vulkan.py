from pathlib import Path
import os,subprocess,time,json,hashlib,signal,sys,zipfile,io,datetime,gzip
R=Path(__file__).resolve().parent;P=R/'project';T=R/'toolchain';G=T/'graphics-verified'
mode=sys.argv[1] if len(sys.argv)>1 else 'create';case='vulkan-'+mode
capture=R/case
if capture.exists():
 for i in range(1,20):
  retry=R/(case+'-attempt'+str(i))
  if not retry.exists():capture=retry;break
capture.mkdir(exist_ok=False)
run=Path(os.environ.get('DEEPPRIMAL_NATIVE_RUN_DIR', str(P/'run-vulkan-primary')));run.mkdir(exist_ok=True)
prior_crashes={f.name:hashlib.sha256(f.read_bytes()).hexdigest() for f in (run/'crash-reports').glob('*')}
(capture/'prior-crash-reports.json').write_text(json.dumps(prior_crashes,indent=2)+'\n')
marker=run/'config/deepprimal-vulkan-starting'
(capture/'startup-marker-before.json').write_text(json.dumps({'present':marker.exists(),'explicit_retry':mode.endswith('retry')})+'\n')
options=run/'options.txt'
if not options.exists():options.write_text('renderDistance:4\nsimulationDistance:2\nmaxFps:30\nenableVsync:false\npauseOnLostFocus:false\nautoJump:false\n')
display=200+int(hashlib.sha256(str(capture).encode()).hexdigest()[:4],16)%1000
env=os.environ.copy();env.update({'LD_LIBRARY_PATH':str(G/'usr/lib/x86_64-linux-gnu'),'VK_DRIVER_FILES':str(G/'usr/share/vulkan/icd.d/lvp_icd.json'),'VK_LAYER_PATH':str(G/'usr/share/vulkan/explicit_layer.d'),'DISPLAY':'127.0.0.1:'+str(display),'XDG_RUNTIME_DIR':str(capture),'DEEPPRIMAL_SHUTDOWN_RECORD':str(capture/'shutdown.txt'),'PATH':str(G/'usr/bin')+os.pathsep+env['PATH'],'LP_NUM_THREADS':'4'})
xlog=(capture/'xvfb.log').open('w');x=None
cmd=[sys.executable,'tools/build_with_current_proxy.py','--java-home',str(T/'jdk-25.0.4.1+1'),'--gradle',str(T/'gradle-9.2.1/bin/gradle'),'--gradle-user-home',str(T/'gradle-cache'),'--trust-store','/etc/ssl/certs/java/cacerts','--neoform-agent',str(T/'checked-agent/neoform-process-fallback.jar'),'--','--no-daemon','--console=plain','--max-workers=2','-Dorg.gradle.jvmargs=-Xmx1G','-Porg.gradle.java.installations.paths='+str(T/'jdk-21.0.12.1+1')+','+str(T/'jdk-25.0.4.1+1'),'-PvulkanPlatform=linux','-Pmod_version=0.0.1-alpha.25-dev','-PvisualValidation','-PgraphicsBackend=vulkan','-PvulkanValidation','-PvulkanDebug','-PclientRunDir='+str(run),'-PcaptureDir='+str(capture)]
if mode.startswith('create'):cmd+=['-PworldCreationValidation','-PcreationName=Vulkan-primary-20261022']
else:cmd+=['-PcaptureWorld=Vulkan-primary-20261022','-PterrainValidation','-PacceptWorldWarning']
if mode.endswith('retry'):cmd+=['-PvulkanRetry']

if 'packaged' in mode:cmd+=['-PpackagedValidation','-PvalidationJar='+str(P/'build/libs/DeepPrimal-26.1.2-0.0.1-alpha.25-dev-vulkan-linux.jar')]
cmd+=['runClient'];(capture/'command.json').write_text(json.dumps(cmd,indent=2)+'\n')
outer=P/'build/libs/DeepPrimal-26.1.2-0.0.1-alpha.25-dev-vulkan-linux.jar'
if outer.exists():
 with zipfile.ZipFile(outer) as archive:
  assert archive.testzip() is None
  name,=[n for n in archive.namelist() if '.DeepPrimal-Vulkan-' in n]
  module_bytes=archive.read(name)
 with zipfile.ZipFile(io.BytesIO(module_bytes)) as module:
  assert module.testzip() is None
  for compiled in (P/'renderer-vulkan/build/classes/java/main').rglob('*.class'):
   assert module.read(compiled.relative_to(P/'renderer-vulkan/build/classes/java/main').as_posix())==compiled.read_bytes()
 direct=P/'renderer-vulkan/build/libs/DeepPrimal-Vulkan-0.0.1-alpha.25-dev-linux.jar'
 direct.write_bytes(module_bytes)
 cmd[-1:-1]=['-x',':renderer-vulkan:jar']
 (capture/'module-prelaunch-audit.json').write_text(json.dumps({'crc':True,'all_current_compiled_vulkan_classes_match':True,'sha256':hashlib.sha256(module_bytes).hexdigest(),'jar_task_excluded_after_verified_full_build':True})+'\n')
 (capture/'command.json').write_text(json.dumps(cmd,indent=2)+'\n')
started_utc=datetime.datetime.now(datetime.timezone.utc).isoformat();start=time.monotonic();p=None;timeout=False
actions=[]
if mode.startswith('marker'):
 actions=[('backend-check=Vulkan','BACKEND_SELECTION_PASS backend=Vulkan',0),('startup-observe=joined','STARTUP_LIFECYCLE_OBSERVATION label=joined',1),('reload-check','RESOURCE_RELOAD_PASS',8),('startup-observe=reloaded','STARTUP_LIFECYCLE_OBSERVATION label=reloaded',1)]
 if 'rearm' in mode:actions += [('startup-rearm-check','STARTUP_REARM_NATIVE_PASS',1)]
 actions += [('vulkan-checks','VULKAN_STENCIL_PASS format=DEPTH32_STENCIL8 reference=1',2),('marker-lifecycle','DEEPPRIMAL_CAPTURE_SAVED marker-lifecycle',2),('stop',None,0)]
elif mode.startswith('frame'):
 actions=[('backend-check=Vulkan','BACKEND_SELECTION_PASS backend=Vulkan',0),('vulkan-frame-completion-check','VULKAN_FRAME_COMPLETION_NATIVE_PASS',1),('vulkan-checks','VULKAN_STENCIL_PASS format=DEPTH32_STENCIL8 reference=1',2),('frame-completion','DEEPPRIMAL_CAPTURE_SAVED frame-completion',2),('reload-check','RESOURCE_RELOAD_PASS',10),('stop',None,0)]
elif mode.startswith('disk'):
 actions=[('backend-check=Vulkan','BACKEND_SELECTION_PASS backend=Vulkan',0),('startup-observe=joined','STARTUP_LIFECYCLE_OBSERVATION label=joined',1),('lod-disk-recovery-check','LOD_DISK_RECOVERY_NATIVE_PASS',1),('vulkan-frame-completion-check','VULKAN_FRAME_COMPLETION_NATIVE_PASS',1),('vulkan-checks','VULKAN_STENCIL_PASS format=DEPTH32_STENCIL8 reference=1',2),('water-view=true,false,true','WATER_VIEW_OPTIONS animated=true waves=false lod=true',2),('audit-lake-camera',None,25),('water-view-observe=disk-recovery','WATER_VIEW_OBSERVATION label=disk-recovery',1),('disk-recovery','DEEPPRIMAL_CAPTURE_SAVED disk-recovery',2),('reload-check','RESOURCE_RELOAD_PASS',10),('startup-observe=reloaded','STARTUP_LIFECYCLE_OBSERVATION label=reloaded',1),('stop',None,0)]
elif mode.startswith('view'):
 actions=[('backend-check=Vulkan','BACKEND_SELECTION_PASS backend=Vulkan',0),('vulkan-checks','VULKAN_STENCIL_PASS format=DEPTH32_STENCIL8 reference=1',2),('water-view=true,false,true','WATER_VIEW_OPTIONS animated=true waves=false lod=true',1),('audit-lake-camera',None,35),('water-view-observe=enabled-animated','WATER_VIEW_OBSERVATION label=enabled-animated',1),('enabled-animated','DEEPPRIMAL_CAPTURE_SAVED enabled-animated',2),('water-view=false,false,true','WATER_VIEW_OPTIONS animated=false waves=false lod=true',15),('water-view-observe=enabled-fallback','WATER_VIEW_OBSERVATION label=enabled-fallback',1),('enabled-fallback','DEEPPRIMAL_CAPTURE_SAVED enabled-fallback',2),('water-view=true,true,true','WATER_VIEW_OPTIONS animated=true waves=true lod=true',15),('enabled-waves','DEEPPRIMAL_CAPTURE_SAVED enabled-waves',2),('water-view=true,false,false','WATER_VIEW_OPTIONS animated=true waves=false lod=false',10),('diagnostic-no-lod','DEEPPRIMAL_CAPTURE_SAVED diagnostic-no-lod',2),('water-view=true,false,true','WATER_VIEW_OPTIONS animated=true waves=false lod=true',2),('reload-check','RESOURCE_RELOAD_PASS',15),('water-view-observe=enabled-reloaded','WATER_VIEW_OBSERVATION label=enabled-reloaded',1),('enabled-reloaded','DEEPPRIMAL_CAPTURE_SAVED enabled-reloaded',2),('memory-guard-checks','MEMORY_GUARD_CHECKS_PASS',1),('stop',None,0)]
elif mode=='smoke':
 actions=[('backend-check=Vulkan','BACKEND_SELECTION_PASS backend=Vulkan',0),('reopened-vulkan','DEEPPRIMAL_CAPTURE_SAVED reopened-vulkan',2),('stop',None,0)]
action_index=0;waiting=None;sent=0;before_count=0;history=[]
try:
 x=subprocess.Popen([str(G/'usr/bin/Xvfb-private'),':'+str(display),'-screen','0','1200x800x24','-ac','-nolisten','unix','-nolisten','local','-listen','tcp','-fp',str(G/'usr/share/fonts/X11/misc')],env=env,stdout=xlog,stderr=subprocess.STDOUT)
 time.sleep(1)
 assert x.poll() is None,'Display failed: '+(capture/'xvfb.log').read_text()
 with (capture/'console.log').open('w') as log:
  launch=cmd
  if 'strace' in mode:
   launch=['strace','-f','-tt','-s','160','-e','trace=openat,unlink,unlinkat,rename,renameat,renameat2','-P',str(marker),'-o',str(capture/'startup-file-syscalls.log')]+cmd
   (capture/'trace-command.json').write_text(json.dumps(launch,indent=2)+'\n')
  p=subprocess.Popen(launch,cwd=P,env=env,stdout=log,stderr=subprocess.STDOUT,start_new_session=True)
  while p.poll() is None:
   live=(capture/'console.log').read_text()
   if 'Failed to start FML' in live:
    os.killpg(p.pid,signal.SIGTERM);p.wait(timeout=20);break
   if actions:
    text=(capture/'console.log').read_text();request=capture/'capture-request.txt'
    (capture/'live-action.json').write_text(json.dumps({'index':action_index,'waiting':waiting,'before_count':before_count,'elapsed':round(time.monotonic()-start,2),'request_exists':request.exists(),'last_console_line':text.splitlines()[-1:]})+'\n')
    if waiting is not None:
     if time.monotonic()-sent>120:
      timeout=True;os.killpg(p.pid,signal.SIGTERM);p.wait(timeout=20);break
     action,expected,delay=actions[action_index]
     if not request.exists() and (expected is None or text.count(expected)>before_count) and time.monotonic()-sent>=delay:
      history.append({'action':action,'completed':True,'elapsed':round(time.monotonic()-start,2)})
      action_index+=1;waiting=None
    if waiting is None and action_index<len(actions) and not request.exists():
     action,expected,delay=actions[action_index];before_count=text.count(expected) if expected else 0
     request.write_text(action);waiting=action;sent=time.monotonic()
   if time.monotonic()-start>(1200 if mode.startswith('create') else 600):timeout=True;os.killpg(p.pid,signal.SIGTERM);p.wait(timeout=20);break
   time.sleep(1)
 # Snapshot only after the launcher exits and its console bytes settle. Seal one
 # immutable compressed copy; later mutations of the live console remain observable.
 observations=[];previous=None;stable=0
 for sample in range(20):
  raw=(capture/'console.log').read_bytes();digest=hashlib.sha256(raw).hexdigest()
  observations.append({'sample':sample,'bytes':len(raw),'sha256':digest})
  stable=stable+1 if digest==previous else 0;previous=digest
  if stable>=4:break
  time.sleep(.25)
 sealed=gzip.compress(raw,mtime=0)
 temporary=capture/'console-final.gz.tmp';temporary.write_bytes(sealed)
 temporary.replace(capture/'console-final.gz')
 (capture/'sealed-console.json').write_text(json.dumps({'stable_samples':stable,'observations':observations,'raw_bytes':len(raw),'raw_sha256':digest,'gzip_sha256':hashlib.sha256(sealed).hexdigest(),'launcher_exit':p.returncode,'scope':'Closed-launcher console snapshot; live-console changes after this snapshot do not alter these sealed bytes.'},indent=2)+'\n')
 text=raw.decode();lines=text.splitlines()
 (capture/'action-history.json').write_text(json.dumps(history,indent=2)+'\n')
 report={'case':case,'run_directory':str(run),'started_utc':started_utc,'finished_utc':datetime.datetime.now(datetime.timezone.utc).isoformat(),'exit_code':p.returncode,'timeout':timeout,'elapsed_seconds':round(time.monotonic()-start,2),'build_successful':'BUILD SUCCESSFUL' in text,'records':[s for s in lines if any(k in s for k in ['_PASS','VULKAN_','VUID','Vulkan device','llvmpipe','AssertionError','Exception in thread','crash report','resources=','tracked='])],'console_sha256':digest,'console_stable_samples':stable,'sealed_console':'console-final.gz','screenshots':[str(f.relative_to(capture)) for f in capture.rglob('*.png')],'crash_reports':[f.name for f in (run/'crash-reports').glob('*')],'software_driver':True,'owner_gpu_acceptance':False}
 (capture/'result.json').write_text(json.dumps(report,indent=2)+'\n');print(json.dumps(report,indent=2),flush=True)
 (capture/'crash-delta.json').write_text(json.dumps({'prior':prior_crashes,'new_or_changed':{f.name:hashlib.sha256(f.read_bytes()).hexdigest() for f in (run/'crash-reports').glob('*') if prior_crashes.get(f.name)!=hashlib.sha256(f.read_bytes()).hexdigest()}},indent=2)+'\n')
 (capture/'startup-marker-after.json').write_text(json.dumps({'present':marker.exists()})+'\n')
finally:
 if p and p.poll() is None:os.killpg(p.pid,signal.SIGTERM)
 if x and x.poll() is None:x.terminate();x.wait(timeout=10)
 xlog.close()
