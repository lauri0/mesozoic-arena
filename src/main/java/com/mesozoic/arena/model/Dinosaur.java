package com.mesozoic.arena.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a dinosaur combatant.
 */
public class Dinosaur {
    private final String name;
    private int health;
    private final int speed;
    private final String imagePath;
    private int stamina;
    private final List<Move> moves;

    public Dinosaur(String name, int health, int speed, String imagePath, int stamina, List<Move> moves) {
        this.name = name;
        this.health = health;
        this.speed = speed;
        this.imagePath = imagePath;
        this.stamina = stamina;
        if (moves == null) {
            this.moves = new ArrayList<>();
        } else {
            this.moves = new ArrayList<>(moves);
        }
    }

    public String getName() {
        return name;
    }

    public int getHealth() {
        return health;
    }

    public int getSpeed() {
        return speed;
    }

    public String getImagePath() {
        return imagePath;
    }

    public int getStamina() {
        return stamina;
    }

    public List<Move> getMoves() {
        return new ArrayList<>(moves);
    }

    public void adjustHealth(int amount) {
        health += amount;
    }

    public void adjustStamina(int amount) {
        stamina += amount;
    }
}
