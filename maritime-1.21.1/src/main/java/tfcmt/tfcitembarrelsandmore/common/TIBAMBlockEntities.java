package tfcmt.tfcitembarrelsandmore.common;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import tfcmt.tfcitembarrelsandmore.common.TIBAMBlocks;
import tfcmt.tfcitembarrelsandmore.common.blockentities.AirCompressorBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blockentities.BoilerBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blockentities.BoilerTankBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blockentities.ExpellerPressBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blockentities.ExplorationLampBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blockentities.FumeDuctBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blockentities.GrinderBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blockentities.ItemBarrelBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blockentities.LargeSinglePistonEngineBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blockentities.MechanicalPowerHammerBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blockentities.PneumaticPowerHammerBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blockentities.SinglePistonEngineBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blockentities.SmokeShackBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blockentities.TripleExpansionEngineBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.BoomFillerBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.CleatBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.MastBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.MastFillerBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.YardFillerBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blockentities.rotation.CastIronAxleBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blockentities.rotation.CastIronAxleBraceBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blockentities.rotation.PropellerBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blockentities.rotation.WaterPaddleBlockEntity;
import tfcmt.tfcitembarrelsandmore.compat.create.TFCSpeedTelegraphInputBlockEntity;
import tfcmt.tfcitembarrelsandmore.compat.create.TFCSpeedTelegraphOutputBlockEntity;

