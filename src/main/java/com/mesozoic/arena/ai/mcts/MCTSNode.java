package com.mesozoic.arena.ai.mcts;

import com.mesozoic.arena.model.Move;
import com.mesozoic.arena.model.Player;
import com.mesozoic.arena.model.Dinosaur;
import com.mesozoic.arena.util.Config;

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
    private final double selfProbability;
    private final double opponentProbability;
    private int visitCount;
    private double winScore;
    private int winCount;
    private int drawCount;

    public MCTSNode(GameState state, MCTSNode parent, Move move,
            double selfMinimaxProbability, double opponentMinimaxProbability) {
        this.state = state;
        this.parent = parent;
        this.move = move;
        this.selfProbability = selfMinimaxProbability;
        this.opponentProbability = opponentMinimaxProbability;
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

    private static int totalHealth(Player player) {
        int sum = 0;
        for (Dinosaur dino : player.getDinosaurs()) {
            sum += dino.getHealth();
        }
        return sum;
    }

    private static int evaluateState(GameState gameState) {
        int winner = gameState.winner();
        if (winner == 1) {
            return Integer.MAX_VALUE / 2;
        }
        if (winner == -1) {
            return Integer.MIN_VALUE / 2;
        }
        int p1Health = totalHealth(gameState.getPlayerOne());
        int p2Health = totalHealth(gameState.getPlayerTwo());
        return p1Health - p2Health;
    }

    private Move minimaxMove(Random random) {
        return minimaxMove(state, random, true);
    }

    private Move minimaxMove(GameState currentState, Random random, boolean forPlayerOne) {
        Player maximizer = forPlayerOne ? currentState.getPlayerOne() : currentState.getPlayerTwo();
        Player minimizer = forPlayerOne ? currentState.getPlayerTwo() : currentState.getPlayerOne();

        List<Move> maxMoves = currentState.availableMovesFor(maximizer);
        if (maxMoves.isEmpty()) {
            return null;
        }
        List<Move> minMoves = currentState.availableMovesFor(minimizer);
        if (minMoves.isEmpty()) {
            return maxMoves.get(random.nextInt(maxMoves.size()));
        }

        Move bestMove = null;
        int bestValue = Integer.MIN_VALUE;
        for (Move maxMove : maxMoves) {
            int worstValue = Integer.MAX_VALUE;
            for (Move minMove : minMoves) {
                GameState next = forPlayerOne
                        ? currentState.nextState(maxMove, minMove, random)
                        : currentState.nextState(minMove, maxMove, random);
                int value = evaluateState(next);
                if (!forPlayerOne) {
                    value = -value;
                }
                if (value < worstValue) {
                    worstValue = value;
                }
            }
            if (worstValue > bestValue) {
                bestValue = worstValue;
                bestMove = maxMove;
            }
        }

        if (bestMove == null) {
            return maxMoves.get(random.nextInt(maxMoves.size()));
        }
        return bestMove;
    }

    private Move chooseSelfMove(GameState currentState, Random random) {
        if (random.nextDouble() < selfProbability) {
            return minimaxMove(currentState, random, false);
        }
        return randomMove(currentState.getPlayerTwo(), random);
    }

    private Move chooseOpponentMove(GameState currentState, Random random) {
        if (random.nextDouble() < opponentProbability) {
            return minimaxMove(currentState, random, true);
        }
        return randomMove(currentState.getPlayerOne(), random);
    }

    public MCTSNode expand(Random selectionRandom, Random simulationRandom) {
        if (untriedMoves.isEmpty()) {
            return this;
        }
        Move chosenMove = untriedMoves.remove(selectionRandom.nextInt(untriedMoves.size()));
        Move opponentMove = chooseOpponentMove(state, simulationRandom);
        GameState nextState = state.nextState(opponentMove, chosenMove, simulationRandom);
        MCTSNode child = new MCTSNode(nextState, this, chosenMove, selfProbability, opponentProbability);
        children.add(child);
        return child;
    }

    public MCTSNode bestChild(Random random, double epsilon) {
        if (random != null && epsilon > 0 && !children.isEmpty()
                && random.nextDouble() < epsilon) {
            return children.get(random.nextInt(children.size()));
        }
        double exploration = Config.mctsExploration();
        MCTSNode best = null;
        double bestValue = Double.NEGATIVE_INFINITY;
        for (MCTSNode child : children) {
            double exploitation = child.winScore / (child.visitCount + 1e-6);
            double exploreTerm = Math.sqrt(Math.log(visitCount + 1)
                    / (child.visitCount + 1e-6));
            double uctValue = exploitation + exploration * exploreTerm;
            if (uctValue > bestValue) {
                bestValue = uctValue;
                best = child;
            }
        }
        return best;
    }

    public MCTSNode bestChild() {
        return bestChild(null, 0.0);
    }

    public int rollout(Random simulationRandom) {
        GameState current = state;
        int steps = 0;
        while (!current.isTerminal() && steps < MAX_ROLLOUT_STEPS) {
            Move ourMove = chooseSelfMove(current, simulationRandom);
            Move opponentMove = chooseOpponentMove(current, simulationRandom);
            current = current.nextState(opponentMove, ourMove, simulationRandom);
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
