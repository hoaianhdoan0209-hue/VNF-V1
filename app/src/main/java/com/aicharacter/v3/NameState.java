package com.aicharacter.v3;
import org.json.JSONObject;
public final class NameState {
 public static final String DEVELOPER_CODENAME="Haru";
 public String officialName="", namingEventId=""; public long grantedAt=0L;
 public boolean isNamed(){return officialName!=null&&!officialName.trim().isEmpty();}
 public String playerFacing(){return isNamed()?officialName:"Cô gái";}
 public JSONObject toJson(){JSONObject j=new JSONObject();try{j.put("officialName",officialName);j.put("namingEventId",namingEventId);j.put("grantedAt",grantedAt);}catch(Exception ignored){}return j;}
 public static NameState fromJson(JSONObject j){NameState n=new NameState();if(j!=null){n.officialName=j.optString("officialName","");n.namingEventId=j.optString("namingEventId","");n.grantedAt=j.optLong("grantedAt",0);}return n;}
}