package com.mesozoic.arena.model;

/**
 * Active instance of a persistent effect on a team.
 */
public class PersistentEffect {
    private final PersistentEffectDefinition definition;
    private int remaining;

    public PersistentEffect(PersistentEffectDefinition definition) {
        this.definition = definition;
        this.remaining = definition.getDuration();
    }

    public PersistentEffect(PersistentEffect other) {
        this.definition = other.definition;
        this.remaining = other.remaining;
    }

    public PersistentEffect copy() {
        return new PersistentEffect(this);
    }

    public String getName() {
        return definition.getName();
    }

    public String getDescription() {
        return definition.getDescription();
    }

    public int getDuration() {
        return definition.getDuration();
    }

    public int getRemaining() {
        return remaining;
    }

    public void tick() {
        if (definition.getDuration() <= 0) {
            return;
        }
        if (remaining > 0) {
            remaining--;
        }
    }

    public boolean isExpired() {
        return definition.getDuration() > 0 && remaining <= 0;
    }
}
