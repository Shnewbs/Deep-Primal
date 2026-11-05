from pathlib import Path
R=Path(__file__).resolve().parent;P=R/'project'
p=P/'src/main/java/com/shnewbs/deepprimal/client/render/AnimatedWaterRenderer.java';s=p.read_text();(R/'pre-atlas-AnimatedWaterRenderer.java').write_text(s);needle='      p.texture.m13(still.getU(0)).m22(still.getU(1) - still.getU(0));';assert needle in s;s=s.replace(needle,needle+'''
      // Keep nonperiodic side/underside endpoints inside the atlas, including exact V=1.
      int atlasHeight = Minecraft.getInstance().getAtlasManager()
          .getAtlasOrThrow(net.minecraft.data.AtlasIds.BLOCKS).getTexture().getHeight(0);
      p.texture.m23(.5f / atlasHeight);''');p.write_text(s)
p=P/'graphics-settings/src/main/resources/assets/deepprimal_graphics/shaders/core/animated_water.vsh';s=p.read_text();(R/'pre-atlas-animated_water.vsh').write_text(s);needle='            previousTexCoord = oldTile.xy + localUV * oldTile.zw * oldScale;';assert needle in s;s=s.replace(needle,needle+'''
            float oldPad = min(TextureMat[2][3], oldTile.w * oldScale * 0.5);
            previousTexCoord.y = clamp(previousTexCoord.y, oldTile.y + oldPad,
                                      oldTile.y + oldTile.w * oldScale - oldPad);''');s=s.replace('texCoord.y = clamp(UV0.y - displacement * span, start, start + span);','''float pad = min(TextureMat[2][3], span * 0.5);
        texCoord.y = clamp(UV0.y - displacement * span, start + pad, start + span - pad);''');p.write_text(s)
print('half-texel V guard uses spare matrix field; no new varying/vertex/uniform allocation')
