package com.mesozoic.arena;

import com.mesozoic.arena.ai.mcts.GameState;
import com.mesozoic.arena.ai.mcts.MCTSNode;
import com.mesozoic.arena.model.Dinosaur;
import com.mesozoic.arena.model.Move;
import com.mesozoic.arena.model.Player;

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
        MCTSNode root = new MCTSNode(state, null, null);
        Random random = new Random(0);

        MCTSNode child = root.expand(random);
        int result = child.rollout(random);
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
        MCTSNode root = new MCTSNode(state, null, null);
        Random random = new Random(0);

        MCTSNode first = root.expand(random);
        MCTSNode second = root.expand(random);
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
}
