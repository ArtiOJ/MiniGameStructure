package org.test.minigamestructure.gameevents;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.test.minigamestructure.utilities.Colorize;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class LockChestsEvent implements Listener {
    private final JavaPlugin plugin;
    private final int lockDuration;
    private int timeLeft;
    private final Set<UUID> lockedPlayers = new HashSet<>();

    public LockChestsEvent(JavaPlugin plugin, int lockDuration) {
        this.plugin = plugin;
        this.lockDuration = lockDuration;
        timeLeft = lockDuration;
        startLockingChests();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private void startLockingChests() {
        BukkitScheduler scheduler = Bukkit.getScheduler();
        for (Player player : Bukkit.getOnlinePlayers()) {
            lockedPlayers.add(player.getUniqueId());
        }

        scheduler.scheduleSyncRepeatingTask(plugin, () -> {
                    timeLeft--;
                    if (timeLeft <= 0) {
                        lockedPlayers.clear();
                    }
                }, 1, lockDuration
        );
    }


    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (lockedPlayers.contains(event.getPlayer().getUniqueId()) && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block clickedBlock = event.getClickedBlock();
            if (clickedBlock != null && (clickedBlock.getType() == Material.CHEST || clickedBlock.getType() == Material.TRAPPED_CHEST)) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(Colorize.format("&cChests are locked for &e" + timeLeft + "&c seconds."));
            }
        }
    }
}
