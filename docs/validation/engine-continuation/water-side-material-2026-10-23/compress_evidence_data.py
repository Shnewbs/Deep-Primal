from pathlib import Path
import gzip,json,hashlib
R=Path(__file__).resolve().parent;E=R/'project/docs/validation/engine-continuation/water-side-material-2026-10-23'
manifest=E/'compressed-data-manifest.json'
rows=json.loads(manifest.read_text()) if manifest.exists() else []
for p in sorted(E.rglob('*')):
 if not p.is_file() or p.suffix not in ['.json','.bin'] or p.stat().st_size<64000 or not p.relative_to(E).parts[0].startswith('vulkan-'):continue
 raw=p.read_bytes();encoded=gzip.compress(raw,mtime=0);target=p.with_name(p.name+'.gz');target.write_bytes(encoded)
 assert gzip.decompress(encoded)==raw
 rows=[x for x in rows if x['path']!=str(p.relative_to(E))]
 rows.append({'path':str(p.relative_to(E)),'bytes':len(raw),'sha256':hashlib.sha256(raw).hexdigest(),'gzip_path':str(target.relative_to(E)),'gzip_bytes':len(encoded),'gzip_sha256':hashlib.sha256(encoded).hexdigest()});p.unlink()
(E/'compressed-data-manifest.json').write_text(json.dumps(rows,indent=2)+'\n')
(E/'compress_evidence_data.py').write_bytes(Path(__file__).read_bytes())
print(json.dumps({'files':len(rows),'raw_bytes':sum(x['bytes'] for x in rows),'gzip_bytes':sum(x['gzip_bytes'] for x in rows)}))
