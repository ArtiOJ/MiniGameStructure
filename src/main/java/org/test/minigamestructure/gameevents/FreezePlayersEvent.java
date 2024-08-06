package org.test.minigamestructure.gameevents;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class FreezePlayersEvent implements Listener {
    private final JavaPlugin plugin;
    private final long freezeTimeTicks;
    private final boolean lockHead;

    public FreezePlayersEvent(JavaPlugin plugin, int freezeTimeSeconds, boolean lockHead) {
        this.plugin = plugin;
        this.freezeTimeTicks = freezeTimeSeconds * 20L; // Convert seconds to ticks
        this.lockHead = lockHead;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        scheduleUnfreeze();
    }

    private void scheduleUnfreeze() {
        BukkitScheduler scheduler = Bukkit.getScheduler();
        scheduler.runTaskLater(plugin, this::unregister, freezeTimeTicks);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (lockHead) {
            if (event.getFrom().distanceSquared(event.getTo()) != 0) {
                event.setTo(event.getFrom());
            }
        } else {
            if (event.getFrom().getX() != event.getTo().getX() || event.getFrom().getZ() != event.getTo().getZ()) {
                event.setTo(event.getFrom());
            }
        }
    }

    private void unregister() {
        PlayerMoveEvent.getHandlerList().unregister(this);
        PlayerQuitEvent.getHandlerList().unregister(this);
    }
}
