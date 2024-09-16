package org.limaverse.minigamestructure.handlers.game.playerhandlers;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.limaverse.minigamestructure.MiniGameStructure;
import org.limaverse.minigamestructure.handlers.game.events.GameStartEvent;
import org.limaverse.minigamestructure.utilities.Colorize;
import org.limaverse.minigamestructure.utilities.ScoreboardUtility;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.DisplaySlot;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PreGameHandler implements Listener {
    private static PreGameHandler instance; // Singleton instance

    private final MiniGameStructure plugin;
    private long startDelay;
    private final long fallbackStartDelay;
    private static final String SPAWNS_FILE_NAME = "player_spawns.yml";
    private int currentPlayerCount = 0;
    private final int MAX_PLAYER_COUNT;
    private boolean gameStarted = false;
    private boolean fallbackTimerStarted = false;
    private BukkitTask countdownTask;
    private Scoreboard scoreboard;
    private Objective objective;
    private World startWorld;
    private World gameWorld;
    private BukkitScheduler scheduler;
    private final List<Player> spectatingPlayers = new ArrayList<>();
    private final List<Player> gamePlayers = new ArrayList<>(); // Store players in the game

    private PreGameHandler() {
        this.plugin = MiniGameStructure.getInstance();
        this.startDelay = startDelay;
        this.fallbackStartDelay = 20;
        this.scheduler = Bukkit.getScheduler();
        this.startWorld = Bukkit.getWorld("world");
        this.gameWorld = Bukkit.getWorld("game_map");


        File dataFolder = new File(plugin.getServer().getWorld("game_map").getWorldFolder(), "hunger_games_data");
        File configFile = new File(dataFolder, SPAWNS_FILE_NAME);
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        MAX_PLAYER_COUNT = config.getInt("max_players");


        // Register the handler as an event listener
        Bukkit.getPluginManager().registerEvents(this, plugin);

        // Setup scoreboard
        this.scoreboard = ScoreboardUtility.getScoreboard();
        this.objective = scoreboard.getObjective("gameStart");
    }

    // Singleton instance method
    public static synchronized PreGameHandler getInstance() {
        if (instance == null) {
            instance = new PreGameHandler();
        }
        return instance;
    }

    public synchronized void unregister() {
        // Cancel any active countdown tasks
        if (countdownTask != null && !countdownTask.isCancelled()) {
            countdownTask.cancel();
        }

        // Unregister event listeners
        HandlerList.unregisterAll(this);

        // Clear and reset player-related data
        spectatingPlayers.clear();
        gamePlayers.clear();
        currentPlayerCount = 0;
        gameStarted = false;
        fallbackTimerStarted = false;

        // Clear the scoreboard
        clearScoreboard();

        // Set instance to null
        instance = null;
    }

    // Update scoreboard method
    private void updateScoreboard(String statusMessage) {
        scoreboard.getEntries().forEach(scoreboard::resetScores);
        objective.getScore(" ").setScore(3);
        objective.getScore(Colorize.format("&aPlayers: &e" + currentPlayerCount + "&7/&e" + MAX_PLAYER_COUNT)).setScore(2);
        objective.getScore(Colorize.format("&bStatus: &f" + statusMessage)).setScore(1);

        for (var player : Bukkit.getOnlinePlayers()) {
            player.setScoreboard(scoreboard);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (gameStarted) {
            setSpectator(player);
        } else {
            currentPlayerCount++;
            gamePlayers.add(player);
            updateScoreboard(getStatusMessage());
            checkStartConditions();
            player.getInventory().clear();
            player.getInventory().setArmorContents(null);
            player.clearActivePotionEffects();
            player.setGameMode(GameMode.ADVENTURE);
            player.teleport(startWorld.getSpawnLocation());
            player.setAllowFlight(true);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (spectatingPlayers.contains(player)) {
            spectatingPlayers.remove(player);
        } else {
            currentPlayerCount--;
            gamePlayers.remove(player);
            updateScoreboard(getStatusMessage());
            checkStartConditions();
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        scheduler.runTaskLater(plugin, () -> {
            player.setGameMode(GameMode.ADVENTURE);
            player.setAllowFlight(true);

       }, 5);
    }

    public void checkStartConditions() {
        if (gameStarted) return;
        if (currentPlayerCount == MAX_PLAYER_COUNT) {
            startCountdown(startDelay);
        } else if (currentPlayerCount >= MAX_PLAYER_COUNT / 2) {
            if (!fallbackTimerStarted) {
                fallbackTimerStarted = true;
                startCountdown(fallbackStartDelay);
            }
        } else {
            stopCountdown();
            fallbackTimerStarted = false;
        }
    }

    private void startCountdown(long delay) {
        if (countdownTask != null) countdownTask.cancel();
        countdownTask = scheduler.runTaskTimer(plugin, this::countdown, 0L, 20L);
    }

    private void stopCountdown() {
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }
        updateScoreboard(getStatusMessage());
    }

    private void countdown() {
        int secondsLeft = (int) (startDelay / 20);
        if (secondsLeft > 0) {
            updateScoreboard("Starting in " + secondsLeft + " seconds...");
            startDelay -= 20;
            if (currentPlayerCount < (fallbackTimerStarted ? MAX_PLAYER_COUNT / 2 : MAX_PLAYER_COUNT)) {
                stopCountdown();
            }
        } else {
            startGame();
        }
    }

    private void startGame() {
        if (gameStarted) return;
        gameStarted = true;

        // Call the GameStartEvent
        Bukkit.getPluginManager().callEvent(new GameStartEvent(gamePlayers, spectatingPlayers));

        clearScoreboard();
    }

    private void clearScoreboard() {
        for (var player : Bukkit.getOnlinePlayers()) {
            Scoreboard playerScoreboard = player.getScoreboard();
            Objective sidebarObjective = playerScoreboard.getObjective(DisplaySlot.SIDEBAR);
            if (sidebarObjective != null) {
                sidebarObjective.unregister();
            }
        }
    }

    public void forceStart() {
        gameStarted = true;

        // Call the GameStartEvent
        Bukkit.getPluginManager().callEvent(new GameStartEvent(gamePlayers, spectatingPlayers));

        clearScoreboard();
    }

    public void startPreGame() {
        scheduler.runTaskTimer(plugin, this::checkStartConditions, 0L, 20L);
    }

    // Function to set a player as a spectator
    public void setSpectator(Player player) {
        if (gamePlayers.contains(player)) {
            gamePlayers.remove(player); // Remove player from game
        }
        player.setGameMode(GameMode.SPECTATOR);
        player.teleport(gameWorld.getSpawnLocation());
        spectatingPlayers.add(player); // Add player to spectator list
    }

    private String getStatusMessage() {
        if (currentPlayerCount == MAX_PLAYER_COUNT) {
            return "Starting in " + startDelay / 20 + " seconds...";
        } else if (currentPlayerCount >= MAX_PLAYER_COUNT / 2) {
            return "Early start possible, waiting on players!";
        } else {
            return "Waiting on players!";
        }
    }
}
