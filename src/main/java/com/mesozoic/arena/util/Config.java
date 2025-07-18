package com.mesozoic.arena.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Loads configuration from {@code constants.ini}.
 */
public final class Config {
    private static final String CONFIG_FILE = "constants.ini";
    private static final String GEMINI_ENV_FILE = "gemini.env";
    private static final Properties properties = new Properties();
    private static final Properties secrets = new Properties();

    static {
        loadProperties(CONFIG_FILE, properties);
        loadProperties(GEMINI_ENV_FILE, secrets);
    }

    private static void loadProperties(String fileName, Properties target) {
        try (InputStream in = Config.class.getClassLoader()
                .getResourceAsStream(fileName)) {
            if (in != null) {
                target.load(in);
                return;
            }
        } catch (IOException ignored) {
        }

        try (InputStream in = Files.newInputStream(Path.of(fileName))) {
            target.load(in);
        } catch (IOException ignored) {
        }
    }

    private Config() {
    }

    /**
     * Indicates whether the application should use the LLM for the opponent.
     */
    public static boolean useLLMAgent() {
        return Boolean.parseBoolean(properties.getProperty("useLLMAgent", "false"));
    }

    /**
     * Returns the iteration count used by the MCTS agent.
     */
    public static int mctsIterations() {
        String value = properties.getProperty("mctsIterations", "1000");
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return 1000;
        }
    }


    /**
     * Returns the Gemini API key from {@code gemini.env} or an empty string if
     * none is provided.
     */
    public static String geminiApiKey() {
        return secrets.getProperty("API_KEY", "");
    }
}
