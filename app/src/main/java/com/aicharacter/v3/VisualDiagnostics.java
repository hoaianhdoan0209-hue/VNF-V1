package com.aicharacter.v3;
public final class VisualDiagnostics{private VisualDiagnostics(){}
 public static String animation(WorldState s){GirlAnimationController.Visual v=GirlAnimationController.select(s);return"simulation="+s.haruActivity+"\nvisual="+v.state+"\nasset="+v.asset+"\nfacing="+(v.state==GirlAnimationController.State.WALK_LEFT?"LEFT":"RIGHT")+"\nframes="+v.frames+" fps="+v.fps+"\nreason="+v.reason;}
 public static String camera(WorldState s){CatCameraDirector.Frame f=CatCameraDirector.direct(s);double clear=s.girlPhysics==null?0:s.girlPhysics.groundClearanceM;return"mode="+f.mode+"\ncatX="+(int)s.catX+" haruX="+(int)s.haruX+"\ngirlClearanceM="+String.format(java.util.Locale.US,"%.2f",clear)+" girlLiftPx="+(int)f.girlScreenLiftPx+"\ncameraX/Y="+(int)f.cameraX+"/"+(int)f.elevation+" airFollowPx="+(int)f.airFollowPx+"\nlookTarget="+(int)f.lookX+","+(int)f.lookY+"\nreason="+f.reason;}
}
