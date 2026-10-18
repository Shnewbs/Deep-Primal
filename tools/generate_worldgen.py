"""Generate the authored prototype worldgen resources. Python 3, no dependencies."""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources'

def write(path, value):
    target = ROOT / path
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text(json.dumps(value, indent=2) + '\n')

def op(kind, a, b):
    return {'type': 'minecraft:' + kind, 'argument1': a, 'argument2': b}

def unary(kind, a):
    return {'type': 'minecraft:' + kind, 'argument': a}

def noise(name, xz=1.0, y=0.0):
    return {'type': 'minecraft:noise', 'noise': name, 'xz_scale': xz, 'y_scale': y}

def climate_noise(name):
    return {'type': 'minecraft:shifted_noise', 'noise': 'minecraft:' + name,
            'shift_x': 'minecraft:shift_x', 'shift_y': 0.0, 'shift_z': 'minecraft:shift_z',
            'xz_scale': 0.25, 'y_scale': 0.0}

def clamp(value, low, high):
    return {'type': 'minecraft:clamp', 'input': value, 'min': low, 'max': high}

def condition(test, result):
    return {'type': 'minecraft:condition', 'if_true': test, 'then_run': result}

def block(name):
    return {'type': 'minecraft:block', 'result_state': {'Name': 'minecraft:' + name}}

def above(y):
    return {'type': 'minecraft:y_above', 'anchor': {'absolute': y}, 'surface_depth_multiplier': 0, 'add_stone_depth': False}

def depth(offset=0, add=False):
    return {'type': 'minecraft:stone_depth', 'offset': offset, 'add_surface_depth': add, 'secondary_depth_range': 0, 'surface_type': 'floor'}

def sequence(*rules):
    return {'type': 'minecraft:sequence', 'sequence': list(rules)}

# Use vanilla's seeded climate fields so the multi-noise biome source remains useful.
continents = 'minecraft:overworld/continents'
erosion = 'minecraft:overworld/erosion'
ridges = 'minecraft:overworld/ridges'
land = clamp(op('mul', op('add', continents, 0.12), 3.0), 0.0, 1.0)
mountain = op('mul', 360.0, op('mul', land, op('mul',
    unary('square', unary('abs', ridges)),
    clamp(op('add', 0.8, op('mul', -1.0, erosion)), 0.0, 1.0))))
valley = op('mul', 34.0, op('mul', land,
    clamp(op('add', 1.0, op('mul', -14.0, unary('abs', ridges))), 0.0, 1.0)))
detail = op('mul', 5.0, noise('deepprimal:surface_detail'))
height = op('add', op('add', op('add', 112.0, op('mul', 220.0, continents)),
            op('add', mountain, op('mul', -1.0, valley))), detail)
write('data/deepprimal/worldgen/noise/surface_detail.json', {'firstOctave': -5, 'amplitudes': [1.0, 0.5, 0.25]})
# The native density grid uses four-block X/Z cells. Precompute its bounded 5x5
# height grid once per chunk instead of retaining only the last sampled column.
write('data/deepprimal/worldgen/density_function/terrain_height.json', unary('flat_cache', height))

