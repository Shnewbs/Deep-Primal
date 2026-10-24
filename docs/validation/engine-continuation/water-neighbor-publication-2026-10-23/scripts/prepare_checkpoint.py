from pathlib import Path
import json,hashlib,gzip,zipfile,shutil,difflib,re
R=Path(__file__).resolve().parent;P=R/'project';B=Path('/tmp/deepprimal-neighbor-1227');PREFIX='docs/validation/engine-continuation/water-neighbor-publication-2026-10-23';E=P/PREFIX;E.mkdir(parents=True,exist_ok=True)
def sha(b):return hashlib.sha256(b).hexdigest()
def gs(b):return hashlib.sha1(b'blob '+str(len(b)).encode()+b'\0'+b).hexdigest()
def save(n,v):p=E/n;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(v,indent=2)+'\n')
def cp(s,n):p=E/n;p.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(s,p)
old=json.loads((R/'restored-inputs.json').read_text());inputs=[];changed=[]
for r in old:
 p=P/r['path'];h=gs(p.read_bytes());inputs.append(r|{'current_sha':h,'unchanged':h==r['sha']})
 if h!=r['sha']:changed.append(r['path'])
new=['engine-core/src/main/java/com/shnewbs/deepprimal/core/WaterPresentation.java','src/coreChecks/java/com/shnewbs/deepprimal/core/WaterPresentationChecks.java'];assert len(inputs)==575 and len(changed)==3
staged=changed+new;manifest=[]
for n in staged:
 b=(P/n).read_bytes();cp(P/n,'staged/'+n);manifest.append({'path':n,'parent_sha':next((r['sha']for r in old if r['path']==n),None),'candidate_sha':gs(b),'sha256':sha(b)})
save('staged-restore-manifest.json',manifest);save('source-input-manifest.json',inputs)
save('source-scope-audit.json',{'parent':'f85fc8a565a64d1ed7789f830193f1b7aa614bba','guarded_parent':json.loads((R/'guarded-parent.json').read_text())['sha'],'existing_inputs_checked':575,'candidate_unchanged_inputs':572,'candidate_changed_existing':changed,'candidate_new':new,'staged_files':staged,'published_production_changes':0,'published_validation_source_changes':0,'candidate_not_promoted':True,'solver_quantity_worldgen_lod_backend_unchanged':True,'performance_gate':False,'installer_released':False})
patch=''
for n in staged:
 if n in new:before=''
 elif n.endswith('AnimatedWaterRenderer.java'):before=(R/'before-AnimatedWaterRenderer.java').read_text()
 elif n.endswith('WaterCornerFieldChecks.java'):before=(R/'before-WaterCornerFieldChecks.java').read_text()
 else:before=(R/'baseline/project'/n).read_text()
 patch+=''.join(difflib.unified_diff(before.splitlines(True),(P/n).read_text().splitlines(True),fromfile='a/'+n,tofile='b/'+n))
(E/'staged-fix.patch').write_text(patch)
for p in R.glob('*.py'):cp(p,'scripts/'+p.name)
for n in ['WaterPresentationChecks.java','WaterPresentationBudget.java','core-check.txt','core-compile.txt','presentation-budget.txt','baseline-source-proof.json','environment-recovery.json','package-audit.json','package-auditor-correction.json','strict-native-audit.json','screenshot-inspection.json','neighbor-contact.png','gl-scene.png','guarded-parent.json']:cp(R/n,n)
for label,d in [('baseline-build',R/'baseline'),('candidate-build',B/'candidate-build')]:
 for n in ['candidate-build-result.json','candidate-build-final.gz']:cp(d/n,label+'/'+n)
 result=json.loads((d/'candidate-build-result.json').read_text());raw=gzip.decompress((d/'candidate-build-final.gz').read_bytes());assert result['pass']and sha(raw)==result['raw_sha256']
assert b'WATER_PRESENTATION_CORE_PASS cases=180000' in gzip.decompress((B/'candidate-build/candidate-build-final.gz').read_bytes())
CASES=['vulkan-create','vulkan-corners-neighbor-baseline','vulkan-corners-neighbor-candidate','vulkan-corners-neighbor-candidate-packaged','vulkan-reuse-gl-corners-neighbor-candidate'];archives=[]
for label in CASES:
 d=B/label;index={};unique={}
 for p in sorted(d.glob('*.png')):
  b=p.read_bytes();h=sha(b);index[p.name]=h;unique[h]=b
 zpath=E/(label+'-pixels.zip')
 with zipfile.ZipFile(zpath,'w',zipfile.ZIP_DEFLATED)as z:
  z.writestr('manifest.json',json.dumps(index,sort_keys=True))
  for h,b in sorted(unique.items()):z.writestr('pixels/'+h+'.png',b)
 archives.append({'case':label,'names':len(index),'unique':len(unique),'sha256':sha(zpath.read_bytes()),'archive_bytes':zpath.stat().st_size})
 for p in d.iterdir():
  if p.is_file()and p.suffix in['.json','.txt','.gz','.bin']and p.name!='live-action.json':cp(p,label+'/'+p.name)
 for p in(d/'screenshots').glob('*.png'):cp(p,label+'/screenshots/'+p.name)
 seal=json.loads((d/'sealed-console.json').read_text());raw=gzip.decompress((d/'console-final.gz').read_bytes());assert sha(raw)==seal['raw_sha256']and not json.loads((d/'crash-delta.json').read_text())['new_or_changed']
