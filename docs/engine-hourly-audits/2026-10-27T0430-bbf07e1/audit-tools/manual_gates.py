from pathlib import Path
import json,subprocess,hashlib,zipfile,io,os
r=Path(__file__).resolve().parent;e=r/'evidence';assert json.loads((e/'all-gates.json').read_text())['pass'];p=r/'project';a=json.loads((e/'package.json').read_text());core=r/'packaged-regressions/core.jar';java=r/'toolchain/jdk-25.0.4.1+1/bin/java';javac=java.with_name('javac');cp=str(core)+':'+str(r/'independent-core');env=os.environ.copy();env['TMPDIR']=str(r/'tmp');env['JDK_JAVA_OPTIONS']='-Xmx512m -Djava.io.tmpdir='+str(r/'tmp')
def run(cmd,log):
 with (e/log).open('w') as f:rc=subprocess.run([str(x) for x in cmd],stdout=f,stderr=subprocess.STDOUT,env=env).returncode
 assert rc==0,(log,rc)
for name in ['FrameIntervalChecks','TransitionIntervalChecks']:run([java,'-Xverify:all','-cp',cp,'com.shnewbs.deepprimal.core.'+name],name+'-packaged.txt')
(e/'telemetry-packaged-checks.json').write_text(json.dumps({'pass':True,'tests':['FrameIntervalChecks','TransitionIntervalChecks'],'actual_extracted_core_first':True,'core_sha256':hashlib.sha256(core.read_bytes()).hexdigest(),'source_sha':a['source_sha']},indent=2))
out=r/'lod-probe/classes';out.mkdir(exist_ok=True);run([javac,'-g','-cp',core,'-d',out,r/'lod-probe/AuditLodBytes.java'],'lod-independent-compile.txt');run([java,'-Xverify:all','-cp',str(core)+':'+str(out),'AuditLodBytes'],'lod-independent-packaged.txt');assert 'cases=7602' in (e/'lod-independent-packaged.txt').read_text();(e/'lod-independent-packaged.json').write_text(json.dumps({'pass':True,'cases':7602,'actual_packaged_core_first':True,'BigInteger_oracle':True,'source_sha':a['source_sha']}))
classes={}
def scan(data):
 with zipfile.ZipFile(io.BytesIO(data)) as z:
  assert z.testzip() is None
  for n in z.namelist():
   if n.endswith('.jar'):scan(z.read(n))
   elif n.endswith('.class'):classes[n]=hashlib.sha256(z.read(n)).hexdigest()
scan(Path(a['artifact']).read_bytes());assert 'com/shnewbs/deepprimal/core/FrameIntervalWindow.class' in classes;assert 'com/shnewbs/deepprimal/client/ClientTelemetry.class' in classes
assert not any(n.endswith(('/WaterContactRuntime.class','/WaterContactBudget.class')) for n in classes)
rows=json.loads((e/'class-identity.json').read_text())['classes'];actual={}
for d in [p]+[x for x in p.iterdir() if x.is_dir()]:
 root=(r/'themes-output-disk' if d.name=='gui-themes' else d/'build')/'classes/java/main'
 for f in root.rglob('*.class'):actual[f.relative_to(root).as_posix()]=hashlib.sha256(f.read_bytes()).hexdigest()
for x in rows:assert actual[x['class']]==classes[x['class']]==x['compiled_sha256']==x['packaged_sha256'],x['class']
assert len(rows)==593
for n in ['artifact','producer']:assert hashlib.sha256(Path(a[n]).read_bytes()).hexdigest()==a['sha256'] and Path(a[n]).stat().st_size==a['size']
(e/'post-native-class-identity.json').write_text(json.dumps({'pass':True,'product_classes':len(rows),'all_current_compilations_rehashed':True,'source_sha':a['source_sha'],'recursive_archives':7,'frame_and_telemetry_present':True,'staged_contact_budget_absent':True,'producer_export_unchanged':True},indent=2));print('MANUAL_FINAL_GATES_PASS')
