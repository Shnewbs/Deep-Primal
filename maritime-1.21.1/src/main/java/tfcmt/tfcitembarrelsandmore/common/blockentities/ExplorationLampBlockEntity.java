package tfcmt.tfcitembarrelsandmore.common.blockentities;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.dries007.tfc.common.blockentities.TFCBlockEntity;

import net.dries007.tfc.common.capabilities.FluidTankCallback;
import net.dries007.tfc.common.capabilities.InventoryFluidTank;
import net.dries007.tfc.util.data.LampFuel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import tfcmt.tfcitembarrelsandmore.common.TIBAMBlockEntities;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.ExplorationLampBeam;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.ExplorationLampBlock;

public class ExplorationLampBlockEntity
extends TFCBlockEntity
implements FluidTankCallback {
    public static final int CAPACITY = 250;
    private final InventoryFluidTank tank;
    private final IFluidHandler fluidHolder;
    private Mode mode = Mode.AREA;
    private int liveTicksSinceDrain = 0;
    private boolean pendingLitRestore = false;
    private static final String NBT_TANK = "Tank";
    private static final String NBT_MODE = "Mode";
    private static final String NBT_LIT = "Lit";

    public ExplorationLampBlockEntity(BlockPos pos, BlockState state) {
        super(TIBAMBlockEntities.EXPLORATION_LAMP.get(), pos, state);
        this.tank = new InventoryFluidTank(250, fluid -> LampFuel.get((Fluid)fluid.getFluid(), (BlockState)state) != null, (FluidTankCallback)this);
        this.fluidHolder = this.tank;
    }

    public void fluidTankChanged() {
        this.markForSync();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ExplorationLampBlockEntity lamp) {
        if (level.isClientSide()) {
            return;
        }
        if (lamp.pendingLitRestore) {
            lamp.pendingLitRestore = false;
            if (!lamp.tank.isEmpty()) {
                BlockState litState = (BlockState)state.setValue((Property)ExplorationLampBlock.LIT, Boolean.valueOf(true));
                level.setBlockAndUpdate(pos, litState);
                if (level instanceof ServerLevel) {
                    ServerLevel serverLevel = (ServerLevel)level;
                    ExplorationLampBeam.sync(serverLevel, pos, litState);
                }
            }
            return;
        }
        if (!((Boolean)state.getValue((Property)ExplorationLampBlock.LIT)).booleanValue()) {
            lamp.liveTicksSinceDrain = 0;
            return;
        }
        LampFuel fuel = lamp.getFuel();
        if (fuel == null) {
            lamp.extinguish(level, pos, state);
            return;
        }
        if (fuel.burnRate() == -1) {
            return;
        }
        ++lamp.liveTicksSinceDrain;
        if (lamp.liveTicksSinceDrain >= fuel.burnRate()) {
            lamp.liveTicksSinceDrain = 0;
            lamp.tank.drain(1, IFluidHandler.FluidAction.EXECUTE);
            if (lamp.tank.getFluidAmount() <= 0) {
                lamp.extinguish(level, pos, state);
            }
        }
    }

    private void extinguish(Level level, BlockPos pos, BlockState state) {
        BlockState newState = (BlockState)state.setValue((Property)ExplorationLampBlock.LIT, Boolean.valueOf(false));
        level.setBlockAndUpdate(pos, newState);
        this.markForSync();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            ExplorationLampBeam.sync(serverLevel, pos, newState);
        }
    }

    public void ignite(Level level, BlockPos pos, BlockState state, @Nullable Player player, ItemStack stack) {
        if (level.isClientSide) {
            return;
        }
        BlockState litState = (BlockState)state.setValue((Property)ExplorationLampBlock.LIT, Boolean.valueOf(true));
        level.setBlockAndUpdate(pos, litState);
        this.markForSync();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            ExplorationLampBeam.sync(serverLevel, pos, litState);
        }
        if (player != null && !stack.isEmpty()) {
            stack.hurtAndBreak(1, player, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
        }
        level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0f, 1.0f);
    }

    public boolean isAwaitingIgnition(BlockState state) {
        return !this.getFluid().isEmpty() && (Boolean)state.getValue((Property)ExplorationLampBlock.LIT) == false;
    }

    @Nullable
    public LampFuel getFuel() {
        FluidStack fluid = this.tank.getFluid();
        if (fluid.isEmpty()) {
            return null;
        }
        return ExplorationLampBlockEntity.resolveFuel(fluid, this.getBlockState());
    }

    @Nullable
    private static LampFuel resolveFuel(FluidStack fluidStack, BlockState state) {
        for (LampFuel candidate : LampFuel.MANAGER.getValues()) {
            if (!candidate.fluid().test(fluidStack) || !candidate.lamps().test(state)) continue;
            return candidate;
        }
        return null;
    }

    public Mode getMode() {
        return this.mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
        this.markForSync();
    }

    public void cycleMode() {
        this.setMode(this.mode.next());
    }

    public FluidStack getFluid() {
        return this.tank.getFluid();
    }

    public int getFluidAmount() {
        return this.tank.getFluidAmount();
    }

    public void setModeFromItem(ItemStack stack) {
        if (tfcmt.tfcitembarrelsandmore.common.MaritimeItemData.has(stack) && tfcmt.tfcitembarrelsandmore.common.MaritimeItemData.get(stack).contains("LampMode")) {
            String name = tfcmt.tfcitembarrelsandmore.common.MaritimeItemData.get(stack).getString("LampMode");
            for (Mode m : Mode.values()) {
                if (!m.getSerializedName().equals(name)) continue;
                this.mode = m;
                return;
            }
        }
    }

    public void saveModeToItem(ItemStack stack) {
        tfcmt.tfcitembarrelsandmore.common.MaritimeItemData.update(stack, data -> data.putString("LampMode", this.mode.getSerializedName()));
    }

    public void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.tank.readFromNBT(registries, tag.getCompound(NBT_TANK));
        if (tag.contains(NBT_MODE)) {
            String name = tag.getString(NBT_MODE);
            for (Mode m : Mode.values()) {
                if (!m.getSerializedName().equals(name)) continue;
                this.mode = m;
                break;
            }
        }
        this.pendingLitRestore = tag.getBoolean(NBT_LIT) && !this.tank.isEmpty();
    }

    public void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put(NBT_TANK, (Tag)this.tank.writeToNBT(registries, new CompoundTag()));
        tag.putString(NBT_MODE, this.mode.getSerializedName());
        BlockState state = this.getBlockState();
        boolean lit = state.hasProperty((Property)ExplorationLampBlock.LIT) && (Boolean)state.getValue((Property)ExplorationLampBlock.LIT) != false;
        tag.putBoolean(NBT_LIT, lit);
    }





    public static enum Mode implements StringRepresentable
    {
        AREA("area"),
        SEARCH("search");

        private final String name;

        private Mode(String name) {
            this.name = name;
        }

        public String getSerializedName() {
            return this.name;
        }

        public Mode next() {
            return this == AREA ? SEARCH : AREA;
        }
    }

    public net.neoforged.neoforge.fluids.capability.IFluidHandler maritimeFluids(net.minecraft.core.Direction side) { return this.tank; }
}

