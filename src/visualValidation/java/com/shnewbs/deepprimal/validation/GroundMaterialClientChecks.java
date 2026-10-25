package com.shnewbs.deepprimal.validation;

import com.mojang.blaze3d.systems.RenderSystem;
import com.shnewbs.deepprimal.client.GroundMaterialColors;
import com.shnewbs.deepprimal.engine.GroundMaterials;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

/** Bounded real-client model/tint/reload check, with actual inventory captures and no game world. */
@Mod(value = "deepprimal", dist = Dist.CLIENT)
public final class GroundMaterialClientChecks {
  private int stage, ticks;
  private long started;
  private volatile boolean captured, reloaded;
  private volatile Throwable reloadFailure;
  private java.util.concurrent.CompletableFuture<Void> waterfallUploads;

  public GroundMaterialClientChecks() {
    NeoForge.EVENT_BUS.register(this);
  }

  private static List<Block> blocks() {
    return List.of(GroundMaterials.GRASS.get(), GroundMaterials.DIRT.get(), GroundMaterials.PODZOL.get());
  }

  @SubscribeEvent
  public void tick(ClientTickEvent.Post event) {
    if (!Boolean.getBoolean("deepprimal.validationGroundMaterials") || stage == 5) return;
    var mc = Minecraft.getInstance();
    if (started == 0) started = System.nanoTime();
    if (System.nanoTime() - started > 120_000_000_000L)
      throw new AssertionError("Ground material client validation timed out, stage=" + stage);
    ticks++;
    if (mc.getOverlay() != null || mc.screen == null) return;
    try {
      if (stage == 0) {
        stage = -1;
        String backend = RenderSystem.getDevice().getBackendName();
        require(backend.equals(System.getProperty("deepprimal.graphicsBackend")), "Requested graphics backend");
        net.minecraft.client.gui.screens.worldselection.CreateWorldScreen.openFresh(mc, () -> {});
        require(mc.screen instanceof net.minecraft.client.gui.screens.worldselection.CreateWorldScreen,
            "Native world-data load binds item components before inventory preview");
        GroundPackageChecks.verify(GroundMaterials.class, GroundMaterialColors.class,
            com.shnewbs.deepprimal.client.GroundMaterialShaders.class,
            com.shnewbs.deepprimal.client.GroundSideSpriteSource.class,
            com.shnewbs.deepprimal.client.render.AnimatedWaterRenderer.class);
        check(mc);
        waterfallUploads = java.util.concurrent.CompletableFuture.allOf(
            WaterFadeContinuityChecks.run(mc, output()), WaterfallUploadChecks.run(output()));
        mc.setScreen(new Preview());
        stage = 1;
        ticks = 0;
      } else if (stage == 1 && ticks > 30) {
        capture(mc, "materials.png");
        stage = 2;
        ticks = 0;
      } else if (stage == 2 && captured && ticks > 5) {
        if (!waterfallUploads.isDone()) return;
        waterfallUploads.join();
        checkCapture(mc, "materials.png");
        mc.reloadResourcePacks().whenComplete((nothing, failure) -> {
          reloadFailure = failure;
          reloaded = true;
        });
        stage = 3;
        ticks = 0;
      } else if (stage == 3 && reloaded && ticks > 30) {
        if (reloadFailure != null) throw new IllegalStateException("Material resource reload failed", reloadFailure);
        check(mc);
        capture(mc, "materials-reloaded.png");
        stage = 4;
        ticks = 0;
      } else if (stage == 4 && captured && ticks > 5) {
        checkCapture(mc, "materials-reloaded.png");
        String result = "GROUND_MATERIAL_CLIENT_PASS backend=" + RenderSystem.getDevice().getBackendName()
            + " sampleBlocks=3 soilBlocks=36 quads=234 tintIndices=true sprites=true items=true resourceReload=true captures=2 opaqueFacePixels=true\n";
        Files.writeString(output().resolve("result.txt"), result);
        System.out.print(result);
        stage = 5;
        mc.stop();
      }
    } catch (java.io.IOException failure) {
      throw new RuntimeException("Ground client validation output failed", failure);
    } catch (ReflectiveOperationException failure) {
      throw new RuntimeException("Waterfall production upload fixture failed", failure);
    }
  }

