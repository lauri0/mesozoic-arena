package com.mesozoic.arena.model;

/**
 * Represents a persistent ailment affecting a dinosaur.
 */
public class Ailment {
    private final String name;

    public Ailment(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
