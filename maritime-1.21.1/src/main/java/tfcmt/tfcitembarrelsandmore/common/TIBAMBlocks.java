package tfcmt.tfcitembarrelsandmore.common;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.component.size.Size;
import net.dries007.tfc.common.component.size.Weight;
import net.dries007.tfc.util.registry.RegistrationHelpers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import tfcmt.tfcitembarrelsandmore.common.TIBAMBlockEntities;
import tfcmt.tfcitembarrelsandmore.common.TIBAMItems;
import tfcmt.tfcitembarrelsandmore.common.blockentities.AirCompressorBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blockentities.BoilerBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blockentities.BoilerTankBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blockentities.ExpellerPressBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blockentities.ExplorationLampBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blockentities.GrinderBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blockentities.LargeSinglePistonEngineBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blockentities.MechanicalPowerHammerBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blockentities.SinglePistonEngineBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blockentities.SmokeShackBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blockentities.TripleExpansionEngineBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.AirCompressorBlock;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.BoilerBlock;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.BoilerTankBlock;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.EngineFillerBlock;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.ExpellerPressBlock;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.ExplorationLampBlock;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.FumeDuctBlock;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.GrinderBlock;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.ItemBarrelBlock;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.LargeSinglePistonEngineBlock;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.MechanicalPowerHammerBlock;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.PneumaticPowerHammerBlock;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.SinglePistonEngineBlock;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.SmokeShackBlock;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.TripleExpansionEngineBlock;
import tfcmt.tfcitembarrelsandmore.common.blocks.mast.BoomFillerBlock;
import tfcmt.tfcitembarrelsandmore.common.blocks.mast.CleatBlock;
import tfcmt.tfcitembarrelsandmore.common.blocks.mast.MastAnchorBlock;
import tfcmt.tfcitembarrelsandmore.common.blocks.mast.MastFillerBlock;
import tfcmt.tfcitembarrelsandmore.common.blocks.mast.YardFillerBlock;
import tfcmt.tfcitembarrelsandmore.common.blocks.rotation.CastIronAxleBlock;
import tfcmt.tfcitembarrelsandmore.common.blocks.rotation.CastIronAxleBraceBlock;
import tfcmt.tfcitembarrelsandmore.common.blocks.rotation.PropellerBlock;
import tfcmt.tfcitembarrelsandmore.common.blocks.rotation.WaterPaddleBlock;
import tfcmt.tfcitembarrelsandmore.common.items.AirCompressorBlockItem;
import tfcmt.tfcitembarrelsandmore.common.items.ExplorationLampBlockItem;
import tfcmt.tfcitembarrelsandmore.common.items.MastBlockItem;
import tfcmt.tfcitembarrelsandmore.common.items.SizedBlockItem;
import tfcmt.tfcitembarrelsandmore.compat.create.TFCSpeedTelegraphInputBlock;
import tfcmt.tfcitembarrelsandmore.compat.create.TFCSpeedTelegraphInputItem;
import tfcmt.tfcitembarrelsandmore.compat.create.TFCSpeedTelegraphOutputBlock;

