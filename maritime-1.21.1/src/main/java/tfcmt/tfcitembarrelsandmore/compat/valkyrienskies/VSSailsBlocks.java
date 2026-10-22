package tfcmt.tfcitembarrelsandmore.compat.valkyrienskies;

import com.quintonc.vs_sails.blocks.HelmBlock;
import com.quintonc.vs_sails.blocks.HelmWheel;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import net.dries007.tfc.common.component.size.Size;
import net.dries007.tfc.common.component.size.Weight;
import net.dries007.tfc.util.registry.RegistrationHelpers;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import tfcmt.tfcitembarrelsandmore.common.TIBAMBlocks;
import tfcmt.tfcitembarrelsandmore.common.items.SizedBlockItem;

public final class VSSailsBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, (String)"tfc_item_barrels_and_more");
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, (String)"tfc_item_barrels_and_more");
    public static final Map<TIBAMBlocks.Wood, DeferredHolder<Block, Block>> HELMS = Arrays.stream(TIBAMBlocks.Wood.values()).collect(Collectors.toMap(wood -> wood, wood -> RegistrationHelpers.registerBlock(BLOCKS, ITEMS, (String)("wood/" + wood.getId() + "/vs_sails_helm"), () -> new HelmBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD).strength(3.0f).noOcclusion()), item -> new SizedBlockItem((Block)item, new Item.Properties(), Size.HUGE, Weight.VERY_HEAVY))));
    public static final Map<TIBAMBlocks.Wood, DeferredHolder<Block, Block>> HELM_WHEELS = Arrays.stream(TIBAMBlocks.Wood.values()).collect(Collectors.toMap(wood -> wood, wood -> RegistrationHelpers.registerBlock(BLOCKS, ITEMS, (String)("wood/" + wood.getId() + "/vs_sails_helm_wheel"), () -> new HelmWheel(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD).strength(3.0f).noOcclusion()), item -> new SizedBlockItem((Block)item, new Item.Properties(), Size.HUGE, Weight.VERY_HEAVY))));

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
    }

    private VSSailsBlocks() {
    }
}

