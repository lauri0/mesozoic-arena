package com.mesozoic.arena.model;

import java.util.List;

/**
 * Represents the action of switching to another dinosaur.
 */
public class SwitchMove extends Move {
    private final int targetIndex;

    public SwitchMove(Dinosaur target, int index) {
        super("Switch to " + target.getName(), 0, 0, List.of());
        this.targetIndex = index;
    }

    /**
     * Index of the dinosaur to switch to in the player's roster.
     */
    public int getTargetIndex() {
        return targetIndex;
    }
}
