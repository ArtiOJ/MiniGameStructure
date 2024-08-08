package org.test.minigamestructure;

import org.bukkit.plugin.java.JavaPlugin;
import org.test.minigamestructure.commands.*;
import org.test.minigamestructure.handlers.PlayerHandler;
import org.test.minigamestructure.handlers.chesthandler.ChestHandler;
import org.test.minigamestructure.handlers.ChestSpawningHandler;
import org.test.minigamestructure.handlers.ResetHandler;

/**
 * The main class for the MiniGameStructure plugin.
 * This class initializes the plugin, registers commands and events, and sets up necessary handlers.
 */
public class MiniGameStructure extends JavaPlugin {
    private ChestHandler chestHandler;
    private PlayerHandler playerHandler;
    private ChestSpawningHandler chestSpawningHandler;
    private ResetHandler resetHandler;

    /**
     * Called when the plugin is enabled. Initializes all components and registers commands.
     */
    @Override
    public void onEnable() {

        chestHandler = new ChestHandler(this);
        playerHandler = new PlayerHandler(this);
        chestSpawningHandler = new ChestSpawningHandler(this);
        resetHandler = new ResetHandler(this, chestHandler);

        registerCommands();
    }

    /**
     * Registers all commands used in the plugin.
     */
    private void registerCommands() {
        new StartGameCommand(this, playerHandler, chestHandler, chestSpawningHandler);
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
}
