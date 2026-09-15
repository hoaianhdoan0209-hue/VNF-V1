package com.aicharacter.v3;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.LinkedHashMap;
import java.util.Map;

public final class WorldState {
    public static final int SAVE_VERSION = 17;
    public long createdAt;
    public long lastSavedAt;
    public long lastOpenedAt;
    public long lastSimulatedAt;
    public double worldMinutes;
    public float catX;
    public float haruX;
    public String acceptedName;
    public int age;
    public double brainGrowth;
    public boolean pendingBirthdayLearning;
    // Growth is persistent: God may teach, but never directly rewrites personality or decisions.
    public final Map<String,Double> skills = new LinkedHashMap<>();
    public final java.util.Set<String> learnedActions = new java.util.LinkedHashSet<>();
    public long lastDevelopmentAt=0L;
    public boolean proactiveEnabled;
    public int quietStartHour;
    public int quietEndHour;
    public String haruMood;
    public String haruActivity;
    public BodyState body = new BodyState();
    public RelationshipState relationship;
    public final List<MemoryEntry> memories = new ArrayList<>();
    public final List<DiaryEntry> diary = new ArrayList<>();
    public final Map<String,Integer> knowledge = new LinkedHashMap<>();
    public final Map<String,Double> beliefs = new LinkedHashMap<>();
    public final Map<String,BeliefState> beliefStates = new LinkedHashMap<>();
    public EmotionState emotion = new EmotionState();
    public String currentIntention="observe_lake";
    public long lastProactiveAt=0L;
    public EnvironmentState environment=new EnvironmentState();
    public transient WorldModel world;
    public WorldRuntimeState runtime=new WorldRuntimeState();
    public final List<WorldHistoryEntry> worldHistory=new ArrayList<>();
    public final List<CaretakerLogEntry> caretakerLog=new ArrayList<>();
    public final List<DeveloperReport> developerReports=new ArrayList<>();
    public String lastCaretakerCheckpoint="";
    public transient boolean devForceRepairRegression=false;
    public final Map<String,PreferenceState> preferences=new LinkedHashMap<>();
    public final Map<String,HabitState> habits=new LinkedHashMap<>();
    public final Map<String,PlaceAssociation> placeAssociations=new LinkedHashMap<>();
    public MoodState mood=new MoodState();
    public PersonalityState personality=new PersonalityState();
    public final List<ThoughtState> thoughts=new ArrayList<>();
    public CreatureState reedling=new CreatureState();
    public long lastCatSeenAt=0L;
    public long lastAbsenceMinutes=0L;
    public String reunionContext="";
    public String persistentIntentionTarget="";
    public long intentionStartedAt=0L;
    public transient String lastOfflineTrace="";
    // V0.6: player-facing identity is separate from the developer codename and legacy acceptedName.
    public NameState nameState=new NameState();
    public CatState catState=new CatState();
    public GirlCatSearchEngine.SearchState catSearch=new GirlCatSearchEngine.SearchState();
    public final List<NotificationEventQueue.Event> notificationEvents=new ArrayList<>();
    // V0.7 persistent plans/travel/God inbox. Defaults migrate V0.6 without resetting history.
    public TravelState girlTravel=new TravelState();
    public TravelState catTravel=new TravelState();
    public PlanState planState=new PlanState();
    public RouteRuntimeState routeRuntime=new RouteRuntimeState();
    public transient String lastPerceptionTrace="", lastTravelTraceV2="";
    public final List<GodMessage> godInbox=new ArrayList<>();
    // Separate persistent memory for God/System conversations. Never merged into girl memory.
    public GodMemory godMemory=new GodMemory();
    public final java.util.Set<String> processedLearningIds=new java.util.LinkedHashSet<>();
    public double worldWetness=0, visibility=1.0;
    public transient String lastGodTrace="",cameraTrace="",lastDecisionTrace="";

