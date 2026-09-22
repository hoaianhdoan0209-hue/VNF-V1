package com.aicharacter.v3;

/** Commits a bounded proposal contract; only the legacy atmospheric adapter touches an existing World-owned input. */
public final class GodWorldEventExecutor{
 private GodWorldEventExecutor(){}

 public static Result apply(WorldState s,GodWorldEventProposal p,long now){
  GodWorldConditionContract.Result r=GodWorldConditionContract.propose(s,p,now);if(!r.ok)return new Result(false,r.message,null);
  WorldHistoryEntry e=WorldEventBus.publishId(s,now,"god_world_"+p.id+"_"+Long.toHexString(now),"GOD_WORLD_CONDITION_PROPOSED",p.scopeTarget,
          "condition="+p.condition+" scope="+p.scopeType+":"+p.scopeTarget+" durationMin="+fmt(p.durationMinutes)+" intensity="+fmt(p.intensity)+" status=PROPOSED");
  // Backward-compatible bounded adapter. New condition types remain proposals for World/Physics to consume.
  if("ATMOSPHERE_PERTURBATION".equals(p.condition)){
   s.atmospherePerturbation=AtmospherePerturbation.fromProposal(p,now);
   GodWorldConditionContract.markApplied(s,p.id,"AtmosphereEvolutionEngine:legacy_adapter",now);
  }
  return new Result(true,"world condition proposal committed",e);
 }

 public static Result requestRollback(WorldState s,String proposalId,long now){
  if(!GodWorldConditionContract.requestRollback(s,proposalId,now))return new Result(false,"proposal is not in an applied state",null);
  WorldConditionProposalState p=GodWorldConditionContract.get(s,proposalId);
  if(p!=null&&"ATMOSPHERE_PERTURBATION".equals(p.condition)&&s.atmospherePerturbation!=null&&proposalId.equals(s.atmospherePerturbation.id)){
   s.atmospherePerturbation.active=false;
   GodWorldConditionContract.markRolledBack(s,proposalId,"AtmosphereEvolutionEngine:legacy_adapter",now);
  }
  WorldHistoryEntry e=WorldEventBus.publishId(s,now,"god_world_rollback_"+proposalId+"_"+Long.toHexString(now),"GOD_WORLD_CONDITION_ROLLBACK_REQUESTED","environment","A bounded world-condition rollback was requested.");
  return new Result(true,"rollback requested",e);
 }

 private static String fmt(double d){return String.format(java.util.Locale.US,"%.2f",d);}
 public static final class Result{public final boolean ok;public final String message;public final WorldHistoryEntry event;Result(boolean ok,String message,WorldHistoryEntry event){this.ok=ok;this.message=message;this.event=event;}}
}
