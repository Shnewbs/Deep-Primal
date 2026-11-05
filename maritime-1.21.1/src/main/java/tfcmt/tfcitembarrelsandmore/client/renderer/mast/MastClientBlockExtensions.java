package tfcmt.tfcitembarrelsandmore.client.renderer.mast;

import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;
import tfcmt.tfcitembarrelsandmore.client.renderer.mast.MastParticles;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.MastCell;

public enum MastClientBlockExtensions implements IClientBlockExtensions
{
    INSTANCE;


    public boolean addDestroyEffects(BlockState state, Level level, BlockPos pos, ParticleEngine manager) {
        MastCell cell;
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof MastCell && MastParticles.destroy(level, pos, manager, (cell = (MastCell)blockEntity).getWood());
    }

    public boolean addHitEffects(BlockState state, Level level, HitResult target, ParticleEngine manager) {
        MastCell cell;
        BlockHitResult hit;
        BlockEntity blockEntity;
        return target instanceof BlockHitResult && (blockEntity = level.getBlockEntity((hit = (BlockHitResult)target).getBlockPos())) instanceof MastCell && MastParticles.hit(level, hit, manager, (cell = (MastCell)blockEntity).getWood());
    }
}

