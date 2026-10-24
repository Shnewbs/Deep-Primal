from pathlib import Path
import argparse,hashlib,json,subprocess,shutil
parser=argparse.ArgumentParser();parser.add_argument('--checkout',type=Path);parser.add_argument('--java-home',type=Path,required=True);a=parser.parse_args()
e=Path(__file__).resolve().parent;r=e.parent;meta=json.loads((e/'parent-tree.json').read_text());checks=json.loads((e/'checks-tree.json').read_text())
if a.checkout:
 for item in meta['core']:
  dest=r/'core'/item['path'].split('/core/',1)[1];dest.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(a.checkout/item['path'],dest)
 shutil.copyfile(a.checkout/meta['profile']['path'],e/'TerrainProfile.java')
 for item in checks:shutil.copyfile(a.checkout/item['path'],e/Path(item['path']).name)
def verify():
 result={}
 for item in meta['core']+[meta['profile']]+checks:
  p=r/'core'/item['path'].split('/core/',1)[1] if item in meta['core'] else e/Path(item['path']).name
  data=p.read_bytes();assert hashlib.sha1(b'blob '+str(len(data)).encode()+b'\0'+data).hexdigest()==item['sha'],str(p)
  result[item['path']]=hashlib.sha256(data).hexdigest()
 return result
before=verify();sources=sorted((r/'core').rglob('*.java'));out=r/'classes';out.mkdir(exist_ok=True);java=a.java_home/'bin'
cmd=[str(java/'javac'),'-g','-d',str(out),*map(str,sources),str(e/'BalanceAudit.java'),*[str(e/Path(f['path']).name) for f in checks]]
b=subprocess.run(cmd,capture_output=True,text=True);(e/'compile.txt').write_text(b.stdout+b.stderr);assert b.returncode==0,b.stderr
results={}
for cls,record in [('BalanceAudit','result.txt'),('TerrainV6Checks','TerrainV6Checks-result.txt'),('DepositPlanChecks','DepositPlanChecks-result.txt')]:
 with (e/record).open('w') as log:b=subprocess.run([str(java/'java'),'-Xmx512m','-cp',str(out),'com.shnewbs.deepprimal.core.'+cls],stdout=log,stderr=subprocess.STDOUT)
 text=(e/record).read_text();assert b.returncode==0 and 'PASS' in text,text
 results[cls]={'exit':b.returncode,'record_sha256':hashlib.sha256((e/record).read_bytes()).hexdigest()};print(text,flush=True)
assert before==verify()
classes={str(f.relative_to(out)):hashlib.sha256(f.read_bytes()).hexdigest() for f in sorted(out.rglob('*.class'))}
version=subprocess.run([str(java/'java'),'-version'],capture_output=True,text=True)
(e/'identity.json').write_text(json.dumps({'parent':meta['parent'],'java':version.stdout+version.stderr,'sources':before,'fixture_sha256':hashlib.sha256((e/'BalanceAudit.java').read_bytes()).hexdigest(),'classes':classes,'checks':results,'pass':True},indent=2)+'\n')
