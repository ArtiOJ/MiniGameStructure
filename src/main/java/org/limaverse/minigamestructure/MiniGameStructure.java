package org.limaverse.minigamestructure;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.limaverse.minigamestructure.commands.*;
import org.limaverse.minigamestructure.handlers.ChestSpawningHandler;
import org.limaverse.minigamestructure.handlers.PlayerXpTimerHandler;
import org.limaverse.minigamestructure.handlers.ResetHandler;
import org.limaverse.minigamestructure.handlers.chesthandler.ChestHandler;
import org.limaverse.minigamestructure.handlers.PlayerHandler;
import org.limaverse.minigamestructure.gameevents.StartGameEvent;
import org.limaverse.minigamestructure.handlers.GameStartHandler;

/**
 * The main class for the MiniGameStructure plugin.
 * This class initializes the plugin, registers commands and events, and sets up necessary handlers.
 */
public class MiniGameStructure extends JavaPlugin {
    private ChestHandler chestHandler;
    private PlayerHandler playerHandler;
    private ChestSpawningHandler chestSpawningHandler;
    private ResetHandler resetHandler;
    private PlayerXpTimerHandler playerXpTimerHandler;
    private GameStartHandler gameStartHandler;
    private StartGameEvent startGameEvent;

    /**
     * Called when the plugin is enabled. Initializes all components and registers commands.
     */
    @Override
    public void onEnable() {
        resetHandler = new ResetHandler(this);
        resetHandler.initWorld("game_map");
        chestHandler = new ChestHandler(this);
        playerHandler = new PlayerHandler(this);
        chestSpawningHandler = new ChestSpawningHandler(this);
        playerXpTimerHandler = new PlayerXpTimerHandler(this);

        // Initialize StartGameEvent with necessary handlers
        startGameEvent = new StartGameEvent(this, playerHandler, chestHandler, chestSpawningHandler, playerXpTimerHandler);
        // Initialize GameStartHandler with conditions for starting the game
        int maxPlayers = 8; // Replace with your actual max players value
        long startDelay = 100L; // 5 seconds delay (100 ticks) after the lobby fills up
        long fallbackStartDelay = 20 * 300L; // 300 seconds fallback delay (6000 ticks) if the lobby doesn't fill up
        gameStartHandler = new GameStartHandler(this, startGameEvent, maxPlayers, startDelay, fallbackStartDelay);

        // Schedule periodic checks for game start conditions
        Bukkit.getScheduler().runTaskTimer(this, gameStartHandler::checkStartConditions, 0L, 20L);

        registerCommands();
    }

    /**
     * Registers all commands used in the plugin.
     */
    private void registerCommands() {
        new StartGameCommand(this, startGameEvent);
        new SetPlayerSpawnCommand(this);
        new SetChestSpawnCommand(this, chestSpawningHandler);
        new ResetMapCommand(this);
        new ResetMapPreciseCommand(this);
        new ResetChestsCommand(this, chestHandler);
        new ResetRandomCommand(this);
    }

    // Getters for the handlers, if needed
    public ChestHandler getChestHandler() {
        return chestHandler;
    }

    public PlayerHandler getPlayerHandler() {
        return playerHandler;
    }

    public ChestSpawningHandler getChestSpawningHandler() {
        return chestSpawningHandler;
    }

    public ResetHandler getResetHandler() {
        return resetHandler;
    }

    public PlayerXpTimerHandler getPlayerXpTimerHandler() {
        return playerXpTimerHandler;
    }

    public GameStartHandler getGameStartHandler() {
        return gameStartHandler;
    }
}
