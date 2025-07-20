package com.mesozoic.arena;

import com.mesozoic.arena.ai.mcts.GameState;
import com.mesozoic.arena.ai.mcts.MCTSNode;
import com.mesozoic.arena.model.Dinosaur;
import com.mesozoic.arena.model.Move;
import com.mesozoic.arena.model.Player;
import com.mesozoic.arena.model.Effect;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class MCTSNodeTest {

    @Test
    public void testRolloutBackpropagateUpdatesScores() {
        Move win = new Move("Win", 10, 0, List.of());
        Move wait = new Move("Wait", 0, 0, List.of());
        Dinosaur attacker = new Dinosaur("Attacker", 10, 5,
                "assets/animals/allosaurus.png", 1, 1,
                List.of(win), null);
        Dinosaur defender = new Dinosaur("Defender", 10, 5,
                "assets/animals/allosaurus.png", 1, 1,
                List.of(wait), null);
        Player p1 = new Player(List.of(defender));
        Player p2 = new Player(List.of(attacker));
        GameState state = new GameState(p1, p2);
        MCTSNode root = new MCTSNode(state, null, null, 0.0);
        Random selectionRandom = new Random(0);
        Random simulationRandom = new Random(1);

        MCTSNode child = root.expand(selectionRandom, simulationRandom);
        int result = child.rollout(simulationRandom);
        child.backpropagate(result);

        assertEquals(1, child.getVisitCount());
        assertEquals(1, root.getVisitCount());
        assertTrue(child.getWinScore() > 0);
    }

    @Test
    public void testBestChildSelectsWinningMove() {
        Move win = new Move("Win", 10, 0, List.of());
        Move lose = new Move("Lose", 0, 0, List.of());
        Move wait = new Move("Wait", 0, 0, List.of());
        Dinosaur attacker = new Dinosaur("Attacker", 10, 5,
                "assets/animals/allosaurus.png", 1, 1,
                List.of(win, lose), null);
        Dinosaur defender = new Dinosaur("Defender", 10, 5,
                "assets/animals/allosaurus.png", 1, 1,
                List.of(wait), null);
        Player p1 = new Player(List.of(defender));
        Player p2 = new Player(List.of(attacker));
        GameState state = new GameState(p1, p2);
        MCTSNode root = new MCTSNode(state, null, null, 0.0);
        Random selectionRandom = new Random(0);
        Random simulationRandom = new Random(1);

        MCTSNode first = root.expand(selectionRandom, simulationRandom);
        MCTSNode second = root.expand(selectionRandom, simulationRandom);
        if ("Win".equals(first.getMove().getName())) {
            first.backpropagate(1);
            second.backpropagate(-1);
        } else {
            first.backpropagate(-1);
            second.backpropagate(1);
        }

        MCTSNode best = root.bestChild();
        assertEquals("Win", best.getMove().getName());
    }

    @Test
    public void testBestChildEpsilonGreedy() {
        Move first = new Move("First", 0, 0, List.of());
        Move second = new Move("Second", 0, 0, List.of());
        Dinosaur attacker = new Dinosaur("Attacker", 10, 5,
                "assets/animals/allosaurus.png", 1, 1,
                List.of(first, second), null);
        Dinosaur defender = new Dinosaur("Defender", 10, 5,
                "assets/animals/allosaurus.png", 1, 1,
                List.of(first), null);
        Player p1 = new Player(List.of(defender));
        Player p2 = new Player(List.of(attacker));
        GameState state = new GameState(p1, p2);
        MCTSNode root = new MCTSNode(state, null, null, 0.0);
        Random selectionRandom = new Random(0);
        Random simulationRandom = new Random(1);

        root.expand(selectionRandom, simulationRandom);
        root.expand(selectionRandom, simulationRandom);

        Random epsilonRandom = new Random(0);
        MCTSNode chosen = root.bestChild(epsilonRandom, 1.0);

        Random expectedRandom = new Random(0);
        expectedRandom.nextDouble();
        int expectedIndex = expectedRandom.nextInt(root.getChildren().size());
        assertEquals(root.getChildren().get(expectedIndex), chosen);
    }

    @Test
    public void testRolloutStopsWithoutWinner() {
        Move waitOne = new Move("Wait", 0, 0, List.of());
        Move waitTwo = new Move("Wait", 0, 0, List.of());
        Dinosaur dinoOne = new Dinosaur("IdleOne", 10, 5,
                "assets/animals/allosaurus.png", 1, 1,
                List.of(waitOne), null);
        Dinosaur dinoTwo = new Dinosaur("IdleTwo", 10, 5,
                "assets/animals/allosaurus.png", 1, 1,
                List.of(waitTwo), null);
        Player p1 = new Player(List.of(dinoOne));
        Player p2 = new Player(List.of(dinoTwo));
        GameState state = new GameState(p1, p2);
        MCTSNode root = new MCTSNode(state, null, null, 0.0);
        Random simulationRandom = new Random(0);

        int result = root.rollout(simulationRandom);
        assertEquals(0, result);
    }

    @Test
    public void testRolloutAppliesBraceHistory() {
        Move brace = new Move("Brace", 0, 0, List.of(new Effect("brace")));
        Move strike = new Move("Strike", 10, 0, List.of());

        Dinosaur attacker = new Dinosaur("Attacker", 20, 5,
                "assets/animals/allosaurus.png", 1, 1,
                List.of(strike), null);
        Dinosaur defender = new Dinosaur("Defender", 20, 5,
                "assets/animals/allosaurus.png", 1, 1,
                List.of(brace), null);
        Player p1 = new Player(List.of(attacker));
        Player p2 = new Player(List.of(defender));
        GameState state = new GameState(p1, p2);
        MCTSNode root = new MCTSNode(state, null, null, 0.0);
        Random simulationRandom = new Random(0);

        int result = root.rollout(simulationRandom);
        assertEquals(-1, result);
    }
}
