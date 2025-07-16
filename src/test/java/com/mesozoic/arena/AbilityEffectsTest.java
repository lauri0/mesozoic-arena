import com.mesozoic.arena.model.Dinosaur;
import com.mesozoic.arena.model.Move;
import com.mesozoic.arena.model.Ability;
import com.mesozoic.arena.model.Player;
import com.mesozoic.arena.engine.Battle;
import com.mesozoic.arena.engine.AbilityEffects;

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
        assertEquals(95, spiky.getHealth());
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
        assertEquals(95, armored.getHealth());
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

        assertEquals(1, berserker.getAttackStage());
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
        assertEquals(90, tough.getHealth());
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
        assertEquals(60, tough.getHealth());
    }
}

