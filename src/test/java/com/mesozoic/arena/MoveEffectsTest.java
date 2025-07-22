package com.mesozoic.arena;

import com.mesozoic.arena.model.Dinosaur;
import com.mesozoic.arena.model.Move;
import com.mesozoic.arena.model.Effect;
import com.mesozoic.arena.model.Player;
import com.mesozoic.arena.engine.Battle;
import com.mesozoic.arena.model.MoveType;
import com.mesozoic.arena.model.DinoType;
import com.mesozoic.arena.model.Ailment;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class MoveEffectsTest {
    @Test
    public void testAreaHealHealsWholeTeam() {
        Move areaHeal = new Move("Rally", 0, 0, List.of(new Effect("area heal")));
        Move noop = new Move("Wait", 0, 0, List.of());
        Dinosaur active = new Dinosaur("Active", 100, 50, "assets/animals/allosaurus.png", 10, 10, List.of(areaHeal), null);
        Dinosaur bench = new Dinosaur("Bench", 100, 50, "assets/animals/allosaurus.png", 10, 10, List.of(noop), null);
        Player p1 = new Player(List.of(active, bench));
        Player p2 = new Player(List.of(new Dinosaur("Opponent", 100, 50, "assets/animals/allosaurus.png", 10, 10, List.of(noop), null)));
        Battle battle = new Battle(p1, p2);

        active.adjustHealth(-20);
        bench.adjustHealth(-40);
        battle.executeRound(areaHeal, noop);

        assertEquals(90, active.getHealth());
        assertEquals(70, bench.getHealth());
    }

    @Test
    public void testHealCapsAtMax() {
        Move heal = new Move("Recover", 0, 0, List.of(new Effect("heal")));
        Move noop = new Move("Wait", 0, 0, List.of());
        Dinosaur active = new Dinosaur("Active", 100, 50, "assets/animals/allosaurus.png", 10, 10, List.of(heal), null);
        Dinosaur bench = new Dinosaur("Bench", 100, 50, "assets/animals/allosaurus.png", 10, 10, List.of(noop), null);
        Player p1 = new Player(List.of(active, bench));
        Player p2 = new Player(List.of(new Dinosaur("Opponent", 100, 50, "assets/animals/allosaurus.png", 10, 10, List.of(noop), null)));
        Battle battle = new Battle(p1, p2);

        active.adjustHealth(-80); // 20 health left
        bench.adjustHealth(-10);  // should remain unchanged by heal
        battle.executeRound(heal, noop);

        assertEquals(50, active.getHealth());
        assertEquals(90, bench.getHealth());

        // heal again to test cap at max health
        battle.executeRound(heal, noop);
        assertEquals(80, active.getHealth());
    }

    @Test
    public void testDoubleAttackHitsTwice() {
        Move doubleHit = new Move("Double", 1, 0, List.of(new Effect("double attack")));
        Move noop = new Move("Wait", 0, 0, List.of());
        Dinosaur attacker = new Dinosaur("Attacker", 100, 50, "assets/animals/allosaurus.png", 10, 10, List.of(doubleHit), null);
        Dinosaur defender = new Dinosaur("Defender", 100, 50, "assets/animals/allosaurus.png", 10, 10, List.of(noop), null);
        Player p1 = new Player(List.of(attacker));
        Player p2 = new Player(List.of(defender));
        Battle battle = new Battle(p1, p2);

        battle.executeRound(doubleHit, noop);
        assertEquals(84, defender.getHealth());
    }

    @Test
    public void testTripleAttackHitsThrice() {
        Move tripleHit = new Move("Triple", 1, 0, List.of(new Effect("triple attack")));
        Move noop = new Move("Wait", 0, 0, List.of());
        Dinosaur attacker = new Dinosaur("Attacker", 100, 50, "assets/animals/allosaurus.png", 10, 10, List.of(tripleHit), null);
        Dinosaur defender = new Dinosaur("Defender", 100, 50, "assets/animals/allosaurus.png", 10, 10, List.of(noop), null);
        Player p1 = new Player(List.of(attacker));
        Player p2 = new Player(List.of(defender));
        Battle battle = new Battle(p1, p2);

        battle.executeRound(tripleHit, noop);
        assertEquals(76, defender.getHealth());
    }

    @Test
    public void testFrenzyRaisesAttack() {
        Move frenzy = new Move("Rage", 0, 0, List.of(new Effect("frenzy")));
        Move noop = new Move("Wait", 0, 0, List.of());
        Dinosaur attacker = new Dinosaur("Attacker", 100, 50, "assets/animals/allosaurus.png", 10, 10, List.of(frenzy), null);
        Dinosaur defender = new Dinosaur("Defender", 100, 50, "assets/animals/allosaurus.png", 10, 10, List.of(noop), null);
        Player p1 = new Player(List.of(attacker));
        Player p2 = new Player(List.of(defender));
        Battle battle = new Battle(p1, p2);

        battle.executeRound(frenzy, noop);
        assertEquals(2, attacker.getHeadAttackStage());
    }

    @Test
    public void testAdrenalineRaisesAttackAndSpeed() {
        Move adrenaline = new Move("Boost", 0, 0, List.of(new Effect("adrenaline")));
        Move noop = new Move("Wait", 0, 0, List.of());
        Dinosaur attacker = new Dinosaur("Attacker", 100, 50, "assets/animals/allosaurus.png", 10, 10, List.of(adrenaline), null);
        Dinosaur defender = new Dinosaur("Defender", 100, 50, "assets/animals/allosaurus.png", 10, 10, List.of(noop), null);
        Player p1 = new Player(List.of(attacker));
        Player p2 = new Player(List.of(defender));
        Battle battle = new Battle(p1, p2);

        battle.executeRound(adrenaline, noop);
        assertEquals(1, attacker.getHeadAttackStage());
        assertEquals(1, attacker.getSpeedStage());
    }

    @Test
    public void testSlowLowersOpponentSpeed() {
        Move slow = new Move("Mud", 0, 0, List.of(new Effect("slow")));
        Move noop = new Move("Wait", 0, 0, List.of());
        Dinosaur attacker = new Dinosaur("Attacker", 100, 50, "assets/animals/allosaurus.png", 10, 10, List.of(slow), null);
        Dinosaur defender = new Dinosaur("Defender", 100, 50, "assets/animals/allosaurus.png", 10, 10, List.of(noop), null);
        Player p1 = new Player(List.of(attacker));
        Player p2 = new Player(List.of(defender));
        Battle battle = new Battle(p1, p2);

        battle.executeRound(slow, noop);
        assertEquals(-1, defender.getSpeedStage());
    }

    @Test
    public void testBleedDealsEndTurnDamage() {
        Move bleed = new Move("Cut", 0, 0, List.of(new Effect("bleed")));
        Move noop = new Move("Wait", 0, 0, List.of());
        Dinosaur attacker = new Dinosaur("Attacker", 100, 50, "assets/animals/allosaurus.png", 10, 10, List.of(bleed), null);
        Dinosaur defender = new Dinosaur("Defender", 100, 50, "assets/animals/allosaurus.png", 10, 10, List.of(noop), null);
        Player p1 = new Player(List.of(attacker));
        Player p2 = new Player(List.of(defender));
        Battle battle = new Battle(p1, p2);

        battle.executeRound(bleed, noop);
        assertEquals(90, defender.getHealth());
    }

    @Test
    public void testBleedingHalvesHealing() {
        Move bleed = new Move("Cut", 0, 0, List.of(new Effect("bleed")));
        Move heal = new Move("Recover", 0, 0, List.of(new Effect("heal")));
        Move noop = new Move("Wait", 0, 0, List.of());
        Dinosaur attacker = new Dinosaur("Attacker", 100, 50, "assets/animals/allosaurus.png", 10, 10, List.of(bleed), null);
        Dinosaur defender = new Dinosaur("Defender", 100, 50, "assets/animals/allosaurus.png", 10, 10, List.of(heal), null);
        defender.adjustHealth(-40);
        Player p1 = new Player(List.of(attacker));
        Player p2 = new Player(List.of(defender));
        Battle battle = new Battle(p1, p2);

        battle.executeRound(bleed, noop);
        battle.executeRound(noop, heal);
        assertEquals(55, defender.getHealth());
    }

    @Test
    public void testSwitchOutAfterAttack() {
        Move switchMove = new Move("Retreat", 5, 0, List.of(new Effect("switch out")));
        Move noop = new Move("Wait", 0, 0, List.of());
        Dinosaur first = new Dinosaur("First", 100, 50, "assets/animals/allosaurus.png", 10, 10, List.of(switchMove), null);
        Dinosaur second = new Dinosaur("Second", 100, 50, "assets/animals/allosaurus.png", 10, 10, List.of(noop), null);
        Player p1 = new Player(List.of(first, second));
        Player p2 = new Player(List.of(new Dinosaur("Opponent", 100, 50, "assets/animals/allosaurus.png", 10, 10, List.of(noop), null)));
        Battle battle = new Battle(p1, p2);

        battle.executeRound(switchMove, noop);
        assertEquals(second, p1.getActiveDinosaur());
    }

    @Test
    public void testFatigueLowersAttack() {
        Move fatigue = new Move("Heavy", 0, 0, List.of(new Effect("fatigue")));
        Move noop = new Move("Wait", 0, 0, List.of());
        Dinosaur attacker = new Dinosaur("Attacker", 100, 50, "assets/animals/allosaurus.png", 10, 10, List.of(fatigue), null);
        Dinosaur defender = new Dinosaur("Defender", 100, 50, "assets/animals/allosaurus.png", 10, 10, List.of(noop), null);
        Player p1 = new Player(List.of(attacker));
        Player p2 = new Player(List.of(defender));
        Battle battle = new Battle(p1, p2);

        battle.executeRound(fatigue, noop);
        assertEquals(-1, attacker.getHeadAttackStage());
    }

    @Test
    public void testRecoilDealsQuarterDamage() {
        Move recoil = new Move("Crash", 20, 0, List.of(new Effect("recoil")));
        Move noop = new Move("Wait", 0, 0, List.of());
        Dinosaur attacker = new Dinosaur("Attacker", 100, 50, "assets/animals/allosaurus.png", 1, 1, List.of(recoil), null);
        Dinosaur defender = new Dinosaur("Defender", 100, 50, "assets/animals/allosaurus.png", 1, 1, List.of(noop), null);
        Player p1 = new Player(List.of(attacker));
        Player p2 = new Player(List.of(defender));
        Battle battle = new Battle(p1, p2);

        battle.executeRound(recoil, noop);
        assertEquals(97, attacker.getHealth());
    }

    @Test
    public void testBigDrainHealsHalfDamage() {
        Move drain = new Move("Drain", 20, 0, "", MoveType.HEAD, DinoType.RUNNER,
                List.of(new Effect("big drain")), 1.0);
        Move noop = new Move("Wait", 0, 0, List.of());
        Dinosaur attacker = new Dinosaur("Attacker", 100, 50, "assets/animals/allosaurus.png", 1, 1,
                List.of(drain), null, List.of(DinoType.CHARGER));
        Dinosaur defender = new Dinosaur("Defender", 100, 50, "assets/animals/allosaurus.png", 1, 1,
                List.of(noop), null);
        Player p1 = new Player(List.of(attacker));
        Player p2 = new Player(List.of(defender));
        Battle battle = new Battle(p1, p2);

        attacker.adjustHealth(-40);
        battle.executeRound(drain, noop);

        assertEquals(70, attacker.getHealth());
        assertEquals(80, defender.getHealth());
    }

    @Test
    public void testSmallDrainHealsQuarterDamage() {
        Move drain = new Move("Nibble", 20, 0, "", MoveType.HEAD, DinoType.RUNNER,
                List.of(new Effect("small drain")), 1.0);
        Move noop = new Move("Wait", 0, 0, List.of());
        Dinosaur attacker = new Dinosaur("Attacker", 100, 50, "assets/animals/allosaurus.png", 1, 1,
                List.of(drain), null, List.of(DinoType.CHARGER));
        Dinosaur defender = new Dinosaur("Defender", 100, 50, "assets/animals/allosaurus.png", 1, 1,
                List.of(noop), null);
        Player p1 = new Player(List.of(attacker));
        Player p2 = new Player(List.of(defender));
        Battle battle = new Battle(p1, p2);

        attacker.adjustHealth(-40);
        battle.executeRound(drain, noop);

        assertEquals(65, attacker.getHealth());
        assertEquals(80, defender.getHealth());
    }

    @Test
    public void testTrampleDamagesBench() {
        Move trample = new Move("Stomp", 10, 0, List.of(new Effect("trample")));
        Move noop = new Move("Wait", 0, 0, List.of());
        Dinosaur attacker = new Dinosaur("Attacker", 100, 50, "assets/animals/allosaurus.png", 10, 10, List.of(trample), null);
        Dinosaur defender = new Dinosaur("Defender", 100, 50, "assets/animals/allosaurus.png", 10, 10, List.of(noop), null);
        Dinosaur bench = new Dinosaur("Bench", 100, 50, "assets/animals/allosaurus.png", 10, 10, List.of(noop), null);
        Player p1 = new Player(List.of(attacker));
        Player p2 = new Player(List.of(defender, bench));
        Battle battle = new Battle(p1, p2);

        battle.executeRound(trample, noop);

        assertEquals(90, bench.getHealth());
    }

    @Test
    public void testRetaliateBoostsDamage() {
        Move strike = new Move("Strike", 1, 0, List.of());
        Move retaliate = new Move("Counter", 1, 0, List.of(new Effect("retaliate")));
        Dinosaur dino1 = new Dinosaur("A", 100, 50, "assets/animals/allosaurus.png", 10, 10, List.of(strike), null);
        Dinosaur dino2 = new Dinosaur("B", 100, 50, "assets/animals/allosaurus.png", 10, 10, List.of(retaliate), null);
        Player p1 = new Player(List.of(dino1));
        Player p2 = new Player(List.of(dino2));
        Battle battle = new Battle(p1, p2);

        battle.executeRound(strike, retaliate);

        assertEquals(88, dino1.getHealth());
    }

    @Test
    public void testHealingMistCuresAilments() {
        Move healing = new Move("Mist", 0, 0, List.of(new Effect("healing mist")));
        Move noop = new Move("Wait", 0, 0, List.of());
        Dinosaur user = new Dinosaur("Healer", 100, 50, "assets/animals/allosaurus.png", 10, 10, List.of(healing), null);
        user.addAilment(new Ailment("Bleeding"));
        user.adjustHealth(-30);
        Player p1 = new Player(List.of(user));
        Player p2 = new Player(List.of(new Dinosaur("Target", 100, 50, "assets/animals/allosaurus.png", 10, 10, List.of(noop), null)));
        Battle battle = new Battle(p1, p2);

        battle.executeRound(healing, noop);

        assertTrue(user.getAilments().isEmpty());
        assertEquals(95, user.getHealth());
    }
}
