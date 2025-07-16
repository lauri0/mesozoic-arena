package com.mesozoic.arena.data;

import com.mesozoic.arena.model.Dinosaur;
import com.mesozoic.arena.model.Effect;
import com.mesozoic.arena.model.Move;
import com.mesozoic.arena.model.MoveType;
import com.mesozoic.arena.model.Ability;
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
    private static final String ABILITY_FILE = "data/abilities.yaml";
    private static final String IMAGE_DIR = "assets/animals";

    private final List<Dinosaur> availableDinosaurs;
    private final Map<String, Move> moveTemplates;
    private final Map<String, Ability> abilityTemplates;

    /**
     * Parses the YAML data file at construction time.
     */
    public DinosaurLoader() throws IOException {
        moveTemplates = loadMoves();
        abilityTemplates = loadAbilities();
        availableDinosaurs = Collections.unmodifiableList(loadDinosaurs());
    }

    /**
     * Returns a new player containing three randomly selected dinosaurs.
     * Selections will not include duplicates.
     */
    public Player createRandomPlayer() {
        return new Player(selectRandomDinosaurs(3));
    }

    private List<Dinosaur> selectRandomDinosaurs(int count) {
        List<Dinosaur> shuffledDinosaurs = new ArrayList<>(availableDinosaurs);
        Collections.shuffle(shuffledDinosaurs, new Random());
        List<Dinosaur> selection = new ArrayList<>();
        int limit = Math.min(count, shuffledDinosaurs.size());
        for (int index = 0; index < limit; index++) {
            Dinosaur template = shuffledDinosaurs.get(index);
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
        double defaultAttack = ((Number) values.getOrDefault("attack", 1)).doubleValue();
        double headAttack = ((Number) values.getOrDefault("head attack", defaultAttack)).doubleValue();
        double bodyAttack = ((Number) values.getOrDefault("body attack", defaultAttack)).doubleValue();
        String imagePath = (String) values.get("image");
        validateImageExists(imagePath);

        @SuppressWarnings("unchecked")
        List<String> moveNames = (List<String>) values.get("moves");
        List<Move> moves = resolveMoves(moveNames);

        String abilityName = String.valueOf(values.getOrDefault("ability", "None"));
        Ability ability = abilityTemplates.get(abilityName);

        return new Dinosaur(name, health, speed, imagePath, 100, headAttack, bodyAttack, moves, ability);
    }

    private List<Move> resolveMoves(List<String> names) {
        List<Move> moves = new ArrayList<>();
        if (names != null) {
            for (String moveName : names) {
                Move template = moveTemplates.get(moveName);
                if (template != null) {
                    moves.add(new Move(template.getName(), template.getDamage(),
                            template.getStaminaChange(), template.getPriority(),
                            template.getDescription(), template.getEffects()));
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
                    move.getStaminaChange(), move.getPriority(),
                    move.getDescription(), move.getEffects()));
        }
        return new Dinosaur(source.getName(), source.getHealth(), source.getSpeed(), source.getImagePath(),
                source.getStamina(), source.getHeadAttack(), source.getBodyAttack(), copiedMoves, source.getAbility());
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
                    String description = String.valueOf(values.getOrDefault("description", ""));
                    String typeLabel = String.valueOf(values.getOrDefault("type", "body"));
                    MoveType type = "head".equalsIgnoreCase(typeLabel) ? MoveType.HEAD : MoveType.BODY;
                    List<Effect> effects = parseEffects(values.get("effects"));
                    map.put(name, new Move(name, damage, stamina, priority, description, type, effects));
                }
            }
        }
        return map;
    }

    private Map<String, Ability> loadAbilities() throws IOException {
        Map<String, Ability> map = new HashMap<>();
        Yaml yaml = new Yaml();
        Path path = Paths.get(ABILITY_FILE);
        try (InputStream input = Files.newInputStream(path)) {
            Map<String, Object> root = yaml.load(input);
            if (root != null) {
                for (Entry<String, Object> entry : root.entrySet()) {
                    String name = entry.getKey();
                    String description = entry.getValue() == null ? "" : String.valueOf(entry.getValue());
                    map.put(name, new Ability(name, description));
                }
            }
        }
        return map;
    }
}
