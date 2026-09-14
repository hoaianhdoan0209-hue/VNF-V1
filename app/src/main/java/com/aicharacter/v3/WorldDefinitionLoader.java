package com.aicharacter.v3;

import android.content.Context;
import org.json.JSONObject;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/** Loads immutable authored world content once. Runtime state is a separate overlay. */
public final class WorldDefinitionLoader {
    private WorldDefinitionLoader() {}
    public static WorldModel load(Context context) {
        try (InputStream in=context.getAssets().open("world/world_v1.json")) {
            String json=new String(in.readAllBytes(), StandardCharsets.UTF_8);
            return WorldModel.fromDefinitionJson(new JSONObject(json));
        } catch (Exception e) {
            throw new IllegalStateException("World definition could not be loaded", e);
        }
    }
}
