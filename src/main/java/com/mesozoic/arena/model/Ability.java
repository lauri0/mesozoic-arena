package com.mesozoic.arena.model;

/**
 * Represents a passive ability that can activate without using a move.
 */
public class Ability {
    private final String name;
    private final String description;

    public Ability(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
