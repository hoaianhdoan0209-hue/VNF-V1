package com.aicharacter.v3;
public final class WorldIssue {
 public final String id,severity,domain,entityId,description,repairType,dependency; public final int impact; public final boolean safeRepair; public final double confidence;
 public WorldIssue(String i,String s,String d,String e,String x,String r){this(i,s,d,e,x,r,"",50,!"DEVELOPER_REPORT".equals(r),1.0);}
 public WorldIssue(String i,String s,String d,String e,String x,String r,String dep,int impact,boolean safe,double conf){id=i;severity=s;domain=d;entityId=e;description=x;repairType=r;dependency=dep;this.impact=impact;safeRepair=safe;confidence=conf;}
 public int priorityScore(){int sev="CRITICAL".equals(severity)?400:"HIGH".equals(severity)?300:"MEDIUM".equals(severity)?200:100;return sev+impact+(safeRepair?10:0)-(dependency.isEmpty()?0:5);}
 public String key(){return id+"@"+entityId;} public String toString(){return severity+" "+id+" ["+entityId+"]: "+description;}
}
