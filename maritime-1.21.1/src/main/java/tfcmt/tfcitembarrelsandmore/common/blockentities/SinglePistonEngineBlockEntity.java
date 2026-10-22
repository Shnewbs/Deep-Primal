package tfcmt.tfcitembarrelsandmore.common.blockentities;

import java.util.Map;
import javax.annotation.Nonnull;
import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blockentities.TickableInventoryBlockEntity;
import net.dries007.tfc.common.blockentities.rotation.RotatingBlockEntity;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.common.items.TFCItems;
import tfcmt.tfcitembarrelsandmore.common.IntArrayBuilder;
import net.dries007.tfc.util.Metal;
import net.dries007.tfc.util.rotation.NetworkAction;
import net.dries007.tfc.util.rotation.Node;
import net.dries007.tfc.util.rotation.Rotation;
import net.dries007.tfc.util.rotation.SinkNode;
import net.dries007.tfc.util.rotation.SourceNode;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidActionResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

import net.neoforged.neoforge.registries.DeferredHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import tfcmt.tfcitembarrelsandmore.common.PistonPressure;
import tfcmt.tfcitembarrelsandmore.common.RotationNetworkWalker;
import tfcmt.tfcitembarrelsandmore.common.SinkTollRegistry;
import tfcmt.tfcitembarrelsandmore.common.SteamEquilibriumModel;
import tfcmt.tfcitembarrelsandmore.common.TIBAMBlockEntities;
import tfcmt.tfcitembarrelsandmore.common.TIBAMConfig;
import tfcmt.tfcitembarrelsandmore.common.blockentities.BoilerBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.BoilerBlock;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.SinglePistonEngineBlock;
import tfcmt.tfcitembarrelsandmore.common.container.SinglePistonEngineContainer;
import tfcmt.tfcitembarrelsandmore.compat.create.EngineSoundInstance;
import tfcmt.tfcitembarrelsandmore.compat.valkyrienskies.VSCompat;

