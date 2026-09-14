package com.aicharacter.v3;
/** Durable event bus. Events are emitted only after state transitions and deduplicated by stable event id. */
public final class WorldEventBus{private WorldEventBus(){}
 public static WorldHistoryEntry publish(WorldState s,long time,String type,String entity,String summary){String id="evt_"+type.toLowerCase()+"_"+Long.toHexString(time)+"_"+Integer.toHexString((entity+summary).hashCode());return publishId(s,time,id,type,entity,summary);}
 public static WorldHistoryEntry publishId(WorldState s,long time,String id,String type,String entity,String summary){for(WorldHistoryEntry old:s.worldHistory)if(id.equals(old.eventId))return old;WorldHistoryEntry e=new WorldHistoryEntry(time,id,type,entity,summary);s.worldHistory.add(e);GodAttentionEngine.consume(s,e);return e;}
 public static boolean has(WorldState s,String id){for(WorldHistoryEntry e:s.worldHistory)if(id.equals(e.eventId))return true;return false;}
}
