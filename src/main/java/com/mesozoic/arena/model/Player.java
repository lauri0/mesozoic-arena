package com.mesozoic.arena.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a player controlling a team of dinosaurs.
 */
public class Player {
    private final List<Dinosaur> dinosaurs;
    private Dinosaur activeDinosaur;
    private Dinosaur queuedSwitch;

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
            if (activeDinosaur != null && !activeDinosaur.equals(dinosaur)) {
                activeDinosaur.resetStages();
            }
            this.activeDinosaur = dinosaur;
        }
    }

    /**
     * Queues a switch to the specified dinosaur for the next turn.
     */
    public void queueSwitch(Dinosaur dinosaur) {
        if (dinosaurs.contains(dinosaur) && !dinosaur.equals(activeDinosaur)) {
            queuedSwitch = dinosaur;
        }
    }

    /**
     * Returns the dinosaur selected for switching or {@code null} if none.
     */
    public Dinosaur getQueuedSwitch() {
        return queuedSwitch;
    }

    /**
     * Clears any queued switch action.
     */
    public void clearQueuedSwitch() {
        queuedSwitch = null;
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

    /**
     * Creates a deep copy of this player including cloned dinosaurs.
     */
    public Player copy() {
        List<Dinosaur> copies = new ArrayList<>();
        for (Dinosaur d : dinosaurs) {
            copies.add(d.copy());
        }

        Player clone = new Player(copies);
        if (activeDinosaur != null) {
            int index = dinosaurs.indexOf(activeDinosaur);
            if (index >= 0 && index < copies.size()) {
                clone.activeDinosaur = copies.get(index);
            }
        }
        return clone;
    }
}
