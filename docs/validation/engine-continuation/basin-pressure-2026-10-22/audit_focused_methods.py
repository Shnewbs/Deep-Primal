"""Compare exact method bodies against the preserved prior staged FIFO source."""
from pathlib import Path
import hashlib,json,re
ROOT=Path(__file__).resolve().parent
old_source=(ROOT/'fifo-WaterRuntime.java').read_text()
new_source=(ROOT/'limited-drain-WaterRuntime.java').read_text()
def methods(source):
    out={}
    pattern=r'^  +(?:public|private) (?:static )?(?:[\w<>\[\].]+) ([A-Za-z][\w]*)\([^;]*?\) \{'
    for match in re.finditer(pattern,source,re.M):
        brace=source.index('{',match.start()); i=brace+1; depth=1
        while depth:
            depth+=(source[i]=='{')-(source[i]=='}'); i+=1
        signature=' '.join(source[match.start():brace].split())
        assert signature not in out,signature
        out[signature]=(match.group(1),source[match.start():i])
    return out
old,new=methods(old_source),methods(new_source)
assert old.keys()==new.keys()
changed=[old[key][0] for key in old if old[key][1]!=new[key][1]]
assert set(changed)=={'localPressure','continueDrain','equalize'},changed
unchanged=['step','runDrains','runPressure','drain','transfer','move','changed',
    'open','units','chunkTicketChanged','chunkLoaded','chunkUnloaded']
for name in unchanged:
    selected=[key for key,value in old.items() if value[0]==name]
    assert selected,name
    assert all(old[key][1]==new[key][1] for key in selected),name
record={'baseline_runtime_sha256':hashlib.sha256(old_source.encode()).hexdigest(),
    'candidate_runtime_sha256':hashlib.sha256(new_source.encode()).hexdigest(),
    'changed_methods':changed,'unchanged_scheduling_and_lifecycle_methods':unchanged,
    'total_unchanged_methods':len(old)-len(changed),
    'scope':'Exact method bodies compared with prior staged FIFO; drain quantity changes deliberately. Local/retained pressure caps delegate to core. Not whole-world output equivalence.'}
(ROOT/'focused-method-audit.json').write_text(json.dumps(record,indent=2)+'\n')
print(json.dumps(record))
