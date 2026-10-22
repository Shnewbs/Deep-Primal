package tfcmt.tfcitembarrelsandmore.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.model.data.ModelData;
import tfcmt.tfcitembarrelsandmore.client.renderer.ExplorationLampBER;
import tfcmt.tfcitembarrelsandmore.common.blockentities.ExplorationLampBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.items.ExplorationLampBlockItem;

public class ExplorationLampISTER
extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation AREA_MODEL = ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/exploration_lamp_area");
    private static final ResourceLocation SEARCH_MODEL = ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/exploration_lamp_search");

    public ExplorationLampISTER() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource buffers, int packedLight, int packedOverlay) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        ExplorationLampBlockEntity.Mode mode = ExplorationLampBlockItem.getModeFromStack(stack);
        ResourceLocation modelLoc = mode == ExplorationLampBlockEntity.Mode.SEARCH ? SEARCH_MODEL : AREA_MODEL;
        BakedModel model = mc.getModelManager().getModel(net.minecraft.client.resources.model.ModelResourceLocation.standalone(modelLoc));
        mc.getBlockRenderer().getModelRenderer().renderModel(poseStack.last(), buffers.getBuffer(Sheets.cutoutBlockSheet()), null, model, 1.0f, 1.0f, 1.0f, packedLight, packedOverlay, ModelData.EMPTY, Sheets.cutoutBlockSheet());
        if (!ExplorationLampBlockItem.isLit(stack)) {
            return;
        }
        long gameTime = level != null ? level.getGameTime() : 0L;
        ExplorationLampBER.drawGlow(mode, gameTime, poseStack, buffers);
    }
}

