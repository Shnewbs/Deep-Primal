from pathlib import Path
import json,gzip,hashlib,sys
def audit(case,build=False):
 if build:
  d=json.loads((case/'candidate-build-result.json').read_text());raw=gzip.decompress((case/'candidate-build-final.gz').read_bytes());live=(case/'candidate-build.log').read_bytes();digest=d['raw_sha256'];assert d['pass']and d['exit_code']==0
 else:
  d=json.loads((case/'result.json').read_text());raw=gzip.decompress((case/'console-final.gz').read_bytes());live=(case/'console.log').read_bytes();digest=d['console_sha256'];assert d['exit_code']==0 and d['build_successful']and not d['timeout'];sealed=json.loads((case/'sealed-console.json').read_text());assert sealed['raw_sha256']==digest and sealed['stable_samples']>=4
 assert raw==live and hashlib.sha256(raw).hexdigest()==digest
 return {'case':case.name,'pass':True,'raw_and_sealed_bytes_identical':True,'sha256':digest,'bytes':len(raw),'build':build}
if __name__=='__main__':
 b=Path(sys.argv[1]);rows=[audit(b/n)for n in ['vulkan-batch-r40-control','vulkan-corners-shared-final-quad','vulkan-corners-shared-final-quad-packaged','vulkan-reuse-gl-corners-shared-final-quad']]+[audit(b/n,True)for n in ['build-baseline','build-candidate']];print(json.dumps({'pass':True,'sessions':rows},indent=2))
