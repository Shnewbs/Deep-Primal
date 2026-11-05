package com.shnewbs.deepprimal.core.density;
import com.shnewbs.deepprimal.core.density.*;
import java.util.*;
import static com.shnewbs.deepprimal.core.density.DensityGraph.*;

/** Scalar oracle plus reordered definitions, high node indices and changing sample order. */
public final class AdversarialChecks {
  static long checks;static int accepted,rejected;
  static void same(double a,double b) {checks++;if(Double.doubleToRawLongBits(a)!=Double.doubleToRawLongBits(b))throw new AssertionError(a+" != "+b);}
  static final class Builder {
    final List<Node> nodes=new ArrayList<>();final Random random;
    Builder(long seed){random=new Random(seed);}
    String id(){return "n"+nodes.size();}
    String leaf(){String id=id();switch(random.nextInt(6)) {
      case 0 -> nodes.add(Node.constant(id,new double[]{-0.,0.,-1.,1.,Double.MIN_VALUE,-Double.MIN_VALUE,.25}[random.nextInt(7)]));
      case 1,2 -> nodes.add(Node.noise(id,new NoiseSpec(.01,.02,.03,2,2,.5,random.nextLong()),random.nextBoolean()));
      default -> {nodes.add(Node.operation(id,new Op[]{Op.X,Op.Y,Op.Z}[random.nextInt(3)]));String bounded=id();nodes.add(Node.bounded(bounded,Op.CLAMP,id,-1,1));return bounded;}
    }return id;}
    Definition definition(){String current=leaf();for(int j=0;j<7;j++) {
      int op=random.nextInt(9);String a=current,b=null,c=null;
      if(op>=3&&op<8)b=random.nextInt(4)==0?current:leaf();
      if(op==7)c=leaf();
      if(op==8){b=leaf();c=leaf();String d=leaf();String id=id();nodes.add(new Node(id,Op.WARP,List.of(current,b,c,d),List.of(random.nextBoolean()?64.:65536.),null));current=id;continue;}
      String id=id();switch(op) {
        case 0 -> nodes.add(Node.operation(id,Op.ABS,a));
        case 1 -> nodes.add(Node.bounded(id,Op.CLAMP,a,-1,1));
        case 2 -> nodes.add(Node.bounded(id,Op.SMOOTHSTEP,a,-1,1));
        case 3,4,5,6 -> nodes.add(Node.operation(id,new Op[]{Op.ADD,Op.MULTIPLY,Op.MIN,Op.MAX}[op-3],a,b));
        case 7 -> nodes.add(Node.operation(id,Op.LERP,a,b,c));
      }current=id;
      // Keep displacement/range analysis valid without changing the ordered inner arithmetic.
      String bounded=id();nodes.add(Node.bounded(bounded,Op.CLAMP,current,-1,1));current=bounded;
    }return new Definition(1,current,nodes);}
  }
  public static void main(String[] args)throws Exception {
    for(int i=0;i<180;i++) {
      Definition definition=new Builder(628831L+i).definition();DensityGraph graph;
      try{graph=definition.compile(i-90);}catch(IllegalArgumentException e){if(!e.getMessage().contains("budget"))throw e;rejected++;continue;}
      accepted++;var shuffled=new ArrayList<>(definition.nodes());Collections.shuffle(shuffled,new Random(i));
      var reordered=new Definition(1,definition.root(),shuffled).compile(i-90);var a=graph.newSampler();var b=reordered.newSampler();
      var r=new Random(i*41L);var points=new double[64][3];
      for(int p=0;p<64;p++)points[p]=new double[]{(p%4==0?-0.:r.nextInt(100)-50),p%7==0?Double.MIN_VALUE:r.nextInt(768)-128,p%5==0?29_999_999.25:r.nextInt(100)-50};
      for(int order=0;order<3;order++)for(int p=0;p<64;p++) {
        int at=order==0?p:order==1?63-p:(p*17)%64;double[] v=points[at];double oracle=graph.sample(v[0],v[1],v[2]);
        same(oracle,a.sample(v[0],v[1],v[2]));same(oracle,b.sample(v[0],v[1],v[2]));
        String channel=definition.nodes().get(p%definition.nodes().size()).id();same(graph.channel(channel,v[0],v[1],v[2]),a.channel(channel,v[0],v[1],v[2]));
      }
    }
    if(accepted<120)throw new AssertionError("insufficient accepted graph corpus: "+accepted);
    for(boolean fourth:new boolean[]{false,true}) {
      var nodes=new ArrayList<Node>();nodes.add(Node.constant("mask",-0.));
      nodes.add(Node.noise("noise",NoiseSpec.DEFAULT,false));nodes.add(Node.operation("y",Op.Y));
      nodes.add(Node.bounded("f2",Op.CLAMP,"y",.125,.75));nodes.add(Node.operation("f3",Op.ABS,"f2"));nodes.add(Node.operation("f4",Op.ADD,"f2","f3"));
      nodes.add(Node.operation("p1",Op.MULTIPLY,"noise","mask"));nodes.add(Node.operation("p2",Op.MULTIPLY,"p1","f2"));
      nodes.add(Node.operation("p3",Op.MULTIPLY,"p2","f3"));nodes.add(Node.operation("product",Op.MULTIPLY,"p3","f4"));
      // A balanced reachable left tree fills the full 128-node budget without deep recursion.
      var layer=new ArrayList<String>();for(int j=0;j<57;j++){String id="c"+j;nodes.add(Node.constant(id,j%2==0?1:-.25));layer.add(id);}
      int seq=0;while(layer.size()>1){var next=new ArrayList<String>();for(int j=0;j<layer.size();j+=2){if(j+1==layer.size())next.add(layer.get(j));else{String id="s"+seq++;nodes.add(Node.operation(id,Op.ADD,layer.get(j),layer.get(j+1)));next.add(id);}}layer=next;}
      String left=layer.getFirst();while(nodes.size()<127){String id="a"+nodes.size();nodes.add(Node.operation(id,Op.ABS,left));left=id;}
      nodes.add(Node.operation("root",Op.ADD,left,"product"));
      // Put a selected factor at index127, including the fourth packed byte with its sign bit set.
      String target=fourth?"mask":"f4";Node tail=nodes.stream().filter(n->n.id().equals(target)).findFirst().orElseThrow();nodes.remove(tail);nodes.add(tail);
      if(nodes.size()!=128)throw new AssertionError("full node corpus");var graph=new Definition(1,"root",nodes).compile(62);var sampler=graph.newSampler();
      for(int j=-128;j<768;j++)same(graph.sample(j,j,3),sampler.sample(j,j,3));
    }
    System.out.println("DENSITY_ADVERSARIAL_PASS checks="+checks+" acceptedGraphs="+accepted+" rejectedByBudget="+rejected+" permutations=true orders=3 full128Nodes=true highPackedByte=true");
  }
}
