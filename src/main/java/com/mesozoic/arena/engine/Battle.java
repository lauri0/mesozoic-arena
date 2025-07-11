package com.mesozoic.arena.engine;

import com.mesozoic.arena.ai.RandomOpponent;
import com.mesozoic.arena.model.Dinosaur;
import com.mesozoic.arena.model.Move;
import com.mesozoic.arena.model.Player;

/**
 * Handles turn management and rule enforcement for a battle between two
 * players.
 */
public class Battle {
    private final Player playerOne;
    private final Player playerTwo;
    private final RandomOpponent opponentAI;
    private Player winner;

    public Battle(Player playerOne, Player playerTwo) {
        this(playerOne, playerTwo, new RandomOpponent());
    }

    public Battle(Player playerOne, Player playerTwo, RandomOpponent opponentAI) {
        this.playerOne = playerOne;
        this.playerTwo = playerTwo;
        this.opponentAI = opponentAI;
    }

    /**
     * Executes a single round where each player performs the provided move.
     * The order of execution is determined by the active dinosaurs' speed.
     */
    public void executeRound(Move playerOneMove, Move playerTwoMove) {
        if (winner != null) {
            return;
        }

        Dinosaur dinoOne = playerOne.getActiveDinosaur();
        Dinosaur dinoTwo = playerTwo.getActiveDinosaur();
        if (dinoOne == null || dinoTwo == null) {
            return;
        }

        if (dinoOne.getSpeed() >= dinoTwo.getSpeed()) {
            performTurn(playerOne, playerTwo, playerOneMove);
            if (winner == null) {
                performTurn(playerTwo, playerOne, playerTwoMove);
            }
        } else {
            performTurn(playerTwo, playerOne, playerTwoMove);
            if (winner == null) {
                performTurn(playerOne, playerTwo, playerOneMove);
            }
        }
    }

    /**
     * Executes a round using the AI to select the opponent's move.
     */
    public void executeRound(Move playerOneMove) {
        Move playerTwoMove = opponentAI.chooseMove(playerTwo.getActiveDinosaur());
        executeRound(playerOneMove, playerTwoMove);
    }

    /**
     * Returns the winning player once the battle has concluded or {@code null}
     * if it is still ongoing.
     */
    public Player getWinner() {
        return winner;
    }

    private void performTurn(Player actingPlayer, Player opposingPlayer, Move move) {
        Dinosaur attacker = actingPlayer.getActiveDinosaur();
        Dinosaur defender = opposingPlayer.getActiveDinosaur();
        if (attacker == null || defender == null || move == null) {
            return;
        }

        // regenerate stamina at the start of the turn
        attacker.adjustStamina(10);

        // apply move effects
        attacker.adjustStamina(-move.getStaminaCost());
        defender.adjustHealth(-move.getDamage());

        checkFaint(opposingPlayer);
    }

    private void checkFaint(Player player) {
        Dinosaur active = player.getActiveDinosaur();
        if (active != null && active.getHealth() <= 0) {
            player.removeDinosaur(active);
            if (!player.hasRemainingDinosaurs()) {
                winner = (player == playerOne) ? playerTwo : playerOne;
            }
        }
    }
}
