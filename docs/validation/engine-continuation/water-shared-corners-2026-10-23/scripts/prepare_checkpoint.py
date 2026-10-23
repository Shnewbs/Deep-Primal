from pathlib import Path
import json,gzip,hashlib,zipfile,shutil,difflib,io
R=Path(__file__).resolve().parent;P=R/'project';B=Path('/tmp/deepprimal-corners-1110');E=P/'docs/validation/engine-continuation/water-shared-corners-2026-10-23';E.mkdir(parents=True,exist_ok=True)
def sha(b):return hashlib.sha256(b).hexdigest()
def gs(b):return hashlib.sha1(b'blob '+str(len(b)).encode()+b'\0'+b).hexdigest()
def save(n,v):p=E/n;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(v,indent=2)+'\n')
def cp(s,n):p=E/n;p.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(s,p)
parent=json.loads((R/'parent-tree.json').read_text());inputs=[];changed=[]
for row in parent:
 if row['type']!='blob' or row['path'].startswith('docs/'):continue
 p=P/row['path'];assert p.is_file(),row['path'];h=gs(p.read_bytes());inputs.append({'path':row['path'],'parent_git_sha':row['sha'],'current_git_sha':h,'unchanged':h==row['sha']})
 if h!=row['sha']:changed.append(row['path'])
new=['engine-core/src/main/java/com/shnewbs/deepprimal/core/WaterFlowField.java','graphics-settings/src/main/resources/assets/deepprimal_graphics/shaders/include/water_flow.glsl','src/coreChecks/java/com/shnewbs/deepprimal/WaterFlowFieldChecks.java','src/visualValidation/java/com/shnewbs/deepprimal/validation/WaterCornerPacking.java','src/visualValidation/java/com/shnewbs/deepprimal/validation/WaterCornerFieldChecks.java']
# Discover the core check package rather than assuming its package location.
new=[str(next(P.rglob('WaterFlowFieldChecks.java')).relative_to(P)) if n.endswith('/WaterFlowFieldChecks.java') else n for n in new]
assert len(inputs)==570 and all((P/n).is_file() for n in new)
prod=[n for n in changed+new if '/src/main/' in '/'+n]
tests=[n for n in changed+new if n not in prod];assert len(prod)==5
save('source-input-manifest.json',inputs);save('source-scope-audit.json',{'parent':'71383974f8c52856f3c2fba8d5d95bff06354b89','guarded_parent':json.loads((R/'final-parent.json').read_text())['sha'],'existing_inputs_checked':len(inputs),'unchanged':sum(x['unchanged'] for x in inputs),'production_changed_or_added':prod,'validation_changed_or_added':tests,'new_files':new,'solver_quantity_worldgen_lod_backend_unchanged':True,'installer_release':False})
patch=''
for n in prod:
 before=R/'before'/n
 if n in changed:
  assert before.is_file(),n;assert gs(before.read_bytes())==next(x['sha'] for x in parent if x['path']==n);cp(before,'before/'+Path(n).name)
 old=before.read_text().splitlines(True) if before.exists() else []
 patch+=''.join(difflib.unified_diff(old,(P/n).read_text().splitlines(True),fromfile='a/'+n,tofile='b/'+n))
(E/'production.patch').write_text(patch)
for f in R.glob('*.py'):cp(f,'scripts/'+f.name)
for f in ['strict-corner-native-audit.json','vulkan-build-audit.json','isolation-proof.json','final-parent.json']:
 cp(R/f,f)
for f in ['first-candidate-AnimatedWaterRenderer.java','pre-clock-AnimatedWaterRenderer.java','pre-uniform-animated_water.vsh','pre-uniform-animated_water.fsh','uniform-only-animated_water.vsh','uniform-only-animated_water.fsh','pre-mip-WaterCornerFieldChecks.java']:
 cp(R/f,'rejected-source/'+f)
