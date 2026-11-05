from pathlib import Path
import json,subprocess,csv,statistics,hashlib
r=Path(__file__).resolve().parent.parent;java=r.parent/'lod-future-r57/toolchain/jdk-25.0.4.1+1/bin/java';runs=[]
for fork in range(3):
 for mode in (['control','candidate'] if fork%2==0 else ['candidate','control']):
  classes=r/('control-classes' if mode=='control' else 'classes');path=r/'evidence'/f'{mode}-{fork}.csv'
  p=subprocess.run([str(java),'-Xverify:all','-cp',str(classes),'ReuseBenchmark',str(fork),str(path)],capture_output=True,text=True,timeout=120);assert p.returncode==0,p.stderr
  rows=list(csv.DictReader(path.open()));chosen=[x for x in rows if int(x['iteration'])>=0];times=sorted(int(x['nanos'])/1e6 for x in chosen)
  runs.append({'fork':fork,'mode':mode,'n':len(times),'mean_ms':statistics.mean(times),'p50_ms':statistics.median(times),'p95_ms':times[-1],'max_ms':times[-1],'mean_allocated_bytes':statistics.mean(int(x['allocated_bytes']) for x in chosen),'class_sha256':{str(p.relative_to(classes)):hashlib.sha256(p.read_bytes()).hexdigest() for p in (classes/'com/shnewbs/deepprimal/core/density').glob('DensityGraph*.class')}})
 for iteration in range(-4,8):
  vals=[]
  for mode in ['control','candidate']:
   rows=list(csv.DictReader((r/'evidence'/f'{mode}-{fork}.csv').open()));vals.append(next(x['checksum'] for x in rows if int(x['iteration'])==iteration))
  assert vals[0]==vals[1],'checksum mismatch'
(r/'evidence/benchmark-summary.json').write_text(json.dumps({'runs':runs,'all_paired_checksums_match':True,'scope':'component-only196608samples pervolume,Yinner,1seed/defaultrecipe,4warm8measured per fork/mode,3separate paired JVMs; not native or owner hardware'},indent=2)+'\n');print(json.dumps(runs,indent=2))
