#!/usr/bin/env python3
"""Verify this documentation checkpoint, not engine/native/GPU behavior.

Run from any directory: python3 docs/handbook/tools/verify_handbook.py
Later source edits intentionally cause checkpoint drift; refresh the catalog after review.
"""
from pathlib import Path
import collections, hashlib, json, posixpath, re, sys, urllib.parse

HANDBOOK=Path(__file__).resolve().parents[1]
ROOT=HANDBOOK.parents[1]
catalog=json.loads((HANDBOOK/'audit/catalog.json').read_text())
known=set(json.loads((HANDBOOK/'audit/known-tracked-paths.json').read_text()))
known.update(str(p.relative_to(ROOT)) for p in HANDBOOK.rglob('*') if p.is_file())
known.add('docs/README.md')
supplement=json.loads((HANDBOOK/'audit/publication-supplement.json').read_text())
known.update(d['path'] for d in supplement)
errors=[];checked=0;authored_links=0;source_links=0
def links(path):
    fenced=False
    for n,line in enumerate(path.read_text().splitlines(),1):
        if re.match(r'^\s*(```|~~~)',line):
            fenced=not fenced
            continue
        if fenced:continue
        targets=re.findall(r'\]\(([^\n]+?)\)',line)
        targets+=re.findall(r'^\s*\[[^\]]+\]:\s*(\S+)',line)
        for target in targets:
            target=target.strip().split(' "',1)[0].strip('<>')
            if not target or target.startswith('#') or re.match(r'^[a-zA-Z][a-zA-Z0-9+.-]*:',target):continue
            target=urllib.parse.unquote(target.split('#',1)[0].split('?',1)[0])
            if not target:continue
            parent=str(path.parent.relative_to(ROOT))
            resolved=posixpath.normpath(posixpath.join(parent,target)) if not target.startswith('/') else target.lstrip('/')
            exists=resolved in known or any(p.startswith(resolved.rstrip('/')+'/') for p in known)
            yield n,target,resolved,exists

ids=[];hashes=collections.defaultdict(list)
for d in catalog['documents']:
    ids.append(d['id']);p=ROOT/d['canonical_path']
    if not p.is_file():errors.append('missing source: '+d['id']);continue
    got=hashlib.sha256(p.read_bytes()).hexdigest()
    if got!=d['published_sha256']:errors.append('source checkpoint drift: '+d['canonical_path'])
    if d['origin']=='library' and not d['duplicate_git_path'] and got!=d['source_sha256']:errors.append('imported source changed: '+d['id'])
    hashes[d['source_sha256']].append(d['id']);checked+=1
    if d['origin']=='git':
        for n,target,resolved,exists in links(p):
            source_links+=1
            if not exists:errors.append(f'original Git link: {d["canonical_path"]}:{n}: {target} -> {resolved}')
if len(ids)!=len(set(ids)):errors.append('duplicate source identity')
if len(ids)!=544:errors.append('unexpected source identity count')
if len(catalog['retained_expansion'])!=507:errors.append('unexpected frozen expansion count')
expanded_ids=[d['path'] for d in catalog['retained_expansion']]
if len(set(expanded_ids))!=len(expanded_ids):errors.append('duplicate expansion path')
expanded_present=0;expanded_missing=0
for d in catalog['retained_expansion']:
    if not re.fullmatch(r'[0-9a-f]{40}',d['git_blob_sha']):errors.append('invalid expansion blob identity: '+d['path'])
    p=ROOT/d['path']
    if p.is_file():
        b=p.read_bytes();got=hashlib.sha1(b'blob '+str(len(b)).encode()+b'\0'+b).hexdigest();expanded_present+=1
        if got!=d['git_blob_sha']:errors.append('frozen expansion changed: '+d['path'])
    else:
        expanded_missing+=1
        if '--require-complete-expansion' in sys.argv:errors.append('frozen expansion unavailable: '+d['path'])
for d in supplement:
    p=ROOT/d['path']
    if not p.is_file() or hashlib.sha256(p.read_bytes()).hexdigest()!=d['sha256']:errors.append('supplement checkpoint drift: '+d['path'])
    if p.is_file():
        for n,target,resolved,exists in links(p):
            source_links+=1
            if not exists:errors.append(f'supplement link: {d["path"]}:{n}: {target} -> {resolved}')
if len(hashes)!=533:errors.append('unexpected unique source count')
groups=sorted(sorted(v) for v in hashes.values() if len(v)>1)
if groups!=sorted(sorted(v) for v in catalog['exact_duplicate_groups']):errors.append('duplicate group mismatch')

authored=[ROOT/'docs/README.md']+[p for p in HANDBOOK.rglob('*.md') if 'sources' not in p.relative_to(HANDBOOK).parts]
for p in authored:
    for n,target,resolved,exists in links(p):
        authored_links+=1
        if not exists:errors.append(f'handbook link: {p.relative_to(ROOT)}:{n}: {target} -> {resolved}')
receipt={'scope':'documentation checkpoint only','source_identities_checked':checked,'publication_supplement_checked':len(supplement),'unique_source_hashes':len(hashes),'exact_duplicate_groups':len(groups),'retained_expansion_paths_inventoried':len(catalog['retained_expansion']),'retained_expansion_available_blob_checks':expanded_present,'retained_expansion_not_materialized':expanded_missing,'original_git_relative_targets_checked':source_links,'authored_relative_targets_checked':authored_links,'preserved_library_context_targets':16,'errors':errors,'pass':not errors}
print(json.dumps(receipt,indent=2))
sys.exit(1 if errors else 0)
