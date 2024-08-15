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
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.test.minigamestructure.utilities.Colorize;

import java.util.ArrayList;

public class ChestGlowEvent implements Listener {

    private final JavaPlugin plugin;
    private final BukkitScheduler scheduler;
    private int timeLeft;
    private boolean active = false;
    private ArrayList<LivingEntity> magmaCubes = new ArrayList<>();
    public ChestGlowEvent(JavaPlugin plugin) {
        this.plugin = plugin;
        scheduler = Bukkit.getScheduler();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void summonGlowingMagmaCube(Location location, int duration) {
        timeLeft = duration;
        location.setX(location.x() + .5);
        location.setZ(location.z() + .5);
        MagmaCube magmaCube = (MagmaCube) location.getWorld().spawnEntity(location, EntityType.MAGMA_CUBE);
        magmaCube.setAI(false);
        magmaCube.setInvisible(true);
        magmaCube.setSize(2);
        magmaCube.setGlowing(true);
        magmaCube.setInvulnerable(true);
        magmaCubes.add(magmaCube);

        final ChestGlowEvent SELF = this;
        if (!active) {
            active = true;
            new BukkitRunnable() {
                @Override
                public void run() {
                    timeLeft--;
                    if (timeLeft <= 0) {
                        this.cancel();
                        HandlerList.unregisterAll(SELF);
                        for (LivingEntity magmaCube : magmaCubes) {
                            magmaCube.remove();
                        }
                    }
                }
            }.runTaskTimer(plugin, 20, 20);

        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event){
        if (event.getEntity() instanceof MagmaCube) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteractEnitity(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof MagmaCube){
            event.getPlayer().sendMessage(Colorize.format("&cChests are locked for &e" + timeLeft + "&c seconds."));
        }
    }
}
