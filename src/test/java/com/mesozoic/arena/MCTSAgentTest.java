package com.mesozoic.arena;

import com.mesozoic.arena.ai.mcts.MCTSAgent;
import com.mesozoic.arena.engine.Battle;
import com.mesozoic.arena.model.Dinosaur;
import com.mesozoic.arena.model.Move;
import com.mesozoic.arena.model.Player;
import com.mesozoic.arena.model.Ability;
import com.mesozoic.arena.util.Config;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class MCTSAgentTest {

    private static Properties configProperties() throws Exception {
        Field field = Config.class.getDeclaredField("properties");
        field.setAccessible(true);
        return (Properties) field.get(null);
    }

    private static String setUseLLMAgent(boolean value) throws Exception {
        Properties props = configProperties();
        String previous = props.getProperty("useLLMAgent");
        props.setProperty("useLLMAgent", Boolean.toString(value));
        return previous;
    }

    private static void restoreUseLLMAgent(String previous) throws Exception {
        Properties props = configProperties();
        if (previous == null) {
            props.remove("useLLMAgent");
        } else {
            props.setProperty("useLLMAgent", previous);
        }
    }

    @Test
    public void testAgentChoosesWinningMove() throws Exception {
        String original = setUseLLMAgent(false);
        try {
            Move win = new Move("Win", 10, 0, List.of());
            Move wait = new Move("Wait", 0, 0, List.of());
            Move foeAttack = new Move("Strike", 10, 0, List.of());
            Dinosaur agentDino = new Dinosaur("Agent", 10, 10,
                    "assets/animals/allosaurus.png", 1, 1,
                    List.of(win, wait), null);
            Dinosaur foeDino = new Dinosaur("Foe", 1, 5,
                    "assets/animals/allosaurus.png", 1, 1,
                    List.of(foeAttack), null);
            Player self = new Player(List.of(agentDino));
            Player enemy = new Player(List.of(foeDino));
            MCTSAgent agent = new MCTSAgent(50, new Random(0));

            for (int i = 0; i < 3; i++) {
                Move chosen = agent.chooseMove(self, enemy, List.of());
                assertEquals("Win", chosen.getName());

                String stats = agent.getLastStats();
                assertFalse(stats.isBlank());
                assertTrue(stats.contains("Win"));
            }
        } finally {
            restoreUseLLMAgent(original);
        }
    }

    @Test
    public void testOpponentIntimidateDoesNotChangeRealPlayers() throws Exception {
        String original = setUseLLMAgent(false);
        try {
            Move win = new Move("Win", 10, 0, List.of());
            Move wait = new Move("Wait", 0, 0, List.of());
            Dinosaur agentDino = new Dinosaur("Agent", 10, 10,
                    "assets/animals/allosaurus.png", 1, 1,
                    List.of(win), null);
            Dinosaur intimidator = new Dinosaur("Foe", 10, 5,
                    "assets/animals/allosaurus.png", 1, 1,
                    List.of(wait), new Ability("Intimidate", ""));
            Player self = new Player(List.of(agentDino));
            Player enemy = new Player(List.of(intimidator));
            MCTSAgent agent = new MCTSAgent(50, new Random(0));

            for (int i = 0; i < 3; i++) {
                Move chosen = agent.chooseMove(self, enemy, List.of());
                assertEquals("Win", chosen.getName());
                assertEquals(0, self.getActiveDinosaur().getAttackStage());
                assertEquals(0, enemy.getActiveDinosaur().getAttackStage());
            }
        } finally {
            restoreUseLLMAgent(original);
        }
    }

    @Test
    public void testBattleUsesMctsWhenLlmDisabled() throws Exception {
        String original = setUseLLMAgent(false);
        try {
            Move wait = new Move("Wait", 0, 0, List.of());
            Dinosaur playerDino = new Dinosaur("Player", 10, 5,
                    "assets/animals/allosaurus.png", 1, 1,
                    List.of(wait), null);
            Dinosaur npcDino = new Dinosaur("NPC", 10, 5,
                    "assets/animals/allosaurus.png", 1, 1,
                    List.of(wait), null);
            Player p1 = new Player(List.of(playerDino));
            Player p2 = new Player(List.of(npcDino));
            Battle battle = new Battle(p1, p2);

            assertTrue(battle.getOpponentAI() instanceof MCTSAgent);
        } finally {
            restoreUseLLMAgent(original);
        }
    }

    @Test
    public void testBattleLogsMctsStats() throws Exception {
        String original = setUseLLMAgent(false);
        try {
            Move wait = new Move("Wait", 0, 0, List.of());
            Dinosaur playerDino = new Dinosaur("Player", 10, 5,
                    "assets/animals/allosaurus.png", 1, 1,
                    List.of(wait), null);
            Dinosaur npcDino = new Dinosaur("NPC", 10, 5,
                    "assets/animals/allosaurus.png", 1, 1,
                    List.of(wait), null);
            Player p1 = new Player(List.of(playerDino));
            Player p2 = new Player(List.of(npcDino));
            Battle battle = new Battle(p1, p2, new MCTSAgent(5, new Random(0)));

            battle.executeRound(wait);

            List<String> log = battle.getAiLog();
            assertFalse(log.isEmpty());
            assertTrue(log.get(0).contains("visits"));
        } finally {
            restoreUseLLMAgent(original);
        }
    }

    @Test
    public void testAgentCanSwitch() throws Exception {
        String original = setUseLLMAgent(false);
        try {
            Move wait = new Move("Wait", 0, 0, List.of());
            Move strike = new Move("Strike", 10, 0, List.of());
            Dinosaur active = new Dinosaur("Active", 10, 5,
                    "assets/animals/allosaurus.png", 1, 1,
                    List.of(), null);
            Dinosaur bench = new Dinosaur("Bench", 10, 5,
                    "assets/animals/allosaurus.png", 1, 1,
                    List.of(strike), null);
            Dinosaur foe = new Dinosaur("Foe", 10, 5,
                    "assets/animals/allosaurus.png", 1, 1,
                    List.of(wait), null);
            Player self = new Player(List.of(active, bench));
            Player enemy = new Player(List.of(foe));
            MCTSAgent agent = new MCTSAgent(20, new Random(0));

            Move chosen = agent.chooseMove(self, enemy, List.of());
            assertNull(chosen);
            assertEquals("Bench", self.getQueuedSwitch().getName());
        } finally {
            restoreUseLLMAgent(original);
        }
    }
}
