from pathlib import Path
import json,hashlib,gzip,zipfile,io,subprocess,difflib
r=Path(__file__).resolve().parent;p=r/'project';e=r/'evidence';e.mkdir(exist_ok=True)
def read(path):return json.loads((r/path).read_text())
def dump(path,data):
 f=e/path;f.parent.mkdir(parents=True,exist_ok=True);f.write_text(json.dumps(data,indent=2)+'\n')
def sha(b):return hashlib.sha256(b).hexdigest()
def git(b):return hashlib.sha1(b'blob '+str(len(b)).encode()+b'\0'+b).hexdigest()
subprocess.run(['python',str(r/'audit_package.py')],check=True,stdout=subprocess.DEVNULL)
build=read('build-result.json');package=read('package-audit.json');assert build['pass'] and package['pass']
assert 'fresh-compile.gradle' in ' '.join(build['command'])
assert sha(gzip.decompress((r/'build.log.gz').read_bytes()))==build['raw_sha256']
base=read('server-baseline-verified/deposits.json');candidate=read('server-v5-verified-reopen/deposits.json')
assert len(base['hashes'])==96 and all(candidate['hashes'][k]==v for k,v in base['hashes'].items())
assert candidate['reopen_edit_verified'] and candidate['saved_revision']==5 and candidate['changed_material_blocks']==58
assert candidate['reversed_chunks']==24 and candidate['oracle_blocks']==1305522
for key in ['cave_air_blocks','water_blocks','soil_band_blocks','bedrock_band_blocks']:assert candidate[key]>0
native=[]
for name,project in [('baseline-verified','baseline-project'),('v5-verified-reopen','project')]:
 record=read('server-'+name+'/result.json');data=record['deposit_record'];raw=gzip.decompress((r/('server-'+name)/'console.log.gz').read_bytes())
 assert record['pass'] and record['saved'] and record['stopped'] and not record['crashes']
 assert sha(raw)==record['raw_sha256'] and b'Stopping server' in raw and b'All dimensions are saved' in raw
 assert data['fixture_schema']==2 and data['sites_count']==12
 classfile=r/project/'build/classes/java/visualValidation/com/shnewbs/deepprimal/validation/DepositNativeChecks.class'
 assert data['fixture_class_sha256']==sha(classfile.read_bytes())
 (e/(name+'-fixture.class.gz')).write_bytes(gzip.compress(classfile.read_bytes(),mtime=0))
 core=r/project/'engine-core/build/libs/DeepPrimal-Core-0.0.1-alpha.26.jar'
 jar=r/project/'build/libs/DeepPrimal-26.1.2-0.0.1-alpha.26-vulkan-linux.jar'
 outer=zipfile.ZipFile(jar);nameIn,=[n for n in outer.namelist() if 'DeepPrimal-Core-' in n and n.endswith('.jar')]
 embedded=zipfile.ZipFile(io.BytesIO(outer.read(nameIn)));external=zipfile.ZipFile(core)
 assert set(embedded.namelist())==set(external.namelist())
 assert all(embedded.read(n)==external.read(n) for n in embedded.namelist())
 assert data['root_origin'].endswith(jar.name) and data['core_origin'].endswith(core.name)
 native.append({'session':name,'pass':True,'fixture_class_sha256':data['fixture_class_sha256'],'compiled_fixture_matches_runtime':True,'root_jar_sha256':sha(jar.read_bytes()),'core_members_match_embedded':True,'explicit_stop_and_saves':True,'crashes':0})
assert native[-1]['root_jar_sha256']==package['sha256']
profile=read('cost-summary.json');assert profile['matched_query_checksums']['control']==profile['matched_query_checksums']['candidate']
assert profile['results']['candidate']['candidate_query_ns_bytes_sum']['allocated_bytes_per_operation']==[0.0]
paths=['engine-core/src/main/java/com/shnewbs/deepprimal/core/'+n+'.java' for n in ['Geology','DepositField','TerrainEngine']]
paths+=['src/main/java/com/shnewbs/deepprimal/engine/'+n+'.java' for n in ['EngineChunkGenerator','EngineRegistry']]
paths+=['src/coreChecks/java/com/shnewbs/deepprimal/core/'+n+'.java' for n in ['CoreChecks','DepositFieldChecks']]
paths+=['src/visualValidation/java/com/shnewbs/deepprimal/validation/DepositNativeChecks.java']
tree={x['path']:x['sha'] for x in read('git-tree.json') if x['type']=='blob'};manifest=read('source-manifest.json');differ=[]
for item in manifest:
 b=(p/item['path']).read_bytes()
 if git(b)!=tree[item['path']]:differ.append(item['path'])
