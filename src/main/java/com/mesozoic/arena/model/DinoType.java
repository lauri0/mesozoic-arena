package com.mesozoic.arena.model;

import java.util.EnumSet;

/**
 * Represents the elemental typing for dinosaurs and moves.
 */
public enum DinoType {
    BITER,
    BLEEDER,
    CHARGER,
    CRUSHER,
    DEFENDER,
    GRAZER,
    IMPALER,
    LANDSCAPER,
    RUNNER,
    SLASHER,
    SWIMMER;

    private EnumSet<DinoType> weakTo;
    private EnumSet<DinoType> resistantTo;

    static {
        BITER.weakTo = EnumSet.of(CRUSHER, IMPALER);
        BITER.resistantTo = EnumSet.of(BITER, SLASHER);

        BLEEDER.weakTo = EnumSet.of(CRUSHER, RUNNER);
        BLEEDER.resistantTo = EnumSet.of(BLEEDER, LANDSCAPER);

        CHARGER.weakTo = EnumSet.of(SLASHER, GRAZER);
        CHARGER.resistantTo = EnumSet.of(CHARGER, CRUSHER, BLEEDER, SWIMMER);

        CRUSHER.weakTo = EnumSet.of(SLASHER, CHARGER);
        CRUSHER.resistantTo = EnumSet.of(DEFENDER, GRAZER, IMPALER);

        DEFENDER.weakTo = EnumSet.of(CRUSHER, LANDSCAPER);
        DEFENDER.resistantTo = EnumSet.of(BITER, BLEEDER, DEFENDER, GRAZER, SLASHER, SWIMMER);

        GRAZER.weakTo = EnumSet.of(BITER, BLEEDER, CRUSHER, SLASHER);
        GRAZER.resistantTo = EnumSet.of(DEFENDER, GRAZER);

        IMPALER.weakTo = EnumSet.of(DEFENDER, RUNNER);
        IMPALER.resistantTo = EnumSet.of(BLEEDER, CHARGER, IMPALER);

        LANDSCAPER.weakTo = EnumSet.of(BLEEDER);
        LANDSCAPER.resistantTo = EnumSet.of(CRUSHER, LANDSCAPER);

        RUNNER.weakTo = EnumSet.of(BITER, GRAZER, SWIMMER);
        RUNNER.resistantTo = EnumSet.of(RUNNER, LANDSCAPER);

        SLASHER.weakTo = EnumSet.of(CHARGER, DEFENDER);
        SLASHER.resistantTo = EnumSet.of(LANDSCAPER, SLASHER);

        SWIMMER.weakTo = EnumSet.of(BLEEDER);
        SWIMMER.resistantTo = EnumSet.of(DEFENDER, SWIMMER);
    }

    /**
     * Returns the damage multiplier when attacked by the given type.
     */
    public double getMultiplierFrom(DinoType attackType) {
        if (attackType == null) {
            return 1.0;
        }
        if (weakTo.contains(attackType)) {
            return 2.0;
        }
        if (resistantTo.contains(attackType)) {
            return 0.5;
        }
        return 1.0;
    }

    /**
     * Parses a type label, returning BITER if the value is invalid.
     */
    public static DinoType fromString(String label) {
        if (label == null) {
            return BITER;
        }
        for (DinoType type : values()) {
            if (type.name().equalsIgnoreCase(label)) {
                return type;
            }
        }
        return BITER;
    }
}
