from pathlib import Path
import hashlib,json,subprocess,os,time
root=Path('/workspace/scratch/c748d58856d9')
run=Path('/dev/shm/dp-renderer-transition-c748d588')
java=root/'renderer-toolchain/jdk-25.0.4.1+1/bin'
def blob(p):
 b=p.read_bytes();return hashlib.sha1(b'blob '+str(len(b)).encode()+b'\0'+b).hexdigest()
manifest=json.loads((run/'evidence/core-tree.json').read_text())
inputs=[]
for f in manifest['tree']:
 if f['type']=='blob' and f['path'].startswith('src/main/java/') and f['path'].endswith('.java'):
  p=root/'renderer-work/engine-core'/f['path']
  if blob(p)!=f['sha']:raise RuntimeError('Source mismatch '+str(p))
  inputs.append(p)
fixtures=list((root/'renderer-work/src/coreChecks/java').rglob('*.java'))
fixtures=[p for p in fixtures if p.name!='CoreChecks.java']
sources=inputs+fixtures+[run/'candidate/FrameIntervalWindow.java',run/'candidate/CoreChecks.java',run/'checks/FrameIntervalChecks.java',run/'checks/TransitionIntervalChecks.java']
def call(name,args):
 start=time.monotonic()
 with (run/'evidence'/f'{name}.txt').open('w') as log:
  p=subprocess.run([str(x) for x in args],cwd=run,stdout=log,stderr=subprocess.STDOUT,env={**os.environ, 'TMPDIR':str(run/'tmp'), 'JDK_JAVA_OPTIONS':'-XX:-UsePerfData -Djava.io.tmpdir='+str(run/'tmp')})
 result={'exit':p.returncode,'seconds':time.monotonic()-start,'command':[str(x) for x in args]}
 (run/'evidence'/f'{name}.json').write_text(json.dumps(result,indent=2))
 if p.returncode: raise RuntimeError(name+' failed')
 return result
(run/'evidence/source-identity.json').write_text(json.dumps({'production_sources':len(inputs),'fixture_sources':len(fixtures)+3,'base_core_git_tree':manifest['sha'],'all_base_production_blobs_verified':True},indent=2))
call('core-compile',[java/'javac','-g','-d',run/'classes']+sources)
call('frame-checks',[java/'java','-Xverify:all','-cp',run/'classes','com.shnewbs.deepprimal.core.FrameIntervalChecks'])
# Compile the actual adapter against prior qualified compiled modules and actual game dependencies.
products=[root/'renderer-work'/p for p in ['build/classes/java/main','graphics-settings/build/classes/java/main','performance-engine/build/classes/java/main','lod-engine/build/classes/java/main','renderer-vulkan/build/classes/java/main']]
jars=sorted(p for p in (root/'qualification/gradle-cache/caches/modules-2/files-2.1').rglob('*.jar')
    if not p.name.endswith(('-sources.jar','-javadoc.jar')))
jars+=[root/'renderer-work/build/moddev/artifacts/minecraft-patched-26.1.2.122-merged.jar']
cp=os.pathsep.join(str(p) for p in [run/'classes']+products+jars)
call('adapter-compile',[java/'javac','-g','-proc:none','-sourcepath','', '-cp',cp,'-d',run/'adapter-classes',run/'candidate/ClientTelemetry.java'])
call('full-core',[java/'java','-Xmx512m','-Xverify:all','-cp',run/'classes','com.shnewbs.deepprimal.core.CoreChecks'])
(run/'evidence/checks-complete.json').write_text(json.dumps({'pass':True,'base_sources':len(inputs),'fixtures_including_registration':len(fixtures)+3,'adapter_compiles_against_actual_game_and_prior_qualified_module_classes':True,'full_gradle':False,'package_native':False},indent=2))
print('All direct checks passed')
