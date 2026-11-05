from pathlib import Path
import json,hashlib,shutil,platform,subprocess,time
r=Path(__file__).resolve().parent;o=r.parent/'audit-2067e27-20261026T1339';p=r/'project';e=r/'evidence';e.mkdir(exist_ok=True);d=json.loads((r/'restoration-input.json').read_text());tree={x['path']:x for x in d['tree']['tree'] if x['type']=='blob'}
def gh(b):return hashlib.sha1(b'blob '+str(len(b)).encode()+b'\0'+b).hexdigest()
for n,x in tree.items():
 f=o/'project'/n;assert f.is_file() and gh(f.read_bytes())==x['sha'],n
 target=p/n;target.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(f,target)
for f in (o/'project/docs').rglob('*'):
 if f.is_file():
  target=p/f.relative_to(o/'project');target.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(f,target)
for x in d['files']:
 f=p/x['path'];f.parent.mkdir(parents=True,exist_ok=True);f.write_text(x['content']);assert gh(f.read_bytes())==x['sha'],x['path']
ident=[]
for f in p.rglob('*'):
 if f.is_file():
  n=f.relative_to(p).as_posix();b=f.read_bytes();h=gh(b);assert n not in tree or h==tree[n]['sha'];ident.append({'path':n,'git_blob':h,'sha256':hashlib.sha256(b).hexdigest()})
(e/'source-identity.json').write_text(json.dumps({'pass':True,'source_sha':d['sha'],'verified_inputs':len(ident),'files':ident},indent=2))
(r/'toolchain').symlink_to(o/'toolchain',target_is_directory=True);shutil.move(str(o/'gradle-cache'),str(r/'gradle-cache'))
names=['run_build.py','run_checks.py','validate_package.py','packaged_regressions.py','native_smoke.py','baselines.json','native-memory.init.gradle','strict-compile.init.gradle','source-excerpts.py','aquifer_native_smoke.py','finish_gates.py','post_native_verify.py','rolling_package.py','prepare_publication.py']
for name in names:
 s=(o/name).read_text().replace('2067e273e85fb4f80530c86a7c238106cbbb4cdf',d['sha']).replace('20261026T1339','20261026T1433').replace('2026-10-26T1339','2026-10-26T1433').replace('2067e27','9b26b86');(r/name).write_text(s)
for folder in ['precipitation-native','receiver-native']:
 (r/folder).mkdir(exist_ok=True)
 for f in (o/folder).glob('*.py'):(r/folder/f.name).write_text(f.read_text().replace(str(o.resolve()),str(r.resolve())))
v='0.0.1-alpha.29-dev.29-audit-20261026T1433-9b26b86';(r/'audit-version.gradle').write_text("gradle.beforeProject { p -> if (p.path != ':gui-themes') p.ext.mod_version = '"+v+"' }\n")
(r/'native-cache').mkdir();assets=r/'server-unused-assets';assets.mkdir();f=p/'build/moddev/minecraft_assets.properties';f.parent.mkdir(parents=True);f.write_text('assets_root='+str(assets)+'\nasset_index=server-unused\n')
(e/'lease.json').write_text(json.dumps({'role':'auditor','source_sha':d['sha'],'status':'active','builder_mutated':False,'isolated_cache_and_output':True,'timing_reservation_observed':'renderer component CPU benchmark claim active; audit heavy work deferred'},indent=2))
h=json.loads((o/'evidence/host.json').read_text());h.update(platform=platform.platform(),cpu=subprocess.check_output(['lscpu'],text=True),captured_utc=time.strftime('%Y-%m-%dT%H:%M:%SZ',time.gmtime()));(e/'host.json').write_text(json.dumps(h,indent=2))
t=r/'toolchain';d=json.loads((t/'restoration.json').read_text())
for a in d['assets']:
 f=t/a['asset'];assert f.stat().st_size==a['bytes'] and hashlib.sha256(f.read_bytes()).hexdigest()==a['sha256']
assert hashlib.sha256((t/'checked-agent/neoform-process-fallback.jar').read_bytes()).hexdigest()=='6feb7d68e9b0be38568cd32822e8180cd5a40dce9147df4a9542fbc2126fe8b4'
d.update(verify_only=True,verified_current_audit=True);(e/'toolchain-verification.json').write_text(json.dumps(d,indent=2));print('RESTORED',len(ident),v)
