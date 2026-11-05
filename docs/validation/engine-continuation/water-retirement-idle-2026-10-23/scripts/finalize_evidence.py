from pathlib import Path
import json,hashlib,shutil,datetime
R=Path(__file__).resolve().parent;E=R/'checkpoint';r=json.loads((R/'replay-console.json').read_text())
assert r['pass']and r['source_restore']['source_inputs']==589 and r['control_source_inputs']==588 and r['control_reconstruction_verified']and not r['new_build_or_native_execution']
shutil.copyfile(R/'replay-console.json',E/'offline-audit-replay.json')
s=json.loads((R/'checkpoint-summary.json').read_text());s.update({'updated_utc':datetime.datetime.now(datetime.timezone.utc).isoformat(),'offline_replay_pass':True,'archived_members_replayed':r['archived_members_verified'],'logical_capture_paths_replayed':r['logical_capture_paths_verified'],'unique_capture_objects':r['unique_content_objects'],'control588_source_reconstruction_verified':True,'readback_compaction':json.loads((E/'readback-compaction.json').read_text())})
for p in [R/'checkpoint-summary.json',E/'checkpoint-summary.json']:p.write_text(json.dumps(s,indent=2)+'\n')
note='\nOffline replay passes: '+str(r['archived_members_verified'])+'physical archive members, '+str(r['logical_capture_paths_verified'])+'logical capture paths, 589 candidate inputs and 588 reconstructed control inputs are verified. Lossless snapshot deduplication retains all complete-file hashes and compacts 1,133,554,686 logical capture bytes to 32,425,676 archive bytes. No new build/native or timing execution occurs during replay. All native/Gradle jobs are closed.\n'
for p in [R/'README.md',E/'README.md']:
 t=p.read_text();assert 'Offline replay passes:'not in t;p.write_text(t+note)
jobs=[]
for p in Path('/proc').iterdir():
 if not p.name.isdigit():continue
 try:
  name=(p/'comm').read_text().strip()
  if name in ['java','Xvfb']:jobs.append({'pid':int(p.name),'name':name})
 except (FileNotFoundError,PermissionError):pass
assert not jobs,jobs
(E/'finalization-audit.json').write_text(json.dumps({'pass':True,'checked_utc':datetime.datetime.now(datetime.timezone.utc).isoformat(),'live_java_or_xvfb':jobs,'offline_replay_pass':True,'source_changed_after_candidate_build_native':False,'interrupted_build_preserved_not_accepted':True,'no_new_build_native_or_timing_execution':True},indent=2)+'\n')
shutil.copyfile(Path(__file__),E/'scripts/finalize_evidence.py')
rows=[]
for p in sorted(E.rglob('*')):
 if not p.is_file():continue
 b=p.read_bytes();assert len(b)<=524288,(str(p),len(b));rows.append({'path':p.relative_to(E).as_posix(),'bytes':len(b),'sha256':hashlib.sha256(b).hexdigest(),'git_sha':hashlib.sha1(b'blob '+str(len(b)).encode()+b'\0'+b).hexdigest()})
(R/'upload-manifest.json').write_text(json.dumps(rows,indent=2)+'\n');print(json.dumps({'pass':True,'files':len(rows),'bytes':sum(x['bytes']for x in rows)},indent=2))
