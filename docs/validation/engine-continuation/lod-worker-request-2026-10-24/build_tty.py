from pathlib import Path
import subprocess,sys,os,pty,select,time,signal,errno,json,hashlib,re
r=Path(__file__).resolve().parent;e=r/'evidence';cmd=json.loads((e/'initial-build-result.json').read_text())['command']
old=Path(cmd[cmd.index('--java-home')+1]).parents[1];cmd=[x.replace(str(old),str(r)) for x in cmd];cmd[0]=sys.executable
master,slave=pty.openpty();start=time.monotonic();timed_out=False
with (e/'build-tty.log').open('wb') as log:
 p=subprocess.Popen(cmd,cwd=r/'project',stdin=subprocess.DEVNULL,stdout=slave,stderr=slave,start_new_session=True);os.close(slave)
 while True:
  if time.monotonic()-start>600 and p.poll() is None:timed_out=True;os.killpg(p.pid,signal.SIGTERM)
  ready,_,_=select.select([master],[],[],.2)
  if ready:
   try:b=os.read(master,65536)
   except OSError as ex:
    if ex.errno==errno.EIO:break
    raise
   if not b:break
   log.write(b);log.flush()
  elif p.poll() is not None:
   try:
    b=os.read(master,65536)
    if b:log.write(b);continue
   except OSError:pass
   break
 p.wait(timeout=30);os.close(master)
b=(e/'build-tty.log').read_bytes();s=b.decode(errors='replace').replace('\r','');required=list(json.loads((e/'initial-build-result.json').read_text())['markers']);d={'exit':p.returncode,'timed_out':timed_out,'raw_sha256':hashlib.sha256(b).hexdigest(),'markers':{m:m in s for m in required},'tasks':re.findall(r'\d+ actionable tasks:.*',s),'command':cmd,'capture':'fresh pty-backed stdout+stderr'};d['pass']=p.returncode==0 and not timed_out and all(d['markers'].values());(e/'build-tty-result.json').write_text(json.dumps(d,indent=2)+'\n');print(json.dumps(d));raise SystemExit(0 if d['pass'] else 1)
