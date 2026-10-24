package tfcmt.tfcitembarrelsandmore.client.model;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class DiveSuitModel
extends HumanoidModel<LivingEntity> {
    public static final ModelLayerLocation HELMET_LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "diving_helmet"), "main");
    public static final ModelLayerLocation CHEST_LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "diving_suit"), "main");
    public static final ModelLayerLocation LEGGINGS_LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "diving_leggings"), "main");
    public static final ModelLayerLocation BOOTS_LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "diving_boots"), "main");

    public DiveSuitModel(ModelPart root) {
        super(root);
    }

    private static void addVanillaHeadAndHat(PartDefinition root) {
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(70, 18).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), PartPose.ZERO);
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
    }

    private static void addVanillaBody(PartDefinition root) {
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(40, 0).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f), PartPose.ZERO);
    }

    private static void addVanillaArms(PartDefinition root) {
        root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset((float)-5.0f, (float)2.0f, (float)0.0f));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(20, 0).addBox(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset((float)5.0f, (float)2.0f, (float)0.0f));
    }

    private static void addVanillaLegs(PartDefinition root) {
        root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 18).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset((float)-1.9f, (float)12.0f, (float)0.0f));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(104, 0).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset((float)1.9f, (float)12.0f, (float)0.0f));
    }

    public static LayerDefinition createHelmetLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(70, 18).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f).texOffs(68, 0).addBox(4.0f, -8.0f, -4.0f, 1.0f, 9.0f, 8.0f).texOffs(86, 0).addBox(-5.0f, -8.0f, -4.0f, 1.0f, 9.0f, 8.0f).texOffs(34, 18).addBox(-4.0f, -8.0f, 4.0f, 8.0f, 8.0f, 1.0f).texOffs(52, 18).addBox(-4.0f, -8.0f, -5.0f, 8.0f, 8.0f, 1.0f).texOffs(70, 18).addBox(-4.0f, -9.0f, -4.0f, 8.0f, 1.0f, 8.0f).texOffs(0, 33).addBox(-4.0f, 0.0f, -4.0f, 8.0f, 1.0f, 8.0f).texOffs(76, 33).addBox(5.0f, -7.0f, -3.0f, 1.0f, 6.0f, 6.0f).texOffs(90, 33).addBox(-6.0f, -7.0f, -3.0f, 1.0f, 6.0f, 6.0f).texOffs(20, 18).addBox(-3.0f, -7.0f, -6.0f, 6.0f, 6.0f, 1.0f), PartPose.ZERO);
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        DiveSuitModel.addVanillaBody(root);
        DiveSuitModel.addVanillaArms(root);
        DiveSuitModel.addVanillaLegs(root);
        return LayerDefinition.create((MeshDefinition)mesh, (int)128, (int)64);
    }

    public static LayerDefinition createChestLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        DiveSuitModel.addVanillaHeadAndHat(root);
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(40, 0).addBox(-4.5f, -0.5f, -2.5f, 9.0f, 13.0f, 5.0f), PartPose.ZERO);
        root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 0).addBox(-3.5f, -2.5f, -2.5f, 5.0f, 13.0f, 5.0f), PartPose.offset((float)-5.0f, (float)2.0f, (float)0.0f));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(20, 0).addBox(-1.5f, -2.5f, -2.5f, 5.0f, 13.0f, 5.0f), PartPose.offset((float)5.0f, (float)2.0f, (float)0.0f));
        DiveSuitModel.addVanillaLegs(root);
        return LayerDefinition.create((MeshDefinition)mesh, (int)128, (int)64);
    }

    public static LayerDefinition createLeggingsLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        DiveSuitModel.addVanillaHeadAndHat(root);
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(40, 0).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f, new CubeDeformation(-0.5f)), PartPose.ZERO);
        root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset((float)-5.0f, (float)2.0f, (float)0.0f));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(20, 0).addBox(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset((float)5.0f, (float)2.0f, (float)0.0f));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 18).addBox(-2.4f, -0.4f, -2.5f, 5.0f, 10.0f, 5.0f), PartPose.offset((float)-1.9f, (float)12.0f, (float)0.0f));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(104, 0).addBox(-2.4f, -0.4f, -2.5f, 5.0f, 10.0f, 5.0f), PartPose.offset((float)1.9f, (float)12.0f, (float)0.0f));
        return LayerDefinition.create((MeshDefinition)mesh, (int)128, (int)64);
    }

    public static LayerDefinition createBootsLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        DiveSuitModel.addVanillaHeadAndHat(root);
        DiveSuitModel.addVanillaBody(root);
        DiveSuitModel.addVanillaArms(root);
        root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(32, 33).addBox(-3.0f, 9.1f, -3.0f, 5.0f, 3.0f, 6.0f), PartPose.offset((float)-1.9f, (float)12.0f, (float)0.0f));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(54, 33).addBox(-2.0f, 9.1f, -3.0f, 5.0f, 3.0f, 6.0f), PartPose.offset((float)1.9f, (float)12.0f, (float)0.0f));
        return LayerDefinition.create((MeshDefinition)mesh, (int)128, (int)64);
    }
}

