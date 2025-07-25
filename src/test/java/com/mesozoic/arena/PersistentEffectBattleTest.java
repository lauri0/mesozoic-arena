package com.mesozoic.arena;

import com.mesozoic.arena.engine.Battle;
import com.mesozoic.arena.model.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class PersistentEffectBattleTest {
    @Test
    public void testTailwindAndRocksMechanics() {
        Move tailwind = new Move("Tailwind", 0, 0,
                List.of(new Effect("tailwind")));
        Move rocks = new Move("Rocks", 0, 0,
                List.of(new Effect("rocks")));
        Dinosaur a1 = new Dinosaur("A1", 100, 100, "", 1, 1,
                List.of(tailwind), null);
        Dinosaur a2 = new Dinosaur("A2", 80, 50, "", 1, 1,
                List.of(), null);
        Player p1 = new Player(List.of(a1, a2));
        Dinosaur b1 = new Dinosaur("B1", 100, 80, "", 1, 1,
                List.of(rocks), null);
        Player p2 = new Player(List.of(b1));

        Battle battle = new Battle(p1, p2);
        battle.executeRound(tailwind, rocks);

        assertTrue(p1.hasPersistentEffect("Tailwind"));
        assertEquals(4, p1.getPersistentEffects().get(0).getRemaining());

        p1.queueSwitch(a2);
        battle.executeRound(null, null);
        int expected = Math.round(a2.getMaxHealth() * 0.125f);
        assertEquals(a2.getMaxHealth() - expected, a2.getHealth());
    }
}
