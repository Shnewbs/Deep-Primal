package tfcmt.tfcitembarrelsandmore.common.blockentities;

import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.dries007.tfc.common.blockentities.TickableInventoryBlockEntity;
import net.dries007.tfc.common.component.heat.HeatCapability;
import net.dries007.tfc.common.component.heat.IHeatConsumer;
import net.dries007.tfc.common.items.Powder;
import net.dries007.tfc.common.items.TFCItems;
import net.dries007.tfc.util.data.Fuel;
import tfcmt.tfcitembarrelsandmore.common.IntArrayBuilder;
import net.dries007.tfc.util.Metal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

import net.neoforged.neoforge.registries.DeferredHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tfcmt.tfcitembarrelsandmore.common.TIBAMBlockEntities;
import tfcmt.tfcitembarrelsandmore.common.TIBAMConfig;
import tfcmt.tfcitembarrelsandmore.common.blockentities.SinglePistonEngineBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.BoilerBlock;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.SinglePistonEngineBlock;
import tfcmt.tfcitembarrelsandmore.common.container.BoilerContainer;
import tfcmt.tfcitembarrelsandmore.compat.valkyrienskies.VSCompat;

public class BoilerBlockEntity
extends TickableInventoryBlockEntity<ItemStackHandler>
implements MenuProvider {
    private static final Logger LOGGER = LogManager.getLogger((String)"TIBAM/Boiler");
    public static final int SLOT_FUEL_1 = 0;
    public static final int SLOT_FUEL_2 = 1;
    public static final int SLOT_FUEL_3 = 2;
    public static final int SLOT_ASH = 3;
    public static final int INVENTORY_SIZE = 4;
    private static final Component NAME = Component.translatable((String)"block.tfc_item_barrels_and_more.boiler");
    private static final TagKey<Item> TAG_FIREPIT_FUEL = ItemTags.create((ResourceLocation)ResourceLocation.fromNamespaceAndPath("tfc", "firepit_fuel"));
    private static final TagKey<Item> TAG_FORGE_FUEL = ItemTags.create((ResourceLocation)ResourceLocation.fromNamespaceAndPath("tfc", "forge_fuel"));
    private static final TagKey<Item> TAG_FIRE_STARTER = ItemTags.create((ResourceLocation)ResourceLocation.fromNamespaceAndPath("tfc", "fire_starter"));
    private float temperature = 0.0f;
    private int fuelBurnTicksRemaining = 0;
    private float fuelBurnTemp = 0.0f;
    private boolean needsIgnition = false;
    private int activeFuelSlot = 0;
    private float averageBurnTemp = 0.0f;
    private boolean midConsumption = false;
    private int itemPickupCooldown = 0;
    private static final int ITEM_PICKUP_INTERVAL_TICKS = 10;
    private final IHeatConsumer heatCapability = new IHeatConsumer(){

        public float getTemperature() {
            return BoilerBlockEntity.this.temperature;
        }

        public void setTemperature(float temp) {
        }
    };
    private final IHeatConsumer heatOptional = this.heatCapability;
    @Nullable
    private SinglePistonEngineBlockEntity linkedEngine = null;
    private final IItemHandler itemHandler;

    public IntArrayBuilder getSyncableData() {
        return new IntArrayBuilder().add(() -> (int)(this.temperature * 10.0f), v -> {
            this.temperature = (float)v / 10.0f;
        }).add(() -> this.fuelBurnTicksRemaining, v -> {
            this.fuelBurnTicksRemaining = v;
        }).add(() -> this.getAshCount(), v -> {}).add(() -> this.activeFuelSlot, v -> {
            this.activeFuelSlot = v;
        });
    }

    public void setLinkedEngine(@Nullable SinglePistonEngineBlockEntity engine) {
        this.linkedEngine = engine;
    }

    protected void onLoadAdditional() {
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        BlockState state = this.getBlockState();
        if (!((Boolean)state.getValue((Property)BoilerBlock.LINKED)).booleanValue()) {
            return;
        }
        BlockPos enginePos = this.worldPosition.relative(((Direction)state.getValue((Property)BoilerBlock.FACING)).getOpposite());
        BlockEntity blockEntity = this.level.getBlockEntity(enginePos);
        if (blockEntity instanceof SinglePistonEngineBlockEntity) {
            SinglePistonEngineBlockEntity engine;
            this.linkedEngine = engine = (SinglePistonEngineBlockEntity)blockEntity;
        }
    }

    private void reestablishLinkIfNeeded(Level level, BlockPos pos, BlockState state) {
        SinglePistonEngineBlockEntity engine;
        if (!((Boolean)state.getValue((Property)BoilerBlock.LINKED)).booleanValue()) {
            this.linkedEngine = null;
            return;
        }
        BlockPos enginePos = pos.relative(((Direction)state.getValue((Property)BoilerBlock.FACING)).getOpposite());
        BlockEntity blockEntity = level.getBlockEntity(enginePos);
        this.linkedEngine = blockEntity instanceof SinglePistonEngineBlockEntity ? (engine = (SinglePistonEngineBlockEntity)blockEntity) : null;
    }

    public BoilerBlockEntity(BlockPos pos, BlockState state) {
        super((BlockEntityType)TIBAMBlockEntities.BOILER.get(), pos, state, self -> new BoilerItemHandler(), NAME.getString());
        ((BoilerItemHandler)this.inventory).owner = this;
        this.itemHandler = this.inventory;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BoilerBlockEntity boiler) {
        boiler.tick(level, pos, state);
    }

    private void tick(Level level, BlockPos pos, BlockState state) {
        this.reestablishLinkIfNeeded(level, pos, state);
        this.tryPickupDroppedFuel(level, pos, state);
        float heatGainFactor = (float)((Double)TIBAMConfig.SERVER.boilerHeatGainFactor.get()).doubleValue();
        float heatLossPerTick = (float)((Double)TIBAMConfig.SERVER.boilerHeatLossPerTick.get()).doubleValue();
        if (!BoilerBlock.hasOpenVent(level, pos)) {
            this.temperature = Math.max(0.0f, this.temperature - heatLossPerTick);
            this.setLit(false);
            HeatCapability.provideHeatTo((Level)level, (BlockPos)pos.above(), (float)this.temperature);
            if (this.linkedEngine != null) {
                this.linkedEngine.receiveBoilerTemperature(this.temperature);
            }
            this.setChanged();
            this.markForSync();
            this.checkForLastTickSync();
            return;
        }
        if (this.fuelBurnTicksRemaining > 0) {
            --this.fuelBurnTicksRemaining;
            this.temperature += (this.fuelBurnTemp - this.temperature) * heatGainFactor;
        } else if (!this.tryConsumeFuel()) {
            this.temperature = Math.max(0.0f, this.temperature - heatLossPerTick);
            this.setLit(false);
        }
        HeatCapability.provideHeatTo((Level)level, (BlockPos)pos.above(), (float)this.temperature);
        float destructionTemp = (float)((Double)TIBAMConfig.SERVER.boilerDestructionTemp.get()).doubleValue();
        float rodPopTemp = (float)((Double)TIBAMConfig.SERVER.copperRodPopTemp.get()).doubleValue();
        if (this.temperature >= destructionTemp) {
            this.handleDestruction(level, pos, state);
            return;
        }
        if (this.temperature >= rodPopTemp && ((Boolean)state.getValue((Property)BoilerBlock.LINKED)).booleanValue()) {
            this.popRod(level, pos, state);
        }
        if (this.linkedEngine != null) {
            this.linkedEngine.receiveBoilerTemperature(this.temperature);
        }
        this.setChanged();
        this.markForSync();
        this.checkForLastTickSync();
    }

    private void recomputeAverageBurnTemp() {
        float total = 0.0f;
        int count = 0;
        for (int slot = 0; slot <= 2; ++slot) {
            Fuel fuel;
            ItemStack stack = ((ItemStackHandler)this.inventory).getStackInSlot(slot);
            if (stack.isEmpty() || (fuel = Fuel.get((ItemStack)stack)) == null) continue;
            total += fuel.getTemperature();
            ++count;
        }
        this.averageBurnTemp = count > 0 ? total / (float)count : 0.0f;
    }

    private boolean tryConsumeFuel() {
        if (this.needsIgnition) {
            return false;
        }
        ItemStack ashSlot = ((ItemStackHandler)this.inventory).getStackInSlot(3);
        if (ashSlot.getCount() >= 64) {
            return false;
        }
        for (int attempt = 0; attempt < 3; ++attempt) {
            Fuel fuel;
            this.activeFuelSlot = (this.activeFuelSlot + 1) % 3;
            int slot = 0 + this.activeFuelSlot;
            ItemStack queued = ((ItemStackHandler)this.inventory).getStackInSlot(slot);
            if (queued.isEmpty() || (fuel = Fuel.get((ItemStack)queued)) == null) continue;
            float thisFuelTemp = fuel.getTemperature();
            this.midConsumption = true;
            ItemStack consumed = queued.copy();
            consumed.setCount(1);
            queued.shrink(1);
            ((ItemStackHandler)this.inventory).setStackInSlot(slot, queued);
            this.midConsumption = false;
            this.fuelBurnTicksRemaining = fuel.getDuration();
            int remainingCount = 0;
            float remainingTotal = 0.0f;
            for (int s = 0; s <= 2; ++s) {
                Fuel remainingFuel;
                ItemStack remaining = ((ItemStackHandler)this.inventory).getStackInSlot(s);
                if (remaining.isEmpty() || (remainingFuel = Fuel.get((ItemStack)remaining)) == null) continue;
                remainingTotal += remainingFuel.getTemperature();
                ++remainingCount;
            }
            this.fuelBurnTemp = remainingCount > 0 ? (remainingTotal + thisFuelTemp) / (float)(remainingCount + 1) : thisFuelTemp;
            this.setLit(true);
            this.tryProduceAsh(consumed);
            return true;
        }
        this.fuelBurnTemp = 0.0f;
        this.averageBurnTemp = 0.0f;
        return false;
    }

    private void tryProduceAsh(ItemStack consumedFuel) {
        float chance;
        if (this.level == null) {
            return;
        }
        if (consumedFuel.is(TAG_FIREPIT_FUEL)) {
            chance = 0.75f;
        } else if (consumedFuel.is(TAG_FORGE_FUEL)) {
            chance = 0.25f;
        } else {
            return;
        }
        if (this.level.getRandom().nextFloat() < chance) {
            ItemStack ashSlot = ((ItemStackHandler)this.inventory).getStackInSlot(3);
            ItemStack ashItem = new ItemStack((ItemLike)((DeferredHolder)TFCItems.POWDERS.get(Powder.WOOD_ASH)).get());
            if (ashSlot.isEmpty()) {
                ((ItemStackHandler)this.inventory).setStackInSlot(3, ashItem);
            } else if (ashSlot.getCount() < 64) {
                ashSlot.grow(1);
            }
        }
    }

    public InteractionResult onPlayerInteract(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        Fuel fuel;
        ItemStack held = player.getItemInHand(hand);
        if (player.isShiftKeyDown() && held.isEmpty() && ((Boolean)state.getValue((Property)BoilerBlock.LINKED)).booleanValue()) {
            this.unlinkFromEngine(level, pos, state);
            BoilerBlockEntity.spawnRodDrop(level, pos, state);
            return InteractionResult.sidedSuccess((boolean)level.isClientSide);
        }
        if (!player.isShiftKeyDown() && this.isCopperRod(held)) {
            return this.tryLinkEngine(state, level, pos, player, hand, held);
        }
        if (!player.isShiftKeyDown() && held.isEmpty()) {
            if (!level.isClientSide && player instanceof ServerPlayer) {
                ServerPlayer serverPlayer = (ServerPlayer)player;
                ((ServerPlayer)serverPlayer).openMenu(this, this.worldPosition);
            }
            return InteractionResult.sidedSuccess((boolean)level.isClientSide);
        }
        if (!held.isEmpty() && held.is(TAG_FIRE_STARTER) && this.needsIgnition) {
            this.ignite(level, pos, state, player, held);
            return InteractionResult.sidedSuccess((boolean)level.isClientSide);
        }
        if (!held.isEmpty() && !level.isClientSide && (fuel = Fuel.get((ItemStack)held)) != null) {
            return this.tryInsertFuel(held, player, hand, level);
        }
        return InteractionResult.PASS;
    }

    private boolean isCopperRod(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return stack.getItem() == ((DeferredHolder)((Map)TFCItems.METAL_ITEMS.get(Metal.COPPER)).get(Metal.ItemType.ROD)).get();
    }

    private InteractionResult tryLinkEngine(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, ItemStack rod) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        Direction facing = (Direction)state.getValue((Property)BoilerBlock.FACING);
        Direction pipeFace = facing.getOpposite();
        BlockPos enginePos = pos.relative(pipeFace);
        BlockState engineState = level.getBlockState(enginePos);
        if (!(engineState.getBlock() instanceof SinglePistonEngineBlock)) {
            player.displayClientMessage((Component)Component.translatable((String)"message.tfc_item_barrels_and_more.boiler.no_engine"), true);
            return InteractionResult.FAIL;
        }
        Direction engineFacing = (Direction)engineState.getValue((Property)SinglePistonEngineBlock.FACING);
        if (engineFacing != pipeFace) {
            player.displayClientMessage((Component)Component.translatable((String)"message.tfc_item_barrels_and_more.boiler.wrong_facing"), true);
            return InteractionResult.FAIL;
        }
        if (!player.isCreative()) {
            rod.shrink(1);
        }
        level.setBlock(pos, (BlockState)state.setValue((Property)BoilerBlock.LINKED, (Comparable)Boolean.valueOf(true)), 2);
        level.setBlock(enginePos, (BlockState)engineState.setValue((Property)SinglePistonEngineBlock.LINKED, (Comparable)Boolean.valueOf(true)), 2);
        BlockEntity blockEntity = level.getBlockEntity(enginePos);
        if (blockEntity instanceof SinglePistonEngineBlockEntity) {
            SinglePistonEngineBlockEntity engineBe = (SinglePistonEngineBlockEntity)blockEntity;
            this.setLinkedEngine(engineBe);
        }
        level.playSound(null, pos, SoundEvents.COPPER_PLACE, SoundSource.BLOCKS, 0.8f, 1.2f);
        return InteractionResult.sidedSuccess((boolean)false);
    }

    private void unlinkFromEngine(Level level, BlockPos pos, BlockState state) {
        Direction facing = (Direction)state.getValue((Property)BoilerBlock.FACING);
        BlockPos enginePos = pos.relative(facing.getOpposite());
        level.setBlock(pos, (BlockState)state.setValue((Property)BoilerBlock.LINKED, (Comparable)Boolean.valueOf(false)), 2);
        this.setLinkedEngine(null);
        BlockState engineState = level.getBlockState(enginePos);
        if (engineState.getBlock() instanceof SinglePistonEngineBlock) {
            level.setBlock(enginePos, (BlockState)engineState.setValue((Property)SinglePistonEngineBlock.LINKED, (Comparable)Boolean.valueOf(false)), 2);
        }
        LOGGER.debug("[boiler] rod unlinked at {}", (Object)pos);
    }

    private void popRod(Level level, BlockPos pos, BlockState state) {
        this.unlinkFromEngine(level, pos, state);
        BoilerBlockEntity.spawnRodDrop(level, pos, state);
        level.playSound(null, pos, SoundEvents.COPPER_BREAK, SoundSource.BLOCKS, 0.9f, 0.8f);
        LOGGER.debug("[boiler] copper rod popped at {} (temp={})", (Object)pos, (Object)String.format("%.1f", Float.valueOf(this.temperature)));
    }

    public static void spawnRodDrop(Level level, BlockPos pos, BlockState state) {
        Direction pipeFace = ((Direction)state.getValue((Property)BoilerBlock.FACING)).getOpposite();
        double x = (double)pos.getX() + 0.5 + (double)pipeFace.getStepX() * 0.6;
        double y = (double)pos.getY() + 0.5;
        double z = (double)pos.getZ() + 0.5 + (double)pipeFace.getStepZ() * 0.6;
        ItemStack rodStack = new ItemStack((ItemLike)((DeferredHolder)((Map)TFCItems.METAL_ITEMS.get(Metal.COPPER)).get(Metal.ItemType.ROD)).get());
        Vec3 dropPos = VSCompat.spawnPosToWorld(level, pos, x, y, z);
        level.addFreshEntity((Entity)new ItemEntity(level, dropPos.x, dropPos.y, dropPos.z, rodStack));
    }

    private InteractionResult tryInsertFuel(ItemStack held, Player player, InteractionHand hand, Level level) {
        int inserted = this.insertFuelIntoQueue(held);
        if (inserted <= 0) {
            return InteractionResult.PASS;
        }
        if (!player.isCreative()) {
            held.shrink(inserted);
        }
        this.setChanged();
        this.markForSync();
        return InteractionResult.sidedSuccess((boolean)level.isClientSide);
    }

    private int insertFuelIntoQueue(ItemStack source) {
        int[] fuelSlots;
        if (source.isEmpty()) {
            return 0;
        }
        int remaining = source.getCount();
        int totalInserted = 0;
        for (int slot : fuelSlots = new int[]{0, 1, 2}) {
            int spaceInSlot;
            int toInsert;
            boolean slotPartial;
            if (remaining <= 0) break;
            ItemStack existing = ((ItemStackHandler)this.inventory).getStackInSlot(slot);
            boolean slotEmpty = existing.isEmpty();
            boolean bl = slotPartial = !slotEmpty && ItemStack.isSameItemSameComponents((ItemStack)existing, (ItemStack)source) && existing.getCount() < existing.getMaxStackSize();
            if (!slotEmpty && !slotPartial || (toInsert = Math.min(spaceInSlot = slotEmpty ? source.getMaxStackSize() : source.getMaxStackSize() - existing.getCount(), remaining)) <= 0) continue;
            if (slotEmpty) {
                ItemStack toPlace = source.copy();
                toPlace.setCount(toInsert);
                ((ItemStackHandler)this.inventory).setStackInSlot(slot, toPlace);
            } else {
                existing.grow(toInsert);
                this.recomputeAverageBurnTemp();
                if (this.fuelBurnTicksRemaining <= 0) {
                    this.needsIgnition = true;
                }
            }
            remaining -= toInsert;
            totalInserted += toInsert;
        }
        return totalInserted;
    }

    private void tryPickupDroppedFuel(Level level, BlockPos pos, BlockState state) {
        if (--this.itemPickupCooldown > 0) {
            return;
        }
        this.itemPickupCooldown = 10;
        Direction facing = (Direction)state.getValue((Property)BoilerBlock.FACING);
        BlockPos frontPos = pos.relative(facing);
        AABB pickupBox = new AABB(frontPos).inflate(0.3);
        boolean anyInserted = false;
        for (ItemEntity itemEntity : level.getEntitiesOfClass(ItemEntity.class, pickupBox, e -> e.isAlive() && !e.getItem().isEmpty())) {
            int inserted;
            ItemStack stack = itemEntity.getItem();
            if (Fuel.get((ItemStack)stack) == null || (inserted = this.insertFuelIntoQueue(stack)) <= 0) continue;
            stack.shrink(inserted);
            if (stack.isEmpty()) {
                itemEntity.discard();
            } else {
                itemEntity.setItem(stack);
            }
            anyInserted = true;
        }
        if (!anyInserted) {
            return;
        }
        level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.2f, 1.0f + (level.random.nextFloat() - 0.5f) * 0.3f);
        this.setChanged();
        this.markForSync();
    }

    public boolean isAwaitingIgnition() {
        return this.needsIgnition;
    }

    public void ignite(Level level, BlockPos pos, BlockState state, @Nullable Player player, ItemStack stack) {
        if (level.isClientSide) {
            return;
        }
        if (!BoilerBlock.hasOpenVent(level, pos)) {
            return;
        }
        this.needsIgnition = false;
        if (player != null && !stack.isEmpty()) {
            stack.hurtAndBreak(1, (LivingEntity)player, p -> p.broadcastBreakEvent(InteractionHand.MAIN_HAND));
        }
        this.tryConsumeFuel();
        this.setChanged();
        this.markForSync();
        level.playSound(null, this.worldPosition, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0f, 1.0f);
    }

    public AbstractContainerMenu createMenu(int windowId, Inventory playerInv, Player player) {
        return BoilerContainer.create(this, playerInv, windowId);
    }

    public Component getDisplayName() {
        return NAME;
    }

    public ItemStackHandler getInventory() {
        return (ItemStackHandler)this.inventory;
    }

    public int getAshCount() {
        return ((ItemStackHandler)this.inventory).getStackInSlot(3).getCount();
    }

    public int getFuelTicksRemaining() {
        return this.fuelBurnTicksRemaining;
    }

    private void handleDestruction(Level level, BlockPos pos, BlockState state) {
        BlockPos enginePos;
        BlockState engineState;
        LOGGER.warn("[boiler] overheated and destroyed at {} (temp={})", (Object)pos, (Object)String.format("%.1f", Float.valueOf(this.temperature)));
        if (((Boolean)state.getValue((Property)BoilerBlock.LINKED)).booleanValue() && (engineState = level.getBlockState(enginePos = pos.relative(((Direction)state.getValue((Property)BoilerBlock.FACING)).getOpposite()))).getBlock() instanceof SinglePistonEngineBlock) {
            level.setBlock(enginePos, (BlockState)engineState.setValue((Property)SinglePistonEngineBlock.LINKED, (Comparable)Boolean.valueOf(false)), 2);
        }
        this.dropAllItems(level, pos);
        this.spawnSalvage(level, pos);
        if (((Boolean)TIBAMConfig.SERVER.boilerExplosionOnOverheat.get()).booleanValue()) {
            float explosionRadius = (float)((Double)TIBAMConfig.SERVER.boilerExplosionRadius.get()).doubleValue();
            Level.ExplosionInteraction interaction = (Boolean)TIBAMConfig.SERVER.boilerExplosionBlockDamage.get() != false ? Level.ExplosionInteraction.BLOCK : Level.ExplosionInteraction.NONE;
            level.explode(null, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, explosionRadius, false, interaction);
        } else {
            level.playSound(null, pos, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 0.5f, 1.5f);
        }
        level.removeBlock(pos, false);
    }

    private void spawnSalvage(Level level, BlockPos pos) {
        ItemStack sheets = new ItemStack((ItemLike)((DeferredHolder)((Map)TFCItems.METAL_ITEMS.get(Metal.CAST_IRON)).get(Metal.ItemType.SHEET)).get(), 5);
        ItemStack doubleIngot = new ItemStack((ItemLike)((DeferredHolder)((Map)TFCItems.METAL_ITEMS.get(Metal.CAST_IRON)).get(Metal.ItemType.DOUBLE_INGOT)).get(), 1);
        Vec3 dropPos = VSCompat.spawnPosToWorld(level, pos, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5);
        level.addFreshEntity((Entity)new ItemEntity(level, dropPos.x, dropPos.y, dropPos.z, sheets));
        level.addFreshEntity((Entity)new ItemEntity(level, dropPos.x, dropPos.y, dropPos.z, doubleIngot));
    }

    private void dropAllItems(Level level, BlockPos pos) {
        for (int slot = 0; slot < 4; ++slot) {
            ItemStack stack = ((ItemStackHandler)this.inventory).getStackInSlot(slot);
            if (stack.isEmpty()) continue;
            Vec3 dropPos = VSCompat.spawnPosToWorld(level, pos, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5);
            level.addFreshEntity((Entity)new ItemEntity(level, dropPos.x, dropPos.y, dropPos.z, stack.copy()));
            ((BoilerItemHandler)this.inventory).clearSlotDirect(slot);
        }
    }

    public float getTemperature() {
        return this.temperature;
    }

    public boolean isLit() {
        return this.fuelBurnTicksRemaining > 0;
    }

    private void setLit(boolean lit) {
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        BlockState current = this.level.getBlockState(this.worldPosition);
        if ((Boolean)current.getValue((Property)BoilerBlock.LIT) == lit) {
            return;
        }
        this.level.setBlock(this.worldPosition, (BlockState)current.setValue((Property)BoilerBlock.LIT, (Comparable)Boolean.valueOf(lit)), 2);
    }





    public void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.temperature = tag.getFloat("Temperature");
        this.fuelBurnTicksRemaining = tag.getInt("FuelTicks");
        this.fuelBurnTemp = tag.getFloat("FuelTemp");
        this.needsIgnition = tag.getBoolean("NeedsIgnition");
        this.activeFuelSlot = tag.getInt("ActiveFuelSlot");
        this.averageBurnTemp = tag.getFloat("AverageBurnTemp");
    }

    public void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putFloat("Temperature", this.temperature);
        tag.putInt("FuelTicks", this.fuelBurnTicksRemaining);
        tag.putFloat("FuelTemp", this.fuelBurnTemp);
        tag.putBoolean("NeedsIgnition", this.needsIgnition);
        tag.putInt("ActiveFuelSlot", this.activeFuelSlot);
        tag.putFloat("AverageBurnTemp", this.averageBurnTemp);
    }

    private static final class BoilerItemHandler
    extends ItemStackHandler {
        BoilerBlockEntity owner = null;

        BoilerItemHandler() {
            super(4);
        }

        public void setStackInSlot(int slot, ItemStack stack) {
            super.setStackInSlot(slot, stack);
            if (this.owner == null) {
                return;
            }
            if (slot < 0 || slot > 2) {
                return;
            }
            if (!(stack.isEmpty() || this.owner.isLit() || this.owner.midConsumption)) {
                this.owner.needsIgnition = true;
            }
            this.owner.recomputeAverageBurnTemp();
            this.owner.setChanged();
        }

        void clearSlotDirect(int slot) {
            this.stacks.set(slot, (Object)ItemStack.EMPTY);
        }
    }

    public net.neoforged.neoforge.items.IItemHandler maritimeItems(net.minecraft.core.Direction side) { return this.itemHandler; }
    public net.dries007.tfc.common.component.heat.IHeatConsumer maritimeHeat(net.minecraft.core.Direction side) { return side == net.minecraft.core.Direction.UP ? this.heatCapability : null; }
}

