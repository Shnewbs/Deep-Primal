from pathlib import Path
import hashlib,json,re
m=Path('/dev/shm/audit-e528d06-20261026T1934');e=m/'evidence';p=Path('/workspace/scratch/a2f7076b7762/audit-8665e07-20261026T1834/project');old=json.loads(Path('/dev/shm/audit-8665e07-20261026T1834/evidence/source-identity.json').read_text())
for f in old['files']:assert hashlib.sha256((p/f['path']).read_bytes()).hexdigest()==f['sha256'],f['path']
(e/'production-inputs.json').write_text(json.dumps({'pass':True,'source_sha':'e528d066d8202ac1a2822de700099a34ce67d470','verified_inputs':len(old['files']),'fresh_compile':False,'basis':'Fresh retained input rehash plus complete Git compare with no non-documentation changes','previous_source_sha':old['source_sha']},indent=2))
def body(source,name):
 # Mask Java comments/quoted literals while keeping positions for brace matching.
 masked=re.sub(r'/\*[\s\S]*?\*/|//[^\n]*|"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\'',lambda x:' '*len(x.group()),source)
 match=re.search(r'^  (?:private |public |static |final |protected )*[^\n;{}]*\b'+name+r'\([^;{]*\)\s*\{',masked,re.M)
 if not match:raise RuntimeError('No unique method '+name)
 opening=masked.index('{',match.start());depth=1;i=opening+1
 while depth:depth+=(masked[i]=='{')-(masked[i]=='}');i+=1
 return source[match.start():i]
a=(p/'src/main/java/com/shnewbs/deepprimal/engine/HydrologyRuntime.java').read_text();b=(m/'stage/HydrologyRuntime.java').read_text();rows=[]
for n in ['replenishStream','streamAllowance','visit','precipitation','write']:
 x,y=body(a,n),body(b,n);rows.append({'method':n,'equal':x==y,'production_sha256':hashlib.sha256(x.encode()).hexdigest(),'stage_sha256':hashlib.sha256(y.encode()).hexdigest()})
(e/'physical-methods.json').write_text(json.dumps({'pass':all(x['equal'] for x in rows),'scope':'Exact full method bodies; not scheduling or whole-world conservation proof','methods':rows},indent=2));assert all(x['equal'] for x in rows)
print('PRODUCTION_INPUTS_AND_FIVE_PHYSICAL_METHODS_PASS')

