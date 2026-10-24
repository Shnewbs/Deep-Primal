package tfcmt.tfcitembarrelsandmore.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import tfcmt.tfcitembarrelsandmore.common.container.ExpellerPressContainer;

public class ExpellerPressScreen
extends AbstractContainerScreen<ExpellerPressContainer> {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "textures/gui/expeller_press.png");
    private static final int TANK_X = 8;
    private static final int TANK_Y = 20;
    private static final int TANK_W = 16;
    private static final int TANK_H = 50;

    public ExpellerPressScreen(ExpellerPressContainer container, Inventory playerInventory, Component title) {
        super(container, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 186;
        this.inventoryLabelY = this.imageHeight - 106;
    }

    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(BACKGROUND, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    private void renderFluidTank(GuiGraphics graphics) {
        int amount = ((ExpellerPressContainer)this.menu).getFluidAmount();
        int capacity = ((ExpellerPressContainer)this.menu).getFluidCapacity();
        if (capacity <= 0 || amount <= 0) {
            return;
        }
        FluidStack tankFluid = ((ExpellerPressContainer)this.menu).getOutputFluid();
        if (tankFluid.isEmpty()) {
            return;
        }
        int fillH = Math.min((int)((float)amount / (float)capacity * 50.0f), 50);
        int fillY = 20 + (50 - fillH);
        IClientFluidTypeExtensions ext = IClientFluidTypeExtensions.of((Fluid)tankFluid.getFluid());
        int tint = ext.getTintColor();
        TextureAtlasSprite sprite = (TextureAtlasSprite)Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(ext.getStillTexture());
        float r = (float)(tint >> 16 & 0xFF) / 255.0f;
        float g = (float)(tint >> 8 & 0xFF) / 255.0f;
        float b = (float)(tint & 0xFF) / 255.0f;
        float a = (float)(tint >> 24 & 0xFF) / 255.0f;
        if (a == 0.0f) {
            a = 1.0f;
        }
        int screenX = this.leftPos + 8;
        int screenY = this.topPos + fillY;
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture((int)0, (ResourceLocation)TextureAtlas.LOCATION_BLOCKS);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buf = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        float spriteW = sprite.getU1() - sprite.getU0();
        float spriteH = sprite.getV1() - sprite.getV0();
        for (int ty = 0; ty < fillH; ty += 16) {
            int drawH = Math.min(16, fillH - ty);
            float v0 = sprite.getV0();
            float v1 = sprite.getV0() + spriteH * ((float)drawH / 16.0f);
            for (int tx = 0; tx < 16; tx += 16) {
                int drawW = Math.min(16, 16 - tx);
                float u0 = sprite.getU0();
                float u1 = sprite.getU0() + spriteW * ((float)drawW / 16.0f);
                int x0 = screenX + tx;
                int y0 = screenY + ty;
                int x1 = x0 + drawW;
                int y1 = y0 + drawH;
                float z = 100.0f;
                buf.addVertex((float)x0, (float)y1, (float)z).setUv(u0, v1).setColor(r, g, b, a);
                buf.addVertex((float)x1, (float)y1, (float)z).setUv(u1, v1).setColor(r, g, b, a);
                buf.addVertex((float)x1, (float)y0, (float)z).setUv(u1, v0).setColor(r, g, b, a);
                buf.addVertex((float)x0, (float)y0, (float)z).setUv(u0, v0).setColor(r, g, b, a);
            }
        }
        com.mojang.blaze3d.vertex.BufferUploader.drawWithShader(buf.buildOrThrow());
        RenderSystem.disableBlend();
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderFluidTank(graphics);
        this.renderTooltip(graphics, mouseX, mouseY);
        if (this.isHovering(8, 20, 16, 50, mouseX, mouseY)) {
            FluidStack tankFluid = ((ExpellerPressContainer)this.menu).getOutputFluid();
            Component fluidName = tankFluid.isEmpty() ? Component.literal((String)"Empty") : tankFluid.getFluid().getFluidType().getDescription();
            graphics.renderTooltip(this.font, (Component)Component.literal((String)(((ExpellerPressContainer)this.menu).getFluidAmount() + " / " + ((ExpellerPressContainer)this.menu).getFluidCapacity() + " mB of ")).append((Component)fluidName), mouseX, mouseY);
        }
    }
}

