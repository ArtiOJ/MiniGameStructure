package org.limaverse.minigamestructure;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.limaverse.minigamestructure.commands.*;
import org.limaverse.minigamestructure.commands.testcommands.*;
import org.limaverse.minigamestructure.handlers.*;
import org.limaverse.minigamestructure.handlers.game.GameStateHandler;
import org.limaverse.minigamestructure.handlers.game.chesthandler.ChestHandler;
//import org.limaverse.minigamestructure.gameevents.StartGameEvent;
import org.limaverse.minigamestructure.handlers.game.chesthandler.ChestSpawningHandler;

/**
 * The main class for the MiniGameStructure plugin.
 * This class initializes the plugin, registers commands and events, and sets up necessary handlers.
 */
public class MiniGameStructure extends JavaPlugin {
    private ChestHandler chestHandler;
    private ChestSpawningHandler chestSpawningHandler;
    private ResetHandler resetHandler;
    private GameStateHandler gameStateHandler;
//    private PlayerXpTimerHandler playerXpTimerHandler;
//    private GameStartHandler gameStartHandler;
//    private StartGameEvent startGameEvent;
    private static MiniGameStructure instance;

    /**
     * Called when the plugin is enabled. Initializes all components and registers commands.
     */
    @Override
    public void onEnable() {
        instance = this;
        resetHandler = new ResetHandler(this);
        resetHandler.initWorld("game_map");
        gameStateHandler = GameStateHandler.getInstance();
        new GetState(this);
        new SetState(this);
        new Start(this);
        new EliminatePlayer(this);
        new GetPoints(this);
        //playerXpTimerHandler = new PlayerXpTimerHandler(this);
        // Initialize StartGameEvent with necessary handlers
        //startGameEvent = new StartGameEvent(chestHandler, chestSpawningHandler, playerXpTimerHandler);
        // Initialize GameStartHandler with conditions for starting the game
        //long startDelay = 5 * 20; // 5 seconds delay (100 ticks) after the lobby fills up
        //long fallbackStartDelay = 20 * 20L;
        //gameStartHandler = new GameStartHandler(this, startGameEvent, startDelay, fallbackStartDelay);

        // Schedule periodic checks for game start conditions
        //Bukkit.getScheduler().runTaskTimer(this, gameStartHandler::checkStartConditions, 0L, 20L);

        registerCommands();
    }

    /**
     * Registers all commands used in the plugin.
     */
    private void registerCommands() {
//        new StartGameCommand(this, gameStartHandler);
//        new SetPlayerSpawnCommand(this);
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

    public ChestSpawningHandler getChestSpawningHandler() {
        return chestSpawningHandler;
    }

    public ResetHandler getResetHandler() {
        return resetHandler;
    }

//    public PlayerXpTimerHandler getPlayerXpTimerHandler() {
//        return playerXpTimerHandler;
//    }
//
//    public GameStartHandler getGameStartHandler() {
//        return gameStartHandler;
//    }

    public static MiniGameStructure getInstance() {
        return instance;
    }
}