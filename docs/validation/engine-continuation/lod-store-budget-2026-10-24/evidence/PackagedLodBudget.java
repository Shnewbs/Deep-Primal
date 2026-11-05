package com.shnewbs.deepprimal.core.lod;
import java.security.*;
import java.util.*;
public final class PackagedLodBudget {
 public static void main(String[] args)throws Exception{
  String sha;
  try(var in=LodStore.class.getResourceAsStream("LodStore.class")){sha=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(in.readAllBytes()));}
  if(!sha.equals(args[0]))throw new AssertionError("Loaded production class mismatch "+sha);
  System.out.println("PACKAGED_LOD_IDENTITY_PASS class_sha256="+sha+" origin="+LodStore.class.getProtectionDomain().getCodeSource().getLocation());
  LodStoreBudgetChecks.main(new String[0]);
 }
}
