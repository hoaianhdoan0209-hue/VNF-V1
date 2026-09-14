package com.aicharacter.v3;
import org.json.JSONObject;
import java.util.UUID;
public final class RepairTransaction {public final String id="tx-"+UUID.randomUUID();public final String observationId;public final WorldIssue issue;public final String beforeJson;public String afterSummary="";public RepairTransaction(String obs,WorldIssue i,WorldState s)throws Exception{observationId=obs;issue=i;String old=s.lastCaretakerCheckpoint;s.lastCaretakerCheckpoint="";beforeJson=s.toJson().toString();s.lastCaretakerCheckpoint=old;}public WorldState restore()throws Exception{return WorldState.fromJson(new JSONObject(beforeJson));}}
