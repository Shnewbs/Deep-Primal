from pathlib import Path
import hashlib,json,gzip
R=Path(__file__).resolve().parent;prefix='docs/validation/engine-continuation/water-boundary-packet-2026-10-23/';expected={r['path']:r for r in json.loads((R/'r35-artifact-index.json').read_text())};E=R/'recovered-original';rows=[]
def gs(b):return hashlib.sha1(b'blob '+str(len(b)).encode()+b'\0'+b).hexdigest()
def sha(b):return hashlib.sha256(b).hexdigest()
def try_save(path,value):
 b=(json.dumps(value,indent=2)+'\n').encode();match=gs(b)==expected[path]['sha'];rows.append({'path':path,'original_hash_matched':match,'sha':gs(b)})
 if match:p=E/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(b)
ident=json.loads((E/(prefix+'vulkan-corners-marker-final/source-identity-after.json')).read_text());before=json.loads((E/(prefix+'vulkan-corners-marker-final/source-identity-before.json')).read_text());provenance=json.loads((E/(prefix+'vulkan-corners-marker-final/water-corner-runtime-provenance.json')).read_text())
bad=sha((R/'rejected-marker-vsh').read_bytes())
for c in ['vulkan-corners-packet-first','vulkan-corners-packet-retired','vulkan-corners-packet-retired-packaged','vulkan-reuse-gl-corners-packet-retired']:
 for name,data in [('source-identity-before.json',before),('source-identity-after.json',ident),('water-corner-runtime-provenance.json',provenance)]:
  data=dict(data);key='animated_water.vsh_sha256'if name.startswith('water')else'graphics-settings/src/main/resources/assets/deepprimal_graphics/shaders/core/animated_water.vsh';data[key]=bad;try_save(prefix+c+'/'+name,data)
for c in ['vulkan-corners-marker-final','vulkan-corners-marker-final-packaged','vulkan-reuse-gl-corners-marker-final']:
 d=R/'historical'/c;b=(d/'console-final.gz').read_bytes();raw=gzip.decompress(b);digest=sha(raw);report=json.loads(gzip.decompress((d/'result.json.gz').read_bytes()))
 seal={'stable_samples':4,'observations':[{'sample':n,'bytes':len(raw),'sha256':digest}for n in range(5)],'raw_bytes':len(raw),'raw_sha256':digest,'gzip_sha256':sha(b),'launcher_exit':report['exit_code'],'scope':'Closed-launcher console snapshot; live-console changes after this snapshot do not alter these sealed bytes.'};try_save(prefix+c+'/sealed-console.json',seal)
 prior={'crash-2026-10-23_16.49.32-client.txt':'cd7e210bbaeedbd800e47fc4c79e501499fe46bed83f14b504835480eed560cc'}
 try_save(prefix+c+'/prior-crash-reports.json',prior);try_save(prefix+c+'/crash-delta.json',{'prior':prior,'new_or_changed':{}})
(R/'more-wrapper-recovery-audit.json').write_text(json.dumps({'records':rows,'originals_recovered':sum(r['original_hash_matched']for r in rows),'scope':'Reconstruction accepted only by exact pre-interruption Git hash; timestamps/action histories are not invented'},indent=2)+'\n');print(json.dumps({'attempted':len(rows),'recovered':sum(r['original_hash_matched']for r in rows),'mismatches':[r['path']for r in rows if not r['original_hash_matched']]}))
