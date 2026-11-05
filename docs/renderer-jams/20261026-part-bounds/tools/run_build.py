import json,subprocess,os,time
from pathlib import Path
r=Path(__file__).parent.resolve();cmd=json.loads((r/"evidence/build-command.json").read_text());start=time.monotonic()
with (r/"evidence/build.txt").open("w") as f: rc=subprocess.run(cmd,cwd=r/"project",stdout=f,stderr=subprocess.STDOUT,env=dict(os.environ,JDK_JAVA_OPTIONS="-Xmx768m",TMPDIR=str(r/"tmp"))).returncode
(r/"evidence/build-result.json").write_text(json.dumps(dict(exit=rc,elapsed_seconds=time.monotonic()-start)))
