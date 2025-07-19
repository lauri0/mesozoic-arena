package com.mesozoic.arena.ai.mcts;

import com.mesozoic.arena.ai.OpponentAgent;
import com.mesozoic.arena.model.Move;
import com.mesozoic.arena.model.Player;
import com.mesozoic.arena.model.SwitchMove;
import com.mesozoic.arena.model.Dinosaur;
import com.mesozoic.arena.engine.TurnRecord;

import java.util.List;
import java.util.Random;

/**
 * Opponent controlled by Monte Carlo Tree Search.
 */
public class MCTSAgent implements OpponentAgent {
    private final int iterations;
    private final Random random;
    private String lastStats = "";

    public MCTSAgent(int iterations, Random random) {
        this.iterations = iterations;
        this.random = random;
    }

    @Override
    public Move chooseMove(Player self, Player enemy, List<TurnRecord> history) {
        if (self == null || self.getActiveDinosaur() == null) {
            return null;
        }

        GameState rootState = new GameState(enemy, self, history);
        MCTSNode root = new MCTSNode(rootState, null, null);

        for (int i = 0; i < iterations; i++) {
            MCTSNode node = root;
            while (node.isFullyExpanded() && !node.getChildren().isEmpty()) {
                node = node.bestChild();
            }
            if (!node.getState().isTerminal()) {
                if (!node.isFullyExpanded()) {
                    node = node.expand(random);
                }
            }
            int result = node.rollout(random);
            node.backpropagate(result);
        }

        StringBuilder summary = new StringBuilder();
        for (MCTSNode child : root.getChildren()) {
            if (child.getVisitCount() == 0) {
                continue;
            }
            double average = child.getWinScore() / child.getVisitCount();
            if (summary.length() > 0) {
                summary.append("\n");
            }
            summary.append(child.getMove().getName())
                    .append(": ")
                    .append(child.getVisitCount())
                    .append(" visits, avg score ")
                    .append(String.format("%.2f", average));
        }
        lastStats = summary.toString();

        MCTSNode bestChild = null;
        int highestVisits = -1;
        for (MCTSNode child : root.getChildren()) {
            if (child.getVisitCount() > highestVisits) {
                highestVisits = child.getVisitCount();
                bestChild = child;
            }
        }

        if (bestChild == null) {
            List<Move> moves = self.getActiveDinosaur().getMoves();
            if (moves.isEmpty()) {
                return null;
            }
            return moves.get(random.nextInt(moves.size()));
        }

        Move chosen = bestChild.getMove();
        if (chosen instanceof SwitchMove switchMove) {
            List<Dinosaur> dinos = self.getDinosaurs();
            if (switchMove.getTargetIndex() >= 0
                    && switchMove.getTargetIndex() < dinos.size()) {
                self.queueSwitch(dinos.get(switchMove.getTargetIndex()));
            }
            return null;
        }

        return chosen;
    }

    /**
     * Returns a summary of statistics for the most recent search.
     */
    public String getLastStats() {
        return lastStats;
    }
}