def profile(name, min_y, height):
    top = min_y + height
    terrain = op('add', op('mul', 1 / 80, 'deepprimal:terrain_height'), {
        'type': 'minecraft:y_clamped_gradient', 'from_y': min_y, 'to_y': top,
        'from_value': -min_y / 80, 'to_value': -top / 80})
    # Tunnels use a cheap 3D noise band; near-surface protection keeps most roofs intact.
    caves = op('mul', 3.0, op('add', unary('abs', noise('minecraft:cave_cheese', 0.7, 0.7)), -0.045))
    final = {'type': 'minecraft:range_choice', 'input': terrain,
             'min_inclusive': 0.14, 'max_exclusive': 10000.0,
             'when_in_range': op('min', terrain, caves), 'when_out_of_range': terrain}
    # Seal the bottom and keep the upper boundary empty, independently of terrain noise.
    final = op('max', final, {'type': 'minecraft:y_clamped_gradient', 'from_y': min_y, 'to_y': min_y + 8, 'from_value': 1.0, 'to_value': -1000.0})
    final = op('min', final, {'type': 'minecraft:y_clamped_gradient', 'from_y': top - 16, 'to_y': top, 'from_value': 1000.0, 'to_value': -1.0})
    router = {
        'barrier': noise('minecraft:aquifer_barrier', 1.0, 0.5),
        'fluid_level_floodedness': noise('minecraft:aquifer_fluid_level_floodedness', 1.0, 0.67),
        'fluid_level_spread': noise('minecraft:aquifer_fluid_level_spread', 1.0, 0.7142857142857143),
        'lava': noise('minecraft:aquifer_lava', 1.0, 1.0),
        'temperature': climate_noise('temperature'),
        'vegetation': climate_noise('vegetation'),
        'continents': continents, 'erosion': erosion, 'ridges': ridges,
        'depth': terrain, 'initial_density_without_jaggedness': terrain,
        'final_density': unary('interpolated', final),
        'preliminary_surface_level': {'type': 'deepprimal:column_surface', 'height': 'deepprimal:terrain_height',
            'cell_height': 8, 'lower_bound': min_y, 'upper_bound': top - 8},
        'vein_toggle': 0.0, 'vein_ridged': 0.0, 'vein_gap': 0.0}
    arid = {'type': 'minecraft:biome', 'biome_is': ['minecraft:desert', 'minecraft:badlands',
            'minecraft:eroded_badlands', 'minecraft:wooded_badlands']}
    rocky = sequence(
        condition({'type': 'minecraft:noise_threshold', 'noise': 'minecraft:surface',
            'min_threshold': 0.12, 'max_threshold': 1000.0}, block('stone')),
        block('andesite'))
    surfaces = sequence(
        condition({'type': 'minecraft:vertical_gradient', 'random_name': 'deepprimal:bedrock',
            'true_at_and_below': {'above_bottom': 0}, 'false_at_and_above': {'above_bottom': 5}}, block('bedrock')),
        condition({'type': 'minecraft:above_preliminary_surface'}, sequence(
            condition(depth(), sequence(
                condition({'type': 'minecraft:not', 'invert': above(99)}, block('sand')),
                condition({'type': 'minecraft:steep'}, rocky),
                condition(above(360), block('snow_block')),
                condition(arid, block('sand')),
                block('grass_block'))),
            condition(depth(0, True), sequence(condition(above(224), block('andesite')),
                condition(arid, block('sand')), block('dirt'))))),
        condition(above(192), block('andesite')),
        condition(above(16), block('granite')),
        condition(above(-64), block('diorite')),
        block('deepslate'))
    write(f'data/deepprimal/worldgen/noise_settings/{name}.json', {
        'aquifers_enabled': True, 'default_block': {'Name': 'minecraft:stone'},
        'default_fluid': {'Name': 'minecraft:water', 'Properties': {'level': '0'}},
        'disable_mob_generation': False, 'legacy_random_source': False,
        'noise': {'min_y': min_y, 'height': height, 'size_horizontal': 1, 'size_vertical': 2},
        'noise_router': router, 'ore_veins_enabled': False, 'sea_level': 96,
        'spawn_target': [{'temperature': [-1.0, 1.0], 'humidity': [-1.0, 1.0],
            'continentalness': [0.1, 1.0], 'erosion': [-1.0, 1.0], 'depth': 0.0,
            'weirdness': [-1.0, 1.0], 'offset': 0.0}], 'surface_rule': surfaces})
    write(f'data/deepprimal/dimension_type/{name}.json', {
        'ambient_light': 0.0, 'attributes': {
            'minecraft:visual/cloud_height': 420.0,
            'minecraft:visual/sky_color': '#78a7ff',
            'minecraft:visual/fog_color': '#c0d8ff',
            'minecraft:visual/ambient_light_color': '#0a0a0a',
            'minecraft:visual/cloud_color': '#ccffffff',
            'minecraft:gameplay/bed_rule': {'can_set_spawn': 'always', 'can_sleep': 'when_dark',
                'error_message': {'translate': 'block.minecraft.bed.no_sleep'}},
            'minecraft:gameplay/respawn_anchor_works': False},
        'coordinate_scale': 1.0, 'default_clock': 'minecraft:overworld', 'has_ceiling': False,
        'has_ender_dragon_fight': False, 'has_skylight': True, 'height': height,
        'infiniburn': '#minecraft:infiniburn_overworld', 'logical_height': height, 'min_y': min_y,
        'monster_spawn_block_light_limit': 0,
        'monster_spawn_light_level': {'type': 'minecraft:uniform', 'min_inclusive': 0, 'max_inclusive': 7},
        'timelines': '#minecraft:in_overworld'})
    write(f'data/deepprimal/worldgen/world_preset/{name}.json', {'dimensions': {
        'minecraft:overworld': {'type': 'deepprimal:' + name, 'generator': {
            'type': 'minecraft:noise', 'biome_source': {'type': 'minecraft:multi_noise', 'preset': 'minecraft:overworld'},
            'settings': 'deepprimal:' + name}},
        'minecraft:the_nether': {'type': 'minecraft:the_nether', 'generator': {
            'type': 'minecraft:noise', 'biome_source': {'type': 'minecraft:multi_noise', 'preset': 'minecraft:nether'}, 'settings': 'minecraft:nether'}},
        'minecraft:the_end': {'type': 'minecraft:the_end', 'generator': {
            'type': 'minecraft:noise', 'biome_source': {'type': 'minecraft:the_end'}, 'settings': 'minecraft:end'}}}})

profile('prototype', -128, 768)
profile('prototype_deep', -384, 1024)
write('data/minecraft/tags/worldgen/world_preset/normal.json', {'replace': False, 'values': ['deepprimal:prototype', 'deepprimal:prototype_deep']})
print('Generated two presets: -128..639 and -384..639, sea level 96.')
