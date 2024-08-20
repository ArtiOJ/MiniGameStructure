package org.test.minigamestructure.gameevents;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MagmaCube;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.ChatColor;
import org.test.minigamestructure.utilities.Colorize;

import java.util.ArrayList;

public class ChestGlowEvent implements Listener {

    private final JavaPlugin plugin;
    private int timeLeft;
    private boolean active = false;
    private final ArrayList<LivingEntity> magmaCubes = new ArrayList<>();
    private Team magmaCubeTeam;
    private final ChatColor[] colorSequence = {
            ChatColor.DARK_RED, ChatColor.RED, ChatColor.GOLD, ChatColor.YELLOW,
            ChatColor.GREEN, ChatColor.DARK_GREEN
    };
    private int colorIndex = 0;
    private int interval; // Interval for color transitions

    public ChestGlowEvent(JavaPlugin plugin) {
        this.plugin = plugin;
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
        magmaCubes.add(magmaCube);

        // Set up the team for the magma cubes
        setupMagmaCubeTeam();

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
                        HandlerList.unregisterAll(ChestGlowEvent.this);
                        for (LivingEntity magmaCube : magmaCubes) {
                            magmaCube.remove();
                        }
                        active = false;  // Reset the active flag
                    }
                }
            }.runTaskTimer(plugin, 20, 20);  // Run every second (20 ticks)
        }
    }

    private void setupMagmaCubeTeam() {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        magmaCubeTeam = scoreboard.getTeam("magmaCubeTeam");

        if (magmaCubeTeam == null) {
            magmaCubeTeam = scoreboard.registerNewTeam("magmaCubeTeam");
            magmaCubeTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
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
        }
    }
}
