package com.mesozoic.arena.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a player controlling a team of dinosaurs.
 */
public class Player {
    private final List<Dinosaur> dinosaurs;
    private Dinosaur activeDinosaur;

    public Player(List<Dinosaur> dinosaurs) {
        if (dinosaurs == null) {
            this.dinosaurs = new ArrayList<>();
        } else {
            this.dinosaurs = new ArrayList<>(dinosaurs);
        }
        if (!this.dinosaurs.isEmpty()) {
            this.activeDinosaur = this.dinosaurs.get(0);
        }
    }

    public List<Dinosaur> getDinosaurs() {
        return new ArrayList<>(dinosaurs);
    }

    public Dinosaur getActiveDinosaur() {
        return activeDinosaur;
    }

    public void setActiveDinosaur(Dinosaur dinosaur) {
        if (dinosaurs.contains(dinosaur)) {
            this.activeDinosaur = dinosaur;
        }
    }

    /**
     * Removes the given dinosaur from the player's roster. If the removed
     * dinosaur was active, the next available dinosaur becomes active.
     */
    public void removeDinosaur(Dinosaur dinosaur) {
        dinosaurs.remove(dinosaur);
        if (dinosaur.equals(activeDinosaur)) {
            activateNextDinosaur();
        }
    }

    /**
     * Sets the next dinosaur in the roster as active. Returns the new active
     * dinosaur or {@code null} if none remain.
     */
    public Dinosaur activateNextDinosaur() {
        if (dinosaurs.isEmpty()) {
            activeDinosaur = null;
        } else {
            activeDinosaur = dinosaurs.get(0);
        }
        return activeDinosaur;
    }

    /**
     * Indicates whether the player still has dinosaurs remaining.
     */
    public boolean hasRemainingDinosaurs() {
        return !dinosaurs.isEmpty();
    }
}
