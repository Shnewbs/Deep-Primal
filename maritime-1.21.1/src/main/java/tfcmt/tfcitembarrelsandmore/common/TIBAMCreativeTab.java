package tfcmt.tfcitembarrelsandmore.common;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import tfcmt.tfcitembarrelsandmore.common.TIBAMBlocks;
import tfcmt.tfcitembarrelsandmore.common.TIBAMItems;
import tfcmt.tfcitembarrelsandmore.compat.create.CreateCompat;
import tfcmt.tfcitembarrelsandmore.compat.valkyrienskies.VSSailsBlocks;

public final class TIBAMCreativeTab {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create((ResourceKey)Registries.CREATIVE_MODE_TAB, (String)"tfc_item_barrels_and_more");
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register("main", () -> CreativeModeTab.builder().title((Component)Component.translatable((String)"itemGroup.tfc_item_barrels_and_more.main")).icon(() -> new ItemStack((ItemLike)TIBAMBlocks.ITEM_BARRELS.get((Object)TIBAMBlocks.Wood.OAK).get())).displayItems(TIBAMCreativeTab::populate).build());

    private TIBAMCreativeTab() {
    }

    private static void populate(CreativeModeTab.ItemDisplayParameters params, CreativeModeTab.Output output) {
        boolean afcLoaded = ModList.get().isLoaded("afc");
        for (TIBAMBlocks.Wood wood : TIBAMBlocks.Wood.values()) {
            if (wood.getMod().equals("afc") && !afcLoaded) continue;
            output.accept((ItemLike)TIBAMBlocks.ITEM_BARRELS.get((Object)wood).get());
        }
        for (TIBAMBlocks.Wood wood : TIBAMBlocks.Wood.values()) {
            if (wood.getMod().equals("afc") && !afcLoaded) continue;
            output.accept((ItemLike)TIBAMBlocks.SMOKE_SHACKS.get((Object)wood).get());
        }
        for (TIBAMBlocks.Wood wood : TIBAMBlocks.Wood.values()) {
            if (wood.getMod().equals("afc") && !afcLoaded) continue;
            output.accept((ItemLike)TIBAMBlocks.WATER_PADDLES.get((Object)wood).get());
        }
        for (TIBAMBlocks.Wood wood : TIBAMBlocks.Wood.values()) {
            if (wood.getMod().equals("afc") && !afcLoaded) continue;
            output.accept((ItemLike)TIBAMBlocks.MAST_ANCHORS.get((Object)wood).get());
        }
        for (TIBAMBlocks.Wood wood : TIBAMBlocks.Wood.values()) {
            if (wood.getMod().equals("afc") && !afcLoaded) continue;
            output.accept((ItemLike)TIBAMItems.YARDS.get((Object)wood).get());
            output.accept((ItemLike)TIBAMItems.BOOMS.get((Object)wood).get());
        }
        if (ModList.get().isLoaded("vs_sails")) {
            for (Enum enum_ : TIBAMBlocks.Wood.values()) {
                if (((TIBAMBlocks.Wood)enum_).getMod().equals("afc") && !afcLoaded) continue;
                output.accept((ItemLike)VSSailsBlocks.HELMS.get(enum_).get());
            }
            for (Enum enum_ : TIBAMBlocks.Wood.values()) {
                if (((TIBAMBlocks.Wood)enum_).getMod().equals("afc") && !afcLoaded) continue;
                output.accept((ItemLike)VSSailsBlocks.HELM_WHEELS.get(enum_).get());
            }
        }
        for (Enum enum_ : TIBAMBlocks.Metal.values()) {
            output.accept((ItemLike)TIBAMBlocks.CLEATS.get(enum_).get());
        }
        output.accept((ItemLike)TIBAMBlocks.BOILER.get());
        output.accept((ItemLike)TIBAMBlocks.BOILER_TANK.get());
        output.accept((ItemLike)TIBAMBlocks.STEEL_BOILER_TANK.get());
        output.accept((ItemLike)TIBAMBlocks.FUME_HOOD_DUCT.get());
        output.accept((ItemLike)TIBAMBlocks.SINGLE_PISTON_ENGINE.get());
        output.accept((ItemLike)TIBAMBlocks.TRIPLE_EXPANSION_ENGINE.get());
        output.accept((ItemLike)TIBAMBlocks.LARGE_SINGLE_PISTON_ENGINE.get());
        output.accept((ItemLike)TIBAMBlocks.CAST_IRON_AXLE.get());
        output.accept((ItemLike)TIBAMBlocks.CAST_IRON_AXLE_BRACE.get());
        output.accept((ItemLike)TIBAMBlocks.TWO_BLADE_PROPELLER.get());
        output.accept((ItemLike)TIBAMBlocks.FOUR_BLADE_PROPELLER.get());
        output.accept((ItemLike)TIBAMBlocks.AIR_COMPRESSOR.get());
        output.accept((ItemLike)TIBAMItems.AIR_HOSE.get());
        output.accept((ItemLike)TIBAMItems.KNOT_METER.get());
        output.accept((ItemLike)TIBAMItems.SOUNDING_LINE.get());
        output.accept((ItemLike)TIBAMItems.WIND_GAUGE.get());
        output.accept((ItemLike)TIBAMItems.UNFINISHED_DIVING_HELMET.get());
        output.accept((ItemLike)TIBAMItems.DIVING_HELMET.get());
        output.accept((ItemLike)TIBAMItems.DIVING_SUIT.get());
        output.accept((ItemLike)TIBAMItems.DIVING_LEGGINGS.get());
        output.accept((ItemLike)TIBAMItems.UNFINISHED_DIVING_BOOTS.get());
        output.accept((ItemLike)TIBAMItems.DIVING_BOOTS.get());
        output.accept((ItemLike)TIBAMBlocks.GRINDER.get());
        output.accept((ItemLike)TIBAMBlocks.EXPELLER_PRESS.get());
        output.accept((ItemLike)TIBAMBlocks.MECHANICAL_POWER_HAMMER.get());
        output.accept((ItemLike)TIBAMBlocks.PNEUMATIC_POWER_HAMMER.get());
        output.accept((ItemLike)TIBAMBlocks.EXPLORATION_LAMP.get());
        if (ModList.get().isLoaded("create")) {
            output.accept((ItemLike)CreateCompat.SPEED_TELEGRAPH_INPUT.get());
            output.accept((ItemLike)CreateCompat.SPEED_TELEGRAPH_OUTPUT.get());
        }
        output.accept((ItemLike)TIBAMBlocks.TFC_SPEED_TELEGRAPH_INPUT.get());
        output.accept((ItemLike)TIBAMBlocks.TFC_SPEED_TELEGRAPH_OUTPUT.get());
        output.accept((ItemLike)TIBAMItems.WROUGHT_IRON_PRESS_SCREW.get());
        output.accept((ItemLike)TIBAMItems.STEEL_PRESS_SCREW.get());
        output.accept((ItemLike)TIBAMItems.BLACK_STEEL_PRESS_SCREW.get());
        output.accept((ItemLike)TIBAMItems.BLUE_STEEL_PRESS_SCREW.get());
        output.accept((ItemLike)TIBAMItems.RED_STEEL_PRESS_SCREW.get());
        output.accept((ItemLike)TIBAMItems.PISTON_HEAD.get());
        output.accept((ItemLike)TIBAMItems.ROD_ASSEMBLY.get());
        output.accept((ItemLike)TIBAMItems.CRANK_ASSEMBLY.get());
        output.accept((ItemLike)TIBAMItems.ROD_DIE.get());
        output.accept((ItemLike)TIBAMItems.BARS_DIE.get());
        output.accept((ItemLike)TIBAMItems.TRAPDOOR_DIE.get());
        output.accept((ItemLike)TIBAMItems.TUYERE_DIE.get());
        output.accept((ItemLike)TIBAMItems.BUCKET_DIE.get());
        output.accept((ItemLike)TIBAMItems.JAR_LID_DIE.get());
        output.accept((ItemLike)TIBAMItems.CHAIN_DIE.get());
        output.accept((ItemLike)TIBAMItems.BRASS_MECHANISMS_DIE.get());
        output.accept((ItemLike)TIBAMItems.BLOWPIPE_DIE.get());
        output.accept((ItemLike)TIBAMItems.PRESS_CAKE.get());
        output.accept((ItemLike)TIBAMItems.RAW_GROUND_BEEF.get());
        output.accept((ItemLike)TIBAMItems.COOKED_GROUND_BEEF.get());
        output.accept((ItemLike)TIBAMItems.RAW_GROUND_PORK.get());
        output.accept((ItemLike)TIBAMItems.COOKED_GROUND_PORK.get());
        output.accept((ItemLike)TIBAMItems.RAW_GROUND_MUTTON.get());
        output.accept((ItemLike)TIBAMItems.COOKED_GROUND_MUTTON.get());
        output.accept((ItemLike)TIBAMItems.RAW_GROUND_VENISON.get());
        output.accept((ItemLike)TIBAMItems.COOKED_GROUND_VENISON.get());
        output.accept((ItemLike)TIBAMItems.RAW_GROUND_CAMELIDAE.get());
        output.accept((ItemLike)TIBAMItems.COOKED_GROUND_CAMELIDAE.get());
        output.accept((ItemLike)TIBAMItems.RAW_GROUND_MYSTERY.get());
        output.accept((ItemLike)TIBAMItems.COOKED_GROUND_MYSTERY.get());
        output.accept((ItemLike)TIBAMItems.RAW_BEEF_SAUSAGE.get());
        output.accept((ItemLike)TIBAMItems.BEEF_SAUSAGE.get());
        output.accept((ItemLike)TIBAMItems.RAW_PORK_SAUSAGE.get());
        output.accept((ItemLike)TIBAMItems.PORK_SAUSAGE.get());
        output.accept((ItemLike)TIBAMItems.RAW_MUTTON_SAUSAGE.get());
        output.accept((ItemLike)TIBAMItems.MUTTON_SAUSAGE.get());
        output.accept((ItemLike)TIBAMItems.RAW_VENISON_SAUSAGE.get());
        output.accept((ItemLike)TIBAMItems.VENISON_SAUSAGE.get());
        output.accept((ItemLike)TIBAMItems.RAW_CAMELIDAE_SAUSAGE.get());
        output.accept((ItemLike)TIBAMItems.CAMELIDAE_SAUSAGE.get());
        output.accept((ItemLike)TIBAMItems.RAW_MYSTERY_SAUSAGE.get());
        output.accept((ItemLike)TIBAMItems.MYSTERY_SAUSAGE.get());
    }
}

