package com.mesozoic.arena.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an action that a dinosaur can perform in battle.
 */
public class Move {
    private final String name;
    private final int damage;
    private final int staminaChange;
    private final int priority;
    private final List<Effect> effects;
    private final String description;
    private final MoveType type;

    public Move(String name, int damage, int staminaChange, int priority, List<Effect> effects) {
        this(name, damage, staminaChange, priority, "", MoveType.BODY, effects);
    }

    public Move(String name, int damage, int staminaChange, int priority, String description, List<Effect> effects) {
        this(name, damage, staminaChange, priority, description, MoveType.BODY, effects);
    }

    public Move(String name, int damage, int staminaChange, int priority, String description, MoveType type, List<Effect> effects) {
        this.name = name;
        this.damage = damage;
        this.staminaChange = staminaChange;
        this.priority = priority;
        if (effects == null) {
            this.effects = new ArrayList<>();
        } else {
            this.effects = new ArrayList<>(effects);
        }
        this.description = description == null ? "" : description;
        this.type = type == null ? MoveType.BODY : type;
    }

    public String getName() {
        return name;
    }

    public int getDamage() {
        return damage;
    }

    public int getStaminaChange() {
        return staminaChange;
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

    public MoveType getType() {
        return type;
    }

    public String getDescriptionWithDamageAndStamina(Dinosaur user) {
        double attackValue = type == MoveType.HEAD
                ? user.getEffectiveHeadAttack()
                : user.getEffectiveBodyAttack();
        long realDamage = Math.round(damage * attackValue);
        String descriptionWithStamina = description.replace("YY", String.valueOf(Math.abs(staminaChange)));
        return descriptionWithStamina.replace("XX", String.valueOf(realDamage));
    }
}
