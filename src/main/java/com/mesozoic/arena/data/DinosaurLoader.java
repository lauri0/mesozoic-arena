package com.mesozoic.arena.data;

import com.mesozoic.arena.model.Dinosaur;
import com.mesozoic.arena.model.Effect;
import com.mesozoic.arena.model.Move;
import com.mesozoic.arena.model.Player;
import org.yaml.snakeyaml.Yaml;
import java.util.HashMap;
import java.util.Map.Entry;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Loads dinosaur definitions from {@code data/animals.yaml} and provides helper
 * methods for generating random player teams.
 */
public class DinosaurLoader {
    private static final String DATA_FILE = "data/animals.yaml";
    private static final String MOVE_FILE = "data/moves.yaml";
    private static final String IMAGE_DIR = "assets/animals";

    private final List<Dinosaur> availableDinosaurs;
    private final Map<String, Move> moveTemplates;

    /**
     * Parses the YAML data file at construction time.
     */
    public DinosaurLoader() throws IOException {
        moveTemplates = loadMoves();
        availableDinosaurs = Collections.unmodifiableList(loadDinosaurs());
    }

    /**
     * Returns a new player containing three randomly selected dinosaurs.
     * Duplicate selections are allowed.
     */
    public Player createRandomPlayer() {
        return new Player(selectRandomDinosaurs(3));
    }

    private List<Dinosaur> selectRandomDinosaurs(int count) {
        Random random = new Random();
        List<Dinosaur> selection = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Dinosaur template = availableDinosaurs.get(random.nextInt(availableDinosaurs.size()));
            selection.add(copyDinosaur(template));
        }
        return selection;
    }

    private List<Dinosaur> loadDinosaurs() throws IOException {
        List<Dinosaur> dinosaurs = new ArrayList<>();
        Yaml yaml = new Yaml();
        Path path = Paths.get(DATA_FILE);
        try (InputStream input = Files.newInputStream(path)) {
            Map<String, Object> root = yaml.load(input);
            if (root != null) {
                for (Entry<String, Object> entry : root.entrySet()) {
                    String name = entry.getKey();
                    @SuppressWarnings("unchecked")
                    Map<String, Object> values = (Map<String, Object>) entry.getValue();
                    dinosaurs.add(parseDinosaur(name, values));
                }
            }
        }
        return dinosaurs;
    }

    private Dinosaur parseDinosaur(String name, Map<String, Object> values) throws IOException {
        int health = ((Number) values.get("health")).intValue();
        int speed = ((Number) values.get("speed")).intValue();
        int attack = ((Number) values.getOrDefault("attack", 10)).intValue();
        String imagePath = (String) values.get("image");
        validateImageExists(imagePath);

        @SuppressWarnings("unchecked")
        List<String> moveNames = (List<String>) values.get("moves");
        List<Move> moves = resolveMoves(moveNames);

        return new Dinosaur(name, health, speed, imagePath, 100, attack, moves);
    }

    private List<Move> resolveMoves(List<String> names) {
        List<Move> moves = new ArrayList<>();
        if (names != null) {
            for (String moveName : names) {
                Move template = moveTemplates.get(moveName);
                if (template != null) {
                    moves.add(new Move(template.getName(), template.getDamage(),
                            template.getStaminaChange(), template.getPriority(),
                            template.getEffects()));
                }
            }
        }
        return moves;
    }

    @SuppressWarnings("unchecked")
    private List<Effect> parseEffects(Object raw) {
        List<Effect> list = new ArrayList<>();
        if (raw instanceof List<?> items) {
            for (Object item : items) {
                if (item != null) {
                    list.add(new Effect(String.valueOf(item)));
                }
            }
        }
        return list;
    }

    private void validateImageExists(String path) throws IOException {
        Path image = Paths.get(path);
        if (!Files.exists(image)) {
            throw new IOException("Missing image file: " + path);
        }
    }

    private Dinosaur copyDinosaur(Dinosaur source) {
        List<Move> copiedMoves = new ArrayList<>();
        for (Move move : source.getMoves()) {
            copiedMoves.add(new Move(move.getName(), move.getDamage(),
                    move.getStaminaChange(), move.getPriority(), move.getEffects()));
        }
        return new Dinosaur(source.getName(), source.getHealth(), source.getSpeed(), source.getImagePath(),
                source.getStamina(), source.getAttack(), copiedMoves);
    }

    private Map<String, Move> loadMoves() throws IOException {
        Map<String, Move> map = new HashMap<>();
        Yaml yaml = new Yaml();
        Path path = Paths.get(MOVE_FILE);
        try (InputStream input = Files.newInputStream(path)) {
            Map<String, Object> root = yaml.load(input);
            if (root != null) {
                for (Entry<String, Object> entry : root.entrySet()) {
                    String name = entry.getKey();
                    @SuppressWarnings("unchecked")
                    Map<String, Object> values = (Map<String, Object>) entry.getValue();
                    int damage = ((Number) values.getOrDefault("damage", 0)).intValue();
                    int stamina = ((Number) values.getOrDefault("stamina", 0)).intValue();
                    int priority = ((Number) values.getOrDefault("priority", 0)).intValue();
                    List<Effect> effects = parseEffects(values.get("effects"));
                    map.put(name, new Move(name, damage, stamina, priority, effects));
                }
            }
        }
        return map;
    }
}
