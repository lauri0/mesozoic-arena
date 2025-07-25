import com.mesozoic.arena.util.EffectLoader;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class EffectLoaderTest {
    @Test
    public void testLoadDescriptions() {
        Map<String, String> effects = EffectLoader.loadDescriptions();
        assertTrue(effects.containsKey("tailwind"));
        assertTrue(effects.containsKey("rocks"));
    }
}
