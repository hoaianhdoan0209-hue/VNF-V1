package com.aicharacter.v3;
public final class VisualDiagnostics{private VisualDiagnostics(){}
 public static String animation(WorldState s){GirlAnimationController.Visual v=GirlAnimationController.select(s);return"simulation="+s.haruActivity+"\nvisual="+v.state+"\nasset="+v.asset+"\nfacing="+(v.state==GirlAnimationController.State.WALK_LEFT?"LEFT":"RIGHT")+"\nframes="+v.frames+" fps="+v.fps+"\nreason="+v.reason;}
 public static String camera(WorldState s){CatCameraDirector.Frame f=CatCameraDirector.direct(s);return"mode="+f.mode+"\ncatX="+(int)s.catX+"\ncameraX/Y="+(int)f.cameraX+"/"+(int)f.elevation+"\nlookTargetX="+(int)f.lookX+"\nreason="+f.reason;}
}
