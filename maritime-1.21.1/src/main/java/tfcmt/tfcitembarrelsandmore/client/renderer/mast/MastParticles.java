package tfcmt.tfcitembarrelsandmore.client.renderer.mast;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import tfcmt.tfcitembarrelsandmore.client.renderer.mast.MastTerrainParticle;
import tfcmt.tfcitembarrelsandmore.common.TIBAMBlocks;

public final class MastParticles {
    private MastParticles() {
    }

    private static TextureAtlasSprite sprite(TIBAMBlocks.Wood wood) {
        ResourceLocation texLoc = ResourceLocation.fromNamespaceAndPath(wood.getMod(), "block/wood/stripped_log/" + wood.getId());
        return (TextureAtlasSprite)Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(texLoc);
    }

    public static boolean destroy(Level level, BlockPos pos, ParticleEngine manager, TIBAMBlocks.Wood wood) {
        if (!(level instanceof ClientLevel)) {
            return false;
        }
        ClientLevel clientLevel = (ClientLevel)level;
        TextureAtlasSprite spr = MastParticles.sprite(wood);
        int grid = 4;
        for (int xi = 0; xi < grid; ++xi) {
            for (int yi = 0; yi < grid; ++yi) {
                for (int zi = 0; zi < grid; ++zi) {
                    double x = (double)pos.getX() + ((double)xi + 0.5) / (double)grid;
                    double y = (double)pos.getY() + ((double)yi + 0.5) / (double)grid;
                    double z = (double)pos.getZ() + ((double)zi + 0.5) / (double)grid;
                    MastTerrainParticle particle = new MastTerrainParticle(clientLevel, x, y, z, x - (double)pos.getX() - 0.5, y - (double)pos.getY() - 0.5, z - (double)pos.getZ() - 0.5, clientLevel.getBlockState(pos), spr);
                    manager.add((Particle)particle);
                }
            }
        }
        return true;
    }

    public static boolean hit(Level level, BlockHitResult hit, ParticleEngine manager, TIBAMBlocks.Wood wood) {
        if (!(level instanceof ClientLevel)) {
            return false;
        }
        ClientLevel clientLevel = (ClientLevel)level;
        BlockPos pos = hit.getBlockPos();
        TextureAtlasSprite spr = MastParticles.sprite(wood);
        MastTerrainParticle particle = new MastTerrainParticle(clientLevel, hit.getLocation().x, hit.getLocation().y, hit.getLocation().z, 0.0, 0.0, 0.0, clientLevel.getBlockState(pos), spr);
        manager.add((Particle)particle);
        return true;
    }
}

