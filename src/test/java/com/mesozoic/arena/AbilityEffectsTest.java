package com.mesozoic.arena;

import com.mesozoic.arena.model.Dinosaur;
import com.mesozoic.arena.model.Move;
import com.mesozoic.arena.model.Ability;
import com.mesozoic.arena.model.Player;
import com.mesozoic.arena.engine.Battle;
import com.mesozoic.arena.engine.AbilityEffects;
import com.mesozoic.arena.model.MoveType;
import com.mesozoic.arena.model.DinoType;

import java.util.Random;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class AbilityEffectsTest {

    @Test
    public void testSpikyBodyReflectsDamage() {
        Move strike = new Move("Strike", 5, 0, List.of());
        Move waitMove = new Move("Wait", 0, 0, List.of());

        Dinosaur attacker = new Dinosaur(
                "Attacker", 100, 50, "assets/animals/allosaurus.png", 1,
                1, List.of(strike), null);
        Dinosaur spiky = new Dinosaur(
                "Spiky", 100, 50, "assets/animals/allosaurus.png", 10,
                10, List.of(waitMove), new Ability("Spiky Body", ""));

        Player p1 = new Player(List.of(attacker));
        Player p2 = new Player(List.of(spiky));
        Battle battle = new Battle(p1, p2);

        battle.executeRound(strike, waitMove);

        assertEquals(90, attacker.getHealth());
        assertEquals(96, spiky.getHealth());
    }

    @Test
    public void testArmoredReducesDamageByTen() {
        Move strike = new Move("Strike", 15, 0, List.of());
        Move waitMove = new Move("Wait", 0, 0, List.of());

        Dinosaur attacker = new Dinosaur(
                "Attacker", 100, 50, "assets/animals/allosaurus.png", 1,
                1, List.of(strike), null);
        Dinosaur armored = new Dinosaur(
                "Armored", 100, 50, "assets/animals/allosaurus.png", 10,
                10, List.of(waitMove), new Ability("Armored", ""));

        Player p1 = new Player(List.of(attacker));
        Player p2 = new Player(List.of(armored));
        Battle battle = new Battle(p1, p2);

        battle.executeRound(strike, waitMove);

        assertEquals(100, attacker.getHealth());
        assertEquals(99, armored.getHealth());
    }

    @Test
    public void testSupporterAddsPriority() {
        Move cheer = new Move("Cheer", 0, 0, List.of());
        Dinosaur helper = new Dinosaur(
                "Helper", 100, 50, "assets/animals/allosaurus.png", 10,
                10, List.of(cheer), new Ability("Supporter", ""));

        int modified = AbilityEffects.modifyPriority(helper, cheer);
        assertEquals(1, modified);

        Move strike = new Move("Strike", 5, 0, List.of());
        modified = AbilityEffects.modifyPriority(helper, strike);
        assertEquals(0, modified);
    }

    @Test
    public void testBerserkRaisesAttackOnKnockout() {
        Move strike = new Move("Strike", 20, 0, List.of());
        Move waitMove = new Move("Wait", 0, 0, List.of());

        Dinosaur berserker = new Dinosaur(
                "Berserker", 100, 50, "assets/animals/allosaurus.png", 10,
                10, List.of(strike), new Ability("Berserk", ""));
        Dinosaur target = new Dinosaur(
                "Target", 20, 50, "assets/animals/allosaurus.png", 10,
                10, List.of(waitMove), null);

        Player p1 = new Player(List.of(berserker));
        Player p2 = new Player(List.of(target));
        Battle battle = new Battle(p1, p2);

        battle.executeRound(strike, waitMove);

        assertEquals(1, berserker.getHeadAttackStage());
    }

    @Test
    public void testToughReducesDamageAtFullHealth() {
        Move strike = new Move("Strike", 30, 0, List.of());
        Move waitMove = new Move("Wait", 0, 0, List.of());

        Dinosaur attacker = new Dinosaur(
                "Attacker", 100, 50, "assets/animals/allosaurus.png", 1,
                1, List.of(strike), null);
        Dinosaur tough = new Dinosaur(
                "Tough", 100, 50, "assets/animals/allosaurus.png", 10,
                10, List.of(waitMove), new Ability("Tough", ""));

        Player p1 = new Player(List.of(attacker));
        Player p2 = new Player(List.of(tough));
        Battle battle = new Battle(p1, p2);

        battle.executeRound(strike, waitMove);

        assertEquals(100, attacker.getHealth());
        assertEquals(92, tough.getHealth());
    }

    @Test
    public void testToughOnlyAtFullHealth() {
        Move strike = new Move("Strike", 30, 0, List.of());
        Move waitMove = new Move("Wait", 0, 0, List.of());

        Dinosaur attacker = new Dinosaur(
                "Attacker", 100, 50, "assets/animals/allosaurus.png", 1,
                1, List.of(strike), null);
        Dinosaur tough = new Dinosaur(
                "Tough", 100, 50, "assets/animals/allosaurus.png", 10,
                10, List.of(waitMove), new Ability("Tough", ""));

        Player p1 = new Player(List.of(attacker));
        Player p2 = new Player(List.of(tough));
        Battle battle = new Battle(p1, p2);

        tough.adjustHealth(-10);
        battle.executeRound(strike, waitMove);

        assertEquals(100, attacker.getHealth());
        assertEquals(67, tough.getHealth());
    }

    @Test
    public void testIntimidateTriggersOnAutoSwitch() {
        Move strike = new Move("Strike", 20, 0, List.of());
        Move waitMove = new Move("Wait", 0, 0, List.of());

        Dinosaur fodder = new Dinosaur(
                "Fodder", 10, 5, "assets/animals/allosaurus.png", 1,
                1, List.of(waitMove), null);
        Dinosaur intimidator = new Dinosaur(
                "Intimidator", 100, 50, "assets/animals/allosaurus.png", 10,
                10, List.of(waitMove), new Ability("Intimidate", ""));
        Player p1 = new Player(List.of(fodder, intimidator));

        Dinosaur attacker = new Dinosaur(
                "Attacker", 100, 50, "assets/animals/allosaurus.png", 10,
                10, List.of(strike), null);
        Player p2 = new Player(List.of(attacker));
        Battle battle = new Battle(p1, p2);

        battle.executeRound(waitMove, strike);

        assertEquals(intimidator, p1.getActiveDinosaur());
        assertEquals(-1, attacker.getHeadAttackStage());
    }

    @Test
    public void testPreciseBoostsAccuracy() {
        Move lowAcc = new Move("LowAcc", 10, 0, "", MoveType.HEAD,
                DinoType.BITER, List.of(), 0.8);
        Move waitMove = new Move("Wait", 0, 0, List.of());

        Dinosaur sharpshooter = new Dinosaur("Sharp", 100, 50,
                "assets/animals/allosaurus.png", 1, 1, List.of(lowAcc),
                new Ability("Precise", ""));
        Dinosaur target = new Dinosaur("Target", 100, 50,
                "assets/animals/allosaurus.png", 1, 1, List.of(waitMove), null);

        Player p1 = new Player(List.of(sharpshooter));
        Player p2 = new Player(List.of(target));
        Battle battle = new Battle(p1, p2);

        Random rng = new Random() {
            @Override
            public double nextDouble() {
                return 0.9; // would miss without ability
            }
        };

        battle.executeRound(lowAcc, waitMove, rng);

        assertEquals(92, target.getHealth());
    }

    @Test
    public void testRegeneratorHealsOnMoveUse() {
        Move waitMove = new Move("Wait", 0, 0, List.of());
        Dinosaur healer = new Dinosaur("Healer", 100, 50,
                "assets/animals/allosaurus.png", 1, 1, List.of(waitMove),
                new Ability("Regenerator", ""));
        Dinosaur target = new Dinosaur("Target", 100, 50,
                "assets/animals/allosaurus.png", 1, 1, List.of(waitMove), null);

        Player p1 = new Player(List.of(healer));
        Player p2 = new Player(List.of(target));
        Battle battle = new Battle(p1, p2);

        healer.adjustHealth(-20);
        battle.executeRound(waitMove, waitMove, new Random(0));

        assertEquals(90, healer.getHealth());
    }

    @Test
    public void testResilientSurvivesFatalHit() {
        Move strike = new Move("Strike", 50, 0, List.of());
        Move waitMove = new Move("Wait", 0, 0, List.of());

        Dinosaur attacker = new Dinosaur(
                "Attacker", 100, 50, "assets/animals/allosaurus.png", 1,
                1, List.of(strike), null);
        Dinosaur sturdy = new Dinosaur(
                "Sturdy", 30, 50, "assets/animals/allosaurus.png", 10,
                10, List.of(waitMove), new Ability("Resilient", ""));

        Player p1 = new Player(List.of(attacker));
        Player p2 = new Player(List.of(sturdy));
        Battle battle = new Battle(p1, p2);

        battle.executeRound(strike, waitMove);

        assertEquals(1, sturdy.getHealth());
    }

    @Test
    public void testScavengerHealsOnKnockOut() {
        Move strike = new Move("Strike", 30, 0, List.of());
        Move waitMove = new Move("Wait", 0, 0, List.of());

        Dinosaur scavenger = new Dinosaur(
                "Scav", 100, 50, "assets/animals/allosaurus.png", 1,
                1, List.of(strike), new Ability("Scavenge", ""));
        Dinosaur target = new Dinosaur(
                "Target", 20, 50, "assets/animals/allosaurus.png", 10,
                10, List.of(waitMove), null);

        Player p1 = new Player(List.of(scavenger));
        Player p2 = new Player(List.of(target));
        Battle battle = new Battle(p1, p2);

        scavenger.adjustHealth(-40);
        battle.executeRound(strike, waitMove);

        assertEquals(80, scavenger.getHealth());
    }

    @Test
    public void testCamouflageBlocksFirstHit() {
        Move strike = new Move("Strike", 20, 0, List.of());
        Move waitMove = new Move("Wait", 0, 0, List.of());

        Dinosaur attacker = new Dinosaur(
                "Attacker", 100, 50, "assets/animals/allosaurus.png", 1,
                1, List.of(strike), null);
        Dinosaur stealthy = new Dinosaur(
                "Stealthy", 100, 50, "assets/animals/allosaurus.png", 10,
                10, List.of(waitMove), new Ability("Camouflage", ""));

        Player p1 = new Player(List.of(attacker));
        Player p2 = new Player(List.of(stealthy));
        Battle battle = new Battle(p1, p2);

        Random rng = new Random() {
            @Override
            public double nextDouble() {
                return 0.4;
            }
        };

        battle.executeRound(strike, waitMove, rng);
        assertEquals(100, stealthy.getHealth());

        battle.executeRound(strike, waitMove, rng);
        assertEquals(85, stealthy.getHealth());
    }

    @Test
    public void testTiringLowersOpponentSpeed() {
        Move strike = new Move("Strike", 5, 0, List.of());
        Move waitMove = new Move("Wait", 0, 0, List.of());

        Dinosaur attacker = new Dinosaur(
                "Attacker", 100, 50, "assets/animals/allosaurus.png", 1,
                1, List.of(strike), null);
        Dinosaur tiring = new Dinosaur(
                "Tiring", 100, 50, "assets/animals/allosaurus.png", 10,
                10, List.of(waitMove), new Ability("Tiring", ""));

        Player p1 = new Player(List.of(attacker));
        Player p2 = new Player(List.of(tiring));
        Battle battle = new Battle(p1, p2);

        battle.executeRound(strike, waitMove);

        assertEquals(-1, attacker.getSpeedStage());
    }
}

