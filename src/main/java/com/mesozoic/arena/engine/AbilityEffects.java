package com.mesozoic.arena.engine;

import com.mesozoic.arena.model.Dinosaur;
import com.mesozoic.arena.model.Ability;
import com.mesozoic.arena.model.Move;

/**
 * Utility methods for applying ability based effects.
 */
public final class AbilityEffects {
    private AbilityEffects() {
    }

    /**
     * Applies effects that trigger when a dinosaur enters the field.
     *
     * @param entering the dinosaur entering
     * @param opponent the opposing active dinosaur
     */
    public static void onEntry(Dinosaur entering, Dinosaur opponent) {
        if (entering == null) {
            return;
        }
        Ability ability = entering.getAbility();
        if (ability == null) {
            return;
        }
        String name = ability.getName();
        if ("Intimidate".equalsIgnoreCase(name) && opponent != null) {
            opponent.adjustAttackStage(-1);
        }
    }

    /**
     * Modifies incoming damage based on the defender's ability.
     */
    public static int modifyIncomingDamage(Dinosaur defender, int damage) {
        Ability ability = defender == null ? null : defender.getAbility();
        if (ability == null) {
            return damage;
        }

        String name = ability.getName();
        if ("Thick Skin".equalsIgnoreCase(name)) {
            return Math.round(damage * 0.8f);
        }

        if ("Armored".equalsIgnoreCase(name)) {
            int modified = damage - 10;
            return Math.max(0, modified);
        }

        return damage;
    }

    /**
     * Applies effects after a dinosaur has been attacked.
     *
     * @param attacker the dinosaur that initiated the attack
     * @param defender the dinosaur that was attacked
     * @param move     the move used by the attacker
     */
    public static void onAttacked(Dinosaur attacker, Dinosaur defender, Move move) {
        if (attacker == null || defender == null || move == null) {
            return;
        }

        Ability ability = defender.getAbility();
        if (ability != null && "Spiky Body".equalsIgnoreCase(ability.getName())
                && move.getDamage() > 0) {
            attacker.adjustHealth(-10);
        }
    }

    /**
     * Applies end of turn effects for the active dinosaur.
     */
    public static void endTurn(Dinosaur active) {
        Ability ability = active == null ? null : active.getAbility();
        if (ability != null && "Adrenaline".equalsIgnoreCase(ability.getName())) {
            active.adjustStamina(10);
        }
    }
}
