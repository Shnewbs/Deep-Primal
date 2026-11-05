from pathlib import Path
import json,hashlib,gzip,zipfile,io,subprocess,statistics,math,difflib
r=Path(__file__).resolve().parent;p=r/'project';e=r/'evidence';e.mkdir(exist_ok=True)
def read(path):return json.loads((r/path).read_text())
def sha(b):return hashlib.sha256(b).hexdigest()
def git(b):return hashlib.sha1(b'blob '+str(len(b)).encode()+b'\0'+b).hexdigest()
def dump(path,data):
 f=e/path;f.parent.mkdir(parents=True,exist_ok=True);f.write_text(json.dumps(data,indent=2)+'\n')
subprocess.run(['python',str(r/'audit_package.py')],check=True,stdout=subprocess.DEVNULL)
package=read('package-audit.json');build=read('build-result.json');assert package['pass'] and build['pass']
assert sha(gzip.decompress((r/'build.log.gz').read_bytes()))==build['raw_sha256']
independent=read('independent-core-audit.json');expected=read('independent-compile.json')
for kind,project in [('control','baseline-project'),('candidate','project')]:
 assert independent[kind]['source_files']==44 and independent[kind]['classes_match']==71
 for name,h in independent[kind]['source_sha256'].items():assert sha((r/project/name).read_bytes())==h
 for name,h in independent[kind]['class_sha256'].items():assert sha((r/project/'engine-core/build/classes/java/main'/name).read_bytes())==h
names=['baseline-guard-1-reopen','candidate-guard-1-reopen','candidate-guard-2-reopen','baseline-guard-2-reopen']
records=[];coverage=[];native=[];fixtures=set()
legacy=Path('/workspace/scratch/a22609fabda0/versioned-deposits-0632/evidence/server-v5-verified-reopen/deposits.json')
old=json.loads(legacy.read_text())
for name in names:
 data=read('server-'+name+'/result.json');d=data['deposit_record'];c=data['coverage_record'];kind='control' if name.startswith('baseline') else 'candidate';project='baseline-project' if kind=='control' else 'project'
 assert data['pass'] and data['stopped'] and data['saved'] and not data['crashes']
 raw=gzip.decompress((r/('server-'+name)/'console.log.gz').read_bytes());assert sha(raw)==data['raw_sha256']
 assert b'Stopping server' in raw and b'All dimensions are saved' in raw
 assert c['fixture_schema']==2 and c['deposit_class_sha256']==expected[kind]['com/shnewbs/deepprimal/core/DepositField.class']
 assert d['hashes']==old['hashes'] and d['saved_revision']==5 and d['reopen_edit_verified']
 assert d['full_saved_chunk_blocks']==240260 and c['witnesses']==128 and c['seeds']==16
 assert len(c['ore_types'])==8 and set(c['ore_types'].values())=={16}
 for cls,record in [('DepositNativeChecks',d),('DepositCoverageChecks',c)]:
  f=r/project/'build/classes/java/visualValidation/com/shnewbs/deepprimal/validation'/str(cls+'.class')
  assert sha(f.read_bytes())==record['fixture_class_sha256']
 fixtures.add(c['fixture_class_sha256'])
 jar=r/project/'build/libs/DeepPrimal-26.1.2-0.0.1-alpha.26-vulkan-linux.jar';outer=zipfile.ZipFile(jar)
 core=r/project/'engine-core/build/libs/DeepPrimal-Core-0.0.1-alpha.26.jar';external=zipfile.ZipFile(core)
 nested,=[n for n in outer.namelist() if 'DeepPrimal-Core-' in n and n.endswith('.jar')];inside=zipfile.ZipFile(io.BytesIO(outer.read(nested)))
 assert set(inside.namelist())==set(external.namelist()) and all(inside.read(n)==external.read(n) for n in inside.namelist())
 assert all(sha(external.read(n))==h for n,h in independent[kind]['class_sha256'].items())
 native.append({'session':name,'variant':kind,'pass':True,'raw_console_sha256':data['raw_sha256'],'root_jar_sha256':sha(jar.read_bytes()),'loaded_deposit_class_sha256':c['deposit_class_sha256'],'fixture_class_sha256':c['fixture_class_sha256'],'saved_revision':5,'edit_reopened':True,'crashes':0})
 records.append(data);coverage.append(c)
assert len(fixtures)==1 and all(c['hashes']==coverage[0]['hashes'] for c in coverage)
assert all(c['native_counts']==coverage[0]['native_counts'] for c in coverage)
assert all(x['root_jar_sha256']==package['sha256'] for x in native if x['variant']=='candidate')
def stats(rows,key):
 v=sorted(x[key] for x in rows)
 return {'min':v[0],'median':statistics.median(v),'p95':v[math.ceil(.95*len(v))-1],'max':v[-1],'samples':len(v)}
