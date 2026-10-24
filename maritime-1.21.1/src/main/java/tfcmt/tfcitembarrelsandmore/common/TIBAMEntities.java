package tfcmt.tfcitembarrelsandmore.common;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import tfcmt.tfcitembarrelsandmore.common.entities.ThrownSoundingLineEntity;

public class TIBAMEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, (String)"tfc_item_barrels_and_more");
    public static final DeferredHolder<EntityType<?>, EntityType<ThrownSoundingLineEntity>> SOUNDING_LINE = ENTITY_TYPES.register("sounding_line", () -> EntityType.Builder.<ThrownSoundingLineEntity>of(ThrownSoundingLineEntity::new, (MobCategory)MobCategory.MISC).sized(0.25f, 0.25f).clientTrackingRange(4).updateInterval(10).build("sounding_line"));
}

