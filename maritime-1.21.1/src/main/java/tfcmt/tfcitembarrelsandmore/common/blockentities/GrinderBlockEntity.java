package tfcmt.tfcitembarrelsandmore.common.blockentities;

import java.util.List;
import javax.annotation.Nonnull;
import net.dries007.tfc.common.blockentities.TickableInventoryBlockEntity;
import net.dries007.tfc.common.blockentities.rotation.RotationSinkBlockEntity;

import net.dries007.tfc.util.rotation.NetworkAction;
import net.dries007.tfc.util.rotation.Node;
import net.dries007.tfc.util.rotation.Rotation;
import net.dries007.tfc.util.rotation.SinkNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import tfcmt.tfcitembarrelsandmore.common.ITollableSink;
import tfcmt.tfcitembarrelsandmore.common.TIBAMBlockEntities;
import tfcmt.tfcitembarrelsandmore.common.blockentities.ExpellerPressBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.GrinderBlock;
import tfcmt.tfcitembarrelsandmore.common.items.PressScrewItem;
import tfcmt.tfcitembarrelsandmore.common.recipes.GrinderRecipe;
import tfcmt.tfcitembarrelsandmore.compat.valkyrienskies.VSCompat;

public class GrinderBlockEntity
extends TickableInventoryBlockEntity<ItemStackHandler>
implements RotationSinkBlockEntity,
ITollableSink {
    private static final Logger LOGGER = LogManager.getLogger((String)"TIBAM/Grinder");
    public static final int GRIND_TICKS = 40;
    public static final float BASE_SPEED = 0.5f;
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_SCREW = 1;
    private static final int PICKUP_COOLDOWN_TICKS = 8;
    private static final Component NAME = Component.translatable((String)"block.tfc_item_barrels_and_more.grinder");
    private SinkNode sinkNode;
    private float grindProgress = 0.0f;
    private int pickupCooldown = 0;
    private boolean invalidInNetwork = false;
    private IItemHandler inputCap = new RangedWrapper(this.inventory, 0, 1);

    public void markAsInvalidInNetwork() {
        this.invalidInNetwork = true;
    }

    public boolean isInvalidInNetwork() {
        return this.invalidInNetwork;
    }

    public Node getRotationNode() {
        return this.sinkNode;
    }

    public GrinderBlockEntity(BlockPos pos, BlockState state) {
        super(TIBAMBlockEntities.GRINDER.get(), pos, state, self -> new ItemStackHandler(2){

            public boolean isItemValid(int slot, ItemStack stack) {
                if (slot == 1) {
                    return stack.getItem() instanceof PressScrewItem;
                }
                if (slot == 0) {
                    if (self.getLevel() == null) {
                        return false;
                    }
                    return GrinderRecipe.getRecipe(self.getLevel(), stack) != null;
                }
                return false;
            }

            protected void onContentsChanged(int slot) {
                self.setChanged();
                self.markForSync();
            }
        }, NAME.getString());
    }





    public InteractionResult onPlayerInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        boolean clientSide = player.level().isClientSide;
        if (held.getItem() instanceof PressScrewItem && ((ItemStackHandler)this.inventory).getStackInSlot(1).isEmpty()) {
            if (!clientSide) {
                ItemStack toInsert = held.copy();
                toInsert.setCount(1);
                ItemStack remainder = ((ItemStackHandler)this.inventory).insertItem(1, toInsert, false);
                if (remainder.isEmpty()) {
                    held.shrink(1);
                }
                this.setChanged();
                this.markForSync();
                this.setScrewState();
            }
            return InteractionResult.sidedSuccess((boolean)clientSide);
        }
        if (player.isShiftKeyDown() && held.isEmpty()) {
            if (!clientSide) {
                ItemStack screw = ((ItemStackHandler)this.inventory).getStackInSlot(1);
                if (!screw.isEmpty()) {
                    GrinderBlockEntity.ejectToPlayer(player, screw.copy());
                    ((ItemStackHandler)this.inventory).setStackInSlot(1, ItemStack.EMPTY);
                    this.setChanged();
                    this.markForSync();
                    this.setScrewState();
                    return InteractionResult.sidedSuccess((boolean)false);
                }
                ItemStack input = ((ItemStackHandler)this.inventory).getStackInSlot(0);
                if (!input.isEmpty()) {
                    GrinderBlockEntity.ejectToPlayer(player, input.copy());
                    ((ItemStackHandler)this.inventory).setStackInSlot(0, ItemStack.EMPTY);
                    this.setChanged();
                }
            }
            return InteractionResult.sidedSuccess((boolean)clientSide);
        }
        if (!held.isEmpty() && ((ItemStackHandler)this.inventory).isItemValid(0, held)) {
            ItemStack remainder;
            if (!clientSide && (remainder = ((ItemStackHandler)this.inventory).insertItem(0, held.copy().split(1), false)).isEmpty()) {
                held.shrink(1);
            }
            return InteractionResult.sidedSuccess((boolean)clientSide);
        }
        return InteractionResult.PASS;
    }

    private static void ejectToPlayer(Player player, ItemStack stack) {
        if (!player.addItem(stack)) {
            player.drop(stack, false);
        }
    }

    public boolean hasScrew() {
        return !((ItemStackHandler)this.inventory).getStackInSlot(1).isEmpty();
    }

    public ItemStack getScrewStack() {
        return ((ItemStackHandler)this.inventory).getStackInSlot(1);
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
        if ((current = this.getBlockState()).getValue(GrinderBlock.SCREW) != type) {
            BlockState next = (BlockState)current.setValue(GrinderBlock.SCREW, type);
            if (type == GrinderBlock.ScrewType.NONE) {
                next = (BlockState)next.setValue((Property)GrinderBlock.ROTATING, Boolean.valueOf(false));
            }
            this.level.setBlock(this.worldPosition, next, 3);
        }
    }

    private void syncRotatingState() {
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        boolean nowRotating = this.sinkNode != null && this.sinkNode.isConnectedToNetwork() && this.getBlockState().getValue(GrinderBlock.SCREW) != GrinderBlock.ScrewType.NONE;
        BlockState current = this.getBlockState();
        if ((Boolean)current.getValue((Property)GrinderBlock.ROTATING) != nowRotating) {
            this.level.setBlock(this.worldPosition, (BlockState)current.setValue((Property)GrinderBlock.ROTATING, Boolean.valueOf(nowRotating)), 3);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, GrinderBlockEntity grinder) {
        ItemStack result;
        Rotation rotation;
        if (grinder.sinkNode != null && (rotation = grinder.sinkNode.rotation()) instanceof Rotation.Tickable) {
            Rotation.Tickable tickable = (Rotation.Tickable)rotation;
            tickable.tick();
        }
        if (!grinder.isRotating()) {
            return;
        }
        ItemStack input = grinder.getInputItem();
        if (input.isEmpty()) {
            return;
        }
        if (level.getGameTime() % 4L != 0L) {
            return;
        }
        ItemStack particleStack = input;
        GrinderRecipe recipe = GrinderRecipe.getRecipe(level, input);
        if (recipe != null && !(result = recipe.assemble(input)).isEmpty()) {
            particleStack = result;
        }
        RandomSource rand = level.getRandom();
        for (int i = 0; i < 2; ++i) {
            double px = (double)pos.getX() + 0.3 + rand.nextDouble() * 0.4;
            double py = (double)pos.getY() + 1.1;
            double pz = (double)pos.getZ() + 0.3 + rand.nextDouble() * 0.4;
            double vx = (rand.nextDouble() - 0.5) * 0.04;
            double vy = rand.nextDouble() * 0.04 + 0.02;
            double vz = (rand.nextDouble() - 0.5) * 0.04;
            level.addParticle((ParticleOptions)new ItemParticleOption(ParticleTypes.ITEM, particleStack), px, py, pz, vx, vy, vz);
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, GrinderBlockEntity grinder) {
        grinder.destroyIfInvalid(level, pos);
        grinder.pickUpItems(level, pos);
        grinder.syncRotatingState();
        if (grinder.sinkNode == null || grinder.sinkNode.rotation() == null) {
            grinder.grindProgress = 0.0f;
            return;
        }
        if (!grinder.hasScrew()) {
            grinder.grindProgress = 0.0f;
            return;
        }
        grinder.checkForLastTickSync();
        float speed = Math.abs(grinder.getRotationSpeed());
        float increment = speed > 0.0f ? speed / 0.5f : 0.0f;
        grinder.grindProgress += increment;
        if (grinder.grindProgress >= 40.0f) {
            grinder.grindProgress = 0.0f;
            grinder.tryGrind();
        }
    }

    private void pickUpItems(Level level, BlockPos pos) {
        if (this.pickupCooldown > 0) {
            --this.pickupCooldown;
            return;
        }
        this.pickupCooldown = 8;
        ItemStack existing = ((ItemStackHandler)this.inventory).getStackInSlot(0);
        if (!existing.isEmpty() && existing.getCount() >= existing.getMaxStackSize()) {
            return;
        }
        AABB scanBox = new AABB((double)pos.getX(), (double)pos.getY() + 1.0, (double)pos.getZ(), (double)pos.getX() + 1.0, (double)pos.getY() + 2.0, (double)pos.getZ() + 1.0);
        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, scanBox);
        for (ItemEntity itemEntity : items) {
            GrinderRecipe recipe;
            ItemStack entityStack = itemEntity.getItem();
            if (entityStack.isEmpty() || (recipe = GrinderRecipe.getRecipe(level, entityStack)) == null) continue;
            ItemStack remainder = ((ItemStackHandler)this.inventory).insertItem(0, entityStack.copy(), false);
            int accepted = entityStack.getCount() - remainder.getCount();
            if (accepted <= 0) continue;
            entityStack.shrink(accepted);
            if (entityStack.isEmpty()) {
                itemEntity.discard();
            } else {
                itemEntity.setItem(entityStack);
            }
            this.setChanged();
            break;
        }
    }

    private void tryGrind() {
        if (this.level == null) {
            return;
        }
        ItemStack input = ((ItemStackHandler)this.inventory).getStackInSlot(0);
        if (input.isEmpty()) {
            return;
        }
        GrinderRecipe recipe = GrinderRecipe.getRecipe(this.level, input);
        if (recipe == null) {
            return;
        }
        ItemStack result = recipe.assemble(input);
        if (result.isEmpty()) {
            return;
        }
        this.damageScrewByOne();
        this.pushOrDrop(result);
        ((ItemStackHandler)this.inventory).extractItem(0, 1, false);
    }

    private void damageScrewByOne() {
        if (this.level == null) {
            return;
        }
        ItemStack screw = ((ItemStackHandler)this.inventory).getStackInSlot(1);
        if (screw.isEmpty()) {
            return;
        }
        int dmg = screw.getDamageValue() + 1;
        if (dmg >= screw.getMaxDamage()) {
            ((ItemStackHandler)this.inventory).setStackInSlot(1, ItemStack.EMPTY);
            this.level.playSound(null, this.worldPosition, SoundEvents.ITEM_BREAK, SoundSource.BLOCKS, 0.8f, 1.2f);
            this.setScrewState();
            LOGGER.debug("[grind] screw broke at {}", (Object)this.worldPosition);
        } else {
            screw.setDamageValue(dmg);
            ((ItemStackHandler)this.inventory).setStackInSlot(1, screw);
        }
        this.setChanged();
        this.markForSync();
    }

    private void pushOrDrop(ItemStack result) {
        net.neoforged.neoforge.items.IItemHandler cap;
        ExpellerPressBlockEntity press;
        if (this.level == null) {
            return;
        }
        Direction facing = (Direction)this.getBlockState().getValue((Property)GrinderBlock.FACING);
        BlockPos facingPos = this.worldPosition.relative(facing);
        BlockEntity blockEntity = this.level.getBlockEntity(facingPos);
        if (blockEntity instanceof ExpellerPressBlockEntity && (press = (ExpellerPressBlockEntity)blockEntity).acceptPaste(result.copy())) {
            return;
        }
        BlockEntity facingBE = this.level.getBlockEntity(facingPos);
        if (facingBE != null && (cap = this.level.getCapability(net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK, facingPos, facing.getOpposite())) != null) {
            IItemHandler handler = cap;
            ItemStack remainder = ItemHandlerHelper.insertItem((IItemHandler)handler, (ItemStack)result.copy(), (boolean)false);
            if (remainder.isEmpty()) {
                return;
            }
            result = remainder;
        }
        double x = (double)this.worldPosition.getX() + 0.5 + (double)facing.getStepX() * 0.6;
        double y = (double)this.worldPosition.getY() + 0.5 + (double)facing.getStepY() * 0.6;
        double z = (double)this.worldPosition.getZ() + 0.5 + (double)facing.getStepZ() * 0.6;
        Vec3 dropPos = VSCompat.spawnPosToWorld(this.level, this.worldPosition, x, y, z);
        ItemEntity entity = new ItemEntity(this.level, dropPos.x, dropPos.y, dropPos.z, result.copy());
        entity.setDeltaMovement((double)facing.getStepX() * 0.05, (double)facing.getStepY() * 0.05 + 0.05, (double)facing.getStepZ() * 0.05);
        this.level.addFreshEntity((Entity)entity);
    }

    public ItemStack getInputItem() {
        return ((ItemStackHandler)this.inventory).getStackInSlot(0);
    }

    public float getGrindProgress() {
        return this.grindProgress;
    }

    @Override
    public boolean isTollableWorking() {
        return this.grindProgress > 0.0f;
    }

    public ItemStackHandler getInventory() {
        return (ItemStackHandler)this.inventory;
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
        Direction crankFace = ((Direction)this.getBlockState().getValue((Property)GrinderBlock.FACING)).getOpposite();
        this.sinkNode = new SinkNode(this.worldPosition, crankFace){};
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
        this.grindProgress = tag.getFloat("GrindProgress");
        this.invalidInNetwork = tag.getBoolean("InvalidInNetwork");
    }

    public void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putFloat("GrindProgress", this.grindProgress);
        tag.putBoolean("InvalidInNetwork", this.invalidInNetwork);
    }

    public net.neoforged.neoforge.items.IItemHandler maritimeItems(net.minecraft.core.Direction side) { return side == net.minecraft.core.Direction.UP ? this.inputCap : null; }
}

