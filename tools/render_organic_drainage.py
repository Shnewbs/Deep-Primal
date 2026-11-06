"""Render exact OrganicDrainageCapture CSVs; this is a terrain diagnostic, not a game screenshot."""
import argparse
from pathlib import Path
import numpy as np
from PIL import Image, ImageDraw, ImageFont


def render(captures: Path, output: Path):
    size = 640
    canvas = Image.new('RGB', (size * 2 + 72, (size + 76) * 2 + 112), '#101c24')
    draw = ImageDraw.Draw(canvas)
    font_path = Path('C:/Windows/Fonts/arial.ttf')
    font = ImageFont.truetype(str(font_path), 21) if font_path.exists() else ImageFont.load_default()
    small = ImageFont.truetype(str(font_path), 16) if font_path.exists() else ImageFont.load_default()
    draw.text((24, 16), 'Measured generated terrain: matched seed and coordinates', font=font, fill='#e9f1f6')
    draw.text((24, 48), 'Revision 9 versus terrain-derived revision 10 | 1,536 × 1,536 blocks per panel', font=small, fill='#bacbd6')
    rows = []
    for seed in (0, 20261020):
        row = []
        for revision in (9, 10):
            data = np.genfromtxt(captures / f'{seed}-r{revision}-surface.csv', delimiter=',', names=True)
            side = int(np.sqrt(len(data)))
            assert side * side == len(data)
            row.append((revision, data, side))
        rows.append((seed, row))
    all_bed = np.concatenate([d['bed'] for _, row in rows for _, d, _ in row])
    lo, hi = np.percentile(all_bed, [2, 98])
    for iy, (seed, row) in enumerate(rows):
        for ix, (revision, data, side) in enumerate(row):
            bed = data['bed'].reshape(side, side)
            water = data['water'].reshape(side, side)
            speed = data['speed'].reshape(side, side)
            elevation = np.clip((bed - lo) / (hi - lo), 0, 1)
            rgb = np.stack([71 + elevation * 95, 103 + elevation * 72, 66 + elevation * 70], axis=-1)
            dz, dx = np.gradient(bed, 4)
            lighting = np.clip((1 - .6 * dx - .4 * dz) / np.sqrt(1 + dx * dx + dz * dz), .3, 1.35)
            rgb *= lighting[..., None]
            wet = np.isfinite(water)
            depth = np.clip((water - bed) / 24, 0, 1)
            rgb[wet] = np.stack([61 - depth[wet] * 31, 158 - depth[wet] * 64, 180 - depth[wet] * 51], axis=-1)
            flowing = wet & (speed > 0)
            rgb[flowing] *= .85
            picture = Image.fromarray(np.uint8(np.clip(rgb, 0, 255))).resize((size, size), Image.Resampling.NEAREST)
            x, y = 24 + ix * (size + 24), 92 + iy * (size + 76)
            canvas.paste(picture, (x, y))
            draw.text((x, y + size + 8), f'Seed {seed} | revision {revision}', font=font, fill='#e9f1f6')
            draw.text((x, y + size + 36), f'{int(wet.sum()):,} wet samples | identical view bounds', font=small, fill='#bacbd6')
    draw.text((24, canvas.height - 25), 'Core surface data at 4-block spacing. No vegetation, native voxel mesh or lighting acceptance implied.', font=small, fill='#bacbd6')
    output.parent.mkdir(parents=True, exist_ok=True)
    canvas.save(output)
    print(output)


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('captures', type=Path)
    parser.add_argument('output', type=Path)
    args = parser.parse_args()
    render(args.captures, args.output)
