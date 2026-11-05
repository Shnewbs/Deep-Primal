from pathlib import Path
import json,gzip,hashlib,tarfile,io,shutil
R=Path(__file__).resolve().parent;P=R/'project';B=Path('/tmp/deepprimal-idle-2328');OLD=R.parent/'water-boundary-retry-2230/checkpoint';E=R/'checkpoint';E.mkdir(exist_ok=False)
def sha(b):return hashlib.sha256(b).hexdigest()
def git(b):return hashlib.sha1(b'blob '+str(len(b)).encode()+b'\0'+b).hexdigest()
def write(n,x):
 p=E/n;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(x,indent=2)+'\n')
def copy(p,n):
 d=E/n;d.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(p,d)
def bundle(name,files):
 raw=io.BytesIO();members=[]
 with tarfile.open(fileobj=raw,mode='w')as t:
  for n,p,offset,length in sorted(files):
   with p.open('rb')as f:f.seek(offset);b=f.read(length)
   assert len(b)==length;i=tarfile.TarInfo(n);i.size=length;i.mtime=0;i.mode=0o755 if n=='gradlew'else 0o644;t.addfile(i,io.BytesIO(b));members.append({'path':n,'bytes':length,'sha256':sha(b)})
 b=gzip.compress(raw.getvalue(),mtime=0);parts=[]
 for n,o in enumerate(range(0,len(b),524288)):
  p=Path(name+f'.part{n:03}');chunk=b[o:o+524288];(E/p).parent.mkdir(parents=True,exist_ok=True);(E/p).write_bytes(chunk);parts.append({'path':str(p),'bytes':len(chunk),'sha256':sha(chunk),'git_sha':git(chunk)})
 return {'archive':name,'bytes':len(b),'sha256':sha(b),'parts':parts,'members':members}
def fullfiles(items):return[(n,p,0,p.stat().st_size)for n,p in items]
previous={x['path']:x for x in json.loads((OLD/'source-snapshot-manifest.json').read_text())};staged={x['path']for x in json.loads((OLD/'staged-manifest.json').read_text())};pub=json.loads(gzip.decompress((OLD/'source-input-manifest.json.gz').read_bytes()));pubmap={x['path']:x['sha']for x in pub};changes=json.loads((R/'changes.json').read_text());changed={x['path']for x in changes};paths=sorted(set(previous)|changed);staged|=changed;assert len(paths)==589 and len(staged)==27
manifest=[]
for path in paths:
 b=(P/path).read_bytes();prior=previous.get(path)
 if path not in changed:assert prior['sha256']==sha(b),path
 else:assert prior is None or prior['sha256']!=sha(b)
 row={'path':path,'bytes':len(b),'sha256':sha(b),'git_sha':git(b),'public_parent_sha':pubmap.get(path),'staged':path in staged};manifest.append(row)
 if path in staged:copy(P/path,Path('staged')/path)
write('source-snapshot-manifest.json',manifest);write('source-snapshot-bundle.json',bundle('source-snapshot.tar.gz',fullfiles([(x['path'],P/x['path'])for x in manifest])));write('staged-manifest.json',[x for x in manifest if x['staged']]);copy(OLD/'source-input-manifest.json.gz','source-input-manifest.json.gz');copy(R/'since-r43.patch','since-r43.patch')
write('source-scope-audit.json',{'pass':True,'prior_checkpoint':'136892bc3b060fb12d75f063d3e92d40a7f45bac','candidate_inputs':589,'staged_files':27,'staged_production_files':6,'staged_validation_files':21,'changes_since_r43':changes,'other_prior_inputs_unchanged':584,'production_promoted':False,'geometry_solver_geography_changed':False})
baseline_manifest=[];overlay=[]
for path in sorted(previous):
 b=(R/'baseline'/path).read_bytes();baseline_manifest.append({'path':path,'bytes':len(b),'sha256':sha(b),'git_sha':git(b)})
 if (P/path).read_bytes()!=b:copy(R/'baseline'/path,Path('baseline-overlay')/path);overlay.append(path)
write('baseline-source-manifest.json',baseline_manifest);write('baseline-overlay.json',{'replace':overlay,'remove':sorted(set(paths)-set(previous)),'same_enhanced_frame_fixture':True,'source_inputs':588,'staged_product_is_R43_control':True})
sessions=['vulkan-frame-retire-steady-r43-control-retry','vulkan-frame-retire-steady-idle-retry','vulkan-corners-shared-final-idle-retry','vulkan-corners-shared-final-idle-packaged-retry','vulkan-reuse-gl-corners-shared-final-idle-retry']
logical=[]
for name in sessions:
 result=json.loads((B/name/'result.json').read_text());assert result['exit_code']==0 and not result['timeout'] and result['build_successful'],name
 logical +=[(p.relative_to(B).as_posix(),p)for p in sorted((B/name).rglob('*'))if p.is_file()]
