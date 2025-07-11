package com.mesozoic.arena.ai;

import com.mesozoic.arena.model.Dinosaur;
import com.mesozoic.arena.model.Move;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Simple AI that selects a random move the dinosaur has enough stamina to use.
 */
public class RandomOpponent implements OpponentAgent {
    private final Random random = new Random();

    @Override
    public Move chooseMove(Dinosaur self, Dinosaur enemy) {
        if (self == null) {
            return null;
        }
        List<Move> usable = getUsableMoves(self);
        if (usable.isEmpty()) {
            return null;
        }
        return usable.get(random.nextInt(usable.size()));
    }

    private List<Move> getUsableMoves(Dinosaur dinosaur) {
        List<Move> usable = new ArrayList<>();
        for (Move move : dinosaur.getMoves()) {
            if (dinosaur.getStamina() >= move.getStaminaCost()) {
                usable.add(move);
            }
        }
        return usable;
    }
}
