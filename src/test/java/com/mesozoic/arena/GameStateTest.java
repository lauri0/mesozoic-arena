package com.mesozoic.arena;

import com.mesozoic.arena.ai.mcts.GameState;
import com.mesozoic.arena.engine.Battle;
import com.mesozoic.arena.model.Ability;
import com.mesozoic.arena.model.Dinosaur;
import com.mesozoic.arena.model.Player;

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
}
