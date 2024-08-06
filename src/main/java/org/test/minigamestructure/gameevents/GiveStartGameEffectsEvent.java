package org.test.minigamestructure.gameevents;

import org.bukkit.Bukkit;
import org.bukkit.block.Barrel;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;
import org.test.minigamestructure.utilities.Colorize;


public class GiveStartGameEffectsEvent implements Listener {
    private final JavaPlugin plugin;
    private final int effectDuration;

    public GiveStartGameEffectsEvent(JavaPlugin plugin, int effectDuration) {
        this.plugin = plugin;
        this.effectDuration = effectDuration;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        applyEffectsToPlayers();
    }

    private void applyEffectsToPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            applyEffects(player);
        }
        startEffectRemovalTask();
    }

    private void applyEffects(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, effectDuration * 20, 0, true, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, effectDuration * 20, 0, true, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, effectDuration * 20, 0, true, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, effectDuration * 20, 20, true, false));
        player.setHealth(20);
    }

    private void startEffectRemovalTask() {
        BukkitScheduler scheduler = Bukkit.getScheduler();
        scheduler.runTaskLater(plugin, this::removeEffectsFromAllPlayers, effectDuration * 20L);
    }

    private void removeEffectsFromPlayer(Player player) {
        player.removePotionEffect(PotionEffectType.SPEED);
        player.removePotionEffect(PotionEffectType.INVISIBILITY);
        player.removePotionEffect(PotionEffectType.GLOWING);
        player.sendMessage(Colorize.format("&cYour effects have expired!"));
    }

    private boolean playerHasEffects(Player player) {
        if (player.hasPotionEffect(PotionEffectType.SPEED) && player.hasPotionEffect(PotionEffectType.INVISIBILITY) && player.hasPotionEffect(PotionEffectType.GLOWING)) {
            return true;
        }
        return false;
    }

    private void removeEffectsFromAllPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!(playerHasEffects(player))) continue;
            removeEffectsFromPlayer(player);
        }

        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onPlayerHurt(EntityDamageEvent event) {
        Entity entity = event.getEntity();

        if (entity instanceof Player) {
            Player player = (Player) entity;

            if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                event.setCancelled(true);
                return;
            }

            if (event instanceof EntityDamageByEntityEvent) {
                EntityDamageByEntityEvent entityDamageByEntityEvent = (EntityDamageByEntityEvent) event;
                Entity damager = entityDamageByEntityEvent.getDamager();

                if (damager instanceof Player) {
                    Player attacker = (Player) damager;

                    if (attacker.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                        removeEffectsFromPlayer(attacker);
                    }
                }
            }

        }
    }


    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof Chest || holder instanceof Barrel || holder instanceof DoubleChest) {
            Player player = (Player) event.getPlayer();
            if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)) removeEffectsFromPlayer(player);
        }
    }
}
