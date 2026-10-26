from pathlib import Path
import subprocess,json,hashlib
r=Path(__file__).resolve().parents[1];p=r/'priority-probe';e=r/'evidence';jdk=r/'toolchain/jdk-25.0.4.1+1/bin';rows=[]
for mode in ['baseline','stage']:
 out=p/(mode+'-exact-classes');out.mkdir()
 cmd=[str(jdk/'javac'),'-J-Xmx96m','-J-XX:-UsePerfData','-sourcepath',str(p/mode),'-d',str(out),str(p/mode/'StreamSourceIndex.java'),str(p/'stage/StreamSourcePriorityChecks.java')]
 c=subprocess.run(cmd,capture_output=True,text=True);(e/('priority-'+mode+'-compile.txt')).write_text(c.stdout+c.stderr);assert c.returncode==0
 c=subprocess.run([str(jdk/'java'),'-Xmx96m','-XX:-UsePerfData','-Xverify:all','-cp',str(out),'com.shnewbs.deepprimal.engine.StreamSourcePriorityChecks'],capture_output=True,text=True);s=c.stdout+c.stderr;(e/('priority-'+mode+'-run.txt')).write_text(s)
 assert (c.returncode!=0 and 'source priority changed after dry/re-add' in s) if mode=='baseline' else (c.returncode==0 and 'SOURCE_PRIORITY_PASS checks=100003 transitions=100000' in s)
 rows.append({'mode':mode,'exit':c.returncode,'expected_negative_control':mode=='baseline','accepted':True})
(e/'priority-independent-result.json').write_text(json.dumps({'pass':True,'source_sha':'0a936f9dbacf9bc363ae01c317f64060cc7587b3','scope':'Independent execution of exact pinned author model fixture against staged index and author compatibility baseline; no whole runtime or native world execution; no production promotion','candidate_checks':100003,'randomized_transitions':100000,'results':rows,'input_sha256':{str(f.relative_to(p)):hashlib.sha256(f.read_bytes()).hexdigest() for f in p.rglob('*.java')}},indent=2));print('STAGED_PRIORITY_INDEPENDENT_EXECUTION_PASS')
