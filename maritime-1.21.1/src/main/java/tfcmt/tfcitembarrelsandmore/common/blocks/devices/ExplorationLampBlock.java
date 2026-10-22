package tfcmt.tfcitembarrelsandmore.common.blocks.devices;

import java.util.List;
import javax.annotation.Nullable;
import net.dries007.tfc.common.blocks.EntityBlockExtension;
import net.dries007.tfc.common.blocks.ExtendedBlock;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.util.loot.CopyFluidFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import tfcmt.tfcitembarrelsandmore.common.blockentities.ExplorationLampBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.ExplorationLampBeam;
import tfcmt.tfcitembarrelsandmore.common.items.ExplorationLampBlockItem;
import tfcmt.tfcitembarrelsandmore.compat.valkyrienskies.VSCompat;

public class ExplorationLampBlock
extends ExtendedBlock
implements EntityBlockExtension {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = BooleanProperty.create((String)"lit");
    public static final EnumProperty<ExplorationLampBlockEntity.Mode> MODE = EnumProperty.create((String)"mode", ExplorationLampBlockEntity.Mode.class);
    private static final TagKey<Item> TAG_FIRE_STARTER = ItemTags.create((ResourceLocation)ResourceLocation.fromNamespaceAndPath("tfc", "fire_starter"));
    private static final VoxelShape SHAPE = Shapes.or((VoxelShape)Block.box((double)4.0, (double)0.0, (double)4.0, (double)12.0, (double)12.0, (double)12.0), (VoxelShape)Block.box((double)6.0, (double)12.0, (double)6.0, (double)10.0, (double)16.0, (double)10.0));

    public ExplorationLampBlock(ExtendedProperties properties, boolean defaultLit, ExplorationLampBlockEntity.Mode defaultMode) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue((Property)FACING, Direction.NORTH)).setValue((Property)LIT, Boolean.valueOf(defaultLit))).setValue(MODE, defaultMode));
    }

    public ExplorationLampBlock(ExtendedProperties properties) {
        this(properties, false, ExplorationLampBlockEntity.Mode.AREA);
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(new Property[]{FACING, LIT, MODE});
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return (BlockState)this.defaultBlockState().setValue((Property)FACING, ctx.getHorizontalDirection().getOpposite());
    }

    public InteractionResult legacyUse(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ExplorationLampBlockEntity lamp;
        ItemStack held = player.getItemInHand(hand);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof ExplorationLampBlockEntity) {
            ExplorationLampBlockEntity fireLamp = (ExplorationLampBlockEntity)blockEntity;
            if (!held.isEmpty() && (held.is(TAG_FIRE_STARTER) || held.getItem() == Items.FLINT_AND_STEEL) && fireLamp.isAwaitingIgnition(state)) {
                fireLamp.ignite(level, pos, state, player, held);
                return InteractionResult.sidedSuccess((boolean)level.isClientSide);
            }
        }
        if ((blockEntity = level.getBlockEntity(pos)) instanceof ExplorationLampBlockEntity && FluidHelpers.transferBetweenBlockEntityAndItem((ItemStack)held, (BlockEntity)(lamp = (ExplorationLampBlockEntity)blockEntity), (Player)player, (InteractionHand)hand)) {
            lamp.markForSync();
            return InteractionResult.sidedSuccess((boolean)level.isClientSide);
        }
        if (held.isEmpty()) {
            BlockEntity serverLevel;
            if (player.isShiftKeyDown() && ((Boolean)state.getValue((Property)LIT)).booleanValue()) {
                if (!level.isClientSide) {
                    BlockState newState = (BlockState)state.setValue((Property)LIT, Boolean.valueOf(false));
                    level.setBlockAndUpdate(pos, newState);
                    BlockEntity blockEntity2 = level.getBlockEntity(pos);
                    if (blockEntity2 instanceof ExplorationLampBlockEntity) {
                        ExplorationLampBlockEntity lamp2 = (ExplorationLampBlockEntity)blockEntity2;
                        lamp2.markForSync();
                    }
                    if (level instanceof ServerLevel) {
                        ServerLevel serverLevel2 = (ServerLevel)level;
                        ExplorationLampBeam.sync(serverLevel2, pos, newState);
                    }
                }
                return InteractionResult.sidedSuccess((boolean)level.isClientSide);
            }
            if (!player.isShiftKeyDown()) {
                if (!level.isClientSide && (serverLevel = level.getBlockEntity(pos)) instanceof ExplorationLampBlockEntity) {
                    lamp = (ExplorationLampBlockEntity)serverLevel;
                    lamp.cycleMode();
                    BlockState newState = (BlockState)state.setValue(MODE, (lamp.getMode()));
                    level.setBlockAndUpdate(pos, newState);
                    if (level instanceof ServerLevel) {
                        ServerLevel serverLevel2 = (ServerLevel)level;
                        ExplorationLampBeam.sync(serverLevel2, pos, newState);
                    }
                }
                return InteractionResult.sidedSuccess((boolean)level.isClientSide);
            }
        }
        return InteractionResult.PASS;
    }

    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        BlockEntity blockEntity;
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && (blockEntity = level.getBlockEntity(pos)) instanceof ExplorationLampBlockEntity) {
            ExplorationLampBlockEntity lamp = (ExplorationLampBlockEntity)blockEntity;
            CopyFluidFunction.copyFromItem((ItemStack)stack, (BlockEntity)lamp);
            lamp.setModeFromItem(stack);
            lamp.markForSync();
        }
    }

    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!level.isClientSide && state.getBlock() != newState.getBlock() && level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            ExplorationLampBeam.onLampRemoved(serverLevel, pos);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    public ItemStack getCloneItemStack(BlockState state, HitResult target, net.minecraft.world.level.LevelReader level, BlockPos pos, Player player) {
        boolean lit = (Boolean)state.getValue((Property)LIT);
        ItemStack stack = new ItemStack((ItemLike)this.asItem());
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof ExplorationLampBlockEntity) {
            ExplorationLampBlockEntity lamp = (ExplorationLampBlockEntity)blockEntity;
            CopyFluidFunction.copyToItem((ItemStack)stack, (BlockEntity)lamp);
            lamp.saveModeToItem(stack);
        }
        ExplorationLampBlockItem.setLit(stack, lit);
        return stack;
    }

    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
    }

    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.DESTROY;
    }

    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return false;
    }

    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide) {
            ItemStack drop = this.getCloneItemStack(state, null, (net.minecraft.world.level.LevelReader)level, pos, player);
            Vec3 dropPos = VSCompat.spawnPosToWorld(level, pos, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5);
            ItemEntity entity = new ItemEntity(level, dropPos.x, dropPos.y, dropPos.z, drop);
            entity.setDefaultPickUpDelay();
            level.addFreshEntity((Entity)entity);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        if (builder.getOptionalParameter(LootContextParams.THIS_ENTITY) instanceof Player) {
            return List.of();
        }
        return super.getDrops(state, builder);
    }

    public static int getLightEmission(BlockState state) {
        if (!((Boolean)state.getValue((Property)LIT)).booleanValue()) {
            return 0;
        }
        return switch ((ExplorationLampBlockEntity.Mode)((Object)state.getValue(MODE))) {
            default -> throw new IncompatibleClassChangeError();
            case ExplorationLampBlockEntity.Mode.SEARCH -> 0;
            case ExplorationLampBlockEntity.Mode.AREA -> 14;
        };
    }

    @Override
    public net.minecraft.world.ItemInteractionResult useItemOn(net.minecraft.world.item.ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return switch (legacyUse(state, level, pos, player, hand, hit)) {
            case SUCCESS -> net.minecraft.world.ItemInteractionResult.SUCCESS;
            case CONSUME -> net.minecraft.world.ItemInteractionResult.CONSUME;
            case CONSUME_PARTIAL -> net.minecraft.world.ItemInteractionResult.CONSUME_PARTIAL;
            case FAIL -> net.minecraft.world.ItemInteractionResult.FAIL;
            default -> net.minecraft.world.ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        };
    }
    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return legacyUse(state, level, pos, player, InteractionHand.MAIN_HAND, hit);
    }
}

