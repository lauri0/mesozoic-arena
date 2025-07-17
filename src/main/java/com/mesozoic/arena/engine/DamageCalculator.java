package com.mesozoic.arena.engine;

import com.mesozoic.arena.model.Dinosaur;
import com.mesozoic.arena.model.Move;
import com.mesozoic.arena.model.MoveType;

/**
 * Utility for calculating move damage before ability modifiers.
 */
public final class DamageCalculator {
    private DamageCalculator() {
    }

    /**
     * Computes the raw damage of a move after factoring in attack stats, STAB
     * and type advantages.
     *
     * @param attacker the dinosaur using the move
     * @param defender the opposing dinosaur
     * @param move     the move being used
     * @return the calculated damage value
     */
    public static int calculate(Dinosaur attacker, Dinosaur defender, Move move) {
        if (attacker == null || move == null) {
            return 0;
        }
        double attackStat = move.getKind() == MoveType.HEAD
                ? attacker.getEffectiveHeadAttack()
                : attacker.getEffectiveBodyAttack();
        double stab = attacker.hasType(move.getType()) ? 1.5 : 1.0;
        double typeMultiplier = defender == null ? 1.0
                : defender.getMultiplierFrom(move.getType());
        long baseDamage = Math.round(move.getDamage() * attackStat * stab
                * typeMultiplier);
        return Math.toIntExact(baseDamage);
    }
}