save('compact-pixel-archives.json',archives)
a=json.loads((R/'strict-native-audit.json').read_text());assert a['pass']and len(a['candidate_sessions'])==3
budget=(R/'presentation-budget.txt').read_text();m=re.search(r'bytes=(\d+).*medianNanos=(\d+) p95Nanos=(\d+).*fitsDeadline=(\w+)',budget);assert m and m[4]=='false'
save('performance-gate.json',{'accepted':False,'bytes':int(m[1]),'median_nanoseconds':int(m[2]),'p95_nanoseconds':int(m[3]),'extraction_target_nanoseconds':2000000,'scope':'synthetic maximum-residency rebase alone; excludes scan, copy and GPU writes; not measured full-scene frame time','cpu_mirror_retained_upper_bound_bytes':33554432,'new_gpu_allocation':False,'reason':'Functional candidate cannot be promoted on current performance evidence. Bound/reduce preparation and publication cost; retain existing coverage and clock tests.'})
summary={'production_changes_promoted':0,'validation_source_changes_promoted':0,'staged_production_files':2,'staged_validation_files':3,'baseline_failed_pairs':32,'baseline_bad_channels':15280,'baseline_max_channel_error':120,'prior_routes_regressions':0,'new_request_routes_per_session':80,'candidate_sessions':3,'candidate_draws':30576,'candidate_comparisons':15288,'candidate_bad_channels':0,'candidate_max_channel_error':1,'candidate_uncovered_pixels':0,'candidate_decoded_top_vertices':sum(x['decoded_top_vertices']for x in a['candidate_sessions']),'candidate_new_shared_corner_checks':sum(x['new_request_shared_corner_checks']for x in a['candidate_sessions']),'fresh_full_builds':2,'full_build_tasks_each':25,'new_core_cases':180000,'archive_crcs':6,'class_identities':411,'nested_module_identities':5,'fresh_world_create_join_save_shutdown':True,'fresh_crashes':0,'software_vulkan_sessions':2,'software_opengl_sessions':1,'performance_accepted':False,'performance_p95_nanoseconds':int(m[3]),'extraction_target_nanoseconds':2000000,'candidate_promoted':False,'installer_released':False,'owner_gpu_fps_accepted':False,'full_scene_continuity_accepted':False,'acceptance_complete':False};save('checkpoint-summary.json',summary)
save('code-self-audit.json',{'staged_fix':'Retain exact accepted vertex bytes per GPU page; update at most eight same-layer resident neighbors from canonical history before next draw. No neighbor capture/remesh or new endpoint request; no clock restart. Rebase height, opacity, side-material and top-corner clocks when changing the page upload origin. Only clock/endpoint metadata and unused normal padding for cellY differ; geometry/light/quantity unchanged.','steady_path':'Compare actual admitted source/target/age and skip equivalent resident representations.','budget':'Retained CPU payload <=32MiB resident GPU bound, plus temporary copies/direct transfer buffers. Full-neighbor scanning/rebasing/copying and GPU updates remain too costly at the synthetic residency bound; not promoted.','native_scope':'Reproject actual uploaded top payloads at equal shared-edge coordinates. Near/far, X/Z, both first-page orders, same-page controls, staggered requests at0/25/50/75/100ms; far cases include20s old resident clocks.','limits':['No actual long-stream/ocean or full scene flicker acceptance','Active cap churn, GPU update exceptions/rollback and further stale/queued/height/opacity/edit combinations need coverage','CPU mirror adds retained memory and transient peaks; no broad resident/frame-time acceptance','Synthetic budget is not an owner GPU/FPS or actual full-scene measurement','Distant rectangular LOD/horizon boundaries, water budgets/chemistry, pressure and geology remain open'],'production_and_public_test_sources_unchanged':True})
save('recovery-and-audit-corrections.json',{'workspace':'Previous scratch toolchain/world removed by maintenance. Recovered575 Git blobs by verified parent SHA and restored pinned JDK/Gradle/agent/graphics packages. All reported current measurements are fresh.','private_git_cli':'No CLI authentication; used connected repository reads and guarded writes without reconstructing credentials.','source_recovery':'First API blob read returned decoded text rather than JSON; changed to fetch_file base64 with SHA verification. A batch exceeded argument bound; resumed with bounded byte batches.','package_auditor':'Initial raw resources check incorrectly compared expanded mods.toml with unexpanded source; exact build-declared version substitution applied. No shader/production change and no comparison tolerance widened.','promotion':'Budget probe fails2ms target; candidate source stored only under staged evidence, not applied to working branch production/public tests.'})
compressed=[]
for p in sorted(E.rglob('*')):
 if p.is_file()and p.suffix in['.json','.bin']and p.stat().st_size>=64000:
  b=p.read_bytes();z=gzip.compress(b,mtime=0);dest=p.with_name(p.name+'.gz');dest.write_bytes(z);compressed.append({'path':str(p.relative_to(E)),'bytes':len(b),'sha256':sha(b),'gzip_path':str(dest.relative_to(E)),'gzip_bytes':len(z),'gzip_sha256':sha(z)});p.unlink()
save('compressed-data-manifest.json',compressed)
print(json.dumps({'files':sum(p.is_file()for p in E.rglob('*')),'bytes':sum(p.stat().st_size for p in E.rglob('*')if p.is_file()),'compressed':len(compressed)}))
