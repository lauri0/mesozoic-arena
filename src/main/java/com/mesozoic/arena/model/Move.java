package com.mesozoic.arena.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an action that a dinosaur can perform in battle.
 */
public class Move {
    private final String name;
    private final int damage;
    private final int staminaCost;
    private final List<Effect> effects;

    public Move(String name, int damage, int staminaCost, List<Effect> effects) {
        this.name = name;
        this.damage = damage;
        this.staminaCost = staminaCost;
        if (effects == null) {
            this.effects = new ArrayList<>();
        } else {
            this.effects = new ArrayList<>(effects);
        }
    }

    public String getName() {
        return name;
    }

    public int getDamage() {
        return damage;
    }

    public int getStaminaCost() {
        return staminaCost;
    }

    public List<Effect> getEffects() {
        return new ArrayList<>(effects);
    }
}
