from pathlib import Path
import json,hashlib,gzip,shutil,difflib
r=Path(__file__).resolve().parent;p=r/'project';e=r/'evidence';e.mkdir(exist_ok=False)
files=['engine-core/src/main/java/com/shnewbs/deepprimal/core/Geology.java','engine-core/src/main/java/com/shnewbs/deepprimal/core/DepositField.java','src/coreChecks/java/com/shnewbs/deepprimal/core/CoreChecks.java','src/coreChecks/java/com/shnewbs/deepprimal/core/DepositFieldChecks.java']
staged=[]
for name in files:
 b=(p/name).read_bytes();dst=e/'staged'/name;dst.parent.mkdir(parents=True,exist_ok=True);dst.write_bytes(b)
 staged.append({'path':name,'sha256':hashlib.sha256(b).hexdigest(),'bytes':len(b)})
(e/'staged-manifest.json').write_text(json.dumps(staged,indent=2)+'\n')
for name in ['build.py','audit_package.py','seal.py','LegacyOreDigest.java','legacy-audit.json','baseline-legacy.txt','candidate-legacy.txt','final-candidate-legacy.txt','quick-check.txt','quick-compile.txt','source-manifest.json','build-result.json','build.log.gz','package-audit.json']:
 shutil.copyfile(r/name,e/name)
shutil.copytree(r/'baseline',e/'baseline');shutil.copytree(r/'intermediate-build',e/'intermediate-build')
for base in [e,e/'intermediate-build']:
 d=json.loads((base/'build-result.json').read_text());raw=gzip.decompress((base/'build.log.gz').read_bytes());assert hashlib.sha256(raw).hexdigest()==d['raw_sha256'];assert d['pass'] and all(d['required'].values());assert b'DEPOSIT_FIELD_CORE_PASS cases=90960 clippedV4=1072' in raw
legacy=json.loads((e/'legacy-audit.json').read_text());assert legacy['pass'] and legacy['results'][0]==legacy['results'][1]==legacy['final_candidate_result']
archive=json.loads((e/'package-audit.json').read_text());assert archive['pass'] and archive['private_validation_absent']
patch=''
for name in files:
 old=Path('physical-budget-0424/project')/name
 before=old.read_text().splitlines(True) if old.exists() else []
 patch+=''.join(difflib.unified_diff(before,(p/name).read_text().splitlines(True),fromfile='a/'+name if before else '/dev/null',tofile='b/'+name))
(e/'candidate.patch').write_text(patch)
summary={'checkpoint':'R49','parent':'24b32f4dff107c2a8b9f369aea268eb04f20a29a','scope':'staged core deposit-lookup candidate; not active terrain integration','staged_core_files':2,'staged_private_validation_files':2,'final_full_builds_passed':1,'intermediate_full_builds_passed':1,'tasks_each':31,'deposit_oracle_cases':90960,'clipped_legacy_lookup_witnesses':1072,'signed_cell_seam_queries':25000,'query_candidate_limit':9,'wider_oracle_candidates':49,'metadata_array_bytes_per_column':324,'metadata_excludes_object_headers_host_reference_and_process_peak':True,'legacy_geology_cases_per_version':4280000,'legacy_digest_match':True,'archive_crcs':7,'compiled_class_identities':archive['compiled_class_matches'],'failed_checks':0,'native_sessions':0,'performance_or_visual_acceptance':False,'candidate_promoted':False,'existing_generator_or_saved_revision_changed':False,'public_production_source_changed':False,'installer_released':False,'engine_acceptance_complete':False,'next':'Integrate behind an explicit new saved terrain revision, preserve existing V1-V4 output, then validate native generation/base columns/chunk order, density/overlap and construction/query/allocation cost.'}
(e/'summary.json').write_text(json.dumps(summary,indent=2)+'\n');print(json.dumps(summary))
