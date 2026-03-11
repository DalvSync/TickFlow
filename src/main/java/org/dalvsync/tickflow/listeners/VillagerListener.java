package org.dalvsync.tickflow.listeners;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.inventory.MerchantRecipe;
import org.dalvsync.tickflow.TickFlow;
import org.dalvsync.tickflow.config.Config;

public class VillagerListener implements Listener {

    private final TickFlow plugin;

    public VillagerListener(TickFlow plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntitiesLoad(EntitiesLoadEvent event) {
        if (!Config.villagerLobotomyEnabled || !TickFlow.IS_PAPER) return;

        for (Entity entity : event.getEntities()) {
            if (entity instanceof Villager villager) {
                startVillagerTask(villager);
            }
        }
    }

    @EventHandler
    public void onVillagerSpawn(CreatureSpawnEvent event) {
        if (!Config.villagerLobotomyEnabled || !TickFlow.IS_PAPER) return;

        if (event.getEntity() instanceof Villager villager) {
            startVillagerTask(villager);
        }
    }

    private void startVillagerTask(Villager villager) {
        villager.getScheduler().runAtFixedRate(plugin, task -> {
            if (!villager.isValid()) {
                task.cancel();
                return;
            }

            boolean trapped = isTrapped(villager);
            if (villager.isAware() == trapped) {
                villager.setAware(!trapped);
            }

            if (!villager.isAware()) {
                long time = villager.getWorld().getTime();
                if (time > 2000 && time < 9000) {
                    if (Math.random() < 0.05) {
                        for (MerchantRecipe recipe : villager.getRecipes()) {
                            recipe.setUses(0);
                        }
                    }
                }
            }
        }, null, 100L, 200L);
    }

    private boolean isTrapped(Villager villager) {
        if (villager.isInsideVehicle()) return true;

        Block b = villager.getLocation().getBlock();
        int solidCount = 0;

        if (b.getRelative(1, 0, 0).getType().isSolid()) solidCount++;
        if (b.getRelative(-1, 0, 0).getType().isSolid()) solidCount++;
        if (b.getRelative(0, 0, 1).getType().isSolid()) solidCount++;
        if (b.getRelative(0, 0, -1).getType().isSolid()) solidCount++;

        return solidCount >= 3;
    }
}