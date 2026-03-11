package org.dalvsync.tickflow.listeners;

import org.bukkit.Material;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.dalvsync.tickflow.TickFlow;
import org.dalvsync.tickflow.config.Config;
import org.dalvsync.tickflow.utils.TpsMonitor;

public class MechanicsListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onBlockGrowThrottling(BlockGrowEvent event) {
        if (!Config.adaptiveGrowthEnabled) return;

        Material type = event.getNewState().getType();
        if (type == Material.SUGAR_CANE || type == Material.CACTUS || type == Material.KELP || type == Material.BAMBOO) {
            if (TpsMonitor.isLagging(17.0) && Math.random() < 0.50) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!Config.projectileCleanupEnabled) return;

        if (event.getEntity() instanceof AbstractArrow arrow) {
            if (event.getHitBlock() != null) {
                if (arrow instanceof Trident || arrow.getShooter() instanceof Player) {
                    return;
                }
                arrow.remove();
            }
        }
    }

    @EventHandler
    public void onArmorStandSpawn(CreatureSpawnEvent event) {
        if (!Config.armorStandOptimization || !TickFlow.IS_PAPER) return;

        if (event.getEntity() instanceof ArmorStand armorStand) {
            try {
                if (armorStand.isMarker() || !armorStand.isVisible()) {
                    armorStand.setGravity(false);
                }
            } catch (NoSuchMethodError ignored) {
            }
        }
    }
}