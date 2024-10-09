package org.limaverse.minigamestructure.handlers.game;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRecipeDiscoverEvent;
import org.limaverse.minigamestructure.MiniGameStructure;
import org.limaverse.minigamestructure.handlers.game.events.GameStartEvent;
import org.limaverse.minigamestructure.handlers.game.events.RoundEndEvent;
import org.limaverse.minigamestructure.handlers.game.events.RoundStartEvent;
import org.limaverse.minigamestructure.utilities.EliminationUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameStats implements Listener {

    private static GameStats instance;
    private List<Player> startGamePlayers = new ArrayList<>();
    private List<Player> activeGamePlayers = new ArrayList();
    private List<Player> gameSpectators = new ArrayList();
    private List<Player> alivePlayers = new ArrayList();
    private Map<Player, Integer> playerPoints = new HashMap();
    private boolean gameStarted = false;
    private boolean inRound = false;

    private GameStats() {
        Bukkit.getPluginManager().registerEvents(this, MiniGameStructure.getInstance());
    }

    public static synchronized GameStats getInstance() {
        if (instance == null) {
            instance = new GameStats();
        }
        return instance;
    }

    public List<Player> getStartGamePlayers() {
        return startGamePlayers;
    }

    public List<Player> getActiveGamePlayers() {
        return activeGamePlayers;
    }

    public List<Player> getGameSpectators() {
        return gameSpectators;
    }

    public List<Player> getAlivePlayers() {
        return alivePlayers;
    }
    @EventHandler
    public void onGameStart(GameStartEvent event) {
        startGamePlayers = event.getPlayers();
        activeGamePlayers = event.getPlayers();
        gameSpectators = event.getSpectators();
        alivePlayers = activeGamePlayers;
        gameStarted = true;
        inRound = true;
    }

    @EventHandler
    public void onRoundEnd(RoundEndEvent event) {
        Bukkit.broadcastMessage("round ended");
        alivePlayers.clear();
        inRound = false;
    }

    @EventHandler
    public void onRoundStart(RoundStartEvent event) {
        alivePlayers = event.getPlayersInGame();
        inRound = true;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (gameSpectators.contains(player)) {
            gameSpectators.remove(player);
        }
        if (startGamePlayers.contains(player)) {
            activeGamePlayers.remove(player);
        }
        if (alivePlayers.contains(player)) {
            alivePlayers.remove(player);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (startGamePlayers.contains(event.getPlayer())) {
            new EliminationUtil(event.getPlayer());
            activeGamePlayers.add(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!GameStateHandler.getInstance().isState(GameStates.STARTED)) return;
        Bukkit.broadcastMessage("did this run");
        Bukkit.broadcastMessage(event.getPlayer().toString());
        Bukkit.broadcastMessage(alivePlayers.toString());
        Bukkit.broadcastMessage(String.valueOf(alivePlayers.contains(event.getPlayer())));
        if (alivePlayers.contains(event.getPlayer())) {
            Bukkit.broadcastMessage("and this?");
            alivePlayers.remove(event.getPlayer());
            new EliminationUtil(event.getPlayer());
        }
    }
}
