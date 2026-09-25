package com.aicharacter.v3;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.util.Log;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Low-volume procedural renderer for VNF ambience/foley/moment cues.
 * Audio failure is isolated from simulation: no method here mutates WorldState.
 */
public final class ProceduralAudioEngine {
 private static final String TAG="VNF-Audio";
 private static final int SAMPLE_RATE=22050,BLOCK_FRAMES=512;
 private static final double TWO_PI=Math.PI*2.0;
 static final double AMBIENCE_GAIN=1.55,FOOTSTEP_GAIN=1.85,BREATH_GAIN=1.70,PURR_GAIN=1.95,MASTER_GAIN=.86;
 private final ConcurrentLinkedQueue<SoundEvent> cueQueue=new ConcurrentLinkedQueue<>();
 private volatile AudioSceneFrame frame=AudioSceneFrame.quiet();
 private volatile boolean active=false,destroyed=false;
 private AudioTrack track;private Thread worker;
 private long rng=0x51A7B33FL;
 private double windLp,leafLp,waterLp,windPhase,waterPhase,divinePhase,breathPhase,haruStepPhase,catStepPhase,stepTonePhase,purrPhase,purrPhase2;
 private double haruStepEnv,catStepEnv;
 private CueVoice cueVoice;

 public static final class CueProfile{
  public final double frequencyA,frequencyB,noiseMix,gain,durationSeconds;
  CueProfile(double a,double b,double noise,double gain,double duration){frequencyA=finite(a,440);frequencyB=finite(b,0);noiseMix=cl(noise,0,1);this.gain=cl(gain,0,.35);durationSeconds=cl(duration,.08,8);}
 }

 public ProceduralAudioEngine(){}

 public void update(AudioSceneFrame next){if(next!=null&&next.finite())frame=next;}
 public void play(SoundEvent event){if(event!=null&&event.valid()&&!destroyed){while(cueQueue.size()>8)cueQueue.poll();cueQueue.offer(event);}}

 public synchronized void resume(){
  if(destroyed)return;active=true;ensureTrack();
  try{if(track!=null&&track.getState()==AudioTrack.STATE_INITIALIZED&&track.getPlayState()!=AudioTrack.PLAYSTATE_PLAYING)track.play();}catch(Throwable e){Log.w(TAG,"Audio resume failed",e);}
  if(worker==null||!worker.isAlive()){worker=new Thread(this::audioLoop,"VNF-Procedural-Audio");worker.setDaemon(true);worker.start();}
 }
 public synchronized void pause(){
  active=false;try{if(track!=null&&track.getState()==AudioTrack.STATE_INITIALIZED)track.pause();}catch(Throwable e){Log.w(TAG,"Audio pause failed",e);}
 }
 public synchronized void destroy(){
  destroyed=true;active=false;if(worker!=null)worker.interrupt();
  try{if(track!=null){if(track.getState()==AudioTrack.STATE_INITIALIZED)track.stop();track.release();}}catch(Throwable e){Log.w(TAG,"Audio destroy failed",e);}track=null;worker=null;cueQueue.clear();cueVoice=null;
 }

 private synchronized void ensureTrack(){
  if(track!=null&&track.getState()==AudioTrack.STATE_INITIALIZED)return;
  try{
   int min=AudioTrack.getMinBufferSize(SAMPLE_RATE,AudioFormat.CHANNEL_OUT_STEREO,AudioFormat.ENCODING_PCM_16BIT);
   int bytes=Math.max(min>0?min:0,BLOCK_FRAMES*2*2*4);
   AudioAttributes attrs=new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();
   AudioFormat fmt=new AudioFormat.Builder().setSampleRate(SAMPLE_RATE).setEncoding(AudioFormat.ENCODING_PCM_16BIT).setChannelMask(AudioFormat.CHANNEL_OUT_STEREO).build();
   track=new AudioTrack.Builder().setAudioAttributes(attrs).setAudioFormat(fmt).setBufferSizeInBytes(bytes).setTransferMode(AudioTrack.MODE_STREAM).build();
   if(track.getState()!=AudioTrack.STATE_INITIALIZED){track.release();track=null;}
  }catch(Throwable e){Log.w(TAG,"AudioTrack unavailable; world continues silently",e);track=null;}
 }

 private void audioLoop(){
  short[] pcm=new short[BLOCK_FRAMES*2];
  while(!destroyed){
   if(!active){sleep(40);continue;}
   AudioTrack local=track;if(local==null||local.getState()!=AudioTrack.STATE_INITIALIZED){synchronized(this){ensureTrack();local=track;}if(local==null){sleep(250);continue;}try{local.play();}catch(Throwable ignored){}}
   synthesize(pcm);
   try{int wrote=local.write(pcm,0,pcm.length,AudioTrack.WRITE_BLOCKING);if(wrote<0)sleep(40);}catch(Throwable e){Log.w(TAG,"Audio write failed; retrying without touching world state",e);sleep(120);}
  }
 }

