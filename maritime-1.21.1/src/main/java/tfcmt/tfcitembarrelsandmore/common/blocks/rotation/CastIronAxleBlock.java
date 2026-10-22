package tfcmt.tfcitembarrelsandmore.common.blocks.rotation;

import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.rotation.AxleBlock;
import net.dries007.tfc.common.items.TFCItems;
import net.dries007.tfc.util.Metal;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredHolder;
import tfcmt.tfcitembarrelsandmore.compat.valkyrienskies.VSCompat;

public class CastIronAxleBlock
extends AxleBlock {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/cast_iron_axle");

    public CastIronAxleBlock(ExtendedProperties properties) {
        super(properties, null, TEXTURE);
    }

    public ResourceLocation getAxleTextureLocation() {
        return TEXTURE;
    }

    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        return List.of(new ItemStack((ItemLike)TFCItems.METAL_ITEMS.get(Metal.CAST_IRON).get(Metal.ItemType.ROD).get()));
    }

    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity be, ItemStack tool) {
        if (!level.isClientSide) {
            ItemStack rod = new ItemStack((ItemLike)TFCItems.METAL_ITEMS.get(Metal.CAST_IRON).get(Metal.ItemType.ROD).get());
            Vec3 dropPos = VSCompat.spawnPosToWorld(level, pos, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5);
            ItemEntity entity = new ItemEntity(level, dropPos.x, dropPos.y, dropPos.z, rod);
            entity.setDefaultPickUpDelay();
            level.addFreshEntity((Entity)entity);
        }
    }
}

