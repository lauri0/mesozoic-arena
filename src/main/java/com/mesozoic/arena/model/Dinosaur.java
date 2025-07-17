package com.mesozoic.arena.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a dinosaur combatant.
 */
public class Dinosaur {
    private final String name;
    private int health;
    private final int maxHealth;
    private final int speed;
    private int speedStage = 0;
    private final String imagePath;
    private final Ability ability;
    private final double headAttack;
    private final double bodyAttack;
    private int attackStage = 0;
    private final List<Move> moves;
    private final List<DinoType> types;
    private final List<Ailment> ailments = new ArrayList<>();

    public Dinosaur(String name, int health, int speed, String imagePath,
                    double headAttack, double bodyAttack, List<Move> moves, Ability ability) {
        this(name, health, speed, imagePath, headAttack, bodyAttack, moves, ability,
                List.of(DinoType.BITER));
    }

    public Dinosaur(String name, int health, int speed, String imagePath,
                    double headAttack, double bodyAttack, List<Move> moves,
                    Ability ability, List<DinoType> types) {
        this.name = name;
        this.health = health;
        this.maxHealth = health;
        this.speed = speed;
        this.imagePath = imagePath;
        this.ability = ability;
        this.headAttack = headAttack;
        this.bodyAttack = bodyAttack;
        if (moves == null) {
            this.moves = new ArrayList<>();
        } else {
            this.moves = new ArrayList<>(moves);
        }
        if (types == null || types.isEmpty()) {
            this.types = new ArrayList<>(List.of(DinoType.BITER));
        } else {
            this.types = new ArrayList<>(types);
        }
    }

    public String getName() {
        return name;
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public int getSpeed() {
        return speed;
    }

    public String getImagePath() {
        return imagePath;
    }

    public double getHeadAttack() {
        return headAttack;
    }

    public double getBodyAttack() {
        return bodyAttack;
    }

    public Ability getAbility() {
        return ability;
    }



    public List<Move> getMoves() {
        return new ArrayList<>(moves);
    }

    public List<DinoType> getTypes() {
        return new ArrayList<>(types);
    }

    public double getMultiplierFrom(DinoType attackType) {
        double multiplier = 1.0;
        for (DinoType type : types) {
            multiplier *= type.getMultiplierFrom(attackType);
        }
        return multiplier;
    }

    public List<Ailment> getAilments() {
        return new ArrayList<>(ailments);
    }

    public boolean hasAilment(String ailmentName) {
        if (ailmentName == null) {
            return false;
        }
        for (Ailment ailment : ailments) {
            if (ailmentName.equalsIgnoreCase(ailment.getName())) {
                return true;
            }
        }
        return false;
    }

    public void addAilment(Ailment ailment) {
        if (ailment != null && !hasAilment(ailment.getName())) {
            ailments.add(ailment);
        }
    }

    public void removeAilment(String ailmentName) {
        if (ailmentName == null) {
            return;
        }
        ailments.removeIf(a -> ailmentName.equalsIgnoreCase(a.getName()));
    }

    public void adjustHealth(int amount) {
        health += amount;
        if (health > maxHealth) {
            health = maxHealth;
        }
        if (health < 0) {
            health = 0;
        }
    }


    public int getAttackStage() {
        return attackStage;
    }

    public int getSpeedStage() {
        return speedStage;
    }

    public void adjustAttackStage(int amount) {
        attackStage = clampStage(attackStage + amount);
    }

    public void adjustSpeedStage(int amount) {
        speedStage = clampStage(speedStage + amount);
    }

    public void resetStages() {
        attackStage = 0;
        speedStage = 0;
    }

    public double getEffectiveHeadAttack() {
        return headAttack * stageMultiplier(attackStage);
    }

    public double getEffectiveBodyAttack() {
        return bodyAttack * stageMultiplier(attackStage);
    }

    public int getEffectiveSpeed() {
        return Math.round((float) speed * stageMultiplier(speedStage));
    }

    private int clampStage(int stage) {
        if (stage > 6) {
            return 6;
        }
        if (stage < -6) {
            return -6;
        }
        return stage;
    }

    private static float stageMultiplier(int stage) {
        if (stage >= 0) {
            return (4f + stage) / 4f;
        }
        return 4f / (4 - stage);
    }
}
