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

    public StartGameCommand(MiniGameStructure plugin, PlayerHandler playerHandler, ChestHandler chestHandler, ChestSpawningHandler chestSpawningHandler) {
        this.plugin = plugin;
        this.playerHandler = playerHandler;
        this.chestHandler = chestHandler;
        this.chestSpawningHandler = chestSpawningHandler;
        plugin.getCommand("startgame").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // Run player-related tasks on the main thread as they interact with Bukkit API.
        StartItemsConfigManager configManager = new StartItemsConfigManager(plugin);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.getInventory().clear();
            player.clearActivePotionEffects();
            player.setGameMode(GameMode.SURVIVAL);
        }


        World world = plugin.getServer().getWorld("world");
        List<Entity> entList = world.getEntities();
        for (Entity current : entList) {
            if (current.getType() == EntityType.DROPPED_ITEM) {
                current.remove();
            }
        }


        // Run player teleportation and chest spawning on the main thread.
        playerHandler.teleportPlayers();
        chestSpawningHandler.spawnChestPlatforms();

        // Run the events asynchronously.
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            new FreezePlayersEvent(plugin, 5, false);
            new CountDownEvent(plugin, 5, Sound.BLOCK_NOTE_BLOCK_PLING);
            // Reset all chests
            chestHandler.resetChests();
            configManager.giveStartingItems();
        });

        // Schedule the start game effects on the main thread after 5 seconds (5 * 20 ticks).
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            new GiveStartGameEffectsEvent(plugin, 12);
        }, 5 * 20);

        return true;
    }
}
