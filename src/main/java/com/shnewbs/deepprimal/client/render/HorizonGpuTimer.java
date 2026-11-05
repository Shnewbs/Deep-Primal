package com.shnewbs.deepprimal.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL33C;

/** At most one asynchronous timestamp pair. No waits or overlapping elapsed queries. */
final class HorizonGpuTimer implements AutoCloseable {
    private int start, end, frames;
    private boolean pending, recording;
    double milliseconds;
    boolean available;
    void begin() {
        available = RenderSystem.getDevice().getBackendName().equalsIgnoreCase("OpenGL") && GL.getCapabilities().GL_ARB_timer_query;
        if (!available) return;
        if (pending) {
            if (GL15C.glGetQueryObjecti(end, GL15C.GL_QUERY_RESULT_AVAILABLE) == 0) return;
            long a=GL33C.glGetQueryObjecti64(start,GL15C.GL_QUERY_RESULT), b=GL33C.glGetQueryObjecti64(end,GL15C.GL_QUERY_RESULT);
            milliseconds=Math.max(0,b-a)/1e6; pending=false;
        }
        if (++frames % 30 != 0) return;
        if (start==0) { start=GL15C.glGenQueries(); end=GL15C.glGenQueries(); }
        GL33C.glQueryCounter(start,GL33C.GL_TIMESTAMP); recording=true;
    }
    void end() { if (recording) { GL33C.glQueryCounter(end,GL33C.GL_TIMESTAMP); recording=false; pending=true; } }
    @Override public void close() {
        if (start!=0) { GL15C.glDeleteQueries(start); GL15C.glDeleteQueries(end); }
        start=end=frames=0; pending=recording=available=false; milliseconds=0;
    }
}
