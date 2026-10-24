from pathlib import Path
import subprocess,json,datetime,hashlib,shutil
R=Path(__file__).parent;J=R/'toolchain/jdk-25.0.4.1+1/bin';P=R/'project'
assert not(R/'benchmarks').exists(), 'Use a new benchmark directory'
subprocess.run(['python',str(R/'prepare_benchmark.py')],check=True)
classes={};identities={}
for name,source in [('scalar',R/'baseline/WaterCornerPacket.java'),('bulk',P/'engine-core/src/main/java/com/shnewbs/deepprimal/core/WaterCornerPacket.java')]:
 out=R/'benchmarks'/(name+'-core-classes');out.mkdir(exist_ok=True);sources=[str(x)for x in(P/'engine-core/src/main/java').rglob('*.java')if x.name!='WaterCornerPacket.java']+[str(source)];cmd=[str(J/'javac'),'-d',str(out),*sources];subprocess.run(cmd,check=True);classes[name]=out
 d=R/'benchmarks'/name;subprocess.run([str(J/'javac'),'-cp',str(out),'-d',str(d),str(d/'CornerPacketBenchmark.java')],check=True)
 identities[name]={'source_sha256':hashlib.sha256(source.read_bytes()).hexdigest(),'core_class_sha256':hashlib.sha256((out/'com/shnewbs/deepprimal/core/WaterCornerPacket.class').read_bytes()).hexdigest(),'benchmark_class_sha256':hashlib.sha256((d/'com/shnewbs/deepprimal/core/CornerPacketBenchmark.class').read_bytes()).hexdigest(),'compiler_options':'Same pinnedJDK25 javac default options for both source variants','all_core_class_sha256':{p.relative_to(out).as_posix():hashlib.sha256(p.read_bytes()).hexdigest()for p in out.rglob('*.class')}}
rows=[]
for n,name in enumerate(['scalar','bulk','bulk','scalar']):
 d=R/'benchmarks'/name;out=R/'benchmarks'/('run-'+str(n)+'-'+name);started=datetime.datetime.now(datetime.timezone.utc).isoformat();cmd=[str(J/'java'),'-Xms256m','-Xmx256m','-cp',str(d)+':'+str(classes[name]),'com.shnewbs.deepprimal.core.CornerPacketBenchmark',str(out)];a=subprocess.run(cmd,capture_output=True,text=True);assert a.returncode==0,a.stderr
 row={'variant':name,'run':n,'start':started,'finish':datetime.datetime.now(datetime.timezone.utc).isoformat(),'rows':[json.loads(v)for v in a.stdout.splitlines()],'stderr':a.stderr,'pass':True,'command':cmd,'identity_before':identities[name]};(out/'result.json').write_text(json.dumps(row,indent=2)+'\n');rows.append(row);print(name,[(v['mask'],v['median_nanos']/1e6,v['p95_nanos']/1e6)for v in row['rows']],flush=True)
 for p,sha in identities[name]['all_core_class_sha256'].items():assert hashlib.sha256((classes[name]/p).read_bytes()).hexdigest()==sha
(R/'benchmark-results.json').write_text(json.dumps(rows,indent=2)+'\n');(R/'benchmark-identities.json').write_text(json.dumps(identities,indent=2)+'\n')
