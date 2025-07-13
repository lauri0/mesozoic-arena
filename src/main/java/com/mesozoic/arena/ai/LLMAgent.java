package com.mesozoic.arena.ai;

import com.mesozoic.arena.model.Dinosaur;
import com.mesozoic.arena.model.Move;
import com.mesozoic.arena.util.Config;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Opponent powered by the Gemini Flash API.
 */
public class LLMAgent implements OpponentAgent, AutoCloseable {

    private static final String API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private String lastResponse;

    @Override
    public Move chooseMove(Dinosaur self, Dinosaur enemy) {
        if (self == null || enemy == null) {
            return null;
        }

        String prompt = buildPrompt(self, enemy);
        System.out.println(prompt);
        try {
            String output = sendPrompt(prompt);
            lastResponse = output;
            System.out.println("LLM response: " + output);
            if (output.isEmpty()) {
                System.out.println("LLM response: No response");
            }
            return parseMove(output, self);
        } catch (Exception e) {
            e.printStackTrace();
            lastResponse = "";
            return new RandomOpponent().chooseMove(self, enemy);
        }
    }

    private String sendPrompt(String prompt) throws IOException, InterruptedException {
        String apiKey = Config.geminiApiKey();
        if (apiKey.isBlank()) {
            System.err.println("Gemini API key missing. Provide it in gemini.env");
            return "";
        }

        String escaped = escapeJson(prompt);
        String body = "{\"contents\":[{\"parts\":[{\"text\":\"" + escaped + "\"}]}]}";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "?key=" + apiKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return extractText(response.body());
    }

    private String extractText(String json) {
        Matcher matcher = Pattern.compile("\\\"text\\\"\\s*:\\s*\\\"(.*?)\\\"",
                Pattern.DOTALL).matcher(json);
        if (matcher.find()) {
            return matcher.group(1).replace("\\n", "\n");
        }
        return "";
    }

    private String escapeJson(String text) {
        return text.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private Move parseMove(String output, Dinosaur self) {
        String lowerCase = output.toLowerCase();
        for (Move move : self.getMoves()) {
            if (lowerCase.contains(move.getName().toLowerCase()) &&
                    self.getStamina() >= move.getStaminaCost()) {
                return move;
            }
        }
        return new RandomOpponent().chooseMove(self, null);
    }

    private String buildPrompt(Dinosaur self, Dinosaur enemy) {
        String moveList = self.getMoves().stream()
                .map(Move::getName)
                .collect(Collectors.joining(", "));
        return "Choose the best move for " + self.getName() +
                " against " + enemy.getName() + ". Available moves: " +
                moveList + ".\nMove:";
    }

    public String getLastResponse() {
        return lastResponse;
    }

    @Override
    public void close() {
        // nothing to close
    }
}
