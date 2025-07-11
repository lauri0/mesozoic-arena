package com.mesozoic.arena.ai;

import com.mesozoic.arena.model.Dinosaur;
import com.mesozoic.arena.model.Move;

/**
 * Strategy interface for selecting a move in battle.
 */
public interface OpponentAgent {
    /**
     * Chooses a move for the acting dinosaur.
     *
     * @param self the acting dinosaur
     * @param enemy the opposing dinosaur
     * @return the selected move or {@code null} if none can be performed
     */
    Move chooseMove(Dinosaur self, Dinosaur enemy);
}
