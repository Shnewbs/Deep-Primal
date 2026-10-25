package com.shnewbs.deepprimal.validation;

import com.shnewbs.deepprimal.engine.GroundMaterials;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Native registry, loot, placement and separate-process reopen contract for material samples. */
final class GroundMaterialChecks {
  static void run(ServerLevel level, boolean reopen) {
    var blocks = new java.util.ArrayList<Block>(List.of(GroundMaterials.GRASS.get(), GroundMaterials.DIRT.get(),
        GroundMaterials.PODZOL.get()));
    blocks.addAll(GroundMaterials.soilsList());
    var ids = blocks.stream().map(block -> BuiltInRegistries.BLOCK.getKey(block).getPath()).toList();
    try {
      var permeability = com.shnewbs.deepprimal.engine.HydrologyRuntime.class.getDeclaredMethod(
          "permeability", BlockState.class, com.shnewbs.deepprimal.core.Geology.Column.class);
      permeability.setAccessible(true);
      for (var soil : GroundMaterials.soilsList())
        require((double) permeability.invoke(null, soil.defaultBlockState(), null) == soil.texture().permeability(),
            "Hydrology reads each soil family's permeability independently of cover");
    } catch (ReflectiveOperationException failure) {
      throw new AssertionError("Production soil permeability contract", failure);
    }
    int checks = 0;
    var tool = new ItemStack(Items.IRON_SHOVEL);
    for (int i = 0; i < blocks.size(); i++) {
      Block block = blocks.get(i);
      BlockState state = block.defaultBlockState();
      var id = Identifier.fromNamespaceAndPath("deepprimal", ids.get(i));
      require(BuiltInRegistries.BLOCK.getKey(block).equals(id), "Block registry " + id);
      require(BuiltInRegistries.ITEM.getKey(block.asItem()).equals(id), "Item registry " + id);
      require(state.is(BlockTags.DIRT), "Plant substrate tag " + id);
      require(state.is(BlockTags.MINEABLE_WITH_SHOVEL), "Shovel tag " + id);
      require(!state.isRandomlyTicking(), "Material sample must not spread vanilla blocks " + id);
      var pos = new BlockPos(15 + i, level.getMaxY() - 8, -16);
      if (reopen) {
        require(level.getBlockState(pos).is(block), "Saved material identity " + id);
      } else {
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        require(level.getBlockState(pos).isAir(), "Clear fixture position " + id);
        require(level.setBlock(pos, state, Block.UPDATE_CLIENTS), "Place material " + id);
      }
      var drops = Block.getDrops(state, level, pos, null, null, tool);
      require(drops.size() == 1 && drops.getFirst().is(block.asItem())
          && drops.getFirst().getCount() == 1, "Self drop " + id + ": " + drops);
      require(level.getBlockState(pos).is(block), "Read material " + id);
      require(Blocks.SHORT_GRASS.defaultBlockState().canSurvive(level, pos.above()),
          "Grass plant support " + id);
      checks += reopen ? 9 : 10;
    }
    System.out.println("GROUND_MATERIAL_NATIVE_PASS checks=" + checks
        + " materials=" + blocks.size() + " reopen=" + reopen
        + " selfDrops=true plantSupport=true soilPermeabilities=36");
  }

  private static void require(boolean condition, String message) {
    if (!condition) throw new AssertionError(message);
  }

  private GroundMaterialChecks() {}
}
