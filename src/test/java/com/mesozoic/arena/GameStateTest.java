package com.mesozoic.arena;

import com.mesozoic.arena.ai.mcts.GameState;
import com.mesozoic.arena.engine.Battle;
import com.mesozoic.arena.model.Ability;
import com.mesozoic.arena.model.Dinosaur;
import com.mesozoic.arena.model.Player;
import com.mesozoic.arena.model.Move;
import com.mesozoic.arena.model.Effect;
import com.mesozoic.arena.model.SwitchMove;
import java.util.Random;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GameStateTest {

    @Test
    public void testIntimidateEffectNotStacked() {
        Dinosaur intimidator = new Dinosaur(
                "Intimidator", 100, 50, "assets/animals/allosaurus.png",
                10, 10, List.of(), new Ability("Intimidate", ""));
        Dinosaur target = new Dinosaur(
                "Target", 100, 50, "assets/animals/allosaurus.png",
                10, 10, List.of(), null);
        Player playerOne = new Player(List.of(intimidator));
        Player playerTwo = new Player(List.of(target));
        new Battle(playerOne, playerTwo);
        assertEquals(-1, target.getAttackStage());

        GameState state = new GameState(playerOne, playerTwo);
        int stage = state.getPlayerTwo().getActiveDinosaur().getAttackStage();
        assertEquals(-1, stage);
    }

    @Test
    public void testAvailableMovesIncludeSwitch() {
        Dinosaur first = new Dinosaur("First", 100, 50,
                "assets/animals/allosaurus.png", 10, 10, List.of(), null);
        Dinosaur second = new Dinosaur("Second", 100, 50,
                "assets/animals/allosaurus.png", 10, 10, List.of(), null);
        Player playerOne = new Player(List.of(first, second));
        Player playerTwo = new Player(List.of(first.copy()));

        GameState state = new GameState(playerOne, playerTwo);
        boolean hasSwitch = state.availableMovesFor(state.getPlayerOne())
                .stream()
                .anyMatch(m -> m.getName().contains("Switch to Second"));
        assertTrue(hasSwitch);
    }

    @Test
    public void testNextStatePerformsSwitch() {
        Dinosaur first = new Dinosaur("First", 100, 50,
                "assets/animals/allosaurus.png", 10, 10, List.of(), null);
        Dinosaur second = new Dinosaur("Second", 100, 50,
                "assets/animals/allosaurus.png", 10, 10, List.of(), null);
        Player playerOne = new Player(List.of(first, second));
        Player playerTwo = new Player(List.of(first.copy()));

        GameState state = new GameState(playerOne, playerTwo);
        Move switchMove = state.availableMovesFor(state.getPlayerOne())
                .stream()
                .filter(m -> m.getName().contains("Switch to Second"))
                .findFirst()
                .orElseThrow();
        GameState next = state.nextState(switchMove, null, new Random(0));
        assertEquals("Second", next.getPlayerOne().getActiveDinosaur().getName());
    }

    @Test
    public void testBraceFailsWhenRepeated() {
        Move brace = new Move("Brace", 0, 0, List.of(new Effect("brace")));
        Move strike = new Move("Strike", 10, 0, List.of());
        Dinosaur defender = new Dinosaur("Defender", 100, 50,
                "assets/animals/allosaurus.png", 1, 1, List.of(brace), null);
        Dinosaur attacker = new Dinosaur("Attacker", 100, 50,
                "assets/animals/allosaurus.png", 1, 1, List.of(strike), null);
        Player p1 = new Player(List.of(defender));
        Player p2 = new Player(List.of(attacker));

        GameState state = new GameState(p1, p2);
        state = state.nextState(brace, strike, new Random(0));
        assertEquals(100, state.getPlayerOne().getActiveDinosaur().getHealth());

        GameState next = state.nextState(brace, strike, new Random(0));
        assertTrue(next.getPlayerOne().getActiveDinosaur().getHealth() < 100);
    }

    @Test
    public void testNextStateIgnoresInvalidSwitch() {
        Dinosaur first = new Dinosaur("Solo", 100, 50,
                "assets/animals/allosaurus.png", 10, 10, List.of(), null);
        Player playerOne = new Player(List.of(first));
        Player playerTwo = new Player(List.of(first.copy()));

        GameState state = new GameState(playerOne, playerTwo);
        Move invalid = new SwitchMove(first, 1);

        GameState next = state.nextState(invalid, null, new Random(0));
        assertEquals("Solo", next.getPlayerOne().getActiveDinosaur().getName());
    }
}
