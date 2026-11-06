"""Render the measured NaturalShoreChecks CSV as a bed-depth comparison (Pillow + NumPy).

This is a core geometry plot, not a game screenshot or a visual acceptance result.
"""
import argparse
import csv
import pathlib
import numpy as np
from PIL import Image, ImageDraw, ImageFont


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('csv', type=pathlib.Path)
    parser.add_argument('output', type=pathlib.Path)
    args = parser.parse_args()
    with args.csv.open(encoding='utf-8', newline='') as stream:
        rows = list(csv.DictReader(stream))
    xs = sorted({int(r['x']) for r in rows})
    zs = sorted({int(r['z']) for r in rows})
    before = np.array([float(r['before']) for r in rows]).reshape(len(zs), len(xs))
    after = np.array([float(r['after']) for r in rows]).reshape(len(zs), len(xs))
    before_stage = np.array([float(r['before_water']) for r in rows]).reshape(len(zs), len(xs))
    after_stage = np.array([float(r['after_water']) for r in rows]).reshape(len(zs), len(xs))
    limit = max(float(np.nanmax(before_stage-before)),float(np.nanmax(after_stage-after)))
    font_path = pathlib.Path('C:/Windows/Fonts/segoeui.ttf')
    def font(size):
        return ImageFont.truetype(str(font_path),size) if font_path.exists() else ImageFont.load_default()
    image = Image.new('RGB',(1220,730),'#111d24')
    draw = ImageDraw.Draw(image)
    draw.text((28,18),'Measured lakebed: uniform bowl vs natural shelves and pools',font=font(25),fill='#edf4f6')
    draw.text((28,55),'Same basin, seed and lake level. Irregular margins retreat inside the old wet footprint.',font=font(16),fill='#bdced6')
    for index, (bed,stage,title) in enumerate([(before,before_stage,'Before — channel revision 8'),(after,after_stage,'After — channel revision 9')]):
        wet = np.isfinite(stage)
        depth = np.where(wet,np.maximum(0,stage-bed),0)
        t = np.minimum(1,depth/limit)
        shallow = np.array([170,219,207]); deep = np.array([23,67,104])
        colors = shallow[None,None,:]*(1-t[:,:,None]) + deep[None,None,:]*t[:,:,None]
        # Identical depth bands on both panels make contour regularity visible.
        bands = np.floor(depth/2)
        contour = np.zeros(wet.shape,dtype=bool)
        contour[1:,:] |= (bands[1:,:] != bands[:-1,:]) & wet[1:,:] & wet[:-1,:]
        contour[:,1:] |= (bands[:,1:] != bands[:,:-1]) & wet[:,1:] & wet[:,:-1]
        colors[contour] *= .68
        colors[~wet] = [89,112,76]
        panel = Image.fromarray(colors.clip(0,255).astype('uint8')).resize((560,560),Image.Resampling.NEAREST)
        x = 28 + index*604
        draw.text((x,91),title,font=font(20),fill='#e5eff2')
        image.paste(panel,(x,123))
        draw.rectangle((x-1,122,x+560,683),outline='#6d8993')
    draw.text((28,697),'Core geometry fixture; 2-block depth contours. Not an in-game render or completed ecosystem.',font=font(15),fill='#bdced6')
    args.output.parent.mkdir(parents=True,exist_ok=True)
    image.save(args.output)
    print(f'SHORE_COMPARISON_RENDERED {args.output} maxDepth={limit:.2f} blocks')


if __name__ == '__main__':
    main()
