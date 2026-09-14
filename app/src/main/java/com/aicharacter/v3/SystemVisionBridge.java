package com.aicharacter.v3;
/** Future backend contract. V0.3 provides no remote vision implementation. */
public interface SystemVisionBridge{
 final class Input{public final byte[] pngFrame;public final String structuredSnapshot;public Input(byte[] p,String s){pngFrame=p;structuredSnapshot=s;}}
 final class VisualIssue{public final String severity,entityOrRegion,suggestion;public final double confidence;public VisualIssue(String s,String e,String g,double c){severity=s;entityOrRegion=e;suggestion=g;confidence=c;}}
 final class Critique{public final String summary;public final java.util.List<VisualIssue> issues;public Critique(String s,java.util.List<VisualIssue> i){summary=s;issues=i;}}
 interface Callback{void onCritique(Critique critique);void onError(String reason);}boolean isAvailable();void evaluate(Input input,Callback callback);
}
