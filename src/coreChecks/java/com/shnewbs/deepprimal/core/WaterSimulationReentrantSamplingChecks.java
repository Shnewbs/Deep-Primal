package com.shnewbs.deepprimal.core;
import java.lang.reflect.Field;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

/** Reentrant height callbacks must not corrupt the outer tick's sampled terrain. */
public final class WaterSimulationReentrantSamplingChecks {
  private static long assertions;
  private static void check(boolean valid,String detail){assertions++;if(!valid)throw new AssertionError(detail+" assertion="+assertions);}
  private static double pressure(int ground){return ground*9810.0;}
  public static void main(String[] args) throws Exception {
    Random rng=new Random(0x19120261102L);
    for(int trial=0;trial<300;trial++){
      WaterSimulation simulation=new WaterSimulation();
      int trigger=rng.nextInt(256), outer=rng.nextInt(2001)-1000, inner=rng.nextInt(2001)-1000;
      AtomicBoolean once=new AtomicBoolean();
      simulation.tick(0,(x,z)->{
        if(z*16+x==trigger && once.compareAndSet(false,true))
          simulation.tick(0,(nx,nz)->inner);
        return outer;
      });
      check(once.get(),"callback did not reenter");
      for(int z=0;z<16;z++)for(int x=0;x<16;x++)
        check(simulation.getPressure(x,z)==pressure(outer),
          "reentrant terrain corruption trial="+trial+" trigger="+trigger+" cell="+(z*16+x)+" expected="+outer+" actual="+simulation.getPressure(x,z));
    }
    WaterSimulation nested=new WaterSimulation();
    AtomicBoolean once=new AtomicBoolean();
    try {
      nested.tick(0,(x,z)->{
        if(x==7&&z==5&&once.compareAndSet(false,true))nested.tick(0,(nx,nz)->400);
        if(x==15&&z==15)throw new IllegalArgumentException("height callback failed");
        return 1;
      });
      throw new AssertionError("outer callback failure not propagated");
    }catch(IllegalArgumentException expected){check(expected.getMessage().contains("height callback failed"),"wrong failure");}
    for(int z=0;z<16;z++)for(int x=0;x<16;x++)check(nested.getPressure(x,z)==pressure(400),"failed outer tick overwrote inner commit");
    nested.tick(0,(x,z)->0);
    for(int z=0;z<16;z++)for(int x=0;x<16;x++)check(nested.getPressure(x,z)==0,"recovery tick stale scratch");
    WaterSimulation caught=new WaterSimulation();
    AtomicBoolean attempted=new AtomicBoolean();
    caught.tick(0,(x,z)->{
      if(x==8 && z==8 && attempted.compareAndSet(false,true)){
        try { caught.tick(0,(nx,nz)->nx==15&&nz==15?Double.NaN:900); }
        catch(IllegalArgumentException expected) { check("ground height".equals(expected.getMessage()),"unexpected nested rejection"); }
      }
      return 17;
    });
    check(attempted.get(),"nested failure callback not invoked");
    for(int z=0;z<16;z++)for(int x=0;x<16;x++)
      check(caught.getPressure(x,z)==pressure(17),"nested failure contaminated outer scratch");

    WaterSimulation depthThree=new WaterSimulation();
    AtomicBoolean outerEntered=new AtomicBoolean(), innerEntered=new AtomicBoolean();
    depthThree.tick(0,(x,z)->{
      if(x==5&&z==5&&outerEntered.compareAndSet(false,true))
        depthThree.tick(0,(nx,nz)->{
          if(nx==6&&nz==6&&innerEntered.compareAndSet(false,true))
            depthThree.tick(0,(tx,tz)->999);
          return -200;
        });
      return 88;
    });
    check(outerEntered.get()&&innerEntered.get(),"three-deep sampling not exercised");
    for(int z=0;z<16;z++)for(int x=0;x<16;x++)
      check(depthThree.getPressure(x,z)==pressure(88),"three-deep scratch corruption");

    Field scratch=WaterSimulation.class.getDeclaredField("nextGround");scratch.setAccessible(true);
    Object first=scratch.get(nested);
    for(int i=0;i<100;i++){nested.tick(0,(x,z)->x-z);check(scratch.get(nested)==first,"ordinary tick replaced reusable scratch");}
    System.out.println("WATER_SIMULATION_REENTRANT_SAMPLING_PASS assertions="+assertions+" trials=300");
  }
}
