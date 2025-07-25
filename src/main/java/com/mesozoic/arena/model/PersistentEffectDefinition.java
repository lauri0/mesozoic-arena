package com.mesozoic.arena.model;

/**
 * Defines a persistent effect loaded from configuration.
 */
public class PersistentEffectDefinition {
    private final String name;
    private final String description;
    private final int duration;

    public PersistentEffectDefinition(String name, String description, int duration) {
        this.name = name;
        this.description = description == null ? "" : description;
        this.duration = duration;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getDuration() {
        return duration;
    }
}
