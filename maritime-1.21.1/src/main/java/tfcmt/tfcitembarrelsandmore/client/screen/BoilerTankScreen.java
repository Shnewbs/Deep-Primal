package tfcmt.tfcitembarrelsandmore.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import tfcmt.tfcitembarrelsandmore.client.screen.AbstractSteamEngineScreen;
import tfcmt.tfcitembarrelsandmore.common.blockentities.BoilerTankBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.container.BoilerTankContainer;

public class BoilerTankScreen
extends AbstractSteamEngineScreen<BoilerTankContainer> {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "textures/gui/boiler_tank.png");
    private static final AbstractSteamEngineScreen.Palette CAST_IRON = new AbstractSteamEngineScreen.Palette(-14803426, -10855846, -5592406, -15066598, 0xC8C8C8, -10843222, -16117996, -14013910, -13092808, -10461088, -15461356, 0xC8C8C8);
    private static final AbstractSteamEngineScreen.Palette STEEL = new AbstractSteamEngineScreen.Palette(-15460069, -12298405, -4931647, -15985642, 13095636, -10503970, -16248813, -14932440, -14140094, -8220010, -15985642, 13095636);

    public BoilerTankScreen(BoilerTankContainer container, Inventory playerInventory, Component title) {
        super(container, playerInventory, title, container.getTier() == BoilerTankBlockEntity.TankTier.STEEL ? STEEL : CAST_IRON);
    }

    @Override
    protected void renderTexture(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(BACKGROUND, this.leftPos, this.topPos, 0.0f, 0.0f, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
    }
}

