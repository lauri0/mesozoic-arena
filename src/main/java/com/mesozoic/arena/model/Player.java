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
}
