package tfcmt.tfcitembarrelsandmore.common.blocks.devices;

import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.LargeSinglePistonEngineBlock;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.TripleExpansionEngineBlock;

public class EngineFillerBlock
extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<Role> ROLE = EnumProperty.create((String)"role", Role.class);

    public EngineFillerBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)this.defaultBlockState().setValue((Property)FACING, Direction.NORTH)).setValue(ROLE, Role.ABOVE));
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(new Property[]{FACING, ROLE});
    }

    public static BlockPos masterPos(BlockState state, BlockPos pos) {
        Direction facing = (Direction)state.getValue((Property)FACING);
        return switch ((Role)((Object)state.getValue(ROLE))) {
            default -> throw new IncompatibleClassChangeError();
            case Role.ABOVE -> pos.below();
            case Role.HORIZONTAL -> pos.relative(facing.getOpposite());
            case Role.DIAGONAL -> pos.below().relative(facing.getOpposite());
        };
    }

    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        BlockPos masterPos;
        BlockState masterState;
        Block masterBlock;
        if (!level.isClientSide && ((masterBlock = (masterState = level.getBlockState(masterPos = EngineFillerBlock.masterPos(state, pos))).getBlock()) instanceof TripleExpansionEngineBlock || masterBlock instanceof LargeSinglePistonEngineBlock)) {
            masterBlock.playerWillDestroy(level, masterPos, masterState, player);
            level.destroyBlock(masterPos, false);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        BlockPos masterPos;
        Block masterBlock;
        if (!state.is(newState.getBlock()) && !level.isClientSide && ((masterBlock = level.getBlockState(masterPos = EngineFillerBlock.masterPos(state, pos)).getBlock()) instanceof TripleExpansionEngineBlock || masterBlock instanceof LargeSinglePistonEngineBlock)) {
            level.destroyBlock(masterPos, true);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    public static enum Role implements StringRepresentable
    {
        ABOVE,
        HORIZONTAL,
        DIAGONAL;


        public String getSerializedName() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }
}

