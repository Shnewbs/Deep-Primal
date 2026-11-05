#!/usr/bin/env python3
"""Render FSR 1 GLSL kernels against upstream AMD reference on headless EGL.

Linux-only optional GPU regression check; requires libEGL, libGL, and cpp.
Uses software Mesa when no hardware is available. This checks shader math and
GLSL 330 compatibility, not Minecraft hook wiring or real GPU performance.
"""
import ctypes as c
import argparse
import os
import json
from pathlib import Path
import struct
import math
import subprocess

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument("--output", type=Path, help="Optional JSON results file")
args = parser.parse_args()
os.environ.setdefault("MESA_SHADER_CACHE_DISABLE", "true")
ROOT = Path(__file__).resolve().parents[2]
SHADERS = ROOT / 'graphics-settings/src/main/resources/assets/deepprimal_graphics/shaders'
E = c.CDLL('libEGL.so.1')
G = c.CDLL('libGL.so.1')

def egl(name, ret, *args):
    fn=getattr(E,name);fn.restype=ret;fn.argtypes=list(args);return fn

getproc=egl('eglGetProcAddress',c.c_void_p,c.c_char_p)
getdisplay=c.CFUNCTYPE(c.c_void_p,c.c_uint,c.c_void_p,c.POINTER(c.c_int))(getproc(b'eglGetPlatformDisplayEXT'))
d=getdisplay(0x31DD,None,None)
major,minor=c.c_int(),c.c_int()
assert egl('eglInitialize',c.c_uint,c.c_void_p,c.POINTER(c.c_int),c.POINTER(c.c_int))(d,c.byref(major),c.byref(minor))
assert egl('eglBindAPI',c.c_uint,c.c_uint)(0x30A2)
attrs=(c.c_int*15)(0x3040,8,0x3033,1,0x3024,8,0x3023,8,0x3022,8,0x3021,8,0x3025,0,0x3038)
config=c.c_void_p();count=c.c_int()
assert egl('eglChooseConfig',c.c_uint,c.c_void_p,c.POINTER(c.c_int),c.POINTER(c.c_void_p),c.c_int,c.POINTER(c.c_int))(d,attrs,c.byref(config),1,c.byref(count)) and count.value
ctxattrs=(c.c_int*7)(0x3098,4,0x30FB,5,0x30FD,1,0x3038)
ctx=egl('eglCreateContext',c.c_void_p,c.c_void_p,c.c_void_p,c.c_void_p,c.POINTER(c.c_int))(d,config,None,ctxattrs)
assert ctx
assert egl('eglMakeCurrent',c.c_uint,c.c_void_p,c.c_void_p,c.c_void_p,c.c_void_p)(d,None,None,ctx)

def gl(name, ret, *args):
    fn=getattr(G,name);fn.restype=ret;fn.argtypes=list(args);return fn

u=c.c_uint;i=c.c_int;p=c.c_void_p
GetString=gl('glGetString',c.c_char_p,u)
print('GL',GetString(0x1F02).decode(),GetString(0x1F01).decode())
CreateShader=gl('glCreateShader',u,u)
ShaderSource=gl('glShaderSource',None,u,i,c.POINTER(c.c_char_p),c.POINTER(i))
CompileShader=gl('glCompileShader',None,u)
GetShaderiv=gl('glGetShaderiv',None,u,u,c.POINTER(i))
GetShaderInfoLog=gl('glGetShaderInfoLog',None,u,i,c.POINTER(i),p)
CreateProgram=gl('glCreateProgram',u)
AttachShader=gl('glAttachShader',None,u,u)
LinkProgram=gl('glLinkProgram',None,u)
GetProgramiv=gl('glGetProgramiv',None,u,u,c.POINTER(i))
GetProgramInfoLog=gl('glGetProgramInfoLog',None,u,i,c.POINTER(i),p)
UseProgram=gl('glUseProgram',None,u)
GetUniformLocation=gl('glGetUniformLocation',i,u,c.c_char_p)
Uniform1i=gl('glUniform1i',None,i,i)
GetUniformBlockIndex=gl('glGetUniformBlockIndex',u,u,c.c_char_p)
UniformBlockBinding=gl('glUniformBlockBinding',None,u,u,u)
GenTextures=gl('glGenTextures',None,i,c.POINTER(u))
BindTexture=gl('glBindTexture',None,u,u)
TexImage2D=gl('glTexImage2D',None,u,i,i,i,i,i,u,u,p)
TexParameteri=gl('glTexParameteri',None,u,u,i)
GenFramebuffers=gl('glGenFramebuffers',None,i,c.POINTER(u))
BindFramebuffer=gl('glBindFramebuffer',None,u,u)
FramebufferTexture2D=gl('glFramebufferTexture2D',None,u,u,u,u,i)
CheckFramebufferStatus=gl('glCheckFramebufferStatus',u,u)
GenBuffers=gl('glGenBuffers',None,i,c.POINTER(u))
BindBuffer=gl('glBindBuffer',None,u,u)
BufferData=gl('glBufferData',None,u,c.c_ssize_t,p,u)
BindBufferBase=gl('glBindBufferBase',None,u,u,u)
GenVertexArrays=gl('glGenVertexArrays',None,i,c.POINTER(u))
BindVertexArray=gl('glBindVertexArray',None,u)
Viewport=gl('glViewport',None,i,i,i,i)
DrawArrays=gl('glDrawArrays',None,u,i,i)
ReadPixels=gl('glReadPixels',None,i,i,i,i,u,u,p)
GetError=gl('glGetError',u)
DeleteTextures=gl('glDeleteTextures',None,i,c.POINTER(u))
DeleteBuffers=gl('glDeleteBuffers',None,i,c.POINTER(u))
Finish=gl('glFinish',None)

