package tfcmt.tfcitembarrelsandmore.compat.create;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.dries007.tfc.common.blockentities.TFCBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import tfcmt.tfcitembarrelsandmore.common.TIBAMSounds;
import tfcmt.tfcitembarrelsandmore.compat.create.CreateCompat;
import tfcmt.tfcitembarrelsandmore.compat.create.SpeedTelegraphInputRenderer;
import tfcmt.tfcitembarrelsandmore.compat.create.SpeedTelegraphOrder;
import tfcmt.tfcitembarrelsandmore.compat.create.SpeedTelegraphOutputBlockEntity;
import tfcmt.tfcitembarrelsandmore.tfcitembarrelsandmore;

public class SpeedTelegraphInputBlockEntity
extends TFCBlockEntity {
    private static final Component NAME = Component.translatable((String)"block.tfc_item_barrels_and_more.speed_telegraph_input");
    static final Map<UUID, WeakReference<SpeedTelegraphInputBlockEntity>> INPUT_REGISTRY = new ConcurrentHashMap<UUID, WeakReference<SpeedTelegraphInputBlockEntity>>();
    static final Map<UUID, WeakReference<SpeedTelegraphOutputBlockEntity>> OUTPUT_REGISTRY = new ConcurrentHashMap<UUID, WeakReference<SpeedTelegraphOutputBlockEntity>>();
    static final String ITEM_NBT_PENDING_OUTPUTS = "PendingTelegraphOutputUUIDs";
    private UUID ownUUID = UUID.randomUUID();
    private SpeedTelegraphOrder order = SpeedTelegraphOrder.STOP;
    private SpeedTelegraphOrder acknowledgedOrder = SpeedTelegraphOrder.STOP;
    private final List<UUID> outputUUIDs = new ArrayList<UUID>();
    private final List<BlockPos> resolvedOutputPositions = new ArrayList<BlockPos>();
    private static final float RED_TICK_SPEED = 0.12f;
    private static final float STEP_DEG = 22.0f;
    private float redAngle = 0.0f;
    private float redAnglePrev = 0.0f;
    private int lazyResolveCounter = 0;

    @Nullable
    public static SpeedTelegraphInputBlockEntity findInputByUUID(UUID uuid) {
        WeakReference<SpeedTelegraphInputBlockEntity> ref = INPUT_REGISTRY.get(uuid);
        if (ref == null) {
            return null;
        }
        SpeedTelegraphInputBlockEntity be = (SpeedTelegraphInputBlockEntity)((Object)ref.get());
        if (be == null) {
            INPUT_REGISTRY.remove(uuid);
        }
        return be;
    }

    @Nullable
    public static SpeedTelegraphOutputBlockEntity findOutputByUUID(UUID uuid) {
        WeakReference<SpeedTelegraphOutputBlockEntity> ref = OUTPUT_REGISTRY.get(uuid);
        if (ref == null) {
            return null;
        }
        SpeedTelegraphOutputBlockEntity be = (SpeedTelegraphOutputBlockEntity)((Object)ref.get());
        if (be == null) {
            OUTPUT_REGISTRY.remove(uuid);
        }
        return be;
    }

    public SpeedTelegraphInputBlockEntity(BlockPos pos, BlockState state) {
        super((BlockEntityType)CreateCompat.SPEED_TELEGRAPH_INPUT_BE.get(), pos, state);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, SpeedTelegraphInputBlockEntity be) {
        be.redAnglePrev = be.redAngle;
        float targetAngle = SpeedTelegraphInputRenderer.orderToAngle(be.acknowledgedOrder);
        float gap = targetAngle - be.redAngle;
        if (Math.abs(gap) > 0.05f) {
            int currStep;
            float prevAngle = be.redAngle;
            be.redAngle += gap * 0.12f;
            float minDeg = -110.0f;
            int prevStep = Math.round((prevAngle - minDeg) / 22.0f);
            if (prevStep != (currStep = Math.round((be.redAngle - minDeg) / 22.0f))) {
                level.playLocalSound((double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), (SoundEvent)TIBAMSounds.TELEGRAPH_BELL.get(), SoundSource.BLOCKS, 1.0f, 1.0f, false);
            }
        } else {
            be.redAngle = targetAngle;
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SpeedTelegraphInputBlockEntity be) {
        if (++be.lazyResolveCounter >= 20) {
            be.lazyResolveCounter = 0;
            be.resolveOutputPositions();
        }
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

    public SpeedTelegraphOrder getOrder() {
        return this.order;
    }

    public boolean isLinked() {
        return !this.outputUUIDs.isEmpty();
    }

    void resolveOutputPositions() {
        if (this.outputUUIDs.size() == this.resolvedOutputPositions.size()) {
            return;
        }
        this.resolvedOutputPositions.clear();
        for (UUID uuid : this.outputUUIDs) {
            SpeedTelegraphOutputBlockEntity output = SpeedTelegraphInputBlockEntity.findOutputByUUID(uuid);
            if (output == null) continue;
            this.resolvedOutputPositions.add(output.getBlockPos());
        }
    }

    public InteractionResult onPlayerInteract(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        SpeedTelegraphOrder newOrder;
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        SpeedTelegraphOrder speedTelegraphOrder = newOrder = player.isShiftKeyDown() ? this.order.stepAstern() : this.order.stepAhead();
        if (newOrder != this.order) {
            this.order = newOrder;
            this.setChanged();
            this.markForSync();
            this.notifyOutputsOrderChanged(level);
            float pitch = 1.2f - (0.8f + (float)this.order.ordinal() / (float)(SpeedTelegraphOrder.values().length - 1) * 0.4f);
            level.playSound(null, pos, (SoundEvent)SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.BLOCKS, 0.6f, pitch);
            player.displayClientMessage((Component)Component.translatable((String)("tibam.telegraph.order." + this.order.getSerializedName())), true);
        }
        return InteractionResult.sidedSuccess((boolean)false);
    }

    public void onPlacedBy(Level level, BlockPos pos, ItemStack fromStack) {
        if (!tfcmt.tfcitembarrelsandmore.common.MaritimeItemData.has(fromStack)) {
            return;
        }
        CompoundTag tag = tfcmt.tfcitembarrelsandmore.common.MaritimeItemData.get(fromStack);
        if (!tag.contains(ITEM_NBT_PENDING_OUTPUTS, 9)) {
            return;
        }
        ListTag list = tag.getList(ITEM_NBT_PENDING_OUTPUTS, 8);
        for (int i = 0; i < list.size(); ++i) {
            UUID outputUUID;
            try {
                outputUUID = UUID.fromString(list.getString(i));
            }
            catch (IllegalArgumentException e) {
                tfcitembarrelsandmore.LOGGER.warn("[telegraph] Skipping malformed UUID: {}", (Object)list.getString(i));
                continue;
            }
            SpeedTelegraphOutputBlockEntity registryOutput = SpeedTelegraphInputBlockEntity.findOutputByUUID(outputUUID);
            if (registryOutput == null) {
                tfcitembarrelsandmore.LOGGER.warn("[telegraph] Output UUID {} not in registry at placement \u2014 skipped", (Object)outputUUID);
                continue;
            }
            BlockPos outputPos = registryOutput.getBlockPos();
            BlockEntity blockEntity = level.getBlockEntity(outputPos);
            if (!(blockEntity instanceof SpeedTelegraphOutputBlockEntity)) {
                tfcitembarrelsandmore.LOGGER.warn("[telegraph] Output at {} no longer a valid BE \u2014 skipped", (Object)outputPos);
                continue;
            }
            SpeedTelegraphOutputBlockEntity output = (SpeedTelegraphOutputBlockEntity)blockEntity;
            if (output.isLinked()) {
                tfcitembarrelsandmore.LOGGER.warn("[telegraph] Output {} already linked \u2014 skipped", (Object)outputUUID);
                continue;
            }
            output.linkInput(this.ownUUID, level);
            this.outputUUIDs.add(outputUUID);
            this.resolvedOutputPositions.add(output.getBlockPos());
            tfcitembarrelsandmore.LOGGER.debug("[telegraph] Input {} linked to output {} at {}", new Object[]{this.ownUUID, outputUUID, output.getBlockPos()});
        }
        if (!this.outputUUIDs.isEmpty()) {
            this.setChanged();
            this.markForSync();
        }
        tfcmt.tfcitembarrelsandmore.common.MaritimeItemData.update(fromStack, data -> data.remove(ITEM_NBT_PENDING_OUTPUTS));
    }

    public void onOutputHandleChanged(SpeedTelegraphOrder newAcknowledged) {
        this.acknowledgedOrder = newAcknowledged;
        this.setChanged();
        this.markForSync();
    }

    public void notifyOutputsOnBreak(Level level) {
        for (BlockPos outputPos : this.resolvedOutputPositions) {
            BlockEntity blockEntity = level.getBlockEntity(outputPos);
            if (!(blockEntity instanceof SpeedTelegraphOutputBlockEntity)) continue;
            SpeedTelegraphOutputBlockEntity output = (SpeedTelegraphOutputBlockEntity)blockEntity;
            output.unlinkInput(level);
        }
        this.outputUUIDs.clear();
        this.resolvedOutputPositions.clear();
    }

    public void onOutputBroken(UUID brokenOutputUUID, BlockPos brokenOutputPos) {
        this.outputUUIDs.remove(brokenOutputUUID);
        this.resolvedOutputPositions.remove(brokenOutputPos);
        this.setChanged();
        this.markForSync();
    }

    private void notifyOutputsOrderChanged(Level level) {
        for (BlockPos outputPos : this.resolvedOutputPositions) {
            BlockEntity blockEntity = level.getBlockEntity(outputPos);
            if (!(blockEntity instanceof SpeedTelegraphOutputBlockEntity)) continue;
            SpeedTelegraphOutputBlockEntity output = (SpeedTelegraphOutputBlockEntity)blockEntity;
            output.onOrderChanged(this.order, level);
        }
    }

    public void loadAdditional(CompoundTag tag) {
        super.loadAdditional(tag);
        this.ownUUID = tag.contains("OwnUUID") ? UUID.fromString(tag.getString("OwnUUID")) : UUID.randomUUID();
        INPUT_REGISTRY.put(this.ownUUID, new WeakReference<SpeedTelegraphInputBlockEntity>(this));
        this.order = SpeedTelegraphOrder.fromOrdinal(tag.getInt("Order"));
        this.acknowledgedOrder = SpeedTelegraphOrder.fromOrdinal(tag.contains("AcknowledgedOrder") ? tag.getInt("AcknowledgedOrder") : tag.getInt("Order"));
        this.outputUUIDs.clear();
        this.resolvedOutputPositions.clear();
        if (tag.contains("OutputUUIDs", 9)) {
            ListTag list = tag.getList("OutputUUIDs", 8);
            for (int i = 0; i < list.size(); ++i) {
                try {
                    this.outputUUIDs.add(UUID.fromString(list.getString(i)));
                    continue;
                }
                catch (IllegalArgumentException illegalArgumentException) {
                    // empty catch block
                }
            }
        }
    }

    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("OwnUUID", this.ownUUID.toString());
        tag.putInt("Order", this.order.ordinal());
        tag.putInt("AcknowledgedOrder", this.acknowledgedOrder.ordinal());
        ListTag list = new ListTag();
        for (UUID uuid : this.outputUUIDs) {
            list.add((Object)StringTag.valueOf((String)uuid.toString()));
        }
        tag.put("OutputUUIDs", (Tag)list);
    }

    @Nonnull
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        this.saveAdditional(tag);
        return tag;
    }
}

