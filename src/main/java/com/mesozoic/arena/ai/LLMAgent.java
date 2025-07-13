package com.mesozoic.arena.ai;

import com.mesozoic.arena.model.Dinosaur;
import com.mesozoic.arena.model.Move;
import com.mesozoic.arena.model.Player;
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
    public Move chooseMove(Player self, Player enemy) {
        Dinosaur activeSelf = self.getActiveDinosaur();
        Dinosaur activeEnemy = enemy.getActiveDinosaur();
        if (activeSelf == null || activeEnemy == null) {
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

    private Move parseMove(String output, Player selfPlayer) {
        String answer = extractAnswer(output);
        if (answer != null) {
            String lower = answer.toLowerCase();
            if (trySwitch(lower, selfPlayer)) {
                return null;
            }
            Move move = findNamedMove(answer, selfPlayer.getActiveDinosaur());
            if (move != null) {
                return move;
            }
        }

        String lowerCase = output.toLowerCase();
        if (trySwitch(lowerCase, selfPlayer)) {
            return null;
        }
        Move move = findMoveInText(lowerCase, selfPlayer.getActiveDinosaur());
        if (move != null) {
            return move;
        }
        return new RandomOpponent().chooseMove(selfPlayer, null);
    }

    private String extractAnswer(String text) {
        Matcher matcher = Pattern.compile("(?i)answer:\\s*(.+)").matcher(text);
        if (matcher.find()) {
            String result = matcher.group(1).trim();
            int newline = result.indexOf('\n');
            if (newline >= 0) {
                result = result.substring(0, newline).trim();
            }
            return result;
        }
        return null;
    }

    private Move findNamedMove(String name, Dinosaur active) {
        if (active == null) {
            return null;
        }
        for (Move move : active.getMoves()) {
            if (move.getName().equalsIgnoreCase(name) &&
                    active.getStamina() >= move.getStaminaChange()) {
                return move;
            }
        }
        return null;
    }

    private Move findMoveInText(String text, Dinosaur active) {
        if (active == null) {
            return null;
        }
        for (Move move : active.getMoves()) {
            if (text.contains(move.getName().toLowerCase()) &&
                    active.getStamina() >= move.getStaminaChange()) {
                return move;
            }
        }
        return null;
    }

    private boolean trySwitch(String text, Player selfPlayer) {
        if (!text.contains("switch")) {
            return false;
        }
        for (Dinosaur dino : selfPlayer.getDinosaurs()) {
            if (dino.equals(selfPlayer.getActiveDinosaur())) {
                continue;
            }
            if (text.contains(dino.getName().toLowerCase())) {
                selfPlayer.queueSwitch(dino);
                return true;
            }
        }
        return false;
    }

    private String formatMoves(Dinosaur dino) {
        return dino.getMoves().stream()
                .map(m -> m.getName() + " (" + m.getDamage() + " dmg, "
                        + m.getStaminaChange() + " sta, " + m.getPriority() + " prio)")
                .collect(Collectors.joining(", "));
    }

    private String buildPrompt(Player selfPlayer, Player enemyPlayer) {
        Dinosaur self = selfPlayer.getActiveDinosaur();
        Dinosaur enemy = enemyPlayer.getActiveDinosaur();
        String selfMoves = formatMoves(self);
        String enemyMoves = formatMoves(enemy);

        String bench = selfPlayer.getDinosaurs().stream()
                .filter(d -> !d.equals(self))
                .map(Dinosaur::getName)
                .collect(Collectors.joining(", "));

        return "You are playing Mesozoic Arena, a turn based dinosaur battle. " +
                "The dinosaur with more speed goes first. A dinosaur using a higher priority move goes before " +
                "dinosaurs using lower priority moves regardless of speed. " +
                "Using a move changes your dinosaur's available stamina and deals damage equal to that move. " +
                "You may also switch dinosaurs, which happens before attacks but " +
                "skips using a move.\n" +
                "Your dinosaur: " + self.getName() + " (HP: " + self.getHealth() +
                ", Stamina: " + self.getStamina() + ", Speed: " + self.getSpeed() + ")\n" +
                "Opponent dinosaur: " + enemy.getName() + " (HP: " + enemy.getHealth() +
                ", Stamina: " + enemy.getStamina() + ", Speed: " + enemy.getSpeed() + ")\n" +
                "Your moves (sta refers to stamina change when using the move, which can be positive or negative): " + selfMoves + "\n" +
                "Opponent moves: " + enemyMoves + "\n" +
                "You can also switch to: " + bench + ".\n" +
                "Respond with the move name to attack or 'Switch to <name>' to switch. " +
                "End your response with 'Answer: <move>' or 'Answer: Switch to <name>'.\nAction:";
    }

    public String getLastResponse() {
        return lastResponse;
    }

    @Override
    public void close() {
        // nothing to close
    }
}
