package com.aicharacter.v3;

import android.content.Context;
import org.json.JSONObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/** Atomic-ish private world save with causal-version recovery across primary, backup and synced temp files. */
public final class WorldRepository{
 private final File saveFile,backupFile,tmpFile;
 private final Context context;

 public WorldRepository(Context context){
  this.context=context.getApplicationContext();
  File dir=new File(this.context.getFilesDir(),"world");
  if(!dir.exists()&&!dir.mkdirs())throw new IllegalStateException("Could not create private world directory");
  saveFile=new File(dir,"world.json");
  backupFile=new File(dir,"world.backup.json");
  tmpFile=new File(dir,"world.tmp");
 }

 public synchronized WorldState loadOrCreate(){
  long now=System.currentTimeMillis();
  WorldState primary=tryLoad(saveFile),backup=tryLoad(backupFile),pending=tryLoad(tmpFile);
  WorldState state=primary;
  File source=primary==null?null:saveFile;

  if(compareCausalVersion(backup,state)>0){state=backup;source=backupFile;}
  if(compareCausalVersion(pending,state)>0){state=pending;source=tmpFile;}

  if(state==null){
   state=WorldState.fresh();
   StateInvariantChecker.normalize(state,now);
   attachDefinition(state);
   StateInvariantChecker.repairOrReport(state,now);
   save(state);
   return state;
  }

  StateInvariantChecker.normalize(state,now);
  attachDefinition(state);
  StateInvariantChecker.repairOrReport(state,now);

  try{
   if(source==tmpFile){
    promotePending(primary,state);
   }else if(source==backupFile){
    copy(backupFile,saveFile);
    requireCommittedAtLeast(state,"Recovered backup did not survive promotion");
    discardStaleTemp(pending,state);
   }else{
    discardStaleTemp(pending,state);
   }
  }catch(Exception e){
   throw new IllegalStateException("Could not recover VNF world",e);
  }
  return state;
 }

 private void promotePending(WorldState priorPrimary,WorldState selected)throws IOException{
  if(priorPrimary!=null)copy(saveFile,backupFile);
  if(!tmpFile.renameTo(saveFile)){
   copy(tmpFile,saveFile);
   if(tmpFile.exists()&&!tmpFile.delete())tmpFile.deleteOnExit();
  }
  requireCommittedAtLeast(selected,"Recovered temporary world lost causal progress");
 }

 private void discardStaleTemp(WorldState pending,WorldState selected){
  if(!tmpFile.exists())return;
  if(pending==null||compareCausalVersion(pending,selected)<=0){
   if(!tmpFile.delete())tmpFile.deleteOnExit();
  }
 }

 private void requireCommittedAtLeast(WorldState expected,String message)throws IOException{
  WorldState committed=tryLoad(saveFile);
  if(committed==null||compareCausalVersion(committed,expected)<0)throw new IOException(message);
 }

 private static long causalCursor(WorldState s){
  return s==null?Long.MIN_VALUE:Math.max(s.lastSimulatedAt,s.lastOpenedAt);
 }

 private static int compareCausalVersion(WorldState a,WorldState b){
  if(a==b)return 0;
  if(a==null)return -1;
  if(b==null)return 1;
  long ac=causalCursor(a),bc=causalCursor(b);
  if(ac!=bc)return Long.compare(ac,bc);
  return Long.compare(a.lastSavedAt,b.lastSavedAt);
 }

 private WorldState tryLoad(File f){
  if(!f.exists()||!f.isFile()||f.length()<=0)return null;
  try(FileInputStream in=new FileInputStream(f);
      ByteArrayOutputStream out=new ByteArrayOutputStream((int)Math.min(f.length(),1024L*1024L))){
   byte[] buf=new byte[8192];
   int n;
   while((n=in.read(buf))!=-1)out.write(buf,0,n);
   WorldState state=WorldState.fromJson(new JSONObject(new String(out.toByteArray(),StandardCharsets.UTF_8)));
   StateInvariantChecker.normalize(state,System.currentTimeMillis());
   return state;
  }catch(Exception ignored){
   return null;
  }
 }

 public synchronized void save(WorldState state){
  try{
   if(state==null)throw new IOException("World state is null");
   long wallNow=System.currentTimeMillis(),priorSaved=state.lastSavedAt;
   StateInvariantChecker.normalize(state,wallNow);
   if(state.world==null)attachDefinition(state);
   StateInvariantChecker.repairOrReport(state,wallNow);
   state.runtime.capture(state.world);
   state.lastSavedAt=Math.max(priorSaved,wallNow);

   byte[] data=state.toJson().toString(2).getBytes(StandardCharsets.UTF_8);
   if(tmpFile.exists()&&!tmpFile.delete())throw new IOException("Could not clear stale temporary world save");
   try(FileOutputStream out=new FileOutputStream(tmpFile)){
    out.write(data);
    out.getFD().sync();
   }

   WorldState staged=tryLoad(tmpFile);
   if(staged==null)throw new IOException("Temporary world save failed validation");

   WorldState current=tryLoad(saveFile);
   if(current!=null&&compareCausalVersion(staged,current)<0)
    throw new IOException("Refusing to replace a causally newer committed world");

   if(current!=null)copy(saveFile,backupFile);
   if(!tmpFile.renameTo(saveFile)){
    copy(tmpFile,saveFile);
    if(tmpFile.exists()&&!tmpFile.delete())tmpFile.deleteOnExit();
   }

   WorldState committed=tryLoad(saveFile);
   if(committed==null){
    restoreBackupIfValid();
    throw new IOException("Committed world save failed validation");
   }
   if(compareCausalVersion(committed,staged)<0){
    restoreBackupIfValid();
    throw new IOException("Committed world save lost causal progress");
   }
  }catch(Exception e){
   throw new IllegalStateException("Could not persist VNF world",e);
  }
 }

 private void restoreBackupIfValid()throws IOException{
  if(tryLoad(backupFile)!=null)copy(backupFile,saveFile);
 }

 private void attachDefinition(WorldState state){
  state.world=WorldDefinitionLoader.load(context);
  state.runtime.mergeDefinition(state.world);
 }

 private static void copy(File from,File to)throws IOException{
  try(FileInputStream in=new FileInputStream(from);
      FileOutputStream out=new FileOutputStream(to)){
   byte[] buf=new byte[8192];
   int n;
   while((n=in.read(buf))!=-1)out.write(buf,0,n);
   out.getFD().sync();
  }
 }
}
