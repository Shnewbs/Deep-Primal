from pathlib import Path
import subprocess,json,hashlib,csv,io,time
r=Path(__file__).resolve().parent.parent;e=r/'evidence';java=r.parent/'resume-tools/toolchain/jdk-25.0.4.1+1/bin/java'
before={v:{str(p.relative_to(r/('classes-'+v))):hashlib.sha256(p.read_bytes()).hexdigest() for p in (r/('classes-'+v)).rglob('*.class')} for v in ['control','candidate']}
results=[]
for mode in ['compile','sample']:
 for fork in range(3):
  for variant in (['control','candidate'] if fork%2==0 else ['candidate','control']):
   command=[str(java),'-Xms512m','-Xmx512m','-cp',str(r/('classes-'+variant)),'MetadataBenchmark',mode]
   start=time.monotonic();p=subprocess.run(command,capture_output=True,text=True,timeout=300)
   path=e/f'{mode}-{variant}-{fork}.csv';path.write_text(p.stdout+p.stderr)
   assert p.returncode==0 and 'METADATA_BENCHMARK_PASS' in p.stdout,(variant,mode,p.stdout,p.stderr)
   lines=[l for l in p.stdout.splitlines() if not l.startswith('METADATA_BENCHMARK_PASS')]
   rows=list(csv.DictReader(io.StringIO('\n'.join(lines))))
   results.append({'mode':mode,'fork':fork,'variant':variant,'command':command,'seconds':time.monotonic()-start,'rows':rows})
   print(mode,fork,variant,'seconds',round(time.monotonic()-start,2),flush=True)
for mode in ['compile','sample']:
 for fork in range(3):
  pair=[x for x in results if x['mode']==mode and x['fork']==fork]
  assert [(r['phase'],r['case'],r['checksum']) for r in pair[0]['rows']]==[(r['phase'],r['case'],r['checksum']) for r in pair[1]['rows']],'checksum mismatch'
for v in before:
 assert before[v]=={str(p.relative_to(r/('classes-'+v))):hashlib.sha256(p.read_bytes()).hexdigest() for p in (r/('classes-'+v)).rglob('*.class')},'class changed during timing'
(e/'benchmark-summary.json').write_text(json.dumps({'pass':True,'class_sha256':before,'runs':results},indent=2)+'\n')
print('R62_BENCHMARK_PASS pairs=6 checksumsMatch=true classBytesStable=true',flush=True)
