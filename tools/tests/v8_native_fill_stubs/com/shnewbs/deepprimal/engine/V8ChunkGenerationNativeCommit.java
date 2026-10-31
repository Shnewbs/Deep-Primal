package com.shnewbs.deepprimal.engine;
import com.shnewbs.deepprimal.core.density.V8ChunkGenerationStamp;
import net.minecraft.nbt.CompoundTag;
public final class V8ChunkGenerationNativeCommit {
  private V8ChunkGenerationNativeCommit(){}
  public static int begins,commits;
  public static boolean activeTicket;
  public interface Owner {boolean allowBegin();boolean allowCommit();}
  public record Ticket(Owner owner,V8ChunkGenerationStamp.Stamp intended){}
  public static Ticket begin(Owner owner,V8ChunkGenerationStamp.Stamp intended){
    begins++;if(!owner.allowBegin())throw new IllegalStateException("border rejected");
    activeTicket=true;
    return new Ticket(owner,intended);
  }
  public static V8ChunkGenerationNbtAdapter.Generated commit(Ticket ticket,Owner owner,CompoundTag nbt){
    commits++;activeTicket=false;
    if(ticket.owner()!=owner)throw new IllegalStateException("wrong owner");
    nbt.marker=true;
    if(!owner.allowCommit())throw new IllegalStateException("atomic publication rejected");
    return new V8ChunkGenerationNbtAdapter.Generated(ticket.intended(),42);
  }
}
