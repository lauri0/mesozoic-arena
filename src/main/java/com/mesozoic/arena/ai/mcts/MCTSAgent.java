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
    private final Random selectionRandom;
    private final Random simulationRandom;
    private final double epsilon;
    private final double selfProbability;
    private final double opponentProbability;
    private String lastStats = "";
    private int expansionCounter = 0;

    public MCTSAgent(int iterations, Random random) {
        this(iterations, new Random(random.nextLong()),
                new Random(random.nextLong()), 0.1, 0.5, 0.75);
    }

    public MCTSAgent(int iterations, Random random, double epsilon) {
        this(iterations, new Random(random.nextLong()),
                new Random(random.nextLong()), epsilon, 0.5, 0.75);
    }

    public MCTSAgent(int iterations, Random selectionRandom, Random simulationRandom) {
        this(iterations, selectionRandom, simulationRandom, 0.1, 0.5, 0.75);
    }

    public MCTSAgent(int iterations, Random selectionRandom, Random simulationRandom,
            double epsilon) {
        this(iterations, selectionRandom, simulationRandom, epsilon, 0.5, 0.75);
    }

    public MCTSAgent(int iterations, Random selectionRandom, Random simulationRandom,
            double epsilon, double selfMinimaxProbability, double opponentMinimaxProbability) {
        this.iterations = iterations;
        this.selectionRandom = selectionRandom;
        this.simulationRandom = simulationRandom;
        this.epsilon = epsilon;
        this.selfProbability = selfMinimaxProbability;
        this.opponentProbability = opponentMinimaxProbability;
    }

    @Override
    public Move chooseMove(Player self, Player enemy, List<TurnRecord> history) {
        if (self == null || self.getActiveDinosaur() == null) {
            return null;
        }

        GameState rootState = new GameState(enemy, self, history);
        MCTSNode root = new MCTSNode(rootState, null, null,
                selfProbability, opponentProbability);
        expansionCounter = 0;

        for (int i = 0; i < iterations; i++) {
            MCTSNode node = root;
            while (node.isFullyExpanded() && !node.getChildren().isEmpty()) {
                node = node.bestChild(selectionRandom, epsilon);
            }
            if (!node.getState().isTerminal()) {
                boolean expanded = false;
                Move expandedMove = null;
                if (!node.isFullyExpanded()) {
                    node = node.expand(selectionRandom, simulationRandom);
                    expanded = true;
                    expandedMove = node.getMove();
                    expansionCounter++;
                    if (expandedMove != null) {
                        //System.out.println("Expansion " + expansionCounter + ": " + expandedMove.getName());
                    }
                }
                double result = node.rollout(simulationRandom);
                if (expanded && expandedMove != null) {
                    //System.out.println("First rollout result for " + expandedMove.getName() + ": " + result);
                }
                node.backpropagate(result);
                continue;
            }
            double result = node.rollout(simulationRandom);
            node.backpropagate(result);
        }

        StringBuilder summary = new StringBuilder();
        for (MCTSNode child : root.getChildren()) {
            if (child.getVisitCount() == 0) {
                continue;
            }
            double average = child.getWinScore() / child.getVisitCount();
            summary.append("\n");
            summary.append(child.getMove().getName())
                    .append(": ")
                    .append(child.getVisitCount())
                    .append(" visits, ")
                    .append(child.getWinCount())
                    .append(" wins, ")
                    .append(" avg score ")
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
        if (!root.getChildren().isEmpty()
                && selectionRandom.nextDouble() < epsilon) {
            bestChild = root.getChildren()
                    .get(selectionRandom.nextInt(root.getChildren().size()));
        }

        if (bestChild == null) {
            List<Move> moves = self.getActiveDinosaur().getMoves();
            if (moves.isEmpty()) {
                return null;
            }
            return moves.get(selectionRandom.nextInt(moves.size()));
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
