package tfcmt.tfcitembarrelsandmore.common.blocks.devices;

import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.blocks.devices.SealableDeviceBlock;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import org.jetbrains.annotations.Nullable;
import tfcmt.tfcitembarrelsandmore.common.BraceData;
import tfcmt.tfcitembarrelsandmore.common.TIBAMBlockEntities;
import tfcmt.tfcitembarrelsandmore.common.TIBAMCommonEvents;
import tfcmt.tfcitembarrelsandmore.common.blockentities.ItemBarrelBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.network.TIBAMNetwork;

public class ItemBarrelBlock
extends SealableDeviceBlock {
    public static final EnumProperty<Direction> FACING = TFCBlockStateProperties.FACING_NOT_DOWN;
    private static final VoxelShape SHAPE_UP = Shapes.create((double)0.125, (double)0.0, (double)0.125, (double)0.875, (double)1.0, (double)0.875);
    private static final VoxelShape SHAPE_X = Shapes.create((double)0.0, (double)0.125, (double)0.125, (double)1.0, (double)0.875, (double)0.875);
    private static final VoxelShape SHAPE_Z = Shapes.create((double)0.125, (double)0.125, (double)0.0, (double)0.875, (double)0.875, (double)1.0);
    public static final BooleanProperty BRACED = BooleanProperty.create((String)"braced");

    public static void toggleSeal(Level level, BlockPos pos, BlockState state) {
        level.getBlockEntity(pos, (BlockEntityType)TIBAMBlockEntities.ITEM_BARREL.get()).ifPresent(barrel -> {
            boolean previousSealed = (Boolean)state.getValue((Property)SEALED);
            level.setBlockAndUpdate(pos, (BlockState)state.setValue((Property)SEALED, (Comparable)Boolean.valueOf(!previousSealed)));
            if (previousSealed) {
                barrel.onUnseal();
            } else {
                barrel.onSeal();
            }
        });
    }

    public ItemBarrelBlock(ExtendedProperties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.getStateDefinition().any()).setValue((Property)SEALED, (Comparable)Boolean.valueOf(false))).setValue(FACING, (Comparable)Direction.UP)).setValue((Property)BRACED, (Comparable)Boolean.valueOf(false)));
    }

    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        ItemStack held = player.getItemInHand(hand);
        if (player.isShiftKeyDown() && held.isEmpty()) {
            level.getBlockEntity(pos, (BlockEntityType)TIBAMBlockEntities.ITEM_BARREL.get()).ifPresent(barrel -> {
                if (player instanceof ServerPlayer) {
                    ItemBarrelBlock.toggleSeal(level, pos, state);
                    level.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.0f, 0.85f);
                }
            });
            return InteractionResult.CONSUME;
        }
        if (!player.isShiftKeyDown()) {
            level.getBlockEntity(pos, (BlockEntityType)TIBAMBlockEntities.ITEM_BARREL.get()).ifPresent(barrel -> {
                if (player instanceof ServerPlayer) {
                    ServerPlayer serverPlayer = (ServerPlayer)player;
                    Helpers.openScreen((ServerPlayer)serverPlayer, (MenuProvider)barrel, (BlockPos)barrel.getBlockPos());
                }
            });
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state != null) {
            Direction dir = context.getClickedFace();
            if (dir == Direction.DOWN) {
                dir = Direction.UP;
            }
            state = (BlockState)state.setValue(FACING, (Comparable)dir);
            Level level = context.getLevel();
            BlockPos pos = context.getClickedPos();
            if (dir == Direction.UP && !level.getBlockState(pos.below()).isFaceSturdy((BlockGetter)level, pos.below(), Direction.UP, SupportType.CENTER)) {
                return null;
            }
        }
        return state;
    }

    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!Helpers.isBlock((BlockState)state, (Block)newState.getBlock())) {
            level.getBlockEntity(pos, (BlockEntityType)TIBAMBlockEntities.ITEM_BARREL.get()).ifPresent(barrel -> {
                if (!((Boolean)state.getValue((Property)SEALED)).booleanValue()) {
                    barrel.ejectInventory();
                } else {
                    ItemStack stack = new ItemStack((ItemLike)this);
                    CompoundTag tag = new CompoundTag();
                    barrel.saveAdditional(tag);
                    tag.putBoolean("Sealed", true);
                    tag.remove("BraceType");
                    tag.remove("BraceVariant");
                    tag.remove("BraceMod");
                    if (!tag.isEmpty()) {
                        stack.addTagElement("BlockEntityTag", (Tag)tag);
                    }
                    Helpers.spawnItem((Level)level, (BlockPos)pos, (ItemStack)stack);
                }
            });
            if (((Boolean)state.getValue((Property)BRACED)).booleanValue() && !level.isClientSide()) {
                level.getBlockEntity(pos, (BlockEntityType)TIBAMBlockEntities.ITEM_BARREL.get()).ifPresent(barrel -> {
                    ResourceLocation itemId;
                    Item item;
                    BraceData brace = barrel.getEffectiveBraceData();
                    if (brace != null && brace.isValid() && (item = (Item)BuiltInRegistries.ITEM.get(itemId = brace.type.equals("wood") ? ResourceLocation.fromNamespaceAndPath(brace.mod, "wood/lumber/" + brace.variant) : ResourceLocation.fromNamespaceAndPath("tfc", "metal/rod/" + brace.variant))) != null && item != Items.AIR) {
                        Helpers.spawnItem((Level)level, (BlockPos)pos, (ItemStack)new ItemStack((ItemLike)item));
                    }
                });
            }
            if (!level.isClientSide() && level instanceof ServerLevel) {
                ServerLevel serverLevel = (ServerLevel)level;
                TIBAMNetwork.removeBrace(pos, serverLevel);
            }
            if (!level.isClientSide()) {
                TIBAMCommonEvents.removeBraceData(level, pos);
            }
            level.removeBlockEntity(pos);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(new Property[]{FACING, BRACED}));
    }

    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ItemBarrelBlockEntity((BlockEntityType)TIBAMBlockEntities.ITEM_BARREL.get(), pos, state);
    }

    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (((Direction)state.getValue(FACING)).getAxis()) {
            case Direction.Axis.X -> SHAPE_X;
            case Direction.Axis.Z -> SHAPE_Z;
            default -> SHAPE_UP;
        };
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return (lvl, p, st, be) -> {
            if (be instanceof ItemBarrelBlockEntity) {
                ItemBarrelBlockEntity barrel = (ItemBarrelBlockEntity)be;
                ItemBarrelBlockEntity.serverTick(lvl, p, st, barrel);
            }
        };
    }
}

