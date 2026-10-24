package tfcmt.tfcitembarrelsandmore.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.client.model.data.ModelData;
import tfcmt.tfcitembarrelsandmore.common.blockentities.PneumaticPowerHammerBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.PneumaticPowerHammerBlock;

public class PneumaticPowerHammerBER
implements BlockEntityRenderer<PneumaticPowerHammerBlockEntity> {
    private static final String MODID = "tfc_item_barrels_and_more";
    private static final ResourceLocation MODEL_PISTON = ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/pneumatic_power_hammer_piston");
    private static final Map<String, ResourceLocation> PISTON_MODELS = Map.of("bismuth_bronze", ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/pneumatic_power_hammer_piston_bismuth_bronze"), "black_bronze", ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/pneumatic_power_hammer_piston_black_bronze"), "bronze", ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/pneumatic_power_hammer_piston_bronze"), "copper", ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/pneumatic_power_hammer_piston_copper"), "wrought_iron", ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/pneumatic_power_hammer_piston_wrought_iron"), "steel", ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/pneumatic_power_hammer_piston_steel"), "black_steel", ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/pneumatic_power_hammer_piston_black_steel"), "blue_steel", ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/pneumatic_power_hammer_piston_blue_steel"), "red_steel", ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/pneumatic_power_hammer_piston_red_steel"));
    private static final Map<String, ResourceLocation> ANVIL_MODELS = Map.of("bismuth_bronze", ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/mechanical_power_hammer_anvil_bismuth_bronze"), "black_bronze", ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/mechanical_power_hammer_anvil_black_bronze"), "bronze", ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/mechanical_power_hammer_anvil_bronze"), "copper", ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/mechanical_power_hammer_anvil_copper"), "wrought_iron", ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/mechanical_power_hammer_anvil_wrought_iron"), "steel", ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/mechanical_power_hammer_anvil_steel"), "black_steel", ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/mechanical_power_hammer_anvil_black_steel"), "blue_steel", ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/mechanical_power_hammer_anvil_blue_steel"), "red_steel", ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/mechanical_power_hammer_anvil_red_steel"));
    private static final float PISTON_STROKE = 0.25f;

    public PneumaticPowerHammerBER(BlockEntityRendererProvider.Context ctx) {
    }

    public void render(PneumaticPowerHammerBlockEntity be, float partialTick, PoseStack ps, MultiBufferSource buffers, int packedLight, int packedOverlay) {
        String metal;
        ResourceLocation anvilModelId;
        ItemStack anvilStack;
        if (be.getLevel() == null) {
            return;
        }
        BlockState state = be.getBlockState();
        Direction facing = (Direction)state.getValue((Property)PneumaticPowerHammerBlock.FACING);
        float theta = be.getRotationAngle(partialTick);
        boolean flipEW = facing == Direction.EAST || facing == Direction.WEST;
        Minecraft mc = Minecraft.getInstance();
        ModelManager models = mc.getModelManager();
        ModelBlockRenderer br = mc.getBlockRenderer().getModelRenderer();
        RenderType rt = RenderType.cutoutMipped();
        ItemStack hammerStack = be.getHammerInventory().getStackInSlot(1);
        if (!hammerStack.isEmpty()) {
            String metal2 = PneumaticPowerHammerBER.metalName(hammerStack);
            BakedModel pm = models.getModel(net.minecraft.client.resources.model.ModelResourceLocation.standalone(PISTON_MODELS.getOrDefault(metal2, MODEL_PISTON)));
            float pistonY = (float)Math.sin(theta) * 0.25f;
            ps.pushPose();
            PneumaticPowerHammerBER.applyFacing(ps, facing);
            if (flipEW) {
                PneumaticPowerHammerBER.applyFlip(ps);
            }
            ps.translate(0.0f, pistonY, 0.0f);
            br.renderModel(ps.last(), buffers.getBuffer(rt), state, pm, 1.0f, 1.0f, 1.0f, packedLight, packedOverlay, ModelData.EMPTY, rt);
            ps.popPose();
        }
        if (!(anvilStack = be.getHammerInventory().getStackInSlot(0)).isEmpty() && (anvilModelId = ANVIL_MODELS.get(metal = PneumaticPowerHammerBER.metalName(anvilStack))) != null) {
            BakedModel anvilModel = models.getModel(net.minecraft.client.resources.model.ModelResourceLocation.standalone(anvilModelId));
            ps.pushPose();
            PneumaticPowerHammerBER.applyFacing(ps, facing);
            float anvilRot = facing == Direction.EAST || facing == Direction.WEST ? -90.0f : 90.0f;
            ps.translate(0.5, 0.5, 0.5);
            ps.mulPose(Axis.YP.rotationDegrees(anvilRot));
            ps.translate(-0.5, -0.5, -0.5);
            br.renderModel(ps.last(), buffers.getBuffer(rt), state, anvilModel, 1.0f, 1.0f, 1.0f, packedLight, packedOverlay, ModelData.EMPTY, rt);
            ps.popPose();
        }
    }

    private static void applyFacing(PoseStack ps, Direction facing) {
        ps.translate(0.5, 0.5, 0.5);
        switch (facing) {
            case EAST: {
                ps.mulPose(Axis.YP.rotationDegrees(90.0f));
                break;
            }
            case SOUTH: {
                ps.mulPose(Axis.YP.rotationDegrees(180.0f));
                break;
            }
            case WEST: {
                ps.mulPose(Axis.YP.rotationDegrees(270.0f));
                break;
            }
        }
        ps.translate(-0.5, -0.5, -0.5);
    }

    private static void applyFlip(PoseStack ps) {
        ps.translate(0.5, 0.5, 0.5);
        ps.mulPose(Axis.YP.rotationDegrees(180.0f));
        ps.translate(-0.5, -0.5, -0.5);
    }

    private static String metalName(ItemStack stack) {
        String path = stack.getItem().builtInRegistryHolder().key().location().getPath();
        String[] seg = path.split("/");
        return seg.length > 0 ? seg[seg.length - 1] : "";
    }

    public int getViewDistance() {
        return 64;
    }

    public boolean shouldRenderOffScreen(PneumaticPowerHammerBlockEntity be) {
        return true;
    }
}

