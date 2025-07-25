package com.mesozoic.arena.util;

import com.mesozoic.arena.model.PersistentEffect;
import com.mesozoic.arena.model.PersistentEffectDefinition;
import java.util.Map;

/**
 * Provides access to persistent effect definitions.
 */
public final class PersistentEffectRegistry {
    private static final Map<String, PersistentEffectDefinition> DEFINITIONS =
            PersistentEffectLoader.loadDefinitions();

    private PersistentEffectRegistry() {
    }

    public static PersistentEffectDefinition getDefinition(String name) {
        return DEFINITIONS.get(name);
    }

    public static PersistentEffect createEffect(String name) {
        PersistentEffectDefinition def = DEFINITIONS.get(name);
        return def == null ? null : new PersistentEffect(def);
    }
}
