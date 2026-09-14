package com.aicharacter.v3;
public final class Intention {public final String id;public double utility;public final String sourceNeed,targetId,expectedOutcome;public final double urgency;public final long expiryAt;
 public Intention(String id,double utility){this(id,utility,"","","",0,0);}public Intention(String id,double utility,String need,String target,String outcome,double urgency,long expiry){this.id=id;this.utility=utility;sourceNeed=need;targetId=target;expectedOutcome=outcome;this.urgency=urgency;expiryAt=expiry;}}
