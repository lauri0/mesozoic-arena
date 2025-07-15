package com.mesozoic.arena.engine;

/**
 * Captures the actions taken by both players during a single turn.
 */
public class TurnRecord {
    private final String playerAction;
    private final String npcAction;

    public TurnRecord(String playerAction, String npcAction) {
        this.playerAction = playerAction;
        this.npcAction = npcAction;
    }

    public String getPlayerAction() {
        return playerAction;
    }

    public String getNpcAction() {
        return npcAction;
    }
}
