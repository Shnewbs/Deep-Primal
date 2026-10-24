from pathlib import Path
import subprocess,json,sys
r=Path(__file__).resolve().parent;p=r/'engine-review';e=r/'review-evidence';java=r/'toolchain/jdk-25.0.4.1+1/bin/java'
cp=str(p/'engine-core/build/classes/java/main')+':'+str(p/'build/classes/java/noiseBenchmark')
for i in range(1,4):
 cmd=[str(java),'-Xms512m','-Xmx512m','-cp',cp,'com.shnewbs.deepprimal.benchmark.NoiseBenchmark']
 out=subprocess.run(cmd,stdout=subprocess.PIPE,stderr=subprocess.STDOUT);(e/f'noise-benchmark-{i}.txt').write_bytes(out.stdout)
 print(out.stdout.decode());assert out.returncode==0 and b'NOISE_BENCHMARK_COMPLETE' in out.stdout
cp=str(p/'engine-core/build/classes/java/main')+':'+str(p/'build/classes/java/coreChecks');fingerprints={}
for name,flags in [('jit',[]),('interpreter',['-Xint']),('no-avx',['-XX:UseAVX=0'])]:
 out=subprocess.check_output([str(java),*flags,'-cp',cp,'com.shnewbs.deepprimal.core.density.NoiseCoreChecks','fingerprint'],text=True)
 fingerprints[name]=out.strip();print(name,out.strip())
assert len(set(fingerprints.values()))==1
(e/'determinism.json').write_text(json.dumps({'pass':True,'same_host_only':True,'modes':fingerprints},indent=2))
