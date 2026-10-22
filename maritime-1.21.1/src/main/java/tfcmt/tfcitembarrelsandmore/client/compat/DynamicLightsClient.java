package tfcmt.tfcitembarrelsandmore.client.compat;

import dev.lambdaurora.lambdynlights.api.item.ItemLightSource;
import dev.lambdaurora.lambdynlights.api.item.ItemLightSources;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import tfcmt.tfcitembarrelsandmore.common.TIBAMBlocks;
import tfcmt.tfcitembarrelsandmore.common.blockentities.ExplorationLampBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.items.ExplorationLampBlockItem;

/** Loaded only when Sodium Dynamic Lights is installed. */
public final class DynamicLightsClient {
    public static void register() {
        ItemLightSources.registerItemLightSource(new ItemLightSource(
            ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "exploration_lamp"),
            TIBAMBlocks.EXPLORATION_LAMP.get().asItem(), true) {
            @Override public int getLuminance(ItemStack stack) {
                if (!ExplorationLampBlockItem.isLit(stack)) return 0;
                return ExplorationLampBlockItem.getModeFromStack(stack) == ExplorationLampBlockEntity.Mode.SEARCH ? 15 : 14;
            }
        });
        ItemLightSources.load(Minecraft.getInstance().getResourceManager());
    }
}
