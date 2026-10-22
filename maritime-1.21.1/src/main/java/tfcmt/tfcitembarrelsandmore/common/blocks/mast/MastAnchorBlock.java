package tfcmt.tfcitembarrelsandmore.common.blocks.mast;

import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;
import tfcmt.tfcitembarrelsandmore.client.renderer.mast.MastClientBlockExtensions;
import tfcmt.tfcitembarrelsandmore.common.TIBAMBlocks;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.MastBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blocks.mast.BoomGrowth;
import tfcmt.tfcitembarrelsandmore.common.blocks.mast.MastGrowth;
import tfcmt.tfcitembarrelsandmore.common.blocks.mast.MastRake;
import tfcmt.tfcitembarrelsandmore.common.blocks.mast.YardGrowth;
import tfcmt.tfcitembarrelsandmore.common.items.BoomItem;
import tfcmt.tfcitembarrelsandmore.common.items.YardItem;
import tfcmt.tfcitembarrelsandmore.compat.valkyrienskies.VSCompat;

public class MastAnchorBlock
extends Block
implements EntityBlock {
    private final TIBAMBlocks.Wood wood;

    public MastAnchorBlock(BlockBehaviour.Properties properties, TIBAMBlocks.Wood wood) {
        super(properties);
        this.wood = wood;
    }

    public TIBAMBlocks.Wood getWood() {
        return this.wood;
    }

    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide) {
            return;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof MastBlockEntity) {
            MastBlockEntity mastBE = (MastBlockEntity)blockEntity;
            mastBE.initTier(this.wood);
        }
    }

    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        int chunk;
        super.playerWillDestroy(level, pos, state, player);
        if (level.isClientSide || player.getAbilities().instabuild) {
            return;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof MastBlockEntity)) {
            return;
        }
        MastBlockEntity mastBE = (MastBlockEntity)blockEntity;
        int extra = mastBE.getTotalHeight() - 1;
        if (extra <= 0) {
            return;
        }
        Item item = this.asItem();
        int maxStack = item.getDefaultInstance().getMaxStackSize();
        Vec3 dropPos = VSCompat.spawnPosToWorld(level, pos, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5);
        for (int remaining = extra; remaining > 0; remaining -= chunk) {
            chunk = Math.min(remaining, maxStack);
            ItemEntity entity = new ItemEntity(level, dropPos.x, dropPos.y, dropPos.z, new ItemStack((ItemLike)item, chunk));
            entity.setDefaultPickUpDelay();
            level.addFreshEntity((Entity)entity);
        }
    }

    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        if (held.getItem() instanceof YardItem) {
            return YardGrowth.tryGrow(level, pos, player, held, hit);
        }
        if (held.getItem() instanceof BoomItem) {
            return BoomGrowth.tryGrow(level, pos, player, held, hit);
        }
        if (Helpers.isItem((ItemStack)held, (TagKey)TFCTags.Items.HAMMERS)) {
            return MastRake.tryRake(level, pos, player, held, hit);
        }
        return MastGrowth.tryGrow(level, pos, player, held);
    }

    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int n;
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof MastBlockEntity) {
            MastBlockEntity mastBE = (MastBlockEntity)blockEntity;
            n = mastBE.getWidthPx();
        } else {
            n = 4;
        }
        int widthPx = n;
        return MastAnchorBlock.postShape(widthPx);
    }

    public void initializeClient(Consumer<IClientBlockExtensions> consumer) {
        consumer.accept(MastClientBlockExtensions.INSTANCE);
    }

    static VoxelShape postShape(int widthPx) {
        double half = (double)widthPx / 2.0;
        return Block.box((double)(8.0 - half), (double)0.0, (double)(8.0 - half), (double)(8.0 + half), (double)16.0, (double)(8.0 + half));
    }

    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MastBlockEntity(pos, state);
    }

    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }
}

