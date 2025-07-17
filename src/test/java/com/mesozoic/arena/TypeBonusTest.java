import com.mesozoic.arena.model.Dinosaur;
import com.mesozoic.arena.model.Move;
import com.mesozoic.arena.model.Player;
import com.mesozoic.arena.model.MoveType;
import com.mesozoic.arena.model.DinoType;
import com.mesozoic.arena.engine.Battle;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class TypeBonusTest {
    @Test
    public void testStabIncreasesDamage() {
        Move charge = new Move("Charge", 10, 0, "", MoveType.BODY,
                DinoType.CHARGER, List.of(), 1.0);
        Move waitMove = new Move("Wait", 0, 0, List.of());
        Dinosaur attacker = new Dinosaur("Attacker", 100, 50,
                "assets/animals/allosaurus.png", 1, 1, List.of(charge), null,
                List.of(DinoType.CHARGER));
        Dinosaur defender = new Dinosaur("Defender", 100, 50,
                "assets/animals/allosaurus.png", 1, 1, List.of(waitMove), null,
                List.of(DinoType.BITER));
        Player p1 = new Player(List.of(attacker));
        Player p2 = new Player(List.of(defender));
        Battle battle = new Battle(p1, p2);

        battle.executeRound(charge, waitMove);
        assertEquals(85, defender.getHealth());
    }

    @Test
    public void testNoStabNormalDamage() {
        Move charge = new Move("Charge", 10, 0, "", MoveType.BODY,
                DinoType.CHARGER, List.of(), 1.0);
        Move waitMove = new Move("Wait", 0, 0, List.of());
        Dinosaur attacker = new Dinosaur("Attacker", 100, 50,
                "assets/animals/allosaurus.png", 1, 1, List.of(charge), null,
                List.of(DinoType.BITER));
        Dinosaur defender = new Dinosaur("Defender", 100, 50,
                "assets/animals/allosaurus.png", 1, 1, List.of(waitMove), null,
                List.of(DinoType.BITER));
        Player p1 = new Player(List.of(attacker));
        Player p2 = new Player(List.of(defender));
        Battle battle = new Battle(p1, p2);

        battle.executeRound(charge, waitMove);
        assertEquals(90, defender.getHealth());
    }
}
