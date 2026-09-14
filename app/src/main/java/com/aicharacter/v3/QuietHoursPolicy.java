package com.aicharacter.v3;import java.util.Calendar;
public final class QuietHoursPolicy{private QuietHoursPolicy(){}public static boolean isQuiet(WorldState s,long time){Calendar c=Calendar.getInstance();c.setTimeInMillis(time);int h=c.get(Calendar.HOUR_OF_DAY);if(s.quietStartHour>s.quietEndHour)return h>=s.quietStartHour||h<s.quietEndHour;return h>=s.quietStartHour&&h<s.quietEndHour;}}
