package com.mesozoic.arena;

import com.mesozoic.arena.data.DinosaurLoader;
import com.mesozoic.arena.model.Player;
import com.mesozoic.arena.util.Config;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TeamGenerationTest {
    @Test
    public void testRandomTeamUsesBudgetEfficiently() throws Exception {
        DinosaurLoader loader = new DinosaurLoader();
        Player team = loader.createRandomPlayer();
        int budget = Config.supplyBudget();
        int total = team.getTotalSupply();
        assertTrue(total >= budget - 2, "team supply too low: " + total);
    }
}
