package fr.liveinground.admin_craft.lang;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class TranslationProvider {
    private static final Gson GSON = new Gson();

    private final Map<String, String> translations = new HashMap<>();

    public void clear() {
        translations.clear();
    }

    public void loadFromFile(Path file) {
        if (!Files.exists(file)) return;

        try {
            JsonObject obj = GSON.fromJson(Files.readString(file), JsonObject.class);

            if (obj == null) return;

            for (String key : obj.keySet()) {
                translations.put(key, obj.get(key).getAsString());
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to load lang file: " + file, e);
        }
    }

    public void putDefaults(Map<String, String> defaults) {
        for (Map.Entry<String, String> e : defaults.entrySet()) {
            translations.putIfAbsent(e.getKey(), e.getValue());
        }
    }

    public String getRaw(String key) {
        return translations.getOrDefault(key, key);
    }

    public Map<String, String> getAll() {
        return translations;
    }
}
