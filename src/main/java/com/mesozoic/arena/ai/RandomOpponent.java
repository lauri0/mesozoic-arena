package com.mesozoic.arena.ai;

import com.mesozoic.arena.model.Move;
import com.mesozoic.arena.model.Dinosaur;
import com.mesozoic.arena.model.Player;
import com.mesozoic.arena.engine.TurnRecord;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Simple AI that selects a random move from the dinosaur's move list.
 */
public class RandomOpponent implements OpponentAgent {
    private final Random random = new Random();

    @Override
    public Move chooseMove(Player self, Player enemy, List<TurnRecord> history) {
        Dinosaur active = self.getActiveDinosaur();
        if (active == null) {
            return null;
        }
        List<Move> moves = active.getMoves();
        if (moves.isEmpty()) {
            return null;
        }
        return moves.get(random.nextInt(moves.size()));
    }

}
