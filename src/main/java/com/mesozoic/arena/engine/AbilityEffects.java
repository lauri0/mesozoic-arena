package com.mesozoic.arena.engine;

import com.mesozoic.arena.model.Dinosaur;
import com.mesozoic.arena.model.Ability;
import com.mesozoic.arena.model.Move;
import java.util.Random;

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
            opponent.adjustHeadAttackStage(-1);
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

        if ("Tough".equalsIgnoreCase(name)
                && defender.getHealth() == defender.getMaxHealth()) {
            return Math.round(damage / 3f);
        }

        if ("Resilient".equalsIgnoreCase(name)
                && defender.getHealth() > 20
                && damage >= defender.getHealth()) {
            return defender.getHealth() - 1;
        }

        return damage;
    }

    /**
     * Adjusts move priority based on the user's ability.
     *
     * @param user the dinosaur performing the move
     * @param move the move being used
     * @return the priority after ability modifications
     */
    public static int modifyPriority(Dinosaur user, Move move) {
        if (user == null || move == null) {
            return move == null ? 0 : move.getPriority();
        }
        Ability ability = user.getAbility();
        if (ability != null && "Supporter".equalsIgnoreCase(ability.getName())
                && move.getDamage() == 0) {
            return move.getPriority() + 1;
        }
        return move.getPriority();
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
        if (ability == null) {
            return;
        }
        String name = ability.getName();
        if ("Spiky Body".equalsIgnoreCase(name) && move.getDamage() > 0) {
            attacker.adjustHealth(-10);
        }
        if ("Tiring".equalsIgnoreCase(name) && move.getDamage() > 0) {
            attacker.adjustSpeedStage(-1);
        }
    }

    /**
     * Applies end of turn effects for the active dinosaur.
     */
    public static void endTurn(Dinosaur active) {
        // no stamina related end-of-turn effects remain
    }

    /**
     * Triggers effects when the user knocks out an opposing dinosaur.
     *
     * @param attacker the dinosaur delivering the final blow
     * @param defender the dinosaur that fainted
     */
    public static void onKnockOut(Dinosaur attacker, Dinosaur defender) {
        if (attacker == null || defender == null) {
            return;
        }
        Ability ability = attacker.getAbility();
        if (ability == null) {
            return;
        }
        String name = ability.getName();
        if ("Berserk".equalsIgnoreCase(name)) {
            attacker.adjustHeadAttackStage(1);
        }
        if ("Scavenger".equalsIgnoreCase(name)) {
            int healAmount = AilmentEffects.modifyHealing(attacker, 20);
            attacker.adjustHealth(healAmount);
        }
    }

    /**
     * Adjusts the accuracy of a move based on the user's ability.
     *
     * @param user the dinosaur using the move
     * @param move the move being used
     * @return the accuracy after ability modifications
     */
    public static double modifyAccuracy(Dinosaur user, Move move) {
        if (user == null || move == null) {
            return move == null ? 0.0 : move.getAccuracy();
        }
        Ability ability = user.getAbility();
        if (ability != null && "Sharpshooter".equalsIgnoreCase(ability.getName())) {
            return Math.min(1.0, move.getAccuracy() + 0.15);
        }
        return move.getAccuracy();
    }

    /**
     * Determines if the first incoming attack should miss due to Camouflage.
     *
     * @param defender the dinosaur being targeted
     * @param random   source of randomness
     * @return {@code true} if the attack misses, otherwise {@code false}
     */
    public static boolean firstAttackMiss(Dinosaur defender, Random random) {
        if (defender == null || random == null) {
            return false;
        }
        Ability ability = defender.getAbility();
        if (ability != null && "Camouflage".equalsIgnoreCase(ability.getName())
                && !defender.isCamouflageUsed()) {
            defender.setCamouflageUsed(true);
            return random.nextDouble() < 0.5;
        }
        return false;
    }

    /**
     * Triggers effects when the dinosaur performs a move.
     *
     * @param user the dinosaur using the move
     */
    public static void onMoveUsed(Dinosaur user) {
        if (user == null) {
            return;
        }
        Ability ability = user.getAbility();
        if (ability != null && "Regenerator".equalsIgnoreCase(ability.getName())) {
            int healAmount = AilmentEffects.modifyHealing(user, 10);
            user.adjustHealth(healAmount);
        }
    }
}
