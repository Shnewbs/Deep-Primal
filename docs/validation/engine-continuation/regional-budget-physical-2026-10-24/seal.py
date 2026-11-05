from pathlib import Path
import json,hashlib,gzip,zipfile,io,shutil
r=Path(__file__).resolve().parent;p=r/'project';e=r/'evidence';e.mkdir(exist_ok=False)
sha=lambda b:hashlib.sha256(b).hexdigest()
for name in ['build.py','server.py','audit_package.py','seal.py','build.log.gz','build-result.json','package-audit.json','native-package-audit.json','core-container-audit.json']:
 shutil.copyfile(r/name,e/name)
for role in ['server-initial','server-fixed','server-observed','server-physical','server-reopen']:
 dest=e/role;dest.mkdir()
 for name in ['console.log.gz','result.json','command.json','environment.txt','regional.txt']:
  src=r/role/name
  if src.exists():shutil.copyfile(src,dest/name)
 result=json.loads((dest/'result.json').read_text());raw=gzip.decompress((dest/'console.log.gz').read_bytes());assert sha(raw)==result['raw_sha256']
 if role in ['server-physical','server-reopen']:assert result['pass'] and not result['crashes']
 else:assert not result['pass'] and result['exit_code']==0 and len(result['crashes'])==1
for name in ['failed-ocean-site','failed-pruning-prediction','failed-held-outlets']:shutil.copytree(r/name,e/name)
candidate=p/'src/main/java/com/shnewbs/deepprimal/engine/HydrologyRuntime.java'
assert candidate.read_bytes()==Path('regional-budget-0233/candidate/HydrologyRuntime.java').read_bytes()
shutil.copyfile(candidate,e/'HydrologyRuntime.java')
shutil.copyfile(p/'src/visualValidation/java/com/shnewbs/deepprimal/validation/PhysicalRegionalChecks.java',e/'PhysicalRegionalChecks.java')
shutil.copyfile(p/'src/visualValidation/java/com/shnewbs/deepprimal/validation/EnvironmentalWaterChecks.java',e/'EnvironmentalWaterChecks.java')
manifest=json.loads(Path('native-budget-0327/source-manifest.json').read_text());changed=[]
for x in manifest:
 b=(p/x['path']).read_bytes()
 if hashlib.sha1(b'blob '+str(len(b)).encode()+b'\0'+b).hexdigest()!=x['sha']:changed.append(x['path'])
assert changed==['src/main/java/com/shnewbs/deepprimal/engine/HydrologyRuntime.java']
shutil.copyfile('native-budget-0327/source-manifest.json',e/'source-manifest.json')
build=json.loads((r/'build-result.json').read_text());assert build['pass']
audit=json.loads((r/'package-audit.json').read_text());assert audit['pass']
old=Path('native-budget-0327/project/build/libs/DeepPrimal-26.1.2-0.0.1-alpha.26-vulkan-linux.jar')
new=p/'build/libs'/audit['jar']
def leaves(data,prefix=''):
 result={}
 with zipfile.ZipFile(io.BytesIO(data)) as z:
  assert z.testzip() is None
  for name in sorted(z.namelist()):
   if name.endswith('/'):continue
   b=z.read(name)
   if name.endswith('.jar'):result.update(leaves(b,prefix+name+'!/'))
   else:result[prefix+name]=sha(b)
 return result
before=leaves(old.read_bytes());after=leaves(new.read_bytes());assert before==after
origin=gzip.decompress((r/'server-physical/console.log.gz').read_bytes()).decode()
origins=[x for x in origin.splitlines() if 'ENVIRONMENT_CLASS_ORIGIN' in x];assert len(origins)==4
record={'pass':True,'source_inputs_checked':len(manifest),'only_runtime_change':changed,'runtime_source_sha256':sha(candidate.read_bytes()),'native_class_origins':origins,'native_root_sha256':sha(old.read_bytes()),'fresh_build_root_sha256':sha(new.read_bytes()),'recursive_archive_leaf_members_equal':len(before),'all_native_and_fresh_build_leaf_members_identical':True,'raw_core_container_bytes_equal':json.loads((r/'core-container-audit.json').read_text())['raw_container_equal'],'note':'Relocated build changed ZIP container metadata; every member byte including all core resources/classes matches. Exact whole-container equivalence is not claimed.'}
(e/'source-artifact-identity.json').write_text(json.dumps(record,indent=2)+'\n')
summary={'checkpoint':'R48','parent':'e5bfb8ff2c2ee865d94650e6ac57bbc189942cf0','runtime_files_promoted':1,'new_runtime_logic_since_R46':False,'new_private_fixture_files':1,'full_builds_passed':1,'full_build_tasks':31,'accepted_packaged_server_processes':2,'rejected_private_fixture_game_runs':3,'rejected_game_crash_reports':3,'rejected_runs_gradle_exit_zero':True,'accepted_runs_crashes':0,'native_auxiliary_FULL_evictions':64,'native_auxiliary_new_chunk_instances':32,'native_outlet_new_chunk_instances':4,'real_auxiliary_unload_events_observed':64,'active_regions':2,'physical_churn_cycles':2,'same_interval_units_each':32,'following_interval_cumulative_units_each':64,'water_salt_sediment_conserved':True,'synthetic_events_or_cache_map_mutation':False,'clock_control':'Chunk-manager service loops keep level clock unchanged during eviction; next interval explicitly advances saved game time20. Not independent host ticks/60Hz.','save_reopen_passed':True,'archive_crcs':7,'compiled_class_identities':audit['compiled_class_matches'],'owner_installer_released':False,'vulkan_graphics_execution':False,'windows_ownerGPU_FPS_acceptance':False,'engine_acceptance_complete':False,'remaining':'Broader natural hydrology/season/storm/throughput and graphics/LOD/performance work remain open; next follow current-main terrain and Vulkan priorities.'}
(e/'summary.json').write_text(json.dumps(summary,indent=2)+'\n');print(json.dumps(summary))
