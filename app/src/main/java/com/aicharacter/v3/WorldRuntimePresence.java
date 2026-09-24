package com.aicharacter.v3;

import java.util.concurrent.atomic.AtomicBoolean;

/** Process-local hint so background workers never race the visible world. */
public final class WorldRuntimePresence {
 private static final AtomicBoolean FOREGROUND=new AtomicBoolean(false);
 private WorldRuntimePresence(){}
 public static void setForeground(boolean value){FOREGROUND.set(value);}
 public static boolean isForeground(){return FOREGROUND.get();}
}
