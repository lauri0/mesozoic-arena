package com.mesozoic.arena.ai.mcts;

import com.mesozoic.arena.model.Move;
import com.mesozoic.arena.model.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Node used by the Monte Carlo Tree Search.
 */
public class MCTSNode {
    private static final int MAX_ROLLOUT_STEPS = 100;
    private final GameState state;
    private final MCTSNode parent;
    private final List<MCTSNode> children = new ArrayList<>();
    private final List<Move> untriedMoves;
    private final Move move;
    private int visitCount;
    private double winScore;
    private int winCount;
    private int drawCount;

    public MCTSNode(GameState state, MCTSNode parent, Move move) {
        this.state = state;
        this.parent = parent;
        this.move = move;
        this.untriedMoves = new ArrayList<>(state.availableMovesFor(state.getPlayerTwo()));
    }

    public GameState getState() {
        return state;
    }

    public Move getMove() {
        return move;
    }

    public List<MCTSNode> getChildren() {
        return children;
    }

    public int getVisitCount() {
        return visitCount;
    }

    public double getWinScore() {
        return winScore;
    }

    public int getWinCount() {
        return winCount;
    }

    public int getDrawCount() {
        return drawCount;
    }

    public boolean isFullyExpanded() {
        return untriedMoves.isEmpty();
    }

    private Move randomMove(Player player, Random random) {
        List<Move> moves = state.availableMovesFor(player);
        if (moves.isEmpty()) {
            return null;
        }
        return moves.get(random.nextInt(moves.size()));
    }

    public MCTSNode expand(Random random) {
        if (untriedMoves.isEmpty()) {
            return this;
        }
        Move chosenMove = untriedMoves.remove(random.nextInt(untriedMoves.size()));
        Move opponentMove = randomMove(state.getPlayerOne(), random);
        GameState nextState = state.nextState(opponentMove, chosenMove, random);
        MCTSNode child = new MCTSNode(nextState, this, chosenMove);
        children.add(child);
        return child;
    }

    public MCTSNode bestChild() {
        double exploration = 5.0;
        MCTSNode best = null;
        double bestValue = Double.NEGATIVE_INFINITY;
        for (MCTSNode child : children) {
            double exploitation = child.winScore / (child.visitCount + 1e-6);
            double exploreTerm = Math.sqrt(Math.log(visitCount + 1) / (child.visitCount + 1e-6));
            double uctValue = exploitation + exploration * exploreTerm;
            if (uctValue > bestValue) {
                bestValue = uctValue;
                best = child;
            }
        }
        return best;
    }

    public int rollout(Random random) {
        GameState current = state;
        int steps = 0;
        while (!current.isTerminal() && steps < MAX_ROLLOUT_STEPS) {
            Move ourMove = randomMove(current.getPlayerTwo(), random);
            Move opponentMove = randomMove(current.getPlayerOne(), random);
            current = current.nextState(opponentMove, ourMove, random);
            steps++;
        }
        int winner = current.winner();
        if (winner == -1) {
            return 1;
        } else if (winner == 1) {
            return -1;
        }
        return 0;
    }

    public void backpropagate(int result) {
        MCTSNode node = this;
        while (node != null) {
            node.visitCount++;
            node.winScore += result;
            if (result > 0) {
                node.winCount++;
            } else if (result == 0) {
                node.drawCount++;
            }
            node = node.parent;
        }
    }
}
