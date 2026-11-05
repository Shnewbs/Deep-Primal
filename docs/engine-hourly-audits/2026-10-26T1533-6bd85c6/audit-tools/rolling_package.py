from pathlib import Path
import json,time,subprocess
r=Path(__file__).resolve().parent;e=r/'evidence'
while not (e/'package.json').exists():time.sleep(2)
while not (r/'packaged-regressions/core.jar').exists():time.sleep(2)
cmd=[str(r/'toolchain/jdk-25.0.4.1+1/bin/java'),'-Xmx768m','-Xverify:all','-cp',str(r/'packaged-regressions/core.jar')+':'+str(r/'independent-core'),'com.shnewbs.deepprimal.core.RollingTimingChecks']
with (e/'rolling-packaged.txt').open('w') as f:rc=subprocess.run(cmd,stdout=f,stderr=subprocess.STDOUT).returncode
assert rc==0
(e/'rolling-package-result.json').write_text(json.dumps({'pass':True,'code_source':'package extracted product core before private fixtures on classpath','exit':rc}))
print('PACKAGED_ROLLING_PASS')
