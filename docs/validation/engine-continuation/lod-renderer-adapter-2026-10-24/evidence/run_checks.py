import hashlib,json,pathlib,subprocess,time
r=pathlib.Path(__file__).resolve().parent.parent
o=r/'classes';o.mkdir(exist_ok=True)
sources=sorted((r/'staged/engine-core').rglob('*.java'))+[r/'evidence/WorkerChecks.java',r/'evidence/AdapterChecks.java',r/'evidence/RequestChecks.java']+list((r/'evidence/stubs').rglob('*.java'))
record={'sources':{str(p.relative_to(r)):hashlib.sha256(p.read_bytes()).hexdigest() for p in sources},'started':time.time()}
for name,cmd in [('compile',['java','-m','jdk.compiler/com.sun.tools.javac.Main','-g','-d',str(o),*map(str,sources)]),('worker',['java','-cp',str(o),'WorkerChecks']),('adapter',['java','-cp',str(o),'AdapterChecks']),('r55_request',['java','-cp',str(o),'com.shnewbs.deepprimal.core.lod.RequestChecks'])]:
 p=subprocess.run(cmd,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,text=True,timeout=120)
 (r/'evidence'/f'{name}.log').write_text(p.stdout);record[name]={'exit':p.returncode,'output':p.stdout};print(name,p.returncode,p.stdout)
 if p.returncode:break
record['pass']=record.get('r55_request',{}).get('exit')==0 and '347' in record.get('r55_request',{}).get('output','') and record.get('adapter',{}).get('exit')==0 and 'PASS R56 extracted renderer adapter checks=' in record.get('adapter',{}).get('output','') and record.get('worker',{}).get('exit')==0 and 'PASS R56 worker checks=' in record.get('worker',{}).get('output','')
(r/'evidence/worker-results.json').write_text(json.dumps(record,indent=2)+'\n')
raise SystemExit(0 if record['pass'] else 1)
