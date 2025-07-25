package com.mesozoic.arena;

import com.mesozoic.arena.model.Dinosaur;
import com.mesozoic.arena.model.Ability;
import com.mesozoic.arena.model.Player;
import com.mesozoic.arena.engine.Battle;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

public class DinosaurTest {
    @Test
    public void testStageAffectsStats() {
        Dinosaur dino = new Dinosaur("Test", 100, 50, "assets/animals/allosaurus.png", 10, 15, List.of(), null);
        assertEquals(10, dino.getEffectiveHeadAttack());
        assertEquals(15, dino.getEffectiveBodyAttack());
        assertEquals(50, dino.getEffectiveSpeed());
        dino.adjustHeadAttackStage(1);
        dino.adjustBodyAttackStage(-1);
        dino.adjustSpeedStage(-1);
        assertEquals(15, Math.round(dino.getEffectiveHeadAttack()));
        assertEquals(10, Math.round(dino.getEffectiveBodyAttack()));
        assertEquals(33, dino.getEffectiveSpeed());
    }

    @Test
    public void testStagesResetOnBench() {
        Dinosaur first = new Dinosaur("First", 100, 50, "assets/animals/allosaurus.png", 10, 10, List.of(), null);
        Dinosaur second = new Dinosaur("Second", 100, 50, "assets/animals/allosaurus.png", 10, 10, List.of(), null);
        Player player = new Player(List.of(first, second));
        first.adjustHeadAttackStage(2);
        first.adjustSpeedStage(-2);
        player.setActiveDinosaur(second);
        assertEquals(0, first.getHeadAttackStage());
        assertEquals(0, first.getSpeedStage());
    }

    @Test
    public void testIntimidateLowersOpponentAttack() {
        Ability intimidate = new Ability("Intimidate", "");
        Dinosaur intimidator = new Dinosaur("Intimidator", 100, 50, "assets/animals/allosaurus.png", 10, 10, List.of(), intimidate);
        Dinosaur target = new Dinosaur("Target", 100, 50, "assets/animals/allosaurus.png", 10, 10, List.of(), null);
        Player p1 = new Player(List.of(intimidator));
        Player p2 = new Player(List.of(target));
        new Battle(p1, p2); // ability triggers on battle start
        assertEquals(-1, target.getHeadAttackStage());
    }
}
