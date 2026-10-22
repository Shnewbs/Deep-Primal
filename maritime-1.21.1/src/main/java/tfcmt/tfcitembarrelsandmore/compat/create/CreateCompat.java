package tfcmt.tfcitembarrelsandmore.compat.create;

import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.component.size.Size;
import net.dries007.tfc.common.component.size.Weight;
import net.dries007.tfc.util.registry.RegistrationHelpers;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import tfcmt.tfcitembarrelsandmore.common.items.SizedBlockItem;
import tfcmt.tfcitembarrelsandmore.compat.create.SpeedTelegraphInputBlock;
import tfcmt.tfcitembarrelsandmore.compat.create.SpeedTelegraphInputBlockEntity;
import tfcmt.tfcitembarrelsandmore.compat.create.SpeedTelegraphInputItem;
import tfcmt.tfcitembarrelsandmore.compat.create.SpeedTelegraphOutputBlock;
import tfcmt.tfcitembarrelsandmore.compat.create.SpeedTelegraphOutputBlockEntity;

public final class CreateCompat {
    public static final DeferredRegister<Block> BLOCKS;
    public static final DeferredRegister<Item> ITEMS;
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES;
    public static final DeferredHolder<Block, Block> SPEED_TELEGRAPH_INPUT;
    public static final DeferredHolder<Block, Block> SPEED_TELEGRAPH_OUTPUT;
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SpeedTelegraphInputBlockEntity>> SPEED_TELEGRAPH_INPUT_BE;
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SpeedTelegraphOutputBlockEntity>> SPEED_TELEGRAPH_OUTPUT_BE;

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
    }

    private CreateCompat() {
    }

    static {
        DeferredHolder[] holder;
        BLOCKS = DeferredRegister.create(Registries.BLOCK, (String)"tfc_item_barrels_and_more");
        ITEMS = DeferredRegister.create(Registries.ITEM, (String)"tfc_item_barrels_and_more");
        BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, (String)"tfc_item_barrels_and_more");
        SPEED_TELEGRAPH_INPUT = RegistrationHelpers.registerBlock(BLOCKS, ITEMS, (String)"speed_telegraph_input", () -> new SpeedTelegraphInputBlock(ExtendedProperties.of().mapColor(MapColor.METAL).sound(SoundType.COPPER).strength(3.0f).requiresCorrectToolForDrops().noOcclusion().blockEntity(SPEED_TELEGRAPH_INPUT_BE)), item -> new SpeedTelegraphInputItem((Block)item, new Item.Properties(), Size.HUGE, Weight.VERY_HEAVY));
        SPEED_TELEGRAPH_OUTPUT = RegistrationHelpers.registerBlock(BLOCKS, ITEMS, (String)"speed_telegraph_output", () -> new SpeedTelegraphOutputBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.COPPER).strength(3.0f).requiresCorrectToolForDrops().noOcclusion()), item -> new SizedBlockItem((Block)item, new Item.Properties(), Size.HUGE, Weight.VERY_HEAVY));
        SPEED_TELEGRAPH_INPUT_BE = BLOCK_ENTITIES.register("speed_telegraph_input", () -> BlockEntityType.Builder.of((pos, state) -> new SpeedTelegraphInputBlockEntity(pos, state), (Block[])new Block[]{(Block)SPEED_TELEGRAPH_INPUT.get()}).build(null));
        holder = new DeferredHolder[]{BLOCK_ENTITIES.register("speed_telegraph_output", () -> BlockEntityType.Builder.of((pos, state) -> new SpeedTelegraphOutputBlockEntity((BlockEntityType)holder[0].get(), pos, state), (Block[])new Block[]{(Block)SPEED_TELEGRAPH_OUTPUT.get()}).build(null))};
        SPEED_TELEGRAPH_OUTPUT_BE = holder[0];
    }
}

