package com.mesozoic.arena.ai.mcts;

import com.mesozoic.arena.engine.Battle;
import com.mesozoic.arena.model.Dinosaur;
import com.mesozoic.arena.model.Move;
import com.mesozoic.arena.model.Player;

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

    /**
     * Creates a new simulation state from the given players.
     * Copies the players before constructing the internal battle.
     */
    public GameState(Player playerOne, Player playerTwo) {
        this(playerOne.copy(), playerTwo.copy(), true);
    }

    private GameState(Player playerOne, Player playerTwo, boolean applyEntry) {
        this.playerOne = playerOne;
        this.playerTwo = playerTwo;
        this.battle = new Battle(this.playerOne, this.playerTwo);
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
        return new ArrayList<>(active.getMoves());
    }

    /**
     * Produces the next game state after both players perform their moves.
     */
    public GameState nextState(Move playerOneMove, Move playerTwoMove, Random random) {
        Player nextPlayerOne = playerOne.copy();
        Player nextPlayerTwo = playerTwo.copy();
        GameState next = new GameState(nextPlayerOne, nextPlayerTwo, false);
        next.battle.executeRound(playerOneMove, playerTwoMove, random);
        return next;
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
