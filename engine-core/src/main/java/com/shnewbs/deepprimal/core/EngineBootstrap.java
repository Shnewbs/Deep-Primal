package com.shnewbs.deepprimal.core;

import com.shnewbs.deepprimal.diagnostics.LocalDashboard;
import java.util.logging.Logger;

/**
 * Engine bootstrap for production initialization.
 * Ensures all core systems are properly initialized and validated.
 */
public final class EngineBootstrap {
    private static final Logger LOGGER = Logger.getLogger(EngineBootstrap.class.getName());
    private static volatile boolean initialized = false;

    /**
     * Initialize the engine core for production use.
     * This should be called early during mod loading.
     */
    public static void initialize() {
        if (initialized) {
            LOGGER.warning("EngineBootstrap.initialize() called multiple times");
            return;
        }

        LOGGER.info("Initializing Deep Primal engine core...");

        try {
            // Initialize validation
            CoreValidation.startPeriodicValidation(300); // Every 5 minutes

            // Register engine metadata with dashboard
            LocalDashboard.metadata("engine.core", java.util.Map.of(
                "version", "1.0.0",
                "build", "production"
            ));

            // Run initial validation
            EngineHealthCheck.initDefaultChecks();
            EngineHealthCheck.runOrThrow();

            LOGGER.info("Engine core initialization complete");
            initialized = true;
        } catch (Exception e) {
            LOGGER.severe("Engine core initialization failed: " + e.getMessage());
            throw new IllegalStateException("Engine core initialization failed", e);
        }
    }

    /**
     * Shutdown the engine core gracefully.
     */
    public static void shutdown() {
        if (!initialized) return;

        LOGGER.info("Shutting down Deep Primal engine core...");
        CoreValidation.stop();
        initialized = false;
        LOGGER.info("Engine core shutdown complete");
    }

    public static boolean isInitialized() {
        return initialized;
    }

    private EngineBootstrap() {}
}