cp(Path('/workspace/scratch/a22609fabda0/water-corners-baseline-1125/baseline-production-proof.json'),'baseline-production-proof.json')
FINAL=['vulkan-corners-clock-final','vulkan-corners-clock-final-packaged','vulkan-reuse-gl-corners-clock-final']
OLD=['vulkan-corners-baseline','vulkan-corners-first','vulkan-corners-final','vulkan-corners-uniform','vulkan-corners-endpoint','vulkan-corners-mip-final','vulkan-corners-mip-final-packaged','vulkan-reuse-gl-corners-final']
CASES=[('exact-baseline',B/'exact-baseline/vulkan-corners-baseline')]+[(n,B/n) for n in OLD+FINAL]
archives=[]
for label,d in CASES:
 index={};unique={}
 for f in sorted(d.glob('*.png')):
  data=f.read_bytes();h=sha(data);index[f.name]=h;unique[h]=data
 zpath=E/(label+'-pixels.zip')
 with zipfile.ZipFile(zpath,'w',zipfile.ZIP_DEFLATED) as z:
  z.writestr('manifest.json',json.dumps(index,sort_keys=True))
  for h,b in sorted(unique.items()):z.writestr('pixels/'+h+'.png',b)
 archives.append({'case':label,'source_files':len(index),'unique_pngs':len(unique),'archive_bytes':zpath.stat().st_size,'sha256':sha(zpath.read_bytes())})
 for f in d.iterdir():
  if f.is_file() and f.suffix in ['.json','.txt','.gz','.bin'] and f.name not in ['live-action.json']:cp(f,label+'/'+f.name)
 for f in (d/'screenshots').glob('*.png'):
  if label in FINAL:cp(f,label+'/screenshots/'+f.name)
 if (d/'console-final.gz').exists():
  raw=gzip.decompress((d/'console-final.gz').read_bytes());seal=json.loads((d/'sealed-console.json').read_text());assert sha(raw)==seal['raw_sha256']
save('compact-pixel-archives.json',archives)
for label in ['clock-final-build','exact-baseline-build','baseline-build','baseline-build-retry','final-build','uniform-build','endpoint-build','final-verified-build']:
 d=B/label
 for f in ['candidate-build-result.json','candidate-build-final.gz']:
  if (d/f).exists():cp(d/f,('build' if label=='clock-final-build' else 'superseded-builds/'+label)+'/'+f)
a=json.loads((R/'strict-corner-native-audit.json').read_text());assert a['pass'] and len(a['sessions'])==3
package=json.loads((R/'vulkan-build-audit.json').read_text());mods=[]
jar,=list((P/'build/libs').glob('*-vulkan-linux.jar'))
with zipfile.ZipFile(jar) as z:
 for n in z.namelist():
  if n.startswith('META-INF/jarjar/') and n.endswith('.jar'):
   b=z.read(n);matches=[p for p in P.glob('*/build/libs/*.jar') if p.read_bytes()==b];assert len(matches)==1;mods.append({'path':n,'standalone':str(matches[0].relative_to(P)),'sha256':sha(b),'bytes':len(b),'identical':True})
