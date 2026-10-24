from pathlib import Path
import json,hashlib,gzip,zipfile,shutil
r=Path(__file__).resolve().parent;p=r/'project';e=r/'evidence';e.mkdir(exist_ok=False)
sha=lambda b:hashlib.sha256(b).hexdigest()
manifest=json.loads((r/'source-manifest.json').read_text());changed=[]
for x in manifest:
 b=(p/x['path']).read_bytes();h=hashlib.sha1(b'blob '+str(len(b)).encode()+b'\0'+b).hexdigest()
 if h!=x['sha']:changed.append(x['path'])
assert changed==['src/main/java/com/shnewbs/deepprimal/engine/HydrologyRuntime.java']
assert (p/changed[0]).read_bytes()==Path('regional-budget-0233/candidate/HydrologyRuntime.java').read_bytes()
for name in ['build.py','server.py','audit_package.py','seal.py','source-manifest.json','build.log.gz','build-result.json','package-audit.json']:
 shutil.copyfile(r/name,e/name)
for role in ['server-initial','server-initial2','server-reopen']:
 dest=e/role;dest.mkdir()
 for name in ['console.log.gz','result.json','command.json','environment.txt','regional.txt']:
  src=r/role/name
  if src.exists():shutil.copyfile(src,dest/name)
 result=json.loads((r/role/'result.json').read_text());raw=gzip.decompress((r/role/'console.log.gz').read_bytes());assert sha(raw)==result['raw_sha256']
 if role!='server-initial':assert result['pass'] and b'BUILD SUCCESSFUL' in raw and b'All dimensions are saved' in raw
assert json.loads((r/'build-result.json').read_text())['pass']
for name in ['RegionalCacheChecks','EnvironmentalWaterChecks']:
 shutil.copyfile(p/f'src/visualValidation/java/com/shnewbs/deepprimal/validation/{name}.java',e/f'{name}.java')
shutil.copyfile(p/changed[0],e/'candidate-HydrologyRuntime.java')
shutil.copytree(r/'failed-private-compile',e/'failed-private-compile')
raw=gzip.decompress((e/'server-initial2/console.log.gz').read_bytes()).decode()
origins=[line for line in raw.splitlines() if 'ENVIRONMENT_CLASS_ORIGIN' in line]
assert len(origins)==4 and 'build/libs/DeepPrimal-' in origins[0]
jar=p/'build/libs/DeepPrimal-26.1.2-0.0.1-alpha.26-vulkan-linux.jar';audit=json.loads((r/'package-audit.json').read_text());assert sha(jar.read_bytes())==audit['sha256']
z=zipfile.ZipFile(jar);core,=[n for n in z.namelist() if '.DeepPrimal-Core-' in n];direct=p/'engine-core/build/libs/DeepPrimal-Core-0.0.1-alpha.26.jar';assert z.read(core)==direct.read_bytes()
source={'verified_git_source_inputs':len(manifest),'omitted_from_engine_build':['independent maritime-1.21.1 project','five standalone Themes wrapper/ignore files','.github and docs'],'only_runtime_change':changed,'candidate_sha256':sha((p/changed[0]).read_bytes()),'private_fixture_sha256':sha((e/'RegionalCacheChecks.java').read_bytes()),'native_class_origins':origins,'tested_core_matches_embedded_core':True,'archive_unchanged_across_native_sessions':True}
(e/'source-identity.json').write_text(json.dumps(source,indent=2)+'\n')
summary={'checkpoint':'R47','parent':'aa71dd1f1dd570139612766ba17c00800965c12f','runtime_changes_since_R46':0,'new_private_fixture_files':1,'full_builds_passed':1,'tasks':31,'packaged_server_sessions_passed':2,'private_compile_attempts_failed':1,'crashes':0,'archive_crcs':7,'compiled_class_matches':audit['compiled_class_matches'],'real_loaded_column_normal_units':32,'real_loaded_column_after_injected_unload_units':32,'exact_native_water_conservation':True,'event_dispatched_on_real_neoforge_bus':True,'cache_population_controlled_by_fixture':True,'physical_chunk_eviction_and_reload_validated':False,'save_reopen_aquifer_composition_V4_and_edits':True,'vulkan_graphics_execution':False,'windows_execution':False,'performance_acceptance':False,'candidate_promoted':False,'installer_released':False,'acceptance_complete':False,'next':'Real server-driven physical unload/reload, repeated cache churn and multiple-region throughput before promotion; then continue terrain and bounded Vulkan publication.'}
(e/'summary.json').write_text(json.dumps(summary,indent=2)+'\n');print(json.dumps(summary))
