from pathlib import Path
import subprocess,json,statistics,hashlib,platform,time
r=Path(__file__).resolve().parent;t=r/'toolchain/jdk-25.0.4.1+1/bin'
core=r/'project/engine-core/build/libs/DeepPrimal-Core-0.0.1-alpha.26.jar'
subprocess.run([str(t/'javac'),'-cp',str(core),'-d',str(r/'profile-classes'),str(r/'DepositCost.java')],check=True)
out=r/'profiles';out.mkdir(exist_ok=True)
records=[]
for i,label in enumerate(['control','candidate','candidate','control','control','candidate']):
 jar=r/'pre-pruning/core.jar' if label=='control' else core
 cmd=[str(t/'java'),'-Xms512m','-Xmx512m','-cp',str(r/'profile-classes')+':'+str(jar),'DepositCost']
 raw=subprocess.check_output(cmd,timeout=120);d=json.loads(raw);path=out/f'{i}-{label}.json';path.write_bytes(raw)
 records.append({'label':label,'file':path.name,'command':cmd,'jar_sha256':hashlib.sha256(jar.read_bytes()).hexdigest(),'sha256':hashlib.sha256(raw).hexdigest(),'data':d})
summary={'scope':'CPU microprobe only, 8 warmups, 9 alternating batches/process, 3 processes/variant; no native, frame/FPS, GPU or full-generation acceptance','columns':8192,'heights':384,'records':[{k:v for k,v in x.items() if k!='data'} for x in records],'results':{}}
for label in ['control','candidate']:
 data=[x['data'] for x in records if x['label']==label];values={}
 for k in data[0]['samples'][0]:
  count=8192*(384 if 'query' in k else 1)
  values[k]={'median_ns_per_operation':statistics.median(s[k][0]/count for d in data for s in d['samples']),'allocated_bytes_per_operation':sorted(set(s[k][1]/count for d in data for s in d['samples']))}
  if 'query' in k:assert values[k]['allocated_bytes_per_operation']==[0.0]
 values['active_histogram']=data[0]['active_histogram'];summary['results'][label]=values
sums={label:{sample['candidate_query_ns_bytes_sum'][2] for record in records if record['label']==label for sample in record['data']['samples']} for label in ['control','candidate']}
assert sums['control']==sums['candidate'] and len(sums['candidate'])==1
summary['matched_query_checksums']={k:list(v) for k,v in sums.items()}
summary['candidate_query_change_percent']=100*(summary['results']['candidate']['candidate_query_ns_bytes_sum']['median_ns_per_operation']/summary['results']['control']['candidate_query_ns_bytes_sum']['median_ns_per_operation']-1)
(r/'cost-summary.json').write_text(json.dumps(summary,indent=2)+'\n');print(json.dumps(summary['results']));print('query change %',summary['candidate_query_change_percent'])
