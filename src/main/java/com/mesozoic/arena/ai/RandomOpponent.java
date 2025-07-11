package com.mesozoic.arena.ai;

import com.mesozoic.arena.model.Dinosaur;
import com.mesozoic.arena.model.Move;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Simple AI that selects a random move the dinosaur has enough stamina to use.
 */
public class RandomOpponent {
    private final Random random = new Random();

    /**
     * Chooses a move for the given dinosaur based on current stamina.
     *
     * @param self the acting dinosaur
     * @return a usable move or {@code null} if none can be performed
     */
    public Move chooseMove(Dinosaur self) {
        if (self == null) {
            return null;
        }
        List<Move> usable = new ArrayList<>();
        for (Move move : self.getMoves()) {
            if (self.getStamina() >= move.getStaminaCost()) {
                usable.add(move);
            }
        }
        if (usable.isEmpty()) {
            return null;
        }
        return usable.get(random.nextInt(usable.size()));
    }
}
