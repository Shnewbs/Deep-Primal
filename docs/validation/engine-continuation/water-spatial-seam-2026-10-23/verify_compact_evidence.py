"""Replay the strict native audit from compact immutable evidence, in temp storage."""
from pathlib import Path
import tempfile,shutil,json,gzip,hashlib,subprocess,sys
E=Path(__file__).resolve().parent
with tempfile.TemporaryDirectory(prefix='deepprimal-spatial-evidence-') as d:
    p=Path(d)/'evidence';shutil.copytree(E,p)
    for row in json.loads((p/'compressed-data-manifest.json').read_text()):
        target=p/row['path'];target.unlink(missing_ok=True);packed=(p/row['gzip']).read_bytes();assert hashlib.sha256(packed).hexdigest()==row['gzip_sha256'] and len(packed)==row['gzip_bytes'];data=gzip.decompress(packed);assert hashlib.sha256(data).hexdigest()==row['sha256'] and len(data)==row['bytes'];target.write_bytes(data)
    cases=[p/'native'/n for n in ['vulkan-spatial-final','vulkan-spatial-final-packaged']]
    r=subprocess.run([sys.executable,str(p/'audit_spatial.py'),*[str(x) for x in cases]],capture_output=True,text=True,timeout=60)
    assert r.returncode==0,(r.stdout,r.stderr);actual=json.loads(r.stdout);expected=json.loads((p/'strict-spatial-native-audit.json').read_text());assert actual==expected
    print(json.dumps({'pass':True,'strict_native_reports_replayed':2,'spatial_shader_draws':24832,'spatial_pixel_comparisons':12416,'raw_data_hashes_and_lengths_checked':True,'native_controls_and_shared_sample_blends_byte_exact':True,'production_spatial_continuity_pass':False,'production_fix_promoted':False},indent=2))
