from pathlib import Path
import subprocess,csv,json,statistics
r=Path(__file__).resolve().parent.parent;java=r.parent/'lod-future-r57/toolchain/jdk-25.0.4.1+1/bin/java';records=[]
for fork in range(3):
 p=subprocess.run([str(java),'-Xverify:all','-cp',str(r/'classes'),'ReuseBenchmark',str(fork),str(r/'evidence'/f'benchmark-{fork}.csv')],capture_output=True,text=True,timeout=180);assert p.returncode==0,p.stderr;print(p.stdout,flush=True)
 rows=list(csv.DictReader((r/'evidence'/f'benchmark-{fork}.csv').open()))
 for iteration in range(-4,8):
  pair=[x for x in rows if int(x['iteration'])==iteration];assert len(pair)==2 and pair[0]['checksum']==pair[1]['checksum'],'checksum mismatch'
 modes={}
 for mode in ['scalar','memo']:
  selected=[x for x in rows if int(x['iteration'])>=0 and x['mode']==mode];times=sorted(int(x['nanos'])/1e6 for x in selected)
  modes[mode]={'n':len(times),'mean_ms':statistics.mean(times),'p50_ms':statistics.median(times),'p95_ms':times[-1],'max_ms':times[-1],'mean_allocated_bytes':statistics.mean(int(x['allocated_bytes']) for x in selected)}
 records.append({'fork':fork,'modes':modes,'all_checksums_match':True})
(r/'evidence/benchmark-summary.json').write_text(json.dumps({'scope':'One graph, component-only196608samples, column-major xyz with Y inner,4warmups/8measured per mode/fork,3JVMs; memo allocation included, graph compile/output allocation excluded; not native/load-ready/frame/FPS','forks':records},indent=2)+'\n');print(json.dumps(records,indent=2),flush=True)
