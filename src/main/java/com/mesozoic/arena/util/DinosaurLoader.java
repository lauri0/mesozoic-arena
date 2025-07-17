package com.mesozoic.arena.util;

import com.mesozoic.arena.model.Dinosaur;
import com.mesozoic.arena.model.Effect;
import com.mesozoic.arena.model.Move;
import com.mesozoic.arena.model.MoveType;
import com.mesozoic.arena.model.DinoType;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.yaml.snakeyaml.Yaml;

/**
 * Utility for loading dinosaur definitions from the bundled YAML file.
 */
public final class DinosaurLoader {

    private DinosaurLoader() {
    }

    /**
     * Loads all dinosaurs defined in {@code animals.yaml}.
     */
    public static List<Dinosaur> load() {
        Yaml yaml = new Yaml();
        try (InputStream moveStream = DinosaurLoader.class.getClassLoader()
                .getResourceAsStream("moves.yaml");
                InputStream dinoStream = DinosaurLoader.class.getClassLoader()
                        .getResourceAsStream("animals.yaml")) {
            if (moveStream == null || dinoStream == null) {
                return new ArrayList<>();
            }
            Map<String, Object> moveData = yaml.load(moveStream);
            Map<String, Move> moves = new HashMap<>();
            for (Entry<String, Object> entry : moveData.entrySet()) {
                String name = entry.getKey();
                Map<String, Object> val = castMap(entry.getValue());
                int damage = toInt(val.get("damage"));
                int priority = toInt(val.getOrDefault("priority", 0));
                String description = String.valueOf(val.getOrDefault("description", ""));
                String kindLabel = String.valueOf(val.getOrDefault("kind", "body"));
                MoveType kind = "head".equalsIgnoreCase(kindLabel) ? MoveType.HEAD : MoveType.BODY;
                DinoType type = DinoType.fromString(String.valueOf(val.getOrDefault("type", "Biter")));
                double accuracy = Double.parseDouble(String.valueOf(val.getOrDefault("accuracy", 1.0)));
                List<Effect> effects = new ArrayList<>();
                List<?> eff = castObjectList(val.get("effects"));
                if (eff != null) {
                    for (Object obj : eff) {
                        if (obj != null) {
                            effects.add(new Effect(String.valueOf(obj)));
                        }
                    }
                }
                moves.put(name, new Move(name, damage, priority, description, kind, type, effects, accuracy));
            }

            Map<String, Object> data = yaml.load(dinoStream);
            List<Dinosaur> dinosaurs = new ArrayList<>();
            for (Entry<String, Object> entry : data.entrySet()) {
                String name = entry.getKey();
                Map<String, Object> dinoData = castMap(entry.getValue());
                int health = toInt(dinoData.get("health"));
                int speed = toInt(dinoData.get("speed"));
                int defaultAttack = toInt(dinoData.getOrDefault("attack", 10));
                int headAttack = toInt(dinoData.getOrDefault("head attack", defaultAttack));
                int bodyAttack = toInt(dinoData.getOrDefault("body attack", defaultAttack));
                String image = String.valueOf(dinoData.get("image"));
                List<?> moveNames = castObjectList(dinoData.get("moves"));
                List<Move> dinoMoves = new ArrayList<>();
                if (moveNames != null) {
                    for (Object n : moveNames) {
                        Move m = moves.get(String.valueOf(n));
                        if (m != null) {
                            dinoMoves.add(new Move(m.getName(), m.getDamage(),
                                    m.getPriority(), m.getDescription(), m.getKind(), m.getType(),
                                    m.getEffects(), m.getAccuracy()));
                        }
                    }
                }

                List<DinoType> types = new ArrayList<>();
                Object rawTypes = dinoData.get("types");
                if (rawTypes instanceof List<?> list) {
                    for (Object obj : list) {
                        types.add(DinoType.fromString(String.valueOf(obj)));
                    }
                } else if (rawTypes != null) {
                    types.add(DinoType.fromString(String.valueOf(rawTypes)));
                }

                dinosaurs.add(new Dinosaur(name, health, speed, image, headAttack, bodyAttack,
                        dinoMoves, null, types));
            }
            return dinosaurs;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> castMap(Object value) {
        return (Map<String, Object>) value;
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> castList(Object value) {
        return (List<Map<String, Object>>) value;
    }

    @SuppressWarnings("unchecked")
    private static List<Object> castObjectList(Object value) {
        return (List<Object>) value;
    }

    private static int toInt(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.parseInt(String.valueOf(value));
    }
}
