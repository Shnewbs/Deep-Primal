from pathlib import Path
import json,os,subprocess,time,hashlib,zipfile
r=Path('/dev/shm/dp-renderer-admission-gate-c748d588');base=Path('/workspace/scratch/c748d58856d9');e=r/'evidence';jdk=base/'renderer-toolchain/jdk-25.0.4.1+1'
command=json.loads((e/'gradle-gate.json').read_text())['command'][:-1]
env={**os.environ,'JAVA_HOME':str(jdk),'TMPDIR':str(r/'tmp'),'JDK_JAVA_OPTIONS':'-XX:-UsePerfData -Xmx384m -Djava.io.tmpdir='+str(r/'tmp')}
rows=[]
def execute(name,args):
 start=time.monotonic();p=subprocess.run(command+args,cwd=r/'harness',env=env,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,text=True);(e/(name+'.txt')).write_text(p.stdout)
 row={'name':name,'command':command+args,'exit':p.returncode,'seconds':time.monotonic()-start};rows.append(row);return p
p=execute('without-lod-graph',[':check','--dry-run','-PwithoutLod']);assert p.returncode==0 and ':lod-engine:pendingGpuBytesChecks' not in p.stdout
p=execute('with-lod-graph',[':check','--dry-run']);assert p.returncode==0 and ':lod-engine:pendingGpuBytesChecks' in p.stdout
source=r/'harness/lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java';original=source.read_text()
try:
 source.write_text(original.replace('detail < count;', 'detail < count - 1;'))
 p=execute('negative-missing-detail',[':check','--rerun-tasks']);assert p.returncode!=0 and 'Wrong resident bytes' in p.stdout
finally:source.write_text(original)
p=execute('final-check-and-module-jar',[':check',':lod-engine:jar','--rerun-tasks']);assert p.returncode==0 and 'PENDING_UPLOAD_BYTES_PASS checks=10063' in p.stdout
jars=list((r/'harness/lod-engine/build/libs').glob('*.jar'));assert len(jars)==1
jar=jars[0]
with zipfile.ZipFile(jar) as z:
 assert z.testzip() is None
 assert len(z.namelist())==len(set(z.namelist()))
 assert not any('PendingUploadBytesChecks' in x or 'LodReductionChecks' in x for x in z.namelist())
 identities=[]
 for f in (r/'harness/lod-engine/build/classes/java/main').rglob('*.class'):
  rel=str(f.relative_to(r/'harness/lod-engine/build/classes/java/main'));assert z.read(rel)==f.read_bytes();identities.append({'class':rel,'sha256':hashlib.sha256(f.read_bytes()).hexdigest()})
(e/'gate-matrix.json').write_text(json.dumps({'pass':True,'rows':rows,'negative_control_expected_failure':True,'candidate_restored_and_final_gate_passed':True,'diagnostic_module_jar_only':{'path':str(jar),'bytes':jar.stat().st_size,'sha256':hashlib.sha256(jar.read_bytes()).hexdigest(),'crc_pass':True,'duplicates':False,'private_fixture_excluded':True,'compiled_classes':identities},'full_distribution_build':False,'gpu_execution':False},indent=2));print('Gate matrix passed; module-only CRC/identities/fixture exclusions passed')
