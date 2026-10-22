package tfcmt.tfcitembarrelsandmore.client.screen.button;

import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.common.blocks.devices.SealableDeviceBlock;
import net.dries007.tfc.network.PacketHandler;
import net.dries007.tfc.network.ScreenButtonPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.network.PacketDistributor;
import tfcmt.tfcitembarrelsandmore.client.screen.ItemBarrelScreen;
import tfcmt.tfcitembarrelsandmore.common.blockentities.ItemBarrelBlockEntity;

public class ItemBarrelSealButton
extends Button {
    private final ItemBarrelBlockEntity barrel;

    public ItemBarrelSealButton(ItemBarrelBlockEntity barrel, int guiLeft, int guiTop, Component tooltip) {
        super(guiLeft + 123, guiTop + 35, 20, 20, tooltip, b -> {}, RenderHelpers.NARRATION);
        this.setTooltip(Tooltip.create((Component)tooltip));
        this.barrel = barrel;
    }

    public void onPress() {
        PacketDistributor.sendToServer(new ScreenButtonPacket(0, (CompoundTag)null));
        this.playDownSound(Minecraft.getInstance().getSoundManager());
    }

    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int v = (Boolean)this.barrel.getBlockState().getValue((Property)SealableDeviceBlock.SEALED) != false ? 0 : 20;
        graphics.blit(ItemBarrelScreen.BACKGROUND, this.getX(), this.getY(), 236.0f, (float)v, 20, 20, 256, 256);
    }
}

