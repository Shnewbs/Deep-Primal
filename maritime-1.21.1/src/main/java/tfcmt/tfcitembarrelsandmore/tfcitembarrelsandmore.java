package tfcmt.tfcitembarrelsandmore;

import com.mojang.logging.LogUtils;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;

import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.ModContainer;
import org.slf4j.Logger;
import tfcmt.tfcitembarrelsandmore.common.TIBAMBlockEntities;
import tfcmt.tfcitembarrelsandmore.common.TIBAMBlocks;
import tfcmt.tfcitembarrelsandmore.common.TIBAMCommonEvents;
import tfcmt.tfcitembarrelsandmore.common.TIBAMConfig;
import tfcmt.tfcitembarrelsandmore.common.TIBAMContainerTypes;
import tfcmt.tfcitembarrelsandmore.common.TIBAMCreativeTab;
import tfcmt.tfcitembarrelsandmore.common.TIBAMDiveSuitEvents;
import tfcmt.tfcitembarrelsandmore.common.TIBAMEntities;
import tfcmt.tfcitembarrelsandmore.common.TIBAMFoodTraits;
import tfcmt.tfcitembarrelsandmore.common.TIBAMItems;
import tfcmt.tfcitembarrelsandmore.common.TIBAMSounds;
import tfcmt.tfcitembarrelsandmore.common.curios.CuriosCompat;
import tfcmt.tfcitembarrelsandmore.common.network.TIBAMNetwork;
import tfcmt.tfcitembarrelsandmore.common.recipes.TIBAMRecipes;
import tfcmt.tfcitembarrelsandmore.compat.astikorcarts.BarrelCartHandler;
import tfcmt.tfcitembarrelsandmore.compat.create.CreateCompat;
import tfcmt.tfcitembarrelsandmore.compat.valkib.ValkibIcebreakerCompat;
import tfcmt.tfcitembarrelsandmore.compat.valkyrienskies.TFCWindBridge;
import tfcmt.tfcitembarrelsandmore.compat.valkyrienskies.VSRegistration;
import tfcmt.tfcitembarrelsandmore.compat.valkyrienskies.VSSailsBlocks;

@Mod(value="tfc_item_barrels_and_more")
public class tfcitembarrelsandmore {
    public static final String MOD_ID = "tfc_item_barrels_and_more";
    public static final Logger LOGGER = LogUtils.getLogger();

    public tfcitembarrelsandmore(IEventBus bus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, TIBAMConfig.COMMON_SPEC, "deepprimal/maritime/common.toml");
        TIBAMBlocks.BLOCKS.register(bus);
        TIBAMItems.ITEMS.register(bus);
        tfcmt.tfcitembarrelsandmore.common.TIBAMArmorMaterials.MATERIALS.register(bus);
        TIBAMBlockEntities.BLOCK_ENTITIES.register(bus);
        TIBAMEntities.ENTITY_TYPES.register(bus);
        TIBAMContainerTypes.CONTAINERS.register(bus);
        TIBAMCreativeTab.TABS.register(bus);
        TIBAMSounds.SOUNDS.register(bus);
        TIBAMRecipes.RECIPE_TYPES.register(bus);
        TIBAMRecipes.RECIPE_SERIALIZERS.register(bus);
        TIBAMFoodTraits.TRAITS.register(bus);
        NeoForge.EVENT_BUS.register((Object)new TIBAMCommonEvents());
        NeoForge.EVENT_BUS.register((Object)new TIBAMDiveSuitEvents());
        if (ModList.get().isLoaded("curios")) {
            bus.addListener(CuriosCompat::registerCapabilities);
        }
        if (ModList.get().isLoaded("astikorcarts") && ModList.get().isLoaded("tfcastikorcarts")) {
            NeoForge.EVENT_BUS.register((Object)new BarrelCartHandler());
            LOGGER.info("TFC Maritime Travelers: Astik\u00f6rCarts barrel cart compat enabled.");
        }
        if (ModList.get().isLoaded("create")) {
            CreateCompat.register(bus);
            LOGGER.info("TFC Maritime Travelers: Create speed telegraph compat enabled.");
        }
        if (tfcmt.tfcitembarrelsandmore.compat.valkyrienskies.VSCompat.isLoaded()) {
            LOGGER.info("TFC Maritime Travelers: Valkyrien Skies compat enabled.");
            NeoForge.EVENT_BUS.register((Object)new TFCWindBridge());
            LOGGER.info("TFC Maritime Travelers: TFC-to-VS wind bridge enabled.");
        }
        if (ModList.get().isLoaded("vs_sails")) {
            VSSailsBlocks.register(bus);
            LOGGER.info("TFC Maritime Travelers: VS Sails helm/wheel compat enabled.");
        }
        if (ModList.get().isLoaded("valkib") && tfcmt.tfcitembarrelsandmore.compat.valkyrienskies.VSCompat.isLoaded()) {
            NeoForge.EVENT_BUS.register((Object)new ValkibIcebreakerCompat());
            LOGGER.info("TFC Maritime Travelers: valkib ice-breaker compat enabled.");
        }
        bus.addListener(this::onCommonSetup);
        bus.addListener(TIBAMNetwork::register);
        bus.addListener(tfcmt.tfcitembarrelsandmore.common.MaritimeCapabilities::register);
        LOGGER.info("TFC Item Barrels and More loading!");
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            if (tfcmt.tfcitembarrelsandmore.compat.valkyrienskies.VSCompat.isLoaded()) {
                VSRegistration.register();
            }
        });
    }
}