 private void synthesize(short[] out){
  AudioSceneFrame f=frame==null?AudioSceneFrame.quiet():frame;
  if(cueVoice==null){SoundEvent e=cueQueue.poll();if(e!=null)cueVoice=new CueVoice(e,profileFor(e.semanticId));}
  for(int i=0;i<BLOCK_FRAMES;i++){
   double noise=noise(),ambient=0;
   windLp+=.010*(noise-windLp);
   leafLp+=.045*(noise-leafLp);
   waterLp+=.018*(noise-waterLp);
   windPhase=wrap(windPhase+TWO_PI*.075/SAMPLE_RATE);
   waterPhase=wrap(waterPhase+TWO_PI*184/SAMPLE_RATE);
   divinePhase=wrap(divinePhase+TWO_PI*73.4/SAMPLE_RATE);
   double windMod=.72+.28*Math.sin(windPhase),exposure=Math.max(.02,Math.min(1,f.weatherExposure));
   ambient+=windLp*(.003+.028*f.wind*(.24+.76*exposure))*windMod;
   if(f.rain>0){double openRain=(noise*.72+(noise-leafLp)*.28)*(.007+.034*f.rain)*exposure;double muffledRain=windLp*(.004+.016*f.rain)*(1-exposure);ambient+=openRain+muffledRain;}
   if(f.water>0)ambient+=(waterLp*.012+Math.sin(waterPhase)*.0028)*f.water;
   if(f.areaId.contains("grove"))ambient+=(noise-leafLp)*(.0035+.007*f.wind);
   else if(f.areaId.contains("garden"))ambient+=leafLp*.0045;
   else if(f.areaId.contains("home"))ambient+=windLp*.0025;
   if("NIGHT".equals(f.dayPhase))ambient+=Math.sin(wrap(waterPhase*.061))*0.0018;

   ambient*=AMBIENCE_GAIN;

   double divine=0;
   if(f.divinePresence>.001){
    divine=(Math.sin(divinePhase)*.010+Math.sin(divinePhase*2.003)*.005+Math.sin(divinePhase*7.11)*.0018)*f.divinePresence;
   }

   double breath=0;
   double breathHz=.18+.24*Math.max(f.haruBreathing,f.haruStress);
   breathPhase=wrap(breathPhase+TWO_PI*breathHz/SAMPLE_RATE);
   double breathEnv=Math.max(0,Math.sin(breathPhase));
   breath=(windLp*.018+noise*.002)*breathEnv*f.haruBreathing*(.45+.55*f.haruStress)*BREATH_GAIN;

   double haruFoot=stepSample(true,f.haruWalking,f.haruSpeed,noise,f.haruSurface)*FOOTSTEP_GAIN;
   double catFoot=stepSample(false,f.catMoving,f.catSpeed,noise,f.catSurface)*FOOTSTEP_GAIN;
   double purr=0;if(f.catPurr>.001){purrPhase=wrap(purrPhase+TWO_PI*48.5/SAMPLE_RATE);purrPhase2=wrap(purrPhase2+TWO_PI*97.0/SAMPLE_RATE);double trem=.76+.24*Math.sin(waterPhase*.37);purr=(Math.sin(purrPhase)*.0075+Math.sin(purrPhase2)*.0035+windLp*.003)*f.catPurr*trem*PURR_GAIN;}

   double left=ambient+divine+breath+haruFoot+catFoot+purr,right=left;
   if(cueVoice!=null){
    CueVoice.Sample cs=cueVoice.next(noise);
    left+=cs.left;right+=cs.right;
    if(cueVoice.done())cueVoice=null;
   }
   double leftSafe=softClip(left*MASTER_GAIN),rightSafe=softClip(right*MASTER_GAIN);
   out[i*2]=(short)Math.round(leftSafe*32767.0);
   out[i*2+1]=(short)Math.round(rightSafe*32767.0);
  }
 }

