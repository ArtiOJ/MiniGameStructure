package org.limaverse.minigamestructure.handlers.game;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.limaverse.minigamestructure.MiniGameStructure;
import org.limaverse.minigamestructure.handlers.game.events.GameStartEvent;
import org.limaverse.minigamestructure.handlers.game.events.RoundEndEvent;
import org.limaverse.minigamestructure.handlers.game.events.RoundStartEvent;
import org.limaverse.minigamestructure.utilities.EliminationUtil;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GameStats implements Listener {

    private static GameStats instance;
    private List<Player> startGamePlayers = new ArrayList<>();
    private List<Player> activeGamePlayers = new ArrayList<>();
    private List<Player> gameSpectators = new ArrayList<>();
    private List<Player> alivePlayers = new ArrayList<>();
    private Map<Player, Integer> playerPoints = new HashMap<>();
    private Map<Player, Integer> playerKills = new HashMap<>(); // Track player kills
    private Map<Player, Integer> playerWins = new HashMap<>(); // Track player wins
    private Map<Player, Instant> playerSurvivalStart = new HashMap<>(); // Track when player joined round
    private boolean gameStarted = false;
    private boolean inRound = false;

    private Instant gameStartTime;
    private Instant roundStartTime;

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

    public Map<Player, Integer> getPlayerKills() {
        return playerKills;
    }

    public Map<Player, Integer> getPlayerWins() {
        return playerWins;
    }

    public int getPlayerPoints(Player player) {
        return playerPoints.getOrDefault(player, 0);
    }

    public String getGameDuration() {
        if (gameStartTime == null) return "Game not started";
        Duration gameDuration = Duration.between(gameStartTime, Instant.now());
        return formatDuration(gameDuration);
    }

    public String getRoundDuration() {
        if (roundStartTime == null) return "Round not started";
        Duration roundDuration = Duration.between(roundStartTime, Instant.now());
        return formatDuration(roundDuration);
    }

    private String formatDuration(Duration duration) {
        long seconds = duration.getSeconds();
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    public void removeAlivePlayer(Player player) {
        removeAlivePlayer(player, null); // Call overloaded method with no eliminator
    }

    public void removeAlivePlayer(Player player, Player eliminator) {
        if (!gameStarted) return;

        if (alivePlayers.contains(player)) alivePlayers.remove(player);

        // Update eliminator stats if applicable
        if (eliminator != null) {
            playerPoints.put(eliminator, playerPoints.getOrDefault(eliminator, 0) + 50);
            playerKills.put(eliminator, playerKills.getOrDefault(eliminator, 0) + 1);
        }

        // Calculate time survived
        Instant startTime = playerSurvivalStart.getOrDefault(player, roundStartTime);
        Duration timeSurvived = Duration.between(startTime, Instant.now());

        // Call the constructor with player, kills, wins, time survived
        int kills = playerKills.getOrDefault(player, 0);
        int wins = playerWins.getOrDefault(player, 0);
        new EliminationUtil(player, kills, wins, formatDuration(timeSurvived));

        if (alivePlayers.size() <= 1) {
            Player winner = alivePlayers.isEmpty() ? null : alivePlayers.get(0);
            if (winner != null) {
                // Increment winner's win count
                playerWins.put(winner, playerWins.getOrDefault(winner, 0) + 1);
            }
            Bukkit.getPluginManager().callEvent(new RoundEndEvent(activeGamePlayers, gameSpectators, winner));
        }
    }

    @EventHandler
    public void onGameStart(GameStartEvent event) {
        startGamePlayers = new ArrayList<>(event.getPlayers());
        activeGamePlayers = new ArrayList<>(event.getPlayers());
        gameSpectators = new ArrayList<>(event.getSpectators());
        alivePlayers = new ArrayList<>(event.getPlayers());
        playerPoints = event.getPlayers().stream().collect(Collectors.toMap(p -> p, p -> 0, (a, b) -> a, HashMap::new));
        playerKills = event.getPlayers().stream().collect(Collectors.toMap(p -> p, p -> 0, (a, b) -> a, HashMap::new)); // Initialize kills
        playerWins = event.getPlayers().stream().collect(Collectors.toMap(p -> p, p -> 0, (a, b) -> a, HashMap::new)); // Initialize wins
        playerSurvivalStart = event.getPlayers().stream().collect(Collectors.toMap(p -> p, p -> Instant.now(), (a, b) -> a, HashMap::new)); // Start survival tracking
        gameStarted = true;
        inRound = true;
        gameStartTime = Instant.now(); // Start game timer
        roundStartTime = Instant.now(); // Start round timer
    }

    @EventHandler
    public void onRoundEnd(RoundEndEvent event) {
        alivePlayers.clear();
        inRound = false;
        roundStartTime = null; // Reset round timer
    }

    @EventHandler
    public void onRoundStart(RoundStartEvent event) {
        alivePlayers = event.getPlayersInGame();
        inRound = true;
        roundStartTime = Instant.now(); // Start a new round timer
        playerSurvivalStart = alivePlayers.stream().collect(Collectors.toMap(p -> p, p -> Instant.now())); // Track survival time for new round
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (gameSpectators.contains(player)) {
            gameSpectators.remove(player);
        }

        if (gameStarted) {
            if (startGamePlayers.contains(player)) {
                activeGamePlayers.remove(player);
            }
            removeAlivePlayer(player);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (gameStarted) {
            if (startGamePlayers.contains(player)) {
                new EliminationUtil(player);
                activeGamePlayers.add(player);
            }
        }
    }
}

