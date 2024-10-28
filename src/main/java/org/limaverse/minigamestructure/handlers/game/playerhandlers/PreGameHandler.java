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
import java.util.List;

public class PreGameHandler implements Listener {
    private static PreGameHandler instance;

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
    private final List<Player> gamePlayers = new ArrayList<>();

    private PreGameHandler() {
        this.plugin = MiniGameStructure.getInstance();
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

        // Retrieve the shared scoreboard and objective
        this.scoreboard = ScoreboardUtility.getScoreboard();
        this.objective = ScoreboardUtility.getObjective();
    }

    public static synchronized PreGameHandler getInstance() {
        if (instance == null) {
            instance = new PreGameHandler();
        }
        return instance;
    }

    public synchronized void unregister() {
        if (countdownTask != null && !countdownTask.isCancelled()) {
            countdownTask.cancel();
        }
        HandlerList.unregisterAll(this);
        spectatingPlayers.clear();
        gamePlayers.clear();
        currentPlayerCount = 0;
        gameStarted = false;
        fallbackTimerStarted = false;
        wipeBoard(); // Only clear entries set by PreGameHandler
        instance = null;
    }

    // Update scoreboard method (adds or updates specific scores)
    private void updateScoreboard(String statusMessage) {
        // Clear previous entries created by PreGameHandler
        resetBoard();

        // Set scores in the desired order and with unique identifiers
        objective.getScore(" ").setScore(3);  // Blank line for separation
        objective.getScore(Colorize.format("&aPlayers: &e" + currentPlayerCount + "&7/&e" + MAX_PLAYER_COUNT)).setScore(2);  // Player count
        objective.getScore(Colorize.format("&bStatus: &f" + statusMessage)).setScore(1);  // Status message

        // Apply the updated scoreboard to all online players
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setScoreboard(scoreboard);
        }
    }

    // Clear only the entries that were added by PreGameHandler
    private void resetBoard() {
        scoreboard.getEntries().forEach(scoreboard::resetScores);
    }

    private void wipeBoard() {
        scoreboard.clearSlot(DisplaySlot.SIDEBAR);
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
        Bukkit.getPluginManager().callEvent(new GameStartEvent(gamePlayers, spectatingPlayers));
        wipeBoard(); // Clear only PreGameHandler-related entries
    }

    public void forceStart() {
        gameStarted = true;
        Bukkit.getPluginManager().callEvent(new GameStartEvent(gamePlayers, spectatingPlayers));
        wipeBoard(); // Clear only PreGameHandler-related entries
    }

    public void startPreGame() {
        scheduler.runTaskTimer(plugin, this::checkStartConditions, 0L, 20L);
    }

    public void setSpectator(Player player) {
        if (gamePlayers.contains(player)) {
            gamePlayers.remove(player);
        }
        player.setGameMode(GameMode.SPECTATOR);
        player.teleport(gameWorld.getSpawnLocation());
        spectatingPlayers.add(player);
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
