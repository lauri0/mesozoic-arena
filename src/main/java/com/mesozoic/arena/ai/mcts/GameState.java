package com.mesozoic.arena.ai.mcts;

import com.mesozoic.arena.engine.Battle;
import com.mesozoic.arena.model.Dinosaur;
import com.mesozoic.arena.model.Move;
import com.mesozoic.arena.model.Player;
import com.mesozoic.arena.model.SwitchMove;
import com.mesozoic.arena.engine.TurnRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Lightweight representation of a battle state used for MCTS simulations.
 */
public class GameState {
    private final Player playerOne;
    private final Player playerTwo;
    private final Battle battle;
    private final List<TurnRecord> history;

    /**
     * Creates a new simulation state from the given players.
     * Copies the players before constructing the internal battle.
     * Entry abilities are not triggered again.
     */
    public GameState(Player playerOne, Player playerTwo) {
        this(playerOne, playerTwo, List.of());
    }

    public GameState(Player playerOne, Player playerTwo, List<TurnRecord> history) {
        this(playerOne.copy(), playerTwo.copy(), false, history);
    }

    /**
     * Internal constructor controlling whether entry abilities are applied.
     *
     * @param playerOne   player representing the first side
     * @param playerTwo   player representing the second side
     * @param applyEntry  when true, active dinosaurs trigger entry abilities
     */
    private GameState(Player playerOne, Player playerTwo, boolean applyEntry, List<TurnRecord> history) {
        this.playerOne = playerOne;
        this.playerTwo = playerTwo;
        this.history = history == null ? new ArrayList<>() : new ArrayList<>(history);
        this.battle = new Battle(this.playerOne, this.playerTwo, this.history);
        if (!applyEntry) {
            revertEntryEffects();
        }
    }

    public Player getPlayerOne() {
        return playerOne;
    }

    public Player getPlayerTwo() {
        return playerTwo;
    }

    /**
     * Returns the moves available to the active dinosaur of the given player.
     */
    public List<Move> availableMovesFor(Player player) {
        if (player == null) {
            return List.of();
        }
        Dinosaur active = player.getActiveDinosaur();
        if (active == null) {
            return List.of();
        }
        List<Move> moves = new ArrayList<>(active.getMoves());
        List<Dinosaur> roster = player.getDinosaurs();
        for (int index = 0; index < roster.size(); index++) {
            Dinosaur bench = roster.get(index);
            if (!bench.equals(active)) {
                moves.add(new SwitchMove(bench, index));
            }
        }
        return moves;
    }

    /**
     * Produces the next game state after both players perform their moves.
     */
    public GameState nextState(Move playerOneMove, Move playerTwoMove, Random random) {
        Player nextPlayerOne = playerOne.copy();
        Player nextPlayerTwo = playerTwo.copy();

        playerOneMove = applySwitchMove(nextPlayerOne, playerOneMove);
        playerTwoMove = applySwitchMove(nextPlayerTwo, playerTwoMove);

        List<TurnRecord> nextHistory = new ArrayList<>(history);
        GameState next = new GameState(nextPlayerOne, nextPlayerTwo, false, nextHistory);
        next.battle.executeRound(playerOneMove, playerTwoMove, random);
        return next;
    }

    private static Move applySwitchMove(Player player, Move move) {
        if (move instanceof SwitchMove switchMove) {
            List<Dinosaur> dinos = player.getDinosaurs();
            int index = switchMove.getTargetIndex();
            if (index >= 0 && index < dinos.size()) {
                player.queueSwitch(dinos.get(index));
            }
            return null;
        }
        return move;
    }

    /**
     * Indicates whether the battle has concluded.
     */
    public boolean isTerminal() {
        return battle.getWinner() != null;
    }

    /**
     * Returns 1 when player one has won, -1 when player two has won
     * and 0 when the battle is still in progress.
     */
    public int winner() {
        if (battle.getWinner() == null) {
            return 0;
        }
        return battle.getWinner() == playerOne ? 1 : -1;
    }

    private void revertEntryEffects() {
        Dinosaur activeOne = playerOne.getActiveDinosaur();
        Dinosaur activeTwo = playerTwo.getActiveDinosaur();
        if (activeOne != null && activeOne.getAbility() != null
                && "Intimidate".equalsIgnoreCase(activeOne.getAbility().getName())
                && activeTwo != null) {
            activeTwo.adjustAttackStage(1);
        }
        if (activeTwo != null && activeTwo.getAbility() != null
                && "Intimidate".equalsIgnoreCase(activeTwo.getAbility().getName())
                && activeOne != null) {
            activeOne.adjustAttackStage(1);
        }
    }
}