  private static void check(Minecraft mc) {
    var models = mc.getModelManager().getBlockStateModelSet();
    String[] names = {"grass", "dirt", "podzol"};
    for (int i = 0; i < blocks().size(); i++) {
      var state = blocks().get(i).defaultBlockState();
      var sources = mc.getBlockColors().getTintSources(state);
      require(sources.size() == 3 && sources.get(1).color(state) == GroundMaterialColors.SOIL,
          "Registered soil tint sources");
      int expectedTop = switch (i) {
        case 0 -> net.minecraft.world.level.GrassColor.getDefaultColor();
        case 1 -> GroundMaterialColors.SOIL;
        default -> net.minecraft.world.level.DryFoliageColor.FOLIAGE_DRY_DEFAULT;
      };
      require(sources.get(0).color(state) == expectedTop, "Default surface tint");
      require(sources.get(0).colorAsTerrainParticle(state, null, null) == GroundMaterialColors.SOIL,
          "Particle tint uses side soil");
      var model = models.get(state);
      require(model != models.missingModel(), "Baked material model");
      var parts = new ArrayList<BlockStateModelPart>();
      model.collectParts(RandomSource.create(0), parts);
      int quads = 0;
      for (var part : parts) {
        require(part.getQuads(null).isEmpty(), "No uncullable material faces");
        for (var face : Direction.values()) {
          var faces = part.getQuads(face);
          require(faces.size() == 1, "One culled quad per cube face");
          var quad = faces.getFirst();
          String texture = face == Direction.DOWN ? "dirt_top"
              : names[i] + (face == Direction.UP ? "_top" : "_side");
          if (face.getAxis().isHorizontal() && i != 1) texture += "_material";
          require(quad.materialInfo().sprite().contents().name().equals(
              Identifier.fromNamespaceAndPath("deepprimal", "block/ground/" + texture)), "Baked ground sprite");
          require(quad.materialInfo().tintIndex() == (face == Direction.UP ? 0 : face == Direction.DOWN ? 1 : 2), "Baked tint index");
          require(quad.materialInfo().layer() == net.minecraft.client.renderer.chunk.ChunkSectionLayer.SOLID,
              "Opaque material stays in the solid pass");
          if (face.getAxis().isHorizontal() && i != 1) GroundSideNativeChecks.quad(quad);
          quads++;
        }
      }
      require(quads == 6, "Six material cube quads");
      var item = new ItemStackRenderState();
      mc.getItemModelResolver().updateForTopItem(
          item, new ItemStack(blocks().get(i)), ItemDisplayContext.GUI, null, null, 0);
      require(!item.isEmpty(), "Baked inventory item");
      var particle = item.pickParticleMaterial(RandomSource.create(0));
      require(particle != null && particle.sprite().contents().name().equals(
          Identifier.fromNamespaceAndPath("deepprimal", "block/ground/" + names[i] + "_side")),
          "Inventory item uses actual ground model");
    }
    for (var block : GroundMaterials.soilsList()) {
      var state = block.defaultBlockState();
      var sources = mc.getBlockColors().getTintSources(state);
      require(sources.size() == 3 && sources.get(1).color(state) == block.texture().color(),
          "Soil family mineral tint");
      var model = models.get(state);
      require(model != models.missingModel(), "Soil family baked model");
      var parts = new ArrayList<BlockStateModelPart>();
      model.collectParts(RandomSource.create(0), parts);
      int quads = 0;
      for (var part : parts)
        for (var face : Direction.values()) {
          var faces = part.getQuads(face);
          require(faces.size() == 1, "One soil quad per face");
          var quad = faces.getFirst();
          require(quad.materialInfo().layer() == net.minecraft.client.renderer.chunk.ChunkSectionLayer.SOLID,
              "Opaque soil family");
          if (face.getAxis().isHorizontal() && block.cover() != GroundMaterials.Cover.BARE)
            GroundSideNativeChecks.quad(quad);
          quads++;
        }
      require(quads == 6, "Six soil cube quads");
      var item = new ItemStackRenderState();
      mc.getItemModelResolver().updateForTopItem(item, new ItemStack(block), ItemDisplayContext.GUI, null, null, 0);
      require(!item.isEmpty(), "Soil family inventory model");
    }
  }

