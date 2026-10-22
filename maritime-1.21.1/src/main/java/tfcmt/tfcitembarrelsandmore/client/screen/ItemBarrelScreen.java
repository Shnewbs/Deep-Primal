package tfcmt.tfcitembarrelsandmore.client.screen;

import net.dries007.tfc.client.screen.BlockEntityScreen;
import net.dries007.tfc.common.blocks.devices.SealableDeviceBlock;
import net.dries007.tfc.common.container.BlockEntityContainer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.state.properties.Property;
import tfcmt.tfcitembarrelsandmore.client.screen.button.ItemBarrelSealButton;
import tfcmt.tfcitembarrelsandmore.common.blockentities.ItemBarrelBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.container.ItemBarrelContainer;

public class ItemBarrelScreen
extends BlockEntityScreen<ItemBarrelBlockEntity, ItemBarrelContainer> {
    private static final Component SEAL = Component.translatable((String)"tfc.tooltip.seal_barrel");
    private static final Component UNSEAL = Component.translatable((String)"tfc.tooltip.unseal_barrel");
    public static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "textures/gui/item_barrel.png");
    private ItemBarrelSealButton sealButton;

    public ItemBarrelScreen(ItemBarrelContainer container, Inventory playerInventory, Component name) {
        super((BlockEntityContainer)container, playerInventory, name, BACKGROUND);
    }

    public void init() {
        super.init();
        this.sealButton = new ItemBarrelSealButton((ItemBarrelBlockEntity)this.blockEntity, this.getGuiLeft(), this.getGuiTop(), this.isSealed() ? UNSEAL : SEAL);
        this.addRenderableWidget((GuiEventListener)this.sealButton);
    }

    public void containerTick() {
        super.containerTick();
        if (this.sealButton != null) {
            this.sealButton.setMessage(this.isSealed() ? UNSEAL : SEAL);
        }
    }

    private boolean isSealed() {
        return (Boolean)((ItemBarrelBlockEntity)this.blockEntity).getBlockState().getValue((Property)SealableDeviceBlock.SEALED);
    }

    protected void renderLabels(GuiGraphics poseStack, int mouseX, int mouseY) {
        super.renderLabels(poseStack, mouseX, mouseY);
        if (this.isSealed()) {
            this.drawDisabled(poseStack, 0, 11);
        }
    }
}

