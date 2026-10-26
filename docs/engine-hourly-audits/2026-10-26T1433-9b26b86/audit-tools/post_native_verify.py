from pathlib import Path
import json,zipfile,io,struct,hashlib
r=Path(__file__).resolve().parent;e=r/'evidence';a=json.loads((e/'package.json').read_text());q=json.loads((e/'qualification-result.json').read_text());assert q['pass'];dll=[];archives=[]
def scan(b,label):
 with zipfile.ZipFile(io.BytesIO(b)) as z:
  assert z.testzip() is None and len(z.namelist())==len(set(z.namelist()));archives.append(label)
  for n in z.namelist():
   assert '/buildcompat/' not in n and '/themes/assets/' not in n and 'PrecipitationPersistenceChecks' not in n
   if n.endswith('.jar'):scan(z.read(n),n)
   if n.endswith('.dll'):
    d=z.read(n);o=struct.unpack_from('<I',d,60)[0];assert d[:2]==b'MZ' and d[o:o+4]==b'PE\0\0' and struct.unpack_from('<H',d,o+4)[0]==0x8664;dll.append({'path':n,'sha256':hashlib.sha256(d).hexdigest(),'machine':'AMD64'})
scan(Path(a['artifact']).read_bytes(),Path(a['artifact']).name);assert len(archives)==7 and len(dll)==2
assert hashlib.sha256(Path(a['artifact']).read_bytes()).hexdigest()==hashlib.sha256(Path(a['producer']).read_bytes()).hexdigest()==a['sha256']
logs=[e/'default-native-0.txt',e/'default-native-1.txt',e/'optin-native-0.txt',e/'optin-native-1.txt',e/'aquifer-fresh-native.log',e/'aquifer-reopen-native.log',e/'aquifer-verify-native.log',r/'precipitation-native/fresh-native.log',r/'precipitation-native/reopen-native.log',r/'receiver-native/candidate-native.log']
for f in logs:
 text=f.read_text();assert 'Got mod coordinates deepprimal%%'+a['artifact'] in text,f
 for mod in ['Core','GraphicsSettings','LOD','Performance','Themes','Vulkan']:assert a['artifact']+' > com.shnewbs.deepprimal.DeepPrimal-'+mod in text,(f,mod)
(e/'post-native-archive.json').write_text(json.dumps({'pass':True,'archive_crcs':archives,'amd64_dlls':dll,'ten_native_package_selections_verified':True,'producer_export_equal':True,'sha256':a['sha256'],'size':a['size'],'source_sha':a['source_sha'],'authoring_and_private_fixtures_excluded':True},indent=2));print('POST_NATIVE_BYTES_ARCHIVES_AND_SELECTION_PASS',a['sha256'])
