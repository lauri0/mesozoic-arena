package com.mesozoic.arena.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an action that a dinosaur can perform in battle.
 */
public class Move {
    private final String name;
    private final int damage;
    private final int priority;
    private final List<Effect> effects;
    private final String description;
    private final MoveType kind;
    private final DinoType type;
    private final double accuracy;

    public Move(String name, int damage, int priority, List<Effect> effects) {
        this(name, damage, priority, "", MoveType.BODY, DinoType.BITER, effects, 1.0);
    }

    public Move(String name, int damage, int priority, String description, List<Effect> effects) {
        this(name, damage, priority, description, MoveType.BODY, DinoType.BITER, effects, 1.0);
    }

    public Move(String name, int damage, int priority, String description, MoveType kind, List<Effect> effects) {
        this(name, damage, priority, description, kind, DinoType.BITER, effects, 1.0);
    }

    public Move(String name, int damage, int priority, String description, MoveType kind,
            DinoType type, List<Effect> effects, double accuracy) {
        this.name = name;
        this.damage = damage;
        this.priority = priority;
        if (effects == null) {
            this.effects = new ArrayList<>();
        } else {
            this.effects = new ArrayList<>(effects);
        }
        this.description = description == null ? "" : description;
        this.kind = kind == null ? MoveType.BODY : kind;
        this.type = type == null ? DinoType.BITER : type;
        this.accuracy = accuracy;
    }

    public DinoType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public int getDamage() {
        return damage;
    }

    public int getPriority() {
        return priority;
    }

    public List<Effect> getEffects() {
        return new ArrayList<>(effects);
    }

    public String getDescription() {
        return description;
    }

    public MoveType getKind() {
        return kind;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public String getDescriptionWithDamage(Dinosaur user) {
        double attackValue = kind == MoveType.HEAD
                ? user.getEffectiveHeadAttack()
                : user.getEffectiveBodyAttack();
        long realDamage = Math.round(damage * attackValue);
        String withDamage = description.replace("XX", String.valueOf(realDamage));
        return withDamage.replace("YY", String.valueOf(accuracy));
    }
}