assert len(mods)==5;save('module-identities.json',mods)
summary={'production_files_changed_or_added':len(prod),'validation_files_changed_or_added':len(tests),'existing_inputs_checked':570,'existing_inputs_unchanged':sum(x['unchanged'] for x in inputs),'final_sessions':FINAL,'baseline_failed_edge_routes':2204,'baseline_uniform_controls_exact':136,'final_edge_pairs':4664*3,'final_uniform_pairs':272*3,'final_corner_draws':9872*3,'final_corner_comparisons':4936*3,'actual_uploaded_tops':4664*3,'raw_vertex_bytes':5037120*3,'raw_pixel_bytes':7153664*3,'maximum_channel_error':max(x['max_channel_error'] for x in a['sessions']),'channels_beyond_1':0,'coverage_bad_pixels':0,'core_codec_cases':8435621,'height_clock_cases':98307,'shared_halo_edges':34000,'history_cap':32768,'new_corner_history_cap':32768,'cpu_float_stride':14,'gpu_vertex_stride_bytes':36,'new_varying_bytes':24,'new_vertex_or_uniform_allocation_bytes':0,'mixed_max_texture_endpoint_evaluations':8,'uniform_static_texture_evaluations':1,'uniform_transition_texture_evaluations':2,'full_clean_serial_build':'pass;25 executed tasks','archive_crcs':6,'class_identities':sum(package['compiled_class_byte_matches'].values()),'nested_module_identities':5,'final_crashes':0,'final_vulkan_errors':0,'final_tracked_vulkan_resources':0,'software_drivers_only':True,'partial_neighbor_request_atomicity':False,'full_stream_ocean_acceptance':False,'owner_gpu_fps_acceptance':False,'performance_acceptance':False,'installer_released':False,'acceptance_complete':False}
save('checkpoint-summary.json',summary)
save('code-self-audit.json',{'implementation':'Each global corner uses mean wet-neighbor FLOW vectors quantized to16 directions, zero/still distinct from dry. Four canonical corner sample colors bilinearly blend; no wrapped-UV interpolation. Four bounded temporal histories share global key, admitted endpoints and latest pending request.','packing':'Four5-bit IDs/source IDs and4ages fit existing36-byte vertex through sentinel UV0, top cell alpha and high light/clock bytes. Expired height offset clamps to[-100,0], regression includes32769 idle offsets. Low light bytes and opacity clock unchanged.','fidelity':'Uniform source and target UVs interpolate vertex values; Q25 coefficients shared include prevents coefficient drift. Fragment explicit gradients/mip controls. Uniform1/2fetch paths retain old endpoint fidelity.','unchanged':['quantity/solver/runtime','player edits','V1/V2/V3 geography','LOD/backend source','GPU vertex/uniform allocations'],'limits':['New corner requested on one uploaded page does not atomically update a resident neighbor; matched delayed repacks tested only after both adopt it','Cap eviction/reset bounded, not continuity under active cap churn','Long streams/ocean, spatial side seams, tint/chemistry/other liquids remain open','Arbitrary alpha/linear packs and exhaustive height/opacity/edit combinations unaccepted','Additional24B varying, up to8 endpoint evaluations and history memory need matched frame-time/resident profiling','LOD horizon cutoffs, pressure/budgets/geology acceptance remain open','Software Vulkan/GL only;no ownerGPU/FPS/60Hz/superiority guarantee']})
save('rejected-and-superseded.json',{'mixed_baseline':'Initial same-directory restoration mixed first-candidate renderer with old shaders; excluded. Exact separate baseline verifies411 production blobs and loaded class/shader identity. Cause unproven.','first_baseline_build':'Gradle binary store temporary read failure; clean serial retry passes.','uniform_fidelity':'First integrated corner candidate passes edges but fails24 nearest-texel channels max5. Interpolated target UV fixes whole-cell controls, but25channels remain at source fade endpoint. Interpolating both endpoints fixes without widening tolerance.','height_clock':'Independent raw audit found expired signed-short height offsets could wrap the new signed byte. Clamp completed offsets to100ms and add98307cases; all3final suites repeated.','superseded':'All earlier passing candidates excluded from final totals; raw vertices/pixels, result and sealed consoles preserved.','validation_corrections':'Generated shader arithmetic plus signs corrected before build/native; GL rows normalized explicitly; independent decoder accounts for delayed separate-page outer-corner start.','final_only':FINAL})
cp(R/'corner-contact.png','corner-contact.png');cp(R/'screenshot-inspection.json','screenshot-inspection.json')
rows=[]
for p in sorted(E.rglob('*')):
 if p.is_file() and p.suffix in ['.json','.bin'] and p.stat().st_size>=64000:
  b=p.read_bytes();z=gzip.compress(b,mtime=0);target=p.with_name(p.name+'.gz');target.write_bytes(z);rows.append({'path':str(p.relative_to(E)),'bytes':len(b),'sha256':sha(b),'gzip_path':str(target.relative_to(E)),'gzip_bytes':len(z),'gzip_sha256':sha(z)});p.unlink()
save('compressed-data-manifest.json',rows)
print(json.dumps({'evidence_files':sum(p.is_file() for p in E.rglob('*')),'evidence_bytes':sum(p.stat().st_size for p in E.rglob('*') if p.is_file()),'production':prod,'validation':tests,'new':new}))
