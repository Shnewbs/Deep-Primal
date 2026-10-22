"""Compile the exact Startup source; isolate platform readiness/preferences with explicit doubles."""
from pathlib import Path
import subprocess,json,hashlib,tempfile
R=Path(__file__).resolve().parent
source=R/'project/renderer-vulkan/src/main/java/net/vulkanmod/deepprimal/Startup.java'
before=(R/'before-Startup.java').read_text() if (R/'before-Startup.java').exists() else source.read_text()
old='''    armed = true;''';new='''    // Every device initialization starts a new attempt, even after an earlier success.
    complete = false;
    armed = true;'''
assert before.count(old)==1
variants=[('before',before),('after',before.replace(old,new))]
stubs={
'org/slf4j/Logger.java':'''package org.slf4j;public class Logger{public void warn(String x,Object... o){}public void info(String x,Object... o){}}''',
'com/mojang/logging/LogUtils.java':'''package com.mojang.logging;public class LogUtils{public static org.slf4j.Logger getLogger(){return new org.slf4j.Logger();}}''',
'com/shnewbs/deepprimal/settings/RendererPreferences.java':'''package com.shnewbs.deepprimal.settings;import java.nio.file.*;public class RendererPreferences{public enum Backend{Vulkan,OpenGL} public static Path recoveryMarker(){return Path.of(System.getProperty("test.marker"));}public static Backend requestedBackend(){return Backend.valueOf(System.getProperty("test.backend","Vulkan"));}}''',
'com/shnewbs/deepprimal/settings/RendererOwnership.java':'''package com.shnewbs.deepprimal.settings;public class RendererOwnership{public static boolean owned(){return Boolean.parseBoolean(System.getProperty("test.owned","true"));}}''',
'net/minecraft/client/Minecraft.java':'''package net.minecraft.client;public class Minecraft{static Minecraft ONE=new Minecraft();public boolean loaded;public static Minecraft getInstance(){return ONE;}public boolean isGameLoadFinished(){return loaded;}}''',
'net/vulkanmod/deepprimal/StartupChecks.java':'''package net.vulkanmod.deepprimal;
import java.nio.file.*;import net.minecraft.client.Minecraft;public class StartupChecks{
 static void check(boolean v,String m){if(!v)throw new AssertionError(m);}
 static boolean complete()throws Exception{var f=Startup.class.getDeclaredField("complete");f.setAccessible(true);return f.getBoolean(null);}
 public static void main(String[]args)throws Exception{
  Path marker=Path.of(System.getProperty("test.marker"));String mode=args[0];
  if(!mode.equals("lifecycle")){check(Startup.enabled()==mode.equals("retry"),"Startup selection");check(Files.exists(marker),"Selection must not erase recovery evidence");System.out.println("STARTUP_SELECTION_PASS mode="+mode);return;}
  check(Startup.enabled(),"Requested Vulkan");Startup.arm();Startup.firstFrame();check(Files.exists(marker)&&!complete(),"Not ready retains marker");
  Minecraft.getInstance().loaded=true;Startup.firstFrame();check(!Files.exists(marker)&&complete(),"Successful initial frame");
  for(int i=0;i<32;i++){
   Startup.arm();boolean armedComplete=complete();Startup.firstFrame();System.out.println("STARTUP_REARM_OBSERVED turn="+i+" completeAfterArm="+armedComplete+" markerAfterFrame="+Files.exists(marker));
   check(!armedComplete&&!Files.exists(marker)&&complete(),"Rearm must reset completion and clear after first frame");}
  Startup.arm();Files.delete(marker);Files.createDirectory(marker);Path child=marker.resolve("blocked");Files.writeString(child,"keep");
  Startup.firstFrame();check(Files.exists(child)&&!complete(),"Delete failure retains incomplete state and evidence");Files.delete(child);Startup.firstFrame();check(!Files.exists(marker)&&complete(),"Later deletion retries");
  Startup.arm();Startup.fallback();check(!Files.exists(marker),"Fallback clears its armed marker");check(Startup.consumeWindowFallback()&&!Startup.consumeWindowFallback(),"Fallback window flag consumed once");
  System.out.println("STARTUP_LIFECYCLE_PASS cycles=32 readiness=true recoveryGuard=true deletionRetry=true fallback=true");
 }
}'''}
records=[];java=R/'toolchain/jdk-25.0.4.1+1/bin'
for variant,text in variants:
 d=R/('startup-extracted-'+variant);src=d/'src';src.mkdir(parents=True,exist_ok=True)
 for name,content in stubs.items():f=src/name;f.parent.mkdir(parents=True,exist_ok=True);f.write_text(content)
 f=src/'net/vulkanmod/deepprimal/Startup.java';f.write_text(text)
 compile=subprocess.run([str(java/'javac'),'-d',str(d/'classes')]+[str(f) for f in src.rglob('*.java')],capture_output=True,text=True);assert compile.returncode==0,compile.stderr
 marker_root=Path(tempfile.mkdtemp(prefix='startup-marker-'+variant+'-',dir=R))
 for mode,backend,owned,retry in [('lifecycle','Vulkan',True,False),('guard','Vulkan',True,False),('retry','Vulkan',True,True),('opengl','OpenGL',True,False),('conflict','Vulkan',False,True)]:
  mark=marker_root/(mode+'-marker');
  if mode!='lifecycle':mark.write_text('Vulkan startup in progress\n')
  result=subprocess.run([str(java/'java'),'-cp',str(d/'classes'),'-Dtest.marker='+str(mark.resolve()),'-Dtest.backend='+backend,'-Dtest.owned='+str(owned).lower(),'-Ddeepprimal.vulkan.retry='+str(retry).lower(),'net.vulkanmod.deepprimal.StartupChecks',mode],capture_output=True,text=True,timeout=15)
  expected=variant=='after' or mode!='lifecycle';assert (result.returncode==0)==expected,(variant,mode,result.stdout,result.stderr)
  if not expected:assert 'Rearm must reset completion' in result.stderr and 'completeAfterArm=true markerAfterFrame=true' in result.stdout,(result.stdout,result.stderr)
  records.append({'variant':variant,'mode':mode,'source_sha256':hashlib.sha256(text.encode()).hexdigest(),'exit_code':result.returncode,'stdout':result.stdout,'stderr':result.stderr,'scope':'Full exact Startup source; explicit platform readiness/preference/ownership doubles'})
(R/'startup-source-extracted.json').write_text(json.dumps(records,indent=2)+'\n');print(json.dumps(records,indent=2))
