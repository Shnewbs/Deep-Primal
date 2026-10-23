"""Compile/run the staged reference in a disposable directory; no Minecraft world."""
from pathlib import Path
import argparse,json,subprocess,tempfile,datetime,hashlib
p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);a=p.parse_args()
source=Path(__file__).resolve().with_name('SharedFlowReference.java')
started=datetime.datetime.now(datetime.timezone.utc).isoformat()
with tempfile.TemporaryDirectory(prefix='deepprimal-shared-flow-') as d:
    compile_command=[str(a.java_home/'bin/javac'),'--release','21','-d',d,str(source)]
    c=subprocess.run(compile_command,capture_output=True,text=True,timeout=60)
    assert c.returncode==0,(c.stdout,c.stderr)
    command=[str(a.java_home/'bin/java'),'-cp',d,'SharedFlowReference']
    r=subprocess.run(command,capture_output=True,text=True,timeout=60)
    assert r.returncode==0,(r.stdout,r.stderr)
    result=json.loads(r.stdout);assert result['pass'] and not result['production_integrated']
    print(json.dumps({'pass':True,'started_utc':started,'finished_utc':datetime.datetime.now(datetime.timezone.utc).isoformat(),'source_sha256':hashlib.sha256(source.read_bytes()).hexdigest(),'compile_command':compile_command,'compile_exit':c.returncode,'command':command,'exit':r.returncode,'result':result},indent=2))
