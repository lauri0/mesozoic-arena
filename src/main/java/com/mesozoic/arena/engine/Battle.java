package com.mesozoic.arena.engine;

import com.mesozoic.arena.ai.LLMAgent;
import com.mesozoic.arena.ai.OpponentAgent;
import com.mesozoic.arena.ai.RandomOpponent;
import com.mesozoic.arena.util.Config;
import com.mesozoic.arena.model.Dinosaur;
import com.mesozoic.arena.model.Move;
import com.mesozoic.arena.model.Player;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles turn management and rule enforcement for a battle between two
 * players.
 */
public class Battle {
    private final Player playerOne;
    private final Player playerTwo;
    private final OpponentAgent opponentAI;
    private final List<String> eventLog = new ArrayList<>();
    private Player winner;

    public Battle(Player playerOne, Player playerTwo) {
        this(playerOne, playerTwo, createAgent());
    }

    public Battle(Player playerOne, Player playerTwo, OpponentAgent opponentAI) {
        this.playerOne = playerOne;
        this.playerTwo = playerTwo;
        this.opponentAI = opponentAI;
    }

    /**
     * Returns a copy of the cumulative battle log.
     */
    public List<String> getEventLog() {
        return new ArrayList<>(eventLog);
    }

    /**
     * Provides access to the opponent agent for external decision making.
     */
    public OpponentAgent getOpponentAI() {
        return opponentAI;
    }

    private static OpponentAgent createAgent() {
        if (Config.useLLMAgent()) {
            try {
                return new LLMAgent();
            } catch (Exception e) {
                System.err.println("Failed to load LLM model: " + e.getMessage());
                e.printStackTrace();
                System.err.println("Falling back to random opponent");
            }
        }
        return new RandomOpponent();
    }

    /**
     * Executes a single round where each player performs the provided move.
     * The order of execution is determined by the active dinosaurs' speed.
     */
    public void executeRound(Move playerOneMove, Move playerTwoMove) {
        if (winner != null) {
            return;
        }

        if (applyQueuedSwitch(playerOne, "Player")) {
            playerOneMove = null;
        }
        if (applyQueuedSwitch(playerTwo, "Opponent")) {
            playerTwoMove = null;
        }

        Dinosaur dinoOne = playerOne.getActiveDinosaur();
        Dinosaur dinoTwo = playerTwo.getActiveDinosaur();
        if (dinoOne == null || dinoTwo == null) {
            return;
        }

        if (dinoOne.getSpeed() >= dinoTwo.getSpeed()) {
            boolean fainted = performTurn(playerOne, playerTwo, playerOneMove);
            if (winner == null && !fainted) {
                performTurn(playerTwo, playerOne, playerTwoMove);
            }
        } else {
            boolean fainted = performTurn(playerTwo, playerOne, playerTwoMove);
            if (winner == null && !fainted) {
                performTurn(playerOne, playerTwo, playerOneMove);
            }
        }
    }

    /**
     * Executes a round using the AI to select the opponent's move.
     */
    public void executeRound(Move playerOneMove) {
        Move playerTwoMove = opponentAI.chooseMove(playerTwo, playerOne);
        logLLMResponse();
        executeRound(playerOneMove, playerTwoMove);
    }

    /**
     * Returns the winning player once the battle has concluded or {@code null}
     * if it is still ongoing.
     */
    public Player getWinner() {
        return winner;
    }

    private boolean performTurn(Player actingPlayer, Player opposingPlayer, Move move) {
        Dinosaur attacker = actingPlayer.getActiveDinosaur();
        Dinosaur defender = opposingPlayer.getActiveDinosaur();
        if (attacker == null || defender == null || move == null) {
            return false;
        }

        // apply move effects
        attacker.adjustStamina(move.getStaminaChange());
        int before = defender.getHealth();
        defender.adjustHealth(-move.getDamage());
        int damage = before - defender.getHealth();
        eventLog.add(attacker.getName() + " used " + move.getName() + " dealing "
                + damage + " damage. " + defender.getName() + " has "
                + Math.max(0, defender.getHealth()) + " health left.");

        Dinosaur beforeDefender = defender;
        checkFaint(opposingPlayer);
        return beforeDefender != opposingPlayer.getActiveDinosaur();
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

    private void logLLMResponse() {
        if (opponentAI instanceof LLMAgent llm) {
            String response = llm.getLastResponse();
            if (response != null && !response.isBlank()) {
                eventLog.add("LLM: " + response);
            }
        }
    }

    private boolean applyQueuedSwitch(Player player, String label) {
        Dinosaur target = player.getQueuedSwitch();
        if (target == null) {
            return false;
        }
        player.setActiveDinosaur(target);
        player.clearQueuedSwitch();
        eventLog.add(label + " switched to " + target.getName() + ".");
        return true;
    }
}