public class TIBAMBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, (String)"tfc_item_barrels_and_more");
    public static final Map<Wood, DeferredHolder<Block, Block>> ITEM_BARRELS = Arrays.stream(Wood.values()).collect(Collectors.toMap(wood -> wood, wood -> TIBAMBlocks.register("wood/" + wood.getId() + "/item_barrel", () -> new ItemBarrelBlock(ExtendedProperties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD).strength(2.0f).noOcclusion()))));
    public static final Map<Wood, DeferredHolder<Block, Block>> SMOKE_SHACKS = Arrays.stream(Wood.values()).collect(Collectors.toMap(wood -> wood, wood -> TIBAMBlocks.registerSmokeShack("wood/" + wood.getId() + "/smoke_shack", () -> new SmokeShackBlock(ExtendedProperties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD).strength(2.0f).noOcclusion().blockEntity(TIBAMBlockEntities.SMOKE_SHACK).ticks(SmokeShackBlockEntity::serverTick)))));
    public static final DeferredHolder<Block, Block> BOILER = TIBAMBlocks.register("boiler", () -> new BoilerBlock(ExtendedProperties.of().mapColor(MapColor.METAL).sound(SoundType.NETHERITE_BLOCK).strength(4.0f).noOcclusion().blockEntity(TIBAMBlockEntities.BOILER).ticks(BoilerBlockEntity::serverTick)));
    public static final DeferredHolder<Block, Block> BOILER_TANK = TIBAMBlocks.register("boiler_tank", () -> new BoilerTankBlock(ExtendedProperties.of().mapColor(MapColor.METAL).sound(SoundType.NETHERITE_BLOCK).strength(4.0f).noOcclusion().blockEntity(TIBAMBlockEntities.BOILER_TANK).ticks(BoilerTankBlockEntity::serverTick)));
    public static final DeferredHolder<Block, Block> STEEL_BOILER_TANK = TIBAMBlocks.register("steel_boiler_tank", () -> new BoilerTankBlock(ExtendedProperties.of().mapColor(MapColor.METAL).sound(SoundType.NETHERITE_BLOCK).strength(4.0f).noOcclusion().blockEntity(TIBAMBlockEntities.STEEL_BOILER_TANK).ticks(BoilerTankBlockEntity::serverTick)));
    public static final DeferredHolder<Block, Block> FUME_HOOD_DUCT = TIBAMBlocks.registerParts("fume_hood_duct", () -> new FumeDuctBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.NETHERITE_BLOCK).strength(3.0f).noOcclusion()));
    public static final DeferredHolder<Block, Block> SINGLE_PISTON_ENGINE = TIBAMBlocks.register("single_piston_engine", () -> new SinglePistonEngineBlock(ExtendedProperties.of().mapColor(MapColor.GOLD).sound(SoundType.COPPER).strength(3.5f).noOcclusion().blockEntity(TIBAMBlockEntities.SINGLE_PISTON_ENGINE).ticks(SinglePistonEngineBlockEntity::serverTick, SinglePistonEngineBlockEntity::clientTick)));
    public static final DeferredHolder<Block, Block> TRIPLE_EXPANSION_ENGINE = TIBAMBlocks.register("triple_expansion_engine", () -> new TripleExpansionEngineBlock(ExtendedProperties.of().mapColor(MapColor.METAL).sound(SoundType.NETHERITE_BLOCK).strength(5.0f).noOcclusion().blockEntity(TIBAMBlockEntities.TRIPLE_EXPANSION_ENGINE).ticks(TripleExpansionEngineBlockEntity::clientTick)));
    public static final DeferredHolder<Block, Block> LARGE_SINGLE_PISTON_ENGINE = TIBAMBlocks.register("large_single_piston_engine", () -> new LargeSinglePistonEngineBlock(ExtendedProperties.of().mapColor(MapColor.METAL).sound(SoundType.NETHERITE_BLOCK).strength(5.0f).noOcclusion().blockEntity(TIBAMBlockEntities.LARGE_SINGLE_PISTON_ENGINE).ticks(LargeSinglePistonEngineBlockEntity::clientTick)));
    public static final DeferredHolder<Block, Block> ENGINE_FILLER = TIBAMBlocks.registerNoItem("engine_filler", () -> new EngineFillerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.NETHERITE_BLOCK).strength(5.0f).requiresCorrectToolForDrops().noOcclusion().noLootTable()));
    public static final DeferredHolder<Block, Block> GRINDER = TIBAMBlocks.register("grinder", () -> new GrinderBlock(ExtendedProperties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).strength(3.0f).noOcclusion().blockEntity(TIBAMBlockEntities.GRINDER).ticks(GrinderBlockEntity::serverTick)));
    public static final DeferredHolder<Block, Block> MECHANICAL_POWER_HAMMER = TIBAMBlocks.register("mechanical_power_hammer", () -> new MechanicalPowerHammerBlock(ExtendedProperties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).strength(4.0f).noOcclusion().blockEntity(TIBAMBlockEntities.MECHANICAL_POWER_HAMMER).ticks(MechanicalPowerHammerBlockEntity::serverTick, MechanicalPowerHammerBlockEntity::clientTick)));
    public static final DeferredHolder<Block, Block> PNEUMATIC_POWER_HAMMER = TIBAMBlocks.register("pneumatic_power_hammer", () -> new PneumaticPowerHammerBlock(ExtendedProperties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).strength(4.0f).noOcclusion().blockEntity(TIBAMBlockEntities.PNEUMATIC_POWER_HAMMER).ticks(MechanicalPowerHammerBlockEntity::serverTick, MechanicalPowerHammerBlockEntity::clientTick)));
    public static final DeferredHolder<Block, Block> EXPELLER_PRESS = TIBAMBlocks.register("expeller_press", () -> new ExpellerPressBlock(ExtendedProperties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).strength(3.0f).noOcclusion().blockEntity(TIBAMBlockEntities.EXPELLER_PRESS).ticks(ExpellerPressBlockEntity::serverTick)));
    public static final DeferredHolder<Block, Block> CAST_IRON_AXLE = TIBAMBlocks.registerParts("cast_iron_axle", () -> new CastIronAxleBlock(ExtendedProperties.of().mapColor(MapColor.METAL).sound(SoundType.NETHERITE_BLOCK).strength(5.0f).noOcclusion().blockEntity(TIBAMBlockEntities.CAST_IRON_AXLE)));
    public static final DeferredHolder<Block, Block> CAST_IRON_AXLE_BRACE = TIBAMBlocks.registerParts("cast_iron_axle_brace", () -> new CastIronAxleBraceBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.NETHERITE_BLOCK).strength(5.0f).noOcclusion()));
    public static final DeferredHolder<Block, Block> TWO_BLADE_PROPELLER = TIBAMBlocks.register("two_blade_propeller", () -> new PropellerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).sound(SoundType.COPPER).strength(3.0f).noOcclusion(), false));
    public static final DeferredHolder<Block, Block> FOUR_BLADE_PROPELLER = TIBAMBlocks.register("four_blade_propeller", () -> new PropellerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).sound(SoundType.COPPER).strength(3.0f).noOcclusion(), true));
    public static final Map<Wood, DeferredHolder<Block, Block>> WATER_PADDLES = Arrays.stream(Wood.values()).collect(Collectors.toMap(wood -> wood, wood -> TIBAMBlocks.register("wood/" + wood.getId() + "/water_paddle", () -> new WaterPaddleBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD).strength(4.0f).noOcclusion(), (Wood)((Object)wood)))));
    public static final DeferredHolder<Block, Block> TFC_SPEED_TELEGRAPH_INPUT = RegistrationHelpers.registerBlock(BLOCKS, TIBAMItems.ITEMS, (String)"tfc_speed_telegraph_input", () -> new TFCSpeedTelegraphInputBlock(ExtendedProperties.of().mapColor(MapColor.METAL).sound(SoundType.COPPER).strength(3.0f).noOcclusion().blockEntity(TIBAMBlockEntities.TFC_SPEED_TELEGRAPH_INPUT)), item -> new TFCSpeedTelegraphInputItem((Block)item, new Item.Properties(), Size.HUGE, Weight.VERY_HEAVY));
    public static final DeferredHolder<Block, Block> TFC_SPEED_TELEGRAPH_OUTPUT = RegistrationHelpers.registerBlock(BLOCKS, TIBAMItems.ITEMS, (String)"tfc_speed_telegraph_output", () -> new TFCSpeedTelegraphOutputBlock(ExtendedProperties.of().mapColor(MapColor.METAL).sound(SoundType.COPPER).strength(3.0f).noOcclusion().blockEntity(TIBAMBlockEntities.TFC_SPEED_TELEGRAPH_OUTPUT)), item -> new SizedBlockItem((Block)item, new Item.Properties(), Size.HUGE, Weight.VERY_HEAVY));
    public static final DeferredHolder<Block, Block> EXPLORATION_LAMP = RegistrationHelpers.registerBlock(BLOCKS, TIBAMItems.ITEMS, (String)"exploration_lamp", () -> new ExplorationLampBlock(ExtendedProperties.of().mapColor(MapColor.METAL).sound(SoundType.LANTERN).strength(2.0f).noOcclusion().lightLevel(ExplorationLampBlock::getLightEmission).blockEntity(TIBAMBlockEntities.EXPLORATION_LAMP).ticks(ExplorationLampBlockEntity::serverTick)), item -> new ExplorationLampBlockItem((Block)item, new Item.Properties()));
    public static final DeferredHolder<Block, Block> AIR_COMPRESSOR = RegistrationHelpers.registerBlock(BLOCKS, TIBAMItems.ITEMS, (String)"air_compressor", () -> new AirCompressorBlock(ExtendedProperties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).strength(4.0f).noOcclusion().blockEntity(TIBAMBlockEntities.AIR_COMPRESSOR).ticks(AirCompressorBlockEntity::serverTick)), item -> new AirCompressorBlockItem((Block)item, new Item.Properties(), Size.HUGE, Weight.VERY_HEAVY));
    public static final Map<Wood, DeferredHolder<Block, Block>> MAST_ANCHORS = Arrays.stream(Wood.values()).collect(Collectors.toMap(wood -> wood, wood -> RegistrationHelpers.registerBlock(BLOCKS, TIBAMItems.ITEMS, (String)("wood/" + wood.getId() + "/mast"), () -> new MastAnchorBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD).strength(3.0f).noOcclusion(), (Wood)((Object)wood)), item -> new MastBlockItem((Block)item, new Item.Properties(), (Wood)((Object)wood), Size.HUGE, Weight.LIGHT))));
    public static final DeferredHolder<Block, Block> MAST_FILLER = TIBAMBlocks.registerNoItem("mast_filler", () -> new MastFillerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD).strength(3.0f).noOcclusion().noLootTable()));
    public static final DeferredHolder<Block, Block> YARD_FILLER = TIBAMBlocks.registerNoItem("yard_filler", () -> new YardFillerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD).strength(3.0f).noOcclusion().noLootTable()));
    public static final DeferredHolder<Block, Block> BOOM_FILLER = TIBAMBlocks.registerNoItem("boom_filler", () -> new BoomFillerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD).strength(3.0f).noOcclusion().noLootTable()));
    public static final Map<Metal, DeferredHolder<Block, Block>> CLEATS = Arrays.stream(Metal.values()).collect(Collectors.toMap(metal -> metal, metal -> TIBAMBlocks.register("metal/" + metal.getId() + "/cleat", () -> new CleatBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).strength(2.0f).noOcclusion()))));

    private static <T extends Block> DeferredHolder<Block, T> register(String name, Supplier<T> block) {
        return RegistrationHelpers.registerBlock(BLOCKS, TIBAMItems.ITEMS, (String)name, block, item -> new SizedBlockItem((Block)item, new Item.Properties(), Size.HUGE, Weight.VERY_HEAVY));
    }

    // TFC 4 validates recipe output counts against the item's weight-derived stack limit.
    private static <T extends Block> DeferredHolder<Block, T> registerParts(String name, Supplier<T> block) {
        return RegistrationHelpers.registerBlock(BLOCKS, TIBAMItems.ITEMS, name, block,
            item -> new SizedBlockItem(item, new Item.Properties(), Size.HUGE, Weight.HEAVY));
    }

    private static <T extends Block> DeferredHolder<Block, T> registerNoItem(String name, Supplier<T> block) {
        return RegistrationHelpers.registerBlock(BLOCKS, TIBAMItems.ITEMS, (String)name, block, null);
    }

    private static <T extends Block> DeferredHolder<Block, T> registerSmokeShack(String name, Supplier<T> block) {
        return RegistrationHelpers.registerBlock(BLOCKS, TIBAMItems.ITEMS, (String)name, block, item -> new SizedBlockItem((Block)item, new Item.Properties(), Size.HUGE, Weight.VERY_HEAVY));
    }

    public static enum Metal {
        BISMUTH,
        BISMUTH_BRONZE,
        BLACK_BRONZE,
        BRASS,
        BRONZE,
        CAST_IRON,
        COPPER,
        GOLD,
        NICKEL,
        RED_STEEL,
        ROSE_GOLD,
        SILVER,
        STEEL,
        TIN,
        WROUGHT_IRON,
        ZINC,
        BLUE_STEEL;

        private final ResourceLocation texture = ResourceLocation.fromNamespaceAndPath("tfc", "block/metal/smooth/" + this.getId());

        public String getId() {
            return this.name().toLowerCase();
        }

        public ResourceLocation texture() {
            return this.texture;
        }
    }

    public static enum Wood {
        ACACIA("tfc"),
        ASH("tfc"),
        ASPEN("tfc"),
        BIRCH("tfc"),
        BLACKWOOD("tfc"),
        CHESTNUT("tfc"),
        DOUGLAS_FIR("tfc"),
        HICKORY("tfc"),
        KAPOK("tfc"),
        MANGROVE("tfc"),
        MAPLE("tfc"),
        OAK("tfc"),
        PALM("tfc"),
        PINE("tfc"),
        ROSEWOOD("tfc"),
        SEQUOIA("tfc"),
        SPRUCE("tfc"),
        SYCAMORE("tfc"),
        WHITE_CEDAR("tfc"),
        WILLOW("tfc"),
        BAOBAB("afc"),
        CYPRESS("afc"),
        EUCALYPTUS("afc"),
        FIG("afc"),
        HEVEA("afc"),
        IPE("afc"),
        IRONWOOD("afc"),
        MAHOGANY("afc"),
        TEAK("afc"),
        TUALANG("afc");

        private final String mod;
        private final ResourceLocation strippedLogTexture;
        private final ResourceLocation plankTexture;
        private static final Map<String, Wood> BY_KEY;

        private Wood(String mod) {
            this.mod = mod;
            this.strippedLogTexture = ResourceLocation.fromNamespaceAndPath(mod, "block/wood/stripped_log/" + this.getId());
            this.plankTexture = ResourceLocation.fromNamespaceAndPath(mod, "block/wood/planks/" + this.getId());
        }

        public String getId() {
            return this.name().toLowerCase();
        }

        public String getMod() {
            return this.mod;
        }

        public ResourceLocation strippedLogTexture() {
            return this.strippedLogTexture;
        }

        public ResourceLocation plankTexture() {
            return this.plankTexture;
        }

        public static Wood byKey(String mod, String id) {
            return BY_KEY.get(mod + ":" + id);
        }

        static {
            BY_KEY = Arrays.stream(Wood.values()).collect(Collectors.toMap(w -> w.mod + ":" + w.getId(), w -> w));
        }
    }
}

