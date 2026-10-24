package tfcmt.tfcitembarrelsandmore.client;

import java.util.Map;
import net.dries007.tfc.client.model.entity.WaterWheelModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import tfcmt.tfcitembarrelsandmore.client.BraceClientCache;
import tfcmt.tfcitembarrelsandmore.client.model.DiveSuitModel;
import tfcmt.tfcitembarrelsandmore.client.model.TFCBarrelBracedModel;
import tfcmt.tfcitembarrelsandmore.client.renderer.AirCompressorBlockEntityRenderer;
import tfcmt.tfcitembarrelsandmore.client.renderer.BoilerBlockEntityRenderer;
import tfcmt.tfcitembarrelsandmore.client.renderer.CastIronAxleBlockEntityRenderer;
import tfcmt.tfcitembarrelsandmore.client.renderer.CastIronAxleBraceRenderer;
import tfcmt.tfcitembarrelsandmore.client.renderer.ExpellerPressBlockEntityRenderer;
import tfcmt.tfcitembarrelsandmore.client.renderer.ExplorationLampBER;
import tfcmt.tfcitembarrelsandmore.client.renderer.GrinderBlockEntityRenderer;
import tfcmt.tfcitembarrelsandmore.client.renderer.ItemBarrelRenderer;
import tfcmt.tfcitembarrelsandmore.client.renderer.LargeSinglePistonEngineBlockEntityRenderer;
import tfcmt.tfcitembarrelsandmore.client.renderer.MechanicalPowerHammerBER;
import tfcmt.tfcitembarrelsandmore.client.renderer.PneumaticPowerHammerBER;
import tfcmt.tfcitembarrelsandmore.client.renderer.PropellerBlockEntityRenderer;
import tfcmt.tfcitembarrelsandmore.client.renderer.SinglePistonEngineBlockEntityRenderer;
import tfcmt.tfcitembarrelsandmore.client.renderer.TripleExpansionEngineBlockEntityRenderer;
import tfcmt.tfcitembarrelsandmore.client.renderer.WaterPaddleBlockEntityRenderer;
import tfcmt.tfcitembarrelsandmore.client.renderer.mast.BoomCellRenderer;
import tfcmt.tfcitembarrelsandmore.client.renderer.mast.MastCellRenderer;
import tfcmt.tfcitembarrelsandmore.client.renderer.mast.YardCellRenderer;
import tfcmt.tfcitembarrelsandmore.client.screen.AirCompressorScreen;
import tfcmt.tfcitembarrelsandmore.client.screen.BoilerScreen;
import tfcmt.tfcitembarrelsandmore.client.screen.BoilerTankScreen;
import tfcmt.tfcitembarrelsandmore.client.screen.ExpellerPressScreen;
import tfcmt.tfcitembarrelsandmore.client.screen.ItemBarrelScreen;
import tfcmt.tfcitembarrelsandmore.client.screen.MechanicalPowerHammerScreen;
import tfcmt.tfcitembarrelsandmore.client.screen.SinglePistonEngineScreen;
import tfcmt.tfcitembarrelsandmore.client.screen.SmokeShackScreen;
import tfcmt.tfcitembarrelsandmore.common.TIBAMBlockEntities;
import tfcmt.tfcitembarrelsandmore.common.TIBAMBlocks;
import tfcmt.tfcitembarrelsandmore.common.TIBAMContainerTypes;
import tfcmt.tfcitembarrelsandmore.common.TIBAMEntities;
import tfcmt.tfcitembarrelsandmore.common.blockentities.ExplorationLampBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.items.ExplorationLampBlockItem;
import tfcmt.tfcitembarrelsandmore.compat.create.TFCSpeedTelegraphInputRenderer;
import tfcmt.tfcitembarrelsandmore.compat.create.TFCSpeedTelegraphOutputRenderer;
import tfcmt.tfcitembarrelsandmore.tfcitembarrelsandmore;

