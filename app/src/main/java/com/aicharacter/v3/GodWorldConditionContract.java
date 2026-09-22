package com.aicharacter.v3;

import java.util.*;

/** Durable proposal/consumer contract. God proposes; World/Physics decides how/if to realize it. */
public final class GodWorldConditionContract {
 private static final Set<String> CONDITIONS=new LinkedHashSet<>(Arrays.asList("WEATHER","WIND","HUMIDITY_MIST","TEMPERATURE","LIGHT","ATMOSPHERE_PERTURBATION"));
 private GodWorldConditionContract(){}

 public static Result propose(WorldState s,GodWorldEventProposal p,long now){
  if(s==null||p==null)return new Result(false,"missing proposal",null);
  GodWorldEventProposal.Result valid=GodWorldEventProposal.validate(s,p,now);if(!valid.ok)return new Result(false,valid.reason,null);
  ensure(s);if(s.characterGod.worldConditions.containsKey(p.id))return new Result(false,"proposal already exists",s.characterGod.worldConditions.get(p.id));
  if(!CONDITIONS.contains(p.condition))return new Result(false,"unsupported condition",null);
  WorldConditionProposalState x=new WorldConditionProposalState();x.id=p.id;x.condition=p.condition;x.desiredValue=p.desiredValue;x.scopeType=p.scopeType;x.scopeTarget=p.scopeTarget;x.intensity=p.intensity;x.durationMinutes=p.durationMinutes;x.provenanceLayer=p.provenanceLayer;x.sourceRef=p.sourceRef;x.sourceConfidence=p.sourceConfidence;x.rollbackPolicy=p.rollbackPolicy;x.proposedAt=now;x.status="PROPOSED";
  s.characterGod.worldConditions.put(x.id,x);s.committedGodEventIds.add(x.id);
  return new Result(true,"world condition proposed",x);
 }
 public static boolean markApplied(WorldState s,String id,String consumerToken,long now){WorldConditionProposalState p=get(s,id);if(p==null||!"PROPOSED".equals(p.status))return false;p.status="APPLIED";p.consumerToken=consumerToken==null?"":consumerToken;p.appliedAt=now;return true;}
 public static boolean requestRollback(WorldState s,String id,long now){WorldConditionProposalState p=get(s,id);if(p==null||!"APPLIED".equals(p.status))return false;p.status="ROLLBACK_REQUESTED";p.rollbackRequestedAt=now;return true;}
 public static boolean markRolledBack(WorldState s,String id,String consumerToken,long now){WorldConditionProposalState p=get(s,id);if(p==null||!"ROLLBACK_REQUESTED".equals(p.status))return false;if(p.consumerToken!=null&&!p.consumerToken.isEmpty()&&consumerToken!=null&&!consumerToken.isEmpty()&&!p.consumerToken.equals(consumerToken))return false;p.status="ROLLED_BACK";p.rolledBackAt=now;return true;}
 public static WorldConditionProposalState get(WorldState s,String id){ensure(s);return id==null?null:s.characterGod.worldConditions.get(id);}
 private static void ensure(WorldState s){if(s.characterGod==null)s.characterGod=new CharacterGodState();}
 public static final class Result{public final boolean ok;public final String message;public final WorldConditionProposalState proposal;Result(boolean ok,String message,WorldConditionProposalState p){this.ok=ok;this.message=message;this.proposal=p;}}
}
