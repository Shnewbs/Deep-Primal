from pathlib import Path
import json,subprocess,os
r=Path(__file__).resolve().parents[1];e=r/'evidence';fake=r/'probes/fake-jdk/bin';fake.mkdir(parents=True,exist_ok=True);(fake/'javac').symlink_to((r/'toolchain/jdk-25.0.4.1+1/bin/javac').resolve());(fake/'java').write_text('#!/bin/sh\nexit 23\n');(fake/'java').chmod(0o700);rows=[]
for mode in ['', '-O','-OO']:
 out=r/'probes'/('negative-'+(mode or 'normal'));cmd=['python3']+([mode] if mode else [])+[str(r/'probes/probe.py'),'--source',str(r/'project/src/main/java/com/shnewbs/deepprimal/engine/HydrologyRuntime.java'),'--source-pin','6bd85c6647a9b9f256df5ae2103589bbc8347161','--java-home',str(fake.parent),'--fixture',str(r/'probes/reproduce-admission.py'),'--output',str(out)]
 p=subprocess.run(cmd,capture_output=True,text=True);(e/('harness-negative-'+(mode or 'normal')+'.txt')).write_text(p.stdout+p.stderr);ok=p.returncode!=0 and 'Admission JVM failed: 23' in p.stderr and not (out/'receipt.json').exists()
 rows.append({'mode':mode or 'normal','expected_jvm_exit':23,'harness_exit':p.returncode,'pass_receipt_written':(out/'receipt.json').exists(),'pass':ok})
 if not ok:raise RuntimeError('failure was accepted')
(e/'harness-failure-controls.json').write_text(json.dumps({'pass':True,'rows':rows,'expected_negative_controls':True},indent=2));print('STRICT_FAILURE_PRECEDENCE_PASS normal -O -OO')
