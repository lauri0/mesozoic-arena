package com.mesozoic.arena.util;

import com.mesozoic.arena.model.PersistentEffectDefinition;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.yaml.snakeyaml.Yaml;

/**
 * Loads persistent effect definitions from {@code persistent_effects.yaml}.
 */
public final class PersistentEffectLoader {
    private static final String EFFECT_FILE = "data/persistent_effects.yaml";

    private PersistentEffectLoader() {
    }

    /**
     * Returns a map of effect names to their definitions.
     */
    public static Map<String, PersistentEffectDefinition> loadDefinitions() {
        Map<String, PersistentEffectDefinition> map = new HashMap<>();
        Yaml yaml = new Yaml();

        try (InputStream input = PersistentEffectLoader.class.getClassLoader()
                .getResourceAsStream("persistent_effects.yaml")) {
            if (input != null) {
                Map<String, Object> root = yaml.load(input);
                copyEntries(root, map);
                return map;
            }
        } catch (IOException ignored) {
        }

        try (InputStream input = Files.newInputStream(Path.of(EFFECT_FILE))) {
            Map<String, Object> root = yaml.load(input);
            copyEntries(root, map);
        } catch (IOException ignored) {
        }
        return map;
    }

    private static void copyEntries(Map<String, Object> source,
            Map<String, PersistentEffectDefinition> target) {
        if (source == null) {
            return;
        }
        for (Entry<String, Object> entry : source.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                continue;
            }
            if (entry.getValue() instanceof Map<?,?> values) {
                String name = entry.getKey();
                Object descRaw = values.get("description");
                Object durRaw = values.get("duration");
                String description = descRaw == null ? "" : String.valueOf(descRaw);
                int duration = durRaw == null ? 0
                        : ((Number) durRaw).intValue();
                target.put(name,
                        new PersistentEffectDefinition(name, description, duration));
            }
        }
    }
}
