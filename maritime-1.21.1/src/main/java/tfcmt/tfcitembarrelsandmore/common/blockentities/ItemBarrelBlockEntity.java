package tfcmt.tfcitembarrelsandmore.common.blockentities;

import net.dries007.tfc.common.blockentities.TickableInventoryBlockEntity;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.capabilities.InventoryItemHandler;
import net.dries007.tfc.common.container.ISlotCallback;
import net.dries007.tfc.util.calendar.Calendars;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.items.IItemHandler;
import tfcmt.tfcitembarrelsandmore.common.BraceData;
import tfcmt.tfcitembarrelsandmore.common.TIBAMCommonEvents;
import tfcmt.tfcitembarrelsandmore.common.blockentities.SaltingManager;
import tfcmt.tfcitembarrelsandmore.common.container.ItemBarrelContainer;
import tfcmt.tfcitembarrelsandmore.common.network.TIBAMNetwork;

public class ItemBarrelBlockEntity
extends TickableInventoryBlockEntity<InventoryItemHandler> {
    public static final int SLOTS = 12;
    private IItemHandler itemHandler = this.inventory;
    private static final String NBT_BRACE_TYPE = "BraceType";
    private static final String NBT_BRACE_VARIANT = "BraceVariant";
    private static final String NBT_BRACE_MOD = "BraceMod";
    private static final String NBT_SALT_CLOCK = "SaltClockAccumulated";
    private String pendingBraceType = null;
    private String pendingBraceVariant = null;
    private String pendingBraceMod = null;
    private boolean needsBraceFlush = false;
    private long saltClockAccumulated = 0L;
    private long saltClockLastCalendarTime = -1L;

    public ItemBarrelBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, self -> new InventoryItemHandler((ISlotCallback)self, 12), "container.tfc_item_barrels_and_more.item_barrel");
    }





    public void onSeal() {
        if (this.level == null || this.level.isClientSide()) {
            return;
        }
        SaltingManager.onSeal((InventoryItemHandler)this.inventory);
        this.markForSync();
    }

    public void onUnseal() {
        if (this.level == null || this.level.isClientSide()) {
            return;
        }
        SaltingManager.onUnseal((InventoryItemHandler)this.inventory);
        this.saltClockLastCalendarTime = -1L;
        this.markForSync();
    }

    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return ItemBarrelContainer.create(this, inventory, containerId);
    }

    public boolean isSealed() {
        return (Boolean)this.getBlockState().getValue((Property)TFCBlockStateProperties.SEALED);
    }

    public void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        BraceData brace;
        super.saveAdditional(tag, registries);
        if (this.level != null && !this.level.isClientSide() && (brace = TIBAMCommonEvents.getBraceData(this.level, this.worldPosition)) != null && brace.isValid()) {
            tag.putString(NBT_BRACE_TYPE, brace.type);
            tag.putString(NBT_BRACE_VARIANT, brace.variant);
            tag.putString(NBT_BRACE_MOD, brace.mod);
        }
        tag.putLong(NBT_SALT_CLOCK, this.saltClockAccumulated);
    }

    public void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains(NBT_BRACE_TYPE)) {
            this.pendingBraceType = tag.getString(NBT_BRACE_TYPE);
            this.pendingBraceVariant = tag.getString(NBT_BRACE_VARIANT);
            this.pendingBraceMod = tag.getString(NBT_BRACE_MOD);
            this.needsBraceFlush = true;
        }
        if (tag.contains(NBT_SALT_CLOCK)) {
            this.saltClockAccumulated = tag.getLong(NBT_SALT_CLOCK);
        }
    }

    public BraceData getEffectiveBraceData() {
        BraceData fromMap;
        if (this.level != null && (fromMap = TIBAMCommonEvents.getBraceData(this.level, this.worldPosition)) != null && fromMap.isValid()) {
            return fromMap;
        }
        if (this.needsBraceFlush && this.pendingBraceType != null && !this.pendingBraceType.isEmpty()) {
            return new BraceData(this.pendingBraceType, this.pendingBraceVariant, this.pendingBraceMod);
        }
        return null;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ItemBarrelBlockEntity barrel) {
        barrel.checkForLastTickSync();
        if (barrel.needsBraceFlush) {
            barrel.needsBraceFlush = false;
            BraceData data = new BraceData(barrel.pendingBraceType, barrel.pendingBraceVariant, barrel.pendingBraceMod);
            TIBAMCommonEvents.putBraceData(level, pos, data);
            TIBAMNetwork.syncBrace(pos, data, (ServerLevel)level);
            barrel.pendingBraceType = null;
            barrel.pendingBraceVariant = null;
            barrel.pendingBraceMod = null;
        }
        if (!((Boolean)state.getValue((Property)TFCBlockStateProperties.SEALED)).booleanValue()) {
            return;
        }
        long currentTime = Calendars.get().getTicks();
        if (barrel.saltClockLastCalendarTime < 0L) {
            barrel.saltClockLastCalendarTime = currentTime;
        } else {
            long delta = currentTime - barrel.saltClockLastCalendarTime;
            if (delta > 0L) {
                barrel.saltClockAccumulated += delta;
                barrel.saltClockLastCalendarTime = currentTime;
            }
        }
        int interval = SaltingManager.getInterval();
        for (int cycles = 0; barrel.saltClockAccumulated >= (long)interval && cycles < 64; ++cycles) {
            barrel.saltClockAccumulated -= (long)interval;
            if (!SaltingManager.runSaltMaintenance((InventoryItemHandler)barrel.inventory, interval)) break;
            barrel.markForSync();
        }
    }

    public net.neoforged.neoforge.items.IItemHandler maritimeItems(net.minecraft.core.Direction side) { return this.isSealed() ? null : this.inventory; }
}

