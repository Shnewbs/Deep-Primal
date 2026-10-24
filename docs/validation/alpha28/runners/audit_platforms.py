from pathlib import Path
import zipfile,io,hashlib,json
r=Path(__file__).resolve().parent

def flat(path):
    out={};archives=[]
    def scan(raw,prefix=''):
        with zipfile.ZipFile(io.BytesIO(raw)) as z:
            assert z.testzip() is None
            for name in z.namelist():
                normalized=name.replace('-linux.jar','-PLATFORM.jar').replace('-windows.jar','-PLATFORM.jar')
                if name.endswith('.jar'):
                    archives.append(name);scan(z.read(name),prefix+normalized+'/')
                elif not name.endswith('/'):
                    value=z.read(name)
                    if name=='META-INF/jarjar/metadata.json':
                        value=value.replace(b'-linux.jar',b'-PLATFORM.jar').replace(b'-windows.jar',b'-PLATFORM.jar')
                    out[prefix+normalized]=hashlib.sha256(value).hexdigest()
    scan(path.read_bytes());return out,archives
l,ln=flat(r/'project/build/libs/DeepPrimal-26.1.2-0.0.1-alpha.28-vulkan-linux.jar')
w,wn=flat(r/'project/build/libs/DeepPrimal-26.1.2-0.0.1-alpha.28-vulkan-windows.jar')
common=l.keys()&w.keys();diff=[n for n in common if l[n]!=w[n]]
assert not diff,diff
assert any(n.endswith('.dll') for n in w) and not any(n.endswith('.so') for n in w)
assert any(n.endswith('.so') for n in l) and not any(n.endswith('.dll') for n in l)
onlyl=sorted(l.keys()-w.keys());onlyw=sorted(w.keys()-l.keys())
assert all(n.endswith(('.so','.so.git')) for n in onlyl),onlyl
assert all(n.endswith(('.dll','.dll.git')) for n in onlyw),onlyw
record={'pass':True,'normalization':'Only Vulkan nested archive filename and matching jarjar metadata path; no runtime class/asset normalization','common_identical_entries':len(common),'common_differences':diff,'linux_only':onlyl,'windows_only':onlyw,'linux_nested':ln,'windows_nested':wn}
(r/'evidence/platform-payload.json').write_text(json.dumps(record,indent=2));print(record)
