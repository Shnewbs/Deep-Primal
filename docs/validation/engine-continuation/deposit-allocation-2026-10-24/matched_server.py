from pathlib import Path
import subprocess,os,json,time,sys,gzip,hashlib
r=Path(__file__).resolve().parent;t=r/'toolchain';mode=sys.argv[1] if len(sys.argv)>1 else 'initial';p=r/('baseline-project' if mode.startswith('baseline') else 'project');capture=r/('server-'+mode);capture.mkdir(exist_ok=False);run=r/('matched-'+mode);run.mkdir(exist_ok=True)
snapshot=r/'world-input/world'
world_manifest=json.loads((r/'world-input-manifest.json').read_text())
if not snapshot.exists():
 import zipfile
 archive=(r/'world-input.zip').read_bytes()
 if hashlib.sha256(archive).hexdigest()!=world_manifest['archive_sha256']:raise AssertionError('world snapshot archive mismatch')
 with zipfile.ZipFile(r/'world-input.zip') as source:
  source.extractall(r/'world-input')
for relative,expected_file_sha in world_manifest['files'].items():
 if hashlib.sha256((snapshot/relative).read_bytes()).hexdigest()!=expected_file_sha:raise AssertionError('world snapshot input changed: '+relative)
if not (run/'world').exists():
 import shutil
 shutil.copytree(snapshot,run/'world')
(run/'eula.txt').write_text('eula=true\n')
(run/'server.properties').write_text('level-type=minecraft:normal\nlevel-seed=20261020\nlevel-name=world\nonline-mode=false\nserver-ip=127.0.0.1\nserver-port=25590\nview-distance=2\nsimulation-distance=2\nspawn-protection=0\n')
props=run/'server.properties';props.write_text(props.read_text().replace('level-type=minecraft:normal','level-type=deepprimal:validation_v5'))
pack=run/'world/datapacks/deposit-v5';(pack/'data/deepprimal/worldgen/world_preset').mkdir(parents=True,exist_ok=True)
(pack/'pack.mcmeta').write_text((p/'src/main/resources/worldgen/engine_standard/pack.mcmeta').read_text())
preset=(p/'src/main/resources/worldgen/engine_standard/data/minecraft/worldgen/world_preset/normal.json').read_text().replace('continental_v4','continental_v5')
(pack/'data/deepprimal/worldgen/world_preset/validation_v5.json').write_text(preset)
env=dict(os.environ);
env['DEEPPRIMAL_SAVED_REVISION']='5';env['DEEPPRIMAL_DEPOSIT_MODE']='reopen' if mode.endswith('reopen') else 'initial'
env['DEEPPRIMAL_DEPOSIT_RECORD']=str(capture/'deposits.json');env['DEEPPRIMAL_DEPOSIT_MAX_REVISION']='5'
expected=json.loads((r/'independent-compile.json').read_text())['control' if mode.startswith('baseline') else 'candidate']['com/shnewbs/deepprimal/core/DepositField.class']
env['DEEPPRIMAL_EXPECT_DEPOSIT_CLASS_SHA']=expected
env['DEEPPRIMAL_COVERAGE_RECORD']=str(capture/'coverage.json');env['DEEPPRIMAL_COVERAGE_SITES']=str(r/'coverage-sites.json')
cmd=[sys.executable,'tools/build_with_current_proxy.py','--java-home',str(t/'jdk-25.0.4.1+1'),'--gradle',str(t/'gradle-9.2.1/bin/gradle'),'--gradle-user-home',str(t/'gradle-cache'),'--trust-store','/etc/ssl/certs/java/cacerts','--neoform-agent',str(t/'checked-agent/neoform-process-fallback.jar'),'--root-game-task','runServer','--','-I',str(r/'fresh-compile.gradle'),'--offline','--no-daemon','--no-build-cache','--console=plain','--max-workers=2','-Dorg.gradle.jvmargs=-Xmx1G','-Porg.gradle.java.installations.paths='+str(t/'jdk-21.0.12.1+1')+','+str(t/'jdk-25.0.4.1+1'),'-PvulkanPlatform=linux','-PvisualValidation','-PpackagedValidation','-PvalidationJar='+str(p/'build/libs/DeepPrimal-26.1.2-0.0.1-alpha.26-vulkan-linux.jar'),'-PrunDir='+str(run),'-x',':jar','-x',':jarJar']
(capture/'command.json').write_text(json.dumps(cmd,indent=2)+'\n');start=time.monotonic();stopped=False
with (capture/'console.log').open('wb') as f:
 process=subprocess.Popen(cmd,cwd=p,env=env,stdin=subprocess.PIPE,stdout=f,stderr=subprocess.STDOUT)
 try:
  while process.poll() is None:
   text=(capture/'console.log').read_text(errors='replace')
   ready=(capture/'deposits.json').exists() and (capture/'coverage.json').exists()
   if not stopped and ready and 'Done (' in text:
    process.stdin.write(b'save-all flush\nstop\n');process.stdin.flush();stopped=True
   if time.monotonic()-start>300:process.terminate();raise TimeoutError('bounded server run')
   time.sleep(.5)
  code=process.wait(timeout=20)
 finally:
  if process.poll() is None:process.terminate();process.wait(timeout=20)
raw=(capture/'console.log').read_bytes();(capture/'console.log.gz').write_bytes(gzip.compress(raw,mtime=0));text=raw.decode(errors='replace')
record={'mode':mode,'exit_code':code,'elapsed_seconds':round(time.monotonic()-start,2),'coverage_record':json.loads((capture/'coverage.json').read_text()) if (capture/'coverage.json').exists() else None,'deposit_record':json.loads((capture/'deposits.json').read_text()) if (capture/'deposits.json').exists() else None,'raw_sha256':hashlib.sha256(raw).hexdigest(),'saved': 'All dimensions are saved' in text,'stopped':stopped,'crashes':[x.name for x in (run/'crash-reports').glob('*')]}
record['pass']=code==0 and record['deposit_record'] is not None and record['deposit_record']['pass'] and record['deposit_record'].get('fixture_schema')==2 and record['deposit_record'].get('sites_count')==12 and record['deposit_record'].get('fixture_class_sha256')==hashlib.sha256((p/'build/classes/java/visualValidation/com/shnewbs/deepprimal/validation/DepositNativeChecks.class').read_bytes()).hexdigest() and record['coverage_record'] is not None and record['coverage_record']['pass'] and record['coverage_record'].get('fixture_schema')==2 and record['coverage_record'].get('deposit_class_sha256')==expected and record['coverage_record']['fixture_class_sha256']==hashlib.sha256((p/'build/classes/java/visualValidation/com/shnewbs/deepprimal/validation/DepositCoverageChecks.class').read_bytes()).hexdigest() and record['saved'] and stopped and not record['crashes']
(capture/'result.json').write_text(json.dumps(record,indent=2)+'\n');print(json.dumps({k:v for k,v in record.items() if k not in ['deposit_record','coverage_record']}));raise SystemExit(0 if record['pass'] else 1)