 private double stepSample(boolean haru,boolean walking,double speed,double noise,String surfaceName){
  double rate=(haru?1.25:1.75)+speed*(haru?1.85:2.55),inc=TWO_PI*rate/SAMPLE_RATE;SurfaceAcoustics.Profile material=surfaceProfile(surfaceName);
  double toneHz=material.lowTone+speed*(material.highTone-material.lowTone)*.32;
  if(haru){
   double old=haruStepPhase;haruStepPhase=wrap(haruStepPhase+inc);if(walking&&haruStepPhase<old)haruStepEnv=.92;
   haruStepEnv*=walking?.9945:.982;stepTonePhase=wrap(stepTonePhase+TWO_PI*toneHz/SAMPLE_RATE);
   double tonal=Math.sin(stepTonePhase),texture=noise*material.noiseMix+tonal*(1-material.noiseMix),wetSlap=material.surface==SurfaceAcoustics.Surface.WET_BANK?Math.sin(stepTonePhase*2.7)*.18:0;
   return (texture+wetSlap)*haruStepEnv*(.009+.013*speed)*(1-material.damping*.20);
  }else{
   double old=catStepPhase;catStepPhase=wrap(catStepPhase+inc);if(walking&&catStepPhase<old)catStepEnv=.70;
   catStepEnv*=walking?.990:.970;double tone=Math.sin(stepTonePhase*1.43),texture=noise*Math.min(.78,material.noiseMix+.12)+tone*Math.max(.22,1-material.noiseMix-.12);
   return texture*catStepEnv*(.0032+.0062*speed)*(1-material.damping*.26);
  }
 }
 public static SurfaceAcoustics.Profile surfaceProfile(String name){try{return SurfaceAcoustics.profile(SurfaceAcoustics.Surface.valueOf(name==null?"SOFT_GROUND":name));}catch(Throwable ignored){return SurfaceAcoustics.profile(SurfaceAcoustics.Surface.SOFT_GROUND);}}

 public static CueProfile profileFor(String semanticId){
  String id=semanticId==null?"":semanticId.toLowerCase(java.util.Locale.ROOT);
  if("social_reunion".equals(id))return new CueProfile(523.25,659.25,.02,.115,2.8);
  if("cat_settle_near".equals(id))return new CueProfile(55,110,.12,.065,2.4);
  if("social_approach".equals(id))return new CueProfile(329.63,440,.04,.075,1.7);
  if("social_retreat".equals(id))return new CueProfile(123.47,185,.38,.070,1.5);
  if("insight_soft".equals(id))return new CueProfile(587.33,880,.015,.070,1.9);
  if("divine_presence_pulse".equals(id))return new CueProfile(82.41,329.63,.04,.095,2.3);
  return new CueProfile(440,660,.06,.050,1.1);
 }

 private final class CueVoice{
  final SoundEvent event;final CueProfile profile;final int totalSamples;int sampleIndex;double phaseA,phaseB;
  CueVoice(SoundEvent e,CueProfile p){event=e;profile=p;double requested=e.durationSeconds>0?e.durationSeconds:p.durationSeconds;totalSamples=Math.max(1,(int)(SAMPLE_RATE*Math.min(requested,p.durationSeconds)));}
  Sample next(double noise){
   double t=sampleIndex/(double)Math.max(1,totalSamples-1),env=Math.sin(Math.PI*Math.max(0,Math.min(1,t)));env=Math.pow(Math.max(0,env),.72);
   phaseA=wrap(phaseA+TWO_PI*profile.frequencyA/SAMPLE_RATE);phaseB=wrap(phaseB+TWO_PI*profile.frequencyB/SAMPLE_RATE);
   double tonal=Math.sin(phaseA)*.70+(profile.frequencyB>0?Math.sin(phaseB)*.30:0),mono=(tonal*(1-profile.noiseMix)+noise*profile.noiseMix)*profile.gain*event.intensity*env;
   double pan=cl(event.panHint,-1,1),lg=Math.sqrt((1-pan)*.5),rg=Math.sqrt((1+pan)*.5);sampleIndex++;return new Sample(mono*lg,mono*rg);
  }
  boolean done(){return sampleIndex>=totalSamples;}
  final class Sample{final double left,right;Sample(double l,double r){left=l;right=r;}}
 }

 private double noise(){rng=rng*6364136223846793005L+1442695040888963407L;return (((rng>>>40)&0xFFFFFF)/(double)0x7FFFFF)-1.0;}
 private static double wrap(double p){p%=TWO_PI;return p<0?p+TWO_PI:p;}
 private static double softClip(double x){if(!Double.isFinite(x))return 0;return Math.tanh(x);}
 private static double finite(double v,double f){return Double.isFinite(v)?v:f;}
 private static double cl(double v,double a,double b){return Double.isFinite(v)?Math.max(a,Math.min(b,v)):a;}
 private static void sleep(long ms){try{Thread.sleep(ms);}catch(InterruptedException ignored){Thread.currentThread().interrupt();}}
}
