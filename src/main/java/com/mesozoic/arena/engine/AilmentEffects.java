package com.mesozoic.arena.engine;

import com.mesozoic.arena.model.Ailment;
import com.mesozoic.arena.model.Dinosaur;

/**
 * Utility methods for applying ailment based effects.
 */
public final class AilmentEffects {
    private AilmentEffects() {
    }

    public static int modifyHealing(Dinosaur dinosaur, int healAmount) {
        if (dinosaur == null) {
            return healAmount;
        }
        return dinosaur.hasAilment("Bleeding") ? healAmount / 2 : healAmount;
    }

    public static void endTurn(Dinosaur dinosaur) {
        if (dinosaur != null && dinosaur.hasAilment("Bleeding")) {
            dinosaur.adjustHealth(-10);
        }
    }

    public static void applyAilment(Dinosaur dinosaur, Ailment ailment) {
        if (dinosaur != null && ailment != null) {
            dinosaur.addAilment(ailment);
        }
    }
}