public class SinglePistonEngineBlockEntity
extends TickableInventoryBlockEntity<ItemStackHandler>
implements MenuProvider,
RotatingBlockEntity,
EngineSoundInstance.SteamEngineAccessor {
    private static final Logger LOGGER = LogManager.getLogger((String)"TIBAM/PistonEngine");
    public static final int SLOT_WATER_IN = 0;
    public static final int SLOT_WATER_OUT = 1;
    public static final int INVENTORY_SIZE = 2;
    private static final Component NAME = Component.translatable((String)"block.tfc_item_barrels_and_more.single_piston_engine");
    private int waterAmount = 0;
    private int steam = 0;
    private int psi = 0;
    private boolean running = false;
    private transient boolean soundStarted = false;
    private int bleederTier = 0;
    private float bleederOpenMargin = -1.0f;
    private int throttle = 100;
    private int steamHeartbeatCounter = 0;
    private int tollHeartbeatCounter = 0;
    private float currentTollRate = 0.0f;
    private float tollAccumulator = 0.0f;
    private float effectiveBoilerTemp = 0.0f;
    private float steamProductionStepPerTick = 0.0f;
    private float steamProductionAccumulator = 0.0f;
    private float steamDrainAccumulator = 0.0f;
    private float pushedBoilerTemp = 0.0f;
    private int waterTickCounter = 0;
    private final SourceNode sourceNode;
    private boolean invalidInNetwork = false;
    private final FluidTank waterTank;
    private final IFluidHandler fluidHandler;
    private final IItemHandler itemHandler;

    public void markAsInvalidInNetwork() {
        this.invalidInNetwork = true;
    }

    public boolean isInvalidInNetwork() {
        return this.invalidInNetwork;
    }

    public Node getRotationNode() {
        return this.sourceNode;
    }

    public SinglePistonEngineBlockEntity(BlockPos pos, BlockState state) {
        super(TIBAMBlockEntities.SINGLE_PISTON_ENGINE.get(), pos, state, self -> new ItemStackHandler(2), NAME.getString());
        this.waterTank = new FluidTank((Integer)TIBAMConfig.SERVER.engineWaterCapacity.get()){

            public boolean isFluidValid(FluidStack stack) {
                return stack.getFluid().is(TFCTags.Fluids.ANY_FRESH_WATER);
            }
        };
        this.fluidHandler = this.waterTank;
        this.itemHandler = this.inventory;
        Direction flywheel = (Direction)state.getValue((Property)SinglePistonEngineBlock.FACING);
        this.sourceNode = new SourceNode(pos, Node.ofAxis((Direction.Axis)flywheel.getAxis()), flywheel, 0.0f){};
    }

    protected void onLoadAdditional() {
        BlockPos boilerPos;
        BlockEntity blockEntity;
        BlockState state;
        this.performNetworkAction(NetworkAction.ADD_SOURCE);
        if (this.level != null && !this.level.isClientSide && ((Boolean)(state = this.getBlockState()).getValue((Property)SinglePistonEngineBlock.LINKED)).booleanValue() && (blockEntity = this.level.getBlockEntity(boilerPos = this.worldPosition.relative(((Direction)state.getValue((Property)SinglePistonEngineBlock.FACING)).getOpposite()))) instanceof BoilerBlockEntity) {
            BoilerBlockEntity boiler = (BoilerBlockEntity)blockEntity;
            boiler.setLinkedEngine(this);
        }
    }

    protected void onUnloadAdditional() {
        this.performNetworkAction(NetworkAction.REMOVE);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SinglePistonEngineBlockEntity engine) {
        engine.tick(level, pos, state);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, SinglePistonEngineBlockEntity engine) {
        Rotation.Tickable tickable = engine.sourceNode.rotation();
        if (tickable != null) {
            tickable.tick();
        }
        if (engine.isRunning() && !engine.soundStarted) {
            EngineSoundInstance.startFor(engine, level, pos, SinglePistonEngineBlock.RUNNING);
            engine.soundStarted = true;
        }
        if (!engine.isRunning()) {
            engine.soundStarted = false;
        }
    }

    private void tick(Level level, BlockPos pos, BlockState state) {
        boolean shouldRun;
        BlockState liveState = level.getBlockState(pos);
        this.destroyIfInvalid(level, pos);
        this.processWaterInputSlot();
        int maxSteam = (Integer)TIBAMConfig.SERVER.engineMaxSteam.get();
        int waterPerSteam = (Integer)TIBAMConfig.SERVER.engineWaterPerSteam.get();
        int waterTickRate = (Integer)TIBAMConfig.SERVER.engineWaterTickRate.get();
        float minSteamTemp = (float)((Double)TIBAMConfig.SERVER.engineMinSteamTemp.get()).doubleValue();
        float equilibriumCeiling = (float)((Double)TIBAMConfig.SERVER.steamEquilibriumCeiling.get()).doubleValue();
        float equilibriumExponent = (float)((Double)TIBAMConfig.SERVER.steamEquilibriumExponent.get()).doubleValue();
        float tempApproachRate = (float)((Double)TIBAMConfig.SERVER.engineTempApproachRate.get()).doubleValue();
        float steamApproachRate = (float)((Double)TIBAMConfig.SERVER.engineSteamApproachRate.get()).doubleValue();
        float drainAtWorking = (float)((Double)TIBAMConfig.SERVER.engineSteamDrainAtWorking.get()).doubleValue();
        float drainAtMax = (float)((Double)TIBAMConfig.SERVER.engineSteamDrainAtMax.get()).doubleValue();
        float baseSpeed = TIBAMConfig.baseSpeedRadPerTick();
        float maxMultiplier = (float)((Double)TIBAMConfig.SERVER.engineMaxSpeedMultiplier.get()).doubleValue();
        float boilerTemp = this.getLinkedBoilerTemperature(level, pos, liveState);
        ++this.waterTickCounter;
        if (this.waterTickCounter >= waterTickRate) {
            this.waterTickCounter = 0;
            if (boilerTemp >= minSteamTemp && this.waterTank.getFluidAmount() >= waterPerSteam) {
                this.waterTank.drain(waterPerSteam, IFluidHandler.FluidAction.EXECUTE);
            }
        }
        SteamEquilibriumModel.ProductionResult production = SteamEquilibriumModel.tickProduction(this.steamHeartbeatCounter, this.effectiveBoilerTemp, this.steamProductionStepPerTick, this.steamProductionAccumulator, this.steam, maxSteam, boilerTemp, minSteamTemp, equilibriumCeiling, equilibriumExponent, tempApproachRate, steamApproachRate, this.waterTank.getFluidAmount() > 0);
        this.steamHeartbeatCounter = production.steamHeartbeatCounter();
        this.effectiveBoilerTemp = production.effectiveBoilerTemp();
        this.steamProductionStepPerTick = production.steamProductionStepPerTick();
        this.steamProductionAccumulator = production.steamProductionAccumulator();
        this.steam = production.steam();
        int prePsi = maxSteam > 0 ? (int)Math.min((float)this.steam / (float)maxSteam * 100.0f, 100.0f) : 0;
        SteamEquilibriumModel.BleederResult bleeder = SteamEquilibriumModel.tickBleeder(this.bleederTier, prePsi, this.steam, maxSteam, this.bleederOpenMargin, (float)((Double)TIBAMConfig.SERVER.bleederVentDropMinPsi.get()).doubleValue(), (float)((Double)TIBAMConfig.SERVER.bleederVentDropMaxPsi.get()).doubleValue(), level.getRandom());
        this.steam = bleeder.steam();
        this.bleederOpenMargin = bleeder.bleederOpenMargin();
        ++this.tollHeartbeatCounter;
        if (this.tollHeartbeatCounter >= 20) {
            this.tollHeartbeatCounter = 0;
            this.currentTollRate = 0.0f;
            for (SinkNode sink : RotationNetworkWalker.findSinks(level, this.sourceNode.pos(), this.sourceNode.network())) {
                BlockPos sinkPos = sink.pos();
                BlockEntity sinkBe = level.getBlockEntity(sinkPos);
                if (sinkBe == null || !SinkTollRegistry.isWorking(level, sinkPos, sinkBe, (Node)sink)) continue;
                this.currentTollRate += (float)SinkTollRegistry.getTier(level.getBlockState(sinkPos).getBlock()).getRate();
            }
        }
        if (this.currentTollRate > 0.0f) {
            this.tollAccumulator += this.currentTollRate / 20.0f;
            int tollWhole = (int)this.tollAccumulator;
            if (tollWhole != 0) {
                this.tollAccumulator -= (float)tollWhole;
                this.steam = Math.max(0, this.steam - tollWhole);
            }
        }
        this.waterAmount = this.waterTank.getFluidAmount();
        this.psi = (int)Math.min((float)this.steam / (float)maxSteam * 100.0f, 100.0f);
        PistonPressure tier = PistonPressure.getTier(this.psi);
        float throttleFraction = (float)this.throttle / 100.0f;
        float targetSpeed = baseSpeed * ((float)this.psi / 100.0f) * maxMultiplier * throttleFraction;
        boolean bl = shouldRun = this.throttle > 0 && tier != null && tier.hasOutput() && (Boolean)liveState.getValue((Property)SinglePistonEngineBlock.LINKED) != false;
        if (level.getGameTime() % 100L == 0L) {
            LOGGER.debug("[piston] pos={} psi={} steam={} bleederTier={} linked={} tier={} shouldRun={} targetSpeed={} sourceNode={} networkId={}", (Object)this.worldPosition, (Object)this.psi, (Object)this.steam, (Object)this.bleederTier, (Object)liveState.getValue((Property)SinglePistonEngineBlock.LINKED), (Object)tier, (Object)shouldRun, (Object)Float.valueOf(targetSpeed), (Object)"present", (Object)this.sourceNode.network());
        }
        if (shouldRun) {
            float scaledDrain = SinglePistonEngineBlockEntity.computeSteamDrain(this.psi, drainAtWorking, drainAtMax) * throttleFraction;
            this.steamDrainAccumulator += scaledDrain;
            if (this.steamDrainAccumulator >= 1.0f) {
                int wholeDrain = (int)this.steamDrainAccumulator;
                this.steamDrainAccumulator -= (float)wholeDrain;
                this.steam = Math.max(0, this.steam - wholeDrain);
            }
            this.sourceNode.rotation().setSpeed(targetSpeed);
            if (this.psi >= PistonPressure.WORKING.getMin()) {
                this.handleDestruction(level, pos, liveState);
                return;
            }
            if (tier.getRodPopChance() > 0.0f && level.getRandom().nextFloat() < tier.getRodPopChance()) {
                this.popRod(level, pos, liveState);
                shouldRun = false;
            }
        } else {
            this.sourceNode.rotation().setSpeed(0.0f);
            this.steamDrainAccumulator = 0.0f;
        }
        this.running = shouldRun;
        if ((Boolean)liveState.getValue((Property)SinglePistonEngineBlock.RUNNING) != shouldRun) {
            level.setBlock(pos, (BlockState)liveState.setValue((Property)SinglePistonEngineBlock.RUNNING, Boolean.valueOf(shouldRun)), 2);
            this.performNetworkAction(NetworkAction.UPDATE);
            this.setChanged();
            this.markForSync();
        }
        this.setChanged();
        this.markForSync();
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
        FluidActionResult result = FluidUtil.tryEmptyContainer((ItemStack)inStack, (IFluidHandler)this.waterTank, (int)((Integer)TIBAMConfig.SERVER.engineWaterCapacity.get()), null, (boolean)false);
        if (!result.isSuccess()) {
            return;
        }
        ItemStack emptyContainer = result.getResult();
        ItemStack outSlot = ((ItemStackHandler)this.inventory).getStackInSlot(1);
        boolean bl = canPlace = outSlot.isEmpty() || ItemStack.isSameItemSameComponents((ItemStack)outSlot, (ItemStack)emptyContainer) && outSlot.getCount() < outSlot.getMaxStackSize();
        if (!canPlace) {
            return;
        }
        FluidUtil.tryEmptyContainer((ItemStack)inStack, (IFluidHandler)this.waterTank, (int)((Integer)TIBAMConfig.SERVER.engineWaterCapacity.get()), null, (boolean)true);
        inStack.shrink(1);
        ((ItemStackHandler)this.inventory).setStackInSlot(0, inStack);
        if (outSlot.isEmpty()) {
            ((ItemStackHandler)this.inventory).setStackInSlot(1, emptyContainer);
        } else {
            outSlot.grow(1);
        }
    }

    private static float computeSteamDrain(int currentPsi, float drainAtWorking, float drainAtMax) {
        int WORKING_PSI = 45;
        if (currentPsi <= 0) {
            return 0.0f;
        }
        if (currentPsi >= 100) {
            return drainAtMax;
        }
        if (currentPsi <= 45) {
            return drainAtWorking * ((float)currentPsi / 45.0f);
        }
        float t = (float)(currentPsi - 45) / 55.0f;
        return drainAtWorking + t * (drainAtMax - drainAtWorking);
    }

    public void receiveBoilerTemperature(float temp) {
        this.pushedBoilerTemp = temp;
    }

    private float getLinkedBoilerTemperature(Level level, BlockPos pos, BlockState state) {
        if (!((Boolean)state.getValue((Property)SinglePistonEngineBlock.LINKED)).booleanValue()) {
            return 0.0f;
        }
        float temp = this.pushedBoilerTemp;
        this.pushedBoilerTemp = 0.0f;
        return temp;
    }

    private void popRod(Level level, BlockPos pos, BlockState state) {
        Direction facing = (Direction)state.getValue((Property)SinglePistonEngineBlock.FACING);
        Direction boilerDir = facing.getOpposite();
        level.setBlock(pos, (BlockState)state.setValue((Property)SinglePistonEngineBlock.LINKED, Boolean.valueOf(false)), 2);
        BlockPos boilerPos = pos.relative(boilerDir);
        BlockEntity blockEntity = level.getBlockEntity(boilerPos);
        if (blockEntity instanceof BoilerBlockEntity) {
            BoilerBlockEntity boilerBe = (BoilerBlockEntity)blockEntity;
            level.setBlock(boilerPos, (BlockState)level.getBlockState(boilerPos).setValue((Property)BoilerBlock.LINKED, Boolean.valueOf(false)), 2);
            boilerBe.setLinkedEngine(null);
        }
        this.dropRod(level, pos, state);
        level.playSound(null, pos, SoundEvents.COPPER_BREAK, SoundSource.BLOCKS, 0.9f, 0.8f);
        LOGGER.debug("[piston] copper rod popped at {} (psi={})", (Object)pos, (Object)this.psi);
    }

    public void dropRod(Level level, BlockPos pos, BlockState state) {
        Direction boilerDir = ((Direction)state.getValue((Property)SinglePistonEngineBlock.FACING)).getOpposite();
        double x = (double)pos.getX() + 0.5 + (double)boilerDir.getStepX() * 0.6;
        double y = (double)pos.getY() + 0.5;
        double z = (double)pos.getZ() + 0.5 + (double)boilerDir.getStepZ() * 0.6;
        Vec3 dropPos = VSCompat.spawnPosToWorld(level, pos, x, y, z);
        ItemStack rod = new ItemStack((ItemLike)TFCItems.METAL_ITEMS.get(Metal.COPPER).get(Metal.ItemType.ROD).get());
        level.addFreshEntity((Entity)new ItemEntity(level, dropPos.x, dropPos.y, dropPos.z, rod));
    }

    private void handleDestruction(Level level, BlockPos pos, BlockState state) {
        LOGGER.warn("[piston] over-pressure explosion at {} (psi={})", (Object)pos, (Object)this.psi);
        if (((Boolean)state.getValue((Property)SinglePistonEngineBlock.LINKED)).booleanValue()) {
            this.dropRod(level, pos, state);
            Direction boilerDir = ((Direction)state.getValue((Property)SinglePistonEngineBlock.FACING)).getOpposite();
            BlockPos boilerPos = pos.relative(boilerDir);
            BlockEntity blockEntity = level.getBlockEntity(boilerPos);
            if (blockEntity instanceof BoilerBlockEntity) {
                BoilerBlockEntity boiler = (BoilerBlockEntity)blockEntity;
                level.setBlock(boilerPos, (BlockState)level.getBlockState(boilerPos).setValue((Property)BoilerBlock.LINKED, Boolean.valueOf(false)), 2);
                boiler.setLinkedEngine(null);
            }
        }
        level.explode(null, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, 1.5f, false, Level.ExplosionInteraction.NONE);
        level.removeBlock(pos, false);
    }

    public InteractionResult onPlayerInteract(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        Direction facing = (Direction)state.getValue((Property)SinglePistonEngineBlock.FACING);
        Direction boilerDir = facing.getOpposite();
        if (player.isShiftKeyDown() && held.isEmpty() && hit.getDirection() == boilerDir && ((Boolean)state.getValue((Property)SinglePistonEngineBlock.LINKED)).booleanValue()) {
            this.popRod(level, pos, state);
            return InteractionResult.sidedSuccess((boolean)level.isClientSide);
        }
        if (!player.isShiftKeyDown() && hit.getDirection() == boilerDir && this.isCopperRod(held)) {
            return this.tryLink(state, level, pos, player, hand, held);
        }
        if (!held.isEmpty() && !level.isClientSide && FluidHelpers.transferBetweenBlockEntityAndItem((ItemStack)held, (BlockEntity)this, (Player)player, (InteractionHand)hand)) {
            this.setChanged();
            this.markForSync();
            return InteractionResult.sidedSuccess((boolean)false);
        }
        if (held.isEmpty() && !player.isShiftKeyDown() && hit.getDirection() != boilerDir) {
            if (!level.isClientSide && player instanceof ServerPlayer) {
                ServerPlayer sp = (ServerPlayer)player;
                ((ServerPlayer)sp).openMenu(this, this.worldPosition);
            }
            return InteractionResult.sidedSuccess((boolean)level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    private InteractionResult tryLink(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, ItemStack rod) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        Direction facing = (Direction)state.getValue((Property)SinglePistonEngineBlock.FACING);
        BlockPos boilerPos = pos.relative(facing.getOpposite());
        BlockEntity blockEntity = level.getBlockEntity(boilerPos);
        if (!(blockEntity instanceof BoilerBlockEntity)) {
            player.displayClientMessage((Component)Component.translatable((String)"message.tfc_item_barrels_and_more.engine.no_boiler"), true);
            return InteractionResult.FAIL;
        }
        BoilerBlockEntity boilerBe = (BoilerBlockEntity)blockEntity;
        if (!player.isCreative()) {
            rod.shrink(1);
        }
        level.setBlock(pos, (BlockState)state.setValue((Property)SinglePistonEngineBlock.LINKED, Boolean.valueOf(true)), 2);
        level.setBlock(boilerPos, (BlockState)level.getBlockState(boilerPos).setValue((Property)BoilerBlock.LINKED, Boolean.valueOf(true)), 2);
        boilerBe.setLinkedEngine(this);
        level.playSound(null, pos, SoundEvents.COPPER_PLACE, SoundSource.BLOCKS, 0.8f, 1.2f);
        return InteractionResult.sidedSuccess((boolean)false);
    }

    private boolean isCopperRod(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return stack.getItem() == TFCItems.METAL_ITEMS.get(Metal.COPPER).get(Metal.ItemType.ROD).get();
    }

    public IntArrayBuilder getSyncableData() {
        return new IntArrayBuilder().add(() -> this.waterAmount, v -> {
            this.waterAmount = v;
            if (v <= 0) {
                this.waterTank.setFluid(FluidStack.EMPTY);
            } else if (this.waterTank.isEmpty()) {
                this.waterTank.setFluid(new FluidStack((Fluid)Fluids.WATER, v));
            } else {
                this.waterTank.getFluid().setAmount(v);
            }
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

    public void setThrottle(int value) {
        this.throttle = Math.max(0, Math.min(100, value));
        this.steamDrainAccumulator = 0.0f;
        this.setChanged();
        this.markForSync();
    }

    public void setBleederTier(int tier) {
        this.bleederTier = Math.max(0, Math.min(4, tier));
        this.bleederOpenMargin = -1.0f;
        this.setChanged();
        this.markForSync();
    }

    public AbstractContainerMenu createMenu(int windowId, Inventory playerInv, Player player) {
        return SinglePistonEngineContainer.create(this, playerInv, windowId);
    }

    public Component getDisplayName() {
        return NAME;
    }

    public int getWaterAmount() {
        return this.waterAmount;
    }

    public int getWaterCapacity() {
        return (Integer)TIBAMConfig.SERVER.engineWaterCapacity.get();
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

    public ItemStackHandler getInventory() {
        return (ItemStackHandler)this.inventory;
    }

    public float getRotationAngle(float partialTick) {
        return RotatingBlockEntity.super.getRotationAngle(partialTick);
    }

    @Override
    public @Nullable Rotation getSourceRotation() {
        return this.sourceNode.rotation();
    }





    public void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.waterTank.readFromNBT(registries, tag.getCompound("WaterTank"));
        this.waterAmount = this.waterTank.getFluidAmount();
        this.steam = tag.getInt("Steam");
        this.psi = tag.getInt("Psi");
        this.running = tag.getBoolean("Running");
        this.invalidInNetwork = tag.getBoolean("InvalidInNetwork");
        this.bleederTier = tag.getInt("BleederTier");
        this.throttle = tag.contains("Throttle") ? tag.getInt("Throttle") : 100;
        this.effectiveBoilerTemp = tag.contains("EffectiveBoilerTemp") ? tag.getFloat("EffectiveBoilerTemp") : 0.0f;
        this.sourceNode.rotation().loadFromTag(tag);
    }

    public void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("WaterTank", (Tag)this.waterTank.writeToNBT(registries, new CompoundTag()));
        tag.putInt("Steam", this.steam);
        tag.putInt("Psi", this.psi);
        tag.putBoolean("Running", this.running);
        tag.putBoolean("InvalidInNetwork", this.invalidInNetwork);
        tag.putInt("BleederTier", this.bleederTier);
        tag.putInt("Throttle", this.throttle);
        tag.putFloat("EffectiveBoilerTemp", this.effectiveBoilerTemp);
        this.sourceNode.rotation().saveToTag(tag);
    }

    public net.neoforged.neoforge.items.IItemHandler maritimeItems(net.minecraft.core.Direction side) { return this.itemHandler; }
    public net.neoforged.neoforge.fluids.capability.IFluidHandler maritimeFluids(net.minecraft.core.Direction side) { return this.waterTank; }
}

