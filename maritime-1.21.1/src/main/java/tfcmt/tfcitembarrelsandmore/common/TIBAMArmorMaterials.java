package tfcmt.tfcitembarrelsandmore.common;

import net.dries007.tfc.common.items.TFCItems;
import net.dries007.tfc.util.PhysicalDamageType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

public enum TIBAMArmorMaterials implements ArmorMaterial,
PhysicalDamageType.Multiplier
{
    DIVE_CANVAS(90, 100, 110, 80, 1, 2, 3, 1, 15, 0.0f, 0.0f, 1.5f, 1.0f, 2.0f);

    private final int feetDurability;
    private final int legDurability;
    private final int chestDurability;
    private final int headDurability;
    private final int feetDefense;
    private final int legDefense;
    private final int chestDefense;
    private final int headDefense;
    private final int enchantability;
    private final float toughness;
    private final float knockbackResistance;
    private final float crushingModifier;
    private final float piercingModifier;
    private final float slashingModifier;

    private TIBAMArmorMaterials(int feetDurability, int legDurability, int chestDurability, int headDurability, int feetDefense, int legDefense, int chestDefense, int headDefense, int enchantability, float toughness, float knockbackResistance, float crushingModifier, float piercingModifier, float slashingModifier) {
        this.feetDurability = feetDurability;
        this.legDurability = legDurability;
        this.chestDurability = chestDurability;
        this.headDurability = headDurability;
        this.feetDefense = feetDefense;
        this.legDefense = legDefense;
        this.chestDefense = chestDefense;
        this.headDefense = headDefense;
        this.enchantability = enchantability;
        this.toughness = toughness;
        this.knockbackResistance = knockbackResistance;
        this.crushingModifier = crushingModifier;
        this.piercingModifier = piercingModifier;
        this.slashingModifier = slashingModifier;
    }

    public int getDurabilityForType(ArmorItem.Type type) {
        return switch (type) {
            default -> throw new IncompatibleClassChangeError();
            case ArmorItem.Type.BOOTS -> this.feetDurability;
            case ArmorItem.Type.LEGGINGS -> this.legDurability;
            case ArmorItem.Type.CHESTPLATE -> this.chestDurability;
            case ArmorItem.Type.HELMET -> this.headDurability;
        };
    }

    public int getDefenseForType(ArmorItem.Type type) {
        return switch (type) {
            default -> throw new IncompatibleClassChangeError();
            case ArmorItem.Type.BOOTS -> this.feetDefense;
            case ArmorItem.Type.LEGGINGS -> this.legDefense;
            case ArmorItem.Type.CHESTPLATE -> this.chestDefense;
            case ArmorItem.Type.HELMET -> this.headDefense;
        };
    }

    public int getEnchantmentValue() {
        return this.enchantability;
    }

    public SoundEvent getEquipSound() {
        return SoundEvents.ARMOR_EQUIP_LEATHER;
    }

    public Ingredient getRepairIngredient() {
        return Ingredient.of((ItemLike[])new ItemLike[]{(ItemLike)TFCItems.BURLAP_CLOTH.get()});
    }

    public String getName() {
        return "tfc_item_barrels_and_more:dive_canvas";
    }

    public float getToughness() {
        return this.toughness;
    }

    public float getKnockbackResistance() {
        return this.knockbackResistance;
    }

    public float crushing() {
        return this.crushingModifier;
    }

    public float piercing() {
        return this.piercingModifier;
    }

    public float slashing() {
        return this.slashingModifier;
    }
}

