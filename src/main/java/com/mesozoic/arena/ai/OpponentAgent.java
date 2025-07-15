package com.mesozoic.arena.ai;

import com.mesozoic.arena.model.Move;
import com.mesozoic.arena.model.Player;
import com.mesozoic.arena.engine.TurnRecord;
import java.util.List;

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
    Move chooseMove(Player self, Player enemy, List<TurnRecord> history);
}
