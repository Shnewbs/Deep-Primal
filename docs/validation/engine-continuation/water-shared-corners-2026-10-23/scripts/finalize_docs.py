from pathlib import Path
import json,hashlib,datetime,shutil
R=Path(__file__).resolve().parent;P=R/'project';prefix='docs/validation/engine-continuation/water-shared-corners-2026-10-23';E=P/prefix
summary=json.loads((E/'checkpoint-summary.json').read_text());s=json.loads((P/'docs/ENGINE-WORK-STATE.json').read_text());s['latest_test_summary']=summary|{'updated_utc':datetime.datetime.now(datetime.timezone.utc).isoformat()};s['updated_at']=s['latest_test_summary']['updated_utc'];(P/'docs/ENGINE-WORK-STATE.json').write_text(json.dumps(s,indent=2)+'\n')
notes=(R/'checkpoint_notes.md').read_text();(E/'README.md').write_text('# Shared-corner water rendering — 2026-10-23\n\nParent source `71383974f8c52856f3c2fba8d5d95bff06354b89`; guarded lease parent `'+json.loads((R/'final-parent.json').read_text())['sha']+'`.\n\n'+notes+'\n## Evidence replay\n\nRun `python scripts/verify_archived_evidence.py .` in this directory. It validates every compressed raw length/hash, ZIP/PNG identity, and independently replays the unchanged baseline plus all three final native suites. Large raw data are gzip-compressed with explicit manifests; repeated PNGs are content-deduplicated in per-session ZIPs. No installer binary is published here.\n\nThe tiny20/40vertex upload CPU observations are retained per session in `strict-corner-native-audit.json`; these include allocation/staging and are noisy, not a matched full-scene performance benchmark. The baseline and candidate differ in render work; no speedup or ownerGPU guarantee follows.\n')
for n in ['final-scenes.png','offline-audit-replay.json']:
 shutil.copyfile(R/n,E/n)
inspection=json.loads((E/'screenshot-inspection.json').read_text());inspection['additional_inspected']=['vulkan-corners-clock-final-packaged/screenshots/corners-scene-before.png','vulkan-reuse-gl-corners-clock-final/screenshots/order-live-frame-1.png'];(E/'screenshot-inspection.json').write_text(json.dumps(inspection,indent=2)+'\n')
shutil.copyfile(R/'finalize_docs.py',E/'scripts/finalize_docs.py')
# Final upload set excludes every expanded raw path even if a scratch duplicate reappears.
excluded={r['path'] for r in json.loads((E/'compressed-data-manifest.json').read_text())}
paths=[prefix+'/'+str(p.relative_to(E)) for p in E.rglob('*') if p.is_file() and str(p.relative_to(E)) not in excluded]
scope=json.loads((E/'source-scope-audit.json').read_text());paths+=scope['production_changed_or_added']+scope['validation_changed_or_added']+['docs/ENGINE-WORK-STATE.json','docs/ENGINE-BUILD-AUDIT-HANDOFF.md','docs/ACTIVE-ENGINE-AUDIT.md'];assert len(paths)==len(set(paths))
rows=[]
for n in sorted(paths):
 b=(P/n).read_bytes();h=hashlib.sha1(b'blob '+str(len(b)).encode()+b'\0'+b).hexdigest();binary=b'\0' in b or n.endswith(('.gz','.zip','.png','.bin'))
 rows.append({'path':n,'sha':h,'bytes':len(b),'binary':binary,'sha256':hashlib.sha256(b).hexdigest()})
(R/'upload-manifest.json').write_text(json.dumps(rows,indent=2)+'\n');print(json.dumps({'files':len(rows),'bytes':sum(x['bytes'] for x in rows),'binary':sum(x['binary'] for x in rows),'compressed_raw_excluded':len(excluded),'unique_binary_bytes':sum({r['sha']:r['bytes'] for r in rows if r['binary']}.values())}))
