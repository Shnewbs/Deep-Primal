from pathlib import Path
import json,hashlib,shutil,datetime
R=Path(__file__).resolve().parent;E=R/'checkpoint'
r=json.loads((R/'replay-compacted-console.json').read_text())
assert r['pass'] and r['source_restore']['source_inputs']==588 and r['control_source_inputs']==587 and r['control_reconstruction_verified']
assert r['logical_capture_paths_verified']==15740 and not r['new_build_or_native_execution']
shutil.copyfile(R/'replay-compacted-console.json',E/'offline-audit-replay.json')
s=json.loads((R/'checkpoint-summary.json').read_text());comp=json.loads((E/'readback-compaction.json').read_text())
s.update({'updated_utc':datetime.datetime.now(datetime.timezone.utc).isoformat(),'archived_members_replayed':r['archived_members_verified'],'logical_capture_paths_replayed':r['logical_capture_paths_verified'],'unique_capture_objects':r['unique_content_objects'],'control587_source_reconstruction_verified':True,'offline_replay_pass':True,'readback_compaction':comp,'transient_exec_disconnect_recovered_same_workspace':True,'source_and_sealed_evidence_hashes_rechecked_after_reconnect':True})
for p in [R/'checkpoint-summary.json',E/'checkpoint-summary.json']:p.write_text(json.dumps(s,indent=2)+'\n')
note='''
Offline replay passes after lossless snapshot-level compaction: 1,092 physical capture/build/historical-admission archive members, 15,740 logical capture paths, 588 candidate inputs and 587 reconstructed control inputs are verified. Current and required historical boundary readbacks share content-addressed CPU/GPU snapshots; every original complete-file SHA-256 is reconstructed. The final archive shrinks from 82,111,663 to 29,827,493 bytes. `capture-compaction.json` records the earlier whole-file deduplication; `readback-compaction.json` and `capture-bundle.json` describe the final layout. Pre-compaction manifests are retained compressed. Replaying performs no new build, native launch or timing measurement.

A transient execution-transport disconnect recovered the same isolated workspace. Source and raw/sealed evidence hashes were rechecked, then both the original and compacted archive replays passed. No validation is inferred from activity during the disconnect. Finalization checks find no live Java or Xvfb jobs. The work-state graphics/restoration aliases are refreshed to this 588-input/26-file checkpoint; older evidence and blockers are retained.
'''
for p in [R/'README.md',E/'README.md']:
 t=p.read_text();assert 'Offline replay passes after lossless snapshot-level compaction'not in t;p.write_text(t+note)
jobs=[]
for p in Path('/proc').iterdir():
 if not p.name.isdigit():continue
 try:
  name=(p/'comm').read_text().strip()
  if name in ['java','Xvfb']:jobs.append({'pid':int(p.name),'name':name})
 except (FileNotFoundError,PermissionError):pass
assert not jobs,jobs
(E/'finalization-audit.json').write_text(json.dumps({'pass':True,'checked_utc':datetime.datetime.now(datetime.timezone.utc).isoformat(),'live_java_or_xvfb':jobs,'offline_replay_pass':True,'transient_transport_disconnect_recovered':True,'source_changed_after_native':False,'no_new_build_native_or_timing_execution':True},indent=2)+'\n')
shutil.copyfile(Path(__file__),E/'scripts/finalize_evidence.py')
rows=[]
for p in sorted(E.rglob('*')):
 if not p.is_file():continue
 b=p.read_bytes();assert len(b)<=524288,(str(p),len(b));rows.append({'path':p.relative_to(E).as_posix(),'bytes':len(b),'sha256':hashlib.sha256(b).hexdigest(),'git_sha':hashlib.sha1(b'blob '+str(len(b)).encode()+b'\0'+b).hexdigest()})
(R/'upload-manifest.json').write_text(json.dumps(rows,indent=2)+'\n')
print(json.dumps({'pass':True,'files':len(rows),'bytes':sum(x['bytes']for x in rows),'archived_members_replayed':r['archived_members_verified'],'logical_paths_replayed':r['logical_capture_paths_verified'],'source_inputs':588,'control_inputs':587},indent=2))
