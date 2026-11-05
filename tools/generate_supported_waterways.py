"""Clone frozen v5 resources into v6 with bank support and stable channel water."""
from pathlib import Path
import json
root = Path(__file__).resolve().parents[1] / 'src/main/resources'
def update(value):
    if isinstance(value, dict):
        if value.get('type') == 'deepprimal:waterway_height':
            value['supported_steps'] = True
        for child in value.values(): update(child)
    elif isinstance(value, list):
        for child in value: update(child)
def clone(source, target):
    text = source.read_text().replace('waterways_v5', 'waterways_v6')
    if source.suffix == '.json':
        data = json.loads(text); update(data); text = json.dumps(data, indent=2) + '\n'
    target.parent.mkdir(parents=True, exist_ok=True); target.write_text(text)
worldgen = root/'data/deepprimal/worldgen'
for directory in ('density_function', 'noise_settings', 'world_preset'):
    for source in (worldgen/directory).rglob('*'):
        if source.is_file() and 'waterways_v5' in source.as_posix():
            clone(source, Path(source.as_posix().replace('waterways_v5', 'waterways_v6')))
for variant in ('standard', 'deep', 'rivers_standard', 'rivers_deep'):
    source = root/'worldgen'/('natural_' + variant)
    for path in source.rglob('*'):
        if path.is_file(): clone(path, root/'worldgen'/('supported_' + variant)/path.relative_to(source))