def costs(group):
 cs=[read('server-'+n+'/coverage.json') for n in group];rows=[]
 for i in range(4):
  a=[x for j,c in enumerate(cs) if j in [0,3] for x in c['profile_samples'] if x['site_index']==i]
  b=[x for j,c in enumerate(cs) if j in [1,2] for x in c['profile_samples'] if x['site_index']==i]
  av=stats(a,'thread_allocated_bytes');bv=stats(b,'thread_allocated_bytes');at=stats(a,'generation_ns');bt=stats(b,'generation_ns')
  rows.append({'site':cs[0]['profile_sites'][i],'control_allocated_bytes':av,'candidate_allocated_bytes':bv,'observed_median_bytes_saved':av['median']-bv['median'],'modeled_array_bytes_avoided':360*(256-cs[0]['profile_sites'][i]['active_columns']),'control_generation_ns':at,'candidate_generation_ns':bt,'p95_change_percent':100*(bt['p95']/at['p95']-1)})
 return rows
finalcost=costs(names);assert all(x['observed_median_bytes_saved']>0 for x in finalcost)
earlier=costs(['baseline-2-reopen','candidate-1-reopen','candidate-2-reopen','baseline-3-reopen'])
dump('native-costs.json',{'final':finalcost,'intermediate_early_rejection':earlier,'scope':coverage[0]['profile_scope'],'latency_acceptance':False,'reason':'Mixed final medians/p95; initial early-rejection variant p95 worse in all four pooled cases. Host/JIT/allocation variation is unproven, not attributed to the source change.'})
rocks=['GRANITE','ANDESITE','DIORITE','BASALT','TUFF','SANDSTONE','CALCITE','DEEPSLATE']
allowed={'COAL':['SANDSTONE','TUFF'],'COPPER':['ANDESITE','CALCITE'],'IRON':rocks,'GOLD':['GRANITE','DIORITE'],'REDSTONE':['BASALT','GRANITE'],'LAPIS':['CALCITE'],'DIAMOND':['BASALT','DEEPSLATE'],'EMERALD':['CALCITE','GRANITE']}
pairs={a+':'+b for a,v in allowed.items() for b in v};observed=set(coverage[0]['ore_hosts']);assert observed<=pairs
dump('strict-native-audit.json',{'pass':True,'sessions':native,'legacy_V1_V4_chunk_digests_match':96,'all_R50_native_digests_match':120,'expanded_V5_chunk_digests_match':len(coverage[0]['hashes']),'ore_witnesses':128,'seeds':16,'all_eight_ore_types':coverage[0]['ore_types'],'ore_host_pairs':sorted(observed),'compatible_pairs':len(pairs),'missing_host_pairs':sorted(pairs-observed),'native_counts':coverage[0]['native_counts'],'footprint_histogram':coverage[0]['footprints'],'latency_or_visual_acceptance':False})
paths=['engine-core/src/main/java/com/shnewbs/deepprimal/core/'+n+'.java' for n in ['Geology','DepositField','TerrainEngine']]
paths+=['src/main/java/com/shnewbs/deepprimal/engine/'+n+'.java' for n in ['EngineChunkGenerator','EngineRegistry']]
paths+=['src/coreChecks/java/com/shnewbs/deepprimal/core/'+n+'.java' for n in ['CoreChecks','DepositFieldChecks','DepositAllocationChecks','R50DepositField']]
paths+=['src/visualValidation/java/com/shnewbs/deepprimal/validation/'+n+'.java' for n in ['DepositNativeChecks','DepositCoverageChecks']]
tree={x['path']:x['sha'] for x in read('git-tree.json') if x['type']=='blob'};manifest=read('source-manifest.json');different=[]
for item in manifest:
 if git((p/item['path']).read_bytes())!=tree[item['path']]:different.append(item['path'])
assert set(different)==set(paths)&set(tree)
staged=[];patch='';since='';r50=Path('/workspace/scratch/a22609fabda0/versioned-deposits-0632')
for path in paths:
 b=(p/path).read_bytes();f=e/'staged'/path;f.parent.mkdir(parents=True,exist_ok=True);f.write_bytes(b)
 staged.append({'path':path,'bytes':len(b),'sha256':sha(b),'git_sha':git(b)})
 before=(r50/'baseline-project'/path).read_bytes() if path in tree else b''
 if path.endswith('/Geology.java'):before=Path('/workspace/scratch/a22609fabda0/deposit-field-0532/baseline/Geology.java').read_bytes()
 if path in tree:assert git(before)==tree[path]
 patch+=''.join(difflib.unified_diff(before.decode().splitlines(True),b.decode().splitlines(True),fromfile='a/'+path,tofile='b/'+path))
 prior=r50/'project'/path;before=prior.read_bytes() if prior.exists() else b''
 since+=''.join(difflib.unified_diff(before.decode().splitlines(True),b.decode().splitlines(True),fromfile='a/'+path,tofile='b/'+path))
