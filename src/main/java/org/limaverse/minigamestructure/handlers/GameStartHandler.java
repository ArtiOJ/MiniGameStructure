package org.limaverse.minigamestructure.handlers;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.limaverse.minigamestructure.MiniGameStructure;
import org.limaverse.minigamestructure.gameevents.StartGameEvent;
import org.limaverse.minigamestructure.utilities.Colorize;
import org.limaverse.minigamestructure.utilities.ScoreboardUtility;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.DisplaySlot;

import java.util.ArrayList;
import java.util.List;

public class GameStartHandler implements Listener {
    private final MiniGameStructure plugin;
    private final StartGameEvent startGameEvent;
    private final int maxPlayers;
    private long startDelay;
    private final long fallbackStartDelay;

    private int currentPlayerCount = 0;
    private boolean gameStarted = false;
    private boolean fallbackTimerStarted = false;
    private BukkitTask countdownTask;
    private Scoreboard scoreboard;
    private Objective objective;
    private World startWorld;
    private World gameWorld; // Add the game world reference
    private BukkitScheduler scheduler;
    private List<Player> spectatingPlayers = new ArrayList<>(); // List to store spectating players

    public GameStartHandler(MiniGameStructure plugin, StartGameEvent startGameEvent, long startDelay, long fallbackStartDelay) {
        this.plugin = plugin;
        this.startGameEvent = startGameEvent;
        this.maxPlayers = Bukkit.getMaxPlayers();
        this.startDelay = startDelay;
        this.fallbackStartDelay = fallbackStartDelay;
        this.scheduler = Bukkit.getScheduler();
        startWorld = Bukkit.getWorld("world");
        gameWorld = Bukkit.getWorld("game_map"); // Assign the game world

        // Register this class as an event listener
        Bukkit.getPluginManager().registerEvents(this, plugin);

        // Setup scoreboard
        this.scoreboard = ScoreboardUtility.getScoreboard();
        this.objective = scoreboard.getObjective("gameStart");
    }

    private void updateScoreboard(String statusMessage) {
        // Clear previous scores to prevent duplicates
        scoreboard.getEntries().forEach(scoreboard::resetScores);

        // Set the updated scores
        objective.getScore(" ").setScore(3); // Blank line for spacing
        objective.getScore(Colorize.format("&aPlayers: &e" + currentPlayerCount + "&7/&e" + maxPlayers)).setScore(2);
        objective.getScore(Colorize.format("&bStatus: &f" + statusMessage)).setScore(1);

        // Update for all online players
        for (var player : Bukkit.getOnlinePlayers()) {
            player.setScoreboard(scoreboard);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (gameStarted) {
            // Send the player to the game world in spectator mode and add them to spectatingPlayers
            player.setGameMode(GameMode.SPECTATOR);
            player.teleport(gameWorld.getSpawnLocation());
            spectatingPlayers.add(player);
        } else {
            currentPlayerCount++;
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
            spectatingPlayers.remove(player); // Remove the player from the spectating list if they leave
        } else {
            currentPlayerCount--;
            updateScoreboard(getStatusMessage());
            checkStartConditions(); // Check start conditions after a player leaves
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        if (gameStarted) {
            scheduler.runTaskLater(plugin, () -> {
                player.setGameMode(GameMode.SPECTATOR);
                player.teleport(gameWorld.getSpawnLocation());
                spectatingPlayers.add(player); // Add player to spectating list on respawn
            }, 5);
        } else {
            scheduler.runTaskLater(plugin, () -> {
                player.setGameMode(GameMode.ADVENTURE);
                player.setAllowFlight(true);
            }, 5);
        }
    }

    public List<Player> getSpectatingPlayers() {
        return new ArrayList<>(spectatingPlayers); // Return a copy of the list to prevent external modification
    }

    public void checkStartConditions() {
        if (gameStarted) return;

        if (currentPlayerCount == maxPlayers) {
            startCountdown(startDelay);
        } else if (currentPlayerCount >= maxPlayers / 2) {
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

        // Only reset startDelay if the countdown is not already running
        if (countdownTask == null || countdownTask.isCancelled()) {
            startDelay = delay; // Set the delay for countdown
        }

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

            // Check if player count drops below the required amount during countdown
            if (currentPlayerCount < (fallbackTimerStarted ? maxPlayers / 2 : maxPlayers)) {
                stopCountdown();
            }
        } else {
            startGame();
        }
    }

    private void startGame() {
        if (gameStarted) return;
        gameStarted = true;
        startGameEvent.startGame();
        clearScoreboard();
    }

    private void clearScoreboard() {
        for (var player : Bukkit.getOnlinePlayers()) {
            Scoreboard playerScoreboard = player.getScoreboard();
            Objective sidebarObjective = playerScoreboard.getObjective(DisplaySlot.SIDEBAR);
            if (sidebarObjective != null) {
                sidebarObjective.unregister(); // Unregister the sidebar objective, clearing only the sidebar
            }
        }
    }


    public void forceStart() {
        gameStarted = true;
        startGameEvent.startGame();
    }

    private String getStatusMessage() {
        if (currentPlayerCount == maxPlayers) {
            return "Starting in " + startDelay / 20 + " seconds...";
        } else if (currentPlayerCount >= maxPlayers / 2) {
            return "Early start possible, waiting on players!";
        } else {
            return "Waiting on players!";
        }
    }
}
