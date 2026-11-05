"""Read-only independent checks of the restored PNGs and approved crop provenance."""
import hashlib, json, subprocess, sys
from pathlib import Path
from PIL import Image
repo, sources, repeat = map(Path, sys.argv[1:4])
manifest = json.loads((repo/'docs/art/ground-textures-v4/validation.json').read_text())
root = Path('src/main/resources/assets/deepprimal/textures/block/ground')
assets = {}
for entry in manifest['assets']:
    path = Path(entry['file']); data = (repo/path).read_bytes()
    assert hashlib.sha256(data).hexdigest() == entry['sha256']
    assert data == (repeat/path).read_bytes(), path
    im = Image.open(repo/path); im.load()
    assert im.size == (64,64) and im.mode == 'RGB', path
    rgb = list(im.tobytes()); gray = rgb[::3]
    assert gray == rgb[1::3] == rgb[2::3], path
    assert all(gray[y*64] == gray[y*64+63] for y in range(64)), path
    if entry['repeat_axes'] == 'xy': assert gray[:64] == gray[-64:], path
    assets[str(path.relative_to(root))] = gray
for name in ['grass', 'podzol']:
    s = assets[f'masks/{name}_side_surface.png']; d = assets[f'masks/{name}_side_soil.png']
    assert all(x+y==255 for x,y in zip(s,d))
    assert max(s[32*64:]) == 0
    assert assets[name+'_side.png'][52*64:] == assets['dirt_top.png'][52*64:]
    assert assets[name+'_side.png'][-64:] == assets['dirt_side.png'][:64]
assert assets['dirt_side.png'] == assets['dirt_top.png']
assert (repo/root/'dirt_side.png').read_bytes() == (repo/root/'dirt_top.png').read_bytes()
interior = 0
for source in manifest['inputs']:
    p = sources/source['file']; assert hashlib.sha256(p.read_bytes()).hexdigest() == source['sha256']
    x,y,w,h = source['crop']; ow,oh = source['output']
    raw = subprocess.check_output(['convert',str(p),'-crop',f'{w}x{h}+{x}+{y}','+repage','-colorspace','Gray','-filter','Lanczos','-resize',f'{ow}x{oh}!','-depth','8','gray:-'])
    actual = assets[source['file']]
    rows = range(3,61) if source['file'].endswith('_top.png') else range(36)
    for yy in rows:
        for xx in range(3,61):
            assert actual[yy*64+xx] == raw[yy*64+xx], (source['file'],xx,yy)
            interior += 1
print(json.dumps({'runtime_pngs':len(assets),'repeat_export_byte_matches':len(assets),'opaque_rgb_neutral':True,'intended_wrap_edges':'pass','mask_partition_checks':8192,'original_crop_interior_pixels_matched':interior,'source_crops_verified':len(manifest['inputs']),'dirt_top_side_identical':True,'surface_bottom_to_dirt_top_identical':True,'full_build':False,'native_game_execution':False},indent=2))
