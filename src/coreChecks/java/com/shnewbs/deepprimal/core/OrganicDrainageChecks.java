package com.shnewbs.deepprimal.core;

import java.net.URLClassLoader;
import java.nio.file.*;
import java.util.*;
import java.util.function.DoubleBinaryOperator;

/** Terrain fixtures and saved-world differential checks for channel revision 10. */
public final class OrganicDrainageChecks {
  private static long checks;
  private static void require(boolean value, String message) {
    checks++;
    if (!value) throw new AssertionError(message);
  }

  private static WatershedRegion region(DoubleBinaryOperator height, boolean organic) {
    return new WatershedRegion(-4096,-4096,height,(x,z)->.7,true,true,
        new TerrainNoise(20261105,true),false,false,false,null,null,true,false,false,organic);
  }

  private static Object field(Object object, String name) throws Exception {
    var field=object.getClass().getDeclaredField(name);
    field.setAccessible(true);
    return field.get(object);
  }

  private static void conserved(WatershedRegion region) throws Exception {
    conserved(region,(x,z)->.7);
  }

  private static void conserved(WatershedRegion region,DoubleBinaryOperator rain) throws Exception {
    region.verify();
    int[] parent=(int[])field(region,"parent"), order=(int[])field(region,"order");
    double[] area=(double[])field(region,"area"), stage=(double[])field(region,"stage");
    boolean[] ocean=(boolean[])field(region,"ocean");
    double ox=(double)field(region,"ox"),oz=(double)field(region,"oz");
    double roots=0, expected=0;
    for (int a=0;a<parent.length;a++) {
      if (!ocean[a]) expected+=1024*rain.applyAsDouble(ox+a%WatershedRegion.SIDE*32,oz+a/WatershedRegion.SIDE*32);
      if (parent[a]<0) roots+=area[a];
      else {
        require(order[parent[a]]<order[a],"Receiver cycle/order");
        require(stage[parent[a]]<=stage[a]+1e-7,"Uphill water stage");
        require(area[parent[a]]+1e-6>=area[a],"Lost contributing area");
      }
    }
    require(Math.abs(roots-expected)<.01,"Runoff conservation: "+roots+" / "+expected);
  }

  private static double[][] corridors(WatershedRegion region) throws Exception {
    Object[] segments=(Object[])field(region,"segments");
    double[][] result=new double[segments.length][6];
    String[] names={"ax","az","bx","bz","ra","rb"};
    for (int n=0;n<segments.length;n++) for (int k=0;k<6;k++)
      result[n][k]=(double)field(segments[n],names[k]);
    return result;
  }

  private static boolean channelAt(double[][] segments,double x,double z) {
    for (var s:segments) {
      double dx=s[2]-s[0],dz=s[3]-s[1],d2=dx*dx+dz*dz;
      double t=Math.clamp(((x-s[0])*dx+(z-s[1])*dz)/d2,0,1);
      if (Math.hypot(x-s[0]-t*dx,z-s[1]-t*dz)<s[4]+t*(s[5]-s[4])+1.01) return true;
    }
    return false;
  }