def shader(kind,source,label):
    sh=CreateShader(kind); b=source.encode();ptr=c.c_char_p(b);ShaderSource(sh,1,c.byref(ptr),None);CompileShader(sh)
    status=i();GetShaderiv(sh,0x8B81,c.byref(status))
    if not status.value:
        log=c.create_string_buffer(65536);GetShaderInfoLog(sh,len(log),None,log);raise AssertionError(label+': '+log.value.decode())
    return sh

def program(vertex,fragment,label):
    pr=CreateProgram();AttachShader(pr,shader(0x8B31,vertex,label+' vertex'));AttachShader(pr,shader(0x8B30,fragment,label+' fragment'));LinkProgram(pr)
    status=i();GetProgramiv(pr,0x8B82,c.byref(status))
    if not status.value:
        log=c.create_string_buffer(65536);GetProgramInfoLog(pr,len(log),None,log);raise AssertionError(label+': '+log.value.decode())
    return pr

def texture(width,height,data=None):
    t=u();GenTextures(1,c.byref(t));BindTexture(0x0DE1,t)
    for pname,val in ((0x2801,0x2600),(0x2800,0x2600),(0x2802,0x812F),(0x2803,0x812F)):
        TexParameteri(0x0DE1,pname,val)
    pixels=c.create_string_buffer(data) if data is not None else None
    TexImage2D(0x0DE1,0,0x8058,width,height,0,0x1908,0x1401,pixels)
    return t

fbo=u();GenFramebuffers(1,c.byref(fbo))
vao=u();GenVertexArrays(1,c.byref(vao));BindVertexArray(vao)

def render(pr,source,width,height,block,values):
    target=texture(width,height)
    BindFramebuffer(0x8D40,fbo);FramebufferTexture2D(0x8D40,0x8CE0,0x0DE1,target,0)
    assert CheckFramebufferStatus(0x8D40)==0x8CD5
    UseProgram(pr);Uniform1i(GetUniformLocation(pr,b'Source'),0);BindTexture(0x0DE1,source)
    uniform=u();GenBuffers(1,c.byref(uniform));BindBuffer(0x8A11,uniform)
    packed=struct.pack('<'+'f'*len(values),*values);buffer=c.create_string_buffer(packed)
    BufferData(0x8A11,len(packed),buffer,0x88E4);BindBufferBase(0x8A11,0,uniform)
    blockindex=GetUniformBlockIndex(pr,block.encode());assert blockindex!=0xffffffff,block
    UniformBlockBinding(pr,blockindex,0)
    Viewport(0,0,width,height);DrawArrays(4,0,3);Finish()
    out=c.create_string_buffer(width*height*4);ReadPixels(0,0,width,height,0x1908,0x1401,out)
    err=GetError();assert err==0,hex(err)
    DeleteBuffers(1,c.byref(uniform));return target,out.raw

