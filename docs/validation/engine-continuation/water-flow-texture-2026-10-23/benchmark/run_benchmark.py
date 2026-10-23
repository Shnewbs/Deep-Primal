from pathlib import Path
import subprocess,json,hashlib,datetime,platform
B=Path(__file__).resolve().parent;R=B.parent;J=R/'toolchain/jdk-25.0.4.1+1/bin';classes=R/'project/engine-core/build/classes/java/main'
for target,sources in [(B/'baseline/classes',[B/'baseline/WaterPatchMesher.java']),(B/'classes',[B/'FlowMeshBenchmark.java'])]:
 target.mkdir(parents=True,exist_ok=True);subprocess.run([str(J/'javac'),'-cp',str(classes),'-d',str(target)]+list(map(str,sources)),check=True,timeout=60)
rows=[]
for repeat in range(2):
 for variant in (['baseline','candidate'] if repeat==0 else ['candidate','baseline']):
  cp=str(B/'classes')+':'+(str(B/'baseline/classes')+':' if variant=='baseline' else '')+str(classes)
  command=[str(J/'java'),'-Xms256m','-Xmx256m','-cp',cp,'FlowMeshBenchmark'];start=datetime.datetime.now(datetime.timezone.utc).isoformat();r=subprocess.run(command,check=True,stdout=subprocess.PIPE,text=True,timeout=120);(B/f'{repeat}-{variant}.jsonl').write_text(r.stdout)
  for line in r.stdout.splitlines():row=json.loads(line);row.update(variant=variant,repeat=repeat,started_utc=start);rows.append(row)
for case in {r['scenario'] for r in rows}:
 samples=[r for r in rows if r['scenario']==case];assert len({(r['vertices'],r['geometryDigest']) for r in samples})==1
summary=[]
for case in sorted({r['scenario'] for r in rows}):
 old=[r for r in rows if r['scenario']==case and r['variant']=='baseline'];new=[r for r in rows if r['scenario']==case and r['variant']=='candidate'];bm=sum(x['medianNs'] for x in old)/2;cm=sum(x['medianNs'] for x in new)/2
 summary.append({'scenario':case,'baseline_mean_of_medians_ns':bm,'candidate_mean_of_medians_ns':cm,'candidate_over_baseline':round(cm/bm,4),'identical_geometry_digest':old[0]['geometryDigest'],'vertices':old[0]['vertices'],'baseline_float_payload_bytes':old[0]['floatPayloadBytes'],'candidate_float_payload_bytes':new[0]['floatPayloadBytes']})
result={'scope':'Isolated dependency-free CPU meshing only; same quantity/geometry and inputs, feature absent on baseline. Alternating variant order,2 process repeats,1000 warmups and400 samples per case/process,256MiB Java heap. Includes allocation/GC. No GPU draw, atlas upload, total process memory, owner hardware/FPS, or market comparison. Not a promotion threshold.','jdk':str(J),'platform':platform.platform(),'benchmark_source_sha256':hashlib.sha256((B/'FlowMeshBenchmark.java').read_bytes()).hexdigest(),'baseline_source_sha256':hashlib.sha256((R/'before-WaterPatchMesher.java').read_bytes()).hexdigest(),'candidate_source_sha256':hashlib.sha256((R/'project/engine-core/src/main/java/com/shnewbs/deepprimal/core/WaterPatchMesher.java').read_bytes()).hexdigest(),'rows':rows,'summary':summary}
(R/'flow-mesh-cpu-observations.json').write_text(json.dumps(result,indent=2)+'\n');print(json.dumps(summary,indent=2))
