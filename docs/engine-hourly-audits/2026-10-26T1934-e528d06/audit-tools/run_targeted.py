from pathlib import Path
import subprocess,json,hashlib
m=Path('/dev/shm/audit-e528d06-20261026T1934');e=m/'evidence';jdk=Path('/workspace/scratch/a2f7076b7762/audit-8665e07-20261026T1834/toolchain/jdk-25.0.4.1+1/bin')
original=(m/'stage/StreamSourceLifecycleChecks.java').read_text()
fixture=m/'ExtractedLifecycleChecks.java'
# Only class/filename and target-class alias change; all fixture assertions remain.
text=original.replace('StreamSourceLifecycleChecks','ExtractedLifecycleChecks').replace('HydrologyRuntime','SourceInvalidationLoopChecks').replace('actualRuntime=true','extractedMethods=true')
fixture.write_text(text);results=[]
for variant in ['baseline','candidate']:
 out=m/'loop'/variant
 cmd=[str(jdk/'javac'),'-J-Xmx128m','-J-XX:-UsePerfData','-sourcepath','','-implicit:none','-proc:none','-cp',str(out),'-d',str(out),str(fixture)]
 c=subprocess.run(cmd,capture_output=True,text=True);(e/(variant+'-lifecycle-compile.txt')).write_text(c.stdout+c.stderr)
 if c.returncode:raise RuntimeError(c.stdout+c.stderr)
 r=subprocess.run([str(jdk/'java'),'-Xmx128m','-XX:-UsePerfData','-Xverify:all','-cp',str(out),'com.shnewbs.deepprimal.engine.ExtractedLifecycleChecks'],capture_output=True,text=True)
 (e/(variant+'-lifecycle.txt')).write_text(r.stdout+r.stderr);results.append({'variant':variant,'exit':r.returncode,'output':r.stdout+r.stderr,'scope':'Extracted actual Work/Cursor/refreshSource/forgetChunk with explicit doubles; not whole runtime or Minecraft'})
 if variant=='baseline' and (r.returncode==0 or 'retired cursor removed newer source generation' not in r.stderr):raise RuntimeError('Expected baseline generation failure absent')
 if variant=='candidate' and (r.returncode or 'SOURCE_LIFECYCLE_PASS checks=5512' not in r.stdout):raise RuntimeError(r.stdout+r.stderr)
out=m/'index';out.mkdir()
c=subprocess.run([str(jdk/'javac'),'-J-Xmx128m','-J-XX:-UsePerfData','-proc:none','-d',str(out),str(m/'stage/StreamSourceIndex.java'),str(m/'stage/StreamSourceIndexChecks.java')],capture_output=True,text=True)
(e/'index-compile.txt').write_text(c.stdout+c.stderr)
if c.returncode:raise RuntimeError(c.stderr)
r=subprocess.run([str(jdk/'java'),'-Xmx96m','-XX:-UsePerfData','-Xverify:all','-cp',str(out),'com.shnewbs.deepprimal.engine.StreamSourceIndexChecks'],capture_output=True,text=True);(e/'index-checks.txt').write_text(r.stdout+r.stderr)
if r.returncode or 'STREAM_SOURCE_INDEX_PASS mutations=20000 checks=20008' not in r.stdout:raise RuntimeError(r.stdout+r.stderr)
(e/'targeted-result.json').write_text(json.dumps({'pass':True,'source_sha':'e528d066d8202ac1a2822de700099a34ce67d470','baseline_failure_controls_required':True,'loop_cases':144,'lifecycle_checks':5512,'index_checks':20008,'index_mutations':20000,'lifecycle_fixture_source_sha256':hashlib.sha256(fixture.read_bytes()).hexdigest(),'lifecycle_scope':'extracted method bodies with doubles, not complete API runtime','results':results,'whole_core_rerun':False,'native_sessions':0,'new_jar':False},indent=2))
print('TARGETED_INDEPENDENT_STAGE_CONTROLS_PASS')

