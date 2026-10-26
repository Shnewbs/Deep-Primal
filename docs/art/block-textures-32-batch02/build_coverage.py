"""Build the catalog crosswalk from retained source rows and verified exports."""
from pathlib import Path
import json,hashlib
r=Path(__file__).resolve().parent
catalog=json.loads((r/'catalog-rows.json').read_text())
new=json.loads((r/'manifest.json').read_text())['assets']
old=json.loads((r/'reference-assets.json').read_text())['assets']
by_id={a['id']:dict(a,batch='block-textures-32-batch02',local=a['tile']) for a in new}
by_id.update({a['id']:dict(a,batch='block-textures-32-batch01',local='reference-tiles/'+a['id']+'.png') for a in old})
for a in by_id.values():
 p=r/a['local']
 # A repository checkout can resolve prior tiles through the adjacent batch.
 if not p.exists() and a['batch'].endswith('01'):p=r.parent/a['batch']/a['tile']
 assert p.exists(),str(p)
 assert hashlib.sha256(p.read_bytes()).hexdigest()==a['sha256'],a['id']
def stone(*names):return ['stone/'+n for n in names]
overrides={
 'ROCK-LEGACY-020':stone('mudstone','claystone'),
 'ROCK-LEGACY-027':stone('lignite','bituminous_coal','anthracite'),
 'ROCK-LEGACY-031':stone('ironstone','banded_iron_formation'),
 'ROCK-ADD-021':stone('mudstone'),
 'ROCK-ADD-023':stone('dolostone'),
 'ROCK-ADD-033':stone('ironstone'),
 'ROCK-ADD-034':stone('eclogite'),
 'ROCK-ADD-035':stone('granulite'),
 'ROCK-ADD-043':stone('mudstone'),
 'ROCK-ADD-044':stone('halite','gypsum'),
}
notes={
 'ROCK-LEGACY-018':'Retained Dolomite rock label maps to the dolostone base; mineral dolomite is a separate catalog subject.',
 'ROCK-LEGACY-020':'Retained grouped row now has distinct mudstone and claystone candidates.',
 'ROCK-LEGACY-027':'The three ranks named in this retained row now have separate candidates. Generic batch-01 coal remains a historical comparison, not a rank assignment.',
 'ROCK-LEGACY-031':'Ironstone and banded iron formation have separate candidate surfaces.',
 'ROCK-LEGACY-044':'Eclogite and granulite have separate candidate surfaces.',
 'ROCK-ADD-021':'Reuses the retained mudstone candidate.',
 'ROCK-ADD-023':'Reuses the dolostone candidate mapped from the retained Dolomite rock label.',
 'ROCK-ADD-033':'Reuses the ironstone candidate from the retained grouped row.',
 'ROCK-ADD-034':'Reuses the eclogite candidate from the retained grouped row.',
 'ROCK-ADD-035':'Reuses the granulite candidate from the retained grouped row.',
 'ROCK-ADD-043':'Provisional art reuse of compact mudstone. Argillite terminology and rock classification still require a design decision; this is not a taxonomy or gameplay change.',
 'ROCK-ADD-044':'Umbrella family selects salt- or gypsum-bearing base art; no third duplicate evaporite resource is introduced.',
}
entries=[]
for row in catalog['rows']:
 ids=overrides.get(row['id'])
 if ids is None:ids=[a['id'] for a in by_id.values() if row['id'] in a['source_catalog'].split(';')]
 assert ids,('unmapped catalog row',row['id'])
 assets=[]
 for aid in ids:
  a=by_id[aid]
  assets.append({'id':aid,'batch':a['batch'],'path':'docs/art/'+a['batch']+'/'+a['tile'],'sha256':a['sha256']})
 entries.append(dict(row,assets=assets,status='provisional_shared_base' if row['id']=='ROCK-ADD-043' else 'base_candidate_mapped',mapping_note=notes.get(row['id'],'Individual grayscale base candidate; palette, directional faces and native rendering review remain pending.')))
assert len(entries)==88 and len({e['id'] for e in entries})==88
assert set(a['id'] for a in new)<=set(a['id'] for e in entries for a in e['assets'])
used={(a['batch'],a['id']) for e in entries for a in e['assets']}
out={'schema':1,'source_commit':catalog['source_commit'],'source_path':catalog['source_path'],'catalog_rows':88,'new_assets':len(new),'mapped_rows':len(entries),'provisional_rows':['ROCK-ADD-043'],'unique_base_candidates':len(used),'reused_prior_candidates':sum(batch.endswith('01') for batch,aid in used),'runtime_integrated':False,'full_family_acceptance':False,'rows':entries}
(r/'rock-coverage.json').write_text(json.dumps(out,indent=2)+'\n')
lines=['# Rock base candidate coverage','','All 88 retained/expanded rock catalog rows have base-art assignments. Argillite has a provisional shared assignment pending terminology review. These are candidate surfaces, not accepted gameplay registrations or complete directional/state families.','','| Catalog ID | Source name | Candidate base(s) | Mapping |','| --- | --- | --- | --- |']
for e in entries:
 links=', '.join('['+a['id'].split('/')[-1]+'](../'+a['batch']+'/tiles/'+a['id']+'.png)' for a in e['assets'])
 lines.append('| '+e['id']+' | '+e['name']+' | '+links+' | '+e['mapping_note']+' |')
(r/'ROCK-COVERAGE.md').write_text('\n'.join(lines)+'\n')
print(json.dumps({k:v for k,v in out.items() if k!='rows'}))
