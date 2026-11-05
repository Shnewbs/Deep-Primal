package com.shnewbs.deepprimal.engine;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.shnewbs.deepprimal.core.*;
import java.util.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;

/** Inline datapack profile becomes a complete saved generator snapshot, including block states. */
public record TerrainProfile(TerrainOptions options,List<Deposit> deposits) {
  private static <E extends Enum<E>> Codec<E> enumeration(Class<E> type) {
    return Codec.STRING.comapFlatMap(s->{try{return DataResult.success(Enum.valueOf(type,s.toUpperCase(Locale.ROOT)));}
      catch(IllegalArgumentException e){return DataResult.error(()->"Unknown "+type.getSimpleName()+": "+s);}},e->e.name().toLowerCase(Locale.ROOT));
  }
  public static final Codec<TerrainOptions> OPTIONS=RecordCodecBuilder.create(i->i.group(
      Codec.BOOL.optionalFieldOf("caves",true).forGetter(TerrainOptions::caves),
      Codec.BOOL.optionalFieldOf("river_tunnels",true).forGetter(TerrainOptions::riverTunnels),
      Codec.BOOL.optionalFieldOf("dunes",true).forGetter(TerrainOptions::dunes),
      Codec.BOOL.optionalFieldOf("glaciers",true).forGetter(TerrainOptions::glaciers),
      Codec.BOOL.optionalFieldOf("debris",true).forGetter(TerrainOptions::debris),
      Codec.doubleRange(.5,2).optionalFieldOf("mountain_scale",1.3).forGetter(TerrainOptions::mountainScale),
      Codec.doubleRange(4000,1000000).optionalFieldOf("polar_distance",16000.).forGetter(TerrainOptions::polarDistance)
  ).apply(i,TerrainOptions::new));
  public record Content(BlockState block,int weight) {
    static final Codec<Content> CODEC=RecordCodecBuilder.create(i->i.group(
        BlockState.CODEC.fieldOf("block").forGetter(Content::block),Codec.intRange(1,10000).optionalFieldOf("weight",1).forGetter(Content::weight)
    ).apply(i,Content::new));
  }
  public record Geometry(DepositPlan.Shape shape,double x,double y,double z) {
    static final Codec<Geometry> CODEC=RecordCodecBuilder.create(i->i.group(
        enumeration(DepositPlan.Shape.class).fieldOf("shape").forGetter(Geometry::shape),
        Codec.doubleRange(.5,96).fieldOf("radius_x").forGetter(Geometry::x),
        Codec.doubleRange(.5,96).fieldOf("radius_y").forGetter(Geometry::y),
        Codec.doubleRange(.5,96).fieldOf("radius_z").forGetter(Geometry::z)
    ).apply(i,Geometry::new));
  }
  public record Vertical(DepositPlan.Reference reference,int min,int max) {
    static final Codec<Vertical> CODEC=RecordCodecBuilder.create(i->i.group(
        enumeration(DepositPlan.Reference.class).fieldOf("reference").forGetter(Vertical::reference),
        Codec.intRange(-2048,2048).fieldOf("min").forGetter(Vertical::min),Codec.intRange(-2048,2048).fieldOf("max").forGetter(Vertical::max)
    ).apply(i,Vertical::new));
  }
  public record Deposit(String id,int spacing,double occurrence,double density,Geometry geometry,Vertical vertical,
      List<Geology.Rock> hosts,List<Content> contents) {
    static final Codec<Deposit> CODEC=RecordCodecBuilder.create(i->i.group(
        Codec.STRING.fieldOf("id").forGetter(Deposit::id),Codec.intRange(32,1024).fieldOf("spacing").forGetter(Deposit::spacing),
        Codec.doubleRange(0,1).fieldOf("occurrence").forGetter(Deposit::occurrence),Codec.doubleRange(0,1).fieldOf("density").forGetter(Deposit::density),
        Geometry.CODEC.fieldOf("geometry").forGetter(Deposit::geometry),Vertical.CODEC.fieldOf("vertical").forGetter(Deposit::vertical),
        enumeration(Geology.Rock.class).listOf().fieldOf("hosts").forGetter(Deposit::hosts),Content.CODEC.listOf().fieldOf("contents").forGetter(Deposit::contents)
    ).apply(i,Deposit::new));
    public Deposit { hosts=List.copyOf(hosts);contents=List.copyOf(contents); }
  }
  private static final Codec<TerrainProfile> RAW=RecordCodecBuilder.create(i->i.group(
      OPTIONS.optionalFieldOf("options",TerrainOptions.DEFAULT).forGetter(TerrainProfile::options),
      Deposit.CODEC.listOf().fieldOf("deposits").forGetter(TerrainProfile::deposits)
  ).apply(i,TerrainProfile::new));
  public static final Codec<TerrainProfile> CODEC=RAW.validate(p->{try{p.plan(new TerrainNoise(0));return DataResult.success(p);}
    catch(IllegalArgumentException ex){return DataResult.error(ex::getMessage);}});
  public TerrainProfile { options=Objects.requireNonNull(options);deposits=List.copyOf(deposits); }
  public List<BlockState> palette() { return deposits.stream().flatMap(d->d.contents.stream()).map(Content::block).toList(); }
  public DepositPlan plan(TerrainNoise noise) {
    var definitions=new ArrayList<DepositPlan.Definition>();int index=0;
    for(var d:deposits) {
      var contents=new ArrayList<DepositPlan.Content>();
      for(var c:d.contents) {
        if(c.block.isAir()||!c.block.getFluidState().isEmpty())throw new IllegalArgumentException("Ore contents must be non-fluid blocks");
        contents.add(new DepositPlan.Content(index++,c.weight));
      }
      definitions.add(new DepositPlan.Definition(d.id,d.geometry.shape,d.vertical.reference,d.spacing,d.occurrence,d.density,
          d.vertical.min,d.vertical.max,d.geometry.x,d.geometry.y,d.geometry.z,Set.copyOf(d.hosts),contents));
    }
    return new DepositPlan(noise,definitions);
  }
  public static TerrainProfile defaults(TerrainOptions options) {
    var all=List.of(Geology.Rock.values());var d=new ArrayList<Deposit>();
    Block[] ores={Blocks.COAL_ORE,Blocks.COPPER_ORE,Blocks.IRON_ORE,Blocks.GOLD_ORE,Blocks.REDSTONE_ORE,Blocks.LAPIS_ORE,Blocks.DIAMOND_ORE,Blocks.EMERALD_ORE};
    String[] names={"coal","copper","iron","gold","redstone","lapis","diamond","emerald"};
    List<List<Geology.Rock>> hosts=List.of(List.of(Geology.Rock.SANDSTONE,Geology.Rock.TUFF),List.of(Geology.Rock.ANDESITE,Geology.Rock.CALCITE),all,
        List.of(Geology.Rock.GRANITE,Geology.Rock.DIORITE),List.of(Geology.Rock.BASALT,Geology.Rock.GRANITE),List.of(Geology.Rock.CALCITE),
        List.of(Geology.Rock.BASALT,Geology.Rock.DEEPSLATE),List.of(Geology.Rock.CALCITE,Geology.Rock.GRANITE));
    for(int i=0;i<8;i++)d.add(new Deposit("deepprimal:"+names[i],i<3?64:96,i<3?.40:.22,i<3?.43:.25,
        new Geometry(i==5?DepositPlan.Shape.DISC:i==6?DepositPlan.Shape.PIPE:i==3?DepositPlan.Shape.VEIN:DepositPlan.Shape.LENS,i<3?14:8,i<3?5:3,i<3?9:5),
        new Vertical(DepositPlan.Reference.DEPTH,i<3?12:25,i<3?100:180),hosts.get(i),List.of(new Content(ores[i].defaultBlockState(),1))));
    return new TerrainProfile(options,d);
  }
}