assert set(differ)==set(paths)&set(tree),differ
staged=[];patch=''
for path in paths:
 b=(p/path).read_bytes();out=e/'staged'/path;out.parent.mkdir(parents=True,exist_ok=True);out.write_bytes(b)
 staged.append({'path':path,'bytes':len(b),'sha256':sha(b),'git_sha':git(b)})
 if path in tree:
  baseline=r/'baseline-project'/path
  if path.endswith('/Geology.java'):baseline=Path('/workspace/scratch/a22609fabda0/deposit-field-0532/baseline/Geology.java')
  before=baseline.read_bytes();assert git(before)==tree[path],path
 else:before=b''
 patch+=''.join(difflib.unified_diff(before.decode().splitlines(True),b.decode().splitlines(True),fromfile='a/'+path,tofile='b/'+path))
(e/'candidate.patch').write_text(patch);dump('staged-manifest.json',staged)
dump('source-scope-audit.json',{'pass':True,'source_parent':'75cdb6f057cacdccac41fc4155d0621b747d5822','verified_original_inputs':len(manifest),'changed_existing_inputs':differ,'new_files':[x for x in paths if x not in tree],'production_files_staged':5,'validation_files_staged':3,'public_production_source_changed':False,'default_presets_unchanged':True})
dump('strict-native-audit.json',{'pass':True,'final_sessions':native,'legacy_chunk_digest_matches':96,'base_column_blocks_per_candidate':candidate['base_column_blocks'],'oracle_blocks_per_candidate':candidate['oracle_blocks'],'protected_blocks_per_candidate':candidate['protected_blocks'],'first_changed':candidate['first_changed'],'native_density_counts':candidate['density'],'saved_revision':5,'player_edit_reopen':True,'full_saved_chunk_blocks':candidate['full_saved_chunk_blocks'],'broad_density_balance_or_visual_acceptance':False})
failures=[{'path':'failed-compile/server-baseline','kind':'private fixture switch-expression syntax; no Minecraft launch'},
 {'path':'failed-resources-baseline','kind':'generated Themes destination temporary file vanished; no Minecraft launch; cause unproven'},
 {'path':'failed-empty-coverage/server-candidate','kind':'fixture picked no changed ore, assertion crashed game despite Gradle0; excluded'},
 {'path':'server-baseline-final','kind':'expanded source had12sites but compiled fixture retained8; excluded as expanded control; cause unproven; rebuilt nonincremental'}]
dump('excluded-attempts.json',failures)
summary={'checkpoint':'R50','source_parent':'75cdb6f057cacdccac41fc4155d0621b747d5822','scope':'staged opt-in V5 native adapter and conservative footprint pruning','production_files_staged':5,'validation_files_staged':3,'promoted':False,'default_or_existing_saved_revisions_changed':False,'final_full_build_tasks':31,'final_full_build_pass':True,'intermediate_full_builds_pass':2,'archive_crcs':7,'compiled_product_class_matches':477,'core_oracle_cases':90960,'source_candidate_limit':9,'raw_metadata_array_bytes':324,'native_legacy_digest_matches':96,'base_column_native_blocks':candidate['base_column_blocks'],'ore_oracle_native_blocks':candidate['oracle_blocks'],'changed_material_blocks':58,'cave_air_blocks':candidate['cave_air_blocks'],'water_blocks':candidate['water_blocks'],'soil_blocks':candidate['soil_band_blocks'],'reversed_chunks':24,'saved_revision':5,'player_edit_reopen_pass':True,'full_saved_chunk_blocks':240260,'final_native_sessions_crashes':0,'excluded_attempts':4,'excluded_game_crashes':1,'microprobe_query_change_percent':profile['candidate_query_change_percent'],'microprobe_build_allocated_bytes_per_column':480,'microprobe_additional_bytes_over_legacy_host':392,'microprobe_extra_bytes_over_unpruned_field':8,'query_allocated_bytes':0,'vulkan_graphics_or_owner_GPU_FPS':False,'installer_released':False,'engine_acceptance_complete':False,'remaining':'Broader all-ore/host/density/overlap balance, client visual transects, matched full chunk generation/allocation profiles before promotion. Then terrain authoring/caves, bounded Vulkan water publication and LOD/pressure/water budget acceptance.'}
dump('summary.json',summary)
copy=['build.py','server.py','fresh-compile.gradle','audit_package.py','audit_final.py','profile.py','DepositCost.java','FindDeposit.java','witness-search.txt','baseline-fixture.java','baseline-verified-fixture.java','source-manifest.json','build-result.json','build.log.gz','package-audit.json','cost-summary.json','cost-r49-field.json','cost-pruned-field.json','pruned-oracle.txt']
for directory in ['profiles','pre-pruning','pre-fresh-build','server-baseline','server-baseline-final','server-baseline-verified','server-v5-initial','server-v5-reopen','server-v5-verified-reopen','failed-compile','failed-resources-baseline','failed-empty-coverage','stale-baseline-fixture']:
 for f in (r/directory).rglob('*'):
  if f.is_file() and f.suffix not in ['.jar','.class'] and f.name!='console.log':copy.append(f.relative_to(r).as_posix())
for path in copy:
 out=e/path;out.parent.mkdir(parents=True,exist_ok=True);out.write_bytes((r/path).read_bytes())
print(json.dumps(summary))
