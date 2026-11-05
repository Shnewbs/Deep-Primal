from pathlib import Path
import hashlib,json,subprocess,os,time
root=Path('/workspace/scratch/c748d58856d9');run=Path('/dev/shm/dp-renderer-pending-bytes-c748d588')
java=root/'renderer-toolchain/jdk-25.0.4.1+1/bin';project=root/'renderer-work'
for name in ['evidence','tmp','classes','baseline-classes','candidate-classes','probe-classes']: (run/name).mkdir(exist_ok=True)
manifest=json.loads((run/'inputs.json').read_text())
def blob(path):
 b=path.read_bytes();return hashlib.sha1(b'blob '+str(len(b)).encode()+b'\0'+b).hexdigest()
sources=[];rows=[]
for module,tree in manifest['trees'].items():
 for f in tree['tree']:
  if f['type']!='blob' or not f['path'].endswith('.java'):continue
  path=project/module/f['path']
  if not path.exists() or blob(path)!=f['sha']:raise RuntimeError('Source mismatch '+str(path))
  rows.append({'path':str(path.relative_to(project)),'blob':f['sha']})
  if module=='engine-core' and f['path'].startswith('src/main/java/'):sources.append(path)
  if module=='src' and f['path'].startswith('coreChecks/java/'):sources.append(path)
(run/'evidence/source-identity.json').write_text(json.dumps({'base_sha':manifest['main'],'verified_java_count':len(rows),'compiled_core_and_fixtures':len(sources),'rows':rows},indent=2))
def call(name,args):
 args=[str(x) for x in args];start=time.monotonic()
 with (run/'evidence'/f'{name}.txt').open('w') as out:
  rc=subprocess.run(args,cwd=run,stdout=out,stderr=subprocess.STDOUT,env={**os.environ,'TMPDIR':str(run/'tmp'),'JDK_JAVA_OPTIONS':'-XX:-UsePerfData -Xmx512m -Djava.io.tmpdir='+str(run/'tmp')}).returncode
 result={'command':args,'exit':rc,'seconds':time.monotonic()-start}
 (run/'evidence'/f'{name}.json').write_text(json.dumps(result,indent=2))
 if rc:raise RuntimeError(name+' failed; inspect log')
call('core-compile',[java/'javac','-g','-d',run/'classes']+sources)
products=[project/p for p in ['build/classes/java/main','graphics-settings/build/classes/java/main','performance-engine/build/classes/java/main','lod-engine/build/classes/java/main','renderer-vulkan/build/classes/java/main']]
jars=sorted(p for p in (root/'qualification/gradle-cache/caches/modules-2/files-2.1').rglob('*.jar') if not p.name.endswith(('-sources.jar','-javadoc.jar')))
jars+=[project/'build/moddev/artifacts/minecraft-patched-26.1.2.122-merged.jar']
cp=os.pathsep.join(str(p) for p in [run/'classes']+products+jars)
for variant in ['baseline','candidate']:
 call(variant+'-compile',[java/'javac','-g','-proc:none','-sourcepath','','-cp',cp,'-d',run/(variant+'-classes'),run/variant/'VoxelLodRenderer.java'])
call('fixture-compile',[java/'javac','-g','-proc:none','-sourcepath','','-cp',cp,'-d',run/'probe-classes',run/'checks/PendingUploadBytesChecks.java'])
# Fresh processes in A/B/B/A order: timings are component batch means, never game-frame percentiles.
for index,variant in enumerate(['baseline','candidate','candidate','baseline']):
 variant_cp=os.pathsep.join([str(run/'probe-classes'),str(run/(variant+'-classes')),cp])
 call(f'probe-{index}-{variant}',[java/'java','-Xverify:all','-cp',variant_cp,'com.shnewbs.deepprimal.core.lod.PendingUploadBytesChecks',run/(variant+'-classes')])
call('full-core',[java/'java','-Xverify:all','-cp',run/'classes','com.shnewbs.deepprimal.core.CoreChecks'])
(run/'evidence/checks-complete.json').write_text(json.dumps({'pass':True,'fresh_core_compile_count':len(sources),'actual_api_client_compilation':True,'fresh_full_core_suite':True,'fresh_forks':['baseline','candidate','candidate','baseline'],'gpu_upload_executed':False,'full_gradle':False,'packaged_client':False},indent=2))
print('All direct candidate checks passed')
