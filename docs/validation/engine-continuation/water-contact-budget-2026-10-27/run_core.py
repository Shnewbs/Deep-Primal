from pathlib import Path
import subprocess,json,re
r=Path(__file__).resolve().parent
(r/'core-tmp').mkdir(exist_ok=True)
j='/workspace/scratch/a22609fabda0/builder2300/toolchain/jdk-25.0.4.1+1/bin/java'
cmd=[j,'-XX:+PerfDisableSharedMem','-Xmx512m','-Xverify:all','-Djava.io.tmpdir='+str(r/'core-tmp'),'-cp',str(r/'core-classes'),'com.shnewbs.deepprimal.core.CoreChecks']
with (r/'full-core.log').open('w') as log:p=subprocess.run(cmd,cwd=r,stdout=log,stderr=subprocess.STDOUT)
s=(r/'full-core.log').read_text()
receipt={'exit':p.returncode,'pass':p.returncode==0 and bool(re.search(r'PASS: [0-9]+ differential surface cases, bounded-cache stress, tick-ring rollover\.',s)) and not any(v in s for v in ['Exception','No space left','AssertionError']),'sources':140,'baseline_sources':139,'added_helper':1,'private_tmp':True,'private_working_directory':str(r),'command':cmd}
(r/'full-core-result.json').write_text(json.dumps(receipt,indent=2)+'\n')
print(s[-1200:]);print(json.dumps(receipt));raise SystemExit(0 if receipt['pass'] else 1)
