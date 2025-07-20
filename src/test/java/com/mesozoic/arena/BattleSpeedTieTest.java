package com.mesozoic.arena;

import com.mesozoic.arena.engine.Battle;
import com.mesozoic.arena.model.Dinosaur;
import com.mesozoic.arena.model.Move;
import com.mesozoic.arena.model.Player;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BattleSpeedTieTest {

    @Test
    public void testLowHealthPlayerActsFirst() {
        Move strike = new Move("Strike", 10, 0, List.of());
        Dinosaur weaker = new Dinosaur("Weaker", 50, 10,
                "assets/animals/allosaurus.png", 1, 1, List.of(strike), null);
        Dinosaur stronger = new Dinosaur("Stronger", 60, 10,
                "assets/animals/allosaurus.png", 1, 1, List.of(strike), null);
        Player p1 = new Player(List.of(weaker));
        Player p2 = new Player(List.of(stronger));
        Battle battle = new Battle(p1, p2);

        battle.executeRound(strike, strike);

        List<String> log = battle.getEventLog();
        assertFalse(log.isEmpty());
        assertTrue(log.get(0).contains("Player Weaker"));
    }

    @Test
    public void testPlayerOneActsFirstWhenHealthEqual() {
        Move strike = new Move("Strike", 10, 0, List.of());
        Dinosaur first = new Dinosaur("First", 50, 10,
                "assets/animals/allosaurus.png", 1, 1, List.of(strike), null);
        Dinosaur second = new Dinosaur("Second", 50, 10,
                "assets/animals/allosaurus.png", 1, 1, List.of(strike), null);
        Player p1 = new Player(List.of(first));
        Player p2 = new Player(List.of(second));
        Battle battle = new Battle(p1, p2);

        battle.executeRound(strike, strike);

        List<String> log = battle.getEventLog();
        assertFalse(log.isEmpty());
        assertTrue(log.get(0).contains("Player First"));
    }
}
