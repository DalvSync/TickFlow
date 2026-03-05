package org.dalvsync.tickflow;

import org.bukkit.block.Block;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable; // Тот самый классический таймер

public final class TickFlow extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("TickFlow v1.1 for Paper 1.19.x is now active!");
    }

    // ==========================================
    // 1. Очистка стрел
    // ==========================================
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!getConfig().getBoolean("features.projectile-cleanup.enabled")) return;
        if (event.getEntity() instanceof AbstractArrow arrow) {
            if (event.getHitBlock() != null) {
                if (arrow instanceof Trident || arrow.getShooter() instanceof Player) return;
                arrow.remove();
            }
        }
    }

    // ==========================================
    // 2. Лоботомия жителей (Классический таймер для 1.19.4)
    // ==========================================
    @EventHandler
    public void onEntitiesLoad(EntitiesLoadEvent event) {
        if (!getConfig().getBoolean("features.villager-lobotomy.enabled")) return;
        for (Entity entity : event.getEntities()) {
            if (entity instanceof Villager villager) startVillagerTask(villager);
        }
    }

    @EventHandler
    public void onVillagerSpawn(CreatureSpawnEvent event) {
        if (!getConfig().getBoolean("features.villager-lobotomy.enabled")) return;
        if (event.getEntity() instanceof Villager villager) startVillagerTask(villager);
    }

    private void startVillagerTask(Villager villager) {
        // Запускаем асинхронную задачу через BukkitRunnable
        new BukkitRunnable() {
            @Override
            public void run() {
                // Если житель умер или пропал - убиваем задачу
                if (!villager.isValid()) {
                    this.cancel();
                    return;
                }
                boolean trapped = isTrapped(villager);
                if (villager.isAware() == trapped) {
                    villager.setAware(!trapped);
                }
            }
        }.runTaskTimer(this, 100L, 200L);
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

    // ==========================================
    // 3. Ускоренное исчезновение мусора
    // ==========================================
    @EventHandler
    public void onTrashItemSpawn(ItemSpawnEvent event) {
        if (!getConfig().getBoolean("features.fast-trash-despawn.enabled")) return;
        Item item = event.getEntity();
        Material type = item.getItemStack().getType();
        if (isTrash(type)) {
            item.setTicksLived(getConfig().getInt("features.fast-trash-despawn.ticks-lived-to-set", 5100));
        }
    }

    private boolean isTrash(Material type) {
        return type == Material.COBBLESTONE || type == Material.DIRT ||
                type == Material.NETHERRACK || type == Material.GRAVEL ||
                type == Material.ANDESITE || type == Material.DIORITE || type == Material.GRANITE;
    }

    // ==========================================
    // 4. Слияние предметов
    // ==========================================
    @EventHandler
    public void onItemSpawnMerge(ItemSpawnEvent event) {
        if (!getConfig().getBoolean("features.item-merging.enabled")) return;
        Item newItem = event.getEntity();
        ItemStack newStack = newItem.getItemStack();
        double radius = getConfig().getDouble("features.item-merging.merge-radius", 3.5);

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

    // ==========================================
    // 5. Слияние опыта
    // ==========================================
    @EventHandler
    public void onExpSpawnMerge(EntitySpawnEvent event) {
        if (!getConfig().getBoolean("features.xp-merging.enabled")) return;
        if (event.getEntity() instanceof ExperienceOrb newOrb) {
            double radius = getConfig().getDouble("features.xp-merging.merge-radius", 4.0);
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