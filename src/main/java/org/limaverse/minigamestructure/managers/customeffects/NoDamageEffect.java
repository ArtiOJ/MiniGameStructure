package org.limaverse.minigamestructure.managers.customeffects;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

public class NoDamageEffect implements Listener {
    private final JavaPlugin plugin;
    private final Set<Player> noDamagePlayers = new HashSet<>();
    private final long duration;

    public NoDamageEffect(JavaPlugin plugin, long duration) {
        this.plugin = plugin;
        this.duration = duration;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void applyEffect(Player player) {
        noDamagePlayers.add(player);

        // Schedule removal of the effect
        new BukkitRunnable() {
            @Override
            public void run() {
                noDamagePlayers.remove(player);
            }
        }.runTaskLater(plugin, duration * 20L); // Convert duration to ticks
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (noDamagePlayers.contains(player)) {
                event.setCancelled(true);
            }
        }
    }
}
