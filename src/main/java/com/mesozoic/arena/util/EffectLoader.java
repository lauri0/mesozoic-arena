package com.mesozoic.arena.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.yaml.snakeyaml.Yaml;

/**
 * Loads effect descriptions from {@code effects.yaml}.
 */
public final class EffectLoader {
    private static final String EFFECT_FILE = "data/effects.yaml";

    private EffectLoader() {
    }

    /**
     * Returns a map of effect names to their descriptions.
     */
    public static Map<String, String> loadDescriptions() {
        Map<String, String> descriptions = new HashMap<>();
        Yaml yaml = new Yaml();

        // First try to load from the classpath
        try (InputStream input = EffectLoader.class.getClassLoader()
                .getResourceAsStream("effects.yaml")) {
            if (input != null) {
                Map<String, Object> root = yaml.load(input);
                copyEntries(root, descriptions);
                return descriptions;
            }
        } catch (IOException ignored) {
            // fall back to file system path
        }

        // Fall back to reading from the data directory on disk
        try (InputStream input = Files.newInputStream(Path.of(EFFECT_FILE))) {
            Map<String, Object> root = yaml.load(input);
            copyEntries(root, descriptions);
        } catch (IOException ignored) {
            // ignore and return whatever we managed to load
        }
        return descriptions;
    }

    private static void copyEntries(Map<String, Object> source, Map<String, String> target) {
        if (source == null) {
            return;
        }
        for (Entry<String, Object> entry : source.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                target.put(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }
    }
}
