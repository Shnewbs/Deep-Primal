"""Preserve qualified CI output without consuming GitHub Actions artifact storage.

Runs only after the native and package checks. Creates a draft release and commits
the six native views plus their exact provenance to main with normal fast-forward pushes.
"""
from pathlib import Path
import base64
import hashlib
import json
import os
import re
import shutil
import subprocess
import zipfile


def command(*args, capture=False, check=True):
    return subprocess.run(args, check=check, text=True, capture_output=capture)


def image_transport(root, repo, source, images, names):
    """Lossless review of private PNGs beyond the Contents API's 1 MiB limit.

    The full source tree is retained in an unreferenced commit; no branch is created
    or moved. Only its content-addressed pointer is included in the receipt.
    """
    def api(endpoint, payload=None):
        args = ['gh', 'api', f'repos/{repo}/{endpoint}']
        if payload is not None:
            request = root / 'build/review-api-request.json'
            request.write_text(json.dumps(payload), encoding='utf-8')
            args += ['--method', 'POST', '--input', str(request)]
        return json.loads(command(*args, capture=True).stdout)
    entries, manifest = [], {}
    for name in names:
        encoded = base64.b64encode((images / name).read_bytes()).decode('ascii')
        parts = []
        for index, offset in enumerate(range(0, len(encoded), 180000)):
            path = f'docs/validation/organic-drainage/transport/{name}.{index:03d}.b64'
            blob = api('git/blobs', {'content': encoded[offset:offset + 180000], 'encoding': 'utf-8'})
            entries.append({'path': path, 'mode': '100644', 'type': 'blob', 'sha': blob['sha']})
            parts.append(path)
        manifest[name] = parts
    base = api('git/commits/' + source)['tree']['sha']
    tree = api('git/trees', {'base_tree': base, 'tree': entries})
    commit = api('git/commits', {'message': 'Lossless native image review transport',
                               'tree': tree['sha'], 'parents': [source]})
    return {'commit': commit['sha'], 'parts': manifest}


def main():
    root = Path(__file__).resolve().parents[1]
    os.chdir(root)
    repo = os.environ['GITHUB_REPOSITORY']
    source = os.environ['GITHUB_SHA']
    run_id = os.environ['GITHUB_RUN_ID']
    if repo != 'Shnewbs/Deep-Primal' or not re.fullmatch(r'[0-9a-f]{40}', source) or not run_id.isdigit():
        raise ValueError('Unexpected qualification destination or identity')
    tag = 'organic-' + run_id
    capture = root / 'build/organic-native'
    images = capture / 'screenshots'
    names = ['scene-' + name + '.png' for name in ['coast', 'headwater', 'river', 'lake', 'outlet', 'ridge']]
    for name in names:
        if not (images / name).read_bytes().startswith(b'\x89PNG\r\n\x1a\n'):
            raise ValueError('Missing native PNG: ' + name)
    packages = list((root / 'build/libs').glob('*vulkan-windows.jar'))
    if len(packages) != 1:
        raise ValueError('Expected one Windows Vulkan package')
    package = packages[0]
    digest = lambda p: hashlib.sha256(p.read_bytes()).hexdigest()
    receipt = {
        'schema': 1, 'tested_commit': source, 'qualification_run': int(run_id),
        'release_tag': tag, 'native_channel_revision': 10, 'owned_biomes': 40,
        'native_review': 'pending', 'package': package.name, 'package_sha256': digest(package),
        'images': {name: digest(images / name) for name in names},
        'run_url': f'https://github.com/{repo}/actions/runs/{run_id}',
        'full_check_receipt': json.loads((root / 'build/full-check-receipt.json').read_text(encoding='utf-8')),
        'image_transport': image_transport(root, repo, source, images, names),
    }
    receipt_file = capture / 'QUALIFICATION.json'
    receipt_file.write_text(json.dumps(receipt, indent=2) + '\n', encoding='utf-8')
    proof = root / 'build/organic-qualification.zip'
    with zipfile.ZipFile(proof, 'w', zipfile.ZIP_DEFLATED) as archive:
        for path in sorted(capture.rglob('*')):
            if path.is_file():
                archive.write(path, path.relative_to(capture))
        archive.write(root / 'build/organic-native-client.log', 'native-client.log')
    notes = root / 'build/organic-release-notes.md'
    notes.write_text(
        'Full build/core checks, actual world creation, six native captures and Windows Vulkan packaging passed.\n\n'
        f'Tested source: {source}\nQualification: {receipt["run_url"]}\n\n'
        'Draft pending visual review of the committed native evidence.\n', encoding='utf-8')
    ref_endpoint = f'repos/{repo}/git/ref/tags/{tag}'
    ref = command('gh', 'api', ref_endpoint, capture=True, check=False)
    if ref.returncode != 0:
        command('gh', 'api', '--method', 'POST', f'repos/{repo}/git/refs',
                '-f', 'ref=refs/tags/' + tag, '-f', 'sha=' + source)
        ref = command('gh', 'api', ref_endpoint, capture=True)
    tag_object = json.loads(ref.stdout)['object']
    if tag_object['type'] != 'commit' or tag_object['sha'] != source:
        raise ValueError('Refusing to replace a differently sourced qualification tag')
    existing = command('gh', 'release', 'view', tag, '--repo', repo, '--json', 'isDraft,targetCommitish',
                       capture=True, check=False)
    if existing.returncode == 0:
        data = json.loads(existing.stdout)
        if not data['isDraft'] or data['targetCommitish'] != source:
            raise ValueError('Refusing to replace an accepted or differently sourced release')
        command('gh', 'release', 'upload', tag, str(package), str(proof), '--repo', repo, '--clobber')
    else:
        command('gh', 'release', 'create', tag, str(package), str(proof), '--repo', repo,
                '--target', source, '--verify-tag', '--draft', '--prerelease',
                '--title', 'Deep Primal organic terrain qualification', '--notes-file', str(notes))
    command('gh', 'auth', 'setup-git', '--hostname', 'github.com')
    command('git', 'config', 'user.name', 'github-actions[bot]')
    command('git', 'config', 'user.email', '41898282+github-actions[bot]@users.noreply.github.com')
    # Preserve any independent main commits made while the native checks were running.
    for attempt in range(3):
        command('git', 'fetch', 'origin', 'main')
        command('git', 'checkout', '--detach', 'FETCH_HEAD')
        destination = root / 'docs/validation/organic-drainage/native'
        destination.mkdir(parents=True, exist_ok=True)
        for name in names + ['QUALIFICATION.json']:
            shutil.copyfile((capture if name == 'QUALIFICATION.json' else images) / name,
                            destination / name)
        command('git', 'add', *[str(destination / name) for name in names + ['QUALIFICATION.json']])
        if command('git', 'diff', '--cached', '--quiet', check=False).returncode == 0:
            print('QUALIFICATION_EVIDENCE_ALREADY_PRESENT', tag)
            return
        command('git', 'commit', '-m', 'Record native organic terrain qualification ' + run_id)
        if command('git', 'push', 'origin', 'HEAD:main', check=False).returncode == 0:
            print('QUALIFICATION_DRAFT_AND_EVIDENCE_SAVED', tag)
            return
    raise RuntimeError('Main advanced during all three fast-forward publication attempts')


if __name__ == '__main__':
    main()