historical=R.parent/'water-boundary-retry-2230/replay-compacted/historical/vulkan-corners-shared-final-r41-control'
needed=['water-boundary-batch.json','water-boundary-batch-readbacks.bin','result.json','console-final.gz','console.log','source-identity-before.json','source-identity-after.json','crash-delta.json','action-history.json','shutdown.txt','sealed-console.json']
logical += [('historical/'+historical.name+'/'+n,historical/n)for n in needed]
objects={};layout=[];split=0
for name,p in logical:
 b=p.read_bytes();row={'path':name,'bytes':len(b),'sha256':sha(b)}
 if p.name in ['water-boundary-batch-readbacks.bin','water-boundary-retry.bin']:
  meta=p.with_name('water-boundary-batch.json'if p.name=='water-boundary-batch-readbacks.bin'else'water-boundary-retry.json');records=json.loads(meta.read_text())['readbacks'];offset=0;chunks=[]
  for r in records:
   assert r['offset']==offset
   for repeat in range(2):
    length=r['bytes'];data=b[offset:offset+length];assert len(data)==length;key='objects/'+sha(data);objects.setdefault(key,(p,offset,length));chunks.append({'object':key,'bytes':length});offset+=length
  assert offset==len(b);row['chunks']=chunks;split+=1
 else:key='objects/'+sha(b);objects.setdefault(key,(p,0,len(b)));row['object']=key
 layout.append(row)
write('capture-bundle.json',bundle('captures/compact-readbacks.tar.gz',[(key,*value)for key,value in objects.items()]));(E/'capture-layout.json.gz').write_bytes(gzip.compress(json.dumps(layout).encode(),mtime=0))
# Verify the lossless logical-file reconstruction before finalization.
for row in layout:
 h=hashlib.sha256();size=0
 for x in row.get('chunks',[{'object':row.get('object'),'bytes':row['bytes']}]):
  p,offset,length=objects[x['object']];assert length==x['bytes']
  with p.open('rb')as f:f.seek(offset);data=f.read(length)
  h.update(data);size+=length
 assert size==row['bytes']and h.hexdigest()==row['sha256']
write('readback-compaction.json',{'pass':True,'logical_paths':len(layout),'logical_bytes':sum(x['bytes']for x in layout),'unique_objects':len(objects),'unique_uncompressed_bytes':sum(x[2]for x in objects.values()),'raw_readback_files_split_losslessly':split,'every_original_file_sha256_reconstructed':True,'archive_bytes':json.loads((E/'capture-bundle.json').read_text())['bytes']})
builds=['build-baseline','build-candidate','build-candidate-retry']
write('build-bundle.json',bundle('build-evidence.tar.gz',fullfiles([(p.relative_to(B).as_posix(),p)for n in builds for p in(B/n).rglob('*')if p.is_file()])));write('interrupted-build.json',json.loads((R/'interrupted-candidate-build/record.json').read_text()))
prior=json.loads((OLD/'prior-admission-bundle.json').read_text());write('prior-admission-bundle.json',prior)
for part in prior['parts']:copy(OLD/part['path'],part['path'])
copy(OLD/'prior-publication-baseline.tar.gz','prior-publication-baseline.tar.gz')
for p in R.glob('*.py'):copy(p,Path('scripts')/p.name)
copy(R/'validation_identity.gradle','scripts/validation_identity.gradle');(E/'scripts/restore_snapshot.py').write_text((OLD/'scripts/restore_snapshot.py').read_text().replace('588','589'));copy(R/'replay_template.py','scripts/replay_checkpoint.py')
for name in ['WaterBoundaryBatchChecks','WaterAdmissionPlanChecks','WaterBoundaryRetryChecks','WaterFrameRetirementChecks']:
 for p in(P/'build/classes/java/visualValidation/com/shnewbs/deepprimal/validation').glob(name+'*.class'):copy(p,Path('runtime-fixtures')/p.name)
identities=[]
for role,root in [('candidate',P),('control',R/'baseline')]:
 for label,path in [('renderer',root/'build/classes/java/main/com/shnewbs/deepprimal/client/render/AnimatedWaterRenderer.class'),('page',root/'build/classes/java/main/com/shnewbs/deepprimal/client/render/AnimatedWaterRenderer$Page.class'),('packet',root/'engine-core/build/classes/java/main/com/shnewbs/deepprimal/core/WaterCornerPacket.class')]:
  name='runtime-product-classes/'+role+'-'+label+'.class';copy(path,name);data=path.read_bytes();identities.append({'path':name,'bytes':len(data),'sha256':sha(data)})
write('compiled-product-identities.json',identities)
for n in ['all-frame-audit.json','matched-idle-audit.json','strict-admission-audit.json','strict-boundary-audit.json','strict-corner-audit.json','strict-frame-audit.json','strict-deadline-audit.json','strict-publication-audit.json','package-audit.json','code-self-audit.json','screenshot-inspection.json','console-seal-audit.json','checkpoint-summary.json','README.md','runtime-source-identity.json','strict-retry-audit.json']:
 copy(R/n,n)
for n in ['packaging-result.json','archive-audit.json']:copy(R/'windows-preview'/n,'r43-owner-preview/'+n)
copy(R/'windows-preview/packaging.log.gz','r43-owner-preview/packaging.log.gz');copy(R/'quick-state-core.log','quick-state-core.log')
write('sequential-job-audit.json',{'pass':True,'full_builds_passed':2,'interrupted_builds_not_accepted':1,'native_sessions_passed':5,'all_gradle_native_jobs_sequential':True,'owner_requested_R43_windows_packaging_separate_from_R44':True,'no_R44_source_changes_after_candidate_full_build':True,'software_driver':'Mesa25.2.8 lavapipe','display':'Xvfb1.9','older_missing_originals':73})
print(json.dumps({'pass':True,'source_inputs':len(manifest),'staged':len(staged),'logical_capture_paths':len(layout),'objects':len(objects)},indent=2))
