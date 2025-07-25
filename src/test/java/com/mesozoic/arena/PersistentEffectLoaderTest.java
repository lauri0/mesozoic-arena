package com.mesozoic.arena;

import com.mesozoic.arena.util.PersistentEffectLoader;
import com.mesozoic.arena.model.PersistentEffectDefinition;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class PersistentEffectLoaderTest {
    @Test
    public void testLoadDefinitions() {
        Map<String, PersistentEffectDefinition> defs =
                PersistentEffectLoader.loadDefinitions();
        assertTrue(defs.containsKey("Tailwind"));
        assertEquals(5, defs.get("Tailwind").getDuration());
        assertTrue(defs.containsKey("Rocks"));
    }
}
