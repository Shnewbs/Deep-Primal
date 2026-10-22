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
import tfcmt.tfcitembarrelsandmore.common.blockentities.MechanicalPowerHammerBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.MechanicalPowerHammerBlock;

public class MechanicalPowerHammerBER
implements BlockEntityRenderer<MechanicalPowerHammerBlockEntity> {
    private static final String MODID = "tfc_item_barrels_and_more";
    private static final ResourceLocation MODEL_FLYWHEEL = ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/mechanical_power_hammer_flywheel");
    private static final ResourceLocation MODEL_ARM = ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/mechanical_power_hammer_arm");
    private static final ResourceLocation MODEL_PISTON = ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/mechanical_power_hammer_piston");
    private static final float PIVOT_X = 0.5f;
    private static final float PIVOT_Y = 1.5f;
    private static final float PIVOT_Z = 0.5f;
    private static final float ARM_SWING_DEG = 25.0f;
    private static final float PISTON_STROKE = 0.25f;
    private static final float ANVIL_X = 0.0f;
    private static final float ANVIL_Y = 0.0f;
    private static final float ANVIL_Z = 0.0f;
    private static final Map<String, ResourceLocation> PISTON_MODELS = Map.of("bismuth_bronze", ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/mechanical_power_hammer_piston_bismuth_bronze"), "black_bronze", ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/mechanical_power_hammer_piston_black_bronze"), "bronze", ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/mechanical_power_hammer_piston_bronze"), "copper", ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/mechanical_power_hammer_piston_copper"), "wrought_iron", ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/mechanical_power_hammer_piston_wrought_iron"), "steel", ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/mechanical_power_hammer_piston_steel"), "black_steel", ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/mechanical_power_hammer_piston_black_steel"), "blue_steel", ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/mechanical_power_hammer_piston_blue_steel"), "red_steel", ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/mechanical_power_hammer_piston_red_steel"));
    private static final Map<String, ResourceLocation> ANVIL_MODELS = Map.of("bismuth_bronze", ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/mechanical_power_hammer_anvil_bismuth_bronze"), "black_bronze", ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/mechanical_power_hammer_anvil_black_bronze"), "bronze", ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/mechanical_power_hammer_anvil_bronze"), "copper", ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/mechanical_power_hammer_anvil_copper"), "wrought_iron", ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/mechanical_power_hammer_anvil_wrought_iron"), "steel", ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/mechanical_power_hammer_anvil_steel"), "black_steel", ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/mechanical_power_hammer_anvil_black_steel"), "blue_steel", ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/mechanical_power_hammer_anvil_blue_steel"), "red_steel", ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "block/mechanical_power_hammer_anvil_red_steel"));

    public MechanicalPowerHammerBER(BlockEntityRendererProvider.Context ctx) {
    }

    public void render(MechanicalPowerHammerBlockEntity be, float partialTick, PoseStack ps, MultiBufferSource buffers, int packedLight, int packedOverlay) {
        String anvilMetal;
        ResourceLocation anvilModelId;
        ItemStack anvilStack;
        if (be.getLevel() == null) {
            return;
        }
        BlockState state = be.getBlockState();
        Direction facing = (Direction)state.getValue((Property)MechanicalPowerHammerBlock.FACING);
        float theta = be.getRotationAngle(partialTick);
        boolean flipEW = facing == Direction.EAST || facing == Direction.WEST;
        Minecraft mc = Minecraft.getInstance();
        ModelManager models = mc.getModelManager();
        ModelBlockRenderer br = mc.getBlockRenderer().getModelRenderer();
        RenderType rt = RenderType.cutoutMipped();
        BakedModel fm = models.getModel(net.minecraft.client.resources.model.ModelResourceLocation.standalone(MODEL_FLYWHEEL));
        ps.pushPose();
        MechanicalPowerHammerBER.applyFacing(ps, facing);
        ps.translate(0.5f, 1.5f, 0.5f);
        ps.mulPose(Axis.ZP.rotation(theta));
        ps.translate(-0.5f, -1.5f, -0.5f);
        br.renderModel(ps.last(), buffers.getBuffer(rt), state, fm, 1.0f, 1.0f, 1.0f, packedLight, packedOverlay, ModelData.EMPTY, rt);
        ps.popPose();
        BakedModel am = models.getModel(net.minecraft.client.resources.model.ModelResourceLocation.standalone(MODEL_ARM));
        float swing = (float)(Math.sin(theta) * Math.toRadians(25.0));
        ps.pushPose();
        MechanicalPowerHammerBER.applyFacing(ps, facing);
        if (flipEW) {
            MechanicalPowerHammerBER.applyFlip(ps);
        }
        ps.translate(0.5f, 1.5f, 0.5f);
        ps.mulPose(Axis.ZP.rotation(swing));
        ps.translate(-0.5f, -1.5f, -0.5f);
        br.renderModel(ps.last(), buffers.getBuffer(rt), state, am, 1.0f, 1.0f, 1.0f, packedLight, packedOverlay, ModelData.EMPTY, rt);
        ps.popPose();
        ItemStack hammerStack = be.getHammerInventory().getStackInSlot(1);
        if (!hammerStack.isEmpty()) {
            String hammerMetal = MechanicalPowerHammerBER.anvilMetalName(hammerStack);
            BakedModel pm = models.getModel(net.minecraft.client.resources.model.ModelResourceLocation.standalone(PISTON_MODELS.getOrDefault(hammerMetal, MODEL_PISTON)));
            float pistonY = (float)Math.sin(theta) * 0.25f;
            ps.pushPose();
            MechanicalPowerHammerBER.applyFacing(ps, facing);
            if (flipEW) {
                MechanicalPowerHammerBER.applyFlip(ps);
            }
            ps.translate(0.0f, pistonY, 0.0f);
            br.renderModel(ps.last(), buffers.getBuffer(rt), state, pm, 1.0f, 1.0f, 1.0f, packedLight, packedOverlay, ModelData.EMPTY, rt);
            ps.popPose();
        }
        if (!(anvilStack = be.getHammerInventory().getStackInSlot(0)).isEmpty() && (anvilModelId = ANVIL_MODELS.get(anvilMetal = MechanicalPowerHammerBER.anvilMetalName(anvilStack))) != null) {
            BakedModel anvilModel = models.getModel(net.minecraft.client.resources.model.ModelResourceLocation.standalone(anvilModelId));
            ps.pushPose();
            MechanicalPowerHammerBER.applyFacing(ps, facing);
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

    private static String anvilMetalName(ItemStack stack) {
        String path = stack.getItem().builtInRegistryHolder().key().location().getPath();
        String[] seg = path.split("/");
        return seg.length > 0 ? seg[seg.length - 1] : "";
    }

    private static float[] metalColor(ItemStack stack) {
        int[] nArray;
        String metal;
        String path = stack.getItem().builtInRegistryHolder().key().location().getPath();
        String[] seg = path.split("/");
        switch (metal = seg.length > 0 ? seg[seg.length - 1] : "") {
            case "copper": {
                int[] nArray2 = new int[3];
                nArray2[0] = 184;
                nArray2[1] = 115;
                nArray = nArray2;
                nArray2[2] = 51;
                break;
            }
            case "bronze": {
                int[] nArray3 = new int[3];
                nArray3[0] = 205;
                nArray3[1] = 127;
                nArray = nArray3;
                nArray3[2] = 50;
                break;
            }
            case "bismuth_bronze": {
                int[] nArray4 = new int[3];
                nArray4[0] = 143;
                nArray4[1] = 172;
                nArray = nArray4;
                nArray4[2] = 129;
                break;
            }
            case "black_bronze": {
                int[] nArray5 = new int[3];
                nArray5[0] = 107;
                nArray5[1] = 76;
                nArray = nArray5;
                nArray5[2] = 59;
                break;
            }
            case "wrought_iron": {
                int[] nArray6 = new int[3];
                nArray6[0] = 130;
                nArray6[1] = 130;
                nArray = nArray6;
                nArray6[2] = 130;
                break;
            }
            case "steel": {
                int[] nArray7 = new int[3];
                nArray7[0] = 120;
                nArray7[1] = 130;
                nArray = nArray7;
                nArray7[2] = 140;
                break;
            }
            case "black_steel": {
                int[] nArray8 = new int[3];
                nArray8[0] = 47;
                nArray8[1] = 49;
                nArray = nArray8;
                nArray8[2] = 48;
                break;
            }
            case "blue_steel": {
                int[] nArray9 = new int[3];
                nArray9[0] = 65;
                nArray9[1] = 105;
                nArray = nArray9;
                nArray9[2] = 225;
                break;
            }
            case "red_steel": {
                int[] nArray10 = new int[3];
                nArray10[0] = 204;
                nArray10[1] = 17;
                nArray = nArray10;
                nArray10[2] = 0;
                break;
            }
            default: {
                int[] nArray11 = new int[3];
                nArray11[0] = 180;
                nArray11[1] = 180;
                nArray = nArray11;
                nArray11[2] = 180;
            }
        }
        int[] rgb = nArray;
        return new float[]{(float)rgb[0] / 255.0f, (float)rgb[1] / 255.0f, (float)rgb[2] / 255.0f};
    }

    public int getViewDistance() {
        return 64;
    }

    public boolean shouldRenderOffScreen(MechanicalPowerHammerBlockEntity be) {
        return true;
    }
}

