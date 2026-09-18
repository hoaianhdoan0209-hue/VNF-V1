package com.aicharacter.v3;
import org.json.JSONObject;
/** Stable System-Reality entity; authored technical metadata stays outside Haru cognition. */
public final class WorldObject {
 public String id,type,areaId,assetRef,haruDescription,tags,renderLayer,habitat,safeEditable,state;public float x,y,width,height,interactionX,interactionRadius;public boolean enabled=true,interactable,collision;
 public WorldObject(String id,String type,String area,String asset,String haru,float x,float y,float w,float h,String tags){this.id=id;this.type=type;areaId=area;assetRef=asset;haruDescription=haru;this.x=x;this.y=y;this.width=w;this.height=h;this.interactionX=x;this.interactionRadius=Math.max(28,Math.min(72,w*.35f+20));this.tags=tags;state="normal";renderLayer="WORLD_PROPS";habitat="";safeEditable="position,enabled";}
 public float collisionLeft(){return x-Math.max(0,width)*.5f;} public float collisionRight(){return x+Math.max(0,width)*.5f;}
 public static WorldObject fromJson(JSONObject j){WorldObject o=new WorldObject(j.optString("id"),j.optString("type"),j.optString("areaId"),j.optString("assetRef"),j.optString("haruDescription",j.optString("perceptionName")),(float)j.optDouble("x"),(float)j.optDouble("ground",j.optDouble("y",846)),(float)j.optDouble("width"),(float)j.optDouble("height"),j.optString("tags"));o.enabled=j.optBoolean("enabled",true);o.interactable=j.optBoolean("interactable",false);o.collision=j.optBoolean("collision",false);o.renderLayer=j.optString("renderLayer","WORLD_PROPS");o.habitat=j.optString("habitat","");o.safeEditable=j.optString("safeEditable","position,enabled");o.state=j.optString("state","normal");o.interactionX=(float)j.optDouble("interactionX",o.x);o.interactionRadius=(float)j.optDouble("interactionRadius",o.interactionRadius);return o;}
}
