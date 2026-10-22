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
import tfcmt.tfcitembarrelsandmore.common.TIBAMBlockEntities;
import tfcmt.tfcitembarrelsandmore.common.TIBAMSounds;
import tfcmt.tfcitembarrelsandmore.compat.create.SpeedTelegraphInputRenderer;
import tfcmt.tfcitembarrelsandmore.compat.create.SpeedTelegraphOrder;
import tfcmt.tfcitembarrelsandmore.compat.create.TFCSpeedTelegraphOutputBlockEntity;
import tfcmt.tfcitembarrelsandmore.tfcitembarrelsandmore;

public class TFCSpeedTelegraphInputBlockEntity
extends TFCBlockEntity {
    private static final Component NAME = Component.translatable((String)"block.tfc_item_barrels_and_more.tfc_speed_telegraph_input");
    static final Map<UUID, WeakReference<TFCSpeedTelegraphInputBlockEntity>> TFC_INPUT_REGISTRY = new ConcurrentHashMap<UUID, WeakReference<TFCSpeedTelegraphInputBlockEntity>>();
    static final Map<UUID, WeakReference<TFCSpeedTelegraphOutputBlockEntity>> TFC_OUTPUT_REGISTRY = new ConcurrentHashMap<UUID, WeakReference<TFCSpeedTelegraphOutputBlockEntity>>();
    static final String ITEM_NBT_PENDING_TFC_OUTPUTS = "PendingTFCTelegraphOutputUUIDs";
    private static final float RED_TICK_SPEED = 0.12f;
    private static final float STEP_DEG = 22.0f;
    private float redAngle = 0.0f;
    private float redAnglePrev = 0.0f;
    private UUID ownUUID = UUID.randomUUID();
    private SpeedTelegraphOrder order = SpeedTelegraphOrder.STOP;
    private SpeedTelegraphOrder acknowledgedOrder = SpeedTelegraphOrder.STOP;
    private final List<UUID> outputUUIDs = new ArrayList<UUID>();
    private final List<BlockPos> resolvedOutputPositions = new ArrayList<BlockPos>();
    private int lazyResolveCounter = 0;

    @Nullable
    public static TFCSpeedTelegraphInputBlockEntity findTFCInputByUUID(UUID uuid) {
        WeakReference<TFCSpeedTelegraphInputBlockEntity> ref = TFC_INPUT_REGISTRY.get(uuid);
        if (ref == null) {
            return null;
        }
        TFCSpeedTelegraphInputBlockEntity be = (TFCSpeedTelegraphInputBlockEntity)((Object)ref.get());
        if (be == null) {
            TFC_INPUT_REGISTRY.remove(uuid);
        }
        return be;
    }

    @Nullable
    public static TFCSpeedTelegraphOutputBlockEntity findTFCOutputByUUID(UUID uuid) {
        WeakReference<TFCSpeedTelegraphOutputBlockEntity> ref = TFC_OUTPUT_REGISTRY.get(uuid);
        if (ref == null) {
            return null;
        }
        TFCSpeedTelegraphOutputBlockEntity be = (TFCSpeedTelegraphOutputBlockEntity)((Object)ref.get());
        if (be == null) {
            TFC_OUTPUT_REGISTRY.remove(uuid);
        }
        return be;
    }

    public TFCSpeedTelegraphInputBlockEntity(BlockPos pos, BlockState state) {
        super(TIBAMBlockEntities.TFC_SPEED_TELEGRAPH_INPUT.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TFCSpeedTelegraphInputBlockEntity be) {
        if (++be.lazyResolveCounter >= 20) {
            be.lazyResolveCounter = 0;
            be.resolveTFCOutputPositions();
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, TFCSpeedTelegraphInputBlockEntity be) {
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

    public UUID getOwnUUID() {
        return this.ownUUID;
    }

    public SpeedTelegraphOrder getOrder() {
        return this.order;
    }

    public boolean isLinked() {
        return !this.outputUUIDs.isEmpty();
    }

    public float getRedAnglePrev() {
        return this.redAnglePrev;
    }

    public float getRedAngle() {
        return this.redAngle;
    }

    void resolveTFCOutputPositions() {
        if (this.outputUUIDs.size() == this.resolvedOutputPositions.size()) {
            return;
        }
        this.resolvedOutputPositions.clear();
        for (UUID uuid : this.outputUUIDs) {
            TFCSpeedTelegraphOutputBlockEntity output = TFCSpeedTelegraphInputBlockEntity.findTFCOutputByUUID(uuid);
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
        if (!tag.contains(ITEM_NBT_PENDING_TFC_OUTPUTS, 9)) {
            return;
        }
        ListTag list = tag.getList(ITEM_NBT_PENDING_TFC_OUTPUTS, 8);
        for (int i = 0; i < list.size(); ++i) {
            UUID outputUUID;
            try {
                outputUUID = UUID.fromString(list.getString(i));
            }
            catch (IllegalArgumentException e) {
                tfcitembarrelsandmore.LOGGER.warn("[tfc-telegraph] Skipping malformed UUID: {}", (Object)list.getString(i));
                continue;
            }
            TFCSpeedTelegraphOutputBlockEntity registryOutput = TFCSpeedTelegraphInputBlockEntity.findTFCOutputByUUID(outputUUID);
            if (registryOutput == null) {
                tfcitembarrelsandmore.LOGGER.warn("[tfc-telegraph] Output UUID {} not in registry at placement \u2014 skipped", (Object)outputUUID);
                continue;
            }
            BlockPos outputPos = registryOutput.getBlockPos();
            BlockEntity blockEntity = level.getBlockEntity(outputPos);
            if (!(blockEntity instanceof TFCSpeedTelegraphOutputBlockEntity)) {
                tfcitembarrelsandmore.LOGGER.warn("[tfc-telegraph] Output at {} no longer a valid BE \u2014 skipped", (Object)outputPos);
                continue;
            }
            TFCSpeedTelegraphOutputBlockEntity output = (TFCSpeedTelegraphOutputBlockEntity)blockEntity;
            if (output.isLinked()) {
                tfcitembarrelsandmore.LOGGER.warn("[tfc-telegraph] Output {} already linked \u2014 skipped", (Object)outputUUID);
                continue;
            }
            output.linkInput(this.ownUUID, level);
            this.outputUUIDs.add(outputUUID);
            this.resolvedOutputPositions.add(output.getBlockPos());
            tfcitembarrelsandmore.LOGGER.debug("[tfc-telegraph] Input {} linked to output {} at {}", new Object[]{this.ownUUID, outputUUID, output.getBlockPos()});
        }
        if (!this.outputUUIDs.isEmpty()) {
            this.setChanged();
            this.markForSync();
        }
        tfcmt.tfcitembarrelsandmore.common.MaritimeItemData.update(fromStack, data -> data.remove(ITEM_NBT_PENDING_TFC_OUTPUTS));
    }

    public void onTFCOutputHandleChanged(SpeedTelegraphOrder newAcknowledged) {
        this.acknowledgedOrder = newAcknowledged;
        this.setChanged();
        this.markForSync();
    }

    public void notifyOutputsOnBreak(Level level) {
        for (BlockPos outputPos : this.resolvedOutputPositions) {
            BlockEntity blockEntity = level.getBlockEntity(outputPos);
            if (!(blockEntity instanceof TFCSpeedTelegraphOutputBlockEntity)) continue;
            TFCSpeedTelegraphOutputBlockEntity output = (TFCSpeedTelegraphOutputBlockEntity)blockEntity;
            output.unlinkInput(level);
        }
        this.outputUUIDs.clear();
        this.resolvedOutputPositions.clear();
    }

    public void onTFCOutputBroken(UUID brokenOutputUUID, BlockPos brokenOutputPos) {
        this.outputUUIDs.remove(brokenOutputUUID);
        this.resolvedOutputPositions.remove(brokenOutputPos);
        this.setChanged();
        this.markForSync();
    }

    private void notifyOutputsOrderChanged(Level level) {
        for (BlockPos outputPos : this.resolvedOutputPositions) {
            BlockEntity blockEntity = level.getBlockEntity(outputPos);
            if (!(blockEntity instanceof TFCSpeedTelegraphOutputBlockEntity)) continue;
            TFCSpeedTelegraphOutputBlockEntity output = (TFCSpeedTelegraphOutputBlockEntity)blockEntity;
            output.onOrderChanged(this.order, level);
        }
    }

    public void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.ownUUID = tag.contains("OwnUUID") ? UUID.fromString(tag.getString("OwnUUID")) : UUID.randomUUID();
        TFC_INPUT_REGISTRY.put(this.ownUUID, new WeakReference<TFCSpeedTelegraphInputBlockEntity>(this));
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

    public void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("OwnUUID", this.ownUUID.toString());
        tag.putInt("Order", this.order.ordinal());
        tag.putInt("AcknowledgedOrder", this.acknowledgedOrder.ordinal());
        ListTag list = new ListTag();
        for (UUID uuid : this.outputUUIDs) {
            list.add(StringTag.valueOf((String)uuid.toString()));
        }
        tag.put("OutputUUIDs", (Tag)list);
    }


}

