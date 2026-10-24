package tfcmt.tfcitembarrelsandmore.common.blockentities;

import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blockentities.TickableInventoryBlockEntity;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.common.items.TFCItems;
import tfcmt.tfcitembarrelsandmore.common.IntArrayBuilder;
import net.dries007.tfc.util.Metal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.fluids.FluidActionResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;

import net.neoforged.neoforge.registries.DeferredHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tfcmt.tfcitembarrelsandmore.common.PistonPressure;
import tfcmt.tfcitembarrelsandmore.common.SteamEquilibriumModel;
import tfcmt.tfcitembarrelsandmore.common.TIBAMBlocks;
import tfcmt.tfcitembarrelsandmore.common.TIBAMConfig;
import tfcmt.tfcitembarrelsandmore.common.blockentities.BoilerBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blockentities.LargeSinglePistonEngineBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blockentities.TripleExpansionEngineBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.BoilerBlock;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.BoilerTankBlock;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.LargeSinglePistonEngineBlock;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.TripleExpansionEngineBlock;
import tfcmt.tfcitembarrelsandmore.common.container.BoilerTankContainer;

public class BoilerTankBlockEntity
extends TickableInventoryBlockEntity<ItemStackHandler>
implements MenuProvider {
    private static final Logger LOGGER = LogManager.getLogger((String)"TIBAM/BoilerTank");
    private static final Component NAME = Component.translatable((String)"block.tfc_item_barrels_and_more.boiler_tank");
    private static final int[] ENGINE_LINK_DISTANCES = new int[]{2, 1};
    public static final int SLOT_WATER_IN = 0;
    public static final int SLOT_WATER_OUT = 1;
    public static final int INVENTORY_SIZE = 2;
    private int waterAmount = 0;
    private int steam = 0;
    private int psi = 0;
    private boolean running = false;
    private int bleederTier = 0;
    private float bleederOpenMargin = -1.0f;
    private int throttle = 0;
    private int steamHeartbeatCounter = 0;
    private int waterTickCounter = 0;
    private float steamProductionAccumulator = 0.0f;
    private float steamProductionStepPerTick = 0.0f;
    private float cachedBoilerTemp = 0.0f;
    private int boilerTempCacheAge = 0;
    private static final int BOILER_TEMP_CACHE_TTL = 20;
    private float effectiveBoilerTemp = 0.0f;
    public static final int MAX_ENGINES = 1;
    private int linkedCount = 0;
    private final FluidTank waterTank;
    private final IFluidHandler fluidHandler;
    private final TankTier tier;

    public BoilerTankBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, TankTier tier) {
        super(type, pos, state, self -> new ItemStackHandler(2), NAME.getString());
        this.waterTank = new FluidTank((Integer)TIBAMConfig.SERVER.tankWaterCapacity.get()){

            public boolean isFluidValid(FluidStack stack) {
                return stack.getFluid().is(TFCTags.Fluids.ANY_FRESH_WATER);
            }
        };
        this.fluidHandler = this.waterTank;
        this.tier = tier;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BoilerTankBlockEntity tank) {
        tank.tick(level, pos, state);
    }

    private void tick(Level level, BlockPos pos, BlockState state) {
        double rupturePsi;
        this.processWaterInputSlot();
        int maxSteam = (Integer)TIBAMConfig.SERVER.engineMaxSteam.get();
        int waterPerSteam = (Integer)TIBAMConfig.SERVER.engineWaterPerSteam.get();
        int waterTickRate = (Integer)TIBAMConfig.SERVER.engineWaterTickRate.get();
        float minSteamTemp = (float)((Double)TIBAMConfig.SERVER.engineMinSteamTemp.get()).doubleValue();
        float equilibriumCeiling = (float)((Double)TIBAMConfig.SERVER.steamEquilibriumCeiling.get()).doubleValue();
        float equilibriumExponent = (float)((Double)TIBAMConfig.SERVER.steamEquilibriumExponent.get()).doubleValue();
        float tempApproachRate = (float)((Double)TIBAMConfig.SERVER.tankTempApproachRate.get()).doubleValue();
        float steamApproachRate = (float)((Double)TIBAMConfig.SERVER.tankSteamApproachRate.get()).doubleValue();
        double rupturePsiBuffer = (Double)TIBAMConfig.SERVER.tankRupturePsiBuffer.get();
        float rawBoilerTemp = this.getBoilerTemperature(level, pos);
        if (rawBoilerTemp > 0.0f) {
            this.cachedBoilerTemp = rawBoilerTemp;
            this.boilerTempCacheAge = 0;
        } else {
            ++this.boilerTempCacheAge;
            if (this.boilerTempCacheAge > 20) {
                this.cachedBoilerTemp = 0.0f;
            }
        }
        float boilerTemp = this.cachedBoilerTemp;
        boolean hasOpenVent = BoilerBlock.hasOpenVentAbove(level, pos.above());
        float ventedBoilerTemp = hasOpenVent ? boilerTemp : 0.0f;
        this.psi = maxSteam > 0 ? Math.min(100, this.steam * 100 / maxSteam) : 0;
        double d = rupturePsi = this.tier == TankTier.STEEL ? (double)PistonPressure.DANGER.getMax() + rupturePsiBuffer : (double)PistonPressure.HIGH.getMin() + rupturePsiBuffer;
        if ((double)this.psi >= rupturePsi) {
            LOGGER.warn("[boiler_tank] over-pressure rupture at {} (psi={})", (Object)pos, (Object)this.psi);
            float explosionRadius = (float)((Double)TIBAMConfig.SERVER.tankExplosionRadius.get()).doubleValue();
            Level.ExplosionInteraction interaction = (Boolean)TIBAMConfig.SERVER.tankExplosionBlockDamage.get() != false ? Level.ExplosionInteraction.BLOCK : Level.ExplosionInteraction.NONE;
            level.explode(null, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, explosionRadius, false, interaction);
            level.removeBlock(pos, false);
            return;
        }
        int scaledWaterDrain = waterPerSteam * Math.max(1, this.linkedCount);
        ++this.waterTickCounter;
        if (this.waterTickCounter >= waterTickRate) {
            this.waterTickCounter = 0;
            if (hasOpenVent && boilerTemp >= minSteamTemp && this.waterTank.getFluidAmount() >= scaledWaterDrain) {
                this.waterTank.drain(scaledWaterDrain, IFluidHandler.FluidAction.EXECUTE);
            }
        }
        SteamEquilibriumModel.ProductionResult production = SteamEquilibriumModel.tickProduction(this.steamHeartbeatCounter, this.effectiveBoilerTemp, this.steamProductionStepPerTick, this.steamProductionAccumulator, this.steam, maxSteam, ventedBoilerTemp, minSteamTemp, equilibriumCeiling, equilibriumExponent, tempApproachRate, steamApproachRate, this.waterTank.getFluidAmount() > 0);
        this.steamHeartbeatCounter = production.steamHeartbeatCounter();
        this.effectiveBoilerTemp = production.effectiveBoilerTemp();
        this.steamProductionStepPerTick = production.steamProductionStepPerTick();
        this.steamProductionAccumulator = production.steamProductionAccumulator();
        this.steam = production.steam();
        int prePsi = maxSteam > 0 ? Math.min(100, this.steam * 100 / maxSteam) : 0;
        SteamEquilibriumModel.BleederResult bleeder = SteamEquilibriumModel.tickBleeder(this.bleederTier, prePsi, this.steam, maxSteam, this.bleederOpenMargin, (float)((Double)TIBAMConfig.SERVER.bleederVentDropMinPsi.get()).doubleValue(), (float)((Double)TIBAMConfig.SERVER.bleederVentDropMaxPsi.get()).doubleValue(), level.getRandom());
        this.steam = bleeder.steam();
        this.bleederOpenMargin = bleeder.bleederOpenMargin();
        this.psi = maxSteam > 0 ? Math.min(100, this.steam * 100 / maxSteam) : 0;
        for (BlockPos candidate : this.getCandidateEnginePositions(state, pos)) {
            TickableInventoryBlockEntity engine;
            BlockEntity blockEntity;
            BlockState engineState = level.getBlockState(candidate);
            if (engineState.is((Block)TIBAMBlocks.TRIPLE_EXPANSION_ENGINE.get()) && ((Boolean)engineState.getValue((Property)TripleExpansionEngineBlock.LINKED)).booleanValue()) {
                blockEntity = level.getBlockEntity(candidate);
                if (!(blockEntity instanceof TripleExpansionEngineBlockEntity)) continue;
                engine = (TripleExpansionEngineBlockEntity)blockEntity;
                ((TripleExpansionEngineBlockEntity)engine).tickFromTank(this, this.psi, this.throttle, level, candidate, engineState);
                continue;
            }
            if (!engineState.is((Block)TIBAMBlocks.LARGE_SINGLE_PISTON_ENGINE.get()) || !((Boolean)engineState.getValue((Property)LargeSinglePistonEngineBlock.LINKED)).booleanValue() || !((blockEntity = level.getBlockEntity(candidate)) instanceof LargeSinglePistonEngineBlockEntity)) continue;
            engine = (LargeSinglePistonEngineBlockEntity)blockEntity;
            ((LargeSinglePistonEngineBlockEntity)engine).tickFromTank(this, this.psi, this.throttle, level, candidate, engineState);
        }
        this.waterAmount = this.waterTank.getFluidAmount();
        if (level.getGameTime() % 100L == 0L && (boilerTemp > 0.0f || this.steam > 0)) {
            LOGGER.debug("[boiler_tank] pos={} boilerTemp={} psi={} steam={} water={}", (Object)pos, (Object)String.format("%.1f", Float.valueOf(boilerTemp)), (Object)this.psi, (Object)this.steam, (Object)this.waterAmount);
        }
        this.setChanged();
        this.markForSync();
        this.checkForLastTickSync();
    }

    private void processWaterInputSlot() {
        boolean canPlace;
        if (this.level == null) {
            return;
        }
        ItemStack inStack = ((ItemStackHandler)this.inventory).getStackInSlot(0);
        if (inStack.isEmpty()) {
            return;
        }
        FluidActionResult result = FluidUtil.tryEmptyContainer((ItemStack)inStack, (IFluidHandler)this.waterTank, (int)((Integer)TIBAMConfig.SERVER.tankWaterCapacity.get()), null, (boolean)false);
        if (!result.isSuccess()) {
            return;
        }
        ItemStack emptyContainer = result.getResult();
        ItemStack outSlot = ((ItemStackHandler)this.inventory).getStackInSlot(1);
        boolean bl = canPlace = outSlot.isEmpty() || ItemStack.isSameItemSameComponents((ItemStack)outSlot, (ItemStack)emptyContainer) && outSlot.getCount() < outSlot.getMaxStackSize();
        if (!canPlace) {
            return;
        }
        FluidUtil.tryEmptyContainer((ItemStack)inStack, (IFluidHandler)this.waterTank, (int)((Integer)TIBAMConfig.SERVER.tankWaterCapacity.get()), null, (boolean)true);
        inStack.shrink(1);
        ((ItemStackHandler)this.inventory).setStackInSlot(0, inStack);
        if (outSlot.isEmpty()) {
            ((ItemStackHandler)this.inventory).setStackInSlot(1, emptyContainer);
        } else {
            outSlot.grow(1);
        }
    }

    private float getBoilerTemperature(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos.below());
        if (blockEntity instanceof BoilerBlockEntity) {
            BoilerBlockEntity boiler = (BoilerBlockEntity)blockEntity;
            return boiler.getTemperature();
        }
        return 0.0f;
    }

    public BlockPos[] getCandidateEnginePositions(BlockState state, BlockPos pos) {
        Direction back = ((Direction)state.getValue((Property)BoilerTankBlock.FACING)).getOpposite();
        BlockPos[] candidates = new BlockPos[ENGINE_LINK_DISTANCES.length * 3];
        int i = 0;
        for (int distance : ENGINE_LINK_DISTANCES) {
            candidates[i++] = pos.relative(back, distance).below();
            candidates[i++] = pos.relative(back, distance).relative(back.getClockWise()).below();
            candidates[i++] = pos.relative(back, distance).relative(back.getCounterClockWise()).below();
        }
        return candidates;
    }

    public void onNeighbourChanged(BlockState state, Level level, BlockPos pos) {
        if (this.linkedCount <= 0) {
            return;
        }
        int stillLinked = 0;
        for (BlockPos candidate : this.getCandidateEnginePositions(state, pos)) {
            BlockState s = level.getBlockState(candidate);
            if (s.is((Block)TIBAMBlocks.TRIPLE_EXPANSION_ENGINE.get()) && ((Boolean)s.getValue((Property)TripleExpansionEngineBlock.LINKED)).booleanValue()) {
                ++stillLinked;
                continue;
            }
            if (!s.is((Block)TIBAMBlocks.LARGE_SINGLE_PISTON_ENGINE.get()) || !((Boolean)s.getValue((Property)LargeSinglePistonEngineBlock.LINKED)).booleanValue()) continue;
            ++stillLinked;
        }
        if (stillLinked < this.linkedCount) {
            this.linkedCount = stillLinked;
            this.setChanged();
            this.markForSync();
        }
    }

    public int drainSteam(int amount) {
        int actual = Math.min(amount, this.steam);
        this.steam = Math.max(0, this.steam - actual);
        return actual;
    }

    public int getLinkedEngineCount() {
        return this.linkedCount;
    }

    public InteractionResult onPlayerInteract(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        if (this.isValidLinkRod(held)) {
            if (level.isClientSide) {
                return InteractionResult.SUCCESS;
            }
            return this.tryLinkOneEngine(state, level, pos, player, held);
        }
        if (!held.isEmpty() && !level.isClientSide && FluidHelpers.transferBetweenBlockEntityAndItem((ItemStack)held, (BlockEntity)this, (Player)player, (InteractionHand)hand)) {
            this.setChanged();
            this.markForSync();
            return InteractionResult.sidedSuccess((boolean)false);
        }
        if (held.isEmpty() && !player.isShiftKeyDown()) {
            if (!level.isClientSide && player instanceof ServerPlayer) {
                ServerPlayer sp = (ServerPlayer)player;
                ((ServerPlayer)sp).openMenu(this, this.worldPosition);
            }
            return InteractionResult.sidedSuccess((boolean)level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    private InteractionResult tryLinkOneEngine(BlockState state, Level level, BlockPos pos, Player player, ItemStack rod) {
        if (this.linkedCount >= 1) {
            player.displayClientMessage((Component)Component.translatable((String)"message.tfc_item_barrels_and_more.tank.no_engines"), true);
            return InteractionResult.FAIL;
        }
        for (BlockPos candidate : this.getCandidateEnginePositions(state, pos)) {
            BlockState engineState = level.getBlockState(candidate);
            boolean isTripleExpansion = engineState.is((Block)TIBAMBlocks.TRIPLE_EXPANSION_ENGINE.get());
            boolean isLargeSinglePiston = engineState.is((Block)TIBAMBlocks.LARGE_SINGLE_PISTON_ENGINE.get());
            if (!isTripleExpansion && !isLargeSinglePiston || this.tier == TankTier.STEEL && !isTripleExpansion) continue;
            if (isTripleExpansion) {
                if (((Boolean)engineState.getValue((Property)TripleExpansionEngineBlock.LINKED)).booleanValue()) continue;
                level.setBlock(candidate, (BlockState)engineState.setValue((Property)TripleExpansionEngineBlock.LINKED, Boolean.valueOf(true)), 2);
            } else {
                if (((Boolean)engineState.getValue((Property)LargeSinglePistonEngineBlock.LINKED)).booleanValue()) continue;
                level.setBlock(candidate, (BlockState)engineState.setValue((Property)LargeSinglePistonEngineBlock.LINKED, Boolean.valueOf(true)), 2);
            }
            ++this.linkedCount;
            if (!player.isCreative()) {
                rod.shrink(1);
            }
            level.playSound(null, pos, SoundEvents.COPPER_PLACE, SoundSource.BLOCKS, 0.8f, 0.9f);
            this.setChanged();
            this.markForSync();
            return InteractionResult.sidedSuccess((boolean)false);
        }
        player.displayClientMessage((Component)Component.translatable((String)"message.tfc_item_barrels_and_more.tank.no_engines"), true);
        return InteractionResult.FAIL;
    }

    private static boolean isCastIronRod(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return stack.getItem() == TFCItems.METAL_ITEMS.get(Metal.CAST_IRON).get(Metal.ItemType.ROD).get();
    }

    private static boolean isSteelRod(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return stack.getItem() == TFCItems.METAL_ITEMS.get(Metal.STEEL).get(Metal.ItemType.ROD).get();
    }

    private boolean isValidLinkRod(ItemStack stack) {
        return this.tier == TankTier.STEEL ? BoilerTankBlockEntity.isSteelRod(stack) : BoilerTankBlockEntity.isCastIronRod(stack);
    }

    public ItemStack getLinkRodStack() {
        Metal metal = this.tier == TankTier.STEEL ? Metal.STEEL : Metal.CAST_IRON;
        return new ItemStack((ItemLike)((DeferredHolder)((Map)TFCItems.METAL_ITEMS.get(metal)).get(Metal.ItemType.ROD)).get());
    }

    public AbstractContainerMenu createMenu(int windowId, Inventory playerInv, Player player) {
        return BoilerTankContainer.create(this, playerInv, windowId);
    }

    public Component getDisplayName() {
        return this.tier == TankTier.STEEL ? Component.translatable((String)"block.tfc_item_barrels_and_more.steel_boiler_tank") : NAME;
    }

    public IntArrayBuilder getSyncableData() {
        return new IntArrayBuilder().add(() -> this.waterAmount, v -> {
            this.waterAmount = v;
        }).add(() -> this.steam, v -> {
            this.steam = v;
        }).add(() -> this.psi, v -> {
            this.psi = v;
        }).add(() -> this.running ? 1 : 0, v -> {
            this.running = v != 0;
        }).add(() -> this.bleederTier, v -> {
            this.bleederTier = v;
        }).add(() -> this.throttle, v -> {
            this.throttle = v;
        });
    }

    public void setBleederTier(int tier) {
        this.bleederTier = Math.max(0, Math.min(4, tier));
        this.bleederOpenMargin = -1.0f;
        this.setChanged();
        this.markForSync();
    }

    public void setThrottle(int value) {
        this.throttle = Math.max(0, Math.min(100, value));
        this.setChanged();
        this.markForSync();
    }

    public TankTier getTier() {
        return this.tier;
    }

    public int getWaterAmount() {
        return this.waterAmount;
    }

    public int getWaterCapacity() {
        return (Integer)TIBAMConfig.SERVER.tankWaterCapacity.get();
    }

    public int getSteam() {
        return this.steam;
    }

    public int getMaxSteam() {
        return (Integer)TIBAMConfig.SERVER.engineMaxSteam.get();
    }

    public int getPsi() {
        return this.psi;
    }

    public boolean isRunning() {
        return this.running;
    }

    public int getBleederTier() {
        return this.bleederTier;
    }

    public int getThrottle() {
        return this.throttle;
    }

    public FluidTank getWaterTank() {
        return this.waterTank;
    }

    public BlockPos getBlockPos() {
        return this.worldPosition;
    }

    public ItemStackHandler getInventory() {
        return (ItemStackHandler)this.inventory;
    }





    public void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.steam = tag.getInt("Steam");
        this.bleederTier = tag.getInt("BleederTier");
        this.throttle = tag.contains("Throttle") ? tag.getInt("Throttle") : 0;
        float f = this.effectiveBoilerTemp = tag.contains("EffectiveBoilerTemp") ? tag.getFloat("EffectiveBoilerTemp") : 0.0f;
        if (tag.contains("WaterTank")) {
            CompoundTag waterTag = tag.getCompound("WaterTank");
            this.waterTank.readFromNBT(registries, waterTag);
        }
        this.waterAmount = this.waterTank.getFluidAmount();
        this.linkedCount = tag.getInt("LinkedCount");
    }

    public void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Steam", this.steam);
        tag.putInt("BleederTier", this.bleederTier);
        tag.putInt("Throttle", this.throttle);
        tag.putFloat("EffectiveBoilerTemp", this.effectiveBoilerTemp);
        CompoundTag waterTag = new CompoundTag();
        this.waterTank.writeToNBT(registries, waterTag);
        tag.put("WaterTank", (Tag)waterTag);
        tag.putInt("LinkedCount", this.linkedCount);
    }

    public static enum TankTier {
        CAST_IRON,
        STEEL;

    }

    public net.neoforged.neoforge.fluids.capability.IFluidHandler maritimeFluids(net.minecraft.core.Direction side) { return this.waterTank; }
}

