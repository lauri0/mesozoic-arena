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
    private static final java.nio.file.Path CONFIG_PATH =
            java.nio.file.Path.of("data", CONFIG_FILE);

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

    private static void saveProperties() {
        try (java.io.OutputStream out = java.nio.file.Files.newOutputStream(CONFIG_PATH)) {
            properties.store(out, null);
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
     * Returns the epsilon value used by the MCTS agent.
     */
    public static double mctsEpsilon() {
        String value = properties.getProperty("mctsEpsilon", "0.1");
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ignored) {
            return 0.1;
        }
    }

    /**
     * Returns the exploration constant used by the MCTS agent.
     */
    public static double mctsExploration() {
        String value = properties.getProperty("mctsExploration", "2.0");
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ignored) {
            return 2.0;
        }
    }

    /**
     * Returns the probability of using a minimax move during rollouts.
     */
    public static double mctsSelfMinimaxProbability() {
        String value = properties.getProperty("mctsSelfMinimaxProbability", "0.5");
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ignored) {
            return 0.5;
        }
    }

    /**
     * Returns the probability of the opponent using a minimax move.
     */
    public static double mctsOpponentMinimaxProbability() {
        String value = properties.getProperty("mctsOpponentMinimaxProbability", "0.75");
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ignored) {
            return 0.75;
        }
    }

    /**
     * Returns the supply budget used when generating random teams.
     */
    public static int supplyBudget() {
        String value = properties.getProperty("supplyBudget", "30");
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return 30;
        }
    }

    /**
     * Updates the supply budget in memory and writes it to {@code data/constants.ini}.
     */
    public static void setSupplyBudget(int budget) {
        properties.setProperty("supplyBudget", Integer.toString(budget));
        saveProperties();
    }


    /**
     * Returns the Gemini API key from {@code gemini.env} or an empty string if
     * none is provided.
     */
    public static String geminiApiKey() {
        return secrets.getProperty("API_KEY", "");
    }
}
