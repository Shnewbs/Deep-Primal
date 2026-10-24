package tfcmt.tfcitembarrelsandmore.compat.create;

import java.lang.ref.WeakReference;
import java.util.EnumSet;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.dries007.tfc.common.blockentities.TFCBlockEntity;
import net.dries007.tfc.common.blockentities.rotation.RotatingBlockEntity;
import net.dries007.tfc.util.rotation.NetworkAction;
import net.dries007.tfc.util.rotation.Node;
import net.dries007.tfc.util.rotation.Rotation;
import net.dries007.tfc.util.rotation.RotationNetworkManager;
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
import tfcmt.tfcitembarrelsandmore.common.TIBAMBlocks;
import tfcmt.tfcitembarrelsandmore.common.TIBAMSounds;
import tfcmt.tfcitembarrelsandmore.compat.create.SpeedTelegraphOrder;
import tfcmt.tfcitembarrelsandmore.compat.create.SpeedTelegraphOutputRenderer;
import tfcmt.tfcitembarrelsandmore.compat.create.TFCSpeedTelegraphInputBlockEntity;
import tfcmt.tfcitembarrelsandmore.compat.create.TFCSpeedTelegraphOutputBlock;

public class TFCSpeedTelegraphOutputBlockEntity
extends TFCBlockEntity
implements RotatingBlockEntity {
    private static final Component NAME = Component.translatable((String)"block.tfc_item_barrels_and_more.tfc_speed_telegraph_output");
    private final Node node;
    private final Rotation.Tickable outputRotation;
    private boolean invalidInNetwork = false;
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
    private float outSpeed = 0.0f;
    private int lazyResolveCounter = 0;

    public void markAsInvalidInNetwork() {
        this.invalidInNetwork = true;
    }

    public boolean isInvalidInNetwork() {
        return this.invalidInNetwork;
    }

    public Node getRotationNode() {
        return this.node;
    }

    public TFCSpeedTelegraphOutputBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        Direction facing = (Direction)state.getValue((Property)TFCSpeedTelegraphOutputBlock.FACING);
        final Direction outputFace = facing.getClockWise();
        final Direction inputFace = facing.getCounterClockWise();
        this.outputRotation = Rotation.of((Direction)outputFace, (float)0.0f);
        this.node = new Node(pos, EnumSet.of(inputFace, outputFace)){

            public Rotation rotation(Rotation sourceRotation, Direction sourceDirection, Direction exitDirection) {
                return sourceDirection == inputFace && exitDirection == outputFace ? TFCSpeedTelegraphOutputBlockEntity.this.outputRotation : sourceRotation;
            }
        };
    }

    protected void onLoadAdditional() {
        RotationNetworkManager.get((Level)this.level).performAction(this.node, NetworkAction.ADD);
    }

    public void onNeighbourChanged() {
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        RotationNetworkManager.get((Level)this.level).performAction(this.node, NetworkAction.UPDATE);
    }

    protected void onUnloadAdditional() {
        this.performNetworkAction(NetworkAction.REMOVE);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TFCSpeedTelegraphOutputBlockEntity be) {
        be.tickDelays(level);
        float newOutSpeed = 0.0f;
        Rotation incoming = be.node.rotation();
        if (incoming != null) {
            float inSpeed = incoming.speed();
            float fraction = be.handleOrder.getRpmFraction();
            float sign = be.handleOrder.isForward() ? 1.0f : -1.0f;
            newOutSpeed = inSpeed * fraction * sign;
        }
        be.outputRotation.setSpeed(newOutSpeed);
        if (Math.abs(newOutSpeed - be.outSpeed) > 0.001f) {
            be.outSpeed = newOutSpeed;
            be.markForSync();
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, TFCSpeedTelegraphOutputBlockEntity be) {
        be.outputRotation.setSpeed(be.outSpeed);
        be.outputRotation.tick();
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

    public void lazyResolve() {
        if (this.resolvedInputPos != null || this.inputUUID == null || this.level == null || this.level.isClientSide) {
            return;
        }
        TFCSpeedTelegraphInputBlockEntity inputBE = TFCSpeedTelegraphInputBlockEntity.findTFCInputByUUID(this.inputUUID);
        if (inputBE != null) {
            this.resolvedInputPos = inputBE.getBlockPos();
            inputBE.resolveTFCOutputPositions();
            inputBE.onTFCOutputHandleChanged(this.handleOrder);
        }
    }

    private void tickDelays(Level level) {
        if (++this.lazyResolveCounter >= 20) {
            this.lazyResolveCounter = 0;
            this.lazyResolve();
        }
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
                this.notifyInputHandleChanged(level);
                if (this.handleOrder != this.bridgeOrder) {
                    this.handleDelayTimer = 10;
                }
                this.setChanged();
                this.markForSync();
            }
        }
    }

    private void notifyInputHandleChanged(Level level) {
        if (this.resolvedInputPos == null) {
            return;
        }
        BlockEntity blockEntity = level.getBlockEntity(this.resolvedInputPos);
        if (blockEntity instanceof TFCSpeedTelegraphInputBlockEntity) {
            TFCSpeedTelegraphInputBlockEntity inputBE = (TFCSpeedTelegraphInputBlockEntity)blockEntity;
            inputBE.onTFCOutputHandleChanged(this.handleOrder);
        }
    }

    public void onOrderChanged(SpeedTelegraphOrder newOrder, Level level) {
        this.bridgeOrder = newOrder;
        this.redDelayTimer = 20;
        this.setChanged();
        this.markForSync();
    }

    public void linkInput(UUID linkedInputUUID, Level level) {
        this.inputUUID = linkedInputUUID;
        TFCSpeedTelegraphInputBlockEntity inputBE = TFCSpeedTelegraphInputBlockEntity.findTFCInputByUUID(linkedInputUUID);
        if (inputBE != null) {
            this.resolvedInputPos = inputBE.getBlockPos();
            this.handleOrder = this.bridgeOrder = inputBE.getOrder();
        }
        this.setChanged();
        this.markForSync();
    }

    public void unlinkInput(Level level) {
        this.inputUUID = null;
        this.resolvedInputPos = null;
        this.bridgeOrder = SpeedTelegraphOrder.STOP;
        this.handleOrder = SpeedTelegraphOrder.STOP;
        this.redDelayTimer = 0;
        this.handleDelayTimer = 0;
        this.setChanged();
        this.markForSync();
    }

    public void notifyInputOnBreak(Level level) {
        if (this.resolvedInputPos != null) {
            BlockEntity blockEntity = level.getBlockEntity(this.resolvedInputPos);
            if (blockEntity instanceof TFCSpeedTelegraphInputBlockEntity) {
                TFCSpeedTelegraphInputBlockEntity inputBE = (TFCSpeedTelegraphInputBlockEntity)blockEntity;
                inputBE.onTFCOutputBroken(this.ownUUID, this.worldPosition);
            }
            this.resolvedInputPos = null;
        }
        this.inputUUID = null;
    }

    public InteractionResult onPlayerInteract(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        if (!held.isEmpty() && held.getItem() == ((Block)TIBAMBlocks.TFC_SPEED_TELEGRAPH_INPUT.get()).asItem()) {
            CompoundTag itemTag;
            if (this.inputUUID != null) {
                player.displayClientMessage((Component)Component.translatable((String)"tibam.telegraph.already_linked"), true);
                return InteractionResult.sidedSuccess((boolean)level.isClientSide);
            }
            if (!tfcmt.tfcitembarrelsandmore.common.MaritimeItemData.has(held)) {
                tfcmt.tfcitembarrelsandmore.common.MaritimeItemData.set(held, new CompoundTag());
            }
            ListTag pendingList = (itemTag = tfcmt.tfcitembarrelsandmore.common.MaritimeItemData.get(held)).contains("PendingTFCTelegraphOutputUUIDs", 9) ? itemTag.getList("PendingTFCTelegraphOutputUUIDs", 8) : new ListTag();
            String thisUUID = this.ownUUID.toString();
            boolean alreadyQueued = false;
            for (int i = 0; i < pendingList.size(); ++i) {
                if (!pendingList.getString(i).equals(thisUUID)) continue;
                alreadyQueued = true;
                break;
            }
            if (!alreadyQueued) {
                pendingList.add(StringTag.valueOf((String)thisUUID));
            }
            itemTag.put("PendingTFCTelegraphOutputUUIDs", (Tag)pendingList);
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

    public void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("OwnUUID", this.ownUUID.toString());
        if (this.inputUUID != null) {
            tag.putString("InputUUID", this.inputUUID.toString());
        }
        tag.putInt("BridgeOrder", this.bridgeOrder.ordinal());
        tag.putInt("HandleOrder", this.handleOrder.ordinal());
        tag.putInt("RedDelayTimer", this.redDelayTimer);
        tag.putInt("HandleDelayTimer", this.handleDelayTimer);
        tag.putBoolean("InvalidInNetwork", this.invalidInNetwork);
        tag.putFloat("OutSpeed", this.outSpeed);
    }

    public void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.ownUUID = tag.contains("OwnUUID") ? UUID.fromString(tag.getString("OwnUUID")) : UUID.randomUUID();
        TFCSpeedTelegraphInputBlockEntity.TFC_OUTPUT_REGISTRY.put(this.ownUUID, new WeakReference<TFCSpeedTelegraphOutputBlockEntity>(this));
        this.resolvedInputPos = null;
        try {
            this.inputUUID = tag.contains("InputUUID") ? UUID.fromString(tag.getString("InputUUID")) : null;
        }
        catch (IllegalArgumentException e) {
            this.inputUUID = null;
        }
        this.bridgeOrder = SpeedTelegraphOrder.fromOrdinal(tag.getInt("BridgeOrder"));
        this.handleOrder = SpeedTelegraphOrder.fromOrdinal(tag.getInt("HandleOrder"));
        this.redDelayTimer = tag.getInt("RedDelayTimer");
        this.handleDelayTimer = tag.getInt("HandleDelayTimer");
        this.invalidInNetwork = tag.getBoolean("InvalidInNetwork");
        this.outSpeed = tag.getFloat("OutSpeed");
    }


}

