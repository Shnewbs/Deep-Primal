package tfcmt.tfcitembarrelsandmore.common;

import java.util.List;
import java.util.Map;
import net.dries007.tfc.common.items.TFCItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class TIBAMArmorMaterials {
    public static final DeferredRegister<ArmorMaterial> MATERIALS = DeferredRegister.create(Registries.ARMOR_MATERIAL, "tfc_item_barrels_and_more");
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> DIVE_CANVAS = MATERIALS.register("dive_canvas", () -> new ArmorMaterial(
        Map.of(ArmorItem.Type.BOOTS, 1, ArmorItem.Type.LEGGINGS, 2, ArmorItem.Type.CHESTPLATE, 3, ArmorItem.Type.HELMET, 1),
        15, SoundEvents.ARMOR_EQUIP_LEATHER, () -> Ingredient.of(TFCItems.BURLAP_CLOTH.get()),
        List.of(new ArmorMaterial.Layer(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "dive_canvas"))), 0.0f, 0.0f));
    private TIBAMArmorMaterials() {}
}
