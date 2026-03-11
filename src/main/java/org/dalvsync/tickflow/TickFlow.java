package org.dalvsync.tickflow;

import org.bukkit.plugin.java.JavaPlugin;
import org.dalvsync.tickflow.commands.ReloadCommand;
import org.dalvsync.tickflow.config.Config;
import org.dalvsync.tickflow.listeners.EntityOptimizerListener;
import org.dalvsync.tickflow.listeners.MechanicsListener;
import org.dalvsync.tickflow.listeners.VillagerListener;

public final class TickFlow extends JavaPlugin {

    public static boolean IS_PAPER = false;
    public static boolean IS_FOLIA = false;

    private int reloadCommandsExecuted = 0;

    @Override
    public void onEnable() {
        checkServerSoftware();

        Config.load(this);

        getServer().getPluginManager().registerEvents(new EntityOptimizerListener(this), this);
        getServer().getPluginManager().registerEvents(new MechanicsListener(), this);
        getServer().getPluginManager().registerEvents(new VillagerListener(this), this);

        if (getCommand("tickflow") != null) {
            getCommand("tickflow").setExecutor(new ReloadCommand(this));
        }

        setupMetrics();

        getLogger().info("TickFlow is live! Modular architecture and smart features activated.");
    }

    public void incrementReloadCount() {
        reloadCommandsExecuted++;
    }

    private void setupMetrics() {
        int pluginId = 29952;
        Metrics metrics = new Metrics(this, pluginId);

        metrics.addCustomChart(new Metrics.SimplePie("chunk_limiter_enabled",
                () -> Config.chunkLimiterEnabled ? "On" : "Off"));

        metrics.addCustomChart(new Metrics.SimplePie("armor_stand_optimization",
                () -> Config.armorStandOptimization ? "On" : "Off"));

        metrics.addCustomChart(new Metrics.SimplePie("projectile_cleanup",
                () -> Config.projectileCleanupEnabled ? "On" : "Off"));

        metrics.addCustomChart(new Metrics.SimplePie("villager_lobotomy",
                () -> Config.villagerLobotomyEnabled ? "On" : "Off"));

        metrics.addCustomChart(new Metrics.SimplePie("fast_trash_despawn",
                () -> Config.fastTrashDespawnEnabled ? "On" : "Off"));

        metrics.addCustomChart(new Metrics.SimplePie("item_merging",
                () -> Config.itemMergeEnabled ? "On" : "Off"));

        metrics.addCustomChart(new Metrics.SimplePie("xp_merging",
                () -> Config.xpMergeEnabled ? "On" : "Off"));

        metrics.addCustomChart(new Metrics.SimplePie("adaptive_spawn_throttling",
                () -> Config.adaptiveSpawnEnabled ? "On" : "Off"));

        metrics.addCustomChart(new Metrics.SimplePie("adaptive_growth_throttling",
                () -> Config.adaptiveGrowthEnabled ? "On" : "Off"));

        metrics.addCustomChart(new Metrics.SimplePie("chunk_limiter_max",
                () -> String.valueOf(Config.maxEntitiesPerChunk)));

        metrics.addCustomChart(new Metrics.SimplePie("fast_trash_despawn_ticks",
                () -> String.valueOf(Config.trashDespawnTicks)));

        metrics.addCustomChart(new Metrics.SimplePie("item_merging_radius",
                () -> String.valueOf(Config.itemMergeRadius)));

        metrics.addCustomChart(new Metrics.SimplePie("xp_merging_radius",
                () -> String.valueOf(Config.xpMergeRadius)));

        metrics.addCustomChart(new Metrics.SingleLineChart("reload_commands_usage", () -> {
            int count = reloadCommandsExecuted;
            reloadCommandsExecuted = 0;
            return count;
        }));
    }

    @Override
    public void onDisable() {
        getLogger().info("TickFlow is disabled.");
    }

    private void checkServerSoftware() {
        try {
            Class.forName("com.destroystokyo.paper.PaperConfig");
            IS_PAPER = true;
        } catch (ClassNotFoundException e) {
            IS_PAPER = false;
            getLogger().warning("Paper not found! Some advanced optimization features will be disabled.");
        }

        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            IS_FOLIA = true;
            getLogger().info("Folia detected! Schedulers adapted.");
        } catch (ClassNotFoundException e) {
            IS_FOLIA = false;
        }
    }
}