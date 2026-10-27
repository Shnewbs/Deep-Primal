from pathlib import Path
import subprocess,json,os,hashlib,time
r=Path(__file__).resolve().parent;p=r/'project';e=r/'evidence';j=r/'toolchain/jdk-25.0.4.1+1/bin';tmp=r/'tmp';tmp.mkdir(exist_ok=True);env=os.environ.copy();env['TMPDIR']=str(tmp);env['JDK_JAVA_OPTIONS']='-Xmx768m -Djava.io.tmpdir='+str(tmp);results=[]
def run(label,cmd,cwd):
 with (e/(label+'.txt')).open('w') as f:rc=subprocess.run(cmd,cwd=cwd,stdout=f,stderr=subprocess.STDOUT,env=env).returncode
 results.append({'label':label,'exit':rc,'pass':rc==0});assert rc==0,label
run('workstate',['python3','tools/validate_engine_work_state.py'],p)
for flags,label in [([],''),(['-O'],'-optimized'),(['-OO'],'-double-optimized')]:
 for test,name in [('test_server_smoke.py','shutdown-tests'),('test_engine_work_state.py','state-tests'),('test_rcon_protocol.py','rcon-tests')]:run(name+label,['python3']+flags+[test],p/'tools')
for flags,label in [([],''),(['-O'],'-O'),(['-OO'],'-OO')]:run('restoration-tests'+label,['python3']+flags+['test_restore_validation_toolchain.py'],p/'tools')
src=sorted((p/'engine-core/src/main/java').rglob('*.java'));tests=sorted((p/'src/coreChecks/java').rglob('*.java'));out=r/'independent-core';out.mkdir(exist_ok=True)
run('independent-compile',[str(j/'javac'),'-g','-d',str(out)]+[str(x) for x in src+tests],p)
run('independent-core',[str(j/'java'),'-Xverify:all','-cp',str(out),'com.shnewbs.deepprimal.core.CoreChecks'],Path('/dev/shm/dp-audit-bbf07e1-0430/project'))
(e/'independent-core-result.json').write_text(json.dumps({'pass':True,'production_sources':len(src),'test_sources':len(tests),'exit':0,'log_sha256':hashlib.sha256((e/'independent-core.txt').read_bytes()).hexdigest()},indent=2))
(e/'python-result.json').write_text(json.dumps(results,indent=2));print('INDEPENDENT_ALL_PASS',flush=True)

