import com.mesozoic.arena.model.Dinosaur;
import com.mesozoic.arena.model.Move;
import com.mesozoic.arena.model.Ability;
import com.mesozoic.arena.model.Player;
import com.mesozoic.arena.engine.Battle;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class AbilityEffectsTest {

    @Test
    public void testSpikyBodyReflectsDamage() {
        Move strike = new Move("Strike", 5, 0, 0, List.of());
        Move waitMove = new Move("Wait", 0, 0, 0, List.of());

        Dinosaur attacker = new Dinosaur(
                "Attacker", 100, 50, "assets/animals/allosaurus.png", 100, 1,
                List.of(strike), null);
        Dinosaur spiky = new Dinosaur(
                "Spiky", 100, 50, "assets/animals/allosaurus.png", 100, 10,
                List.of(waitMove), new Ability("Spiky Body", ""));

        Player p1 = new Player(List.of(attacker));
        Player p2 = new Player(List.of(spiky));
        Battle battle = new Battle(p1, p2);

        battle.executeRound(strike, waitMove);

        assertEquals(90, attacker.getHealth());
        assertEquals(95, spiky.getHealth());
    }

    @Test
    public void testArmoredReducesDamageByTen() {
        Move strike = new Move("Strike", 15, 0, 0, List.of());
        Move waitMove = new Move("Wait", 0, 0, 0, List.of());

        Dinosaur attacker = new Dinosaur(
                "Attacker", 100, 50, "assets/animals/allosaurus.png", 100, 1,
                List.of(strike), null);
        Dinosaur armored = new Dinosaur(
                "Armored", 100, 50, "assets/animals/allosaurus.png", 100, 10,
                List.of(waitMove), new Ability("Armored", ""));

        Player p1 = new Player(List.of(attacker));
        Player p2 = new Player(List.of(armored));
        Battle battle = new Battle(p1, p2);

        battle.executeRound(strike, waitMove);

        assertEquals(100, attacker.getHealth());
        assertEquals(95, armored.getHealth());
    }
}

