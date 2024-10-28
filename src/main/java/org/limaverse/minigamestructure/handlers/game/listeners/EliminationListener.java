package org.limaverse.minigamestructure.handlers.game.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.limaverse.minigamestructure.MiniGameStructure;
import org.limaverse.minigamestructure.handlers.game.GameStateHandler;
import org.limaverse.minigamestructure.handlers.game.GameStates;
import org.limaverse.minigamestructure.handlers.game.GameStats;
import org.limaverse.minigamestructure.utilities.EliminationUtil;

public class EliminationListener implements Listener {

    private static EliminationListener instance;
    private GameStats gameStats;
    private EliminationListener() {
        gameStats = GameStats.getInstance();
    }

    public void startListener() {
        Bukkit.getPluginManager().registerEvents(this, MiniGameStructure.getInstance());
    }

    public void stopListener() {
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!GameStateHandler.getInstance().isState(GameStates.STARTED)) return;
        if (event.getPlayer().equals(event.getEntity().getKiller())) {
            gameStats.removeAlivePlayer(event.getPlayer());
            return;
        }
        gameStats.removeAlivePlayer(event.getPlayer(), event.getEntity().getKiller());
    }
    public static synchronized EliminationListener getInstance() {
        if (instance == null) {
            instance = new EliminationListener();
        }
        return instance;
    }
}
