package tfcmt.tfcitembarrelsandmore.common.blockentities;

import java.util.Iterator;
import javax.annotation.Nonnull;
import net.dries007.tfc.common.blockentities.BarrelBlockEntity;
import net.dries007.tfc.common.blockentities.TickableInventoryBlockEntity;
import net.dries007.tfc.common.blockentities.rotation.RotationSinkBlockEntity;
import net.dries007.tfc.util.rotation.NetworkAction;
import net.dries007.tfc.util.rotation.Node;
import net.dries007.tfc.util.rotation.Rotation;
import net.dries007.tfc.util.rotation.SinkNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;
import org.jetbrains.annotations.Nullable;
import tfcmt.tfcitembarrelsandmore.common.ITollableSink;
import tfcmt.tfcitembarrelsandmore.common.TIBAMBlockEntities;
import tfcmt.tfcitembarrelsandmore.common.TIBAMConfig;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.ExpellerPressBlock;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.GrinderBlock;
import tfcmt.tfcitembarrelsandmore.common.container.ExpellerPressContainer;
import tfcmt.tfcitembarrelsandmore.common.items.PressScrewItem;
import tfcmt.tfcitembarrelsandmore.common.recipes.ExpellerPressRecipe;

public class ExpellerPressBlockEntity
extends TickableInventoryBlockEntity<ItemStackHandler>
implements RotationSinkBlockEntity,
ITollableSink {
    public static final int PRESS_TICKS = 600;
    public static final int TANK_CAPACITY = 1000;
    public static final int DRAIN_RATE = 1;
    public static final int DRAIN_INTERVAL = 2;
    public static final float BASE_SPEED = 0.5f;
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_SCREW = 1;
    public static final int SLOT_OUTPUT = 2;
    private static final Component NAME = Component.translatable((String)"block.tfc_item_barrels_and_more.expeller_press");
    private final FluidTank outputTank = new FluidTank(1000){

        public boolean isFluidValid(FluidStack stack) {
            return !stack.isEmpty();
        }

        protected void onContentsChanged() {
            ExpellerPressBlockEntity.this.setChanged();
            ExpellerPressBlockEntity.this.markForSync();
        }
    };
    private final IFluidHandler fluidHandler = this.outputTank;
    private IItemHandler outputCap = new RangedWrapper(this.inventory, 2, 3);
    private SinkNode sinkNode;
    private float pressProgress = 0.0f;
    private int drainTimer = 0;
    private int dripTimer = 0;
    private boolean invalidInNetwork = false;

    public void markAsInvalidInNetwork() {
        this.invalidInNetwork = true;
    }

    public boolean isInvalidInNetwork() {
        return this.invalidInNetwork;
    }

    public Node getRotationNode() {
        return this.sinkNode;
    }

    public ExpellerPressBlockEntity(BlockPos pos, BlockState state) {
        super((BlockEntityType)TIBAMBlockEntities.EXPELLER_PRESS.get(), pos, state, self -> new ItemStackHandler(3){

            public boolean isItemValid(int slot, ItemStack stack) {
                return switch (slot) {
                    case 0 -> {
                        if (self.getLevel() != null && ExpellerPressRecipe.getRecipe(self.getLevel(), stack) != null) {
                            yield true;
                        }
                        yield false;
                    }
                    case 1 -> stack.getItem() instanceof PressScrewItem;
                    case 2 -> false;
                    default -> false;
                };
            }

            public int getSlotLimit(int slot) {
                return slot == 1 ? 1 : 64;
            }

            protected void onContentsChanged(int slot) {
                self.setChanged();
                self.markForSync();
                if (slot == 1) {
                    ((ExpellerPressBlockEntity)self).setScrewState();
                }
            }
        }, NAME.getString());
    }





    private void syncRotatingState() {
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        boolean nowRotating = this.sinkNode != null && this.sinkNode.isConnectedToNetwork() && this.getBlockState().getValue(ExpellerPressBlock.SCREW) != GrinderBlock.ScrewType.NONE;
        BlockState current = this.getBlockState();
        if ((Boolean)current.getValue((Property)ExpellerPressBlock.ROTATING) != nowRotating) {
            this.level.setBlock(this.worldPosition, (BlockState)current.setValue((Property)ExpellerPressBlock.ROTATING, (Comparable)Boolean.valueOf(nowRotating)), 3);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, ExpellerPressBlockEntity press) {
        if (press.sinkNode == null) {
            return;
        }
        Rotation rotation = press.sinkNode.rotation();
        if (rotation instanceof Rotation.Tickable) {
            Rotation.Tickable tickable = (Rotation.Tickable)rotation;
            tickable.tick();
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ExpellerPressBlockEntity press) {
        ExpellerPressRecipe activeRecipe;
        int ticksNeeded;
        press.destroyIfInvalid(level, pos);
        press.syncRotatingState();
        press.checkForLastTickSync();
        ++press.drainTimer;
        if (press.drainTimer >= 2) {
            press.drainTimer = 0;
            press.tryDrainToBarrel();
        }
        if (press.sinkNode == null || press.sinkNode.rotation() == null) {
            return;
        }
        if (((ItemStackHandler)press.inventory).getStackInSlot(1).isEmpty()) {
            return;
        }
        if (((ItemStackHandler)press.inventory).getStackInSlot(0).isEmpty()) {
            return;
        }
        if (press.outputTank.getFluidAmount() >= press.outputTank.getCapacity()) {
            return;
        }
        float speed = Math.abs(press.getRotationSpeed());
        float increment = speed > 0.0f ? speed / 0.5f : 1.0f;
        press.pressProgress += increment;
        ++press.dripTimer;
        if (press.dripTimer >= 40) {
            press.dripTimer = 0;
            level.playSound(null, pos, SoundEvents.HONEY_BLOCK_FALL, SoundSource.BLOCKS, 0.4f, 0.8f + level.getRandom().nextFloat() * 0.3f);
        }
        int n = ticksNeeded = (activeRecipe = ExpellerPressRecipe.getRecipe(level, ((ItemStackHandler)press.inventory).getStackInSlot(0))) != null ? activeRecipe.getPressTicks() : 600;
        if (press.pressProgress >= (float)ticksNeeded) {
            press.pressProgress = 0.0f;
            press.dripTimer = 0;
            press.tryPress();
        }
    }

    private void tryPress() {
        int actualAmount;
        if (this.level == null) {
            return;
        }
        ItemStack input = ((ItemStackHandler)this.inventory).getStackInSlot(0);
        ItemStack screw = ((ItemStackHandler)this.inventory).getStackInSlot(1);
        if (input.isEmpty() || screw.isEmpty()) {
            return;
        }
        ExpellerPressRecipe recipe = ExpellerPressRecipe.getRecipe(this.level, input);
        if (recipe == null) {
            return;
        }
        FluidStack resultFluid = recipe.getResultFluid();
        if (resultFluid.isEmpty()) {
            return;
        }
        float speed = Math.abs(this.getRotationSpeed());
        int maxAmount = resultFluid.getAmount();
        int minAmount = recipe.getResultFluidMin();
        if (speed <= 0.5f || maxAmount == minAmount) {
            actualAmount = maxAmount;
        } else {
            float maxSpeed = TIBAMConfig.COMMON.pressMaxSpeedRadPerTick();
            float t = Math.min((speed - 0.5f) / Math.max(maxSpeed - 0.5f, 0.001f), 1.0f);
            actualAmount = Math.round((float)maxAmount + t * (float)(minAmount - maxAmount));
            actualAmount = Math.max(minAmount, Math.min(maxAmount, actualAmount));
        }
        FluidStack scaledFluid = new FluidStack(resultFluid.getFluid(), actualAmount);
        if (!this.outputTank.isEmpty() && !this.outputTank.getFluid().getFluid().isSame(scaledFluid.getFluid())) {
            return;
        }
        int simFill = this.outputTank.fill(scaledFluid, IFluidHandler.FluidAction.SIMULATE);
        if (simFill < scaledFluid.getAmount()) {
            return;
        }
        if (recipe.hasResultItem()) {
            ItemStack byproduct = recipe.getResultItem(this.level.registryAccess());
            ItemStack existing = ((ItemStackHandler)this.inventory).getStackInSlot(2);
            if (existing.isEmpty()) {
                ((ItemStackHandler)this.inventory).setStackInSlot(2, byproduct);
            } else if (ItemStack.isSameItemSameComponents((ItemStack)existing, (ItemStack)byproduct) && existing.getCount() + byproduct.getCount() <= existing.getMaxStackSize()) {
                existing.grow(byproduct.getCount());
            } else {
                return;
            }
        }
        this.outputTank.fill(scaledFluid, IFluidHandler.FluidAction.EXECUTE);
        input.shrink(1);
        this.damageScrew(screw);
        this.setChanged();
        this.markForSync();
    }

    private void tryDrainToBarrel() {
        Direction face;
        if (this.level == null || this.outputTank.isEmpty()) {
            return;
        }
        Iterator iterator = Direction.Plane.HORIZONTAL.iterator();
        while (iterator.hasNext() && !this.tryDrainFace(face = (Direction)iterator.next())) {
        }
    }

    private boolean tryDrainFace(Direction face) {
        BlockPos neighbor = this.worldPosition.relative(face);
        if (!(this.level.getBlockEntity(neighbor) instanceof BarrelBlockEntity)) {
            return false;
        }
        IFluidHandler cap = this.level.getCapability(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK, neighbor, face.getOpposite());
        if (cap == null) {
            return false;
        }
        IFluidHandler barrelFluid = cap;
        if (barrelFluid == null) {
            return false;
        }
        Fluid fluidType = this.outputTank.getFluid().getFluid();
        FluidStack toDrain = new FluidStack(fluidType, 1);
        int accepted = barrelFluid.fill(toDrain, IFluidHandler.FluidAction.SIMULATE);
        if (accepted <= 0) {
            return false;
        }
        this.outputTank.drain(accepted, IFluidHandler.FluidAction.EXECUTE);
        barrelFluid.fill(new FluidStack(fluidType, accepted), IFluidHandler.FluidAction.EXECUTE);
        this.setChanged();
        return true;
    }

    private void damageScrew(ItemStack screw) {
        int dmg = screw.getDamageValue() + 1;
        if (dmg >= screw.getMaxDamage()) {
            ((ItemStackHandler)this.inventory).setStackInSlot(1, ItemStack.EMPTY);
            this.setScrewState();
            if (this.level != null) {
                this.level.playSound(null, this.worldPosition, SoundEvents.ITEM_BREAK, SoundSource.BLOCKS, 1.0f, 1.0f);
            }
        } else {
            screw.setDamageValue(dmg);
        }
    }

    private void setScrewState() {
        BlockState current;
        Item item;
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        ItemStack screw = ((ItemStackHandler)this.inventory).getStackInSlot(1);
        GrinderBlock.ScrewType type = GrinderBlock.ScrewType.NONE;
        if (!screw.isEmpty() && (item = screw.getItem()) instanceof PressScrewItem) {
            PressScrewItem screwItem = (PressScrewItem)item;
            type = screwItem.getScrewType();
        }
        if ((current = this.getBlockState()).getValue(ExpellerPressBlock.SCREW) != type) {
            BlockState next = (BlockState)current.setValue(ExpellerPressBlock.SCREW, type);
            if (type == GrinderBlock.ScrewType.NONE) {
                next = (BlockState)next.setValue((Property)ExpellerPressBlock.ROTATING, (Comparable)Boolean.valueOf(false));
            }
            this.level.setBlock(this.worldPosition, next, 3);
        }
    }

    public boolean acceptPaste(ItemStack item) {
        if (this.level == null || ExpellerPressRecipe.getRecipe(this.level, item) == null) {
            return false;
        }
        ItemStack current = ((ItemStackHandler)this.inventory).getStackInSlot(0);
        if (current.isEmpty()) {
            ((ItemStackHandler)this.inventory).setStackInSlot(0, item.copy());
            return true;
        }
        if (ItemStack.isSameItem((ItemStack)current, (ItemStack)item) && current.getCount() < current.getMaxStackSize()) {
            current.grow(1);
            return true;
        }
        return false;
    }

    @javax.annotation.Nullable
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInv, Player player) {
        return ExpellerPressContainer.create(this, playerInv, windowId);
    }

    public ItemStackHandler getInventory() {
        return (ItemStackHandler)this.inventory;
    }

    public FluidTank getOutputTank() {
        return this.outputTank;
    }

    public float getPressProgress() {
        return this.pressProgress;
    }

    @Override
    public boolean isTollableWorking() {
        return this.pressProgress > 0.0f;
    }

    public int getPressTicks() {
        if (this.level == null) {
            return 600;
        }
        ItemStack input = ((ItemStackHandler)this.inventory).getStackInSlot(0);
        if (input.isEmpty()) {
            return 600;
        }
        ExpellerPressRecipe recipe = ExpellerPressRecipe.getRecipe(this.level, input);
        return recipe != null ? recipe.getPressTicks() : 600;
    }

    public boolean isRotating() {
        return this.sinkNode != null && this.sinkNode.rotation() != null;
    }

    public boolean isConnectedToNetwork() {
        return this.sinkNode != null && this.sinkNode.isConnectedToNetwork() && !((ItemStackHandler)this.inventory).getStackInSlot(1).isEmpty();
    }

    public float getRotationAngle(float partialTick) {
        if (this.sinkNode == null) {
            return 0.0f;
        }
        Rotation rotation = this.sinkNode.rotation();
        return rotation != null ? rotation.angle(partialTick) : 0.0f;
    }

    public @Nullable Rotation getSinkRotation() {
        return this.sinkNode != null ? this.sinkNode.rotation() : null;
    }

    public float getRotationSpeed() {
        if (this.sinkNode == null) {
            return 0.0f;
        }
        Rotation rotation = this.sinkNode.rotation();
        return rotation != null ? rotation.speed() : 0.0f;
    }

    public Direction.Axis getAxleAxis() {
        if (this.sinkNode == null) {
            return Direction.Axis.X;
        }
        Direction source = this.sinkNode.source();
        return source != null ? source.getAxis() : Direction.Axis.X;
    }

    public boolean isAxleFromPositiveEnd() {
        if (this.sinkNode == null) {
            return false;
        }
        Direction source = this.sinkNode.source();
        return source != null && source.getAxisDirection() == Direction.AxisDirection.POSITIVE;
    }

    public Direction getRotationDirection() {
        if (this.sinkNode == null) {
            return null;
        }
        Rotation rotation = this.sinkNode.rotation();
        return rotation != null ? rotation.direction() : null;
    }

    public void onLoaded() {
        if (this.level == null) {
            return;
        }
        Direction facing = (Direction)this.getBlockState().getValue((Property)ExpellerPressBlock.FACING);
        this.sinkNode = new SinkNode(this.worldPosition, facing.getOpposite()){};
        this.performNetworkAction(NetworkAction.ADD);
    }

    public void onUnloaded() {
        if (this.level == null || this.sinkNode == null) {
            return;
        }
        if (!this.level.isClientSide) {
            this.performNetworkAction(NetworkAction.REMOVE);
        }
        this.sinkNode = null;
    }

    public void onNeighbourChanged() {
        if (this.level == null || this.level.isClientSide || this.sinkNode == null) {
            return;
        }
        this.performNetworkAction(NetworkAction.UPDATE);
    }

    protected void onLoadAdditional() {
        this.onLoaded();
    }

    protected void onUnloadAdditional() {
        this.onUnloaded();
    }

    public void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.pressProgress = tag.getFloat("PressProgress");
        this.drainTimer = tag.getInt("DrainTimer");
        this.dripTimer = tag.getInt("DripTimer");
        this.invalidInNetwork = tag.getBoolean("InvalidInNetwork");
        this.outputTank.readFromNBT(registries, tag.getCompound("OutputTank"));
    }

    public void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putFloat("PressProgress", this.pressProgress);
        tag.putInt("DrainTimer", this.drainTimer);
        tag.putInt("DripTimer", this.dripTimer);
        tag.putBoolean("InvalidInNetwork", this.invalidInNetwork);
        tag.put("OutputTank", (Tag)this.outputTank.writeToNBT(registries, new CompoundTag()));
    }

    public net.neoforged.neoforge.items.IItemHandler maritimeItems(net.minecraft.core.Direction side) { return side == net.minecraft.core.Direction.DOWN ? this.outputCap : null; }
    public net.neoforged.neoforge.fluids.capability.IFluidHandler maritimeFluids(net.minecraft.core.Direction side) { return this.outputTank; }
}

