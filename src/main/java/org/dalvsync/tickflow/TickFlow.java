package org.dalvsync.tickflow;

import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.entity.Trident;
import org.bukkit.entity.Player;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class TickFlow extends JavaPlugin implements Listener {

    private final int MAX_MONSTERS_PER_CHUNK = 15;
    private final int MAX_ITEMS_PER_CHUNK = 30;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("TickFlow is live! Limits, arrows, and smart villagers are activated.");
    }

    @Override
    public void onDisable() {
        getLogger().info("TickFlow is disabled.");
    }

    @EventHandler
    public void onMonsterSpawn(CreatureSpawnEvent event) {
        if (!(event.getEntity() instanceof Monster)) return;
        Chunk chunk = event.getEntity().getChunk();
        int monsterCount = 0;
        for (Entity entity : chunk.getEntities()) {
            if (entity instanceof Monster) monsterCount++;
        }
        if (monsterCount >= MAX_MONSTERS_PER_CHUNK) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) {
        Chunk chunk = event.getEntity().getChunk();
        int itemCount = 0;
        for (Entity entity : chunk.getEntities()) {
            if (entity instanceof Item) itemCount++;
        }
        if (itemCount >= MAX_ITEMS_PER_CHUNK) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntity() instanceof AbstractArrow arrow) {

            if (event.getHitBlock() != null) {

                if (arrow instanceof Trident) {
                    return;
                }

                if (arrow.getShooter() instanceof Player) {
                    return;
                }

                arrow.remove();
            }
        }
    }

    @EventHandler
    public void onEntitiesLoad(EntitiesLoadEvent event) {
        for (Entity entity : event.getEntities()) {
            if (entity instanceof Villager villager) {
                startVillagerTask(villager);
            }
        }
    }

    @EventHandler
    public void onVillagerSpawn(CreatureSpawnEvent event) {
        if (event.getEntity() instanceof Villager villager) {
            startVillagerTask(villager);
        }
    }

    private void startVillagerTask(Villager villager) {
        villager.getScheduler().runAtFixedRate(this, task -> {

            if (!villager.isValid()) {
                task.cancel();
                return;
            }

            boolean trapped = isTrapped(villager);

            if (villager.isAware() == trapped) {
                villager.setAware(!trapped);
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