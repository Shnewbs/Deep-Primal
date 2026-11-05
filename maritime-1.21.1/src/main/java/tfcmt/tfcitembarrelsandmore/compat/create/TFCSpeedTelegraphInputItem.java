package tfcmt.tfcitembarrelsandmore.compat.create;

import net.dries007.tfc.common.component.size.Size;
import net.dries007.tfc.common.component.size.Weight;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import tfcmt.tfcitembarrelsandmore.common.items.SizedBlockItem;
import tfcmt.tfcitembarrelsandmore.compat.create.TFCSpeedTelegraphOutputBlock;
import tfcmt.tfcitembarrelsandmore.compat.create.TFCSpeedTelegraphOutputBlockEntity;

public class TFCSpeedTelegraphInputItem
extends SizedBlockItem {
    public TFCSpeedTelegraphInputItem(Block block, Item.Properties properties, Size size, Weight weight) {
        super(block, properties, size, weight);
    }

    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Player player = ctx.getPlayer();
        ItemStack held = ctx.getItemInHand();
        if (!(state.getBlock() instanceof TFCSpeedTelegraphOutputBlock)) {
            return super.useOn(ctx);
        }
        if (ctx.getClickedFace() != state.getValue((Property)TFCSpeedTelegraphOutputBlock.FACING)) {
            return super.useOn(ctx);
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TFCSpeedTelegraphOutputBlockEntity) {
            TFCSpeedTelegraphOutputBlockEntity outputBE = (TFCSpeedTelegraphOutputBlockEntity)blockEntity;
            return outputBE.onPlayerInteract(state, level, pos, player, ctx.getHand(), new BlockHitResult(ctx.getClickLocation(), ctx.getClickedFace(), pos, false));
        }
        return InteractionResult.PASS;
    }
}

