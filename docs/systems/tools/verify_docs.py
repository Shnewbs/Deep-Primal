#!/usr/bin/env python3
"""Validate these design documents and examples; does not test engine behavior."""
from pathlib import Path
import hashlib,json,posixpath,re,sys,unicodedata,urllib.parse

HERE=Path(__file__).resolve().parents[1]
ROOT=HERE.parents[1]
errors=[];json_blocks=0;targets=0;anchors=0;external=set();documents=sorted(HERE.glob('*.md'))

def slugs(p):
    result=set(re.findall(r'<a\s+(?:id|name)=[\"\']([^\"\']+)',p.read_text()))
    occurrences={};fenced=False
    for line in p.read_text().splitlines():
        if re.match(r'^\s*(```|~~~)',line):fenced=not fenced;continue
        if fenced or not re.match(r'^#{1,6}\s',line):continue
        title=re.sub(r'^#{1,6}\s+','',line).strip().lower()
        title=re.sub(r'\[([^\]]+)\]\([^)]*\)',r'\1',title)
        title=title.replace('`','')
        slug=''.join(c for c in title if c in '-_ ' or unicodedata.category(c)[0] in ('L','N')).replace(' ','-')
        n=occurrences.get(slug,0);occurrences[slug]=n+1
        result.add(slug if not n else slug+'-'+str(n))
    return result

for p in documents:
    s=p.read_text(encoding='utf-8');fence=None;captured=[]
    for n,line in enumerate(s.splitlines(),1):
        m=re.match(r'^\s*(```|~~~)(.*)$',line)
        if m:
            if fence is None:fence=(m[1],m[2].strip());captured=[]
            elif m[1]==fence[0]:
                if fence[1]=='json':
                    try:
                        obj=json.loads('\n'.join(captured));json_blocks+=1
                        if isinstance(obj,dict) and isinstance(obj.get('quests'),list):
                            ids=[q['id'] for q in obj['quests']]
                            if len(ids)!=len(set(ids)):errors.append(str(p)+': repeated quest ID in example')
                    except (ValueError,KeyError) as e:errors.append(str(p)+': invalid JSON example: '+str(e))
                fence=None
            continue
        if fence:captured.append(line);continue
        dests=re.findall(r'\]\(([^\n]+?)\)',line)+re.findall(r'^\s*\[[^\]]+\]:\s*(\S+)',line)
        for dest in dests:
            dest=dest.strip().split(' "',1)[0].strip('<>')
            if re.match(r'^https?://',dest):external.add(dest);continue
            if not dest or re.match(r'^[A-Za-z][A-Za-z0-9+.-]*:',dest):continue
            target,_,fragment=dest.partition('#')
            q=p if not target else (p.parent/urllib.parse.unquote(target)).resolve()
            targets+=1
            if not q.is_relative_to(ROOT):errors.append(f'{p.name}:{n}: target escapes project: {dest}');continue
            if not q.is_file():errors.append(f'{p.name}:{n}: missing target: {dest}');continue
            if fragment and q.suffix.lower()=='.md':
                anchors+=1
                if urllib.parse.unquote(fragment) not in slugs(q):errors.append(f'{p.name}:{n}: missing anchor: {dest}')
    if fence:errors.append(p.name+': unclosed fence')

manifest_path=HERE/'DOCUMENT-MANIFEST.json'
checked=0
if manifest_path.exists():
    manifest=json.loads(manifest_path.read_text())
    for item in manifest['files']:
        p=ROOT/item['path'];checked+=1
        if not p.is_file() or hashlib.sha256(p.read_bytes()).hexdigest()!=item['sha256']:errors.append('document hash drift: '+item['path'])
else:errors.append('DOCUMENT-MANIFEST.json missing')

receipt={'scope':'documentation, links, example JSON and published-byte checkpoint only','research_date':'2026-10-29 America/Los_Angeles','markdown_documents':len(documents),'manifest_files_checked':checked,'relative_targets_checked':targets,'local_anchors_checked':anchors,'json_examples_parsed':json_blocks,'unique_external_urls':len(external),'external_url_live_recheck':'not run; reference retrieval/version limitations recorded in domain ledgers','content_review':'separate editorial review; not inferred by this checker','runtime_implementation_or_native_tests':'not run for this documentation task','historical_handbook_catalog':'retains earlier pinned checkpoint; later reviewed source edits can produce expected drift there','errors':errors,'pass':not errors}
print(json.dumps(receipt,indent=2))
sys.exit(1 if errors else 0)
