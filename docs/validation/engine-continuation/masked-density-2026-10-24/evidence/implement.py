from pathlib import Path
r=Path(__file__).resolve().parent.parent
p=r/'staged/engine-core/src/main/java/com/shnewbs/deepprimal/core/density/DensityGraph.java';s=(r/'evidence/before-DensityGraph.java').read_text()
s=s.replace('  private final int[] dependencies;','  private final int[] dependencies;\n  private final boolean[] has3D;\n  private final int[][] zeroFactors;')
s=s.replace('    dependencies(root);','''    dependencies(root);
    has3D=new boolean[program.length];
    for(int i=0;i<program.length;i++)has3D[i]=contains3D(i);
    zeroFactors=new int[program.length][];
    for(int i=0;i<program.length;i++) {
      var factors=new LinkedHashSet<Integer>();
      if(program[i].node().op()==Op.ADD&&has3D[program[i].inputs()[1]])collectFactors(program[i].inputs()[1],factors);
      zeroFactors[i]=factors.stream().mapToInt(Integer::intValue).toArray();
    }''')
check='    for (int i=0;i<program.length;i++) if(program[i]==null) throw new IllegalArgumentException("unreachable node: "+definition.nodes().get(i).id());'
s=s.replace(check+'\n','')
s=s.replace('    build(root,new byte[index.size()],seed);','    build(root,new byte[index.size()],seed);\n'+check)
pos=s.index('  /** Bounded exact memo storage')
s=s[:pos]+'''  private boolean contains3D(int at) {
    if(program[at].node().op()==Op.NOISE_3D)return true;
    for(int child:program[at].inputs())if(contains3D(child))return true;
    return false;
  }
  private void collectFactors(int at,Set<Integer> factors) {
    if(factors.size()>=4)return;
    Program p=program[at];
    if(p.node().op()==Op.MULTIPLY) {
      collectFactors(p.inputs()[1],factors);collectFactors(p.inputs()[0],factors);
    } else if(!has3D[at]&&!(p.node().op()==Op.CONSTANT&&p.node().parameters().getFirst()!=0))factors.add(at);
  }

'''+s[pos:]
s=s.replace('''        case ADD -> cached(i[0],x,y,z)+cached(i[1],x,y,z);''','''        case ADD -> {
          double a=cached(i[0],x,y,z);boolean zero=false;
          // A finite nonzero addend is bit-identical after adding either sign of zero.
          // If it is zero, retain original evaluation to preserve signed-zero output.
          if(a!=0)for(int factor:zeroFactors[at])if(cached(factor,x,y,z)==0){zero=true;break;}
          yield zero?a:a+cached(i[1],x,y,z);
        }''')
p.write_text(s)
