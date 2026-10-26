from pathlib import Path
import subprocess,json,hashlib
P=Path(__file__).resolve().parent;R=Path('/workspace/scratch/a22609fabda0');J=R/'toolchain/jdk-25.0.4.1+1/bin';stage=P/'stage';out=P/'runtime/candidate-runtime';cp=Path('/dev/shm/dp-builder1905-oaqp2q1h/classpath.txt').read_text().strip();records=[]
sources=[stage/'StreamSourcePriorityChecks.java',stage/'StreamPriorityRuntimeChecks.java']
r=subprocess.run([str(J/'javac'),'-J-Xmx128m','-J-XX:-UsePerfData','-cp',str(out)+':'+cp,'-d',str(out),*map(str,sources)],capture_output=True,text=True);(P/'additional-compile.txt').write_text(r.stdout+r.stderr)
if r.returncode:raise RuntimeError(r.stderr)
for test in ['com.shnewbs.deepprimal.engine.StreamSourcePriorityChecks','com.shnewbs.deepprimal.engine.StreamPriorityRuntimeChecks','com.shnewbs.deepprimal.validation.AllowanceEntry']:
 fullcp=str(out)+':'+cp
 if test.endswith('AllowanceEntry'):fullcp='/dev/shm/dp-builder1905-oaqp2q1h/allowance:'+fullcp
 r=subprocess.run([str(J/'java'),'-Xmx128m','-XX:-UsePerfData','-Xverify:all','-cp',fullcp,test],capture_output=True,text=True)
 records.append({'test':test,'exit':r.returncode,'output':r.stdout+r.stderr})
 if r.returncode:raise RuntimeError(r.stdout+r.stderr)
(P/'additional-results.json').write_text(json.dumps(records,indent=2));print(json.dumps(records))
