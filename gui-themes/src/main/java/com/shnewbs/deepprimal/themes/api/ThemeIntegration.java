package com.shnewbs.deepprimal.themes.api;

import java.util.Objects;
import java.util.Optional;

/** Client-side bridge. Register from a companion's client setup; values are read on the game thread. */
public final class ThemeIntegration {
    public interface Provider {
        Optional<SurvivalState> survival();
        default Optional<Action> hearth() { return Optional.empty(); }
        default Optional<Action> journal() { return Optional.empty(); }
    }
    public record SurvivalState(float thirst, int level, float levelProgress) {
        public SurvivalState {
            fraction(thirst); fraction(levelProgress);
            if (level < 0) throw new IllegalArgumentException("Negative level");
        }
        private static void fraction(float v) {
            if (!Float.isFinite(v) || v < 0 || v > 1) throw new IllegalArgumentException("Expected fraction in [0,1]");
        }
    }
    public record Action(String label, boolean enabled, Runnable invoke) {
        public Action { Objects.requireNonNull(label); Objects.requireNonNull(invoke); }
    }
    private static final Provider EMPTY = Optional::empty;
    private static volatile Provider provider = EMPTY;
    public static Provider provider() { return provider; }
    public static void register(Provider value) { provider = Objects.requireNonNull(value); }
    public static void unregister(Provider value) { if (provider == value) provider = EMPTY; }
    private ThemeIntegration() {}
}
