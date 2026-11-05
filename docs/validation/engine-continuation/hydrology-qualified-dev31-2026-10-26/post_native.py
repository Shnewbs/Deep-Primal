from pathlib import Path
import json,zipfile,io,struct,hashlib
r=Path(__file__).resolve().parent;f=r/'final';e=f/'evidence';a=json.loads((e/'package.json').read_text());g=json.loads((r/'qualification.json').read_text())
if len(g)!=7 or any(x['exit'] for x in g):raise RuntimeError('Native matrix incomplete')
logs=[e/'default-native-0.txt',e/'default-native-1.txt',e/'optin-native-0.txt',e/'optin-native-1.txt',e/'aquifer-fresh-native.log',e/'aquifer-reopen-native.log',e/'aquifer-verify-native.log',f/'precipitation-native/fresh-native.log',f/'precipitation-native/reopen-native.log',f/'receiver-native/candidate-native.log']
for p in logs:
    text=p.read_text()
    if 'Got mod coordinates deepprimal%%'+a['artifact'] not in text:raise RuntimeError('Root package not selected '+str(p))
    for mod in ['Core','GraphicsSettings','LOD','Performance','Themes','Vulkan']:
        if a['artifact']+' > com.shnewbs.deepprimal.DeepPrimal-'+mod not in text:raise RuntimeError('Nested package not selected '+str(p)+' '+mod)
for p in logs[:4]:
    if 'STREAM_ALLOWANCE_RUNTIME_PASS cases=1445' not in p.read_text():raise RuntimeError('Missing new allowance marker')
archives=[];dlls=[]
def scan(data,label):
    with zipfile.ZipFile(io.BytesIO(data)) as z:
        if z.testzip() is not None or len(z.namelist())!=len(set(z.namelist())):raise RuntimeError('Archive failure')
        archives.append(label)
        for n in z.namelist():
            if n.endswith('.jar'):scan(z.read(n),n)
            if n.endswith('.dll'):
                b=z.read(n);off=struct.unpack_from('<I',b,60)[0]
                if b[:2]!=b'MZ' or b[off:off+4]!=b'PE\0\0' or struct.unpack_from('<H',b,off+4)[0]!=0x8664:raise RuntimeError('Wrong native payload')
                dlls.append(n)
scan(Path(a['artifact']).read_bytes(),a['artifact'])
if len(archives)!=7 or len(dlls)!=2:raise RuntimeError('Incomplete payload')
if hashlib.sha256(Path(a['producer']).read_bytes()).hexdigest()!=a['sha256']:raise RuntimeError('Producer changed')
(r/'post-native.json').write_text(json.dumps({'pass':True,'sessions':10,'nested_module_selections':60,'archive_crcs':7,'amd64_dlls':dlls,'new_allowance_markers':4,'producer_export_equal':True,'sha256':a['sha256'],'size':a['size']},indent=2));print('POST_NATIVE_PASS')
