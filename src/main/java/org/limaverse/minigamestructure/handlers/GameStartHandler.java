package org.limaverse.minigamestructure.handlers;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.limaverse.minigamestructure.gameevents.StartGameEvent;
import org.limaverse.minigamestructure.utilities.Colorize;
import org.limaverse.minigamestructure.utilities.ScoreboardUtility;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Objective;

public class GameStartHandler implements Listener {
    private final Plugin plugin;
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

    public GameStartHandler(Plugin plugin, StartGameEvent startGameEvent, long startDelay, long fallbackStartDelay) {
        this.plugin = plugin;
        this.startGameEvent = startGameEvent;
        this.maxPlayers = Bukkit.getMaxPlayers();
        this.startDelay = startDelay;
        this.fallbackStartDelay = fallbackStartDelay;
        startWorld = Bukkit.getWorld("world");

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
        if (gameStarted) {
            event.getPlayer().kickPlayer("&cA game is in progress!");
        }
        currentPlayerCount++;
        updateScoreboard(getStatusMessage());
        checkStartConditions();
        event.getPlayer().getInventory().clear();
        event.getPlayer().getInventory().setArmorContents(null);
        event.getPlayer().setGameMode(GameMode.ADVENTURE);
        event.getPlayer().teleport(startWorld.getSpawnLocation());
        event.getPlayer().setAllowFlight(true);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        currentPlayerCount--;
        updateScoreboard(getStatusMessage());
        checkStartConditions(); // Check start conditions after a player leaves
    }

    public void checkStartConditions() {
        if (gameStarted) return;

        if (currentPlayerCount >= maxPlayers) {
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
        startDelay = delay; // Set the delay for countdown
        countdownTask = Bukkit.getScheduler().runTaskTimer(plugin, this::countdown, 0L, 20L);
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

    private void startGameIfNotStarted() {
        if (!gameStarted) {
            startGame();
        }
    }

    private void clearScoreboard() {
        for (var player : Bukkit.getOnlinePlayers()) {
            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        }
    }

    private String getStatusMessage() {
        if (currentPlayerCount >= maxPlayers) {
            return "Starting in " + startDelay / 20 + " seconds...";
        } else if (currentPlayerCount >= maxPlayers / 2) {
            return "Early start possible, waiting on players!";
        } else {
            return "Waiting on players!";
        }
    }
}
