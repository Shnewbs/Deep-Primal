from pathlib import Path
r=Path(__file__).resolve().parent.parent
p=r/'staged/engine-core/src/main/java/com/shnewbs/deepprimal/core/density/DensityGraph.java'
s=(r/"parent/engine-core/src/main/java/com/shnewbs/deepprimal/core/density/DensityGraph.java").read_text();s=s.replace('  private final int root;', '  private final int root;\n  private final int[] dependencies;')
s=s.replace('    build(root,new byte[index.size()],seed);','    build(root,new byte[index.size()],seed);\n    dependencies=new int[program.length];\n    Arrays.fill(dependencies,-1);\n    dependencies(root);')
pos=s.index('  private static double clamp(')
# Reuse exactly the scalar switch and arithmetic order; recursive calls route through the sampler.
a=s.index('    Program p=program[at];var n=p.node();int[] i=p.inputs();',s.index('  private double evaluate('));b=s.index('\n  private static double clamp(',a)
body=s[a:b];body=body[:body.rfind('\n  }')]
body=body.replace('evaluate(', 'cached(')
extra='''  private int dependencies(int at) {
    if(dependencies[at]>=0)return dependencies[at];
    Program p=program[at];int mask=switch(p.node().op()) {
      case CONSTANT -> 0; case X -> 1; case Y -> 2; case Z -> 4;
      case NOISE_2D -> 5; case NOISE_3D -> 7;
      default -> {int m=0;for(int input:p.inputs())m|=dependencies(input);yield m;}
    };
    return dependencies[at]=mask;
  }

  /** Bounded exact memo storage owned by one job/thread; never shared by graph readers. */
  public Sampler newSampler() { return new Sampler(); }

  public final class Sampler implements DensityField {
    private final double[] values=new double[program.length];
    private final long[] xs=new long[program.length],ys=new long[program.length],zs=new long[program.length];
    private final boolean[] valid=new boolean[program.length];
    private final Thread owner=Thread.currentThread();

    private void owned() {
      if(owner!=Thread.currentThread())throw new IllegalStateException("Density sampler changed job thread");
    }
    @Override public double sample(double x,double y,double z) {
      owned();coordinates(x,y,z);return cached(root,x,y,z);
    }
    public double channel(String id,double x,double y,double z) {
      owned();Integer at=ids.get(id);if(at==null)throw new IllegalArgumentException("unknown channel: "+id);
      coordinates(x,y,z);return cached(at,x,y,z);
    }
    private double cached(int at,double x,double y,double z) {
      int mask=dependencies[at];
      long xb=Double.doubleToLongBits(x),yb=Double.doubleToLongBits(y),zb=Double.doubleToLongBits(z);
      if(valid[at]&&((mask&1)==0||xs[at]==xb)&&((mask&2)==0||ys[at]==yb)&&((mask&4)==0||zs[at]==zb))return values[at];
      double value=compute(at,x,y,z);
      xs[at]=xb;ys[at]=yb;zs[at]=zb;values[at]=value;valid[at]=true;
      return value;
    }
    private double compute(int at,double x,double y,double z) {
BODY
    }
  }

'''.replace('BODY','\n'.join('  '+line for line in body.splitlines()))
s=s[:pos]+extra+s[pos:];p.write_text(s)
