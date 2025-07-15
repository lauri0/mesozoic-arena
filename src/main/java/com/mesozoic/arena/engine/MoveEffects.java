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
     * Determines whether a move with a brace effect should activate based on the last action.
     *
     * @param move       the move being used
     * @param lastAction the previous action taken by the same player, may be {@code null}
     * @return {@code true} if the brace effect applies, otherwise {@code false}
     */
    public static boolean hasBraceEffect(Move move, String lastAction) {
        if (move == null) {
            return false;
        }
        boolean bracePresent = false;
        for (Effect effect : move.getEffects()) {
            if ("brace".equalsIgnoreCase(effect.getName())) {
                bracePresent = true;
                break;
            }
        }
        if (!bracePresent) {
            return false;
        }
        if (lastAction != null && "brace".equalsIgnoreCase(lastAction)) {
            return false;
        }
        return true;
    }
}
