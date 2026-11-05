"""Fail before Gradle when a restored checkout omits resource-check inputs."""
from pathlib import Path
import argparse,hashlib,json,re
p=argparse.ArgumentParser();p.add_argument('project',type=Path);p.add_argument('--receipt',type=Path);a=p.parse_args()
paths=set(re.findall(r"project\.file\('([^']+)'\)", (a.project/'build.gradle').read_text()))
required=sorted(x for x in paths if x.startswith('docs/'))
if not required:raise RuntimeError('No resource-check documents discovered; review changed build script')
missing=[x for x in required if not (a.project/x).is_file()]
if missing:raise RuntimeError('Missing build resource inputs: '+', '.join(missing))
record={'pass':True,'required_docs':{x:hashlib.sha256((a.project/x).read_bytes()).hexdigest() for x in required}}
if a.receipt:a.receipt.write_text(json.dumps(record,indent=2))
print('BUILD_RESOURCE_INPUTS_PASS',len(required))
