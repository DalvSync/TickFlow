package org.dalvsync.tickflow.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.dalvsync.tickflow.TickFlow;

public class Config {

    public static boolean adaptiveSpawnEnabled;
    public static boolean adaptiveGrowthEnabled;
    public static boolean projectileCleanupEnabled;
    public static boolean villagerLobotomyEnabled;

    public static boolean fastTrashDespawnEnabled;
    public static int trashDespawnTicks;

    public static boolean chunkLimiterEnabled;
    public static int maxEntitiesPerChunk;

    public static boolean itemMergeEnabled;
    public static double itemMergeRadius;
    public static boolean xpMergeEnabled;
    public static double xpMergeRadius;

    public static boolean armorStandOptimization;

    public static void load(TickFlow plugin) {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration cfg = plugin.getConfig();

        adaptiveSpawnEnabled = cfg.getBoolean("features.adaptive-spawn-throttling.enabled", true);
        adaptiveGrowthEnabled = cfg.getBoolean("features.adaptive-growth-throttling.enabled", true);
        projectileCleanupEnabled = cfg.getBoolean("features.projectile-cleanup.enabled", true);
        villagerLobotomyEnabled = cfg.getBoolean("features.villager-lobotomy.enabled", true);

        fastTrashDespawnEnabled = cfg.getBoolean("features.fast-trash-despawn.enabled", true);
        trashDespawnTicks = cfg.getInt("features.fast-trash-despawn.ticks-lived-to-set", 5100);

        chunkLimiterEnabled = cfg.getBoolean("features.chunk-entity-limiter.enabled", true);
        maxEntitiesPerChunk = cfg.getInt("features.chunk-entity-limiter.max-per-type", 25);

        itemMergeEnabled = cfg.getBoolean("features.item-merging.enabled", false);
        itemMergeRadius = cfg.getDouble("features.item-merging.merge-radius", 3.5);
        xpMergeEnabled = cfg.getBoolean("features.xp-merging.enabled", false);
        xpMergeRadius = cfg.getDouble("features.xp-merging.merge-radius", 4.0);

        armorStandOptimization = cfg.getBoolean("features.armor-stand-optimization.enabled", true);
    }
}