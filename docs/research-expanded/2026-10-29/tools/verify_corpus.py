#!/usr/bin/env python3
"""Verify the expanded Markdown corpus using Python's standard library."""
import collections
import hashlib
import json
import pathlib
import re
import sys
from urllib.parse import unquote, urlsplit

root = pathlib.Path(__file__).resolve().parents[1]
manifest = json.loads((root / "manifest.json").read_text())
errors = []
docs = manifest["documents"]
ids = [d["id"] for d in docs]
paths = [d["replica_path"] for d in docs]
if len(set(ids)) != len(ids) or len(set(paths)) != len(paths):
    errors.append("Duplicate document ID or replica path.")
if len(docs) != manifest["source_counts"]["total"]:
    errors.append("Inventory total does not match replica count.")

def sha256(data):
    return hashlib.sha256(data).hexdigest()

def git_blob(data):
    return hashlib.sha1(b"blob " + str(len(data)).encode() + b"\0" + data).hexdigest()

by_path = {d["replica_path"]: d for d in docs}
reviews = {}
for file in (root / "reviews").glob("*.json"):
    for review in json.loads(file.read_text()):
        if review["id"] in reviews:
            errors.append("Duplicate review: " + review["id"])
        reviews[review["id"]] = review
if set(reviews) != set(ids):
    errors.append("Review coverage differs from source inventory.")

git_count = 0
for doc in docs:
    file = root / doc["replica_path"]
    if not file.is_file():
        errors.append("Missing replica: " + doc["replica_path"])
        continue
    data = file.read_bytes()
    if len(data) != doc["replica_bytes"] or sha256(data) != doc["replica_sha256"]:
        errors.append("Replica bytes changed: " + doc["replica_path"])
    start = doc["source_start_byte"]
    original = data[start:start + doc["source_length_bytes"]]
    if len(original) != doc["source_bytes"] or sha256(original) != doc["source_sha256"]:
        errors.append("Preserved source mismatch: " + doc["id"])
    if doc["source"]["kind"] == "git":
        git_count += 1
        if git_blob(original) != doc["source"]["git_blob_sha"]:
            errors.append("Git blob mismatch: " + doc["id"])
    review = reviews.get(doc["id"], {})
    if review.get("review_depth") not in ["full-text", "structured-evidence"]:
        errors.append("Missing/invalid review method: " + doc["id"])
    for field in ["current_scope", "gaps", "proposed_additions", "acceptance_checks", "source_anchors", "chapter", "priority"]:
        if not review.get(field):
            errors.append("Empty review " + field + ": " + doc["id"])
    if not (root / doc["review_chapter"]).is_file():
        errors.append("Missing chapter: " + doc["review_chapter"])

actual_replicas = {str(p.relative_to(root)) for p in (root / "replicas").rglob("*.md")}
if actual_replicas != set(paths):
    errors.append("Replica directory contains missing or unmanifested Markdown.")
if git_count != manifest["source_counts"]["git"]:
    errors.append("Git source count mismatch.")

def without_fences(text):
    out = []
    fence = None
    for line in text.splitlines():
        match = re.match(r"^\s{0,3}(`{3,}|~{3,})", line)
        if match:
            char = match.group(1)[0]
            if fence is None:
                fence = char
            elif char == fence:
                fence = None
            continue
        if fence is None:
            out.append(line)
    return "\n".join(out)

relative_links = 0
for file in root.rglob("*.md"):
    rel = str(file.relative_to(root))
    data = file.read_bytes()
    # Preserve originals without treating historical links as newly authored.
    if rel in by_path:
        data = data[:by_path[rel]["source_start_byte"]]
    text = without_fences(data.decode("utf-8"))
    targets = re.findall(r"\]\(([^\n)]+)\)", text)
    targets += re.findall(r"(?m)^\[[^\]]+\]:\s*(\S+)", text)
    for target in targets:
        target = target.strip()
        if target.startswith("<"):
            target = target[1:target.find(">")]
        else:
            target = target.split()[0]
        url = urlsplit(target)
        if url.scheme or url.netloc or not url.path:
            continue
        if url.path.startswith("/"):
            errors.append("Nonportable authored absolute link: " + rel)
            continue
        path = (file.parent / unquote(url.path)).resolve()
        relative_links += 1
        if not path.is_relative_to(root.resolve()) or not path.exists():
            errors.append("Broken authored link: " + rel + " -> " + target)

depths = collections.Counter(d["review_depth"] for d in docs)
result = {
    "status": "PASS" if not errors else "FAIL",
    "replicas": len(docs),
    "git_source_blobs_verified": git_count,
    "preserved_source_sections_verified": len(docs),
    "review_cards_verified": len(reviews),
    "review_depths": dict(depths),
    "authored_relative_file_links_checked": relative_links,
    "exact_duplicate_groups": len(manifest["exact_duplicate_groups"]),
    "historical_unresolved_relative_references": sum(len(d["unresolved_original_relative_links"]) for d in docs),
    "errors": errors,
}
print(json.dumps(result, ensure_ascii=False, indent=2))
sys.exit(0 if not errors else 1)
