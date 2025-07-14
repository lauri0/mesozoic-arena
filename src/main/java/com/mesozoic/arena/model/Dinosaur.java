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
    private final int attack;
    private int stamina;
    private final List<Move> moves;

    public Dinosaur(String name, int health, int speed, String imagePath, int stamina, int attack,
            List<Move> moves) {
        this.name = name;
        this.health = health;
        this.speed = speed;
        this.imagePath = imagePath;
        this.attack = attack;
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

    public int getAttack() {
        return attack;
    }

    public int getStamina() {
        return stamina;
    }

    /**
     * Indicates whether this dinosaur has enough stamina to perform the given
     * move.
     */
    public boolean canUse(Move move) {
        if (move == null) {
            return false;
        }
        return stamina + move.getStaminaChange() >= 0;
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
