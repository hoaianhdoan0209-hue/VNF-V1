package com.aicharacter.v3;
import java.util.UUID;
public final class IdFactory { private IdFactory(){} public static String next(String prefix){return prefix+"_"+UUID.randomUUID().toString();} }
