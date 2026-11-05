import com.shnewbs.deepprimal.core.density.V8GenerationBorderAdmission;
import com.shnewbs.deepprimal.core.density.V8GenerationBorderAdmission.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class V8GenerationBorderAdmissionChecks {
  private static long checks;
  private static void require(boolean value, String reason) {
    checks++;
    if (!value) throw new AssertionError(reason);
  }
  private static void reject(Runnable action, String reason) {
    checks++;
    try { action.run(); }
    catch (IllegalArgumentException expected) { return; }
    throw new AssertionError("accepted " + reason);
  }
  private static Neighbor neighbor(Direction d, int x, int z, State s, long id) {
    return new Neighbor(d, x + (d == Direction.EAST ? 1 : d == Direction.WEST ? -1 : 0),
        z + (d == Direction.SOUTH ? 1 : d == Direction.NORTH ? -1 : 0), s, id);
  }
  private static List<Neighbor> four(int x, int z, long id) {
    return new ArrayList<>(List.of(
        neighbor(Direction.NORTH, x, z, State.GENERATED, id),
        neighbor(Direction.EAST, x, z, State.UNGENERATED, 0),
        neighbor(Direction.SOUTH, x, z, State.GENERATED, id),
        neighbor(Direction.WEST, x, z, State.UNGENERATED, 0)));
  }
  private static void test(int x, int z, long id) {
    var mutable = four(x,z,id);
    var witness = new Witness(x,z,id,mutable);
    mutable.clear(); // construction must snapshot observations
    V8GenerationBorderAdmission.requireCompatible(witness,x,z,id);
    checks++;
    reject(() -> V8GenerationBorderAdmission.requireCompatible(witness,x,z,id+1),"wrong caller identity");
    reject(() -> V8GenerationBorderAdmission.requireCompatible(witness,x+1,z,id),"wrong caller X");
    reject(() -> V8GenerationBorderAdmission.requireCompatible(witness,x,z+1,id),"wrong caller Z");
    reject(() -> V8GenerationBorderAdmission.requireCompatible(witness,x,z,0),"zero identity");
    for (int i=0;i<4;i++) {
      final int at=i;
      var wrong=four(x,z,id);
      Neighbor old=wrong.get(at);
      wrong.set(at,new Neighbor(old.direction(),old.chunkX(),old.chunkZ(),State.GENERATED,id+1));
      reject(() -> V8GenerationBorderAdmission.requireCompatible(new Witness(x,z,id,wrong),x,z,id),"edited generated neighbor");
      var unknown=four(x,z,id);
      old=unknown.get(at);
      unknown.set(at,new Neighbor(old.direction(),old.chunkX(),old.chunkZ(),State.UNKNOWN,0));
      reject(() -> V8GenerationBorderAdmission.requireCompatible(new Witness(x,z,id,unknown),x,z,id),"unknown neighbor");
      var stale=four(x,z,id);
      old=stale.get(at);
      stale.set(at,new Neighbor(old.direction(),old.chunkX()+1,old.chunkZ(),old.state(),old.savedProfileFingerprint()));
      reject(() -> V8GenerationBorderAdmission.requireCompatible(new Witness(x,z,id,stale),x,z,id),"stale neighbor coordinate");
      var phantom=four(x,z,id);
      old=phantom.get(at);
      phantom.set(at,new Neighbor(old.direction(),old.chunkX(),old.chunkZ(),State.UNGENERATED,id));
      reject(() -> V8GenerationBorderAdmission.requireCompatible(new Witness(x,z,id,phantom),x,z,id),"ungenerated with identity");
    }
    var duplicate=four(x,z,id);
    duplicate.set(3,duplicate.get(0));
    reject(() -> V8GenerationBorderAdmission.requireCompatible(new Witness(x,z,id,duplicate),x,z,id),"duplicate direction");
    reject(() -> new Witness(x,z,id,four(x,z,id).subList(0,3)),"missing neighbor");
    reject(() -> new Witness(x,z,0,four(x,z,id)),"missing owner identity");
    var reordered=four(x,z,id);
    Collections.reverse(reordered);
    V8GenerationBorderAdmission.requireCompatible(new Witness(x,z,id,reordered),x,z,id);
    checks++;
  }
  public static void main(String[] args) {
    for (int x : new int[]{-2000000,-17,-16,-1,0,1,16,17,2000000})
      for (int z : new int[]{-2000000,-17,-16,-1,0,1,16,17,2000000})
        for (long id : new long[]{1L,-1L,Long.MIN_VALUE,Long.MAX_VALUE})
          test(x,z,id);
    reject(() -> V8GenerationBorderAdmission.requireCompatible(
        new Witness(Integer.MAX_VALUE,0,1,four(Integer.MAX_VALUE,0,1)),
        Integer.MAX_VALUE,0,1),"overflow east");
    reject(() -> V8GenerationBorderAdmission.requireCompatible(
        new Witness(0,Integer.MIN_VALUE,1,four(0,Integer.MIN_VALUE,1)),
        0,Integer.MIN_VALUE,1),"overflow north");
    System.out.println("V8_BORDER_ADMISSION_PASS assertions="+checks+" native=false");
  }
}
