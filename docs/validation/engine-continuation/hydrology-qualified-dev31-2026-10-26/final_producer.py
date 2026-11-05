from pathlib import Path
import json,subprocess,time,hashlib
r=Path(__file__).resolve().parent
sha='a1583a508b0d67130d387bc018defd5758acd496'
p=r/'final/source-pin.json'; data=json.loads(p.read_text())
data={'sha':sha};p.write_text(json.dumps(data,indent=2)+'\n')
cmd=json.loads((r/'retry-command.json').read_text())[:-2]+[':jar','--rerun']
(r/'final-producer-command.json').write_text(json.dumps(cmd,indent=2))
t=time.monotonic()
with (r/'final-producer.log').open('w') as log:
    result=subprocess.run(cmd,cwd=r/'project',stdout=log,stderr=subprocess.STDOUT)
receipt={'source_sha':sha,'exit':result.returncode,'seconds':time.monotonic()-t}
(r/'final-producer.json').write_text(json.dumps(receipt,indent=2))
if result.returncode:raise RuntimeError('Final producer failed')
producer=r/'project/build/libs/DeepPrimal-26.1.2-0.0.1-alpha.29-dev.31-vulkan-windows.jar'
export=r/'final'/producer.name
actual=hashlib.sha256(producer.read_bytes()).hexdigest()
if actual!='d1823835f3c2a8c54217edfcf7349df379375df29f0714707c8d028aaaeac876' or producer.read_bytes()!=export.read_bytes():raise RuntimeError('Final producer differs from native-tested candidate')
receipt.update({'sha256':actual,'size':producer.stat().st_size,'same_bytes_as_ten_native_sessions':True,'producer_completed':True})
(r/'final-producer.json').write_text(json.dumps(receipt,indent=2))
for script in [r/'final/validate_package.py',r/'post_native.py']:
    subprocess.run(['python3',str(script)],check=True)
print('FINAL_PRODUCER_PASS')
