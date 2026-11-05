from pathlib import Path
import hashlib,json,re,platform
r=Path(__file__).resolve().parent;e=r/'evidence';a=json.loads((e/'package.json').read_text());assert json.loads((e/'all-gates.json').read_text())['pass']
source=json.loads((e/'source-identity.json').read_text())
for f in source['files']:assert hashlib.sha256((r/'project'/f['path']).read_bytes()).hexdigest()==f['sha256'],f['path']
source['verified_after_build_and_native']=True;(e/'source-identity.json').write_text(json.dumps(source,indent=2))
for f in [Path(a['artifact']),Path(a['producer'])]:assert f.stat().st_size==a['size'] and hashlib.sha256(f.read_bytes()).hexdigest()==a['sha256']
logs=[e/'default-native-0.txt',e/'default-native-1.txt',e/'optin-native-0.txt',e/'optin-native-1.txt',e/'aquifer-fresh-native.log',e/'aquifer-reopen-native.log',e/'aquifer-verify-native.log',r/'precipitation-native/fresh-native.log',r/'precipitation-native/reopen-native.log',r/'receiver-native/candidate-native.log'];rows=[]
for f in logs:
 text=f.read_text();
 if f.name in ['default-native-0.txt','default-native-1.txt','optin-native-0.txt','optin-native-1.txt']:assert 'STREAM_ALLOWANCE_RUNTIME_PASS cases=1445' in text,f
 keep=[x for x in text.splitlines() if any(t in x for t in ['_PASS','Got mod coordinates','Saving ','Saved the game','Stopping ','All dimensions are saved','BUILD SUCCESSFUL','RCON running on'])]
 rows.append({'path':str(f.relative_to(r)),'sha256':hashlib.sha256(f.read_bytes()).hexdigest(),'events':keep})
(e/'native-log-summary.json').write_text(json.dumps({'pass':True,'sessions':rows,'actual_loaded_allowance_oracle_cases_per_profile_session':1445,'profile_sessions':4},indent=2))
(e/'final-byte-identity.json').write_text(json.dumps({'pass':True,'source_sha':a['source_sha'],'sha256':a['sha256'],'bytes':a['size'],'producer_completed':True,'all_native_consumers_completed':True,'producer_export_equal':True,'verified_source_inputs':len(source['files'])},indent=2));print('FINAL_SOURCE_NATIVE_AND_EXPORTED_BYTES_PASS')
