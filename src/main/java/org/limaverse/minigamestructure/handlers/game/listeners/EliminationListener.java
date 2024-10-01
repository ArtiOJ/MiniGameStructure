package org.limaverse.minigamestructure.handlers.game.listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.limaverse.minigamestructure.MiniGameStructure;

public class EliminationListener implements Listener {

    private static EliminationListener instance;
    private EliminationListener() {
    }

    public void startListener() {
        Bukkit.getPluginManager().registerEvents(this, MiniGameStructure.getInstance());
    }

    public void stopListener() {
        HandlerList.unregisterAll(this);
    }
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {

    }

    @EventHandler
    public void onPlayerDisconnect(PlayerQuitEvent event) {

    }

    public static synchronized EliminationListener getInstance() {
        if (instance == null) {
            instance = new EliminationListener();
        }
        return instance;
    }
}
