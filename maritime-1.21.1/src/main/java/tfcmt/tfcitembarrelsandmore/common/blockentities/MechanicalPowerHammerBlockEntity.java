package tfcmt.tfcitembarrelsandmore.common.blockentities;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.dries007.tfc.client.particle.TFCParticles;
import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blockentities.TickableInventoryBlockEntity;
import net.dries007.tfc.common.blockentities.rotation.RotationSinkBlockEntity;
import net.dries007.tfc.common.blocks.devices.Tiered;
import net.dries007.tfc.common.component.forge.ForgeRule;
import net.dries007.tfc.common.component.forge.ForgeStep;
import net.dries007.tfc.common.component.forge.Forging;
import net.dries007.tfc.common.component.forge.ForgingCapability;
import net.dries007.tfc.common.component.heat.HeatCapability;
import net.dries007.tfc.common.component.heat.IHeat;
import net.dries007.tfc.common.recipes.AnvilRecipe;
import net.dries007.tfc.common.recipes.TFCRecipeTypes;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.rotation.NetworkAction;
import net.dries007.tfc.util.rotation.Node;
import net.dries007.tfc.util.rotation.Rotation;
import net.dries007.tfc.util.rotation.SinkNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tfcmt.tfcitembarrelsandmore.common.ITollableSink;
import tfcmt.tfcitembarrelsandmore.common.TIBAMBlockEntities;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.MechanicalPowerHammerBlock;
import tfcmt.tfcitembarrelsandmore.common.container.MechanicalPowerHammerContainer;
import tfcmt.tfcitembarrelsandmore.common.recipes.PowerHammerRecipes;

