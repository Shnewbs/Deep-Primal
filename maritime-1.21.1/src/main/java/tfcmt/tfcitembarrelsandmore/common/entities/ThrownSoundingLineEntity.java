package tfcmt.tfcitembarrelsandmore.common.entities;

import net.dries007.tfc.common.TFCTags;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import tfcmt.tfcitembarrelsandmore.common.TIBAMEntities;
import tfcmt.tfcitembarrelsandmore.common.TIBAMItems;

public class ThrownSoundingLineEntity
extends ThrowableItemProjectile {
    public ThrownSoundingLineEntity(EntityType<? extends ThrowableItemProjectile> type, Level level) {
        super(type, level);
    }

    public ThrownSoundingLineEntity(Level level, Player thrower) {
        super((EntityType)TIBAMEntities.SOUNDING_LINE.get(), (LivingEntity)thrower, level);
    }

    protected Item getDefaultItem() {
        return (Item)TIBAMItems.SOUNDING_LINE.get();
    }

    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!this.level().isClientSide) {
            this.reportDepth(result.getBlockPos());
            this.discard();
        }
    }

    private void reportDepth(BlockPos hitPos) {
        BlockPos.MutableBlockPos cursor;
        Level level = this.level();
        if (!level.getFluidState((BlockPos)(cursor = hitPos.above().mutable())).is(net.minecraft.tags.FluidTags.WATER)) {
            this.sendMessage((Component)Component.translatable((String)"message.tfc_item_barrels_and_more.sounding_line.no_water").withStyle(ChatFormatting.GRAY));
            return;
        }
        int depth = 0;
        int maxY = level.getMaxBuildHeight();
        while (cursor.getY() <= maxY && level.getFluidState((BlockPos)cursor).is(net.minecraft.tags.FluidTags.WATER)) {
            ++depth;
            cursor.move(0, 1, 0);
        }
        this.sendMessage((Component)Component.translatable((String)"message.tfc_item_barrels_and_more.sounding_line.reading", (Object[])new Object[]{depth}).withStyle(ChatFormatting.AQUA));
    }

    private void sendMessage(Component message) {
        Entity entity = this.getOwner();
        if (entity instanceof Player) {
            Player player = (Player)entity;
            player.displayClientMessage(message, true);
        }
    }
}

