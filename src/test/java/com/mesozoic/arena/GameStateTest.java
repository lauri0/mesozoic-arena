package com.mesozoic.arena;

import com.mesozoic.arena.ai.mcts.GameState;
import com.mesozoic.arena.engine.Battle;
import com.mesozoic.arena.model.Ability;
import com.mesozoic.arena.model.Dinosaur;
import com.mesozoic.arena.model.Player;
import com.mesozoic.arena.model.Move;
import com.mesozoic.arena.model.Effect;
import com.mesozoic.arena.util.PersistentEffectRegistry;
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
        assertEquals(-1, target.getHeadAttackStage());

        GameState state = new GameState(playerOne, playerTwo);
        int stage = state.getPlayerTwo().getActiveDinosaur().getHeadAttackStage();
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
    public void testPersistentEffectDurationPreserved() {
        Dinosaur dino = new Dinosaur("A", 100, 50, "", 1, 1, List.of(), null);
        Player p1 = new Player(List.of(dino));
        p1.addPersistentEffect(PersistentEffectRegistry.createEffect("Tailwind"));
        p1.tickPersistentEffects();
        int remaining = p1.getPersistentEffects().get(0).getRemaining();

        Player p2 = new Player(List.of(dino.copy()));
        GameState state = new GameState(p1, p2);
        int copied = state.getPlayerOne().getPersistentEffects().get(0).getRemaining();
        assertEquals(remaining, copied);
    }

    @Test
    public void testHazardDamageNotRepeated() {
        Dinosaur a = new Dinosaur("A", 100, 50, "", 1, 1, List.of(), null);
        Player p1 = new Player(List.of(a));
        p1.addPersistentEffect(PersistentEffectRegistry.createEffect("Rocks"));
        Dinosaur b = new Dinosaur("B", 100, 50, "", 1, 1, List.of(), null);
        Player p2 = new Player(List.of(b));
        Battle battle = new Battle(p1, p2);
        int healthAfterEntry = p1.getActiveDinosaur().getHealth();

        GameState state = new GameState(p1, p2);
        int healthInState = state.getPlayerOne().getActiveDinosaur().getHealth();
        assertEquals(healthAfterEntry, healthInState);
    }
}
