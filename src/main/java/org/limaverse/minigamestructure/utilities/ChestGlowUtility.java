package org.limaverse.minigamestructure.utilities;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MagmaCube;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;
import org.bukkit.ChatColor;

import java.util.ArrayList;

public class ChestGlowUtility implements Listener {

    private final JavaPlugin plugin;
    private int timeLeft;
    private boolean active = false;
    private static final ArrayList<LivingEntity> allMagmaCubes = new ArrayList<>();  // Shared across all instances
    private Team magmaCubeTeam;
    private final ChatColor[] colorSequence = {
            ChatColor.DARK_RED, ChatColor.RED, ChatColor.GOLD, ChatColor.YELLOW,
            ChatColor.GREEN, ChatColor.DARK_GREEN
    };
    private int colorIndex = 0;
    private int interval; // Interval for color transitions

    public ChestGlowUtility(JavaPlugin plugin) {
        this.plugin = plugin;
        this.magmaCubeTeam = ScoreboardUtility.getScoreboard().getTeam("magmaCubeTeam");

        if (this.magmaCubeTeam == null) {
            this.magmaCubeTeam = ScoreboardUtility.getScoreboard().registerNewTeam("magmaCubeTeam");
            this.magmaCubeTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        }

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void summonGlowingMagmaCube(Location location, int duration) {
        timeLeft = duration;

        location.setX(location.getX() + .5);
        location.setZ(location.getZ() + .5);
        MagmaCube magmaCube = (MagmaCube) location.getWorld().spawnEntity(location, EntityType.MAGMA_CUBE);
        magmaCube.setAI(false);
        magmaCube.setInvisible(true);
        magmaCube.setSize(2);
        magmaCube.setGlowing(true);
        magmaCube.setInvulnerable(true);
        allMagmaCubes.add(magmaCube);  // Add to the shared list

        // Add the magma cube to the team
        magmaCubeTeam.addEntry(magmaCube.getUniqueId().toString());

        // Calculate the interval for color transitions based on the duration and the number of colors
        interval = duration / (colorSequence.length - 1);

        if (!active) {
            active = true;
            new BukkitRunnable() {
                @Override
                public void run() {
                    timeLeft--;

                    // Update the color only at the appropriate interval
                    if (timeLeft <= interval * (colorSequence.length - colorIndex - 1)) {
                        colorIndex++;
                        updateGlowingColor();
                    }

                    if (timeLeft <= 0) {
                        this.cancel();
                        remove();  // Call the remove() function here
                    }
                }
            }.runTaskTimer(plugin, 20, 20);  // Run every second (20 ticks)
        }
    }

    private void updateGlowingColor() {
        // Update the team color based on the current index
        if (colorIndex < colorSequence.length) {
            magmaCubeTeam.setColor(colorSequence[colorIndex]);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof MagmaCube) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof MagmaCube) {
            event.getPlayer().sendMessage(Colorize.format("&cChests are locked for &e" + timeLeft + "&c seconds."));
            event.setCancelled(true);
        }
    }

    public static void remove() {
        for (LivingEntity magmaCube : allMagmaCubes) {
            magmaCube.remove();
        }
        allMagmaCubes.clear();  // Clear the shared list to avoid memory leaks
    }
}