public class MechanicalPowerHammerBlockEntity
extends TickableInventoryBlockEntity<ItemStackHandler>
implements RotationSinkBlockEntity,
ITollableSink {
    private static final Logger LOGGER = LogManager.getLogger((String)"TIBAM/PowerHammer");
    public static final int SLOT_ANVIL = 0;
    public static final int SLOT_HAMMER = 1;
    public static final int SLOT_DIE = 2;
    public static final int INVENTORY_SIZE = 3;
    public static final int STEP_TICKS = 20;
    public static final float BASE_SPEED = 0.5f;
    public static final float MIN_WORKING_TEMP = 1000.0f;
    private static final double ANVIL_WORK_SURFACE_Y = 0.6875;
    private static final Component NAME = Component.translatable((String)"block.tfc_item_barrels_and_more.mechanical_power_hammer");
    private static final TagKey<Item> METAL_HAMMERS = TagKey.create((ResourceKey)Registries.ITEM, (ResourceLocation)ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "metal_hammers"));
    private SinkNode sinkNode;
    private boolean invalidInNetwork = false;
    private final IItemHandler hammerCap;
    @Nullable
    private ResourceLocation primedRecipeId = null;
    @Nullable
    private Player activePlayer = null;
    private float serverAngle = 0.0f;
    private float prevSinAngle = 0.0f;
    private final List<ForgeStep> pendingSteps = new ArrayList<ForgeStep>();

    protected float getSpeedMultiplier() {
        return 1.0f;
    }

    public boolean isPneumaticHammer() {
        return false;
    }

    public void markAsInvalidInNetwork() {
        this.invalidInNetwork = true;
    }

    public boolean isInvalidInNetwork() {
        return this.invalidInNetwork;
    }

    public Node getRotationNode() {
        return this.sinkNode;
    }

    public MechanicalPowerHammerBlockEntity(BlockPos pos, BlockState state) {
        this((BlockEntityType)TIBAMBlockEntities.MECHANICAL_POWER_HAMMER.get(), pos, state, NAME.getString());
    }

    protected MechanicalPowerHammerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, Component name) {
        super(type, pos, state, self -> new ItemStackHandler(3){

            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                return switch (slot) {
                    case 0 -> Helpers.isItem((ItemStack)stack, (TagKey)TFCTags.Items.ANVILS);
                    case 1 -> Helpers.isItem((ItemStack)stack, METAL_HAMMERS);
                    case 2 -> PowerHammerRecipes.isDie(stack);
                    default -> false;
                };
            }

            protected void onContentsChanged(int slot) {
                self.setChanged();
                self.markForSync();
                if (slot == 0 || slot == 2) {
                    ((MechanicalPowerHammerBlockEntity)self).onSlotConfigChanged();
                }
            }
        }, name.getString());
        this.hammerCap = new RangedWrapper(this.inventory, 1, 2);
    }





    public InteractionResult onPlayerInteract(Player player, InteractionHand hand, BlockState state) {
        if (player.isShiftKeyDown()) {
            if (!player.level().isClientSide && player instanceof ServerPlayer) {
                ServerPlayer sp = (ServerPlayer)player;
                ((ServerPlayer)sp).openMenu(this, this.worldPosition);
            }
            return InteractionResult.sidedSuccess((boolean)player.level().isClientSide);
        }
        if (!player.level().isClientSide) {
            AnvilRecipe primed;
            ItemStack held = player.getMainHandItem();
            if (held.isEmpty()) {
                return InteractionResult.PASS;
            }
            if (this.primedRecipeId == null) {
                ResourceLocation auto = this.tryAutoSelect(held);
                if (auto != null) {
                    this.primedRecipeId = auto;
                } else {
                    ItemStack dieStack = ((ItemStackHandler)this.inventory).getStackInSlot(2);
                    List<AnvilRecipe> candidates = PowerHammerRecipes.getAvailableRecipes(this.level, this.getAnvilTier(), dieStack, this.isPneumaticHammer()).stream().filter(r -> r.getInput().test(held)).toList();
                    if (candidates.size() > 1) {
                        this.showTooltip(player, "message.tfc_item_barrels_and_more.hammer_multiple_recipes");
                    } else if (candidates.isEmpty()) {
                        boolean needsBetterAnvil = PowerHammerRecipes.getAvailableRecipes(this.level, 6, dieStack, this.isPneumaticHammer()).stream().anyMatch(r -> r.getInput().test(held));
                        if (needsBetterAnvil) {
                            this.showTooltip(player, "message.tfc_item_barrels_and_more.hammer_wrong_anvil_tier");
                        } else {
                            this.showTooltip(player, "message.tfc_item_barrels_and_more.hammer_no_recipe");
                        }
                    }
                    return InteractionResult.FAIL;
                }
            }
            if ((primed = this.getRecipeById(this.level, this.primedRecipeId)) == null || !primed.getInput().test(held)) {
                this.primedRecipeId = null;
                this.showTooltip(player, "message.tfc_item_barrels_and_more.hammer_item_mismatch");
                return InteractionResult.FAIL;
            }
            if (!this.isRotating()) {
                this.showTooltip(player, "message.tfc_item_barrels_and_more.hammer_no_power");
                return InteractionResult.FAIL;
            }
            if (((ItemStackHandler)this.inventory).getStackInSlot(1).isEmpty()) {
                this.showTooltip(player, "message.tfc_item_barrels_and_more.hammer_no_head");
                return InteractionResult.FAIL;
            }
            IHeat heat = HeatCapability.get((ItemStack)held);
            if (heat == null || !heat.canWork()) {
                this.showTooltip(player, "message.tfc_item_barrels_and_more.hammer_too_cold");
                return InteractionResult.FAIL;
            }
            if (this.activePlayer != player || this.pendingSteps.isEmpty()) {
                this.activePlayer = player;
                this.buildStepSequence(held);
            }
        }
        return InteractionResult.CONSUME;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MechanicalPowerHammerBlockEntity hammer) {
        float currSin;
        hammer.destroyIfInvalid(level, pos);
        if (!hammer.isRotating()) {
            hammer.serverAngle = 0.0f;
            hammer.prevSinAngle = 0.0f;
            hammer.clearActiveJob();
            return;
        }
        if (hammer.activePlayer == null || !hammer.activePlayer.isAlive() || hammer.activePlayer.distanceToSqr((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5) > 16.0) {
            hammer.clearActiveJob();
            return;
        }
        float speed = Math.abs(hammer.getRotationSpeed());
        if (speed <= 0.0f) {
            return;
        }
        float prevAngle = hammer.serverAngle;
        hammer.serverAngle += speed;
        float prevSin = hammer.prevSinAngle;
        hammer.prevSinAngle = currSin = (float)Math.sin(hammer.serverAngle);
        if (prevSin < 0.0f && currSin >= 0.0f) {
            hammer.executeNextStep(level, state);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, MechanicalPowerHammerBlockEntity hammer) {
        Rotation rotation;
        if (hammer.sinkNode != null && (rotation = hammer.sinkNode.rotation()) instanceof Rotation.Tickable) {
            Rotation.Tickable tickable = (Rotation.Tickable)rotation;
            tickable.tick();
        }
    }

    private void executeNextStep(Level level, BlockState state) {
        if (this.activePlayer == null || this.primedRecipeId == null) {
            return;
        }
        ItemStack held = this.activePlayer.getMainHandItem();
        if (held.isEmpty()) {
            this.clearActiveJob();
            return;
        }
        IHeat heat = HeatCapability.get((ItemStack)held);
        if (heat == null || !heat.canWork()) {
            this.showTooltip(this.activePlayer, "message.tfc_item_barrels_and_more.hammer_too_cold");
            this.clearActiveJob();
            return;
        }
        if (this.pendingSteps.isEmpty()) {
            this.tryComplete(level, held);
            return;
        }
        Forging forging = ForgingCapability.get((ItemStack)held);
        if (forging == null) {
            this.clearActiveJob();
            return;
        }
        ForgeStep step = this.pendingSteps.remove(0);
        forging.addStep(step);
        LOGGER.debug("[powerHammer] step {} \u2192 work={} target={} at {}", (Object)step.name(), (Object)forging.getWork(), (Object)forging.getWorkTarget(), (Object)this.worldPosition);
        ItemStack hammerStack = ((ItemStackHandler)this.inventory).getStackInSlot(1);
        if (!hammerStack.isEmpty()) {
            ServerPlayer spl;
            Player player = this.activePlayer;
            ServerPlayer sp = player instanceof ServerPlayer ? (spl = (ServerPlayer)player) : null;
            boolean broke = hammerStack.hurt(1, sp != null ? sp.getRandom() : level.getRandom(), sp);
            ((ItemStackHandler)this.inventory).setStackInSlot(1, broke ? ItemStack.EMPTY : hammerStack);
            if (broke) {
                level.playSound(null, (double)this.worldPosition.getX() + 0.5, (double)this.worldPosition.getY() + 0.5, (double)this.worldPosition.getZ() + 0.5, SoundEvents.ITEM_BREAK, SoundSource.BLOCKS, 0.8f, 0.8f + level.getRandom().nextFloat() * 0.4f);
                this.clearActiveJob();
                return;
            }
        }
        level.playSound(null, (double)this.worldPosition.getX() + 0.5, (double)this.worldPosition.getY() + 0.5, (double)this.worldPosition.getZ() + 0.5, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.8f, 0.9f + level.getRandom().nextFloat() * 0.2f);
        this.spawnHitSparks(level);
        this.syncRunningState(level, state, true);
    }

    private void tryComplete(Level level, ItemStack held) {
        if (this.activePlayer == null || this.primedRecipeId == null) {
            return;
        }
        AnvilRecipe recipe = this.getRecipeById(level, this.primedRecipeId);
        if (recipe == null) {
            this.clearActiveJob();
            return;
        }
        final ItemStack workPiece = held;
        final int tier = this.getAnvilTier();
        AnvilRecipe.Inventory inv = new AnvilRecipe.Inventory(){

            public ItemStack getItem() {
                return workPiece;
            }

            public int getTier() {
                return tier;
            }

            public long getSeed() {
                return 0L;
            }
        };
        if (!recipe.checkComplete(inv)) {
            LOGGER.debug("[powerHammer] checkComplete false at {}, rebuilding steps", (Object)this.worldPosition);
            this.buildStepSequence(held);
            return;
        }
        ItemStack result = recipe.assemble(inv);
        if (!result.isEmpty()) {
            IHeat heatIn = HeatCapability.get((ItemStack)held);
            IHeat heatOut = HeatCapability.get((ItemStack)result);
            if (heatIn != null && heatOut != null) {
                heatOut.setTemperatureIfWarmer(heatIn.getTemperature());
            }
            this.activePlayer.setItemInHand(InteractionHand.MAIN_HAND, result);
            this.consumeFlux(this.activePlayer);
            LOGGER.debug("[powerHammer] complete \u2192 {} at {}", (Object)result.getItem().getDescriptionId(), (Object)this.worldPosition);
            level.playSound(null, (double)this.worldPosition.getX() + 0.5, (double)this.worldPosition.getY() + 0.5, (double)this.worldPosition.getZ() + 0.5, SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 0.6f, 1.0f);
            IHeat resultHeat = HeatCapability.get((ItemStack)result);
            ResourceLocation chainId = this.tryAutoSelect(result);
            if (chainId != null && resultHeat != null && resultHeat.canWork()) {
                this.primedRecipeId = chainId;
                LOGGER.debug("[powerHammer] chain primed \u2192 {} at {}", (Object)chainId, (Object)this.worldPosition);
            }
        }
        this.syncRunningState(level, this.getBlockState(), false);
        this.clearActiveJob();
    }

    @Nullable
    private ResourceLocation tryAutoSelect(ItemStack stack) {
        if (this.level == null || stack.isEmpty()) {
            return null;
        }
        ItemStack dieStack = ((ItemStackHandler)this.inventory).getStackInSlot(2);
        List<AnvilRecipe> candidates = PowerHammerRecipes.getAvailableRecipes(this.level, this.getAnvilTier(), dieStack, this.isPneumaticHammer()).stream().filter(r -> r.getInput().test(stack)).toList();
        return candidates.size() == 1 ? candidates.get(0).getId() : null;
    }

    private void consumeFlux(Player player) {
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty() || !Helpers.isItem((ItemStack)stack, (TagKey)TFCTags.Items.FLUX)) continue;
            stack.shrink(1);
            return;
        }
    }

    private void buildStepSequence(final ItemStack workPiece) {
        this.pendingSteps.clear();
        if (this.primedRecipeId == null || this.level == null) {
            return;
        }
        AnvilRecipe recipe = this.getRecipeById(this.level, this.primedRecipeId);
        if (recipe == null) {
            return;
        }
        Forging forging = ForgingCapability.get((ItemStack)workPiece);
        if (forging == null) {
            return;
        }
        final int tier = this.getAnvilTier();
        AnvilRecipe.Inventory inv = new AnvilRecipe.Inventory(){

            public ItemStack getItem() {
                return workPiece;
            }

            public int getTier() {
                return tier;
            }

            public long getSeed() {
                return 0L;
            }
        };
        AnvilRecipe existingRecipe = forging.getRecipe(this.level);
        if (existingRecipe == null || !existingRecipe.getId().equals((Object)recipe.getId())) {
            forging.setRecipe(recipe, inv);
        }
        int current = forging.getWork();
        int target = forging.getWorkTarget();
        ForgeRule[] rules = recipe.getRules();
        ForgeStep requiredLast = null;
        ForgeStep requiredSecondLast = null;
        ForgeStep requiredThirdLast = null;
        for (ForgeRule rule : rules) {
            String name = rule.name();
            ForgeStep mapped = MechanicalPowerHammerBlockEntity.mapRuleStep(name);
            if (mapped == null) continue;
            if (name.endsWith("_LAST") && !name.contains("NOT") && !name.contains("SECOND") && !name.contains("THIRD")) {
                requiredLast = mapped;
                continue;
            }
            if (name.endsWith("_SECOND_LAST")) {
                requiredSecondLast = mapped;
                continue;
            }
            if (!name.endsWith("_THIRD_LAST")) continue;
            requiredThirdLast = mapped;
        }
        ArrayList<ForgeStep> terminal = new ArrayList<ForgeStep>();
        if (requiredThirdLast != null) {
            terminal.add(requiredThirdLast);
        }
        if (requiredSecondLast != null) {
            terminal.add(requiredSecondLast);
        }
        if (requiredLast != null) {
            terminal.add(requiredLast);
        }
        int terminalDelta = terminal.stream().mapToInt(ForgeStep::step).sum();
        int fillerTarget = target - terminalDelta;
        List<ForgeStep> filler = MechanicalPowerHammerBlockEntity.greedyStepsToTarget(current, fillerTarget);
        this.pendingSteps.addAll(filler);
        this.pendingSteps.addAll(terminal);
        LOGGER.debug("[powerHammer] solver: current={} target={} filler={} terminal={} at {}", (Object)current, (Object)target, (Object)filler.size(), (Object)terminal.size(), (Object)this.worldPosition);
    }

    private static List<ForgeStep> greedyStepsToTarget(int current, int target) {
        ForgeStep best;
        ArrayList<ForgeStep> steps = new ArrayList<ForgeStep>();
        int pos = current;
        int limit = 150;
        for (int i = 0; i < 100 && pos != target; pos += best.step(), ++i) {
            best = null;
            int bestDelta = Integer.MAX_VALUE;
            for (ForgeStep s : ForgeStep.values()) {
                int dist;
                int next;
                if (s.name().equals("LIMIT") || (next = pos + s.step()) < 0 || next > 150 || (dist = Math.abs(target - next)) >= bestDelta) continue;
                bestDelta = dist;
                best = s;
            }
            if (best == null) break;
            steps.add(best);
        }
        return steps;
    }

    @Nullable
    private static ForgeStep mapRuleStep(String ruleName) {
        if (ruleName.startsWith("HIT")) {
            return ForgeStep.HIT_MEDIUM;
        }
        if (ruleName.startsWith("DRAW")) {
            return ForgeStep.DRAW;
        }
        if (ruleName.startsWith("PUNCH")) {
            return ForgeStep.PUNCH;
        }
        if (ruleName.startsWith("BEND")) {
            return ForgeStep.BEND;
        }
        if (ruleName.startsWith("UPSET")) {
            return ForgeStep.UPSET;
        }
        if (ruleName.startsWith("SHRINK")) {
            return ForgeStep.SHRINK;
        }
        return null;
    }

    private void clearActiveJob() {
        this.activePlayer = null;
        this.pendingSteps.clear();
        this.serverAngle = 0.0f;
        this.prevSinAngle = 0.0f;
        if (this.level != null && !this.level.isClientSide) {
            this.syncRunningState(this.level, this.getBlockState(), false);
        }
    }

    private void spawnHitSparks(Level level) {
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel server = (ServerLevel)level;
        double x = (double)this.worldPosition.getX() + 0.5 + (server.getRandom().nextDouble() - 0.5) * 0.6;
        double y = (double)this.worldPosition.getY() + 0.6875;
        double z = (double)this.worldPosition.getZ() + 0.5 + (server.getRandom().nextDouble() - 0.5) * 0.6;
        server.sendParticles((ParticleOptions)((SimpleParticleType)TFCParticles.SPARK.get()), x, y, z, 5, 0.0, 0.0, 0.0, (double)0.2f);
    }

    private void syncRunningState(Level level, BlockState state, boolean running) {
        if (level == null || level.isClientSide) {
            return;
        }
        if ((Boolean)state.getValue((Property)MechanicalPowerHammerBlock.RUNNING) != running) {
            level.setBlock(this.worldPosition, (BlockState)state.setValue((Property)MechanicalPowerHammerBlock.RUNNING, (Comparable)Boolean.valueOf(running)), 3);
        }
    }

    private void onSlotConfigChanged() {
        if (this.primedRecipeId == null || this.level == null) {
            return;
        }
        AnvilRecipe recipe = this.getRecipeById(this.level, this.primedRecipeId);
        ItemStack dieStack = ((ItemStackHandler)this.inventory).getStackInSlot(2);
        List<String> activePrefixes = PowerHammerRecipes.getActivePrefixes(dieStack, this.isPneumaticHammer());
        if (recipe == null || recipe.getMinTier() > this.getAnvilTier() || !PowerHammerRecipes.isRecipeAllowed(recipe, this.level, activePrefixes)) {
            this.primedRecipeId = null;
        }
    }

    public int getAnvilTier() {
        BlockItem blockItem;
        ItemStack anvil = ((ItemStackHandler)this.inventory).getStackInSlot(0);
        if (anvil.isEmpty()) {
            return -1;
        }
        Item item = anvil.getItem();
        if (item instanceof BlockItem && (item = (blockItem = (BlockItem)item).getBlock()) instanceof Tiered) {
            Tiered tiered = (Tiered)item;
            return tiered.getTier();
        }
        return 0;
    }

    @Nullable
    private AnvilRecipe getRecipeById(Level level, ResourceLocation id) {
        return (AnvilRecipe)Helpers.getRecipes((Level)level, (Supplier)TFCRecipeTypes.ANVIL).get(id);
    }

    private void showTooltip(Player player, String key) {
        player.displayClientMessage((Component)Component.translatable((String)key), true);
    }

    public AbstractContainerMenu createMenu(int windowId, Inventory playerInv, Player player) {
        return MechanicalPowerHammerContainer.create(this, playerInv, windowId);
    }

    public Component getDisplayName() {
        return NAME;
    }

    public boolean isRotating() {
        return this.sinkNode != null && this.sinkNode.rotation() != null;
    }

    public float getRotationSpeed() {
        if (this.sinkNode == null) {
            return 0.0f;
        }
        Rotation rot = this.sinkNode.rotation();
        return rot != null ? rot.speed() * this.getSpeedMultiplier() : 0.0f;
    }

    public float getRotationAngle(float partialTick) {
        if (this.sinkNode == null) {
            return 0.0f;
        }
        Rotation rot = this.sinkNode.rotation();
        return rot != null ? rot.angle(partialTick) * this.getSpeedMultiplier() : 0.0f;
    }

    public void onLoaded() {
        if (this.level == null) {
            return;
        }
        Direction inputFace = ((Direction)this.getBlockState().getValue((Property)MechanicalPowerHammerBlock.FACING)).getOpposite();
        this.sinkNode = new SinkNode(this.worldPosition, inputFace){};
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
        this.invalidInNetwork = tag.getBoolean("InvalidInNetwork");
        this.serverAngle = tag.getFloat("ServerAngle");
        this.prevSinAngle = tag.getFloat("PrevSinAngle");
        this.primedRecipeId = tag.contains("PrimedRecipe") ? ResourceLocation.tryParse((String)tag.getString("PrimedRecipe")) : null;
    }

    public void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("InvalidInNetwork", this.invalidInNetwork);
        tag.putFloat("ServerAngle", this.serverAngle);
        tag.putFloat("PrevSinAngle", this.prevSinAngle);
        if (this.primedRecipeId != null) {
            tag.putString("PrimedRecipe", this.primedRecipeId.toString());
        }
    }

    @Nullable
    public ResourceLocation getPrimedRecipeId() {
        return this.primedRecipeId;
    }

    public void setPrimedRecipeId(@Nullable ResourceLocation id) {
        this.primedRecipeId = id;
        this.onSlotConfigChanged();
    }

    public ItemStackHandler getHammerInventory() {
        return (ItemStackHandler)this.inventory;
    }

    public boolean isRunning() {
        return this.activePlayer != null;
    }

    @Override
    public boolean isTollableWorking() {
        return this.isRunning();
    }

    public net.neoforged.neoforge.items.IItemHandler maritimeItems(net.minecraft.core.Direction side) { return this.hammerCap; }
}