  public static void main(String[] args) throws Exception {
    double[] heights={12,12,12,12,10,8,12,8,7.5};
    int[] order={0,1,2,3,8,4,5,6,7};
    require(OrganicDrainage.receiver(4,3,32,heights,order,8)==5,
        "Diagonal discovery beat a steeper metric slope");
    Arrays.fill(heights,10);
    require(OrganicDrainage.receiver(4,3,32,heights,order,8)==8,"Flat escape changed");
    require(OrganicDrainage.receiver(0,3,32,heights,order,-1)==-1,"Boundary root changed");
    for (int z=0;z<3;z++) for (int x=0;x<3;x++) heights[z*3+x]=150+x*2+z*3;
    require(!OrganicDrainage.convergent(4,3,32,heights),"Plane became a hollow");
    for (int z=0;z<3;z++) for (int x=0;x<3;x++) heights[z*3+x]=150+x*2+(z-1)*(z-1)*3;
    require(OrganicDrainage.convergent(4,3,32,heights),"Valley not admitted");
    var flat=OrganicDrainage.lowerCorridor((x,z)->150,12,-37,1,0,12);
    require(flat.x()==12 && flat.z()==-37,"Forced wiggle on flat terrain");
    var lower=OrganicDrainage.lowerCorridor((x,z)->150+.2*(z+29)*(z+29),12,-37,1,0,12);
    require(lower.z()==-29,"Path did not follow lower terrain");

    DoubleBinaryOperator plane=(x,z)->250+x*.014+z*.009;
    var oldPlane=region(plane,false); var newPlane=region(plane,true);
    conserved(newPlane);
    require(!oldPlane.centerline().isEmpty(),"Missing lattice-artifact fixture");
    require(newPlane.centerline().isEmpty(),"Sheet runoff cut periodic parallel channels");
    DoubleBinaryOperator bowl=(x,z)->150+x*.014-38*Math.exp(-((x-300)*(x-300)+z*z)/(260.*260));
    var lake=region(bowl,true);
    conserved(lake);
    require(!lake.lakeProbes().isEmpty(),"Real depression lost its lake");
    require(lake.deltaProbes().isEmpty() && lake.oxbowProbes().isEmpty(),"Legacy shape templates enabled");
    var corridors=corridors(lake);
    int rawBeds=0, wet=0;
    for (int z=-400;z<=400;z+=4) for (int x=-100;x<=700;x+=4) {
      double raw=bowl.applyAsDouble(x,z);
      var s=lake.sample(x,z,raw);
      require(Double.isFinite(s.ground()),"Nonfinite bed");
      require(s.equals(lake.sample(x,z,raw)),"Sampling mutated geography");
      if (Double.isFinite(s.water())) {
        wet++;
        require(s.ground()<=Math.ceil(s.water())-1.001+1e-8,"No wet voxel");
        if (s.speed()==0 && raw<s.water()-2) {
          if (Math.abs(s.ground()-raw)<1e-8) rawBeds++;
          else require(channelAt(corridors,x,z),"Basin bed changed outside routed inlet/outlet corridor at "+x+","+z);
        }
      }
    }
    require(wet>1000 && rawBeds>1000,"Missing terrain-derived lake-bed witness");
    // Finite lake interiors must not have a routed lattice carved through their beds.
    int[] lakeAt=(int[])field(lake,"lakeAt");
    Object[] lakes=(Object[])field(lake,"lakes");
    int[] parent=(int[])field(lake,"parent");
    Object[] segments=(Object[])field(lake,"segments");
    require(segments.length>0,"Lake outlet was not routed");
    int internal=0;
    for (int a=0;a<parent.length;a++) {
      int b=parent[a],id=lakeAt[a];
      if (b>=0 && id>0 && id==lakeAt[b]) internal++;
    }
    require(internal>0 && lakes.length>1,"Missing internally routed lake fixture");
    boolean[] flags=(boolean[])field(lake,"channelized");
    for (int a=0;a<flags.length;a++)
      if (flags[a] && parent[a]>=0) require(flags[parent[a]],"Channel trunk disconnected");
    for (var lp:lake.lakeProbes()) {
      var s=lake.sample(lp.outletX(),lp.outletZ(),bowl.applyAsDouble(lp.outletX(),lp.outletZ()));
      require(Double.isFinite(s.water()),"Dry lake outlet");
    }

    int engineWet=0, engineReaches=0, movedKnots=0;
    for (long seed:new long[] {0,1,2,3,-17,20261020,Long.MIN_VALUE,Long.MAX_VALUE}) {
      var engine=new TerrainEngine(seed,7,TerrainOptions.DEFAULT,10);
      var replay=new TerrainEngine(seed,7,TerrainOptions.DEFAULT,10);
      var r=engine.waterways(0,0);
      conserved(r,engine::rainfall);
      require(r.deltaProbes().isEmpty() && r.oxbowProbes().isEmpty(),"Engine applied legacy templates");
      double[] px=(double[])field(r,"pathX"),pz=(double[])field(r,"pathZ");
      double ox=(double)field(r,"ox"),oz=(double)field(r,"oz");
      for (int n=0;n<px.length;n++)
        if (Math.abs(px[n]-(ox+n%WatershedRegion.SIDE*32))>1e-6
            || Math.abs(pz[n]-(oz+n/WatershedRegion.SIDE*32))>1e-6) movedKnots++;
      var center=r.centerline();
      engineReaches+=r.statistics().reaches();
      require(!center.isEmpty(),"Terrain valleys lost all channels");
      for (int n=0;n<center.size();n+=Math.max(1,center.size()/800)) {
        var p=center.get(n); var s=r.sample(p.x(),p.z(),engine.raw(p.x(),p.z()));
        require(Double.isFinite(s.ground()) && Double.isFinite(s.water()),"Dry/nonfinite reach center");
        require(s.ground()<=Math.ceil(s.water())-1.001+1e-8,"No reach wet voxel");
        engineWet++;
      }
      // Signed coordinates and reverse sampling order exercise independent region/cache histories.
      var samples=new ArrayList<TerrainEngine.Sample>();
      for (int n=0;n<64;n++) samples.add(engine.column(-1900+n*67,-900+n*29));
      for (int n=63;n>=0;n--)
        require(samples.get(n).equals(replay.column(-1900+n*67,-900+n*29)),"Seed/order replay changed");
    }
    require(movedKnots>0 && engineWet>0,"Terrain-fitted paths not connected to production engine");
    if (args.length>=2) legacyReplay(Path.of(args[0]),Path.of(args[1]));
    System.out.println("ORGANIC_DRAINAGE_PASS checks="+checks+" planeLegacyReaches="+oldPlane.statistics().reaches()
        +" planeOrganicReaches="+newPlane.statistics().reaches()+" wetBasinSamples="+wet
        +" unmodifiedRawBeds="+rawBeds+" engineSeeds=8 reaches="+engineReaches
        +" wetReachSamples="+engineWet+" terrainFittedKnots="+movedKnots);
  }

