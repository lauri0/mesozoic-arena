package com.mesozoic.arena.engine;

import com.mesozoic.arena.model.Effect;
import com.mesozoic.arena.model.Move;
import com.mesozoic.arena.model.Dinosaur;

/**
 * Utility functions for processing move based effects.
 */
public final class MoveEffects {
    private MoveEffects() {
    }

    /**
     * Checks if the given move has an effect with the specified name.
     */
    public static boolean containsEffect(Move move, String effectName) {
        if (move == null || effectName == null) {
            return false;
        }
        for (Effect effect : move.getEffects()) {
            if (effectName.equalsIgnoreCase(effect.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines whether a move with a brace effect should activate based on the last action.
     *
     * @param move       the move being used
     * @param lastAction the previous action taken by the same player, may be {@code null}
     * @return {@code true} if the brace effect applies, otherwise {@code false}
     */
    public static boolean hasBraceEffect(Move move, String lastAction) {
        if (!containsEffect(move, "brace")) {
            return false;
        }
        if (lastAction != null && "brace".equalsIgnoreCase(lastAction)) {
            return false;
        }
        return true;
    }

    /**
     * Determines how many times the given move should be executed based on its effects.
     * Returns {@code 3} when the move has a triple attack effect, {@code 2} for a double
     * attack effect and {@code 1} otherwise.
     */
    public static int getRepeatCount(Move move) {
        if (containsEffect(move, "triple attack")) {
            return 3;
        }
        if (containsEffect(move, "double attack")) {
            return 2;
        }
        return 1;
    }

    /**
     * Applies drain healing based on the damage dealt and move effects.
     * Heals half the damage for big drain or a quarter for small drain.
     *
     * @param user        the dinosaur using the move
     * @param move        the move being used
     * @param damageDealt the damage inflicted on the opponent
     */
    public static void applyDrain(Dinosaur user, Move move, int damageDealt) {
        if (user == null || move == null || damageDealt <= 0) {
            return;
        }
        int percent = 0;
        if (containsEffect(move, "big drain")) {
            percent = 50;
        } else if (containsEffect(move, "small drain")) {
            percent = 25;
        }
        if (percent == 0) {
            return;
        }
        int healAmount = damageDealt * percent / 100;
        healAmount = AilmentEffects.modifyHealing(user, healAmount);
        user.adjustHealth(healAmount);
    }

    /**
     * Applies the retaliate damage bonus when appropriate.
     *
     * @param move         the move being used
     * @param damage       the calculated damage
     * @param wasHitBefore whether the user was hit earlier in the round
     * @return the damage after applying the retaliate effect
     */
    public static int applyRetaliate(Move move, int damage, boolean wasHitBefore) {
        if (wasHitBefore && containsEffect(move, "retaliate")) {
            return Math.toIntExact(Math.round(damage * 1.5));
        }
        return damage;
    }
}