(e/'candidate.patch').write_text(patch);(e/'since-R50.patch').write_text(since);dump('staged-manifest.json',staged)
dump('source-scope-audit.json',{'pass':True,'parent':'02afc23d5241be07704a653905503ed6b7bd8eda','original_inputs_verified':len(manifest),'changed_existing_inputs':different,'staged_production_files':5,'production_files_changed_since_R50':1,'staged_private_files':6,'public_production_changed':False,'default_or_saved_geography_changed':False})
summary={'checkpoint':'R51','source_parent':'02afc23d5241be07704a653905503ed6b7bd8eda','scope':'staged lazy deposit metadata arrays, native all-ore coverage and source-to-bytecode guards','production_files_changed_since_R50':1,'staged_production_files':5,'staged_private_files':6,'candidate_promoted':False,'public_production_source_changed':False,'final_31task_build_passed':True,'core_R50_comparisons':4194304,'ore_hits':1542,'independent_oracle_cases':90960,'independently_compiled_sources_per_variant':44,'independently_matched_classes_per_variant':71,'archive_crcs':7,'compiled_product_class_matches':477,'final_matched_native_processes':4,'final_crashes':0,'native_ore_witnesses':128,'native_seeds':16,'native_ore_types':8,'observed_host_pairs':len(observed),'compatible_host_pairs':len(pairs),'expanded_V5_digest_matches':128,'legacy_V1_V4_digest_matches':96,'saved_V5_edit_reopen_pass':True,'native_bytes_saved_pooled_median_range':[min(x['observed_median_bytes_saved'] for x in finalcost),max(x['observed_median_bytes_saved'] for x in finalcost)],'latency_acceptance':False,'excluded_attempts':2,'excluded_game_crashes':0,'installer_released':False,'graphics_or_ownerGPU_FPS_acceptance':False,'engine_acceptance_complete':False,'next':'Resolve matched tail-latency context and missing7host pairs, broader density/overlap balance and Vulkan client visual transects before V5 promotion; continue terrain authoring, bounded Vulkan water publication and LOD/pressure/water acceptance.'}
dump('summary.json',summary)
dump('excluded-attempts.json',[{'path':'failed-output-snapshot','accepted':False,'reason':'Gradle unreadable .rsync-tmp output before Minecraft; cleaned own outputs and retried; cause unproven'},{'path':'rejected-stale-full-build','accepted_for_revised_source':False,'reason':'Gradle31task success and compiled/package equality retained intermediate outer classc60d43 instead of source-derived15411; outputs cleared and independently verified retry required; cause unproven'}])
copy=['build.py','server.py','matched_server.py','fresh-compile.gradle','audit_package.py','audit_final.py','profile.py','verify_core.py','DepositCost.java','FindCoverage.java','coverage-sites.json','coverage-search.txt','R50-DepositField.java','source-manifest.json','build-result.json','build.log.gz','package-audit.json','allocation-core.txt','oracle-core.txt','independent-core-audit.json','independent-compile.json','cost-summary-sparse.json','cost-summary-dense.json']
for directory in ['profiles-sparse','profiles-dense','intermediate-early-rejection','rejected-stale-full-build','failed-output-snapshot']:
 for f in (r/directory).rglob('*'):
  if f.is_file() and f.suffix not in ['.jar','.class'] and f.name!='console.log':copy.append(f.relative_to(r).as_posix())
for path in copy:
 f=e/path;f.parent.mkdir(parents=True,exist_ok=True);f.write_bytes((r/path).read_bytes())
# Preserve all native records compactly, without duplicating their large JSON payloads.
for directory in sorted(r.glob('server-*')):
 if not directory.is_dir():continue
 for filename in ['console.log.gz','command.json','deposits.json','coverage.json']:
  f=directory/filename
  if f.exists():out=e/directory.name/filename;out.parent.mkdir(parents=True,exist_ok=True);out.write_bytes(f.read_bytes())
 if (directory/'result.json').exists():
  data=json.loads((directory/'result.json').read_text());original=sha((directory/'result.json').read_bytes())
  for key,filename in [('deposit_record','deposits.json'),('coverage_record','coverage.json')]:
   if key in data:data.pop(key);data[key+'_file']=filename
  data['original_full_result_sha256']=original;dump(directory.name+'/result.json',data)
f=r/'rejected-stale-full-build/observed-DepositField.class';(e/'rejected-stale-full-build/observed-DepositField.class.gz').write_bytes(gzip.compress(f.read_bytes(),mtime=0))
print(json.dumps(summary))
