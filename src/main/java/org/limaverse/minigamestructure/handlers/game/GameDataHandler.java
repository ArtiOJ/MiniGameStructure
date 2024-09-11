package org.limaverse.minigamestructure.handlers.game;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.limaverse.minigamestructure.MiniGameStructure;
import org.limaverse.minigamestructure.handlers.game.events.GameEndEvent;
import org.limaverse.minigamestructure.handlers.game.events.GameStartEvent;
import org.limaverse.minigamestructure.handlers.game.events.RoundEndEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class GameDataHandler implements Listener {
    private List<Player> players;
    private List<Player> spectators;
    private HashMap<Player, Integer> playerPoints;
    private int roundNum = 0;
    private static GameDataHandler instance;

    private GameDataHandler(){
        Bukkit.getPluginManager().registerEvents(this, MiniGameStructure.getInstance());
    }

    @EventHandler
    public void onGameStart(GameStartEvent event) {
        players = new ArrayList<>(event.getPlayers());
        spectators = new ArrayList<>(event.getSpectators());
        playerPoints = new HashMap<>();

        for (Player player : players) {
            playerPoints.put(player, 0);
        }
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerQuitEvent event) {
        removePlayer(event.getPlayer());
    }

    public static synchronized GameDataHandler getInstance() {
        if (instance == null) {
            instance = new GameDataHandler();
        }
        return instance;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void endRound(Player won) {
        roundNum++;
        Bukkit.getPluginManager().callEvent(new RoundEndEvent(players, spectators, roundNum, won));
    }

    public void endGame() {
        Bukkit.getPluginManager().callEvent(new GameEndEvent(playerPoints, spectators));
    }

    public void removePlayer(Player player) {
        players.remove(player);
        playerPoints.remove(player);
    }

    public void addWinPoints(Player player) {
        int currentPoints = playerPoints.getOrDefault(player, 0);
        playerPoints.put(player, currentPoints + 150);
    }

    public void addKillPoints(Player player) {
        int currentPoints = playerPoints.getOrDefault(player, 0);
        playerPoints.put(player, currentPoints + 50);
    }

    public int getPoints(Player player) {
        return playerPoints.getOrDefault(player, 0);
    }

    public HashMap<Player, Integer> getPlayerPoints() {
        return playerPoints;
    }

    // Additional useful methods

    public Player getPlayerWithMostPoints() {
        Player topPlayer = null;
        int maxPoints = 0;

        for (Player player : players) {
            int points = playerPoints.getOrDefault(player.getUniqueId(), 0);
            if (points > maxPoints) {
                maxPoints = points;
                topPlayer = player;
            }
        }

        return topPlayer;
    }
}