vertex=(SHADERS/'core/fsr_fullscreen.vsh').read_text()
easu=program(vertex,(SHADERS/'core/fsr_easu.fsh').read_text(),'production EASU')
rcas=program(vertex,(SHADERS/'core/fsr_rcas.fsh').read_text(),'production RCAS')
src='#define A_GPU 1\n#define A_GLSL 1\n#define FSR_EASU_F 1\n#define FSR_RCAS_F 1\n#include "ffx_a.h"\n#include "ffx_fsr1.h"\n'
amd=subprocess.run(['cpp','-P','-undef','-x','c','-I',str(SHADERS/'upscaling/vendor')],input=src,text=True,capture_output=True,check=True).stdout
refprefix='''#version 430
uniform sampler2D Source;
out vec4 fragColor;
layout(std140) uniform Reference { vec4 Parameters; };
vec4 FsrEasuRF(vec2 p) { return textureGather(Source,p,0); }
vec4 FsrEasuGF(vec2 p) { return textureGather(Source,p,1); }
vec4 FsrEasuBF(vec2 p) { return textureGather(Source,p,2); }
vec4 FsrRcasLoadF(ivec2 p) { return texelFetch(Source,clamp(p,ivec2(0),textureSize(Source,0)-ivec2(1)),0); }
void FsrRcasInputF(inout float r,inout float g,inout float b) {}
'''
refeasu=program(vertex,refprefix+amd+'''
void main() {uvec4 a,b,c,d;vec3 color;FsrEasuCon(a,b,c,d,Parameters.x,Parameters.y,Parameters.x,Parameters.y,Parameters.z,Parameters.w);FsrEasuF(color,uvec2(gl_FragCoord.xy),a,b,c,d);fragColor=vec4(color,1.0);}
''','reference EASU')
refrcas=program(vertex,refprefix+amd+'''
void main() {uvec4 con;vec3 color;FsrRcasCon(con,-log2(Parameters.x));FsrRcasF(color.r,color.g,color.b,uvec2(gl_FragCoord.xy),con);fragColor=vec4(color,1.0);}
''','reference RCAS')

records=[]
for iw,ih,ow,oh in ((1,1,3,5),(7,5,13,9),(17,13,34,26),(31,19,41,29),(16,8,16,8)):
    for kind in ('black','white','constant','gradient','edges'):
        colors=[]
        for y in range(ih):
            for x in range(iw):
                color={'black':(0,0,0),'white':(255,255,255),'constant':(53,133,209),
                    'gradient':(int(x/max(1,iw-1)*255),int(y/max(1,ih-1)*255),(x*37+y*19)%256),
                    'edges':(255 if x>=iw//2 else 0,255 if y>=ih//2 else 0,255 if (x+y)%2 else 0)}[kind]
                colors.extend((*color,255))
        source=texture(iw,ih,bytes(colors))
        sx,sy,ix,iy=iw/ow,ih/oh,1/iw,1/ih
        values=(sx,sy,.5*sx-.5,.5*sy-.5,ix,iy,ix,-iy,-ix,2*iy,ix,2*iy,0,4*iy,0,0)
        actual,pixels=render(easu,source,ow,oh,'FsrEasu',values)
        expected,reference=render(refeasu,source,ow,oh,'Reference',(iw,ih,ow,oh))
        maximum=max(abs(a-b) for a,b in zip(pixels,reference));assert maximum<=1,(iw,ih,kind,'easu',maximum)
        assert all(pixels[o]==255 for o in range(3,len(pixels),4))
        if kind in ('black','white','constant'):
            assert max(abs(pixels[o+k]-colors[k]) for o in range(0,len(pixels),4) for k in range(3))<=1
        sharpness_changes=[]
        for sharpness in (.1,.5,1):
            ta,pa=render(rcas,actual,ow,oh,'FsrRcas',(sharpness,0,0,0))
            tr,pr=render(refrcas,actual,ow,oh,'Reference',(sharpness,0,0,0))
            delta=max(abs(a-b) for a,b in zip(pa,pr));assert delta<=1,(iw,ih,kind,'rcas',sharpness,delta)
            if kind in ('black','white','constant'):
                assert max(abs(pa[o+k]-colors[k]) for o in range(0,len(pa),4) for k in range(3))<=1,(kind,sharpness)
            sharpness_changes.append(sum(a!=b for a,b in zip(pa,pixels)))
            DeleteTextures(1,c.byref(ta));DeleteTextures(1,c.byref(tr))
        records.append({'input':[iw,ih],'output':[ow,oh],'pattern':kind,'easu_reference_max_error':maximum,'rcas_changed_channels':sharpness_changes})
        DeleteTextures(1,c.byref(source));DeleteTextures(1,c.byref(actual));DeleteTextures(1,c.byref(expected))
assert any(r['rcas_changed_channels'][2]>0 for r in records)
result={'pass':True,'backend':GetString(0x1F01).decode(),'gl':GetString(0x1F02).decode(),'patterns':len(records),'production_shader_draws':len(records)*4,'reference_shader_draws':len(records)*4,'records':records}
if args.output:
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(result,indent=2)+"\n")
print(json.dumps({k:v for k,v in result.items() if k!='records'}))
