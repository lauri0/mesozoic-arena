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
    private final int supply;
    private int speedStage = 0;
    private final String imagePath;
    private final Ability ability;
    private final double headAttack;
    private final double bodyAttack;
    private int headAttackStage = 0;
    private int bodyAttackStage = 0;
    private final List<Move> moves;
    private final List<DinoType> types;
    private final List<Ailment> ailments = new ArrayList<>();
    private boolean camouflageUsed = false;

    public Dinosaur(String name, int health, int speed, String imagePath,
                    double headAttack, double bodyAttack, List<Move> moves, Ability ability) {
        this(name, health, speed, imagePath, headAttack, bodyAttack, moves, ability, 0,
                List.of(DinoType.BITER));
    }

    public Dinosaur(String name, int health, int speed, String imagePath,
                    double headAttack, double bodyAttack, List<Move> moves,
                    Ability ability, List<DinoType> types) {
        this(name, health, speed, imagePath, headAttack, bodyAttack, moves, ability, 0, types);
    }

    public Dinosaur(String name, int health, int speed, String imagePath,
                    double headAttack, double bodyAttack, List<Move> moves,
                    Ability ability, int supply, List<DinoType> types) {
        this.name = name;
        this.health = health;
        this.maxHealth = health;
        this.speed = speed;
        this.imagePath = imagePath;
        this.ability = ability;
        this.headAttack = headAttack;
        this.bodyAttack = bodyAttack;
        this.supply = supply;
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

    public int getSupply() {
        return supply;
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

    public String printTypes() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < types.size(); i++) {
            if (i > 0 && i == types.size() - 1) {
                sb.append(" and ");
            } else if (i > 0) {
                sb.append(", ");
            }
            sb.append(types.get(i).name());
        }
        return sb.toString();
    }

    /**
     * Checks if this dinosaur has the specified type.
     *
     * @param searchType the type to look for
     * @return {@code true} if the dinosaur has the type, otherwise {@code false}
     */
    public boolean hasType(DinoType searchType) {
        if (searchType == null) {
            return false;
        }
        for (DinoType currentType : types) {
            if (currentType == searchType) {
                return true;
            }
        }
        return false;
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

    public boolean isCamouflageUsed() {
        return camouflageUsed;
    }

    public void setCamouflageUsed(boolean used) {
        this.camouflageUsed = used;
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


    public int getHeadAttackStage() {
        return headAttackStage;
    }

    public int getBodyAttackStage() {
        return bodyAttackStage;
    }

    public int getSpeedStage() {
        return speedStage;
    }

    public void adjustHeadAttackStage(int amount) {
        headAttackStage = clampStage(headAttackStage + amount);
    }

    public void adjustBodyAttackStage(int amount) {
        bodyAttackStage = clampStage(bodyAttackStage + amount);
    }

    public void adjustSpeedStage(int amount) {
        speedStage = clampStage(speedStage + amount);
    }

    public void resetStages() {
        headAttackStage = 0;
        bodyAttackStage = 0;
        speedStage = 0;
    }

    public double getEffectiveHeadAttack() {
        return headAttack * stageMultiplier(headAttackStage);
    }

    public double getEffectiveBodyAttack() {
        return bodyAttack * stageMultiplier(bodyAttackStage);
    }

    public int getEffectiveSpeed() {
        return Math.round((float) speed * stageMultiplier(speedStage));
    }

    /**
     * Creates a deep copy of this dinosaur instance.
     */
    public Dinosaur copy() {
        List<Move> moveCopies = new ArrayList<>();
        for (Move move : moves) {
            moveCopies.add(move.copy());
        }
        List<DinoType> typeCopies = new ArrayList<>(types);

        Dinosaur clone = new Dinosaur(name, maxHealth, speed, imagePath,
                headAttack, bodyAttack, moveCopies, ability, supply, typeCopies);
        clone.health = health;
        clone.headAttackStage = headAttackStage;
        clone.bodyAttackStage = bodyAttackStage;
        clone.speedStage = speedStage;
        clone.ailments.addAll(ailments);
        clone.camouflageUsed = camouflageUsed;
        return clone;
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
            return (2f + stage) / 2f;
        }
        return 2f / (2 - stage);
    }
}