  private static void legacyReplay(Path baseline,Path coreJar) throws Exception {
    try (var loader=new URLClassLoader(new java.net.URL[] {baseline.toUri().toURL(),coreJar.toUri().toURL()},
        ClassLoader.getPlatformClassLoader())) {
      var type=loader.loadClass("com.shnewbs.deepprimal.core.TerrainEngine");
      var optionsType=loader.loadClass("com.shnewbs.deepprimal.core.TerrainOptions");
      var options=optionsType.getField("DEFAULT").get(null);
      var constructor=type.getConstructor(long.class,int.class,optionsType,int.class);
      var column=type.getMethod("column",double.class,double.class);
      String[] fields={"ground","water","rain","temperature","slope","sediment","flowX","flowZ","speed","rock"};
      for (int revision=0;revision<=9;revision++) for (long seed:new long[] {0,20261020}) {
        Object old=constructor.newInstance(seed,7,options,revision);
        var next=new TerrainEngine(seed,7,TerrainOptions.DEFAULT,revision);
        for (int n=0;n<32;n++) {
          int x=160+n*37,z=-1200+n*59;
          Object a=column.invoke(old,x,z); var b=next.column(x,z);
          double[] values={b.ground(),b.water(),b.rain(),b.temperature(),b.slope(),b.sediment(),b.flowX(),b.flowZ(),b.speed(),b.rock()};
          for (int i=0;i<fields.length;i++) {
            double actual=((Number)a.getClass().getMethod(fields[i]).invoke(a)).doubleValue();
            require(Double.doubleToLongBits(actual)==Double.doubleToLongBits(values[i]),
                "Saved revision "+revision+" changed "+fields[i]);
          }
        }
      }
    }
    System.out.println("ORGANIC_DRAINAGE_LEGACY_PASS revisions=0..9 columns=640 fields=6400");
  }

  private OrganicDrainageChecks() {}
}
