package com.aicharacter.v3;
/** Durable event bus. Events are emitted only after state transitions and deduplicated by stable event id. */
public final class WorldEventBus{private static final int MAX_HISTORY=180;private WorldEventBus(){}
 public static WorldHistoryEntry publish(WorldState s,long time,String type,String entity,String summary){String safeEntity=entity==null?"":entity,safeSummary=summary==null?"":summary;String id="evt_"+(type==null?"event":type.toLowerCase())+"_"+Long.toHexString(time)+"_"+Integer.toHexString((safeEntity+safeSummary).hashCode());return publishId(s,time,id,type,safeEntity,safeSummary);}
 public static WorldHistoryEntry publishId(WorldState s,long time,String id,String type,String entity,String summary){for(WorldHistoryEntry old:s.worldHistory)if(id!=null&&id.equals(old.eventId))return old;WorldHistoryEntry e=new WorldHistoryEntry(time,id==null?"evt_unknown_"+Long.toHexString(time):id,type==null?"EVENT":type,entity==null?"":entity,summary==null?"":summary);s.worldHistory.add(e);GodAttentionEngine.consume(s,e);trim(s);return e;}
 public static boolean has(WorldState s,String id){if(id==null)return false;for(WorldHistoryEntry e:s.worldHistory)if(id.equals(e.eventId))return true;return false;}
 public static boolean hasPrefix(WorldState s,String prefix){if(prefix==null||prefix.isEmpty())return false;for(WorldHistoryEntry e:s.worldHistory)if(e.eventId!=null&&e.eventId.startsWith(prefix))return true;return false;}
 private static void trim(WorldState s){while(s.worldHistory.size()>MAX_HISTORY)s.worldHistory.remove(0);}
}