public class TIBAMBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES;
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ItemBarrelBlockEntity>> ITEM_BARREL;
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BoilerBlockEntity>> BOILER;
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BoilerTankBlockEntity>> BOILER_TANK;
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BoilerTankBlockEntity>> STEEL_BOILER_TANK;
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FumeDuctBlockEntity>> FUME_DUCT;
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SinglePistonEngineBlockEntity>> SINGLE_PISTON_ENGINE;
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TripleExpansionEngineBlockEntity>> TRIPLE_EXPANSION_ENGINE;
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LargeSinglePistonEngineBlockEntity>> LARGE_SINGLE_PISTON_ENGINE;
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GrinderBlockEntity>> GRINDER;
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MechanicalPowerHammerBlockEntity>> MECHANICAL_POWER_HAMMER;
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PneumaticPowerHammerBlockEntity>> PNEUMATIC_POWER_HAMMER;
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExpellerPressBlockEntity>> EXPELLER_PRESS;
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SmokeShackBlockEntity>> SMOKE_SHACK;
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CastIronAxleBlockEntity>> CAST_IRON_AXLE;
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CastIronAxleBraceBlockEntity>> CAST_IRON_AXLE_BRACE;
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PropellerBlockEntity>> PROPELLER;
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WaterPaddleBlockEntity>> WATER_PADDLE;
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TFCSpeedTelegraphInputBlockEntity>> TFC_SPEED_TELEGRAPH_INPUT;
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TFCSpeedTelegraphOutputBlockEntity>> TFC_SPEED_TELEGRAPH_OUTPUT;
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExplorationLampBlockEntity>> EXPLORATION_LAMP;
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AirCompressorBlockEntity>> AIR_COMPRESSOR;
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MastBlockEntity>> MAST_ANCHOR;
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MastFillerBlockEntity>> MAST_FILLER;
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<YardFillerBlockEntity>> YARD_FILLER;
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BoomFillerBlockEntity>> BOOM_FILLER;
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CleatBlockEntity>> CLEAT;

    static {
        
        BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, (String)"tfc_item_barrels_and_more");
        ITEM_BARREL = BLOCK_ENTITIES.register("item_barrel", () -> BlockEntityType.Builder.of((pos, state) -> new ItemBarrelBlockEntity(TIBAMBlockEntities.ITEM_BARREL.get(), pos, state), (Block[])((Block[])TIBAMBlocks.ITEM_BARRELS.values().stream().map(DeferredHolder::get).toArray(Block[]::new))).build(null));
        BOILER = BLOCK_ENTITIES.register("boiler", () -> BlockEntityType.Builder.of(BoilerBlockEntity::new, (Block[])new Block[]{(Block)TIBAMBlocks.BOILER.get()}).build(null));
        BOILER_TANK = BLOCK_ENTITIES.register("boiler_tank", () -> BlockEntityType.Builder.of((pos, state) -> new BoilerTankBlockEntity(TIBAMBlockEntities.BOILER_TANK.get(), pos, state, BoilerTankBlockEntity.TankTier.CAST_IRON), (Block[])new Block[]{(Block)TIBAMBlocks.BOILER_TANK.get()}).build(null));
        STEEL_BOILER_TANK = BLOCK_ENTITIES.register("steel_boiler_tank", () -> BlockEntityType.Builder.of((pos, state) -> new BoilerTankBlockEntity(TIBAMBlockEntities.STEEL_BOILER_TANK.get(), pos, state, BoilerTankBlockEntity.TankTier.STEEL), (Block[])new Block[]{(Block)TIBAMBlocks.STEEL_BOILER_TANK.get()}).build(null));
        FUME_DUCT = BLOCK_ENTITIES.register("fume_hood_duct", () -> BlockEntityType.Builder.of(FumeDuctBlockEntity::new, (Block[])new Block[]{(Block)TIBAMBlocks.FUME_HOOD_DUCT.get()}).build(null));
        SINGLE_PISTON_ENGINE = BLOCK_ENTITIES.register("single_piston_engine", () -> BlockEntityType.Builder.of(SinglePistonEngineBlockEntity::new, (Block[])new Block[]{(Block)TIBAMBlocks.SINGLE_PISTON_ENGINE.get()}).build(null));
        TRIPLE_EXPANSION_ENGINE = BLOCK_ENTITIES.register("triple_expansion_engine", () -> BlockEntityType.Builder.of(TripleExpansionEngineBlockEntity::new, (Block[])new Block[]{(Block)TIBAMBlocks.TRIPLE_EXPANSION_ENGINE.get()}).build(null));
        LARGE_SINGLE_PISTON_ENGINE = BLOCK_ENTITIES.register("large_single_piston_engine", () -> BlockEntityType.Builder.of(LargeSinglePistonEngineBlockEntity::new, (Block[])new Block[]{(Block)TIBAMBlocks.LARGE_SINGLE_PISTON_ENGINE.get()}).build(null));
        GRINDER = BLOCK_ENTITIES.register("grinder", () -> BlockEntityType.Builder.of(GrinderBlockEntity::new, (Block[])new Block[]{(Block)TIBAMBlocks.GRINDER.get()}).build(null));
        MECHANICAL_POWER_HAMMER = BLOCK_ENTITIES.register("mechanical_power_hammer", () -> BlockEntityType.Builder.of(MechanicalPowerHammerBlockEntity::new, (Block[])new Block[]{(Block)TIBAMBlocks.MECHANICAL_POWER_HAMMER.get()}).build(null));
        PNEUMATIC_POWER_HAMMER = BLOCK_ENTITIES.register("pneumatic_power_hammer", () -> BlockEntityType.Builder.of(PneumaticPowerHammerBlockEntity::new, (Block[])new Block[]{(Block)TIBAMBlocks.PNEUMATIC_POWER_HAMMER.get()}).build(null));
        EXPELLER_PRESS = BLOCK_ENTITIES.register("expeller_press", () -> BlockEntityType.Builder.of(ExpellerPressBlockEntity::new, (Block[])new Block[]{(Block)TIBAMBlocks.EXPELLER_PRESS.get()}).build(null));
        SMOKE_SHACK = BLOCK_ENTITIES.register("smoke_shack", () -> BlockEntityType.Builder.of(SmokeShackBlockEntity::new, (Block[])((Block[])TIBAMBlocks.SMOKE_SHACKS.values().stream().map(DeferredHolder::get).toArray(Block[]::new))).build(null));
        CAST_IRON_AXLE = BLOCK_ENTITIES.register("cast_iron_axle", () -> BlockEntityType.Builder.of(CastIronAxleBlockEntity::new, (Block[])new Block[]{(Block)TIBAMBlocks.CAST_IRON_AXLE.get()}).build(null));
        CAST_IRON_AXLE_BRACE = BLOCK_ENTITIES.register("cast_iron_axle_brace", () -> BlockEntityType.Builder.of(CastIronAxleBraceBlockEntity::new, (Block[])new Block[]{(Block)TIBAMBlocks.CAST_IRON_AXLE_BRACE.get()}).build(null));
        PROPELLER = BLOCK_ENTITIES.register("propeller", () -> BlockEntityType.Builder.of(PropellerBlockEntity::new, (Block[])new Block[]{(Block)TIBAMBlocks.TWO_BLADE_PROPELLER.get(), (Block)TIBAMBlocks.FOUR_BLADE_PROPELLER.get()}).build(null));
        WATER_PADDLE = BLOCK_ENTITIES.register("water_paddle", () -> BlockEntityType.Builder.of(WaterPaddleBlockEntity::new, (Block[])((Block[])TIBAMBlocks.WATER_PADDLES.values().stream().map(DeferredHolder::get).toArray(Block[]::new))).build(null));
        TFC_SPEED_TELEGRAPH_INPUT = BLOCK_ENTITIES.register("tfc_speed_telegraph_input", () -> BlockEntityType.Builder.of((pos, state) -> new TFCSpeedTelegraphInputBlockEntity(pos, state), (Block[])new Block[]{(Block)TIBAMBlocks.TFC_SPEED_TELEGRAPH_INPUT.get()}).build(null));
        TFC_SPEED_TELEGRAPH_OUTPUT = BLOCK_ENTITIES.register("tfc_speed_telegraph_output", () -> BlockEntityType.Builder.of((pos, state) -> new TFCSpeedTelegraphOutputBlockEntity(TIBAMBlockEntities.TFC_SPEED_TELEGRAPH_OUTPUT.get(), pos, state), TIBAMBlocks.TFC_SPEED_TELEGRAPH_OUTPUT.get()).build(null));
        EXPLORATION_LAMP = BLOCK_ENTITIES.register("exploration_lamp", () -> BlockEntityType.Builder.of(ExplorationLampBlockEntity::new, (Block[])new Block[]{(Block)TIBAMBlocks.EXPLORATION_LAMP.get()}).build(null));
        AIR_COMPRESSOR = BLOCK_ENTITIES.register("air_compressor", () -> BlockEntityType.Builder.of(AirCompressorBlockEntity::new, (Block[])new Block[]{(Block)TIBAMBlocks.AIR_COMPRESSOR.get()}).build(null));
        MAST_ANCHOR = BLOCK_ENTITIES.register("mast_anchor", () -> BlockEntityType.Builder.of(MastBlockEntity::new, (Block[])((Block[])TIBAMBlocks.MAST_ANCHORS.values().stream().map(DeferredHolder::get).toArray(Block[]::new))).build(null));
        MAST_FILLER = BLOCK_ENTITIES.register("mast_filler", () -> BlockEntityType.Builder.of(MastFillerBlockEntity::new, (Block[])new Block[]{(Block)TIBAMBlocks.MAST_FILLER.get()}).build(null));
        YARD_FILLER = BLOCK_ENTITIES.register("yard_filler", () -> BlockEntityType.Builder.of(YardFillerBlockEntity::new, (Block[])new Block[]{(Block)TIBAMBlocks.YARD_FILLER.get()}).build(null));
        BOOM_FILLER = BLOCK_ENTITIES.register("boom_filler", () -> BlockEntityType.Builder.of(BoomFillerBlockEntity::new, (Block[])new Block[]{(Block)TIBAMBlocks.BOOM_FILLER.get()}).build(null));
        CLEAT = BLOCK_ENTITIES.register("cleat", () -> BlockEntityType.Builder.of(CleatBlockEntity::new, (Block[])((Block[])TIBAMBlocks.CLEATS.values().stream().map(DeferredHolder::get).toArray(Block[]::new))).build(null));
    }
}

