from pathlib import Path
import argparse,json,subprocess,hashlib
parser=argparse.ArgumentParser();parser.add_argument('--classpath',required=True);parser.add_argument('--java-home',type=Path,required=True);args=parser.parse_args()
r=Path(__file__).resolve().parent
cp=args.classpath
cp=':'.join(str(Path(x).resolve()) for x in cp.split(':'))
javac=str((args.java_home/'bin/javac').resolve());java=str(Path(javac).with_name('java'))
records=[]
for label,source in [('baseline','baseline-HydrologyRuntime.java'),('fixed','HydrologyRuntime.java'),('combined','combined-HydrologyRuntime.java')]:
    d=r/('api-'+label);d.mkdir(exist_ok=True);classes=d/'classes';classes.mkdir(exist_ok=True)
    (d/'HydrologyRuntime.java').write_bytes((r/source).read_bytes())
    command=[javac,'-J-Xmx256m','-sourcepath','','-implicit:none','-proc:none','-g','-cp',cp,'-d',str(classes),str(d/'HydrologyRuntime.java'),str(r/'StreamAllowanceNativeChecks.java'),str(r/'StreamSourceNativeChecks.java')]
    (d/'compile-command.json').write_text(json.dumps(command))
    result=subprocess.run(command,capture_output=True,text=True,timeout=60);(d/'compile.log').write_text(result.stdout+result.stderr)
    if result.returncode:raise RuntimeError('API compilation failed '+label)
    run=subprocess.run([java,'-Xmx256m','-cp',str(classes)+':'+cp,'com.shnewbs.deepprimal.validation.StreamAllowanceNativeChecks'],capture_output=True,text=True,timeout=60)
    (d/'run.log').write_text(run.stdout+run.stderr)
    if label=='baseline':
        if run.returncode==0 or 'Stream allowance mismatch' not in run.stderr:raise RuntimeError('Baseline must reproduce quota failure: '+run.stdout+run.stderr)
    elif run.returncode!=0 or 'STREAM_ALLOWANCE_RUNTIME_PASS cases=1445' not in run.stdout:raise RuntimeError('Candidate runtime check failed: '+run.stdout+run.stderr)
    records.append({'variant':label,'source_sha256':hashlib.sha256((r/source).read_bytes()).hexdigest(),'compiled_classes':len(list(classes.rglob('*.class'))),'compile_exit':result.returncode,'test_exit':run.returncode,'test_output':run.stdout.strip(),'expected_negative_control':label=='baseline'})
(r/'api-receipt.json').write_text(json.dumps({'pass':True,'scope':'Actual API-compiled HydrologyRuntime loaded in isolated JVM; no Minecraft server/native/save/reopen execution','records':records},indent=2))
print(json.dumps(records,indent=2))