    public static WorldState fresh() {
        long now=System.currentTimeMillis(); Random r=new Random(now);
        WorldState s=new WorldState(); s.createdAt=now; s.lastSavedAt=now; s.lastOpenedAt=now; s.lastSimulatedAt=now;
        s.worldMinutes=8*60+15; s.catX=320; s.haruX=960; s.acceptedName=""; s.age=15; s.brainGrowth=5.0;
        s.pendingBirthdayLearning=false; s.proactiveEnabled=true; s.quietStartHour=23; s.quietEndHour=7;
        s.environment.weather="CLEAR"; s.environment.weatherSince=now; s.catState=CatState.fromJson(null,s.catX,now); s.catState.areaId="home_shelter"; s.haruMood="curious"; s.haruActivity="watching the lake"; s.relationship=RelationshipState.fresh(r);
        s.memories.add(new MemoryEntry(now,"origin","A quiet morning when a talking cat entered her life.",0.95));
        s.knowledge.put("lake", 2); // 0 unknown, 1 heard of, 2 knows a little, 3 understands fairly well, 4 deep understanding
        s.knowledge.put("talking_cat", 1);
        s.beliefs.put("the_cat_may_be_a_friend", 0.42);
        s.beliefStates.put("cat_intent",new BeliefState("cat_intent","uncertain",0.42)); s.emotion.curiosity=.62; s.lastCatSeenAt=now;
        return s;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject j=new JSONObject(); j.put("saveVersion",SAVE_VERSION); j.put("createdAt",createdAt); j.put("lastSavedAt",lastSavedAt);
        j.put("lastOpenedAt",lastOpenedAt); j.put("lastSimulatedAt",lastSimulatedAt); j.put("worldMinutes",worldMinutes); j.put("catX",catX); j.put("haruX",haruX);
        j.put("acceptedName",acceptedName); j.put("age",age); j.put("brainGrowth",brainGrowth); j.put("pendingBirthdayLearning",pendingBirthdayLearning); j.put("lastDevelopmentAt",lastDevelopmentAt); JSONObject sk=new JSONObject(); for(Map.Entry<String,Double> e:skills.entrySet())sk.put(e.getKey(),e.getValue()); j.put("skills",sk); JSONArray la=new JSONArray(); for(String a:learnedActions)la.put(a); j.put("learnedActions",la);
        j.put("proactiveEnabled",proactiveEnabled); j.put("quietStartHour",quietStartHour); j.put("quietEndHour",quietEndHour);
        j.put("haruMood",haruMood); j.put("haruActivity",haruActivity); j.put("body",body.toJson()); j.put("relationship",relationship.toJson());
        JSONArray m=new JSONArray(); for(MemoryEntry e:memories)m.put(e.toJson()); j.put("memories",m);
        JSONArray d=new JSONArray(); for(DiaryEntry e:diary)d.put(e.toJson()); j.put("diary",d);
        JSONObject k=new JSONObject(); for(Map.Entry<String,Integer> e:knowledge.entrySet())k.put(e.getKey(),e.getValue()); j.put("knowledge",k);
        JSONObject b=new JSONObject(); for(Map.Entry<String,Double> e:beliefs.entrySet())b.put(e.getKey(),e.getValue()); j.put("beliefs",b); JSONObject bs=new JSONObject(); for(Map.Entry<String,BeliefState> e:beliefStates.entrySet())bs.put(e.getKey(),e.getValue().toJson()); j.put("beliefStates",bs); j.put("emotion",emotion.toJson()); j.put("currentIntention",currentIntention); j.put("lastProactiveAt",lastProactiveAt); j.put("environment",environment.toJson()); j.put("worldRuntime",runtime.toJson()); JSONArray wh=new JSONArray(); for(WorldHistoryEntry e:worldHistory)wh.put(e.toJson()); j.put("worldHistory",wh); JSONArray cl=new JSONArray(); for(CaretakerLogEntry e:caretakerLog)cl.put(e.toJson()); j.put("caretakerLog",cl); JSONArray dr=new JSONArray();for(DeveloperReport e:developerReports)dr.put(e.toJson());j.put("developerReports",dr);j.put("lastCaretakerCheckpoint",lastCaretakerCheckpoint); JSONObject pr=new JSONObject();for(Map.Entry<String,PreferenceState> e:preferences.entrySet())pr.put(e.getKey(),e.getValue().toJson());j.put("preferences",pr);JSONObject ha=new JSONObject();for(Map.Entry<String,HabitState> e:habits.entrySet())ha.put(e.getKey(),e.getValue().toJson());j.put("habits",ha);JSONObject pa=new JSONObject();for(Map.Entry<String,PlaceAssociation> e:placeAssociations.entrySet())pa.put(e.getKey(),e.getValue().toJson());j.put("placeAssociations",pa);j.put("moodState",mood.toJson());j.put("personality",personality.toJson());JSONArray th=new JSONArray();for(ThoughtState t:thoughts)th.put(t.toJson());j.put("thoughts",th);j.put("reedlingState",reedling.toJson());j.put("lastCatSeenAt",lastCatSeenAt);j.put("lastAbsenceMinutes",lastAbsenceMinutes);j.put("reunionContext",reunionContext);j.put("persistentIntentionTarget",persistentIntentionTarget);j.put("intentionStartedAt",intentionStartedAt); j.put("nameState",nameState.toJson()); j.put("catState",catState.toJson()); j.put("catSearch",catSearch.toJson()); j.put("notificationEvents",NotificationEventQueue.toJson(notificationEvents)); j.put("girlTravel",girlTravel.toJson());j.put("catTravel",catTravel.toJson());j.put("planState",planState.toJson());j.put("routeRuntime",routeRuntime.toJson());JSONArray gi=new JSONArray();for(GodMessage gm:godInbox)gi.put(gm.toJson());j.put("godInbox",gi); j.put("godMemory",godMemory.toJson()); JSONArray pli=new JSONArray();for(String id:processedLearningIds)pli.put(id);j.put("processedLearningIds",pli);j.put("worldWetness",worldWetness);j.put("visibility",visibility); return j;
    }

