package org.dalvsync.tickflow.listeners;

import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.dalvsync.tickflow.TickFlow;
import org.dalvsync.tickflow.config.Config;
import org.dalvsync.tickflow.utils.TpsMonitor;

public class EntityOptimizerListener implements Listener {

    private final TickFlow plugin;

    public EntityOptimizerListener(TickFlow plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChunkEntityLimit(CreatureSpawnEvent event) {
        if (!Config.chunkLimiterEnabled) return;

        Entity entity = event.getEntity();
        if (!(entity instanceof Animals || entity instanceof Monster)) return;
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM) return;

        int count = 0;
        for (Entity e : entity.getLocation().getChunk().getEntities()) {
            if (e.getType() == entity.getType()) {
                count++;
            }
        }

        if (count >= Config.maxEntitiesPerChunk) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onMonsterSpawnThrottling(CreatureSpawnEvent event) {
        if (!Config.adaptiveSpawnEnabled) return;

        if (event.getEntity() instanceof Monster && event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL) {
            if (TpsMonitor.isLagging(18.0) && Math.random() < 0.30) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onTrashItemSpawn(ItemSpawnEvent event) {
        if (!Config.fastTrashDespawnEnabled) return;

        Item item = event.getEntity();
        Material type = item.getItemStack().getType();

        switch (type) {
            case COBBLESTONE:
            case DIRT:
            case NETHERRACK:
            case GRAVEL:
            case ANDESITE:
            case DIORITE:
            case GRANITE:
                item.setTicksLived(Config.trashDespawnTicks);
                break;
            default:
                break;
        }
    }

    @EventHandler
    public void onItemSpawnMerge(ItemSpawnEvent event) {
        if (!Config.itemMergeEnabled) return;

        Item newItem = event.getEntity();
        ItemStack newStack = newItem.getItemStack();
        double radius = TpsMonitor.isLagging(18.0) ? 12.0 : Config.itemMergeRadius;

        for (Entity entity : newItem.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof Item nearbyItem && !nearbyItem.isDead()) {
                ItemStack nearbyStack = nearbyItem.getItemStack();

                if (nearbyStack.isSimilar(newStack)) {
                    int totalAmount = nearbyStack.getAmount() + newStack.getAmount();

                    if (totalAmount <= nearbyStack.getMaxStackSize()) {
                        nearbyStack.setAmount(totalAmount);
                        nearbyItem.setItemStack(nearbyStack);
                        newItem.remove();
                        return;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onExpSpawnMerge(EntitySpawnEvent event) {
        if (!Config.xpMergeEnabled) return;

        if (event.getEntity() instanceof ExperienceOrb newOrb) {
            double radius = TpsMonitor.isLagging(18.0) ? 12.0 : Config.xpMergeRadius;

            for (Entity entity : newOrb.getNearbyEntities(radius, radius, radius)) {
                if (entity instanceof ExperienceOrb nearbyOrb && !nearbyOrb.isDead()) {
                    nearbyOrb.setExperience(nearbyOrb.getExperience() + newOrb.getExperience());
                    newOrb.remove();
                    return;
                }
            }
        }
    }
}