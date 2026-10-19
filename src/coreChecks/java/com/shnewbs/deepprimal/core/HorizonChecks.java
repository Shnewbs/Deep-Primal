package com.shnewbs.deepprimal.core;

public final class HorizonChecks {
    static void run() {
        int comparisons=0;
        if (HorizonGeometry.sample(new short[]{0,0,0,10},1,256,256)!=0) throw new AssertionError("Transition must match triangle plane, not bilinear saddle");
        for (int detail=0;detail<3;detail++) {
            int n=HorizonGeometry.cells(detail); short[] heights=new short[(n+1)*(n+1)]; int[] colors=new int[heights.length];
            for (int z=0;z<=n;z++) for (int x=0;x<=n;x++) { heights[z*(n+1)+x]=(short)(100+x*512/n/4+z*512/n/8); colors[z*(n+1)+x]=0xff778855; }
            for (int z=0;z<=n;z++) for (int x=0;x<=n;x++) {
                double actual=HorizonGeometry.sample(heights,n,x*512.0/n,z*512.0/n);
                if (actual!=heights[z*(n+1)+x]) throw new AssertionError("Interpolation at sample"); comparisons++;
            }
            var mesh=HorizonMesh.build(heights,colors,detail,-384);
            if (mesh.vertices()!=n*n*6+n*4*12+6) throw new AssertionError("Mesh count");
            if (mesh.minimum()!=-384 || mesh.maximum()!=292) throw new AssertionError("Bounds");
            for (int i=0;i<mesh.vertices();i++) {
                float x=mesh.positions()[i*3],y=mesh.positions()[i*3+1],z=mesh.positions()[i*3+2];
                if (!Float.isFinite(x+y+z) || x<0 || x>512 || z<0 || z>512 || y<mesh.minimum() || y>mesh.maximum()) throw new AssertionError("Invalid geometry");
            }
        }
        // Nonlinear height field: all resolutions share coarse boundary anchors.
        for (int fine=0;fine<3;fine++) for (int coarse=0;coarse<3;coarse++) {
            int a=HorizonGeometry.cells(fine),b=HorizonGeometry.cells(coarse); short[] left=new short[(a+1)*(a+1)],right=new short[(b+1)*(b+1)];
            for (int z=0;z<=a;z++) left[z*(a+1)+a]=(short)(100+Math.sin(z*512.0/a/100)*90);
            for (int z=0;z<=b;z++) right[z*(b+1)]=(short)(100+Math.sin(z*512.0/b/100)*90);
            for (int z=0;z<=8;z++) {
                double l=HorizonGeometry.stitched(left,a,a,z*a/8),r=HorizonGeometry.stitched(right,b,0,z*b/8);
                if (l!=r) throw new AssertionError("Shared boundary anchors"); comparisons++;
            }
        }
        ByteLru<Integer,byte[]> cache=new ByteLru<>(100,10,v->v.length);
        cache.put(1,new byte[60]);cache.put(2,new byte[60]);
        if (cache.get(1)!=null || cache.bytes()!=60) throw new AssertionError("Byte eviction");
        cache.put(2,new byte[20]); if (cache.bytes()!=20) throw new AssertionError("Replacement accounting");
        cache.put(3,new byte[101]); if (cache.size()!=1) throw new AssertionError("Oversized value retained");
        for (int i=0;i<10000;i++) { cache.put(i,new byte[11]); if (cache.bytes()>100 || cache.size()>10) throw new AssertionError("Byte budget"); }
        cache.clear(); if (cache.bytes()!=0 || cache.hits()!=0) throw new AssertionError("Clear");
        System.out.println("PASS: horizon snapshots, closed mesh bounds, "+comparisons+" interpolation/seam cases, byte-budget stress.");
    }
    private HorizonChecks() {}
}
