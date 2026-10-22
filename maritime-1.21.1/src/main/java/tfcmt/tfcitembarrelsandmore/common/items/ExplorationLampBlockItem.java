package tfcmt.tfcitembarrelsandmore.common.items;

import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.neoforged.neoforge.capabilities.Capabilities;

import net.dries007.tfc.common.component.size.IItemSize;
import net.dries007.tfc.common.component.size.Size;
import net.dries007.tfc.common.component.size.Weight;
import net.dries007.tfc.util.data.LampFuel;

import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;


import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import tfcmt.tfcitembarrelsandmore.client.renderer.ExplorationLampISTER;
import tfcmt.tfcitembarrelsandmore.common.blockentities.ExplorationLampBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.ExplorationLampBeam;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.ExplorationLampBlock;

public class ExplorationLampBlockItem
extends BlockItem
implements IItemSize {
    public static final String NBT_MODE = "LampMode";
    public static final String NBT_LIT = "LampLit";
    private static final String NBT_LAST_DRAIN_TICK = "LampLastDrainTick";

    public ExplorationLampBlockItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions(){
            private final java.util.function.Supplier<ExplorationLampISTER> renderer = com.google.common.base.Suppliers.memoize(ExplorationLampISTER::new);

            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return (BlockEntityWithoutLevelRenderer)this.renderer.get();
            }
        });
    }

    public Size getSize(ItemStack stack) {
        return Size.LARGE;
    }

    public Weight getWeight(ItemStack stack) {
        return Weight.HEAVY;
    }

    public static net.neoforged.neoforge.fluids.capability.IFluidHandlerItem fluidHandler(ItemStack stack) {
        return new net.dries007.tfc.common.component.fluid.FluidContainerHandler(stack,
            new net.dries007.tfc.common.component.fluid.FluidContainerInfo() {
                @Override public boolean canContainFluid(Fluid fluid) {
                    return stack.getItem() instanceof ExplorationLampBlockItem lamp && LampFuel.get(fluid, lamp.getBlock().defaultBlockState()) != null;
                }
                @Override public int fluidCapacity() { return 250; }
            });
    }

    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slot, isSelected);
        if (!level.isClientSide) {
            this.catchUpFuel(stack, level);
        }
    }

    private void catchUpFuel(ItemStack stack, Level level) {
        if (!ExplorationLampBlockItem.isLit(stack)) {
            return;
        }
        CompoundTag tag = tfcmt.tfcitembarrelsandmore.common.MaritimeItemData.get(stack);
        long now = level.getGameTime();
        if (!tag.contains(NBT_LAST_DRAIN_TICK)) {
            tfcmt.tfcitembarrelsandmore.common.MaritimeItemData.update(stack, data -> data.putLong(NBT_LAST_DRAIN_TICK, now));
            return;
        }
        long last = tag.getLong(NBT_LAST_DRAIN_TICK);
        long elapsed = now - last;
        if (elapsed <= 0L) {
            return;
        }
        FluidStack fluid = ExplorationLampBlockItem.getFluidInStack(stack);
        if (fluid.isEmpty()) {
            ExplorationLampBlockItem.setLit(stack, false);
            return;
        }
        LampFuel fuel = LampFuel.get((Fluid)fluid.getFluid(), (BlockState)this.getBlock().defaultBlockState());
        if (fuel == null) {
            ExplorationLampBlockItem.setLit(stack, false);
            return;
        }
        if (fuel.burnRate() == -1) {
            tfcmt.tfcitembarrelsandmore.common.MaritimeItemData.update(stack, data -> data.putLong(NBT_LAST_DRAIN_TICK, now));
            return;
        }
        int burnRate = fuel.burnRate();
        long mbToDrain = elapsed / (long)burnRate;
        if (mbToDrain <= 0L) {
            return;
        }
        int available = fluid.getAmount();
        if (mbToDrain > (long)available) {
            mbToDrain = available;
        }
        int finalDrain = (int)mbToDrain;
        var handler = stack.getCapability(Capabilities.FluidHandler.ITEM);
        if (handler != null) handler.drain(finalDrain, IFluidHandler.FluidAction.EXECUTE);
        tfcmt.tfcitembarrelsandmore.common.MaritimeItemData.update(stack, data -> data.putLong(NBT_LAST_DRAIN_TICK, last + (long)finalDrain * burnRate));
        if (ExplorationLampBlockItem.getFluidInStack(stack).isEmpty()) {
            ExplorationLampBlockItem.setLit(stack, false);
        }
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown() && hand == InteractionHand.OFF_HAND && player.getMainHandItem().isEmpty()) {
            ExplorationLampBlockEntity.Mode current = ExplorationLampBlockItem.getModeFromStack(stack);
            ExplorationLampBlockEntity.Mode next = current.next();
            ExplorationLampBlockItem.setModeOnStack(stack, next);
            if (level.isClientSide) {
                player.displayClientMessage((Component)Component.translatable((String)"message.tfc_item_barrels_and_more.exploration_lamp.mode", (Object[])new Object[]{Component.translatable((String)("message.tfc_item_barrels_and_more.exploration_lamp.mode." + next.getSerializedName()))}).withStyle(ChatFormatting.YELLOW), true);
            }
            return InteractionResultHolder.sidedSuccess(stack, (boolean)level.isClientSide);
        }
        return InteractionResultHolder.pass(stack);
    }

    protected boolean updateCustomBlockEntityTag(BlockPos pos, Level level, @Nullable Player player, ItemStack stack, BlockState state) {
        ExplorationLampBlockEntity lamp;
        BlockEntity blockEntity;
        boolean result = super.updateCustomBlockEntityTag(pos, level, player, stack, state);
        if (!level.isClientSide && ExplorationLampBlockItem.isLit(stack) && (blockEntity = level.getBlockEntity(pos)) instanceof ExplorationLampBlockEntity && !(lamp = (ExplorationLampBlockEntity)blockEntity).getFluid().isEmpty()) {
            BlockState litState = (BlockState)level.getBlockState(pos).setValue((Property)ExplorationLampBlock.LIT, Boolean.valueOf(true));
            level.setBlockAndUpdate(pos, litState);
            if (level instanceof ServerLevel) {
                ServerLevel serverLevel = (ServerLevel)level;
                ExplorationLampBeam.sync(serverLevel, pos, litState);
            }
        }
        return result;
    }

    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        Level level = context.level();
        super.appendHoverText(stack, context, tooltip, flag);
        FluidStack fluid = ExplorationLampBlockItem.getFluidInStack(stack);
        if (!fluid.isEmpty()) {
            tooltip.add((Component)Component.literal(fluid.getAmount() + " mB ").append(fluid.getHoverName()));
        } else {
            tooltip.add((Component)Component.translatable((String)"tooltip.tfc_item_barrels_and_more.exploration_lamp.empty").withStyle(ChatFormatting.GRAY));
        }
        ExplorationLampBlockEntity.Mode mode = ExplorationLampBlockItem.getModeFromStack(stack);
        tooltip.add((Component)Component.translatable((String)("tooltip.tfc_item_barrels_and_more.exploration_lamp.mode." + mode.getSerializedName())).withStyle(ChatFormatting.AQUA));
        if (ExplorationLampBlockItem.isLit(stack)) {
            tooltip.add((Component)Component.translatable((String)"tooltip.tfc_item_barrels_and_more.exploration_lamp.lit").withStyle(ChatFormatting.GOLD));
        }
    }

    public static ExplorationLampBlockEntity.Mode getModeFromStack(ItemStack stack) {
        if (tfcmt.tfcitembarrelsandmore.common.MaritimeItemData.has(stack) && tfcmt.tfcitembarrelsandmore.common.MaritimeItemData.get(stack).contains(NBT_MODE)) {
            String name = tfcmt.tfcitembarrelsandmore.common.MaritimeItemData.get(stack).getString(NBT_MODE);
            for (ExplorationLampBlockEntity.Mode m : ExplorationLampBlockEntity.Mode.values()) {
                if (!m.getSerializedName().equals(name)) continue;
                return m;
            }
        }
        return ExplorationLampBlockEntity.Mode.AREA;
    }

    public static void setModeOnStack(ItemStack stack, ExplorationLampBlockEntity.Mode mode) {
        tfcmt.tfcitembarrelsandmore.common.MaritimeItemData.update(stack, data -> data.putString(NBT_MODE, mode.getSerializedName()));
    }

    public static boolean isLit(ItemStack stack) {
        return tfcmt.tfcitembarrelsandmore.common.MaritimeItemData.has(stack) && tfcmt.tfcitembarrelsandmore.common.MaritimeItemData.get(stack).getBoolean(NBT_LIT);
    }

    public static void setLit(ItemStack stack, boolean lit) {
        tfcmt.tfcitembarrelsandmore.common.MaritimeItemData.update(stack, data -> data.putBoolean(NBT_LIT, lit));
    }

    public static FluidStack getFluidInStack(ItemStack stack) {
        var handler = stack.getCapability(Capabilities.FluidHandler.ITEM);
        return handler == null ? FluidStack.EMPTY : handler.getFluidInTank(0);
    }
}

