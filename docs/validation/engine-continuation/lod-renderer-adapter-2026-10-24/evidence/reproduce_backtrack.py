from pathlib import Path
import subprocess,json,hashlib
r=Path(__file__).resolve().parent.parent;p=r/'evidence/AdapterChecks.java';s=p.read_text();old='if (!indexStarted || moved || indexRequest.isCancelled()) {';assert old in s
q=r/'intermediate';q.mkdir(exist_ok=True);(q/'corrected-harness-before-fix.java.txt').write_text(s.replace(old,'if (!indexStarted || moved) {'))
source=q/'backtrack/AdapterChecks.java';source.parent.mkdir(exist_ok=True);source.write_text(s.replace(old,'if (!indexStarted || moved) {'))
o=q/'backtrack/classes';o.mkdir(exist_ok=True)
sources=list((r/'staged/engine-core').rglob('*.java'))+[source]+list((r/'evidence/stubs').rglob('*.java'))
cp=subprocess.run(['java','-m','jdk.compiler/com.sun.tools.javac.Main','-d',str(o),*map(str,sources)],capture_output=True,text=True,timeout=30)
p=subprocess.run(['java','-cp',str(o),'AdapterChecks'],capture_output=True,text=True,timeout=30) if cp.returncode==0 else None
text=(p.stdout+p.stderr) if p else cp.stdout+cp.stderr
(q/'corrected-harness-backtrack-failure.log').write_text(text)
record={'compile_exit':cp.returncode,'execute_exit':p.returncode if p else None,'expected_assertion':'adapter failed to finish','expected_failure_reproduced':p is not None and p.returncode!=0 and 'adapter failed to finish' in text,'source_sha256':hashlib.sha256(source.read_bytes()).hexdigest()}
(q/'corrected-harness-result.json').write_text(json.dumps(record,indent=2)+'\n');print(record);assert record['expected_failure_reproduced']
