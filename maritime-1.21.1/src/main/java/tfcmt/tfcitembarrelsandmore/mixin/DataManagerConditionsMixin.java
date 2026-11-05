package tfcmt.tfcitembarrelsandmore.mixin;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import java.util.HashMap;
import java.util.Map;
import net.dries007.tfc.util.data.DataManager;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import tfcmt.tfcitembarrelsandmore.tfcitembarrelsandmore;

/** TFC's custom data codecs do not evaluate NeoForge conditions themselves. */
@Mixin(DataManager.class)
public abstract class DataManagerConditionsMixin {
    @ModifyVariable(method="apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at=@At("HEAD"), argsOnly=true, ordinal=0, remap=false)
    private Map<ResourceLocation, JsonElement> maritime$filterOwnOptionalData(Map<ResourceLocation, JsonElement> entries) {
        Map<ResourceLocation, JsonElement> filtered = new HashMap<>(entries);
        filtered.entrySet().removeIf(entry -> {
            if (!entry.getKey().getNamespace().equals("tfc_item_barrels_and_more") || !entry.getValue().isJsonObject()) return false;
            var conditions = entry.getValue().getAsJsonObject().get("neoforge:conditions");
            if (conditions == null) return false;
            try {
                return !ICondition.LIST_CODEC.parse(JsonOps.INSTANCE, conditions).getOrThrow().stream().allMatch(condition -> condition.test(ICondition.IContext.EMPTY));
            } catch (RuntimeException error) {
                tfcitembarrelsandmore.LOGGER.error("Invalid Maritime data conditions for {}", entry.getKey(), error);
                return true;
            }
        });
        return filtered;
    }
}
