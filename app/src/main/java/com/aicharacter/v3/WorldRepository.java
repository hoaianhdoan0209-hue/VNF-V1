package com.aicharacter.v3;

import android.content.Context;
import org.json.JSONObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

public final class WorldRepository {
    private final File saveFile;
    private final File backupFile;
    private final Context context;
    public WorldRepository(Context context) {
        this.context=context.getApplicationContext();
        File dir = new File(context.getFilesDir(), "world"); if(!dir.exists()) dir.mkdirs();
        saveFile = new File(dir, "world.json"); backupFile = new File(dir, "world.backup.json");
    }
    public synchronized WorldState loadOrCreate() {
        WorldState state = tryLoad(saveFile);
        if(state == null) state = tryLoad(backupFile);
        if(state == null) state = WorldState.fresh();
        attachDefinition(state);
        if(!saveFile.exists()) save(state);
        return state;
    }
    private WorldState tryLoad(File f) {
        if(!f.exists()) return null;
        try(FileInputStream in = new FileInputStream(f)) {
            byte[] data=in.readAllBytes(); return WorldState.fromJson(new JSONObject(new String(data, StandardCharsets.UTF_8)));
        } catch(Exception ignored) { return null; }
    }
    public synchronized void save(WorldState state) {
        try {
            if(state.world==null) attachDefinition(state);
            state.runtime.capture(state.world);
            state.lastSavedAt=System.currentTimeMillis();
            byte[] data=state.toJson().toString(2).getBytes(StandardCharsets.UTF_8);
            File tmp=new File(saveFile.getParentFile(),"world.tmp");
            try(FileOutputStream out=new FileOutputStream(tmp)){ out.write(data); out.getFD().sync(); }
            if(saveFile.exists()) copy(saveFile, backupFile);
            if(!tmp.renameTo(saveFile)) { copy(tmp, saveFile); tmp.delete(); }
        } catch(Exception e) { throw new IllegalStateException("Could not persist VNF world", e); }
    }
    private void attachDefinition(WorldState state){ state.world=WorldDefinitionLoader.load(context); state.runtime.mergeDefinition(state.world); }
    private static void copy(File from, File to) throws Exception {
        try(FileInputStream in=new FileInputStream(from); FileOutputStream out=new FileOutputStream(to)) { in.transferTo(out); out.getFD().sync(); }
    }
}