    public static WorldState fromJson(JSONObject j) throws JSONException {
        int version=j.optInt("saveVersion",1); if(version> SAVE_VERSION) throw new JSONException("Unsupported save version "+version);
        WorldState s=new WorldState(); s.createdAt=j.optLong("createdAt",System.currentTimeMillis()); s.lastSavedAt=j.optLong("lastSavedAt",s.createdAt);
        s.lastOpenedAt=j.optLong("lastOpenedAt",s.lastSavedAt); s.lastSimulatedAt=j.optLong("lastSimulatedAt",s.lastOpenedAt); s.worldMinutes=j.optDouble("worldMinutes",500); s.catX=(float)j.optDouble("catX",320); s.haruX=(float)j.optDouble("haruX",960);
        s.acceptedName=j.optString("acceptedName",""); s.age=j.optInt("age",15); s.brainGrowth=j.optDouble("brainGrowth",5.0); s.pendingBirthdayLearning=j.optBoolean("pendingBirthdayLearning",false); s.lastDevelopmentAt=j.optLong("lastDevelopmentAt",s.createdAt); JSONObject sk=j.optJSONObject("skills"); if(sk!=null){java.util.Iterator<String> sit=sk.keys();while(sit.hasNext()){String key=sit.next();s.skills.put(key,sk.optDouble(key,0));}} JSONArray la=j.optJSONArray("learnedActions");if(la!=null)for(int i=0;i<la.length();i++)s.learnedActions.add(la.optString(i));
        s.proactiveEnabled=j.optBoolean("proactiveEnabled",true); s.quietStartHour=j.optInt("quietStartHour",23); s.quietEndHour=j.optInt("quietEndHour",7);
        s.haruMood=j.optString("haruMood","curious"); s.haruActivity=j.optString("haruActivity","walking");
        s.body=BodyState.fromJson(j.optJSONObject("body")); s.relationship=RelationshipState.fromJson(j.optJSONObject("relationship"));
        JSONArray m=j.optJSONArray("memories"); if(m!=null)for(int i=0;i<m.length();i++)s.memories.add(MemoryEntry.fromJson(m.getJSONObject(i)));
        JSONArray d=j.optJSONArray("diary"); if(d!=null)for(int i=0;i<d.length();i++)s.diary.add(DiaryEntry.fromJson(d.getJSONObject(i)));
        JSONObject k=j.optJSONObject("knowledge"); if(k!=null){java.util.Iterator<String> it=k.keys(); while(it.hasNext()){String key=it.next(); s.knowledge.put(key,k.optInt(key,0));}}
        JSONObject b=j.optJSONObject("beliefs"); if(b!=null){java.util.Iterator<String> it=b.keys(); while(it.hasNext()){String key=it.next(); s.beliefs.put(key,b.optDouble(key,0.5));}}
        JSONObject bs=j.optJSONObject("beliefStates"); if(bs!=null){java.util.Iterator<String> it=bs.keys();while(it.hasNext()){String key=it.next();JSONObject v=bs.optJSONObject(key);if(v!=null)s.beliefStates.put(key,BeliefState.fromJson(key,v));}} if(s.beliefStates.isEmpty())s.beliefStates.put("cat_intent",new BeliefState("cat_intent","uncertain",s.beliefs.getOrDefault("the_cat_may_be_a_friend",.42))); s.emotion=EmotionState.fromJson(j.optJSONObject("emotion")); s.currentIntention=j.optString("currentIntention","observe_lake"); s.lastProactiveAt=j.optLong("lastProactiveAt",0L); s.environment=EnvironmentState.fromJson(j.optJSONObject("environment")); if(version<4 && j.has("weather") && j.optJSONObject("environment")==null){String legacy=j.optString("weather","clear").toUpperCase();s.environment.weather="CLEAR".equals(legacy)||"CLOUDY".equals(legacy)||"RAIN".equals(legacy)?legacy:"CLEAR";} s.runtime=WorldRuntimeState.fromJson(j.optJSONObject("worldRuntime")); if(version<4 && j.optJSONObject("world")!=null){JSONArray oldObjects=j.optJSONObject("world").optJSONArray("objects");if(oldObjects!=null)for(int oi=0;oi<oldObjects.length();oi++){JSONObject oo=oldObjects.optJSONObject(oi);if(oo!=null)s.runtime.objects.put(oo.optString("id"),new WorldRuntimeState.ObjectState((float)oo.optDouble("x"),oo.optBoolean("enabled",true)));}} JSONArray wh=j.optJSONArray("worldHistory");if(wh!=null)for(int i=0;i<wh.length();i++)s.worldHistory.add(WorldHistoryEntry.fromJson(wh.optJSONObject(i))); JSONArray cl=j.optJSONArray("caretakerLog");if(cl!=null)for(int i=0;i<cl.length();i++)s.caretakerLog.add(CaretakerLogEntry.fromJson(cl.optJSONObject(i)));JSONArray dr=j.optJSONArray("developerReports");if(dr!=null)for(int i=0;i<dr.length();i++)s.developerReports.add(DeveloperReport.fromJson(dr.optJSONObject(i)));s.lastCaretakerCheckpoint=j.optString("lastCaretakerCheckpoint",""); JSONObject pr=j.optJSONObject("preferences");if(pr!=null){java.util.Iterator<String> it=pr.keys();while(it.hasNext()){String key=it.next();s.preferences.put(key,PreferenceState.fromJson(key,pr.optJSONObject(key)));}}JSONObject ha=j.optJSONObject("habits");if(ha!=null){java.util.Iterator<String> it=ha.keys();while(it.hasNext()){String key=it.next();s.habits.put(key,HabitState.fromJson(key,ha.optJSONObject(key)));}}JSONObject pa=j.optJSONObject("placeAssociations");if(pa!=null){java.util.Iterator<String> it=pa.keys();while(it.hasNext()){String key=it.next();s.placeAssociations.put(key,PlaceAssociation.fromJson(key,pa.optJSONObject(key)));}}s.mood=MoodState.fromJson(j.optJSONObject("moodState"));s.personality=PersonalityState.fromJson(j.optJSONObject("personality"));JSONArray th=j.optJSONArray("thoughts");if(th!=null)for(int i=0;i<th.length();i++)s.thoughts.add(ThoughtState.fromJson(th.optJSONObject(i)));s.reedling=CreatureState.fromJson(j.optJSONObject("reedlingState"));s.lastCatSeenAt=j.optLong("lastCatSeenAt",s.lastOpenedAt);s.lastAbsenceMinutes=j.optLong("lastAbsenceMinutes",0);s.reunionContext=j.optString("reunionContext","");s.persistentIntentionTarget=j.optString("persistentIntentionTarget","");s.intentionStartedAt=j.optLong("intentionStartedAt",0);
        s.nameState=NameState.fromJson(j.optJSONObject("nameState"));
        // acceptedName from older prototypes is intentionally NOT promoted to divine officialName.
        s.catState=CatState.fromJson(j.optJSONObject("catState"),s.catX,s.lastOpenedAt);
        s.catSearch=GirlCatSearchEngine.SearchState.fromJson(j.optJSONObject("catSearch"));
        NotificationEventQueue.load(j.optJSONArray("notificationEvents"),s.notificationEvents);
        s.girlTravel=TravelState.fromJson(j.optJSONObject("girlTravel"));s.catTravel=TravelState.fromJson(j.optJSONObject("catTravel"));s.planState=PlanState.fromJson(j.optJSONObject("planState"));s.routeRuntime=RouteRuntimeState.fromJson(j.optJSONObject("routeRuntime"));if(version<9&&"ACTIVE".equals(s.planState.status)){if(s.planState.destination.isEmpty()&&!s.girlTravel.destinationArea.isEmpty())s.planState.destination=s.girlTravel.destinationArea;if(s.planState.plannedAction.isEmpty())s.planState.plannedAction=("seek_shelter".equals(s.planState.intentionId)||"sleep".equals(s.planState.intentionId))?"REST_PROTECT":"reflect".equals(s.planState.intentionId)?"REFLECT":"find_cat".equals(s.planState.intentionId)?"SEARCH_LOCAL":"OBSERVE";s.planState.lastProgressAt=s.lastSavedAt;}JSONArray gi=j.optJSONArray("godInbox");if(gi!=null)for(int i=0;i<gi.length();i++)s.godInbox.add(GodMessage.fromJson(gi.optJSONObject(i))); s.godMemory=GodMemory.fromJson(j.optJSONObject("godMemory")); JSONArray pli=j.optJSONArray("processedLearningIds");if(pli!=null)for(int i=0;i<pli.length();i++)s.processedLearningIds.add(pli.optString(i));s.worldWetness=j.optDouble("worldWetness",0);s.visibility=j.optDouble("visibility",1.0);
        if(s.memories.isEmpty())s.memories.add(new MemoryEntry(s.createdAt,"recovered","Some early memories are faint.",0.5));
        return s;
    }
}
