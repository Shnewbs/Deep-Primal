"""Generate Deep Primal's owned biome assets, habitat tags and active engine palettes.

The JSON manifest is the authored input. --check verifies checked-in output without writing.
Only the four built-in engine profiles are changed; historical native-noise presets and saved
world palettes remain readable. Native environment attribute names are Minecraft API fields.
"""
import argparse
import json
import pathlib
import re

ROOT = pathlib.Path(__file__).resolve().parents[1]
RESOURCES = ROOT / 'src/main/resources'
MANIFEST = ROOT / 'tools/biomes/owned_palette.json'
ENGINE_PROFILES = ('engine_standard', 'engine_deep', 'engine_deltas_standard', 'engine_deltas_deep')


def encoded(value):
    return json.dumps(value, indent=2, ensure_ascii=False) + '\n'


def outputs():
    manifest = json.loads(MANIFEST.read_text(encoding='utf-8'))
    entries = manifest['entries']
    assert manifest['schema'] == 1 and manifest['palette_revision'] == 1
    assert [e['slot'] for e in entries] == list(range(40)), 'Palette slot order changed'
    ids = [e['id'] for e in entries]
    assert len(set(ids)) == 40, 'Duplicate owned biome ID'
    planned = set(re.findall(r'deepprimal:[a-z0-9_]+', (ROOT / 'docs/BIOME-CATALOG.md').read_text(encoding='utf-8')))
    result = {}
    tags = {}
    language_path = RESOURCES / 'assets/deepprimal/lang/en_us.json'
    language = json.loads(language_path.read_text(encoding='utf-8'))
    for entry in entries:
        biome_id = entry['id']
        assert re.fullmatch(r'deepprimal:[a-z0-9_]+(?:/[a-z0-9_]+)?', biome_id), biome_id
        assert entry['profile'] in planned, 'Profile absent from biome catalog: ' + entry['profile']
        assert biome_id.split('/')[0] == entry['profile']
        assert -2 <= entry['temperature'] <= 2 and 0 <= entry['downfall'] <= 1
        name = biome_id.removeprefix('deepprimal:')
        water = entry['water_palette']
        assert .1 <= water['clarity'] <= 8
        assert all(0 <= water[k] <= 0xffffff for k in ('color', 'fog_color'))
        biome = {
            'has_precipitation': entry['has_precipitation'],
            'temperature': entry['temperature'],
            'downfall': entry['downfall'],
            'attributes': {
                'minecraft:visual/sky_color': entry['sky_color'],
                'minecraft:visual/water_fog_color': f"#{water['fog_color']:06x}",
            },
            'effects': {
                'water_color': f"#{water['color']:06x}",
                'grass_color': entry['grass_color'],
                'foliage_color': entry['foliage_color'],
            },
            # The authored engine owns caves, deposits and vegetation. Do not inherit native
            # carvers, feature lists or mob spawn tables merely to register a biome identity.
            'carvers': [],
            'features': [[] for _ in range(11)],
            'spawners': {},
            'spawn_costs': {},
        }
        result[RESOURCES / f'data/deepprimal/worldgen/biome/{name}.json'] = encoded(biome)
        result[RESOURCES / f'assets/deepprimal/deepprimal/water_palette/{name}.json'] = encoded(water)
        language['biome.deepprimal.' + name.replace('/', '.')] = entry['display_name']
        for tag in entry['tags']:
            assert re.fullmatch(r'deepprimal:[a-z0-9_]+', tag), tag
            tags.setdefault(tag, []).append(biome_id)
    assert len(tags) == 14
    for tag, members in tags.items():
        assert set(members) <= set(ids)
        name = tag.removeprefix('deepprimal:')
        result[RESOURCES / f'data/deepprimal/tags/worldgen/biome/{name}.json'] = encoded({'replace': False, 'values': members})
    result[language_path] = encoded(language)
    legacy_ids = [e['legacy_id'] for e in entries]
    for profile in ENGINE_PROFILES:
        path = RESOURCES / f'worldgen/{profile}/data/minecraft/worldgen/world_preset/normal.json'
        preset = json.loads(path.read_text(encoding='utf-8'))
        source = preset['dimensions']['minecraft:overworld']['generator']['biome_source']
        assert source['type'] == 'deepprimal:regional_v1'
        assert source['palette'] in (legacy_ids, ids), 'Unexpected biome palette in ' + profile
        source['palette'] = ids
        result[path] = encoded(preset)
    return result


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--check', action='store_true')
    args = parser.parse_args()
    generated = outputs()
    stale = [str(p.relative_to(ROOT)) for p, content in generated.items()
             if not p.exists() or p.read_text(encoding='utf-8') != content]
    if args.check:
        if stale:
            raise SystemExit('Owned biome output is missing or stale:\n' + '\n'.join(stale))
    else:
        for path, content in generated.items():
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(content, encoding='utf-8')
    print(f'OWNED_BIOMES_{"CHECK" if args.check else "GENERATED"}_PASS '
          f'biomes=40 tags=14 palettes=4 files={len(generated)}')


if __name__ == '__main__':
    main()
