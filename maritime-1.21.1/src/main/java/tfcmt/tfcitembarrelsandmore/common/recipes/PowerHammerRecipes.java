package tfcmt.tfcitembarrelsandmore.common.recipes;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.dries007.tfc.common.recipes.AnvilRecipe;
import net.dries007.tfc.common.recipes.TFCRecipeTypes;
import net.dries007.tfc.util.Helpers;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredHolder;
import tfcmt.tfcitembarrelsandmore.common.TIBAMItems;

public final class PowerHammerRecipes {
    public static final List<String> BASE_PREFIXES = List.of("refined_iron_bloom", "metal/ingot/", "metal/sheet/");
    public static final DieDefinition ROD_DIE = new DieDefinition(TIBAMItems.ROD_DIE, List.of("metal/rod/"), false);
    public static final DieDefinition BARS_DIE = new DieDefinition(TIBAMItems.BARS_DIE, List.of("metal/bars/"), true);
    public static final DieDefinition TRAPDOOR_DIE = new DieDefinition(TIBAMItems.TRAPDOOR_DIE, List.of("metal/trapdoor/"), true);
    public static final DieDefinition TUYERE_DIE = new DieDefinition(TIBAMItems.TUYERE_DIE, List.of("metal/tuyere/"), true);
    public static final DieDefinition BUCKET_DIE = new DieDefinition(TIBAMItems.BUCKET_DIE, List.of("metal/bucket/"), true);
    public static final DieDefinition JAR_LID_DIE = new DieDefinition(TIBAMItems.JAR_LID_DIE, List.of("jar_lid"), true);
    public static final DieDefinition CHAIN_DIE = new DieDefinition(TIBAMItems.CHAIN_DIE, List.of("metal/chain/"), true);
    public static final DieDefinition BRASS_MECHANISMS_DIE = new DieDefinition(TIBAMItems.BRASS_MECHANISMS_DIE, List.of("brass_mechanisms"), false);
    public static final DieDefinition BLOWPIPE_DIE = new DieDefinition(TIBAMItems.BLOWPIPE_DIE, List.of("blowpipe"), false);
    public static final List<DieDefinition> DIES = List.of(ROD_DIE, BARS_DIE, TRAPDOOR_DIE, TUYERE_DIE, BUCKET_DIE, JAR_LID_DIE, CHAIN_DIE, BRASS_MECHANISMS_DIE, BLOWPIPE_DIE);

    private PowerHammerRecipes() {
    }

    public static boolean isDie(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        for (DieDefinition die : DIES) {
            if (die.item().get() != stack.getItem()) continue;
            return true;
        }
        return false;
    }

    public static List<String> getActivePrefixes(ItemStack dieStack, boolean isPneumatic) {
        if (dieStack.isEmpty()) {
            return BASE_PREFIXES;
        }
        for (DieDefinition die : DIES) {
            if (die.item().get() != dieStack.getItem()) continue;
            if (die.pneumaticOnly() && !isPneumatic) {
                return BASE_PREFIXES;
            }
            ArrayList<String> combined = new ArrayList<String>(BASE_PREFIXES);
            combined.addAll(die.prefixes());
            return combined;
        }
        return BASE_PREFIXES;
    }

    public static boolean isRecipeAllowed(AnvilRecipe recipe, Level level, List<String> activePrefixes) {
        ItemStack result = recipe.getResultItem(level.registryAccess());
        if (result.isEmpty()) {
            return false;
        }
        String path = result.getItem().builtInRegistryHolder().key().location().getPath();
        return activePrefixes.stream().anyMatch(path::startsWith);
    }

    public static List<AnvilRecipe> getAvailableRecipes(Level level, int anvilTier, ItemStack dieStack, boolean isPneumatic) {
        if (anvilTier < 0) {
            return List.of();
        }
        List<String> activePrefixes = PowerHammerRecipes.getActivePrefixes(dieStack, isPneumatic);
        return Helpers.getRecipes((Level)level, (Supplier)TFCRecipeTypes.ANVIL).values().stream().filter(r -> r.isCorrectTier(anvilTier)).filter(r -> PowerHammerRecipes.isRecipeAllowed(r, level, activePrefixes)).sorted((a, b) -> {
            String pa = a.getResultItem(level.registryAccess()).getItem().builtInRegistryHolder().key().location().getPath();
            String pb = b.getResultItem(level.registryAccess()).getItem().builtInRegistryHolder().key().location().getPath();
            return pa.compareTo(pb);
        }).toList();
    }

    public record DieDefinition(DeferredHolder<Item, Item> item, List<String> prefixes, boolean pneumaticOnly) {
    }
}

