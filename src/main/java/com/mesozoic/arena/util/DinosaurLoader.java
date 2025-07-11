package com.mesozoic.arena.util;

import com.mesozoic.arena.model.Dinosaur;
import com.mesozoic.arena.model.Effect;
import com.mesozoic.arena.model.Move;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
        try (InputStream stream = DinosaurLoader.class.getClassLoader()
                .getResourceAsStream("animals.yaml")) {
            if (stream == null) {
                return new ArrayList<>();
            }
            Map<String, Object> data = yaml.load(stream);
            List<Dinosaur> dinosaurs = new ArrayList<>();
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                String name = entry.getKey();
                Map<String, Object> dinoData = castMap(entry.getValue());
                int health = toInt(dinoData.get("health"));
                int speed = toInt(dinoData.get("speed"));
                String image = String.valueOf(dinoData.get("image"));
                List<Map<String, Object>> abilityList = castList(
                        dinoData.get("abilities"));
                List<Move> moves = new ArrayList<>();
                if (abilityList != null) {
                    for (Map<String, Object> ability : abilityList) {
                        String moveName = String.valueOf(ability.get("name"));
                        int damage = toInt(ability.get("damage"));
                        int stamina = toInt(ability.get("stamina"));
                        moves.add(new Move(moveName, damage, stamina,
                                new ArrayList<Effect>()));
                    }
                }
                dinosaurs.add(new Dinosaur(name, health, speed, image, 100,
                        moves));
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

    private static int toInt(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.parseInt(String.valueOf(value));
    }
}
