import argparse
import hashlib
import json
import os
from pathlib import Path
import signal
import subprocess
import sys
import time

ROOT=Path(__file__).resolve().parent
PROJECT=ROOT/'project';TOOL=ROOT/'toolchain'
parser=argparse.ArgumentParser()
parser.add_argument('name');parser.add_argument('mode')
parser.add_argument('--ticks',type=int)
parser.add_argument('--suite',choices=['closed','long-pressure','filling','pressure','queue'],default='closed')
parser.add_argument('--reuse')
parser.add_argument('--no-trace',action='store_true')
parser.add_argument('--packaged',action='store_true')
args=parser.parse_args()
directory=PROJECT/(args.reuse or 'run-'+args.name)
if args.reuse:
 assert args.mode=='reopen' and directory.is_dir()
else:
 directory.mkdir(exist_ok=False)
 (directory/'eula.txt').write_text('eula=true\n')
 (directory/'server.properties').write_text('online-mode=false\nserver-ip=127.0.0.1\nserver-port=0\nview-distance=2\nsimulation-distance=2\nlevel-seed=20261020\nmax-tick-time=-1\nspawn-protection=0\npause-when-empty-seconds=0\n')
command=[sys.executable,'tools/build_with_current_proxy.py','--java-home',str(TOOL/'jdk-25.0.4.1+1'),'--gradle',str(TOOL/'gradle-9.2.1/bin/gradle'),'--gradle-user-home',str(TOOL/'gradle-cache'),'--trust-store','/etc/ssl/certs/java/cacerts','--neoform-agent',str(TOOL/'checked-agent/neoform-process-fallback.jar'),'--','--no-daemon','--console=plain','--max-workers=2','-Dorg.gradle.jvmargs=-Xmx1G','-Porg.gradle.java.installations.paths='+str(TOOL/'jdk-21.0.12.1+1')+','+str(TOOL/'jdk-25.0.4.1+1'),'-PwithoutVulkan','-Pmod_version=0.0.1-alpha.25-dev','-PvisualValidation','-PrunDir='+directory.name,'runServer']
if args.packaged:command.insert(-1,'-PpackagedValidation')
env=dict(os.environ)
if not args.no_trace:env['DEEPPRIMAL_DRAIN_TRACE']='true'
record_path=str(ROOT/(args.name+'.txt'))
if args.suite=='closed':
 env.update(DEEPPRIMAL_CLOSED_WATER_MODE=args.mode,DEEPPRIMAL_CLOSED_WATER_RECORD=record_path);marker='CLOSED_WATER_NATIVE_PASS'
elif args.suite=='long-pressure':
 env.update(DEEPPRIMAL_LONG_PRESSURE_CHECKS='true',DEEPPRIMAL_LONG_PRESSURE_MODE=args.mode,DEEPPRIMAL_LONG_PRESSURE_RECORD=record_path)
 marker={'heads':'LONG_PRESSURE_HEAD_CHANGE_NATIVE_PASS','fixed':'LONG_PRESSURE_CONTINUATION_NATIVE_PASS','edit':'LONG_PRESSURE_EDIT_NATIVE_PASS','unload':'LONG_PRESSURE_UNLOAD_NATIVE_PASS','idle':'LONG_PRESSURE_IDLE_NATIVE_PASS','reopen':'LONG_PRESSURE_REOPEN_NATIVE_PASS'}[args.mode]
elif args.suite=='filling':
 env.update(DEEPPRIMAL_FILL_CHECKS='true',DEEPPRIMAL_FILL_RECORD=record_path);marker='WATER_FILLING_NATIVE_PASS'
elif args.suite=='pressure':
 env.update(DEEPPRIMAL_PRESSURE_CHECKS='true',DEEPPRIMAL_PRESSURE_RECORD=record_path);marker='ENGINE_PRESSURE_NATIVE_PASS'
else:
 env['DEEPPRIMAL_ACTIVE_QUEUE_RECORD']=record_path;marker='ACTIVE_QUEUE_NATIVE_PASS'
if args.ticks:env['DEEPPRIMAL_CLOSED_WATER_TICKS']=str(args.ticks)
start=time.monotonic();log=ROOT/(args.name+'.log')
with log.open('w') as output:
 process=subprocess.Popen(command,cwd=PROJECT,env=env,stdout=output,stderr=subprocess.STDOUT,start_new_session=True)
 try:code=process.wait(timeout=480)
 except subprocess.TimeoutExpired:
  os.killpg(process.pid,signal.SIGTERM)
  try:process.wait(timeout=10)
  except subprocess.TimeoutExpired:os.killpg(process.pid,signal.SIGKILL);process.wait()
  code=124
text=log.read_text();lines=text.splitlines();records=[s for s in lines if s.startswith(marker)]
failures=[s for s in lines if any(k in s for k in ['AssertionError','Encountered an unexpected exception','Exception in server tick loop','BUILD FAILED'])]
report={'case':args.name,'mode':args.mode,'suite':args.suite,'trace_enabled':not args.no_trace,'packaged':args.packaged,'exit_code':code,'explicit_pass':code==0 and bool(records) and not failures,'records':records,'failures':failures,'normal_save':'All dimensions are saved' in text,'crash_reports':[p.name for p in (directory/'crash-reports').glob('*')],'elapsed_seconds':round(time.monotonic()-start,2),'traced_source_sha256':hashlib.sha256((PROJECT/'src/main/java/com/shnewbs/deepprimal/engine/WaterRuntime.java').read_bytes()).hexdigest(),'log_sha256':hashlib.sha256(log.read_bytes()).hexdigest()}
(ROOT/(args.name+'.json')).write_text(json.dumps(report,indent=2)+'\n');print(json.dumps(report),flush=True)
