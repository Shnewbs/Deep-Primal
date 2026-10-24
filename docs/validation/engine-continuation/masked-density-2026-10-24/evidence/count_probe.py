from pathlib import Path
import subprocess,json,hashlib
r=Path(__file__).resolve().parent.parent;jdk=r.parent/'lod-future-r57/toolchain/jdk-25.0.4.1+1/bin';records=[]
for mode,src in [('control',r/'control-core'),('candidate',r/'candidate')]:
 dest=r/('instrumented-'+mode);dest.mkdir(exist_ok=True);sources=list(src.rglob('*.java'));copies=[]
 for p in sources:
  if p.name=='DensityGraph.java':
   text=p.read_text().replace('  private final int root;','  public long counted3D;\n  private final int root;').replace('case NOISE_3D -> p.noise().sample(x,y,z);','case NOISE_3D -> {counted3D++;yield p.noise().sample(x,y,z);}')
   target=dest/'DensityGraph.java';target.write_text(text);copies.append(target)
  else:copies.append(p)
 out=dest/'classes';out.mkdir(exist_ok=True)
 cp=subprocess.run([str(jdk/'javac'),'-g','-d',str(out),*map(str,copies),str(r/'evidence/CountProbe.java')],capture_output=True,text=True,timeout=60);assert cp.returncode==0,cp.stderr
 p=subprocess.run([str(jdk/'java'),'-cp',str(out),'CountProbe'],capture_output=True,text=True,timeout=60);assert p.returncode==0,p.stderr
 records.append({'mode':mode,'output':p.stdout,'instrumented_source_sha256':hashlib.sha256((dest/'DensityGraph.java').read_bytes()).hexdigest()});print(mode,p.stdout)
assert records[0]['output'].split('checksum=')[1]==records[1]['output'].split('checksum=')[1]
(r/'evidence/count-probe.json').write_text(json.dumps({'scope':'Diagnostic instrumented source copy; excluded from all timings/product sources','records':records,'checksum_matches':True},indent=2)+'\n')
