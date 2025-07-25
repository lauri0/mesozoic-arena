package com.mesozoic.arena.engine;

import com.mesozoic.arena.ai.LLMAgent;
import com.mesozoic.arena.ai.OpponentAgent;
import com.mesozoic.arena.ai.mcts.MCTSAgent;
import com.mesozoic.arena.util.Config;
import com.mesozoic.arena.model.Dinosaur;
import com.mesozoic.arena.model.Move;
import com.mesozoic.arena.model.MoveType;
import com.mesozoic.arena.model.Player;
import com.mesozoic.arena.engine.MoveEffects;
import com.mesozoic.arena.engine.AbilityEffects;
import com.mesozoic.arena.engine.AilmentEffects;
import com.mesozoic.arena.engine.DamageCalculator;
import com.mesozoic.arena.model.Ailment;
import com.mesozoic.arena.util.PersistentEffectRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Random;
import com.mesozoic.arena.engine.TurnRecord;

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
    private final List<TurnRecord> moveHistory;
    private int turn = 1;
    private Player winner;

    private boolean moveHits(Dinosaur attacker, Dinosaur defender, Move move, Random random) {
        if (move == null || attacker == null || defender == null) {
            return false;
        }
        if (AbilityEffects.firstAttackMiss(defender, random)) {
            return false;
        }
        double accuracy = AbilityEffects.modifyAccuracy(attacker, move);
        return random.nextDouble() < accuracy;
    }

    private void logMiss(Player actingPlayer, Dinosaur attacker, Move move) {
        String actorLabel = actingPlayer == playerOne ? "Player " : "NPC ";
        addEvent(actorLabel + attacker.getName() + " used " + move.getName() + " but missed.");
    }

    public Battle(Player playerOne, Player playerTwo) {
        this(playerOne, playerTwo, createAgent(), new ArrayList<>(), true);
    }

    public Battle(Player playerOne, Player playerTwo, OpponentAgent opponentAI) {
        this(playerOne, playerTwo, opponentAI, new ArrayList<>(), true);
    }

    public Battle(Player playerOne, Player playerTwo, List<TurnRecord> history) {
        this(playerOne, playerTwo, createAgent(), history, true);
    }

    public Battle(Player playerOne, Player playerTwo, OpponentAgent opponentAI, List<TurnRecord> history) {
        this(playerOne, playerTwo, opponentAI, history, true);
    }

    public Battle(Player playerOne, Player playerTwo, List<TurnRecord> history, boolean applyEntry) {
        this(playerOne, playerTwo, createAgent(), history, applyEntry);
    }

    public Battle(Player playerOne, Player playerTwo, OpponentAgent opponentAI, List<TurnRecord> history, boolean applyEntry) {
        this.playerOne = playerOne;
        this.playerTwo = playerTwo;
        this.opponentAI = opponentAI;
        this.moveHistory = history == null ? new ArrayList<>() : history;
        if (applyEntry) {
            handleEntry(playerOne, playerTwo);
            handleEntry(playerTwo, playerOne);
        }
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
     * Returns a copy of the recorded moves and switches for each turn.
     */
    public List<TurnRecord> getMoveHistory() {
        return new ArrayList<>(moveHistory);
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
                System.err.println("Falling back to MCTS opponent");
            }
        }
        Random rng = new Random();
        return new MCTSAgent(Config.mctsIterations(), new Random(rng.nextLong()),
                new Random(rng.nextLong()), Config.mctsEpsilon(),
                Config.mctsSelfMinimaxProbability(),
                Config.mctsOpponentMinimaxProbability());
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
        executeRound(playerOneMove, playerTwoMove, new Random());
    }

    public void executeRound(Move playerOneMove, Move playerTwoMove, Random random) {
        if (winner != null) {
            return;
        }

        Dinosaur switchedOne = applyQueuedSwitch(playerOne, "Player");
        if (switchedOne != null) {
            playerOneMove = null;
        }
        Dinosaur switchedTwo = applyQueuedSwitch(playerTwo, "NPC");
        if (switchedTwo != null) {
            playerTwoMove = null;
        }

        Dinosaur dinoOne = playerOne.getActiveDinosaur();
        Dinosaur dinoTwo = playerTwo.getActiveDinosaur();
        if (dinoOne == null || dinoTwo == null) {
            return;
        }

        int p1Priority = playerOneMove == null ? Integer.MIN_VALUE
                : AbilityEffects.modifyPriority(dinoOne, playerOneMove);
        int p2Priority = playerTwoMove == null ? Integer.MIN_VALUE
                : AbilityEffects.modifyPriority(dinoTwo, playerTwoMove);
        boolean p1First;
        if (p1Priority != p2Priority) {
            p1First = p1Priority > p2Priority;
        } else {
            int p1Speed = playerOne.getModifiedSpeed();
            int p2Speed = playerTwo.getModifiedSpeed();
            if (p1Speed == p2Speed) {
                int p1Total = playerOne.getTotalHealth();
                int p2Total = playerTwo.getTotalHealth();
                if (p1Total == p2Total) {
                    p1First = true;
                } else {
                    p1First = p1Total < p2Total;
                }
            } else {
                p1First = p1Speed > p2Speed;
            }
        }

        boolean p1Braced = false;
        boolean p2Braced = false;
        String lastP1Action = null;
        String lastP2Action = null;
        if (!moveHistory.isEmpty()) {
            TurnRecord lastRecord = moveHistory.get(moveHistory.size() - 1);
            lastP1Action = lastRecord.getPlayerAction();
            lastP2Action = lastRecord.getNpcAction();
        }

        if (p1First) {
            p1Braced = MoveEffects.hasBraceEffect(playerOneMove, lastP1Action);
            boolean fainted = performTurn(playerOne, playerTwo, playerOneMove, p2Braced, random);
            if (winner == null && !fainted) {
                p2Braced = MoveEffects.hasBraceEffect(playerTwoMove, lastP2Action);
                performTurn(playerTwo, playerOne, playerTwoMove, p1Braced, random);
            }
        } else {
            p2Braced = MoveEffects.hasBraceEffect(playerTwoMove, lastP2Action);
            boolean fainted = performTurn(playerTwo, playerOne, playerTwoMove, p1Braced, random);
            if (winner == null && !fainted) {
                p1Braced = MoveEffects.hasBraceEffect(playerOneMove, lastP1Action);
                performTurn(playerOne, playerTwo, playerOneMove, p2Braced, random);
            }
        }

        AbilityEffects.endTurn(playerOne.getActiveDinosaur());
        AbilityEffects.endTurn(playerTwo.getActiveDinosaur());
        AilmentEffects.endTurn(playerOne.getActiveDinosaur());
        AilmentEffects.endTurn(playerTwo.getActiveDinosaur());
        playerOne.tickPersistentEffects();
        playerTwo.tickPersistentEffects();

        String p1Action = switchedOne != null ? "Switch to " + switchedOne.getName()
                : playerOneMove == null ? "None" : playerOneMove.getName();
        String p2Action = switchedTwo != null ? "Switch to " + switchedTwo.getName()
                : playerTwoMove == null ? "None" : playerTwoMove.getName();
        moveHistory.add(new TurnRecord(p1Action, p2Action));
    }

    /**
     * Executes a round using the AI to select the opponent's move.
     */
    public void executeRound(Move playerOneMove) {
        Move playerTwoMove = opponentAI.chooseMove(playerTwo, playerOne,
                Collections.unmodifiableList(moveHistory));
        if (opponentAI instanceof MCTSAgent mcts) {
            String stats = mcts.getLastStats();
            if (stats != null && !stats.isBlank()) {
                addAiLog(stats);
            }
        }
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
            boolean defenderBraced, Random random) {
        int repeatCount = MoveEffects.getRepeatCount(move);
        boolean defenderFainted = false;
        Dinosaur initialAttacker = actingPlayer.getActiveDinosaur();
        if (move != null && initialAttacker != null) {
            AbilityEffects.onMoveUsed(initialAttacker);
        }
        for (int index = 0; index < repeatCount; index++) {
            Dinosaur attacker = actingPlayer.getActiveDinosaur();
            Dinosaur defender = opposingPlayer.getActiveDinosaur();
            if (attacker == null || defender == null || move == null) {
                return defenderFainted;
            }

            if (!moveHits(attacker, defender, move, random)) {
                logMiss(actingPlayer, attacker, move);
                defenderBraced = false;
                continue;
            }

            applyMoveEffects(actingPlayer, opposingPlayer, move);
            if (!defenderBraced) {
                int totalDamage = DamageCalculator.calculate(attacker, defender, move);
                totalDamage = AbilityEffects.modifyIncomingDamage(defender, totalDamage);
                int beforeHealth = defender.getHealth();
                defender.adjustHealth(-totalDamage);
                int damageDealt = beforeHealth - defender.getHealth();
                if (MoveEffects.containsEffect(move, "recoil") && damageDealt > 0) {
                    int recoil = damageDealt / 4;
                    attacker.adjustHealth(-recoil);
                }
                MoveEffects.applyDrain(attacker, move, damageDealt);
                String actorLabel = actingPlayer == playerOne ? "Player " : "NPC ";
                String defenderLabel = opposingPlayer == playerOne ? "Player " : "NPC ";
                addEvent(actorLabel + attacker.getName() + " used " + move.getName() +
                        " dealing " + damageDealt + " damage. " + defenderLabel +
                        defender.getName() + " has " + Math.max(0, defender.getHealth()) +
                        " health left.");
            } else {
                String actorLabel = actingPlayer == playerOne ? "Player " : "NPC ";
                String defenderLabel = opposingPlayer == playerOne ? "Player " : "NPC ";
                addEvent(actorLabel + attacker.getName() + " used " + move.getName() +
                        " but " + defenderLabel + defender.getName() +
                        " braced and took no damage.");
            }

            AbilityEffects.onAttacked(attacker, defender, move);
            boolean faintedThisHit = defender.getHealth() <= 0;
            if (faintedThisHit) {
                AbilityEffects.onKnockOut(attacker, defender);
            }
            Dinosaur beforeDefender = defender;
            checkFaint(opposingPlayer);
            checkFaint(actingPlayer);
            defenderFainted = defenderFainted || beforeDefender != opposingPlayer.getActiveDinosaur();
            if (defenderFainted) {
                break;
            }
            defenderBraced = false;
        }

        if (MoveEffects.containsEffect(move, "switch out")
                && actingPlayer.getActiveDinosaur() != null) {
            performAutoSwitch(actingPlayer, opposingPlayer);
        }

        return defenderFainted;
    }

    private void checkFaint(Player player) {
        Dinosaur active = player.getActiveDinosaur();
        if (active != null && active.getHealth() <= 0) {
            player.removeDinosaur(active);
            Player opponent = player == playerOne ? playerTwo : playerOne;
            handleEntry(player, opponent);
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

    private Dinosaur applyQueuedSwitch(Player player, String label) {
        Dinosaur target = player.getQueuedSwitch();
        if (target == null) {
            return null;
        }
        player.setActiveDinosaur(target);
        Player opponent = player == playerOne ? playerTwo : playerOne;
        handleEntry(player, opponent);
        player.clearQueuedSwitch();
        addEvent(label + " switched to " + target.getName() + ".");
        return target;
    }

    private void performAutoSwitch(Player player, Player opponent) {
        List<Dinosaur> dinosaurs = player.getDinosaurs();
        Dinosaur active = player.getActiveDinosaur();
        int index = dinosaurs.indexOf(active);
        if (index < 0 || dinosaurs.size() <= 1) {
            return;
        }
        int nextIndex = index == dinosaurs.size() - 1 ? 0 : index + 1;
        Dinosaur next = dinosaurs.get(nextIndex);
        player.setActiveDinosaur(next);
        handleEntry(player, opponent);
    }

    private void handleEntry(Player player, Player opponent) {
        Dinosaur entering = player.getActiveDinosaur();
        AbilityEffects.onEntry(entering, opponent.getActiveDinosaur());
        applyRocksDamage(player);
    }

    private void applyRocksDamage(Player player) {
        if (!player.hasPersistentEffect("Rocks")) {
            return;
        }
        Dinosaur dino = player.getActiveDinosaur();
        if (dino == null) {
            return;
        }
        int damage = Math.round(dino.getMaxHealth() * 0.125f);
        dino.adjustHealth(-damage);
        String label = player == playerOne ? "Player " : "NPC ";
        addEvent(label + dino.getName() + " took " + damage + " damage from rocks.");
    }


    private void applyMoveEffects(Player actingPlayer, Player defendingPlayer, Move move) {
        Dinosaur active = actingPlayer.getActiveDinosaur();
        if (active == null || move == null) {
            return;
        }

        if (MoveEffects.containsEffect(move, "small heal")) {
            int healAmount = AilmentEffects.modifyHealing(active, 10);
            active.adjustHealth(healAmount);
        }
        if (MoveEffects.containsEffect(move, "big heal")) {
            int healAmount = AilmentEffects.modifyHealing(active, 30);
            active.adjustHealth(healAmount);
        }
        if (MoveEffects.containsEffect(move, "area heal")) {
            for (Dinosaur dinosaur : actingPlayer.getDinosaurs()) {
                int healAmount = AilmentEffects.modifyHealing(dinosaur, 10);
                dinosaur.adjustHealth(healAmount);
            }
        }
        if (MoveEffects.containsEffect(move, "frenzy")) {
            active.adjustHeadAttackStage(2);
        }
        if (MoveEffects.containsEffect(move, "adrenaline")) {
            active.adjustBodyAttackStage(1);
            active.adjustSpeedStage(1);
        }
        if (MoveEffects.containsEffect(move, "fatigue")) {
            active.adjustHeadAttackStage(-1);
        }
        if (MoveEffects.containsEffect(move, "slow")) {
            Dinosaur defender = defendingPlayer.getActiveDinosaur();
            if (defender != null) {
                defender.adjustSpeedStage(-1);
            }
        }
        if (MoveEffects.containsEffect(move, "bleed")) {
            Dinosaur defender = defendingPlayer.getActiveDinosaur();
            AilmentEffects.applyAilment(defender, new Ailment("Bleeding"));
        }
        if (MoveEffects.containsEffect(move, "tailwind")) {
            actingPlayer.addPersistentEffect(
                    PersistentEffectRegistry.createEffect("Tailwind"));
            String label = actingPlayer == playerOne ? "Player" : "NPC";
            addEvent(label + " set Tailwind.");
        }
        if (MoveEffects.containsEffect(move, "rocks")) {
            defendingPlayer.addPersistentEffect(
                    PersistentEffectRegistry.createEffect("Rocks"));
            String label = defendingPlayer == playerOne ? "Player" : "NPC";
            addEvent("Rocks were scattered on " + label + " side.");
        }
    }
}