  private void capture(Minecraft mc, String name) throws java.io.IOException {
    Files.createDirectories(output());
    captured = false;
    Screenshot.grab(output().toFile(), name, mc.getMainRenderTarget(), 1, message -> captured = true);
  }

  private static Path output() {
    return Path.of(System.getProperty("deepprimal.validationCapture"));
  }

  private static void checkCapture(Minecraft mc, String name) throws java.io.IOException {
    var image = javax.imageio.ImageIO.read(output().resolve("screenshots").resolve(name).toFile());
    require(image != null, "Readable material screenshot");
    int width = mc.screen.width;
    float scale = Math.max(1, Math.min(5, (width - 48) / 80F));
    double pixelsX = (double) image.getWidth() / width;
    double pixelsY = (double) image.getHeight() / mc.screen.height;
    for (int i = 0; i < blocks().size(); i++) {
      int origin = 24 + (width - 48) * i / 3;
      for (double[] point : new double[][] {{8, 4}, {4.5, 10.5}, {11.5, 10.5}}) {
        int x = (int) Math.round((origin + point[0] * scale) * pixelsX);
        int y = (int) Math.round((60 + point[1] * scale) * pixelsY);
        for (int dy = -2; dy <= 2; dy++)
          for (int dx = -2; dx <= 2; dx++)
            require((image.getRGB(x + dx, y + dy) & 0xffffff) != 0x20242a,
                "Missing opaque material face: " + name + " block=" + i + " x=" + x + " y=" + y);
      }
    }
    for (int i = 0; i < GroundMaterials.soilsList().size(); i++) {
      double originX = 16 + (i / 3) * (width - 32) / 12.;
      int originY = 160 + (i % 3) * 30;
      for (double[] point : new double[][] {{8, 4}, {4.5, 10.5}, {11.5, 10.5}}) {
        int x = (int) Math.round((originX + point[0] * 1.35) * pixelsX);
        int y = (int) Math.round((originY + point[1] * 1.35) * pixelsY);
        require((image.getRGB(x, y) & 0xffffff) != 0x20242a, "Missing soil family face " + i);
      }
    }
  }

  private static void require(boolean condition, String message) {
    if (!condition) throw new AssertionError(message);
  }

  private static final class Preview extends Screen {
    private Preview() {
      super(Component.literal("Deep Primal ground material samples"));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
      graphics.fill(0, 0, width, height, 0xff20242a);
      graphics.text(font, "Deep Primal - approved ground textures", 16, 16, 0xffffffff, false);
      graphics.text(font, "Opaque sides blend biome surface and soil through the authored masks.", 16, 30, 0xffc6cbd0, false);
      float scale = Math.max(1, Math.min(5, (width - 48) / 80F));
      for (int i = 0; i < blocks().size(); i++) {
        int x = 24 + (width - 48) * i / 3;
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, 60);
        graphics.pose().scale(scale, scale);
        graphics.item(new ItemStack(blocks().get(i)), 0, 0);
        graphics.pose().popMatrix();
        graphics.text(font, blocks().get(i).getName(), x, (int) (66 + 16 * scale), 0xffffffff, false);
      }
      var soils = GroundMaterials.soilsList();
      for (int i = 0; i < soils.size(); i++) {
        int column = i / 3, row = i % 3;
        graphics.pose().pushMatrix();
        graphics.pose().translate(16 + column * (width - 32) / 12F, 160 + row * 30);
        graphics.pose().scale(1.35F, 1.35F);
        graphics.item(new ItemStack(soils.get(i)), 0, 0);
        graphics.pose().popMatrix();
      }
    }
  }
}
