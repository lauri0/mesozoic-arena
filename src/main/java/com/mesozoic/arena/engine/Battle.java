package com.mesozoic.arena.engine;

import com.mesozoic.arena.ai.LLMAgent;
import com.mesozoic.arena.ai.OpponentAgent;
import com.mesozoic.arena.ai.RandomOpponent;
import com.mesozoic.arena.util.Config;
import com.mesozoic.arena.model.Dinosaur;
import com.mesozoic.arena.model.Move;
import com.mesozoic.arena.model.Player;
import com.mesozoic.arena.model.Effect;
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
    private final List<String> aiLog = new ArrayList<>();
    private int turn = 1;
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
     * Returns a copy of the AI reasoning log.
     */
    public List<String> getAiLog() {
        return new ArrayList<>(aiLog);
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

    private void addEvent(String message) {
        eventLog.add("Turn " + turn + ": " + message);
    }

    private void addAiLog(String message) {
        aiLog.add("Turn " + turn + ": " + message);
    }

    /**
     * Executes a single round where each player performs the provided move.
     * The order of execution is determined by move priority and speed.
     */
    public void executeRound(Move playerOneMove, Move playerTwoMove) {
        if (winner != null) {
            return;
        }

        if (applyQueuedSwitch(playerOne, "Player")) {
            playerOneMove = null;
        }
        if (applyQueuedSwitch(playerTwo, "NPC")) {
            playerTwoMove = null;
        }

        Dinosaur dinoOne = playerOne.getActiveDinosaur();
        Dinosaur dinoTwo = playerTwo.getActiveDinosaur();
        if (dinoOne == null || dinoTwo == null) {
            return;
        }

        int p1Priority = playerOneMove == null ? Integer.MIN_VALUE : playerOneMove.getPriority();
        int p2Priority = playerTwoMove == null ? Integer.MIN_VALUE : playerTwoMove.getPriority();
        boolean p1First;
        if (p1Priority != p2Priority) {
            p1First = p1Priority > p2Priority;
        } else {
            p1First = dinoOne.getSpeed() >= dinoTwo.getSpeed();
        }

        boolean p1Braced = false;
        boolean p2Braced = false;

        if (p1First) {
            p1Braced = hasBraceEffect(playerOneMove);
            boolean fainted = performTurn(playerOne, playerTwo, playerOneMove, p2Braced);
            if (winner == null && !fainted) {
                p2Braced = hasBraceEffect(playerTwoMove);
                performTurn(playerTwo, playerOne, playerTwoMove, p1Braced);
            }
        } else {
            p2Braced = hasBraceEffect(playerTwoMove);
            boolean fainted = performTurn(playerTwo, playerOne, playerTwoMove, p1Braced);
            if (winner == null && !fainted) {
                p1Braced = hasBraceEffect(playerOneMove);
                performTurn(playerOne, playerTwo, playerOneMove, p2Braced);
            }
        }

        regenerateBenchStamina(playerOne);
        regenerateBenchStamina(playerTwo);
    }

    /**
     * Executes a round using the AI to select the opponent's move.
     */
    public void executeRound(Move playerOneMove) {
        Move playerTwoMove = opponentAI.chooseMove(playerTwo, playerOne);
        logLLMResponse();
        executeRound(playerOneMove, playerTwoMove);
        turn++;
    }

    /**
     * Returns the winning player once the battle has concluded or {@code null}
     * if it is still ongoing.
     */
    public Player getWinner() {
        return winner;
    }

    private boolean performTurn(Player actingPlayer, Player opposingPlayer, Move move,
            boolean defenderBraced) {
        Dinosaur attacker = actingPlayer.getActiveDinosaur();
        Dinosaur defender = opposingPlayer.getActiveDinosaur();
        if (attacker == null || defender == null || move == null) {
            return false;
        }

        // apply move effects
        attacker.adjustStamina(move.getStaminaChange());
        if (!defenderBraced) {
            int totalDamage = move.getDamage() * attacker.getAttack();
            int before = defender.getHealth();
            defender.adjustHealth(-totalDamage);
            int damage = before - defender.getHealth();
            String actorLabel = actingPlayer == playerOne ? "Player " : "NPC ";
            String defenderLabel = opposingPlayer == playerOne ? "Player " : "NPC ";
            addEvent(actorLabel + attacker.getName() + " used " + move.getName() +
                    " dealing " + damage + " damage. " + defenderLabel +
                    defender.getName() + " has " + Math.max(0, defender.getHealth()) +
                    " health left.");
        } else {
            String actorLabel = actingPlayer == playerOne ? "Player " : "NPC ";
            String defenderLabel = opposingPlayer == playerOne ? "Player " : "NPC ";
            addEvent(actorLabel + attacker.getName() + " used " + move.getName() +
                    " but " + defenderLabel + defender.getName() +
                    " braced and took no damage.");
        }

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
                addAiLog(response);
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
        addEvent(label + " switched to " + target.getName() + ".");
        return true;
    }

    private boolean hasBraceEffect(Move move) {
        if (move == null) {
            return false;
        }
        for (Effect effect : move.getEffects()) {
            if ("brace".equalsIgnoreCase(effect.getName())) {
                return true;
            }
        }
        return false;
    }

    private void regenerateBenchStamina(Player player) {
        Dinosaur active = player.getActiveDinosaur();
        for (Dinosaur dinosaur : player.getDinosaurs()) {
            if (!dinosaur.equals(active)) {
                dinosaur.adjustStamina(10);
            }
        }
    }
}
