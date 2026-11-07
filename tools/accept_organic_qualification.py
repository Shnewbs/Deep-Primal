"""Promote only the exact successful qualification whose native images were reviewed."""
from pathlib import Path
import hashlib
import json
import os
import re
import subprocess


def main():
    root = Path(__file__).resolve().parents[1]
    repo = os.environ['GITHUB_REPOSITORY']
    if repo != 'Shnewbs/Deep-Primal':
        raise ValueError('Unexpected release destination')
    folder = root / 'docs/validation/organic-drainage/native'
    accepted = json.loads((folder / 'ACCEPTED.json').read_text(encoding='utf-8'))
    raw = (folder / 'QUALIFICATION.json').read_bytes()
    proof = json.loads(raw)
    expected_images = {'scene-' + name + '.png' for name in
                       ['coast', 'headwater', 'river', 'lake', 'outlet', 'ridge']}
    if proof.get('schema') != 1 or set(proof['images']) != expected_images:
        raise ValueError('Incomplete native qualification receipt')
    if hashlib.sha256(raw).hexdigest() != accepted['qualification_sha256']:
        raise ValueError('Reviewed qualification receipt changed')
    source, tag, run_id = proof['tested_commit'], proof['release_tag'], proof['qualification_run']
    if not re.fullmatch(r'[0-9a-f]{40}', source) or tag != 'organic-' + str(run_id):
        raise ValueError('Invalid qualification identity')
    if accepted['tested_commit'] != source or accepted['release_tag'] != tag:
        raise ValueError('Acceptance targets different source or release')
    for name, expected in proof['images'].items():
        path = (folder / name).resolve()
        if path.parent != folder.resolve() or hashlib.sha256(path.read_bytes()).hexdigest() != expected:
            raise ValueError('Reviewed native image changed: ' + name)
    def gh(*args):
        return subprocess.run(['gh', *args], check=True, text=True, capture_output=True).stdout
    run = json.loads(gh('api', f'repos/{repo}/actions/runs/{run_id}'))
    if run['head_sha'] != source or run['conclusion'] != 'success':
        raise ValueError('Qualification run did not succeed for the reviewed source')
    ref = json.loads(gh('api', f'repos/{repo}/git/ref/tags/{tag}'))
    if ref['object']['type'] != 'commit' or ref['object']['sha'] != source:
        raise ValueError('Release tag does not identify the tested source')
    retained_revision = int(proof['native_channel_revision']) - 1
    notes = root / 'accepted-organic-release-notes.md'
    notes.write_text(
        f'Terrain-derived drainage for new standard/deep worlds; saved revisions 0–{retained_revision} retained.\n\n'
        'Full build/core checks, eight-seed drainage checks, native world creation/owned biome '
        'admission, six native views and Windows shaderc/VMA packaging qualified.\n\n'
        f'Tested source: {source}\nQualification: {proof["run_url"]}\n'
        f'Windows package SHA-256: {proof["package_sha256"]}\n\n'
        + accepted['visual_review'] + '\n\n'
        'Native captures use software OpenGL; Windows GPU/Vulkan execution remains a testing target.\n',
        encoding='utf-8')
    version = proof['package'].removeprefix('DeepPrimal-26.1.2-').removesuffix('-vulkan-windows.jar')
    print(gh('release', 'edit', tag, '--repo', repo, '--draft=false', '--prerelease',
             '--title', 'Deep Primal ' + version + ' — terrain-derived water', '--notes-file', str(notes)))
    print('ORGANIC_QUALIFICATION_ACCEPTED', tag)


if __name__ == '__main__':
    main()
