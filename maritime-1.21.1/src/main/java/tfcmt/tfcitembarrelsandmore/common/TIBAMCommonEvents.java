package tfcmt.tfcitembarrelsandmore.common;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import net.dries007.tfc.util.events.StartFireEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.core.registries.BuiltInRegistries;
import tfcmt.tfcitembarrelsandmore.common.BraceData;
import tfcmt.tfcitembarrelsandmore.common.TIBAMBlocks;
import tfcmt.tfcitembarrelsandmore.common.blockentities.BoilerBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blockentities.ExplorationLampBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.BoomHost;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.YardHost;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.BoilerBlock;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.ExplorationLampBeam;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.ExplorationLampBlock;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.ItemBarrelBlock;
import tfcmt.tfcitembarrelsandmore.common.blocks.mast.BoomGrowth;
import tfcmt.tfcitembarrelsandmore.common.blocks.mast.YardGrowth;
import tfcmt.tfcitembarrelsandmore.common.items.ExplorationLampBlockItem;
import tfcmt.tfcitembarrelsandmore.common.network.BraceDataPacket;
import tfcmt.tfcitembarrelsandmore.common.network.TIBAMNetwork;

public class TIBAMCommonEvents {
    private static final Set<String> BARREL_BLACKLIST = Set.of("barrel_cactus", "barrel_rack");
    private static final TagKey<Item> TAG_FIRE_STARTER = ItemTags.create((ResourceLocation)ResourceLocation.fromNamespaceAndPath("tfc", "fire_starter"));
    private static final Map<BracePos, BraceData> SERVER_BRACE_MAP = new HashMap<BracePos, BraceData>();
    private static final Map<ChunkKey, Set<BracePos>> CHUNK_INDEX = new HashMap<ChunkKey, Set<BracePos>>();
    private static final Set<String> VALID_METALS = Set.of("bismuth", "bismuth_bronze", "black_bronze", "brass", "bronze", "cast_iron", "copper", "gold", "nickel", "red_steel", "rose_gold", "silver", "steel", "tin", "wrought_iron", "zinc", "blue_steel");

    public static void removeBraceData(Level level, BlockPos pos) {
        BracePos key = new BracePos((ResourceKey<Level>)level.dimension(), pos.immutable());
        SERVER_BRACE_MAP.remove(key);
        ChunkKey chunkKey = ChunkKey.of(key);
        Set<BracePos> inChunk = CHUNK_INDEX.get(chunkKey);
        if (inChunk != null) {
            inChunk.remove(key);
            if (inChunk.isEmpty()) {
                CHUNK_INDEX.remove(chunkKey);
            }
        }
        if (!level.isClientSide() && level instanceof ServerLevel) {
            ServerLevel sl = (ServerLevel)level;
            TFCBarrelBraceSaveData.get(sl).remove(key);
        }
    }

    public static void putBraceData(Level level, BlockPos pos, BraceData data) {
        BracePos key = new BracePos((ResourceKey<Level>)level.dimension(), pos.immutable());
        SERVER_BRACE_MAP.put(key, data);
        CHUNK_INDEX.computeIfAbsent(ChunkKey.of(key), k -> new HashSet()).add(key);
        if (!level.isClientSide() && level instanceof ServerLevel) {
            ServerLevel sl = (ServerLevel)level;
            TFCBarrelBraceSaveData.get(sl).put(key, data);
        }
    }

    public static BraceData getBraceData(Level level, BlockPos pos) {
        return SERVER_BRACE_MAP.get(new BracePos((ResourceKey<Level>)level.dimension(), pos.immutable()));
    }

    @SubscribeEvent
    public void onLevelLoad(LevelEvent.Load event) {
        LevelAccessor levelAccessor = event.getLevel();
        if (!(levelAccessor instanceof ServerLevel)) {
            return;
        }
        ServerLevel level = (ServerLevel)levelAccessor;
        ResourceKey dimension = level.dimension();
        SERVER_BRACE_MAP.keySet().removeIf(bracePos -> bracePos.dimension().equals((Object)dimension));
        CHUNK_INDEX.keySet().removeIf(chunkKey -> chunkKey.dimension().equals((Object)dimension));
        TFCBarrelBraceSaveData data = TFCBarrelBraceSaveData.get(level);
        for (Map.Entry<BracePos, BraceData> e : data.getAll().entrySet()) {
            SERVER_BRACE_MAP.put(e.getKey(), e.getValue());
            CHUNK_INDEX.computeIfAbsent(ChunkKey.of(e.getKey()), k -> new HashSet()).add(e.getKey());
        }
    }

