package com.aicharacter.v3;
public final class NeedState { public double rest,safety,solitude,connection,curiosity;
 public static NeedState evaluate(WorldState s){NeedState n=new NeedState();n.rest=(100-s.body.energy)*.65+s.body.sleepiness*.55+s.body.pain*.5;n.safety=s.body.pain*.8+s.emotion.fear*45;n.solitude=s.relationship.irritation*.45+s.relationship.hurt*.55+s.emotion.anger*35;n.connection=s.relationship.attachment*.3+s.emotion.loneliness*60+Math.max(0,18-s.relationship.comfort);n.curiosity=s.emotion.curiosity*55+Math.max(0,55-s.body.sleepiness)*.3;return n;}
}
