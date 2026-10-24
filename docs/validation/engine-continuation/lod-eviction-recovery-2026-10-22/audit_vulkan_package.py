"""Audit finished development archives, compiled class identity and payload boundaries."""
from pathlib import Path
import hashlib
import io
import json
import zipfile
ROOT=Path(__file__).resolve().parent
PROJECT=ROOT/'project'
jar,=list((PROJECT/'build/libs').glob('*-vulkan-linux.jar'))
archives=[];matched={}
def check(data,label):
    with zipfile.ZipFile(io.BytesIO(data)) as archive:
        assert archive.testzip() is None, label
        archives.append(label)
        assert not any('/validation/' in n or 'ProcessFallback' in n or 'CheckedExceptionAgent' in n or 'ProcessCommandFallback' in n or 'com/shnewbs/deepprimal/buildcompat/' in n for n in archive.namelist())
        for name in archive.namelist():
            if name.startswith('META-INF/jarjar/') and name.endswith('.jar'):
                check(archive.read(name),label+'!'+name)
def compare(archive,directory):
    count=0
    for p in directory.rglob('*.class'):
        key=p.relative_to(directory).as_posix()
        assert archive.read(key)==p.read_bytes(),key
        count+=1
    assert count>0,directory
    return count
check(jar.read_bytes(),jar.name)
with zipfile.ZipFile(jar) as archive:
    assert not any('WaterRuntime$DrainJob' in n or 'WaterRuntime$DrainDiagnostics' in n for n in archive.namelist()), 'Unaccepted retained-drain classes in accepted solver archive'
    matched['main']=compare(archive,PROJECT/'build/classes/java/main')
    for label,module in [('Core','engine-core'),('GraphicsSettings','graphics-settings'),('LOD','lod-engine'),('Performance','performance-engine'),('Vulkan','renderer-vulkan')]:
        name,=[n for n in archive.namelist() if n.startswith('META-INF/jarjar/') and ('DeepPrimal-'+label+'-') in n and n.endswith('.jar')]
        with zipfile.ZipFile(io.BytesIO(archive.read(name))) as nested:
            matched[module]=compare(nested,PROJECT/module/'build/classes/java/main')
            if module=='renderer-vulkan':
                assert b'STARTUP_TRACE' not in nested.read('net/vulkanmod/deepprimal/Startup.class'), 'Temporary trace in candidate payload'
                for f in ['LICENSE','COPYING','UPSTREAM.json']:
                    assert nested.read('META-INF/licenses/vulkanmod/'+f)==(PROJECT/module/f).read_bytes()
                assert any(n.endswith('.so') for n in nested.namelist()),'Missing Linux natives'
                assert not any(n.endswith(('.dll','.dylib')) for n in nested.namelist()),'Wrong platform natives'
record={'scope':'LOD persisted eviction recovery and queued edit guards; accepted solver and Vulkan startup/frame code unchanged; temporary tracing absent',
    'non_vulkan':False,'changed_source_sha256':{str(f.relative_to(PROJECT)):hashlib.sha256(f.read_bytes()).hexdigest() for f in [PROJECT/'lod-engine/src/main/java/com/shnewbs/deepprimal/lod/client/VoxelLodRenderer.java']},'archives_crc_pass':archives,'compiled_class_byte_matches':matched,
    'jar_bytes':jar.stat().st_size,'jar_sha256':hashlib.sha256(jar.read_bytes()).hexdigest(),
    'runtime_source_sha256':hashlib.sha256((PROJECT/'src/main/java/com/shnewbs/deepprimal/engine/WaterRuntime.java').read_bytes()).hexdigest(),
    'core_source_sha256':hashlib.sha256((PROJECT/'engine-core/src/main/java/com/shnewbs/deepprimal/core/WaterTransport.java').read_bytes()).hexdigest(),
    'validation_and_private_agent_payload_absent':True,'installer_release':False}
(ROOT/'vulkan-build-audit.json').write_text(json.dumps(record,indent=2)+'\n')
print(json.dumps(record))
