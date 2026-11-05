package com.shnewbs.deepprimal.core.lod;
import java.nio.file.*;
import java.util.*;
import java.io.*;
import java.security.*;
/** Actual temporary files and production GZIP IO; no filesystem/world doubles. */
public final class LodStoreBudgetChecks {
 static int checks;
 static void check(boolean b,String why){checks++;if(!b)throw new AssertionError(why);}
 static ColumnarChunk page(long revision,List<String> names){var b=new ColumnarChunk.Builder(-3,7,-384,1,revision);for(int i=0;i<256;i++)b.column(new int[]{0});return b.finish(names);}
 static String digest(Path p)throws Exception{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(p)));}
 static long files(Path p)throws Exception{if(!Files.exists(p))return 0;try(var s=Files.list(p)){return s.count();}}
 public static void main(String[]args)throws Exception{
  boolean bug=args.length>0&&args[0].equals("--expect-bug");Path root=Files.createTempDirectory("deepprimal-lod-budget-");
  try{
   var tooBig=Collections.nCopies(257,"x".repeat(4096));Path dir=root.resolve("replacement");var store=new LodStore(dir);var file=dir.resolve("-3_7.plod");
   check(store.write(page(7,List.of("minecraft:stone"))),"valid old snapshot writes");String old=digest(file);boolean returned=false,rejected=false;
   try{returned=store.write(page(8,tooBig));}catch(IOException expected){rejected=true;}
   boolean readable=true;try{store.read(-3,7);}catch(IOException expected){readable=false;}
   if(bug){check(returned&&!rejected&&!readable,"baseline accepts then rejects its own snapshot");check(!old.equals(digest(file)),"readable previous snapshot lost");System.out.println("LOD_BUDGET_BUG_REPRODUCED acceptedWrite=true readRejected=true previousSnapshotReplaced=true paletteChars=1052672 readLimit=1048576 checks="+checks);return;}
   check(rejected&&!returned&&readable,"oversized replacement rejected before corruption");check(old.equals(digest(file)),"old compressed bytes retained exactly");check(store.read(-3,7).revision==7,"old revision retained");check(files(dir)==1,"no pending file survives rejection");
   var freshDir=root.resolve("fresh");var fresh=new LodStore(freshDir);rejected=false;try{fresh.write(page(1,tooBig));}catch(IOException expected){rejected=true;}
   check(rejected&&!Files.exists(freshDir),"oversized first write creates no cache directory");
   for(String entry:List.of("a".repeat(4096),"\u0800".repeat(4096))){var boundary=Collections.nCopies(256,entry);check(store.write(page(8,boundary)),"exact aggregate limit accepted");var back=store.read(-3,7);check(back.palette().equals(boundary)&&back.revision==8,"ASCII/3-byte UTF exact limit roundtrip");check(back.state(15,-384,15).equals(entry),"vertical payload roundtrip");check(files(dir)==1,"atomic replacement leaves no temporary files");}
   String valid=digest(file);check(!store.write(page(7,List.of("older"))),"stale valid revision rejected");check(valid.equals(digest(file)),"stale write preserves newer bytes");
   for(var invalid:List.of(List.of("z".repeat(4097)),Collections.nCopies(257,"x".repeat(4096)))){rejected=false;try{store.write(page(9,invalid));}catch(IOException expected){rejected=true;}check(rejected&&valid.equals(digest(file)),"all invalid palettes preserve newer payload");}
   var reopened=new LodStore(dir);check(reopened.read(-3,7).revision==8,"independent store reopens exact accepted snapshot");
   System.out.println("LOD_BUDGET_FIXED_PASS checks="+checks+" aggregateLimit=1048576 exactLimitASCII=true exactLimitThreeByteUTF=true rejectedReplacementPreservesBytes=true freshRejectNoDirectory=true staleRevisionPreserved=true reopen=true");
  }finally{try(var walk=Files.walk(root)){for(var p:walk.sorted(Comparator.reverseOrder()).toList())Files.delete(p);}}
 }
}
