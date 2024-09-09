package org.limaverse.minigamestructure.gameevents;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;


public class CountDownEvent implements Listener {
    private final JavaPlugin plugin;
    private final int startCount;
    private final Sound sound;

    public CountDownEvent(JavaPlugin plugin, int startCount, Sound sound) {
        this.plugin = plugin;
        this.startCount = startCount;
        this.sound = sound;
        startCountdown();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private void startCountdown() {
        BukkitScheduler scheduler = Bukkit.getScheduler();
        for (int i = 0; i <= startCount; i++) {
            int count = startCount - i;
            scheduler.runTaskLater(plugin, () -> {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    String title;
                    if (count > 0) {
                        title = getColorForCount(count) + count;
                    } else {
                        title = ChatColor.GREEN + "GO!";
                        HandlerList.unregisterAll(this);
                    }
                    player.sendTitle(title, "", 1, 10, 10);
                    if (sound != null) {
                        player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
                    }
                }
            }, i * 20L);
        }
    }

    private String getColorForCount(int count) {
        if (count > startCount * 0.66) {
            return ChatColor.WHITE.toString();
        } else if (count > startCount * 0.33) {
            return ChatColor.YELLOW.toString();
        } else {
            return ChatColor.RED.toString();
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        event.setCancelled(true);
    }
}
