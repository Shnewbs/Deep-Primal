package tfcmt.tfcitembarrelsandmore.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import tfcmt.tfcitembarrelsandmore.client.screen.AbstractSteamEngineScreen;
import tfcmt.tfcitembarrelsandmore.common.container.SinglePistonEngineContainer;

public class SinglePistonEngineScreen
extends AbstractSteamEngineScreen<SinglePistonEngineContainer> {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "textures/gui/piston_engine.png");
    private static final AbstractSteamEngineScreen.Palette BRASS = new AbstractSteamEngineScreen.Palette(-12965352, -7576536, -3630271, -13491958, 13146945, -2840501, -15068664, -11914472, -12965352, -9744856, -15068664, 13146945);

    public SinglePistonEngineScreen(SinglePistonEngineContainer container, Inventory playerInventory, Component title) {
        super(container, playerInventory, title, BRASS);
    }

    @Override
    protected void renderTexture(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(BACKGROUND, this.leftPos, this.topPos, 0.0f, 0.0f, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
    }
}

