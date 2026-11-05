"""Independent PNG checks. Requires Pillow; reads images without editing them."""
import json, hashlib, sys
from pathlib import Path
from PIL import Image
new, old, reproduction = map(Path, sys.argv[1:4])
report = {'assets': [], 'mask_partition_checks': 0, 'in_game_tested': False}
def pixels(p):
    im = Image.open(p)
    assert im.size == (64, 64) and im.mode == 'L', (p, im.size, im.mode)
    im.load()
    return list(im.tobytes())
def mean(a): return sum(a)/len(a)
for p in sorted(new.rglob('*.png')):
    rel = p.relative_to(new)
    if p.name not in ['dirt_side.png', 'grass_side.png', 'podzol_side.png'] and rel.parts[0] != 'masks': continue
    a = pixels(p)
    assert all(a[y*64] == a[y*64+63] for y in range(64)), rel
    if p.name == 'dirt_side.png': assert a[:64] == a[-64:], rel
    assert p.read_bytes() == (reproduction/rel).read_bytes(), rel
    item = {'path': str(rel), 'sha256': hashlib.sha256(p.read_bytes()).hexdigest(), 'wrap': 'pass', 'reproduction': 'pass'}
    if rel.parts[0] != 'masks':
        b = pixels(old/rel)
        before = mean(b[:512])-mean(b[16*64:56*64])
        after = mean(a[:512])-mean(a[16*64:56*64])
        assert abs(after) < 8, (rel, after)
        assert 180 < mean(a) < 204
        item.update({'old_cap_minus_body_luminance': before, 'new_cap_minus_body_luminance': after, 'mean_luminance': mean(a)})
    report['assets'].append(item)
for name in ['grass', 'podzol']:
    s = pixels(new/'masks'/f'{name}_side_surface.png')
    d = pixels(new/'masks'/f'{name}_side_soil.png')
    assert all(x+y == 255 for x,y in zip(s,d))
    assert max(s[16*64:]) == 0
    report['mask_partition_checks'] += 4096
soil = pixels(new/'dirt_side.png')
for name in ['grass', 'podzol']:
    a = pixels(new/f'{name}_side.png')
    assert a[16*64:61*64] == soil[16*64:61*64]
report['common_lower_soil'] = 'pass'
print(json.dumps(report, indent=2))
