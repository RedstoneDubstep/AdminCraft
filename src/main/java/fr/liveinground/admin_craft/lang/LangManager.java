package fr.liveinground.admin_craft.lang;

import fr.liveinground.admin_craft.PlaceHolderSystem;

import java.nio.file.Path;
import java.util.Map;

public class LangManager {

    private static final TranslationProvider PROVIDER = new TranslationProvider();

    private static String language = "en_us";

    private LangManager() {}

    public static void setLanguage(String lang) {
        language = lang;
    }

    public static String getLanguage() {
        return language;
    }

    public static void load(Path langDir) {
        PROVIDER.clear();

        Path file = langDir.resolve(language + ".json");

        PROVIDER.loadFromFile(file);
    }

    public static String tr(String key) {
        return PROVIDER.getRaw(key);
    }

    public static String tr(String key, Map<String, String> placeholders) {
        String value = tr(key);
        return PlaceHolderSystem.replacePlaceholders(value, placeholders);
    }

    public static void reload(Path langDir) {
        load(langDir);
    }
}
