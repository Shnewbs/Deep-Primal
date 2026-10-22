package tfcmt.tfcitembarrelsandmore.common.blockentities;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import tfcmt.tfcitembarrelsandmore.common.TIBAMBlockEntities;
import tfcmt.tfcitembarrelsandmore.common.TIBAMContainerTypes;
import tfcmt.tfcitembarrelsandmore.common.blockentities.MechanicalPowerHammerBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.container.MechanicalPowerHammerContainer;

public class PneumaticPowerHammerBlockEntity
extends MechanicalPowerHammerBlockEntity {
    private static final Component NAME = Component.translatable((String)"block.tfc_item_barrels_and_more.pneumatic_power_hammer");
    private static final float SPEED_MULTIPLIER = 1.3f;

    public PneumaticPowerHammerBlockEntity(BlockPos pos, BlockState state) {
        super((BlockEntityType)TIBAMBlockEntities.PNEUMATIC_POWER_HAMMER.get(), pos, state, NAME.getString());
    }

    @Override
    protected float getSpeedMultiplier() {
        return 1.3f;
    }

    @Override
    public boolean isPneumaticHammer() {
        return true;
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInv, Player player) {
        return MechanicalPowerHammerContainer.createForType((MenuType)TIBAMContainerTypes.PNEUMATIC_POWER_HAMMER.get(), this, playerInv, windowId);
    }

    @Override
    public Component getDisplayName() {
        return NAME;
    }
}