@EventBusSubscriber(modid="tfc_item_barrels_and_more", bus=EventBusSubscriber.Bus.MOD, value={Dist.CLIENT})
public class TIBAMClientEvents {
    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            NeoForge.EVENT_BUS.addListener(TIBAMClientEvents::onPlayerLogout);
            BlockEntityRenderers.register((BlockEntityType)(TIBAMBlockEntities.BOILER.get()), BoilerBlockEntityRenderer::new);
            BlockEntityRenderers.register((BlockEntityType)(TIBAMBlockEntities.ITEM_BARREL.get()), ItemBarrelRenderer::new);
            BlockEntityRenderers.register((BlockEntityType)(TIBAMBlockEntities.GRINDER.get()), GrinderBlockEntityRenderer::new);
            BlockEntityRenderers.register((BlockEntityType)(TIBAMBlockEntities.MECHANICAL_POWER_HAMMER.get()), MechanicalPowerHammerBER::new);
            BlockEntityRenderers.register((BlockEntityType)(TIBAMBlockEntities.PNEUMATIC_POWER_HAMMER.get()), PneumaticPowerHammerBER::new);
            BlockEntityRenderers.register((BlockEntityType)(TIBAMBlockEntities.EXPELLER_PRESS.get()), ExpellerPressBlockEntityRenderer::new);
            BlockEntityRenderers.register((BlockEntityType)(TIBAMBlockEntities.SINGLE_PISTON_ENGINE.get()), SinglePistonEngineBlockEntityRenderer::new);
            BlockEntityRenderers.register((BlockEntityType)(TIBAMBlockEntities.LARGE_SINGLE_PISTON_ENGINE.get()), LargeSinglePistonEngineBlockEntityRenderer::new);
            BlockEntityRenderers.register((BlockEntityType)(TIBAMBlockEntities.TRIPLE_EXPANSION_ENGINE.get()), TripleExpansionEngineBlockEntityRenderer::new);
            BlockEntityRenderers.register((BlockEntityType)(TIBAMBlockEntities.CAST_IRON_AXLE.get()), CastIronAxleBlockEntityRenderer::new);
            BlockEntityRenderers.register((BlockEntityType)(TIBAMBlockEntities.CAST_IRON_AXLE_BRACE.get()), CastIronAxleBraceRenderer::new);
            BlockEntityType propellerType = TIBAMBlockEntities.PROPELLER.get();
            BlockEntityRenderers.register((BlockEntityType)propellerType, PropellerBlockEntityRenderer::new);
            BlockEntityRenderers.register((BlockEntityType)(TIBAMBlockEntities.WATER_PADDLE.get()), WaterPaddleBlockEntityRenderer::new);
            BlockEntityRenderers.register((BlockEntityType)(TIBAMBlockEntities.MAST_ANCHOR.get()), MastCellRenderer::new);
            BlockEntityRenderers.register((BlockEntityType)(TIBAMBlockEntities.MAST_FILLER.get()), MastCellRenderer::new);
            BlockEntityRenderers.register((BlockEntityType)(TIBAMBlockEntities.YARD_FILLER.get()), YardCellRenderer::new);
            BlockEntityRenderers.register((BlockEntityType)(TIBAMBlockEntities.BOOM_FILLER.get()), BoomCellRenderer::new);
            BlockEntityRenderers.register((BlockEntityType)(TIBAMBlockEntities.AIR_COMPRESSOR.get()), AirCompressorBlockEntityRenderer::new);
            BlockEntityRenderers.register((BlockEntityType)(TIBAMBlockEntities.EXPLORATION_LAMP.get()), ExplorationLampBER::new);
            BlockEntityRenderers.register((BlockEntityType)(TIBAMBlockEntities.TFC_SPEED_TELEGRAPH_INPUT.get()), TFCSpeedTelegraphInputRenderer::new);
            BlockEntityRenderers.register((BlockEntityType)(TIBAMBlockEntities.TFC_SPEED_TELEGRAPH_OUTPUT.get()), TFCSpeedTelegraphOutputRenderer::new);
            if (ModList.get().isLoaded("create")) {
                tfcmt.tfcitembarrelsandmore.client.compat.CreateClient.registerRenderers();
            }
            if (ModList.get().isLoaded("sodiumdynamiclights")) {
                tfcmt.tfcitembarrelsandmore.client.compat.DynamicLightsClient.register();
            }
            if (ModList.get().isLoaded("curios")) {
                tfcmt.tfcitembarrelsandmore.client.compat.CuriosClient.registerRenderers();
            }
            TIBAMBlocks.ITEM_BARRELS.forEach((wood, blockRO) -> ItemProperties.register((Item)((Block)blockRO.get()).asItem(), (ResourceLocation)ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "sealed"), (stack, level, entity, seed) -> {
                CompoundTag tag;
                if (tfcmt.tfcitembarrelsandmore.common.MaritimeItemData.has(stack) && tfcmt.tfcitembarrelsandmore.common.MaritimeItemData.get(stack).contains("BlockEntityTag") && (tag = tfcmt.tfcitembarrelsandmore.common.MaritimeItemData.get(stack).getCompound("BlockEntityTag")).contains("Sealed") && tag.getBoolean("Sealed")) {
                    return 1.0f;
                }
                return 0.0f;
            }));
            ItemProperties.register((Item)((Block)TIBAMBlocks.EXPLORATION_LAMP.get()).asItem(), (ResourceLocation)ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "lamp_lit"), (stack, level, entity, seed) -> ExplorationLampBlockItem.isLit(stack) ? 1.0f : 0.0f);
            ItemProperties.register((Item)((Block)TIBAMBlocks.EXPLORATION_LAMP.get()).asItem(), (ResourceLocation)ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "lamp_mode"), (stack, level, entity, seed) -> ExplorationLampBlockItem.getModeFromStack(stack) == ExplorationLampBlockEntity.Mode.SEARCH ? 1.0f : 0.0f);
        });
    }

    @SubscribeEvent
    public static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(WaterPaddleBlockEntityRenderer.WATER_PADDLE_LAYER, WaterWheelModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void onRegisterEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer((EntityType)TIBAMEntities.SOUNDING_LINE.get(), ThrownItemRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterAdditionalModels(ModelEvent.RegisterAdditional event) {
        event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/single_piston_engine_rotating")));
        event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/single_piston_engine_shaft")));
        event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/large_single_piston_engine_crank")));
        event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/large_single_piston_engine_pivot")));
        event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/large_single_piston_engine_lever")));
        event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/large_single_piston_engine_piston_near")));
        event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/large_single_piston_engine_piston_far")));
        event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/triple_expansion_engine_crank")));
        for (String string : new String[]{"upper_shaft", "lower_shaft"}) {
            for (String cyl : new String[]{"a", "b", "c"}) {
                event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/triple_expansion_engine_" + string + "_" + cyl)));
            }
        }
        event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/speed_telegraph_input_handle")));
        event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/speed_telegraph_input_red")));
        event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/speed_telegraph_output_handle")));
        event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/speed_telegraph_output_red")));
        event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/cast_iron_axle")));
        event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/cast_iron_axle_brace")));
        event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/exploration_lamp_area")));
        event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/exploration_lamp_search")));
        event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(PropellerBlockEntityRenderer.TWO_BLADE_TEXTURE));
        event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(PropellerBlockEntityRenderer.FOUR_BLADE_TEXTURE));
        for (TIBAMBlocks.Wood wood : TIBAMBlocks.Wood.values()) {
            String w = wood.getId();
            event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/wood/" + w + "/item_barrel_side_braced")));
            event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/wood/" + w + "/item_barrel_side_braced_sealed")));
            event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/grinder_rotating_parts")));
            event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/mechanical_power_hammer_flywheel")));
            event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/air_compressor_flywheel")));
            event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/mechanical_power_hammer_arm")));
            event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/mechanical_power_hammer_piston")));
            event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/mechanical_power_hammer_piston_bismuth_bronze")));
            event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/mechanical_power_hammer_piston_black_bronze")));
            event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/mechanical_power_hammer_piston_bronze")));
            event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/mechanical_power_hammer_piston_copper")));
            event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/mechanical_power_hammer_piston_wrought_iron")));
            event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/mechanical_power_hammer_piston_steel")));
            event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/mechanical_power_hammer_piston_black_steel")));
            event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/mechanical_power_hammer_piston_blue_steel")));
            event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/mechanical_power_hammer_piston_red_steel")));
            event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/mechanical_power_hammer_anvil_bismuth_bronze")));
            event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/mechanical_power_hammer_anvil_black_bronze")));
            event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/mechanical_power_hammer_anvil_bronze")));
            event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/mechanical_power_hammer_anvil_copper")));
            event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/mechanical_power_hammer_anvil_wrought_iron")));
            event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/mechanical_power_hammer_anvil_steel")));
            event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/mechanical_power_hammer_anvil_black_steel")));
            event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/mechanical_power_hammer_anvil_blue_steel")));
            event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/mechanical_power_hammer_anvil_red_steel")));
            event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/pneumatic_power_hammer_piston")));
            event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/pneumatic_power_hammer_piston_bismuth_bronze")));
            event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/pneumatic_power_hammer_piston_black_bronze")));
            event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/pneumatic_power_hammer_piston_bronze")));
            event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/pneumatic_power_hammer_piston_copper")));
            event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/pneumatic_power_hammer_piston_wrought_iron")));
            event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/pneumatic_power_hammer_piston_steel")));
            event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/pneumatic_power_hammer_piston_black_steel")));
            event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/pneumatic_power_hammer_piston_blue_steel")));
            event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/pneumatic_power_hammer_piston_red_steel")));
            event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/grinder_rotating/screw_wrought_iron")));
            event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/grinder_rotating/screw_steel")));
            event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/grinder_rotating/screw_black_steel")));
            event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/grinder_rotating/screw_blue_steel")));
            event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/grinder_rotating/screw_red_steel")));
            event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/grinder_rotating_parts_ns")));
            event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/grinder_rotating_ns/screw_wrought_iron")));
            event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/grinder_rotating_ns/screw_steel")));
            event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/grinder_rotating_ns/screw_black_steel")));
            event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/grinder_rotating_ns/screw_blue_steel")));
            event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/grinder_rotating_ns/screw_red_steel")));
            event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/grinder_rotating_parts_ew_flip")));
            event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/grinder_rotating_ew_flip/screw_wrought_iron")));
            event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/grinder_rotating_ew_flip/screw_steel")));
            event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/grinder_rotating_ew_flip/screw_black_steel")));
            event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/grinder_rotating_ew_flip/screw_blue_steel")));
            event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/grinder_rotating_ew_flip/screw_red_steel")));
            event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/expeller_press_rotating_parts")));
            event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/expeller_press_rotating/screw_wrought_iron")));
            event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/expeller_press_rotating/screw_steel")));
            event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/expeller_press_rotating/screw_black_steel")));
            event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/expeller_press_rotating/screw_blue_steel")));
            event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/expeller_press_rotating/screw_red_steel")));
            event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/expeller_press_rotating_parts_ns")));
            event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/expeller_press_rotating_ns/screw_wrought_iron")));
            event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/expeller_press_rotating_ns/screw_steel")));
            event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/expeller_press_rotating_ns/screw_black_steel")));
            event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/expeller_press_rotating_ns/screw_blue_steel")));
            event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/expeller_press_rotating_ns/screw_red_steel")));
            event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/expeller_press_rotating_parts_ew_flip")));
            event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/expeller_press_rotating_ew_flip/screw_wrought_iron")));
            event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/expeller_press_rotating_ew_flip/screw_steel")));
            event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/expeller_press_rotating_ew_flip/screw_black_steel")));
            event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/expeller_press_rotating_ew_flip/screw_blue_steel")));
            event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/expeller_press_rotating_ew_flip/screw_red_steel")));
        }
    }

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(DiveSuitModel.HELMET_LAYER, DiveSuitModel::createHelmetLayer);
        event.registerLayerDefinition(DiveSuitModel.CHEST_LAYER, DiveSuitModel::createChestLayer);
        event.registerLayerDefinition(DiveSuitModel.LEGGINGS_LAYER, DiveSuitModel::createLeggingsLayer);
        event.registerLayerDefinition(DiveSuitModel.BOOTS_LAYER, DiveSuitModel::createBootsLayer);
    }

    @SubscribeEvent
    public static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        Map models = event.getModels();
        String[] facings = new String[]{"east", "north", "south", "up", "west"};
        String[] poweredValues = new String[]{"false", "true"};
        String[] rackValues = new String[]{"false", "true"};
        String[] sealedValues = new String[]{"false", "true"};
        int wrappedCount = 0;
        int bracedFound = 0;
        for (TIBAMBlocks.Wood wood : TIBAMBlocks.Wood.values()) {
            ResourceLocation blockId = ResourceLocation.fromNamespaceAndPath(wood.getMod(), "wood/barrel/" + wood.getId());
            ResourceLocation ourBlockId = ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "wood/" + wood.getId() + "/item_barrel");
            for (String powered : poweredValues) {
                for (String rack : rackValues) {
                    for (String sealed : sealedValues) {
                        for (String facing : facings) {
                            boolean isSide = !facing.equals("up");
                            String variant = "facing=" + facing + ",powered=" + powered + ",rack=" + rack + ",sealed=" + sealed;
                            ModelResourceLocation mrl = new ModelResourceLocation(blockId, variant);
                            BakedModel orig = (BakedModel)models.get(mrl);
                            if (orig == null) continue;
                            BakedModel bracedModel = null;
                            if (isSide) {
                                String bracedVariant = "braced=true,facing=" + facing + ",powered=false,sealed=" + sealed;
                                ModelResourceLocation bracedMrl = new ModelResourceLocation(ourBlockId, bracedVariant);
                                bracedModel = (BakedModel)models.get(bracedMrl);
                            }
                            models.put(mrl, new TFCBarrelBracedModel(orig, bracedModel));
                            ++wrappedCount;
                            if (bracedModel == null) continue;
                            ++bracedFound;
                        }
                    }
                }
            }
        }
        tfcitembarrelsandmore.LOGGER.info("[TIBAM] wrapped {} barrel variants ({} TFC + AFC), {} with braced model", new Object[]{wrappedCount, wrappedCount, bracedFound});
    }

    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        BraceClientCache.clear();
    }

    @SubscribeEvent
    public static void registerScreens(net.neoforged.neoforge.client.event.RegisterMenuScreensEvent event) {
            event.register((MenuType)((MenuType)TIBAMContainerTypes.BOILER.get()), BoilerScreen::new);
            event.register((MenuType)((MenuType)TIBAMContainerTypes.SINGLE_PISTON_ENGINE.get()), SinglePistonEngineScreen::new);
            event.register((MenuType)((MenuType)TIBAMContainerTypes.BOILER_TANK.get()), BoilerTankScreen::new);
            event.register((MenuType)((MenuType)TIBAMContainerTypes.ITEM_BARREL.get()), ItemBarrelScreen::new);
            event.register((MenuType)((MenuType)TIBAMContainerTypes.EXPELLER_PRESS.get()), ExpellerPressScreen::new);
            event.register((MenuType)((MenuType)TIBAMContainerTypes.SMOKE_SHACK.get()), SmokeShackScreen::new);
            event.register((MenuType)((MenuType)TIBAMContainerTypes.MECHANICAL_POWER_HAMMER.get()), MechanicalPowerHammerScreen::new);
            event.register((MenuType)((MenuType)TIBAMContainerTypes.PNEUMATIC_POWER_HAMMER.get()), MechanicalPowerHammerScreen::new);
            event.register((MenuType)((MenuType)TIBAMContainerTypes.AIR_COMPRESSOR.get()), AirCompressorScreen::new);
    }
}

