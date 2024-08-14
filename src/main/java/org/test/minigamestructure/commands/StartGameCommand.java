package org.test.minigamestructure.commands;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.test.minigamestructure.MiniGameStructure;
import org.test.minigamestructure.gameevents.*;
import org.test.minigamestructure.handlers.ChestSpawningHandler;
import org.test.minigamestructure.handlers.chesthandler.ChestHandler;
import org.test.minigamestructure.handlers.PlayerHandler;
import org.test.minigamestructure.managers.StartItemsConfigManager;

import java.util.List;

public class StartGameCommand implements CommandExecutor {
    private final MiniGameStructure plugin;
    private final PlayerHandler playerHandler;
    private final ChestHandler chestHandler;
    private final ChestSpawningHandler chestSpawningHandler;

    // Constructor to initialize the plugin and handlers, and set the command executor.
    public StartGameCommand(MiniGameStructure plugin, PlayerHandler playerHandler, ChestHandler chestHandler, ChestSpawningHandler chestSpawningHandler) {
        this.plugin = plugin;
        this.playerHandler = playerHandler;
        this.chestHandler = chestHandler;
        this.chestSpawningHandler = chestSpawningHandler;
        plugin.getCommand("startgame").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        StartItemsConfigManager configManager = new StartItemsConfigManager(plugin);
        // Iterate over all online players.
        for (Player player : Bukkit.getOnlinePlayers()) {
            // Clear inventory and potion effects, set to survival mode, and give starting items.
            player.getInventory().clear();
            player.clearActivePotionEffects();
            player.setGameMode(GameMode.SURVIVAL);
        }
        World world = plugin.getServer().getWorld("world");
        List<Entity> entList = world.getEntities();
        for(Entity current : entList){
            if (current.getType() == EntityType.DROPPED_ITEM){
                current.remove();
            }
        }


        // Reset all chests
        chestHandler.resetChests();

        // Teleport players to their spawn points.
        playerHandler.teleportPlayers();

        // Spawn chest platforms, game should be ready to start after this is done.
        chestSpawningHandler.spawnChestPlatforms();

        // Freeze players for 5 seconds and start a countdown event.
        new FreezePlayersEvent(plugin, 5, false);
        new CountDownEvent(plugin, 5, Sound.BLOCK_NOTE_BLOCK_PLING);
//        new LockChestsEvent(plugin, 30);
        configManager.giveStartingItems();

        // Schedule a task to give start game effects after 5 seconds (5 * 20 ticks).
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            new GiveStartGameEffectsEvent(plugin, 12);
        }, 5 * 20);

        return true;
    }
}