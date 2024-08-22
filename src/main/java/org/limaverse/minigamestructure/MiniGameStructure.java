package org.limaverse.minigamestructure;

import org.bukkit.plugin.java.JavaPlugin;
import org.limaverse.minigamestructure.commands.*;
import org.limaverse.minigamestructure.handlers.ChestSpawningHandler;
import org.limaverse.minigamestructure.handlers.PlayerXpTimerHandler;
import org.limaverse.minigamestructure.handlers.ResetHandler;
import org.limaverse.minigamestructure.handlers.chesthandler.ChestHandler;
import org.limaverse.minigamestructure.handlers.PlayerHandler;

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

    /**
     * Called when the plugin is enabled. Initializes all components and registers commands.
     */
    @Override
    public void onEnable() {

        chestHandler = new ChestHandler(this);
        playerHandler = new PlayerHandler(this);
        chestSpawningHandler = new ChestSpawningHandler(this);
        resetHandler = new ResetHandler(this, chestHandler);
        playerXpTimerHandler = new PlayerXpTimerHandler(this);
        registerCommands();
    }

    /**
     * Registers all commands used in the plugin.
     */
    private void registerCommands() {
        new StartGameCommand(this, playerHandler, chestHandler, chestSpawningHandler, playerXpTimerHandler);
        new SetPlayerSpawnCommand(this);
        new SetChestSpawnCommand(this, chestSpawningHandler);
        new ResetMapCommand(this, chestHandler);
        new ResetMapPreciseCommand(this, chestHandler);
        new ResetChestsCommand(this, chestHandler);
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

    public PlayerXpTimerHandler getPlayerXpTimerHandler(){
        return playerXpTimerHandler;
    }
}
