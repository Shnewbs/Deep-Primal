package com.shnewbs.deepprimal.core;

/** Alpha 7 reference topology for differential regression checks only. */
public record LegacyHorizonMesh(float[] positions, int[] colors, short[] heights, int minimum, int maximum) {
    public int vertices() { return colors.length; }
    public static LegacyHorizonMesh build(short[] source, int[] sourceColors, int detail, int floor) {
        int cells=HorizonGeometry.cells(detail), width=cells+1;
        if (source.length!=width*width || sourceColors.length!=source.length) throw new IllegalArgumentException("Invalid horizon samples");
        short[] heights=source.clone(); int high=floor;
        for (int z=0;z<=cells;z++) for (int x=0;x<=cells;x++) {
            heights[z*width+x]=(short)Math.round(HorizonGeometry.stitched(source,cells,x,z));
            high=Math.max(high,heights[z*width+x]);
        }
        Builder b=new Builder(cells*cells*6+cells*4*12+6);
        float step=512f/cells;
        for (int z=0;z<cells;z++) {
            if (Thread.currentThread().isInterrupted()) throw new java.util.concurrent.CancellationException();
            for (int x=0;x<cells;x++) {
                int a=z*width+x,c=a+1,d=a+width+1,e=a+width;
                for (int i : new int[]{a,e,c,c,e,d}) b.vertex(i%width*step,heights[i],i/width*step,sourceColors[i]);
            }
        }
        for (int i=0;i<cells;i++) {
            b.wall(i*step,0,(i+1)*step,0,heights[i],heights[i+1],floor,sourceColors[i]);
            int a=cells*width+i;
            b.wall(i*step,512,(i+1)*step,512,heights[a],heights[a+1],floor,sourceColors[a]);
            a=i*width;
            b.wall(0,i*step,0,(i+1)*step,heights[a],heights[a+width],floor,sourceColors[a]);
            a+=cells;
            b.wall(512,i*step,512,(i+1)*step,heights[a],heights[a+width],floor,sourceColors[a]);
        }
        // Downward-facing bottom completes the shell for below-world spectator views.
        for (int i : new int[]{0,1,2,2,1,3}) b.vertex((i%2)*512,floor,(i/2)*512,0xff565d55);
        return new LegacyHorizonMesh(b.positions,b.colors,heights,floor,high);
    }
    private static final class Builder {
        final float[] positions; final int[] colors; int next;
        Builder(int vertices) { positions=new float[vertices*3]; colors=new int[vertices]; }
        void vertex(float x,float y,float z,int color) { int p=next*3; positions[p]=x; positions[p+1]=y; positions[p+2]=z; colors[next++]=color; }
        void wall(float x,float z,float nx,float nz,float y,float ny,float floor,int color) {
            vertex(x,y,z,color); vertex(nx,ny,nz,color); vertex(x,floor,z,color);
            vertex(x,floor,z,color); vertex(nx,ny,nz,color); vertex(nx,floor,nz,color);
            vertex(x,floor,z,color); vertex(nx,ny,nz,color); vertex(x,y,z,color);
            vertex(nx,floor,nz,color); vertex(nx,ny,nz,color); vertex(x,floor,z,color);
        }
    }
}
