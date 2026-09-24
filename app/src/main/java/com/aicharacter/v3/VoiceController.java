package com.aicharacter.v3;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import java.util.ArrayList;
import java.util.Locale;

public final class VoiceController implements TextToSpeech.OnInitListener {
    public interface Listener { void onRecognized(String text); void onStatus(String text); }
    private final Activity activity; private final Listener listener; private TextToSpeech tts; private SpeechRecognizer recognizer; private boolean enabled=false,ttsReady=false;private String pendingProactive="";
    public VoiceController(Activity a, Listener l){activity=a;listener=l; tts=new TextToSpeech(a,this); if(SpeechRecognizer.isRecognitionAvailable(a)){ recognizer=SpeechRecognizer.createSpeechRecognizer(a); recognizer.setRecognitionListener(new RecognitionListener(){
        public void onReadyForSpeech(Bundle p){listener.onStatus("Đang nghe…");} public void onBeginningOfSpeech(){} public void onRmsChanged(float r){} public void onBufferReceived(byte[] b){} public void onEndOfSpeech(){}
        public void onError(int e){listener.onStatus("Không nghe rõ.");} public void onResults(Bundle b){ArrayList<String> r=b.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION); if(r!=null&&!r.isEmpty())listener.onRecognized(r.get(0));}
        public void onPartialResults(Bundle b){} public void onEvent(int t,Bundle p){}
    });}}
    public boolean toggle(){enabled=!enabled; listener.onStatus(enabled?"Voice ON":"Voice OFF"); return enabled;}
    public boolean isEnabled(){return enabled;}
    public void listen(){ if(!enabled){listener.onStatus("Double tap nút mic để bật Voice.");return;} if(activity.checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){activity.requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},402);return;} if(recognizer==null){listener.onStatus("Thiết bị không hỗ trợ SpeechRecognizer.");return;} Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH); i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM); i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"vi-VN"); recognizer.startListening(i); }
    public void speak(String text){if(enabled&&tts!=null)tts.speak(text,TextToSpeech.QUEUE_FLUSH,null,"vnf_haru");}
    public void speakProactive(String text){if(text==null||text.trim().isEmpty()||tts==null)return;if(!ttsReady){pendingProactive=text.trim();return;}tts.speak(text,TextToSpeech.QUEUE_ADD,null,"vnf_haru_proactive_"+System.nanoTime());}
    public void destroy(){pendingProactive="";ttsReady=false;if(recognizer!=null)recognizer.destroy(); if(tts!=null)tts.shutdown();}
    @Override public void onInit(int status){if(status!=TextToSpeech.SUCCESS||tts==null)return;int lang=tts.setLanguage(new Locale("vi","VN"));if(lang==TextToSpeech.LANG_MISSING_DATA||lang==TextToSpeech.LANG_NOT_SUPPORTED){lang=tts.setLanguage(Locale.getDefault());}ttsReady=lang!=TextToSpeech.LANG_MISSING_DATA&&lang!=TextToSpeech.LANG_NOT_SUPPORTED;if(ttsReady&&!pendingProactive.isEmpty()){String p=pendingProactive;pendingProactive="";speakProactive(p);}}
}
