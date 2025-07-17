package com.mesozoic.arena.ai;

import com.mesozoic.arena.model.Dinosaur;
import com.mesozoic.arena.model.Move;
import com.mesozoic.arena.model.Player;
import com.mesozoic.arena.model.Effect;
import com.mesozoic.arena.model.DinoType;
import com.mesozoic.arena.engine.TurnRecord;
import com.mesozoic.arena.util.Config;
import com.mesozoic.arena.util.EffectLoader;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
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
    public Move chooseMove(Player self, Player enemy, List<TurnRecord> history) {
        Dinosaur activeSelf = self.getActiveDinosaur();
        Dinosaur activeEnemy = enemy.getActiveDinosaur();
        if (activeSelf == null || activeEnemy == null) {
            return null;
        }

        String prompt = buildPrompt(self, enemy, history);
        System.out.println(prompt);
        try {
            String output = sendPrompt(prompt);
            lastResponse = output;
            System.out.println("LLM response: " + output);
            if (output.isEmpty()) {
                System.out.println("LLM response: No response");
            }
            return parseMove(output, self, history);
        } catch (Exception e) {
            e.printStackTrace();
            lastResponse = "";
            return new RandomOpponent().chooseMove(self, enemy, history);
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

    private Move parseMove(String output, Player selfPlayer, List<TurnRecord> history) {
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
        return new RandomOpponent().chooseMove(selfPlayer, null, history);
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
            if (move.getName().equalsIgnoreCase(name)) {
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
            if (text.contains(move.getName().toLowerCase())) {
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
                .map(m -> m.getName() + ": " + m.getDescriptionWithDamage(dino))
                .collect(Collectors.joining("\n"));
    }

    private String describeDinosaurStatsAndAbility(Dinosaur dino) {
        return dino.getName() + " (HP: " + dino.getHealth() + ", Speed: "
                + dino.getEffectiveSpeed() + ", Ability: "
                + dino.getAbility().getDescription() + ").\n";
    }

    private String formatTypeChart() {
        StringBuilder chart = new StringBuilder("Type chart:\n");
        for (DinoType defendingType : DinoType.values()) {
            chart.append(defendingType.name()).append(": weak to ")
                    .append(formatTypeList(defendingType, 2.0))
                    .append(", resists ")
                    .append(formatTypeList(defendingType, 0.5))
                    .append("\n");
        }
        return chart.toString();
    }

    private String formatTypeList(DinoType defendingType, double multiplier) {
        List<String> matches = new ArrayList<>();
        for (DinoType attackType : DinoType.values()) {
            if (defendingType.getMultiplierFrom(attackType) == multiplier) {
                matches.add(attackType.name());
            }
        }
        return matches.isEmpty() ? "none" : String.join(", ", matches);
    }

    private String buildPrompt(Player selfPlayer, Player enemyPlayer, List<TurnRecord> history) {
        return "You are playing Mesozoic Arena, a turn based dinosaur battle game. " +
                "The dinosaur with more speed goes first. A dinosaur using a higher priority move goes before " +
                "dinosaurs using lower priority moves regardless of speed. " +
                "You may also switch your active dinosaur, which happens before moves but skips using a move. " +
                "The player who knocks out all of the opponent's dinosaurs first wins the match. " +
                formatTypeChart() +
                getActiveDinosaurInfos(selfPlayer, enemyPlayer) +
                getInactiveDinosaurInfos(selfPlayer, enemyPlayer) +
                formatHistory(history, 2) +
                "Respond with the move name to attack or 'Switch to <name>' to switch. " +
                "End your response with 'Answer: <move>' or 'Answer: Switch to <name>'.\nAction:";
    }

    private StringBuilder getActiveDinosaurInfos(Player selfPlayer, Player enemyPlayer) {
        Dinosaur ownActiveDino = selfPlayer.getActiveDinosaur();
        Dinosaur opponentActiveDino = enemyPlayer.getActiveDinosaur();
        StringBuilder activeInfos = new StringBuilder();
        activeInfos.append("Your active dinosaur: ").append(describeDinosaurStatsAndAbility(ownActiveDino));
        activeInfos.append("Your possible moves:\n").append(formatMoves(ownActiveDino)).append("\n");
        activeInfos.append("Opponent's active dinosaur: ").append(describeDinosaurStatsAndAbility(opponentActiveDino));
        activeInfos.append("Their possible moves:\n").append(formatMoves(opponentActiveDino)).append("\n");
        return activeInfos;
    }

    private StringBuilder getInactiveDinosaurInfos(Player selfPlayer, Player enemyPlayer) {
        StringBuilder allInfos = new StringBuilder();
        allInfos.append("Your inactive dinosaurs which you could switch into your active slot:\n");
        for (Dinosaur dinosaur : selfPlayer.getDinosaurs()) {
            if (dinosaur.equals(selfPlayer.getActiveDinosaur())) {
                continue;
            }
            allInfos.append("Your ").append(describeDinosaurStatsAndAbility(dinosaur));
        }
        allInfos.append("Your opponent's inactive dinosaurs which they could switch into their active slot:\n");
        for (Dinosaur dinosaur : enemyPlayer.getDinosaurs()) {
            if (dinosaur.equals(enemyPlayer.getActiveDinosaur())) {
                continue;
            }
            allInfos.append("Opponent's ").append(describeDinosaurStatsAndAbility(dinosaur));
        }
        return allInfos;
    }

    private String formatHistory(List<TurnRecord> history, int queriedHistoryTurns) {
        if (history.isEmpty()) {
            return "";
        }
        int actualHistoryTurns = Math.max(history.size(), queriedHistoryTurns);
        StringBuilder info = new StringBuilder("History of the last " + actualHistoryTurns + " turns:\n");
        int start = Math.max(0, history.size() - queriedHistoryTurns);
        for (int i = start; i < history.size(); i++) {
            TurnRecord rec = history.get(i);
            info.append("Turn ").append(i + 1).append(": You ")
                    .append(rec.getNpcAction()).append(", Opponent ")
                    .append(rec.getPlayerAction()).append("\n");
        }
        return info.toString();
    }

    public String getLastResponse() {
        return lastResponse;
    }

    @Override
    public void close() {
        // nothing to close
    }
}
