from pathlib import Path
import subprocess,os,json,time,sys,gzip,hashlib
r=Path(__file__).resolve().parent;p=r/'project';t=r/'toolchain';mode=sys.argv[1] if len(sys.argv)>1 else 'initial';capture=r/('server-'+mode);capture.mkdir(exist_ok=False);run=r/'disposable-server-physical';run.mkdir(exist_ok=True)
(run/'eula.txt').write_text('eula=true\n')
(run/'server.properties').write_text('level-type=minecraft:normal\nlevel-seed=20261020\nlevel-name=world\nonline-mode=false\nserver-ip=127.0.0.1\nserver-port=25590\nview-distance=2\nsimulation-distance=2\nspawn-protection=0\n')
env=dict(os.environ);env['DEEPPRIMAL_ENVIRONMENT_RECORD']=str(capture/'environment.txt')
if mode=='reopen':env['DEEPPRIMAL_ENVIRONMENT_MODE']='reopen'
else:env['DEEPPRIMAL_PHYSICAL_RECORD']=str(capture/'regional.txt')
cmd=[sys.executable,'tools/build_with_current_proxy.py','--java-home',str(t/'jdk-25.0.4.1+1'),'--gradle',str(t/'gradle-9.2.1/bin/gradle'),'--gradle-user-home',str(t/'gradle-cache'),'--trust-store','/etc/ssl/certs/java/cacerts','--neoform-agent',str(t/'checked-agent/neoform-process-fallback.jar'),'--root-game-task','runServer','--','--offline','--no-daemon','--no-build-cache','--console=plain','--max-workers=2','-Dorg.gradle.jvmargs=-Xmx1G','-Porg.gradle.java.installations.paths='+str(t/'jdk-21.0.12.1+1')+','+str(t/'jdk-25.0.4.1+1'),'-PvulkanPlatform=linux','-PvisualValidation','-PpackagedValidation','-PvalidationJar='+str(p/'build/libs/DeepPrimal-26.1.2-0.0.1-alpha.26-vulkan-linux.jar'),'-PrunDir='+str(run),'-x',':jar','-x',':jarJar']
(capture/'command.json').write_text(json.dumps(cmd,indent=2)+'\n');start=time.monotonic();stopped=False
with (capture/'console.log').open('wb') as f:
 process=subprocess.Popen(cmd,cwd=p,env=env,stdin=subprocess.PIPE,stdout=f,stderr=subprocess.STDOUT)
 try:
  while process.poll() is None:
   text=(capture/'console.log').read_text(errors='replace')
   ready=(capture/'environment.txt').exists() and (mode=='reopen' or (capture/'regional.txt').exists())
   if not stopped and ready and 'Done (' in text:
    process.stdin.write(b'save-all flush\nstop\n');process.stdin.flush();stopped=True
   if time.monotonic()-start>300:process.terminate();raise TimeoutError('bounded server run')
   time.sleep(.5)
  code=process.wait(timeout=20)
 finally:
  if process.poll() is None:process.terminate();process.wait(timeout=20)
raw=(capture/'console.log').read_bytes();(capture/'console.log.gz').write_bytes(gzip.compress(raw,mtime=0));text=raw.decode(errors='replace')
record={'mode':mode,'exit_code':code,'elapsed_seconds':round(time.monotonic()-start,2),'environment':(capture/'environment.txt').read_text() if (capture/'environment.txt').exists() else None,'regional':(capture/'regional.txt').read_text() if (capture/'regional.txt').exists() else None,'raw_sha256':hashlib.sha256(raw).hexdigest(),'saved': 'All dimensions are saved' in text,'stopped':stopped,'crashes':[x.name for x in (run/'crash-reports').glob('*')]}
record['pass']=code==0 and record['environment'] is not None and (mode=='reopen' or record['regional'] is not None) and record['saved'] and stopped and not record['crashes']
(capture/'result.json').write_text(json.dumps(record,indent=2)+'\n');print(json.dumps(record));raise SystemExit(0 if record['pass'] else 1)
