package com.mesozoic.arena.engine;

import com.mesozoic.arena.model.Effect;
import com.mesozoic.arena.model.Move;

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
}
