from pathlib import Path
import hashlib, json, subprocess, zipfile
r = Path(__file__).resolve().parents[1]
p = r/'project'
out = r/'receiver-next'
javap = Path('/workspace/scratch/a22609fabda0/toolchain/jdk-25.0.4.1+1/bin/javap')
name = 'com/shnewbs/deepprimal/engine/WaterRuntime.class'
old = out/'rejected-WaterRuntime.class'
with zipfile.ZipFile(out/'rejected-stale-WaterRuntime.jar') as jar:
    old.write_bytes(jar.read(name))
new = p/'build/classes/java/main'/name
records = {}
for label, path in [('rejected', old), ('corrected', new)]:
    text = subprocess.check_output([str(javap), '-c', '-p', str(path)], text=True)
    (out/(label+'-WaterRuntime-javap.txt')).write_text(text)
    method = text.split('private static boolean transfer(')[1].split('private static com.shnewbs.deepprimal.engine.WaterRuntime$Budget budget(')[0]
    prepare = method.find('HydrologyRuntime.prepareTransfer:')
    write = method.find('HydrologyRuntime.write:')
    commit = method.find('HydrologyRuntime.transferred:')
    correct = 0 <= prepare < write < commit and 'HydrologyRuntime$Transfer;' in method
    if correct != (label == 'corrected'):
        raise RuntimeError('Unexpected transfer bytecode: '+label)
    records[label] = {'sha256': hashlib.sha256(path.read_bytes()).hexdigest(), 'preparation_before_block_writes': correct}
(out/'bytecode-result.json').write_text(json.dumps(records, indent=2))
print('TRANSFER_BYTECODE_PASS rejected_old_call=true corrected_prepare_before_write=true')
