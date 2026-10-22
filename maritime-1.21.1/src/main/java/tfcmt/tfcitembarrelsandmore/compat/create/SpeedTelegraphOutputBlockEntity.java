package tfcmt.tfcitembarrelsandmore.compat.create;

import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import java.lang.ref.WeakReference;
import java.util.UUID;
import javax.annotation.Nullable;
import net.dries007.tfc.util.rotation.Rotation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import tfcmt.tfcitembarrelsandmore.common.TIBAMConfig;
import tfcmt.tfcitembarrelsandmore.common.TIBAMSounds;
import tfcmt.tfcitembarrelsandmore.common.blockentities.TripleExpansionEngineBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.TripleExpansionEngineBlock;
import tfcmt.tfcitembarrelsandmore.compat.create.CreateCompat;
import tfcmt.tfcitembarrelsandmore.compat.create.SpeedTelegraphInputBlockEntity;
import tfcmt.tfcitembarrelsandmore.compat.create.SpeedTelegraphOrder;
import tfcmt.tfcitembarrelsandmore.compat.create.SpeedTelegraphOutputRenderer;

public class SpeedTelegraphOutputBlockEntity
extends GeneratingKineticBlockEntity {
    private static final float RAD_PER_TICK_TO_RPM = 190.98593f;
    private static final float RED_TICK_SPEED = 0.12f;
    private static final float STEP_DEG = 22.0f;
    private float redAngle = 0.0f;
    private float redAnglePrev = 0.0f;
    private static final int OUTPUT_RED_DELAY_TICKS = 20;
    private static final int OUTPUT_HANDLE_DELAY_TICKS = 10;
    @Nullable
    private UUID inputUUID = null;
    @Nullable
    private BlockPos resolvedInputPos = null;
    private UUID ownUUID = UUID.randomUUID();
    private SpeedTelegraphOrder bridgeOrder = SpeedTelegraphOrder.STOP;
    private SpeedTelegraphOrder handleOrder = SpeedTelegraphOrder.STOP;
    private int redDelayTimer = 0;
    private int handleDelayTimer = 0;

    public SpeedTelegraphOutputBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.setLazyTickRate(20);
    }

    private float readTeeRpm() {
        if (this.level == null) {
            return 0.0f;
        }
        Direction myFacing = (Direction)this.getBlockState().getValue((Property)DirectionalKineticBlock.FACING);
        Direction teeDir = myFacing.getCounterClockWise();
        BlockPos teePos = this.worldPosition.relative(teeDir, 2);
        BlockEntity blockEntity = this.level.getBlockEntity(teePos);
        if (!(blockEntity instanceof TripleExpansionEngineBlockEntity)) {
            return 0.0f;
        }
        TripleExpansionEngineBlockEntity tee = (TripleExpansionEngineBlockEntity)blockEntity;
        BlockState teeState = this.level.getBlockState(teePos);
        if (teeState.getValue((Property)TripleExpansionEngineBlock.FACING) != teeDir.getOpposite() || !((Boolean)teeState.getValue((Property)TripleExpansionEngineBlock.RUNNING)).booleanValue()) {
            return 0.0f;
        }
        Rotation rotation = tee.getSourceRotation();
        return rotation != null ? Math.abs(rotation.speed()) * 190.98593f : 0.0f;
    }

    public float getGeneratedSpeed() {
        float rpm = this.readTeeRpm();
        if (rpm == 0.0f) {
            return 0.0f;
        }
        return this.handleOrder.toCreateRpm(rpm);
    }

    public float calculateAddedStressCapacity() {
        float capacity;
        float rpm = Math.abs(this.getGeneratedSpeed());
        this.lastCapacityProvided = capacity = rpm > 0.0f ? (float)((Double)TIBAMConfig.SERVER.telegraphOutputStressCapacity.get()).doubleValue() / rpm : 0.0f;
        return capacity;
    }

    public float calculateStressApplied() {
        return 0.0f;
    }

    public void lazyTick() {
        SpeedTelegraphInputBlockEntity inputBE;
        super.lazyTick();
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        if (this.resolvedInputPos == null && this.inputUUID != null && (inputBE = SpeedTelegraphInputBlockEntity.findInputByUUID(this.inputUUID)) != null) {
            this.resolvedInputPos = inputBE.getBlockPos();
            inputBE.resolveOutputPositions();
            inputBE.onOutputHandleChanged(this.handleOrder);
        }
        this.updateGeneratedRotation();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SpeedTelegraphOutputBlockEntity be) {
        be.tick();
        be.tickDelays(level);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, SpeedTelegraphOutputBlockEntity be) {
        be.tick();
        be.redAnglePrev = be.redAngle;
        float redTarget = SpeedTelegraphOutputRenderer.orderToAngle(be.bridgeOrder);
        float redGap = redTarget - be.redAngle;
        if (Math.abs(redGap) > 0.05f) {
            int currStep;
            float prev = be.redAngle;
            be.redAngle += redGap * 0.12f;
            int prevStep = Math.round((prev + 110.0f) / 22.0f);
            if (prevStep != (currStep = Math.round((be.redAngle + 110.0f) / 22.0f))) {
                level.playLocalSound((double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), (SoundEvent)TIBAMSounds.TELEGRAPH_BELL.get(), SoundSource.BLOCKS, 1.0f, 1.0f, false);
            }
        } else {
            be.redAngle = redTarget;
        }
    }

    private void tickDelays(Level level) {
        if (this.redDelayTimer > 0) {
            --this.redDelayTimer;
            if (this.redDelayTimer == 0) {
                this.handleDelayTimer = 10;
                this.setChanged();
            }
        }
        if (this.handleDelayTimer > 0) {
            --this.handleDelayTimer;
            if (this.handleDelayTimer == 0 && this.handleOrder != this.bridgeOrder) {
                this.handleOrder = this.bridgeOrder.ordinal() > this.handleOrder.ordinal() ? this.handleOrder.stepAhead() : this.handleOrder.stepAstern();
                this.updateGeneratedRotation();
                this.notifyInputHandleChanged(level);
                if (this.handleOrder != this.bridgeOrder) {
                    this.handleDelayTimer = 10;
                }
                this.setChanged();
                this.sendData();
            }
        }
    }

    private void notifyInputHandleChanged(Level level) {
        if (this.resolvedInputPos == null) {
            return;
        }
        BlockEntity blockEntity = level.getBlockEntity(this.resolvedInputPos);
        if (blockEntity instanceof SpeedTelegraphInputBlockEntity) {
            SpeedTelegraphInputBlockEntity inputBE = (SpeedTelegraphInputBlockEntity)blockEntity;
            inputBE.onOutputHandleChanged(this.handleOrder);
        }
    }

    public void onNeighbourChanged() {
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        this.updateGeneratedRotation();
    }

    public void onOrderChanged(SpeedTelegraphOrder newOrder, Level level) {
        this.bridgeOrder = newOrder;
        this.redDelayTimer = 20;
        this.setChanged();
        this.sendData();
    }

    public void linkInput(UUID linkedInputUUID, Level level) {
        this.inputUUID = linkedInputUUID;
        SpeedTelegraphInputBlockEntity inputBE = SpeedTelegraphInputBlockEntity.findInputByUUID(linkedInputUUID);
        if (inputBE != null) {
            this.resolvedInputPos = inputBE.getBlockPos();
            this.handleOrder = this.bridgeOrder = inputBE.getOrder();
            this.updateGeneratedRotation();
        }
        this.setChanged();
        this.sendData();
    }

    public void unlinkInput(Level level) {
        this.inputUUID = null;
        this.resolvedInputPos = null;
        this.bridgeOrder = SpeedTelegraphOrder.STOP;
        this.handleOrder = SpeedTelegraphOrder.STOP;
        this.redDelayTimer = 0;
        this.handleDelayTimer = 0;
        this.updateGeneratedRotation();
        this.setChanged();
        this.sendData();
    }

    public void notifyInputOnBreak(Level level) {
        if (this.resolvedInputPos != null) {
            BlockEntity blockEntity = level.getBlockEntity(this.resolvedInputPos);
            if (blockEntity instanceof SpeedTelegraphInputBlockEntity) {
                SpeedTelegraphInputBlockEntity inputBE = (SpeedTelegraphInputBlockEntity)blockEntity;
                inputBE.onOutputBroken(this.ownUUID, this.worldPosition);
            }
            this.resolvedInputPos = null;
        }
        this.inputUUID = null;
    }

    public InteractionResult onPlayerInteract(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        if (!held.isEmpty() && held.getItem() == ((Block)CreateCompat.SPEED_TELEGRAPH_INPUT.get()).asItem()) {
            CompoundTag itemTag;
            if (this.inputUUID != null) {
                player.displayClientMessage((Component)Component.translatable((String)"tibam.telegraph.already_linked"), true);
                return InteractionResult.sidedSuccess((boolean)level.isClientSide);
            }
            if (!tfcmt.tfcitembarrelsandmore.common.MaritimeItemData.has(held)) {
                tfcmt.tfcitembarrelsandmore.common.MaritimeItemData.set(held, new CompoundTag());
            }
            ListTag pendingList = (itemTag = tfcmt.tfcitembarrelsandmore.common.MaritimeItemData.get(held)).contains("PendingTelegraphOutputUUIDs", 9) ? itemTag.getList("PendingTelegraphOutputUUIDs", 8) : new ListTag();
            String thisUUID = this.ownUUID.toString();
            boolean alreadyQueued = false;
            for (int i = 0; i < pendingList.size(); ++i) {
                if (!pendingList.getString(i).equals(thisUUID)) continue;
                alreadyQueued = true;
                break;
            }
            if (!alreadyQueued) {
                pendingList.add((Object)StringTag.valueOf((String)thisUUID));
            }
            itemTag.put("PendingTelegraphOutputUUIDs", (Tag)pendingList);
            tfcmt.tfcitembarrelsandmore.common.MaritimeItemData.set(held, itemTag);
            player.displayClientMessage((Component)Component.translatable((String)"tibam.telegraph.output_registered"), true);
            return InteractionResult.sidedSuccess((boolean)level.isClientSide);
        }
        return InteractionResult.sidedSuccess((boolean)level.isClientSide);
    }

    public float getRedAnglePrev() {
        return this.redAnglePrev;
    }

    public float getRedAngle() {
        return this.redAngle;
    }

    public UUID getOwnUUID() {
        return this.ownUUID;
    }

    public boolean isLinked() {
        return this.inputUUID != null;
    }

    public SpeedTelegraphOrder getBridgeOrder() {
        return this.bridgeOrder;
    }

    public SpeedTelegraphOrder getHandleOrder() {
        return this.handleOrder;
    }

    @Deprecated
    public SpeedTelegraphOrder getDisplayOrder() {
        return this.getHandleOrder();
    }

    public void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        tag.putString("OwnUUID", this.ownUUID.toString());
        if (this.inputUUID != null) {
            tag.putString("InputUUID", this.inputUUID.toString());
        }
        tag.putInt("BridgeOrder", this.bridgeOrder.ordinal());
        tag.putInt("HandleOrder", this.handleOrder.ordinal());
        if (!clientPacket) {
            tag.putInt("RedDelayTimer", this.redDelayTimer);
            tag.putInt("HandleDelayTimer", this.handleDelayTimer);
        }
    }

    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        this.ownUUID = tag.contains("OwnUUID") ? UUID.fromString(tag.getString("OwnUUID")) : UUID.randomUUID();
        SpeedTelegraphInputBlockEntity.OUTPUT_REGISTRY.put(this.ownUUID, new WeakReference<SpeedTelegraphOutputBlockEntity>(this));
        this.resolvedInputPos = null;
        try {
            this.inputUUID = tag.contains("InputUUID") ? UUID.fromString(tag.getString("InputUUID")) : null;
        }
        catch (IllegalArgumentException e) {
            this.inputUUID = null;
        }
        this.bridgeOrder = SpeedTelegraphOrder.fromOrdinal(tag.getInt("BridgeOrder"));
        this.handleOrder = SpeedTelegraphOrder.fromOrdinal(tag.getInt("HandleOrder"));
        if (!clientPacket) {
            this.redDelayTimer = tag.getInt("RedDelayTimer");
            this.handleDelayTimer = tag.getInt("HandleDelayTimer");
        }
    }
}

