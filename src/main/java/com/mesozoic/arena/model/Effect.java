package com.mesozoic.arena.model;

/**
 * Simple representation of a move effect.
 */
public class Effect {
    private final String name;

    public Effect(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
