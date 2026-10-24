package tfcmt.tfcitembarrelsandmore.common.items;

import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import tfcmt.tfcitembarrelsandmore.client.model.DiveSuitModel;

public class DiveSuitArmorItem
extends ArmorItem {
    public DiveSuitArmorItem(net.minecraft.core.Holder<ArmorMaterial> material, ArmorItem.Type type, Item.Properties properties) {
        super(material, type, properties);
    }

    @Override public net.minecraft.resources.ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, ArmorMaterial.Layer layer, boolean inner) {
        return net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "textures/models/armor/dive_suit.png");
    }

    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions(){

            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                ModelLayerLocation layer = switch (equipmentSlot) {
                    case HEAD -> DiveSuitModel.HELMET_LAYER;
                    case CHEST -> DiveSuitModel.CHEST_LAYER;
                    case LEGS -> DiveSuitModel.LEGGINGS_LAYER;
                    case FEET -> DiveSuitModel.BOOTS_LAYER;
                    default -> null;
                };
                if (layer == null) {
                    return original;
                }
                ModelPart root = Minecraft.getInstance().getEntityModels().bakeLayer(layer);
                return new DiveSuitModel(root);
            }
        });
    }
}