    @SubscribeEvent
    public void onChunkWatch(ChunkWatchEvent.Watch event) {
        ServerPlayer player = event.getPlayer();
        ChunkPos chunkPos = event.getPos();
        ChunkKey chunkKey = new ChunkKey((ResourceKey<Level>)event.getLevel().dimension(), chunkPos.x, chunkPos.z);
        Set<BracePos> inChunk = CHUNK_INDEX.get(chunkKey);
        if (inChunk == null || inChunk.isEmpty()) {
            return;
        }
        for (BracePos bracePos : inChunk) {
            BraceData data = SERVER_BRACE_MAP.get(bracePos);
            if (data == null) continue;
            PacketDistributor.sendToPlayer(player, new BraceDataPacket(bracePos.pos(), data));
        }
    }

    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        ItemStack held = event.getItemStack();
        BlockState clicked = level.getBlockState(pos);
        if ((clicked.getBlock() instanceof BoilerBlock || clicked.getBlock() instanceof ExplorationLampBlock) && !held.isEmpty() && (held.is(TAG_FIRE_STARTER) || held.getItem() == Items.FLINT_AND_STEEL)) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            if (!level.isClientSide) {
                ExplorationLampBlockEntity lamp;
                BoilerBlockEntity boiler;
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof BoilerBlockEntity && (boiler = (BoilerBlockEntity)blockEntity).isAwaitingIgnition()) {
                    boiler.ignite(level, pos, level.getBlockState(pos), event.getEntity(), held);
                } else if (level.getBlockState(pos).getBlock() instanceof ExplorationLampBlock && (blockEntity = level.getBlockEntity(pos)) instanceof ExplorationLampBlockEntity && (lamp = (ExplorationLampBlockEntity)blockEntity).isAwaitingIgnition(level.getBlockState(pos))) {
                    lamp.ignite(level, pos, level.getBlockState(pos), event.getEntity(), held);
                }
            }
            return;
        }
        if (!event.getEntity().isShiftKeyDown()) {
            return;
        }
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        level = event.getLevel();
        BlockState state = level.getBlockState(pos = event.getPos());
        if (!TIBAMCommonEvents.isBarrel(state)) {
            return;
        }
        if (!this.isSideFacing(state)) {
            return;
        }
        ItemStack held2 = event.getItemStack();
        if (held2.isEmpty()) {
            return;
        }
        String[] brace = this.resolveBraceType(held2);
        if (brace == null) {
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (!level.isClientSide()) {
            String fType = brace[0];
            String fVariant = brace[1];
            String fMod = brace[2];
            if (!event.getEntity().isCreative()) {
                held2.shrink(1);
            }
            level.playSound(null, pos, fType.equals("wood") ? SoundEvents.WOOD_PLACE : SoundEvents.METAL_PLACE, SoundSource.BLOCKS, 1.0f, 1.0f);
            BraceData data = new BraceData(fType, fVariant, fMod);
            TIBAMCommonEvents.putBraceData(level, pos, data);
            if (state.hasProperty((Property)ItemBarrelBlock.BRACED)) {
                level.setBlockAndUpdate(pos, (BlockState)state.setValue((Property)ItemBarrelBlock.BRACED, Boolean.valueOf(true)));
            }
            TIBAMNetwork.syncBrace(pos, data, (ServerLevel)level);
        }
    }

    public static boolean isBarrel(BlockState state) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if (id == null) {
            return false;
        }
        String path = id.getPath();
        if (!path.contains("barrel")) {
            return false;
        }
        for (String blacklisted : BARREL_BLACKLIST) {
            if (!path.contains(blacklisted)) continue;
            return false;
        }
        return true;
    }

    private boolean isSideFacing(BlockState state) {
        for (Property prop : state.getProperties()) {
            Comparable val;
            if (!prop.getName().equals("facing") || !((val = state.getValue(prop)) instanceof Direction)) continue;
            Direction dir = (Direction)val;
            return dir.getAxis().isHorizontal();
        }
        return false;
    }

    private String[] resolveBraceType(ItemStack held) {
        String metalName;
        boolean valid;
        ResourceLocation heldId = BuiltInRegistries.ITEM.getKey(held.getItem());
        if (heldId == null) {
            return null;
        }
        String path = heldId.getPath();
        String ns = heldId.getNamespace();
        if (path.startsWith("wood/lumber/") && (ns.equals("tfc") || ns.equals("afc"))) {
            String woodName = path.substring("wood/lumber/".length());
            boolean valid2 = Arrays.stream(TIBAMBlocks.Wood.values()).anyMatch(w -> w.getId().equals(woodName) && w.getMod().equals(ns));
            if (valid2) {
                return new String[]{"wood", woodName, ns};
            }
        } else if (ns.equals("tfc") && path.startsWith("metal/rod/") && (valid = VALID_METALS.contains(metalName = path.substring("metal/rod/".length())))) {
            return new String[]{"metal", metalName, "tfc"};
        }
        return null;
    }

    @SubscribeEvent
    public void onStartFire(StartFireEvent event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        if (level.isClientSide) {
            return;
        }
        if (!(level.getBlockState(pos).getBlock() instanceof BoilerBlock)) {
            return;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof BoilerBlockEntity)) {
            return;
        }
        BoilerBlockEntity boiler = (BoilerBlockEntity)blockEntity;
        if (!boiler.isAwaitingIgnition()) {
            return;
        }
        event.setCanceled(true);
        boiler.ignite(level, pos, level.getBlockState(pos), event.getPlayer(), event.getItemStack());
    }

    @SubscribeEvent(priority=EventPriority.HIGH)
    public void onRightClickBlockIgniteOffhandLamp(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        this.tryIgniteOffhandLamp(event.getEntity(), event.getItemStack(), (PlayerInteractEvent)event);
    }

    @SubscribeEvent(priority=EventPriority.HIGH)
    public void onRightClickItemIgniteOffhandLamp(PlayerInteractEvent.RightClickItem event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        this.tryIgniteOffhandLamp(event.getEntity(), event.getItemStack(), (PlayerInteractEvent)event);
    }

    private void tryIgniteOffhandLamp(Player player, ItemStack mainHandItem, PlayerInteractEvent event) {
        if (player == null) {
            return;
        }
        if (mainHandItem.isEmpty()) {
            return;
        }
        if (!mainHandItem.is(TAG_FIRE_STARTER) && mainHandItem.getItem() != Items.FLINT_AND_STEEL) {
            return;
        }
        ItemStack offhand = player.getOffhandItem();
        if (!(offhand.getItem() instanceof ExplorationLampBlockItem)) {
            return;
        }
        if (ExplorationLampBlockItem.isLit(offhand)) {
            return;
        }
        if (ExplorationLampBlockItem.getFluidInStack(offhand).isEmpty()) {
            return;
        }
        if (event instanceof PlayerInteractEvent.RightClickBlock blockEvent) {
            blockEvent.setCanceled(true); blockEvent.setCancellationResult(InteractionResult.SUCCESS);
        } else if (event instanceof PlayerInteractEvent.RightClickItem itemEvent) {
            itemEvent.setCanceled(true); itemEvent.setCancellationResult(InteractionResult.SUCCESS);
        }
        if (player.level().isClientSide) {
            return;
        }
        ExplorationLampBlockItem.setLit(offhand, true);
        player.displayClientMessage((Component)Component.translatable((String)"message.tfc_item_barrels_and_more.exploration_lamp.lit"), true);
    }

    @SubscribeEvent
    public void onStartFireExplorationLamp(StartFireEvent event) {
        Level level = event.getLevel();
        if (level.isClientSide) {
            return;
        }
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof ExplorationLampBlock) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (!(blockEntity instanceof ExplorationLampBlockEntity)) {
                return;
            }
            ExplorationLampBlockEntity lamp = (ExplorationLampBlockEntity)blockEntity;
            if (lamp.getFluid().isEmpty()) {
                return;
            }
            if (((Boolean)state.getValue((Property)ExplorationLampBlock.LIT)).booleanValue()) {
                return;
            }
            event.setCanceled(true);
            BlockState newLampState = (BlockState)state.setValue((Property)ExplorationLampBlock.LIT, Boolean.valueOf(true));
            level.setBlockAndUpdate(pos, newLampState);
            lamp.markForSync();
            if (level instanceof ServerLevel) {
                ServerLevel serverLevel = (ServerLevel)level;
                ExplorationLampBeam.sync(serverLevel, pos, newLampState);
            }
        }
    }

    @SubscribeEvent
    public void onLeftClickBlockDetachYard(PlayerInteractEvent.LeftClickBlock event) {
        BoomHost bh;
        boolean hasBoom;
        YardHost yh;
        BlockPos pos;
        Player player = event.getEntity();
        if (!player.isShiftKeyDown()) {
            return;
        }
        Level level = event.getLevel();
        BlockEntity be = level.getBlockEntity(pos = event.getPos());
        boolean hasYard = be instanceof YardHost && (yh = (YardHost)be).hasYard();
        boolean bl = hasBoom = be instanceof BoomHost && (bh = (BoomHost)be).hasBoom();
        if (!hasYard && !hasBoom) {
            return;
        }
        event.setCanceled(true);
        if (!level.isClientSide) {
            if (hasYard) {
                YardGrowth.detachYard(level, pos, (YardHost)be, player);
            }
            if (hasBoom) {
                BoomGrowth.detachBoom(level, pos, (BoomHost)be, player);
            }
        }
    }

    private record BracePos(ResourceKey<Level> dimension, BlockPos pos) {
    }

    private record ChunkKey(ResourceKey<Level> dimension, int chunkX, int chunkZ) {
        static ChunkKey of(BracePos bracePos) {
            return new ChunkKey(bracePos.dimension(), bracePos.pos().getX() >> 4, bracePos.pos().getZ() >> 4);
        }
    }

    public static class TFCBarrelBraceSaveData
    extends SavedData {
        private static final String NAME = "tfc_barrel_braces";
        private final Map<BracePos, BraceData> braces = new HashMap<BracePos, BraceData>();

        public static TFCBarrelBraceSaveData get(ServerLevel level) {
            return (TFCBarrelBraceSaveData)level.getDataStorage().computeIfAbsent(new SavedData.Factory<>(TFCBarrelBraceSaveData::new, (tag, registries) -> TFCBarrelBraceSaveData.load(tag), null), NAME);
        }

        private TFCBarrelBraceSaveData() {
        }

        public void put(BracePos key, BraceData data) {
            this.braces.put(key, data);
            this.setDirty();
        }

        public void remove(BracePos key) {
            this.braces.remove(key);
            this.setDirty();
        }

        public Map<BracePos, BraceData> getAll() {
            return this.braces;
        }

        public CompoundTag save(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
            ListTag list = new ListTag();
            for (Map.Entry<BracePos, BraceData> e : this.braces.entrySet()) {
                CompoundTag entry = new CompoundTag();
                entry.putString("dimension", e.getKey().dimension().location().toString());
                entry.putInt("x", e.getKey().pos().getX());
                entry.putInt("y", e.getKey().pos().getY());
                entry.putInt("z", e.getKey().pos().getZ());
                entry.putString("type", e.getValue().type);
                entry.putString("variant", e.getValue().variant);
                entry.putString("mod", e.getValue().mod);
                list.add(entry);
            }
            tag.put("braces", (Tag)list);
            return tag;
        }

        public static TFCBarrelBraceSaveData load(CompoundTag tag) {
            TFCBarrelBraceSaveData data = new TFCBarrelBraceSaveData();
            ListTag list = tag.getList("braces", 10);
            for (int i = 0; i < list.size(); ++i) {
                BracePos pos;
                CompoundTag entry = list.getCompound(i);
                String type = entry.getString("type");
                String variant = entry.getString("variant");
                String mod = entry.getString("mod");
                if (type.isEmpty()) continue;
                BracePos bracePos = pos = entry.contains("x") ? TFCBarrelBraceSaveData.readPos(entry) : TFCBarrelBraceSaveData.readLegacyPos(entry);
                if (pos == null) continue;
                data.braces.put(pos, new BraceData(type, variant, mod));
            }
            return data;
        }

        @Nullable
        private static BracePos readPos(CompoundTag entry) {
            ResourceKey<Level> dimension = TFCBarrelBraceSaveData.parseDimension(entry.getString("dimension"));
            if (dimension == null) {
                return null;
            }
            return new BracePos(dimension, new BlockPos(entry.getInt("x"), entry.getInt("y"), entry.getInt("z")));
        }

        @Nullable
        private static BracePos readLegacyPos(CompoundTag entry) {
            String key = entry.getString("key");
            int at = key.indexOf(64);
            if (at < 0) {
                return null;
            }
            ResourceKey<Level> dimension = TFCBarrelBraceSaveData.parseDimension(key.substring(0, at));
            if (dimension == null) {
                return null;
            }
            try {
                String[] parts = key.substring(at + 1).split(", ");
                if (parts.length != 3) {
                    return null;
                }
                return new BracePos(dimension, new BlockPos(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2])));
            }
            catch (NumberFormatException e) {
                return null;
            }
        }

        @Nullable
        private static ResourceKey<Level> parseDimension(String s) {
            ResourceLocation rl = ResourceLocation.tryParse((String)s);
            if (rl == null) {
                return null;
            }
            return ResourceKey.create((ResourceKey)Registries.DIMENSION, (ResourceLocation)rl);
        }
    }
}

