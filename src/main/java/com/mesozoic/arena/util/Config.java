package com.mesozoic.arena.util;

import java.io.IOException;
import java.io.InputStream;
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
        try (InputStream in = Config.class.getClassLoader()
                .getResourceAsStream(CONFIG_FILE)) {
            if (in != null) {
                properties.load(in);
            }
        } catch (IOException ignored) {
        }

        try (InputStream in = Config.class.getClassLoader()
                .getResourceAsStream(GEMINI_ENV_FILE)) {
            if (in != null) {
                secrets.load(in);
            }
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
     * Returns the Gemini API key from {@code gemini.env} or an empty string if
     * none is provided.
     */
    public static String geminiApiKey() {
        return secrets.getProperty("API_KEY", "");
    }
}
